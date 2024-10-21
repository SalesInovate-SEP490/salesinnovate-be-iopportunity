package fpt.capstone.iOpportunity.services;

import fpt.capstone.iOpportunity.dto.request.campaign.CampaignDTO;
import fpt.capstone.iOpportunity.dto.response.PageResponse;
import fpt.capstone.iOpportunity.dto.response.campaign.CampaignResponse;
import fpt.capstone.iOpportunity.model.campaign.CampaignStatus;
import fpt.capstone.iOpportunity.model.campaign.CampaignType;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CampaignService {
    PageResponse<?> getListCampaigns(int pageNo, int pageSize);
    CampaignResponse getDetailCampaign(Long id);
    Long createCampaign(String userId,CampaignDTO campaignDTO);
    Boolean patchCampaign(String userId,Long id,CampaignDTO campaignDTO);
    Boolean patchListCampaign(String userId,Long[] id,CampaignDTO campaignDTO);
    Boolean deleteCampaign (String userId,Long[] id);
    PageResponse<?> filterCampaigns(Pageable pageable, String[] search);
    List<CampaignStatus> getListStatus();
    List<CampaignType> getCampaignTypeList();

}
