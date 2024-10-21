package fpt.capstone.iOpportunity.dto.request;

import com.google.type.Decimal;
import fpt.capstone.iOpportunity.model.Product;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OpportunityProductDTO {
    private Long opportunityProductId;
    private Long productId;
    private Integer quantity;
    private BigDecimal sales_price;
    private LocalDateTime date;
    private String line_description;
}
