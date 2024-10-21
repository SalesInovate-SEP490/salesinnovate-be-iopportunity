package fpt.capstone.iOpportunity.model.campaign;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="campaign")
public class Campaign {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "campaign_id")
  private Long  campaignId ;
  @Column(name = "campaign_name")
  private String campaignName;
  @Column(name = "isActive")
  private Boolean isActive ;
  @Column(name = "start_date")
  private LocalDateTime startDate;
  @Column(name = "end_date")
  private LocalDateTime endDate;
  @Column(name = "description")
  private String description;
  @Column(name = "num_sent")
  private BigDecimal num_Sent;
  @Column(name = "budgeted_cost")
  private BigDecimal budgetedCost;
  @Column(name = "actual_cost")
  private BigDecimal actualCost;
  @Column(name = "expected_response")
  private BigDecimal expectedResponse;
  @Column(name = "expected_revenue")
  private BigDecimal expectedRevenue;
  @Column(name = "responses_in_campaign")
  private Integer responsesInCampaign;
  @Column(name = "leads_in_campaign")
  private Integer leadsInCampaign;
  @Column(name = "converted_leads_in_campaign")
  private Integer convertedLeadsInCampaign;
  @Column(name = "contacts_in_campaign")
  private Integer contactsInCampaign;
  @Column(name = "opportunity_in_campaign")
  private Integer opportunityInCampaign;
  @Column(name = "won_opportunity_in_campaign")
  private Integer wonOpportunityInCampaign;
  @Column(name = "value_opportunity_in_campaign")
  private BigDecimal valueOpportunityInCampaign;
  @Column(name = "value_won_opportunity_in_campaign")
  private BigDecimal valueWonOpportunityInCampaign;
  @Column(name = "created_by")
  private String createdBy;
  @Column(name = "edit_by")
  private String editBy;
  @Column(name = "edit_date")
  private LocalDateTime editDate;
  @Column(name = "create_date")
  private LocalDateTime createDate;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "campaign_status_id")
  private CampaignStatus campaignStatus;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "campaign_type_id")
  private CampaignType campaignType;
  }
