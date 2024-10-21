package fpt.capstone.iOpportunity.dto.request;

import lombok.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ConvertFromLeadDTO {
    String opportunityName;
    Long leadId;
    Long accountId;
    Long contactId;
    List<OpportunityUserDTO> userDTOS;
}
