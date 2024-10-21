package fpt.capstone.iOpportunity.dto.request.campaign;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CampaignDTO {
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

    private Long campaignStatus;
    private Long campaignType;

}
