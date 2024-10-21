package fpt.capstone.iOpportunity.dto.response;

import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.Product;
import fpt.capstone.iOpportunity.model.ProductPriceBookCurrency;
import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductPriceBookResponse {
    private Long productPriceBookId;
    private Product product;
    private PriceBook priceBook;
    private BigDecimal listPrice;
    private Integer useStandardPrice;
    private String createdBy;
    private String editBy;
    private LocalDateTime editDate;
    private ProductPriceBookCurrency currency;
}
