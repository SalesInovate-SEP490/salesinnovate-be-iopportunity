package fpt.capstone.iOpportunity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="opportunities")
public class Opportunity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_id")
    private Long opportunityId;
    @Column(name = "opportunity_name")
    private String opportunityName;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "probality")
    private Float probability ;
    @Column(name = "next_step")
    private  String nextStep;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "account_id")
    private Long accountId ;
    @Column(name = "close_date")
    private LocalDateTime closeDate;
    @Column(name = "primary_campaign_source_id")
    private Long primaryCampaignSourceId;
    @Column(name = "description")
    private String description ;
    @Column(name = "last_modified_by")
    private String lastModifiedBy ;
//    @Column(name = "partner_id")
//    private Long partnerId ;

    @Column(name = "create_date")
    private LocalDateTime createDate ;
    @Column(name = "edit_date")
    private LocalDateTime editDate ;
    @Column(name = "create_by")
    private String createBy ;
    @Column(name = "is_deleted")
    private Boolean isDeleted ;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "forecast_category_id")
    private Forecast forecast;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "stage_oppor_id")
    private Stage stage;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "type_oppor_id")
    private Type type;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "lead_source_id")
    private LeadSource leadSource;

    @Column(name = "price_book_id")
    private Long priceBook ;

    @ManyToMany(fetch = FetchType.LAZY,
            cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "opportunity_user",
            joinColumns = @JoinColumn(name = "opportunity_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<Users> users ;
}
