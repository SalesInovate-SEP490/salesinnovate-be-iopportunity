package fpt.capstone.iOpportunity.dto.response.quote;

import fpt.capstone.iOpportunity.dto.response.OpportunityProductResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuoteReport {

    private String prepareBy;
    private String email;
    private String shipToName;
    private String shipToNameStreet;
    private String shipToNameCity;
    private String shipToNamePostalCode;
    private String shipToNameCountry;
    private String billToName;
    private String billToNameStreet;
    private String billToNameCity;
    private String billToNamePostalCode;
    private String billToNameCountry;
    private LocalDateTime createDate;
    private String quoteNumber;
    private BigDecimal subTotal;
    private BigDecimal discount;
    private BigDecimal totalPrice;
    private BigDecimal grandTotal;
    private List<QuoteProductResponse> quoteProductResponses;
}
