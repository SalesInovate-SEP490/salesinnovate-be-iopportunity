package fpt.capstone.iOpportunity.services.impl;


import fpt.capstone.iOpportunity.dto.Converter;
import fpt.capstone.iOpportunity.dto.request.campaign.ContactMemberDTO;
import fpt.capstone.iOpportunity.dto.request.campaign.LeadMemberDTO;
import fpt.capstone.iOpportunity.dto.response.CampaignInfluenceResponse;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.ProductPriceBookResponse;
import fpt.capstone.iOpportunity.dto.response.campaign.CampaignResponse;
import fpt.capstone.iOpportunity.dto.response.campaign.InfluenceOpportunityResponse;
import fpt.capstone.iOpportunity.dto.response.campaign.ViewContactMembers;
import fpt.capstone.iOpportunity.dto.response.campaign.ViewLeadMembers;
import fpt.capstone.iOpportunity.model.CoOppRelation;
import fpt.capstone.iOpportunity.model.campaign.*;
import fpt.capstone.iOpportunity.repositories.CoOppRelationRepository;
import fpt.capstone.iOpportunity.repositories.OpportunityRepository;
import fpt.capstone.iOpportunity.repositories.campaign.*;
import fpt.capstone.iOpportunity.services.MemberService;
import fpt.capstone.iOpportunity.services.OpportunityClientService;
import fpt.capstone.proto.account.AccountDtoProto;
import fpt.capstone.proto.contact.ContactDtoProto;
import fpt.capstone.proto.lead.LeadDtoProto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static fpt.capstone.iOpportunity.services.impl.PriceBookServiceImpl.listToPage;

