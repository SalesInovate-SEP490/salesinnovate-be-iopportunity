package fpt.capstone.iOpportunity.dto.request;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProductFamilyDTO {
    private Long productFamilyId;
    private String productFamilyName;
}
