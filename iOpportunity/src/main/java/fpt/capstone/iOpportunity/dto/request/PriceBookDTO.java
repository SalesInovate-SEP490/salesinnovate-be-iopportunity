package fpt.capstone.iOpportunity.dto.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PriceBookDTO {
    private Long priceBookId;
    private String priceBookName;
    private String priceBookDescription;
    private Integer isActive;
    private Integer isStandardPriceBook;
}
