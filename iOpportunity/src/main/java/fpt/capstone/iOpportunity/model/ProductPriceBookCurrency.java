package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="product_price_book_currency")
public class ProductPriceBookCurrency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_price_book_currency_id")
    private Long id ;
    @Column(name = "product_price_book_currency_name")
    private String name ;
}
