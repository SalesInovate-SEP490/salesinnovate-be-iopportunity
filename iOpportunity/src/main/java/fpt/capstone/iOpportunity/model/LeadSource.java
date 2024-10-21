package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="lead_source")
public class LeadSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_source_id")
    private Long leadSourceId;
    @Column(name = "lead_ource_name")
    private String leadSourceName;
}
