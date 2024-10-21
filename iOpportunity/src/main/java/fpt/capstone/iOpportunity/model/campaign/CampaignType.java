package fpt.capstone.iOpportunity.model.campaign;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="campaign_type")
public class CampaignType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "campaign_type_id")
    private Long campaignTypeId ;
    @Column(name = "campaign_type_name")
    private String campaignTypeName;
}
