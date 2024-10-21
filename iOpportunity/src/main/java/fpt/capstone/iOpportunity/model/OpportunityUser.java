package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="opportunity_user")
public class OpportunityUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_user_id")
    private Long opportunityUserId;
    @Column(name = "opportunity_id")
    private Long opportunityId;
    @Column(name = "user_id")
    private String userId;
}
