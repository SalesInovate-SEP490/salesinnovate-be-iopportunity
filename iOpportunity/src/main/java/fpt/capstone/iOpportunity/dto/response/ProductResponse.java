package fpt.capstone.iOpportunity.dto.response;

import fpt.capstone.iOpportunity.model.ProductFamily;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String productName;
    private String productCode;
    private String productDescription;
    private Integer isActive;
    private ProductFamily productFamily;
}
