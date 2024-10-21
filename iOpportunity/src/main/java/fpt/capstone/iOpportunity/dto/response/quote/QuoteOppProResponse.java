package fpt.capstone.iOpportunity.dto.response.quote;

import fpt.capstone.iOpportunity.dto.response.OpportunityProductResponse;
import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.quote.Quote;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class QuoteOppProResponse {
    private Long opportunityId;
    private Long quoteId;
    private List<OpportunityProductResponse> products;
}
