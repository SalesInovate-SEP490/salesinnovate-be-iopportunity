package fpt.capstone.iOpportunity.dto.response;

import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.OpportunityProduct;
import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.Product;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OpportunityPriceBookProductResponse {
    private Opportunity opportunity;
    private PriceBook priceBook;
    private List<OpportunityProductResponse> products;
}
