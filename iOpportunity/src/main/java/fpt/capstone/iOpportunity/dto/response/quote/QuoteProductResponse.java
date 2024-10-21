package fpt.capstone.iOpportunity.dto.response.quote;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class QuoteProductResponse {
    private String productName;
    private int quantity;
    private Double unitPrice;
    private Double totalPrice2;
    private Double listPrice;
}
