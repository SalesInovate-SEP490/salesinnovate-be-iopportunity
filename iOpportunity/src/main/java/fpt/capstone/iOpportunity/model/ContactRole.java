package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="opportunity_contact_role")
public class ContactRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_contact_role_id")
    private Long opportunityContactRoleId;
    @Column(name = "role_name")
    private String roleName;

}
