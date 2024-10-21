package fpt.capstone.iOpportunity.dto.response.campaign;

import fpt.capstone.iOpportunity.model.campaign.LeadMember;
import fpt.capstone.iOpportunity.model.campaign.MemberStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ViewLeadMembers {
    private LeadMember leadMember;
    private String title ;
    private String firstName;
    private String lastName;
    private String company;
    private LocalDateTime order;

}
