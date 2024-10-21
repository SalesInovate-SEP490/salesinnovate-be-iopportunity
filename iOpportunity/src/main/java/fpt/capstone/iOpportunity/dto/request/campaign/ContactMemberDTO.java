package fpt.capstone.iOpportunity.dto.request.campaign;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ContactMemberDTO {
    private Long contactId;
    private Long campaignId ;
    private Long memberStatus ;
}
