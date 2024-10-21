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
@Table(name="product_price_book")
public class ProductPriceBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_price_book_id")
    private Long productPriceBookId;
    @Column(name = "product_id")
    private Long productId;
    @Column(name = "price_book_id")
    private Long priceBookId;
    @Column(name = "list_price")
    private BigDecimal listPrice;
    @Column(name = "use_standard_price")
    private Integer useStandardPrice;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "edit_by")
    private String editBy;
    @Column(name = "edit_date")
    private LocalDateTime editDate;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "product_price_book_currency_id")
    private ProductPriceBookCurrency currency;
}
