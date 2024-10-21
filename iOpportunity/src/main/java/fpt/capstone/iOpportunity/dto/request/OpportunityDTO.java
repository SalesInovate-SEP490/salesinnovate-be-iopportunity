package fpt.capstone.iOpportunity.dto.request;

import fpt.capstone.iOpportunity.model.Forecast;
import fpt.capstone.iOpportunity.model.Stage;
import fpt.capstone.iOpportunity.model.Type;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OpportunityDTO {
    private Long opportunityId;
    private String opportunityName;
    private Long accountId;
    private String userId;
    private Float probability;
    private String nextStep;
    private BigDecimal amount;
    private LocalDateTime closeDate;
    private Long primaryCampaignSourceId;
    private String description;
    private Long forecast;
    private Long stage;
    private Long type;
    private String lastModifiedBy ;
    private Long leadSource ;
    private LocalDateTime createDate ;
    private LocalDateTime editDate ;
    private String createBy ;
    private Boolean isDeleted ;

}
