package fpt.capstone.iOpportunity.model.campaign;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="campaign_status")
public class CampaignStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_status_id")
    private Long campaignStatusId ;
    @Column(name = "campaign_status_name")
    private String campaignStatusName;
}
