package fpt.capstone.iOpportunity.model.campaign;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="campaign_influence")
public class CampaignInfluence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_influence_id")
    private Long campaignInfluenceId ;
    @Column(name = "campaign_id")
    private Long campaignId;
    @Column(name = "opportunity_contact_id")
    private Long opportunityContactId;
    @Column(name = "influence")
    private BigDecimal influence;
    @Column(name = "revenue_share")
    private BigDecimal revenueShare;

}
