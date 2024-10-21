package fpt.capstone.iOpportunity.model.quote;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="quote")
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_id")
    private Long quoteId;
    @Column(name = "quote_number", length = 50)
    private String quoteNumber;
    @Column(name = "quote_name", length = 50)
    private String quoteName;
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "opportunity_id")
    private Long opportunityId;
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "is_sync")
    private Boolean isSync;
    @Column(name = "description")
    private String description;
    @Column(name = "subtotal")
    private BigDecimal subtotal;
    @Column(name = "discount")
    private BigDecimal discount;
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "tax")
    private BigDecimal tax;
    @Column(name = "shipping_and_handling")
    private BigDecimal shippingHandling;
    @Column(name = "grand_total")
    private BigDecimal grandTotal;

    @Column(name = "contact_id")
    private Long contactId;
    @Column(name = "email")
    private String email;
    @Column(name = "phone")
    private String phone;
    @Column(name = "fax")
    private String fax;
    @Column(name = "billing_name")
    private String billingName;
    @Column(name = "shipping_name")
    private String shippingName;

    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "create_date")
    private LocalDateTime createDate;
    @Column(name = "edit_by")
    private String editBy;
    @Column(name = "edit_date")
    private LocalDateTime editDate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "quote_status_id")
    private QuoteStatus quoteStatus;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true )
    @JoinColumn(name = "billing_information_id")
    private AddressInformation billingInformation;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "shipping_information_id")
    private AddressInformation shippingInformation;
}
