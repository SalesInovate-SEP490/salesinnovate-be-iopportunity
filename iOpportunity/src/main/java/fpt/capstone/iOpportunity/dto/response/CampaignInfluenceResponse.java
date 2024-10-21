package fpt.capstone.iOpportunity.dto.response;

import fpt.capstone.iOpportunity.model.CoOppRelation;
import fpt.capstone.iOpportunity.model.campaign.Campaign;
import lombok.*;

import java.math.BigDecimal;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CampaignInfluenceResponse {
    private Long campaignInfluenceId ;
    private Campaign campaign;
    private String contactName;
    private Long contactId;
    private BigDecimal influence;
    private BigDecimal revenueShare;
    private CoOppRelation coOppRelation;
}
