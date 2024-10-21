package fpt.capstone.iOpportunity.model.quote;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="quote_opportunity_product")
public class QuoteOppPro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_opportunity_product_id")
    private Long quoteOpportunityProductId;
    @Column(name = "quote_id")
    private Long quoteId;
    @Column(name = "opportunity_product_id")
    private Long opportunityProductId;
}
