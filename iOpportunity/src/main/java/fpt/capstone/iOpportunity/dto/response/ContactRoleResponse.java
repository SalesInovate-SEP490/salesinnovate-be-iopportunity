package fpt.capstone.iOpportunity.dto.response;

import fpt.capstone.iOpportunity.model.CoOppRelation;
import fpt.capstone.iOpportunity.model.ContactRole;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ContactRoleResponse {
    private Long contactId;
    private String contactName;
    private String title;
    private String phone;
    private String email;
    private Long accountId;
    private String accountName;
    private CoOppRelation coOppRelation;
}
