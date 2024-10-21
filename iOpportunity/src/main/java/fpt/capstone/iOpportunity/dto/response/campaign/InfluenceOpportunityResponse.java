package fpt.capstone.iOpportunity.dto.response.campaign;

import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.campaign.Campaign;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class InfluenceOpportunityResponse {
    private Opportunity opportunity;
    private Long contactId ;
    private String contactName ;
    private BigDecimal influence ;
    private BigDecimal revenueShare ;
    private Campaign campaign;
}
