package fpt.capstone.iOpportunity.dto.response.quote;

import fpt.capstone.iOpportunity.model.quote.AddressInformation;
import fpt.capstone.iOpportunity.model.quote.QuoteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private Long quoteId;
    private String quoteNumber;
    private String quoteName;
    private LocalDateTime expirationDate;

    private Long opportunityId;
    private String opportunityName;
    private Long accountId;
    private String accountName;

    private Boolean isSync;
    private String description;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal totalPrice;

    private BigDecimal tax;
    private BigDecimal shippingHandling;
    private BigDecimal grandTotal;

    private Long contactId;
    private String contactName;
    private String email;
    private String phone;
    private String fax;

    private String billingName;
    private String shippingName;

    private QuoteStatus quoteStatus;

    private AddressInformation billingInformation;

    private AddressInformation shippingInformation;

    private String createdBy;
    private String createdByName;
    private LocalDateTime createDate;
    private String editBy;
    private String editByName;
    private LocalDateTime editDate;
}
