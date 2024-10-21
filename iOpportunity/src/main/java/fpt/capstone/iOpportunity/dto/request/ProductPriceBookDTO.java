package fpt.capstone.iOpportunity.dto.request;

import com.google.type.Decimal;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductPriceBookDTO {
    private Long productPriceBookId;
    private Long productId;
    private Long priceBookId;
    private BigDecimal listPrice;
    private Integer useStandardPrice;
    private Long currency;
}
