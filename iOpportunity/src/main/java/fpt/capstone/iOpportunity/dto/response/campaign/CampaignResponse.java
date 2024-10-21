package fpt.capstone.iOpportunity.dto.response.campaign;


import fpt.capstone.iOpportunity.model.campaign.CampaignStatus;
import fpt.capstone.iOpportunity.model.campaign.CampaignType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CampaignResponse {
    private Long  campaignId ;
    private String campaignName;
    private Boolean isActive ;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String description;
    private BigDecimal num_Sent;
    private BigDecimal budgetedCost;
    private BigDecimal actualCost;
    private BigDecimal expectedResponse;
    private BigDecimal expectedRevenue;
    private Integer responsesInCampaign;
    private Integer leadsInCampaign;
    private Integer convertedLeadsInCampaign;
    private Integer contactsInCampaign;
    private Integer opportunityInCampaign;
    private Integer wonOpportunityInCampaign;
    private BigDecimal valueOpportunityInCampaign;
    private BigDecimal valueWonOpportunityInCampaign;
    private String createdBy;
    private String edit_By;
    private LocalDateTime createDate;
    private LocalDateTime editDate;

    private CampaignStatus campaignStatus;
    private CampaignType campaignType;

}
