package fpt.capstone.iOpportunity.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse {
    private String userId;
    private String userName;
    private String passWord;
    private String firstName;
    private String lastName;
    private String email;
}
