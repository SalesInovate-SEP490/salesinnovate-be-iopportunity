package fpt.capstone.iOpportunity.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LeadSourceDTO {
    private Long leadSourceId;
    private String leadSourceName;
}
