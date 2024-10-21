package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="opportunity_contact")
public class CoOppRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_contact_id")
    private Long cooppIdId;
    @Column(name = "opportunity_id")
    private Long opportunityId;
    @Column(name = "contact_id")
    private Long contactId ;
    @Column(name = "is_primary")
    private Boolean primary ;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "opportunity_contact_role_id")
    private ContactRole contactRole;
}
