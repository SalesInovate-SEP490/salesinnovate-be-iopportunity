package fpt.capstone.iOpportunity.dto.request;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productId;
    private String productName;
    private String productCode;
    private String productDescription;
    private Integer isActive;
    private Long productFamily;
}
