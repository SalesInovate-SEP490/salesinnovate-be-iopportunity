package fpt.capstone.iOpportunity.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TypeDTO {
    private Long id;
    private String typeName;
}
