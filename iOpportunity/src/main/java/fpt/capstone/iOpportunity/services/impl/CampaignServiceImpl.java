package fpt.capstone.iOpportunity.services.impl;


import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.campaign.CampaignDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.campaign.CampaignResponse;
import fpt.capstone.iOpportunity.model.CoOppRelation;
import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.campaign.*;
import fpt.capstone.iOpportunity.repositories.CoOppRelationRepository;
import fpt.capstone.iOpportunity.repositories.OpportunityRepository;
import fpt.capstone.iOpportunity.repositories.campaign.*;
import fpt.capstone.iOpportunity.repositories.specification.SpecificationsBuilder;
import fpt.capstone.iOpportunity.services.CampaignService;
import fpt.capstone.iOpportunity.services.OpportunityClientService;
import fpt.capstone.proto.contact.ContactDtoProto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fpt.capstone.iOpportunity.services.impl.PriceBookServiceImpl.listToPage;
import static fpt.capstone.iOpportunity.util.AppConst.SEARCH_SPEC_OPERATOR;


@Service
@AllArgsConstructor
@Slf4j
public class CampaignServiceImpl implements CampaignService {
    private final CampaignRepository campaignRepository;
    private final CampaignStatusRepository campaignStatusRepository;
    private final CampaignTypeRepository campaignTypeRepository;
    private final Converter converter;
    private final SearchCampaignRepository searchRepository;
    private final LeadMemberRepository leadMemberRepository;
    private final ContactMemberRepository contactMemberRepository;
    private final CoOppRelationRepository coOppRelationRepository;
    private final OpportunityRepository opportunityRepository;
    private final OpportunityClientService opportunityClientService;



