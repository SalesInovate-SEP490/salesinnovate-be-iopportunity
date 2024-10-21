package fpt.capstone.iOpportunity.dto.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CampaignInfluenceDTO {
    private Long opportunityId ;
    private Long campaignInfluenceId ;
    private BigDecimal revenueShare;
}
