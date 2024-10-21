package fpt.capstone.iOpportunity.dto.request;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OpportunityUserDTO {
    private Long opportunityUserId;
    private Long opportunityId;
    private String userId;
}
