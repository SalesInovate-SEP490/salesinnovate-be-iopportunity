package fpt.capstone.iOpportunity.services;



import fpt.capstone.iOpportunity.dto.request.campaign.ContactMemberDTO;
import fpt.capstone.iOpportunity.dto.request.campaign.LeadMemberDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.model.campaign.MemberStatus;

import java.util.List;

public interface MemberService {
    List<MemberStatus> getListCampaignMemberStatus();
    Long createMemberStatus(String status);
    Boolean patchMemberStatus (long id ,String status);
    Boolean deleteMemberStatus (Long[] id);
    Boolean addLead (String userId,List<LeadMemberDTO> list);
    Boolean addContact (String userId,List<ContactMemberDTO> list);
    Boolean deleteLead (List<LeadMemberDTO> list);
    Boolean deleteContact (List<ContactMemberDTO> list);
    Boolean patchListLead (String userId,List<LeadMemberDTO> list);
    Boolean patchListContact (String userId,List<ContactMemberDTO> list);
    PageResponse<?> viewLeadMember(long campaignId, int pageNo, int pageSize);
    PageResponse<?> viewContactMember(long campaignId, int pageNo, int pageSize);
    Boolean convertCampaignFromLeadToContact(String userId,long leadId,long contactId);
    PageResponse<?> getInfluenceOpportunities(long campaignId, int pageNo, int pageSize);
    // Xoa lead thi se xoa quan he giua lead va cac campaign lien quan
    Boolean deleteRelationLeadCampaign (long leadId);
    PageResponse<?> getListCampaignsByLead(Long leadId,int pageNo, int pageSize);
    PageResponse<?> getListCampaignsByContact(Long contactId,int pageNo, int pageSize);
}
