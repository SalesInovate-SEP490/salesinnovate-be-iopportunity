package fpt.capstone.iOpportunity.dto.response;

import fpt.capstone.iOpportunity.model.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OpportunityResponse {
    private Long opportunityId;
    private String opportunityName;
    private BigDecimal amount;
    private Float probability;
    private String nextStep;
    private String userId;
    private Long accountId;
    private String accountName;
    private LocalDateTime closeDate;
    private Long primaryCampaignSourceId;
    private String description ;
    private String lastModifiedBy ;
    private Forecast forecast;
    private Stage stage;
    private Type type;
    private PriceBook priceBook ;
    private LeadSource leadSource;
    private LocalDateTime createDate ;
    private LocalDateTime editDate ;
    private String createBy ;
    private Boolean isDeleted ;

}
