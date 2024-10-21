package fpt.capstone.iOpportunity.dto.response.campaign;

import fpt.capstone.iOpportunity.model.campaign.ContactMember;
import fpt.capstone.iOpportunity.model.campaign.LeadMember;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ViewContactMembers {
    private ContactMember contactMember;
    private String title ;
    private String firstName;
    private String lastName;
    private String company;
    private LocalDateTime order;
}