@Service
@AllArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {
    private final CampaignRepository campaignRepository;
    private final CampaignStatusRepository campaignStatusRepository;
    private final CampaignTypeRepository campaignTypeRepository;
    private final Converter converter;
    private final SearchCampaignRepository searchRepository;
    private final MemberStatusRepository memberStatusRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private final LeadMemberRepository leadMemberRepository;
    private final ContactMemberRepository contactMemberRepository;
    private final OpportunityClientService opportunityClientService;
    private final CoOppRelationRepository coOppRelationRepository;
    private final CampaignInfluenceRepository campaignInfluenceRepository;
    private final OpportunityRepository opportunityRepository;

    @Override
    public List<MemberStatus> getListCampaignMemberStatus() {
        try{
            List<MemberStatus> list = memberStatusRepository.findAll();
            return list;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Long createMemberStatus(String status) {
        MemberStatus memberStatus =MemberStatus.builder()
                .campaignMemberStatusName(status)
                .build();
        memberStatusRepository.save(memberStatus);
        return memberStatus.getCampaignMemberStatusId();
    }

    @Override
    public Boolean patchMemberStatus(long id, String status) {
        try{
            MemberStatus memberStatus = memberStatusRepository.findById(id).orElse(null);
            if(memberStatus ==null) return false ;
            else{
                memberStatus.setCampaignMemberStatusName(status);
                memberStatusRepository.save(memberStatus);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean deleteMemberStatus(Long[] ids) {
        try{
            for(Long id: ids){
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();
                CriteriaQuery<LeadMember> cq = cb.createQuery(LeadMember.class);
                Root<LeadMember> leadMemberRoot = cq.from(LeadMember.class);
                Join<LeadMember, MemberStatus> memberStatusJoin = leadMemberRoot.join("memberStatus");
                cq.where(cb.equal(memberStatusJoin.get("id"), id));
                List<LeadMember> leadMembers= entityManager.createQuery(cq).getResultList();
                // Get list ContactMember
                CriteriaQuery<ContactMember> builder = cb.createQuery(ContactMember.class);
                Root<ContactMember> root = builder.from(ContactMember.class);
                Join<ContactMember, MemberStatus> statusRoot = root.join("memberStatus");
                builder.where(cb.equal(statusRoot.get("id"), id));
                List<ContactMember> contactMembers= entityManager.createQuery(builder).getResultList();
                if(leadMembers.isEmpty() && contactMembers.isEmpty()){
                    memberStatusRepository.deleteById(id);
                }else return false ;
            }
            return true ;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean addLead(String userId,List<LeadMemberDTO> list) {
        try{
            for(LeadMemberDTO dto :list){
                // Kiem tra neu chua co quan he lead va campaign thi se them
                Specification<LeadMember> spec = new Specification<LeadMember>() {
                    @Override
                    public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("leadsId"), dto.getLeadsId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), dto.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<LeadMember> members = leadMemberRepository.findAll(spec);
                if(!members.isEmpty()) continue;

                LeadMember leadMember = LeadMember.builder()
                        .leadsId(dto.getLeadsId())
                        .campaignId(dto.getCampaignId())
                        .memberStatus(memberStatusRepository.findById(dto.getMemberStatus()==null?1:dto.getMemberStatus()).orElse(null))
                        .createBy(userId)
                        .createDate(LocalDateTime.now())
                        .editBy(userId)
                        .editDate(LocalDateTime.now())
                        .build();
                leadMemberRepository.save(leadMember);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean addContact(String userId,List<ContactMemberDTO> list) {
        try{
            for(ContactMemberDTO dto :list){
                Specification<ContactMember> spec = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), dto.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ContactMember> members = contactMemberRepository.findAll(spec);
                if(!members.isEmpty()) continue;

                ContactMember leadMember = ContactMember.builder()
                        .contactId(dto.getContactId())
                        .campaignId(dto.getCampaignId())
                        .memberStatus(memberStatusRepository.findById(dto.getMemberStatus()==null?1:dto.getMemberStatus()).orElse(null))
                        .createBy(userId)
                        .createDate(LocalDateTime.now())
                        .editBy(userId)
                        .editDate(LocalDateTime.now())
                        .build();
                contactMemberRepository.save(leadMember);

                //Lien ket opportunity voi campaign trong contact
                Specification<CoOppRelation> spec1 = new Specification<CoOppRelation>() {
                    @Override
                    public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<CoOppRelation> coOppRelations = coOppRelationRepository.findAll(spec1);
                for (CoOppRelation relation : coOppRelations){
                    CampaignInfluence campaignInfluence = CampaignInfluence.builder()
                            .campaignId(dto.getCampaignId())
                            .opportunityContactId(relation.getCooppIdId())
                            .build();
                    campaignInfluenceRepository.save(campaignInfluence);
                }
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean deleteLead(List<LeadMemberDTO> list) {
        try{
            for(LeadMemberDTO dto :list){
                Specification<LeadMember> spec = new Specification<LeadMember>() {
                    @Override
                    public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("leadsId"), dto.getLeadsId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), dto.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
                leadMemberRepository.deleteAll(leadMembers);

            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean deleteContact(List<ContactMemberDTO> list) {
        try{
            for(ContactMemberDTO dto :list){
                Specification<ContactMember> spec = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), dto.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ContactMember> contactMembers = contactMemberRepository.findAll(spec);
                contactMemberRepository.deleteAll(contactMembers);

                //Lien ket opportunity voi campaign trong contact
                Specification<CoOppRelation> spec1 = new Specification<CoOppRelation>() {
                    @Override
                    public Predicate toPredicate(Root<CoOppRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<CoOppRelation> coOppRelations = coOppRelationRepository.findAll(spec1);
                for (CoOppRelation relation : coOppRelations){
                    Specification<CampaignInfluence> spec2 = new Specification<CampaignInfluence>() {
                        @Override
                        public Predicate toPredicate(Root<CampaignInfluence> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                            List<Predicate> predicates = new ArrayList<>();
                            predicates.add(criteriaBuilder.equal(root.get("opportunityContactId"), relation.getCooppIdId()));
                            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                        }
                    };
                    List<CampaignInfluence> campaignInfluenceList = campaignInfluenceRepository.findAll(spec2);
                    campaignInfluenceRepository.deleteAll(campaignInfluenceList);
                }
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean patchListLead(String userId,List<LeadMemberDTO> list) {
        try{
            for(LeadMemberDTO dto :list){
                Specification<LeadMember> spec = new Specification<LeadMember>() {
                    @Override
                    public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("leadsId"), dto.getLeadsId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), dto.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
                LeadMember leadMember = leadMembers.get(0);
                leadMember.setMemberStatus(memberStatusRepository.findById(dto.getMemberStatus()).orElse(null));
                leadMember.setEditBy(userId);
                leadMember.setEditDate(LocalDateTime.now());
                leadMemberRepository.save(leadMember);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean patchListContact(String userId,List<ContactMemberDTO> list) {
        try{
            for(ContactMemberDTO dto :list){
                Specification<ContactMember> spec = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), dto.getContactId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), dto.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ContactMember> contactMembers = contactMemberRepository.findAll(spec);
                ContactMember contactMember = contactMembers.get(0);
                contactMember.setMemberStatus(memberStatusRepository.findById(dto.getMemberStatus()).orElse(null));
                contactMember.setEditBy(userId);
                contactMember.setEditDate(LocalDateTime.now());
                contactMemberRepository.save(contactMember);
            }
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> viewLeadMember(long campaignId, int pageNo, int pageSize) {
        try {
            List<ViewLeadMembers> list = new ArrayList<>();
            Specification<LeadMember> spec = new Specification<LeadMember>() {
                @Override
                public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("campaignId"), campaignId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
            for(LeadMember member : leadMembers){
                LeadDtoProto proto = opportunityClientService.getLead(member.getLeadsId());
                if (proto == null||proto.getLeadId() == 0) throw new RuntimeException("Can not get Lead with id = "+member.getLeadsId());
                Specification<LeadMember> spec1 = new Specification<LeadMember>() {
                    @Override
                    public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("leadsId"), member.getLeadsId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), campaignId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<LeadMember> members = leadMemberRepository.findAll(spec1);
                LeadMember leadMember = members.get(0);
                ViewLeadMembers viewLeadMembers = ViewLeadMembers.builder()
                        .leadMember(leadMember)
                        .title(proto.getTitle())
                        .firstName(proto.getFirstName())
                        .lastName(proto.getLastName())
                        .company(proto.getCompany())
                        .order(leadMember.getEditDate())
                        .build();
                list.add(viewLeadMembers);
            }
            Collections.sort(list, new Comparator<ViewLeadMembers>() {
                @Override
                public int compare(ViewLeadMembers o1, ViewLeadMembers o2) {
                    return o2.getOrder().compareTo(o1.getOrder());
                }
            });

            Page<ViewLeadMembers> pageLeadMembers = listToPage(list, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            return converter.convertToPageResponse(pageLeadMembers, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> viewContactMember(long campaignId, int pageNo, int pageSize) {
        try {
            List<ViewContactMembers> list = new ArrayList<>();
            Specification<ContactMember> spec = new Specification<ContactMember>() {
                @Override
                public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("campaignId"), campaignId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactMember> leadMembers = contactMemberRepository.findAll(spec);
            for(ContactMember member : leadMembers){
                ContactDtoProto proto = opportunityClientService.getContact(member.getContactId());
                AccountDtoProto accountDtoProto = opportunityClientService.getAccount(proto.getAccountId());
                if (proto == null||proto.getContactId()==0) throw new RuntimeException("Can not get Lead with id = "+member.getContactId());
                Specification<ContactMember> spec1 = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), member.getContactId()));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), campaignId));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                List<ContactMember> members = contactMemberRepository.findAll(spec1);
                ContactMember contactMember = members.get(0);
                ViewContactMembers viewContactMembers = ViewContactMembers.builder()
                        .contactMember(contactMember)
                        .title(proto.getTitle())
                        .firstName(proto.getFirstName())
                        .lastName(proto.getLastName())
                        .company(accountDtoProto.getAccountName())
                        .order(contactMember.getEditDate())
                        .build();
                list.add(viewContactMembers);
            }
            Collections.sort(list, new Comparator<ViewContactMembers>() {
                @Override
                public int compare(ViewContactMembers o1, ViewContactMembers o2) {
                    return o2.getOrder().compareTo(o1.getOrder());
                }
            });

            Page<ViewContactMembers> pageLeadMembers = listToPage(list, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            return converter.convertToPageResponse(pageLeadMembers, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public Boolean convertCampaignFromLeadToContact(String userId,long leadId, long contactId) {
        try {
            Specification<LeadMember> spec = new Specification<LeadMember>() {
                @Override
                public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadsId"), leadId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
            for (LeadMember member : leadMembers){
                Specification<ContactMember> spec1 = new Specification<ContactMember>() {
                    @Override
                    public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                        List<Predicate> predicates = new ArrayList<>();
                        predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                        predicates.add(criteriaBuilder.equal(root.get("campaignId"), member.getCampaignId()));
                        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                    }
                };
                boolean check = contactMemberRepository.exists(spec1);
                if (!check) {
                    ContactMember contactMember = ContactMember.builder()
                            .memberStatus(member.getMemberStatus())
                            .campaignId(member.getCampaignId())
                            .contactId(contactId)
                            .createDate(LocalDateTime.now())
                            .editDate(LocalDateTime.now())
                            .createBy(userId)
                            .editBy(userId)
                            .build();
                    contactMemberRepository.save(contactMember);
                }
            }
            leadMemberRepository.deleteAll(leadMembers);
            return true ;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getInfluenceOpportunities(long campaignId, int pageNo, int pageSize) {
        try{
            List<InfluenceOpportunityResponse> list = new ArrayList<>();
            Specification<CampaignInfluence> spec = new Specification<CampaignInfluence>() {
                @Override
                public Predicate toPredicate(Root<CampaignInfluence> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("campaignId"), campaignId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<CampaignInfluence> campaignInfluenceList = campaignInfluenceRepository.findAll(spec);
            for (CampaignInfluence influence : campaignInfluenceList){
                CoOppRelation relation = coOppRelationRepository.findById(influence.getOpportunityContactId()).orElse(null);
                assert relation != null;
                ContactDtoProto proto = opportunityClientService.getContact(relation.getContactId());
                InfluenceOpportunityResponse response = InfluenceOpportunityResponse.builder()
                        .opportunity(opportunityRepository.findById(relation.getOpportunityId()).orElse(null))
                        .contactId(proto.getContactId())
                        .contactName(proto.getLastName()+" "+proto.getFirstName())
                        .influence(influence.getInfluence())
                        .revenueShare(influence.getRevenueShare())
                        .campaign(campaignRepository.findById(influence.getCampaignId()).orElse(null))
                        .build();
                list.add(response);
            }
            //Them phan trang
            Page<InfluenceOpportunityResponse> pageLeadMembers = listToPage(list, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            return converter.convertToPageResponse(pageLeadMembers, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Boolean deleteRelationLeadCampaign(long leadId) {
        try {
            Specification<LeadMember> spec = new Specification<LeadMember>() {
                @Override
                public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadsId"), leadId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadMember> campaignInfluenceList = leadMemberRepository.findAll(spec);
            leadMemberRepository.deleteAll(campaignInfluenceList);
            return true;
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getListCampaignsByLead(Long leadId, int pageNo, int pageSize) {
        try {
            List<CampaignResponse> list = new ArrayList<>();
            Specification<LeadMember> spec = new Specification<LeadMember>() {
                @Override
                public Predicate toPredicate(Root<LeadMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("leadsId"), leadId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<LeadMember> leadMembers = leadMemberRepository.findAll(spec);
            for(LeadMember member : leadMembers){
                Campaign proto = campaignRepository.findById(member.getCampaignId()).orElse(null);
                if (proto == null) throw new RuntimeException("Can not get campaign with id = "+member.getCampaignId());
                list.add(converter.entityToCampaignResponse(proto));
            }

            Page<CampaignResponse> pageLeadMembers = listToPage(list, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            return converter.convertToPageResponse(pageLeadMembers, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PageResponse<?> getListCampaignsByContact(Long contactId, int pageNo, int pageSize) {
        try {
            List<CampaignResponse> list = new ArrayList<>();
            Specification<ContactMember> spec = new Specification<ContactMember>() {
                @Override
                public Predicate toPredicate(Root<ContactMember> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(criteriaBuilder.equal(root.get("contactId"), contactId));
                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                }
            };
            List<ContactMember> contactMembers = contactMemberRepository.findAll(spec);
            for(ContactMember member : contactMembers){
                Campaign proto = campaignRepository.findById(member.getCampaignId()).orElse(null);
                if (proto == null) throw new RuntimeException("Can not get campaign with id = "+member.getCampaignId());
                list.add(converter.entityToCampaignResponse(proto));
            }

            Page<CampaignResponse> pageLeadMembers = listToPage(list, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            return converter.convertToPageResponse(pageLeadMembers, pageable);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }


}
