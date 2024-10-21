package fpt.capstone.iOpportunity.dto.response;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PriceBookResponse {
    private Long priceBookId;
    private String priceBookName;
    private String priceBookDescription;
    private Integer isActive;
    private Integer isStandardPriceBook;
}
