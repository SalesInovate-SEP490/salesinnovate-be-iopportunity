package fpt.capstone.iOpportunity.services.impl;

import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.*;
import fpt.capstone.iOpportunity.dto.response.*;
import fpt.capstone.iOpportunity.dto.response.campaign.ViewLeadMembers;
import fpt.capstone.iOpportunity.model.*;
import fpt.capstone.iOpportunity.model.campaign.Campaign;
import fpt.capstone.iOpportunity.model.campaign.CampaignInfluence;
import fpt.capstone.iOpportunity.model.campaign.ContactMember;
import fpt.capstone.iOpportunity.repositories.*;
import fpt.capstone.iOpportunity.repositories.OpportunityProductRepository;
import fpt.capstone.iOpportunity.repositories.campaign.CampaignInfluenceRepository;
import fpt.capstone.iOpportunity.repositories.campaign.CampaignRepository;
import fpt.capstone.iOpportunity.repositories.campaign.ContactMemberRepository;
import fpt.capstone.iOpportunity.repositories.specification.SpecificationsBuilder;
import fpt.capstone.iOpportunity.services.OpportunityClientService;
import fpt.capstone.iOpportunity.services.OpportunityService;
import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.contact.ContactDtoProto;
import fpt.capstone.proto.lead.LeadDtoProto;
import fpt.capstone.proto.user.UserDtoProto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fpt.capstone.iOpportunity.services.impl.PriceBookServiceImpl.listToPage;
import static fpt.capstone.iOpportunity.util.AppConst.SEARCH_SPEC_OPERATOR;

@Slf4j
@Service
@AllArgsConstructor
public class OpportunityServiceImpl implements OpportunityService {
    //    private static final Logger log = LoggerFactory.getLogger(OpportunityServiceImpl.class);
    private final OpportunityRepository opportunityRepository;
    private final Converter converter;
    private final ForecastRepository forecastRepository;
    private final StageRepository stageRepository;
    private final TypeRepository typeRepository;
    private final OpportunityClientService opportunityClientService;
    private final CoOppRelationRepository coOppRelationRepository;
    private final LeadSourceRepository leadSourceRepository;
    private final SearchRepository searchRepository;
    private final PriceBookRepository priceBookRepository;
    private final ProductRepository productRepository;
    private final OpportunityProductRepository opportunityProductRepository;
    private final ProductPriceBookRepository productPriceBookRepository;
    private final ContactRoleRepository contactRoleRepository;
    private final ContactMemberRepository contactMemberRepository;
    private final CampaignInfluenceRepository campaignInfluenceRepository;
    private final CampaignRepository campaignRepository;
    private final OpportunityUserRepository opportunityUserRepository;



    @Override
    public Long createOpportunity(String userId,OpportunityDTO opportunityDTO) {
        Opportunity opportunity = converter.DTOToOpportunity(opportunityDTO);
        if (opportunity.getForecast() == null) {
            throw new RuntimeException("Forecast Category not existed");
        } else if (opportunity.getStage() == null) {
            throw new RuntimeException("Stage not existed");
        }
        opportunity.setUserId(userId);
        opportunity.setCreateBy(userId);
        opportunity.setCreateDate(LocalDateTime.now());
        opportunity.setLastModifiedBy(userId);
        opportunity.setEditDate(LocalDateTime.now());
        opportunity.setIsDeleted(false);
        opportunityRepository.save(opportunity);
        //Them quan he giua opportunity va user
        OpportunityUser user = OpportunityUser.builder()
                .opportunityId(opportunity.getOpportunityId())
                .userId(userId)
                .build();
        opportunityUserRepository.save(user);
        return opportunity.getOpportunityId();
    }


