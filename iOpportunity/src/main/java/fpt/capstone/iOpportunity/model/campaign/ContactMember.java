package fpt.capstone.iOpportunity.model.campaign;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="contact_campaign_member")
public class ContactMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contact_campaign_member_id")
    private Long  contactCampaignMemberId ;
    @Column(name = "contact_id")
    private Long contactId;
    @Column(name = "campaign_id")
    private Long campaignId ;
    @Column(name = "create_date")
    private LocalDateTime createDate ;
    @Column(name = "edit_date")
    private LocalDateTime editDate ;
    @Column(name = "create_by")
    private String createBy ;
    @Column(name = "edit_by")
    private String editBy ;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "campaign_member_status_id")
    private MemberStatus memberStatus ;
}
