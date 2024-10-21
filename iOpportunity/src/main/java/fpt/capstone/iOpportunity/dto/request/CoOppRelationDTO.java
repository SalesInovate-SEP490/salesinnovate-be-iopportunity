package fpt.capstone.iOpportunity.dto.request;

import fpt.capstone.iOpportunity.model.ContactRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CoOppRelationDTO {
    private Long cooppIdId;
    private Long opportunityId;
    private Long contactId ;
    private Long contactRole;
}
