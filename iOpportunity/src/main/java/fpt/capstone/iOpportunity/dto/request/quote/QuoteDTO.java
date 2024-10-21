package fpt.capstone.iOpportunity.dto.request.quote;

import fpt.capstone.iOpportunity.model.quote.AddressInformation;
import fpt.capstone.iOpportunity.model.quote.QuoteStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class QuoteDTO {
    private String quoteName;
    private LocalDateTime expirationDate;

    private String description;

    private BigDecimal discount;

    private BigDecimal tax;
    private BigDecimal shippingHandling;

    private Long contactId;
    private String email;
    private String phone;
    private String fax;

    private Integer quoteStatus;

    private String billingName;
    private String shippingName;
    private AddressInformation billingInformation;
    private AddressInformation shippingInformation;

}
