package fpt.capstone.iOpportunity.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OpportunityPriceBookProductDTO {
    private Long opportunityId ;
    private List<OpportunityProductDTO> opportunityProductDTOS;
}
