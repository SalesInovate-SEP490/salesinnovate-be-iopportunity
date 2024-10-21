package fpt.capstone.iOpportunity.dto.response;

import com.google.type.Decimal;
import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.Product;
import fpt.capstone.iOpportunity.model.ProductPriceBookCurrency;
import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OpportunityProductResponse {
    private Long opportunityProductId;
    private Long opportunityId;
    private Product product;
    private Integer quantity;
    private BigDecimal sales_price;
    private LocalDateTime date;
    private String line_description;
    private ProductPriceBookCurrency currency;
}