    @Override
    public boolean deleteOpportunity(String userId,Long id) {
        Optional<Opportunity> opportunityOptional = opportunityRepository.findById(id);
        if (!opportunityOptional.isPresent()) {
            throw new RuntimeException("Opportunity not existed");
        }
        Opportunity existedOpportunity = opportunityOptional.get();
        existedOpportunity.setIsDeleted(true);
        //Kiem tra quan he
        checkRelationOppAndUser(userId,id);

        //Xoa quan he voi contact
        Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
            @Override
            public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("opportunityId"), id));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        List<CoOppRelation> list = coOppRelationRepository.findAll(spec);
        for (CoOppRelation relation : list){
            if(!deleteContactRole(relation.getContactId(),id)) throw new RuntimeException("Can not delete relation between opportunity and contact");
        }

        //Xoa quan he voi product
        opportunityRepository.save(existedOpportunity);
        return true;
    }

    @Override
    public OpportunityResponse getDetailOpportunity(Long id) {
        Opportunity opportunityOptional = opportunityRepository.findById(id).orElse(null);
        if (opportunityOptional == null || opportunityOptional.getIsDeleted()) {
            throw new RuntimeException("Opportunity not existed");
        }
        return converter.entityToOpportunityResponse(opportunityOptional);

    }

    @Override
    public PageResponse<?> getListOpportunity(String userId,int pageNo, int pageSize) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));

        Specification<Opportunity> spec = new Specification<Opportunity>() {
            @Override
            public Predicate toPredicate(Root<Opportunity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                Join<Opportunity, Users> join = root.join("users", JoinType.INNER);
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        Page<Opportunity> opportunities = opportunityRepository.findAll(spec, pageable);
        return converter.convertToPageResponse(opportunities, pageable);
    }

    @Override
    public List<ForecastDTO> getListForecastCategory() {
        List<Forecast> listForecast = forecastRepository.findAll();
        return listForecast.stream().map(converter::entityToForecastDTO).toList();
    }

    @Override
    public List<StageDTO> getListStage() {
        List<Stage> listStage = stageRepository.findAll(Sort.by(Sort.Direction.ASC, "index"));
        return listStage.stream().map(converter::entityToStageDTO).toList();
    }

    @Override
    public List<TypeDTO> getListType() {
        List<Type> listType = typeRepository.findAll();
        return listType.stream().map(converter::entityToTypeDTO).toList();
    }

    @Override
    public List<LeadSourceDTO> getLeadSource() {
        List<LeadSource> leadSources = leadSourceRepository.findAll();
        return leadSources.stream().map(converter::entityToLeadSourceDTO).toList();
    }

    @Override
    public PageResponse<?> getListOpportunityByAccount(int pageNo, int pageSize, long accountId) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Specification<Opportunity> spec = new Specification<Opportunity>() {
            @Override
            public Predicate toPredicate(Root<Opportunity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
                predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        Page<Opportunity> opportunities = opportunityRepository.findAll(spec, pageable);
        return converter.convertToPageResponse(opportunities, pageable);
    }

    @Override
    public PageResponse<?> getListOpportunityByContact(int pageNo, int pageSize, long contactId) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "createDate"));

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        List<CoOppRelation> list = coOppRelationRepository.getListOpportunityByContact(contactId);
        if (!list.isEmpty()) {
            List<Opportunity> opportunities = new ArrayList<>();
            for (CoOppRelation relation : list) {
                opportunities.add(opportunityRepository.findById(relation.getOpportunityId()).orElse(null));
            }
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), opportunities.size());

            Page<Opportunity> opportunitiesPage = new PageImpl<>(opportunities.subList(start, end), pageable, opportunities.size());
            return converter.convertToPageResponse(opportunitiesPage, pageable);
        }
        return null;
    }

    @Override
    @Transactional
    public Long convertNewOpportunity(String userId,ConvertFromLeadDTO dto) {
        try {
            LeadDtoProto proto = opportunityClientService.getLead(dto.getLeadId());
            LeadSource leadSource = leadSourceRepository.findById(proto.getSource().getLeadSourceId()).orElse(null);
            Opportunity opportunity = Opportunity.builder()
                    .opportunityName(dto.getOpportunityName())
                    .accountId(dto.getAccountId())
                    .probability(10F)
                    .leadSource(leadSource)
                    .forecast(forecastRepository.findById(1L).orElse(null))
                    .stage(stageRepository.findById(1L).orElse(null))
                    .type(typeRepository.findById(1L).orElse(null))
                    .userId(userId)
                    .createBy(userId)
                    .createDate(LocalDateTime.now())
                    .lastModifiedBy(userId)
                    .editDate(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            opportunityRepository.save(opportunity);
            //Them quan he giua nguoi convert voi opportunity
            OpportunityUser user = OpportunityUser.builder()
                    .opportunityId(opportunity.getOpportunityId())
                    .userId(userId)
                    .build();

            //Them quan he giua user voi opportunity
            for (OpportunityUserDTO userDTO : dto.getUserDTOS()) {
                Specification<OpportunityUser> spec = new Specification<OpportunityUser>() {
                    @Override
                    public Predicate toPredicate(Root<OpportunityUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunity.getOpportunityId()));
                        predicates.add(criteriaBuilder.equal(root.get("userId"), userDTO.getUserId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                if (opportunityUserRepository.exists(spec)) continue;
                OpportunityUser contactsUser = OpportunityUser.builder()
                        .opportunityId(opportunity.getOpportunityId())
                        .userId(userDTO.getUserId())
                        .build();
                opportunityUserRepository.save(contactsUser);
            }

            //Thêm quan hệ giua account và contact
            CoOppRelation coOppRelation = CoOppRelation.builder()
                    .opportunityId(opportunity.getOpportunityId())
                    .contactId(dto.getContactId())
                    .build();
            Specification<CoOppRelation> spec1 = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), coOppRelation.getOpportunityId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList1 = coOppRelationRepository.findAll(spec1);
            if(coOppRelationList1.isEmpty()) coOppRelation.setPrimary(true);
            coOppRelationRepository.save(coOppRelation);

            //Lien ket opportunity voi campaign (day la new opportunity vay nen se khong co quan he giua contact va opportunity)
            Specification<ContactMember> spec = new Specification<ContactMember>() {
                @Override
                public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactMember> members = contactMemberRepository.findAll(spec);
            for (ContactMember contactMember : members){
                CampaignInfluence campaignInfluence = CampaignInfluence.builder()
                        .campaignId(contactMember.getCampaignId())
                        .opportunityContactId(coOppRelation.getCooppIdId())
                        .build();
                campaignInfluenceRepository.save(campaignInfluence);
            }

            boolean checkLogCall = opportunityClientService.convertLogCallToOpp(
                    dto.getLeadId(),opportunity.getOpportunityId());
            if(!checkLogCall) throw new RuntimeException("Can not convert log call from lead to opportunity");
            boolean checkLogEmail = opportunityClientService.convertLogEmailToOpp(
                    dto.getLeadId(),opportunity.getOpportunityId());
            if(!checkLogEmail) throw new RuntimeException("Can not convert log call from lead to opportunity");

            return opportunity.getOpportunityId();
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @Override
    public Long convertExistOpportunity(String userId,long leadId,long contactId, long opportunityId,List<OpportunityUserDTO> userDTOS) {
        try {
            //Thêm quan hệ giữa  account và contact
            List<CoOppRelation> list = coOppRelationRepository.countNumRelation(opportunityId, contactId);
            if (list.size() <= 0) {
                CoOppRelation coOppRelation = CoOppRelation.builder()
                        .opportunityId(opportunityId)
                        .contactId(contactId)
                        .build();
                Specification<CoOppRelation> spec1 = new Specification<CoOppRelation>() {
                    @Override
                    public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), coOppRelation.getOpportunityId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<CoOppRelation> coOppRelationList1 = coOppRelationRepository.findAll(spec1);
                if(coOppRelationList1.isEmpty()) coOppRelation.setPrimary(true);
                coOppRelationRepository.save(coOppRelation);

                //Them quan he giua nguoi convert voi opportunity
                OpportunityUser user = OpportunityUser.builder()
                        .opportunityId(opportunityId)
                        .userId(userId)
                        .build();

                //Them quan he giua user voi contact
                for (OpportunityUserDTO userDTO : userDTOS) {
                    Specification<OpportunityUser> spec = new Specification<OpportunityUser>() {
                        @Override
                        public Predicate toPredicate(Root<OpportunityUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                            predicates.add(criteriaBuilder.equal(root.get("userId"), userDTO.getUserId()));
                            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                        }
                    };
                    if (opportunityUserRepository.exists(spec)) continue;
                    OpportunityUser contactsUser = OpportunityUser.builder()
                            .opportunityId(opportunityId)
                            .userId(userDTO.getUserId())
                            .build();
                    opportunityUserRepository.save(contactsUser);
                }

                //Lien ket opportunity voi campaign (day la new opportunity vay nen se khong co quan he giua contact va opportunity)
                Specification<ContactMember> spec = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ContactMember> members = contactMemberRepository.findAll(spec);
                for (ContactMember contactMember : members){
                    CampaignInfluence campaignInfluence = CampaignInfluence.builder()
                            .campaignId(contactMember.getCampaignId())
                            .opportunityContactId(coOppRelation.getCooppIdId())
                            .build();
                    campaignInfluenceRepository.save(campaignInfluence);
                }

                boolean checkLogCall = opportunityClientService.convertLogCallToOpp(
                        leadId,opportunityId);
                if(!checkLogCall) throw new RuntimeException("Can not convert log call from lead to opportunity");

                return opportunityId;
            }
            throw new RuntimeException("Can not get Opportunity");
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public boolean patchOpportunity(String userId,OpportunityDTO opportunityDTO, long id) {
        try{
            checkRelationOppAndUser(userId,id);

            Map<String, Object> patchMap = getPatchData(opportunityDTO);
            if (patchMap.isEmpty()) {
                return true;
            }

            Opportunity opportunity = opportunityRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find Opportunity with id: " + id));

            if (opportunity != null) {
                for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Field fieldDTO = ReflectionUtils.findField(OpportunityDTO.class, key);

                    if (fieldDTO == null) {
                        continue;
                    }

                    fieldDTO.setAccessible(true);
                    Class<?> type = fieldDTO.getType();

                    try {
                        if (type == long.class && value instanceof String) {
                            value = Long.parseLong((String) value);
                        } else if (type == Long.class && value instanceof String) {
                            value = Long.valueOf((String) value);
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }

                    switch (key) {
                        case "forecast":
                            opportunity.setForecast(forecastRepository.findById((Long) value).orElse(null));
                            break;
                        case "stage":
                            Stage stage = stageRepository.findById((Long) value).orElse(null);
                            opportunity.setStage(stage);
                            assert stage != null;
                            opportunity.setProbability(stage.getProbability());
                            break;
                        case "type":
                            opportunity.setType(typeRepository.findById((Long) value).orElse(null));
                            break;
                        case "leadSource":
                            opportunity.setLeadSource(leadSourceRepository.findById((Long) value).orElse(null));
                            break;
                        default:
                            if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                                Field field = ReflectionUtils.findField(Opportunity.class, fieldDTO.getName());
                                assert field != null;
                                field.setAccessible(true);
                                ReflectionUtils.setField(field, opportunity, value);
                            } else {
                                return false;
                            }
                    }
                }
                opportunity.setEditDate(LocalDateTime.now());
                opportunityRepository.save(opportunity);

                //Thêm xử lý notification cho patch lead
                List<String> listUser = new ArrayList<>();
                Specification<OpportunityUser> spec1 = new Specification<OpportunityUser>() {
                    @Override
                    public Predicate toPredicate(Root<OpportunityUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), id));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<OpportunityUser> leadUsers = opportunityUserRepository.findAll(spec1);
                for(OpportunityUser user : leadUsers){
                    listUser.add(user.getUserId());
                }
                opportunityClientService.createNotification(userId,"The Opportunity you were assigned has been updated."
                        ,id,1L,listUser);
                return true;
            }
            return false;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean patchListOpportunity(String userId,Long[] id, OpportunityDTO opportunityDTO) {
        if (id != null) {
            List<Opportunity> opportunityList = new ArrayList<>();
            try {
                for (long i : id) {
                    opportunityRepository.findById(i).ifPresent(opportunityList::add);
                }
                boolean checked;
                for (Opportunity l : opportunityList) {
                    checkRelationOppAndUser(userId,l.getOpportunityId());
                    checked = patchOpportunity(userId,opportunityDTO, l.getOpportunityId());
                    if (!checked) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public PageResponse<?> filterOpportunity(String userId,Pageable pageable, String[] search) {
        SpecificationsBuilder builder = new SpecificationsBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
            for (String l : search) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }

            Page<Opportunity> page = searchRepository.searchUserByCriteriaWithJoin(userId,builder.params, pageable);
            return converter.convertToPageResponse(page, pageable);
        }
        return getListOpportunity(userId,pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    @Transactional
    public Boolean addPricebook(String userId,long opportunityId, long pricebookId) {
        try {
            Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
            PriceBook priceBook = priceBookRepository.findById(pricebookId).orElse(null);
            if (opportunity == null || priceBook == null) return false;
            if (priceBook.getIsActive() == 0) return false;
            if(opportunity.getPriceBook()== null || opportunity.getPriceBook()!=pricebookId){
                checkRelationOppAndUser(userId,opportunityId);
                opportunity.setPriceBook(priceBook.getPriceBookId());
                opportunityRepository.save(opportunity);
                Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                    @Override
                    public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<OpportunityProduct> list = opportunityProductRepository.findAll(spec);
                opportunityProductRepository.deleteAll(list);
                return true;
            }
            throw new RuntimeException("Can not set another pricebook");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Integer countProduct(long opportunity) {
        try {
            Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunity));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> list = opportunityProductRepository.findAll(spec);
            return list.size();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Boolean addProductToOpportunity(String userId,OpportunityPriceBookProductDTO dto) {
        try {
            Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId()).orElse(null);
            PriceBook priceBook = priceBookRepository.findById(opportunity.getPriceBook()).orElse(null);

            if (opportunity == null || priceBook == null) return false;

            checkRelationOppAndUser(userId,opportunity.getOpportunityId());
            for (OpportunityProductDTO opportunityProductDTO : dto.getOpportunityProductDTOS()) {

                Specification<ProductPriceBook> spec = new Specification<ProductPriceBook>() {
                    @Override
                    public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("productId"), opportunityProductDTO.getProductId()));
                        predicates.add(criteriaBuilder.equal(root.get("priceBookId"), priceBook.getPriceBookId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ProductPriceBook> list = productPriceBookRepository.findAll(spec);
                if(!list.isEmpty()){
                    OpportunityProduct opportunityProduct = OpportunityProduct.builder()
                            .opportunityId(opportunity.getOpportunityId())
                            .productId(opportunityProductDTO.getProductId())
                            .sales_price(opportunityProductDTO.getSales_price())
                            .quantity(opportunityProductDTO.getQuantity())
                            .date(opportunityProductDTO.getDate())
                            .line_description(opportunityProductDTO.getLine_description())
                            .currency(list.get(0).getCurrency().getId())
                            .build();

                    opportunityProductRepository.save(opportunityProduct);
                }else return false ;
            }

            //Update lại cho amount
            updateAmountForOpp(opportunity.getOpportunityId());

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public OpportunityPriceBookProductResponse getProduct(long opportunityId) {
        try {
            Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
            PriceBook priceBook = priceBookRepository.findById(opportunity.getPriceBook()).orElse(null);

            if (opportunity == null || priceBook == null)
                throw new EntityNotFoundException("cannot find product or pricebook");

            Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> list = opportunityProductRepository.findAll(spec);
            OpportunityPriceBookProductResponse response = OpportunityPriceBookProductResponse.builder()
                    .opportunity(opportunity)
                    .priceBook(priceBook)
                    .products(list.stream().map(converter::OppproToOppproResponse).toList())
                    .build();
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<PriceBookResponse> searchPriceBookToAdd(long opportunityId, String search) {
        try {
            Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
            if (opportunity == null)
                throw new EntityNotFoundException("Can not find Opportunity with Id" + opportunityId);
            if (opportunity.getPriceBook() == null) {
                Specification<PriceBook> specProduct = new Specification<PriceBook>() {
                    @Override
                    public Predicate toPredicate(Root<PriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.like(root.get("priceBookName"), "%" + search + "%"));
                        predicates.add(criteriaBuilder.equal(root.get("isActive"), 1));

                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<PriceBook> listSearchProduct = priceBookRepository.findAll(specProduct);
                return listSearchProduct.stream().map(converter::entityToPriceBookResponse).toList();
            } else {
                Specification<PriceBook> specProduct = new Specification<PriceBook>() {
                    @Override
                    public Predicate toPredicate(Root<PriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.like(root.get("priceBookName"), "%" + search + "%"));
                        predicates.add(criteriaBuilder.equal(root.get("isActive"), 1));
                        predicates.add(criteriaBuilder.notEqual(root.get("priceBookId"), opportunity.getPriceBook()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<PriceBook> listSearchProduct = priceBookRepository.findAll(specProduct);
                return listSearchProduct.stream().map(converter::entityToPriceBookResponse).toList();
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ProductPriceBookResponse> searchProductToAdd(long opportunityId, String search) {
        try {
            Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
            PriceBook priceBook = priceBookRepository.findById(opportunity.getPriceBook()).orElse(null);

            if (opportunity == null)
                throw new EntityNotFoundException("cannot find opportunity");
            if (priceBook == null)
                return null;
            Specification<ProductPriceBook> spec = new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), priceBook.getPriceBookId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> listProductPriceBooks = productPriceBookRepository.findAll(spec);
            List<Product> productList = getListProductByProductPriceBook(listProductPriceBooks);

            Specification<Product> specProduct = new Specification<Product>() {
                @Override
                public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.notLike(root.get("productName"), "%" + search + "%"));
                    predicates.add(criteriaBuilder.notEqual(root.get("isActive"), 1));
                    return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
                }
            };
            List<Product> listSearchProduct = productRepository.findAll(specProduct);

            Specification<OpportunityProduct> specOppProduct = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> listOppProduct = opportunityProductRepository.findAll(specOppProduct);
            List<Product> productOppList = getListProductByOppProduct(listOppProduct);

            if (productList != null) {
                productList.removeAll(listSearchProduct);
                productList.removeAll(productOppList);
            }
            return convertListProductToOppProduct(productList, priceBook.getPriceBookId());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteProduct(String userId,long opportunityId, long productId) {
        try{
            checkRelationOppAndUser(userId,opportunityId);
            Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    predicates.add(criteriaBuilder.equal(root.get("productId"), productId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> listProductPriceBooks = opportunityProductRepository.findAll(spec);
            if (listProductPriceBooks.isEmpty()) return false;
            else{
                opportunityProductRepository.delete(listProductPriceBooks.get(0));
                updateAmountForOpp(opportunityId);
                return true ;
            }
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getCampaignInfluence(long opportunityId, int pageNo, int pageSize) {
        try{
            List<CampaignInfluenceResponse> list = new ArrayList<>();
            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
            for (CoOppRelation relation : coOppRelationList){
                Specification<CampaignInfluence> spec1 = new Specification<CampaignInfluence>() {
                    @Override
                    public Predicate toPredicate(Root<CampaignInfluence> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityContactId"), relation.getCooppIdId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<CampaignInfluence> campaignInfluenceList = campaignInfluenceRepository.findAll(spec1);
                for (CampaignInfluence influence : campaignInfluenceList){
                    ContactDtoProto proto = opportunityClientService.getContact(relation.getContactId());
                    CampaignInfluenceResponse response = CampaignInfluenceResponse.builder()
                            .campaignInfluenceId(influence.getCampaignInfluenceId())
                            .campaign(campaignRepository.findById(influence.getCampaignId()).orElse(null))
                            .influence(influence.getInfluence())
                            .revenueShare(influence.getRevenueShare())
                            .contactId(proto.getContactId())
                            .contactName(proto.getLastName()+" "+proto.getFirstName())
                            .coOppRelation(relation)
                            .build();
                    list.add(response);
                }
            }
            int page = 0;
            if (pageNo > 0) {
                page = pageNo - 1;
            }
            Page<CampaignInfluenceResponse> pageLeadMembers = listToPage(list, page, pageSize);
            Pageable pageable = PageRequest.of(page, pageSize);
            return converter.convertToPageResponse(pageLeadMembers, pageable);
        }catch (Exception e){
            throw new RuntimeException("Your operation caused an unknown error");
        }
    }

    @Override
    public boolean patchCampaignInfluence(String userId,List<CampaignInfluenceDTO> list) {
        try{
            for (CampaignInfluenceDTO dto :list){
                Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId()).orElse(null);
                if (opportunity==null) throw new RuntimeException("Can not ger opportunity");
                //check relation with user and oppor
                checkRelationOppAndUser(userId,opportunity.getOpportunityId());

                CampaignInfluence influence = campaignInfluenceRepository.findById(dto.getCampaignInfluenceId()).orElse(null);
                if (influence == null) throw new RuntimeException("Can not get relation between opportunity and campaign");
                if(opportunity.getAmount().compareTo(BigDecimal.ZERO) == 0) throw new RuntimeException("Can not update for campaign influence");
                influence.setRevenueShare(dto.getRevenueShare());
                BigDecimal percentage = dto.getRevenueShare().divide(opportunity.getAmount(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                influence.setInfluence(percentage);
                campaignInfluenceRepository.save(influence);
            }
            return true ;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getListContactRole(int pageNo, int pageSize, Long opportunityId) {
        try{
            int page = 0;
            if (pageNo > 0) {
                page = pageNo - 1;
            }
            List<Sort.Order> sorts = new ArrayList<>();
            Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
            List<ContactRoleResponse> contactRoleResponses = new ArrayList<>();
            for (CoOppRelation relation:coOppRelationList){
                ContactDtoProto contactDtoProto = opportunityClientService.getContact(relation.getContactId());
                AccountDtoProto accountDtoProto = opportunityClientService.getAccount(contactDtoProto.getAccountId());
                ContactRoleResponse response = ContactRoleResponse.builder()
                        .contactId(contactDtoProto.getContactId())
                        .contactName(contactDtoProto.getLastName()+contactDtoProto.getFirstName())
                        .title(contactDtoProto.getTitle())
                        .phone(contactDtoProto.getPhone())
                        .accountId(accountDtoProto.getAccountId())
                        .accountName(accountDtoProto.getAccountName())
                        .coOppRelation(relation)
                        .build();
                contactRoleResponses.add(response);
            }

            Page<ContactRoleResponse> roleResponsePage = listToPage(contactRoleResponses, pageNo, pageSize);
            return converter.convertToPageResponse(roleResponsePage, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean addContactRole(String userId,CoOppRelationDTO dto) {
        try {
            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), dto.getOpportunityId()));
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
            if(!coOppRelationList.isEmpty()) {
                throw new RuntimeException("The relation between opportunity and contact have been existed");
            }

            //Kiem tra quan he cua user vaf opportunity
            checkRelationOppAndUser(userId,dto.getOpportunityId());

            //Kiem tra xem co cung accountId khong
            Opportunity opportunity = opportunityRepository.findById(dto.getOpportunityId()).orElse(null);
            ContactDtoProto contactDtoProto = opportunityClientService.getContact(dto.getContactId());
            if(opportunity==null||contactDtoProto==null||opportunity.getAccountId()!=contactDtoProto.getAccountId()){
                throw new RuntimeException("Cannot add relationship between opportunity and contact when they do not have the same account");
            }

            CoOppRelation coOppRelation = CoOppRelation.builder()
                    .opportunityId(dto.getOpportunityId())
                    .contactId(dto.getContactId())
                    .contactRole(dto.getContactRole()!=null?contactRoleRepository.findById(dto.getContactRole()).orElse(null):null)
                    .build();
            //set primary for first contact role
            Specification<CoOppRelation> spec1 = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), dto.getOpportunityId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList1 = coOppRelationRepository.findAll(spec1);
            if(coOppRelationList1.isEmpty()) coOppRelation.setPrimary(true);
            coOppRelationRepository.save(coOppRelation);

            //Add relation for campaign and opportunity
            Specification<ContactMember> spec2 = new Specification<ContactMember>() {
                @Override
                public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactMember> members = contactMemberRepository.findAll(spec2);
            for (ContactMember contactMember : members){
                CampaignInfluence campaignInfluence = CampaignInfluence.builder()
                        .campaignId(contactMember.getCampaignId())
                        .opportunityContactId(coOppRelation.getCooppIdId())
                        .build();
                campaignInfluenceRepository.save(campaignInfluence);
            }
            updateAmountForOpp(dto.getOpportunityId());
            return true;
        }catch (Exception e ){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteContactRole(Long contactId, Long opportunityId) {
        try {
            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
            if(coOppRelationList.isEmpty()) return false;

            Specification<CampaignInfluence> spec1 = new Specification<CampaignInfluence>() {
                @Override
                public Predicate toPredicate(Root<CampaignInfluence> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityContactId"), coOppRelationList.get(0).getCooppIdId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CampaignInfluence> campaignInfluenceList = campaignInfluenceRepository.findAll(spec1);
            campaignInfluenceRepository.deleteAll(campaignInfluenceList);

            coOppRelationRepository.deleteAll(coOppRelationList);
            updateAmountForOpp(opportunityId);
            return true;
        }catch (Exception e ){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean deleteContactRoleByUser(String userId, Long contactId, Long opportunityId) {
        try {
            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
            if(coOppRelationList.isEmpty()) return false;

            checkRelationOppAndUser(userId,opportunityId);

            Specification<CampaignInfluence> spec1 = new Specification<CampaignInfluence>() {
                @Override
                public Predicate toPredicate(Root<CampaignInfluence> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityContactId"), coOppRelationList.get(0).getCooppIdId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CampaignInfluence> campaignInfluenceList = campaignInfluenceRepository.findAll(spec1);
            campaignInfluenceRepository.deleteAll(campaignInfluenceList);

            coOppRelationRepository.deleteAll(coOppRelationList);
            updateAmountForOpp(opportunityId);
            return true;
        }catch (Exception e ){
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    @Transactional
    public boolean editContactRole(String userId,List<CoOppRelationDTO> list) {
        try {
            for(CoOppRelationDTO dto : list){
                Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                    @Override
                    public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), dto.getOpportunityId()));
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
                if(coOppRelationList.isEmpty()) return false;
                checkRelationOppAndUser(userId, dto.getOpportunityId());
                coOppRelationList.get(0).setContactRole(
                        contactRoleRepository.findById(dto.getContactRole()).orElse(null));
                coOppRelationRepository.save(coOppRelationList.get(0));
            }
            return true;
        }catch (Exception e ){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean setPrimary(String userId,CoOppRelationDTO dto) {
        try{
            Specification<CoOppRelation> spec = new Specification<CoOppRelation>() {
                @Override
                public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), dto.getOpportunityId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CoOppRelation> coOppRelationList = coOppRelationRepository.findAll(spec);
            if(coOppRelationList.isEmpty()) return false;
            checkRelationOppAndUser(userId,dto.getOpportunityId());
            for (CoOppRelation relation : coOppRelationList){
                relation.setPrimary(Objects.equals(relation.getContactId(), dto.getContactId()));
                coOppRelationRepository.save(relation);
            }
            return true;
        }catch (Exception e){
           throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<ContactRole> getListRole() {
        return contactRoleRepository.findAll();
    }

    @Override
    public boolean addUserToOpportunity(String userId, Long opportunityId, List<OpportunityUserDTO> userDTOS) {
        try {
            Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
            if (opportunity == null) throw new RuntimeException("Cannot find opportunity");
            checkRelationOppAndUser(userId,opportunityId);
            List<String> listUser = new ArrayList<>();
            for (OpportunityUserDTO dto : userDTOS) {
                Specification<Opportunity> spec = new Specification<Opportunity>() {
                    @Override
                    public Predicate toPredicate(Root<Opportunity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        Join<Opportunity, Users> join = root.join("users", JoinType.INNER);
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(join.get("userId"), dto.getUserId()));
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                boolean existed = opportunityRepository.exists(spec);
                if (existed) continue;
                OpportunityUser contactsUser = OpportunityUser.builder()
                        .userId(dto.getUserId())
                        .opportunityId(dto.getOpportunityId())
                        .build();
                opportunityUserRepository.save(contactsUser);
                listUser.add(dto.getUserId());
            }
            opportunityClientService.createNotification(userId,"You have been assigned to the new Opportunity."
                    ,opportunityId,1L,listUser);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public List<UserResponse> getListUserInOpportunity(Long opportunityId) {
        try {
            List<UserResponse> responses = new ArrayList<>();
            Optional<Opportunity> accounts = opportunityRepository.findById(opportunityId);
            if (accounts.isEmpty()) throw new RuntimeException("Can not find leads");
            Specification<OpportunityUser> spec = new Specification<OpportunityUser>() {
                @Override
                public Predicate toPredicate(Root<OpportunityUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityUser> users = opportunityUserRepository.findAll(spec);
            for (OpportunityUser user : users) {
                UserDtoProto proto = opportunityClientService.getUser(user.getUserId());
                if (proto.getUserId().isEmpty()) continue;
                UserResponse userResponse = UserResponse.builder()
                        .userId(proto.getUserId())
                        .userName(proto.getUserName())
                        .firstName(proto.getFirstName())
                        .lastName(proto.getLastName())
                        .email(proto.getEmail())
                        .build();
                responses.add(userResponse);
            }
            return responses;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public boolean assignUserFollowingAccount(Long accountId, List<String> listUsers) {
        try{
            Specification<Opportunity> spec = new Specification<Opportunity>() {
                @Override
                public Predicate toPredicate(Root<Opportunity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("accountId"), accountId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<Opportunity> opportunities = opportunityRepository.findAll(spec);
            for(Opportunity opportunity : opportunities){
                for (String userId : listUsers) {
                    Specification<Opportunity> spec2 = new Specification<Opportunity>() {
                        @Override
                        public Predicate toPredicate(Root<Opportunity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            Join<Opportunity, Users> join = root.join("users", JoinType.INNER);
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(criteriaBuilder.equal(join.get("userId"), userId));
                            predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunity.getOpportunityId()));
                            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                        }
                    };
                    boolean existed = opportunityRepository.exists(spec2);
                    if (existed) continue;
                    OpportunityUser contactsUser = OpportunityUser.builder()
                            .userId(userId)
                            .opportunityId(opportunity.getOpportunityId())
                            .build();
                    opportunityUserRepository.save(contactsUser);
                }
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public boolean patchProduct(String userId,long opportunityId, List<OpportunityProductDTO> listProducts) {
        try {
            checkRelationOppAndUser(userId,opportunityId);
            for (OpportunityProductDTO productDTO: listProducts) {
                Map<String, Object> patchMap = getPatchData(productDTO);
                if (patchMap.isEmpty()) {
                    continue;
                }
                Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                    @Override
                    public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                        predicates.add(criteriaBuilder.equal(root.get("productId"), productDTO.getProductId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<OpportunityProduct> listProductPriceBooks = opportunityProductRepository.findAll(spec);
                if (listProductPriceBooks.isEmpty()) return false;
                else {
                    OpportunityProduct product = listProductPriceBooks.get(0);
                    for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        Field fieldDTO = ReflectionUtils.findField(OpportunityProductDTO.class, key);

                        if (fieldDTO == null) {
                            continue;
                        }

                        fieldDTO.setAccessible(true);
                        Class<?> type = fieldDTO.getType();

                        try {
                            if (type == long.class && value instanceof String) {
                                value = Long.parseLong((String) value);
                            } else if (type == Long.class && value instanceof String) {
                                value = Long.valueOf((String) value);
                            }
                        } catch (NumberFormatException e) {
                            return false;
                        }
                        switch (key) {
                            case "productId":
                                break;
                            case "opportunityProductId":
                                break;
                            default:
                                if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                                    Field field = ReflectionUtils.findField(OpportunityProduct.class, fieldDTO.getName());
                                    assert field != null;
                                    field.setAccessible(true);
                                    ReflectionUtils.setField(field, product, value);
                                } else {
                                    return false;
                                }
                        }
                        opportunityProductRepository.save(product);
                    }
                }
            }
            //Update lại cho amount
           updateAmountForOpp(opportunityId);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    private Map<String, Object> getPatchData(Object obj) {
        Class<?> objClass = obj.getClass();
        Field[] fields = objClass.getDeclaredFields();
        Map<String, Object> patchMap = new HashMap<>();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    patchMap.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.info(e.getMessage(), e.getCause());
            }
        }
        return patchMap;
    }

    private List<Product> getListProductByProductPriceBook(List<ProductPriceBook> list) {
        List<Product> products = new ArrayList<>();
        for (ProductPriceBook productPriceBook : list) {
            Product product = productRepository.findById(productPriceBook.getProductId()).orElse(null);
            if (product != null) products.add(product);
        }
        return products;
    }

    private List<Product> getListProductByOppProduct(List<OpportunityProduct> listOppProduct) {
        List<Product> products = new ArrayList<>();
        for (OpportunityProduct productPriceBook : listOppProduct) {
            Product product = productRepository.findById(productPriceBook.getProductId()).orElse(null);
            if (product != null) products.add(product);
        }
        return products;
    }

    private List<ProductPriceBookResponse> convertListProductToOppProduct(List<Product> productList, Long priceBookId) {
        List<ProductPriceBookResponse> list = new ArrayList<>();
        for (Product product : productList) {
            Specification<ProductPriceBook> spec = new Specification<ProductPriceBook>() {
                @Override
                public Predicate toPredicate(Root<ProductPriceBook> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("priceBookId"), priceBookId));
                    predicates.add(criteriaBuilder.equal(root.get("productId"), product.getProductId()));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ProductPriceBook> listProductPriceBooks = productPriceBookRepository.findAll(spec);
            list.add(converter.convertToProductPriceBookResponse(listProductPriceBooks.get(0)));
        }
        return list;
    }

    private void updateAmountForOpp (Long opportunityId){
        Opportunity opportunity = opportunityRepository.findById(opportunityId).orElse(null);
        if(opportunity!=null){
            Specification<OpportunityProduct> spec = new Specification<OpportunityProduct>() {
                @Override
                public Predicate toPredicate(Root<OpportunityProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<OpportunityProduct> list = opportunityProductRepository.findAll(spec);
            BigDecimal amount = BigDecimal.valueOf(0);
            for (OpportunityProduct product : list){
                amount = amount.add(product.getSales_price().multiply(BigDecimal.valueOf(product.getQuantity()))) ;
            }
            opportunity.setAmount(amount);
            opportunityRepository.save(opportunity);
        }
    }

    private void checkRelationOppAndUser (String userId,Long opportunityId){
        Specification<OpportunityUser> spec = new Specification<OpportunityUser>() {
            @Override
            public Predicate toPredicate(Root<OpportunityUser> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("opportunityId"), opportunityId));
                predicates.add(criteriaBuilder.equal(root.get("userId"), userId));
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };
        boolean exists = opportunityUserRepository.exists(spec);
        if(!exists) throw new RuntimeException("Did not been assigned to this opportunity ");
    }
}