    @Override
    public PageResponse<?> getListCampaigns(int pageNo, int pageSize) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.DESC, "editDate"));

        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sorts));


        Page<Campaign> campaigns = campaignRepository.findAll(pageable);
        return converter.convertToPageResponse(campaigns, pageable);
    }

    @Override
    public CampaignResponse getDetailCampaign(Long id) {
        try{
            Campaign campaign = campaignRepository.findById(id).orElseThrow();
            CampaignResponse campaignResponse = converter.entityToCampaignResponse(campaign);
            //Leads in Campaign
            Specification<LeadMember> spec = new Specification<LeadMember>() {
                @Override
                public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("campaignId"), id));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
            campaignResponse.setLeadsInCampaign(leadMembers.size());

            //Contacts in Campaign
            Specification<ContactMember> spec1 = new Specification<ContactMember>() {
                @Override
                public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("campaignId"), id));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactMember> contactMembers = contactMemberRepository.findAll(spec1);
            campaignResponse.setContactsInCampaign(contactMembers.size());

            List<Opportunity> opportunityList = new ArrayList<>();
            List<Opportunity> opportunityWonList = new ArrayList<>();

            BigDecimal value = BigDecimal.valueOf(0);
            BigDecimal valueWon = BigDecimal.valueOf(0);
            for(ContactMember contact : contactMembers){
                List<CoOppRelation> list = coOppRelationRepository.getListOpportunityByContact(contact.getContactId());
                    for (CoOppRelation relation : list) {
                        Opportunity opportunity = opportunityRepository.findById(relation.getOpportunityId()).orElse(null);
                        if(opportunity != null && !opportunityList.contains(opportunity)){
                            opportunityList.add(opportunity);
                            value.add(opportunity.getAmount()==null? BigDecimal.valueOf(0) :opportunity.getAmount());
                            if (opportunity.getStage().getStageName().equals("Closed Won")){
                                opportunityWonList.add(opportunity);
                                valueWon.add(opportunity.getAmount()==null? BigDecimal.valueOf(0) :opportunity.getAmount());
                            }
                        }
                    }
            }
            //Opportunities in Campaign
            campaignResponse.setOpportunityInCampaign(opportunityList.size());
            //Value Opportunities in Campaign
            campaignResponse.setValueOpportunityInCampaign(value);
            //Won Opportunities in Campaign
            campaignResponse.setWonOpportunityInCampaign(opportunityWonList.size());
            //Value Won Opportunities in Campaign
            campaignResponse.setValueWonOpportunityInCampaign(valueWon);
            //Created By
            campaignResponse.setCreatedBy(campaign.getCreatedBy());
            campaignResponse.setCreateDate(campaign.getCreateDate());
            //Last Modified By
            campaignResponse.setEdit_By(campaign.getEditBy());
            campaignResponse.setEditDate(campaign.getEditDate());
            return campaignResponse;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Long createCampaign(String userId,CampaignDTO campaignDTO) {
        try{
            Campaign campaign = converter.convertDTOToCampaign(campaignDTO);
            campaign.setCreatedBy(userId);
            campaign.setEditBy(userId);
            campaign.setEditDate(LocalDateTime.now());
            campaign.setCreateDate(LocalDateTime.now());
            campaignRepository.save(campaign);
            return campaign.getCampaignId();
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean patchCampaign(String userId,Long id,CampaignDTO campaignDTO) {
        try {
            Map<String, Object> patchMap = getPatchData(campaignDTO);
            if (patchMap.isEmpty()) {
                return true;
            }

            Campaign campaign = campaignRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Cannot find Campaign with id: " + id));

            if (campaign != null) {
                for (Map.Entry<String, Object> entry : patchMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    Field fieldDTO = ReflectionUtils.findField(CampaignDTO.class, key);

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
                        case "campaignStatus":
                            campaign.setCampaignStatus(campaignStatusRepository.findById((Long) value).orElse(null));
                            break;
                        case "campaignType":
                            campaign.setCampaignType(campaignTypeRepository.findById((Long) value).orElse(null));
                            break;
                        default:
                            if (fieldDTO.getType().isAssignableFrom(value.getClass())) {
                                Field field = ReflectionUtils.findField(Campaign.class, fieldDTO.getName());
                                assert field != null;
                                field.setAccessible(true);
                                ReflectionUtils.setField(field, campaign, value);
                            } else {
                                return false;
                            }
                    }
                }
                campaign.setEditDate(LocalDateTime.now());
                campaign.setEditBy(userId);
                campaignRepository.save(campaign);
                return true;
            }
            return false;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean patchListCampaign(String userId,Long[] id,CampaignDTO campaignDTO) {
        if (id != null) {
            List<Campaign> opportunityList = new ArrayList<>();
            try {
                for (long i : id) {
                    campaignRepository.findById(i).ifPresent(opportunityList::add);
                }
                boolean checked;
                for (Campaign l : opportunityList) {
                    checked = patchCampaign(userId,l.getCampaignId(), campaignDTO);
                    if (!checked) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean deleteCampaign(String userId,Long[] campaignId) {
        try{
            for (Long id: campaignId){
                Campaign campaign = campaignRepository.findById(id).orElse(null);
                if(campaign==null|| !Objects.equals(campaign.getCreatedBy(), userId))
                    throw new RuntimeException("Can not delete that campaign you did not create");

                Specification<LeadMember> spec = new Specification<LeadMember>() {
                    @Override
                    public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), id));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
                leadMemberRepository.deleteAll(leadMembers);

                Specification<ContactMember> spec2 = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), id));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ContactMember> contactMembers = contactMemberRepository.findAll(spec2);
                contactMemberRepository.deleteAll(contactMembers);

                campaignRepository.deleteById(id);
            }
            return true ;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> filterCampaigns(Pageable pageable, String[] search) {
        SpecificationsBuilder builder = new SpecificationsBuilder();

        if (search != null) {
            Pattern pattern = Pattern.compile(SEARCH_SPEC_OPERATOR);
            for (String l : search) {
                Matcher matcher = pattern.matcher(l);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }
            Page<Campaign> page = searchRepository.searchUserByCriteriaWithJoin(builder.params, pageable);
            return converter.convertToPageResponse(page, pageable);
        }
        return getListCampaigns(pageable.getPageNumber(), pageable.getPageSize());
    }

    @Override
    public List<CampaignStatus> getListStatus() {
        return campaignStatusRepository.findAll();
    }

    @Override
    public List<CampaignType> getCampaignTypeList() {
        return campaignTypeRepository.findAll();
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
}
