package fpt.capstone.iOpportunity.dto.request.campaign;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class LeadMemberDTO {
    private Long leadsId;
    private Long campaignId ;
    private Long memberStatus ;
}
