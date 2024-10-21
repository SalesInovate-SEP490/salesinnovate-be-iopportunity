package fpt.capstone.iOpportunity.model;

import com.google.type.Decimal;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "opportunity_product")
public class OpportunityProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opportunity_product_id")
    private Long opportunityProductId;
    @Column(name = "opportunity_id")
    private Long opportunityId;
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "quantity")
    private Integer quantity;
    @Column(name = "sales_price")
    private BigDecimal sales_price;
    @Column(name = "date")
    private LocalDateTime date;
    @Column(name = "line_description")
    private String line_description;
    @Column(name = "currency")
    private Long currency;
}
