package fpt.capstone.iOpportunity.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="price_book")
public class PriceBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_book_id")
    private Long priceBookId;
    @Column(name = "price_book_name")
    private String priceBookName;
    @Column(name = "price_book_description")
    private String priceBookDescription;
    @Column(name = "is_active")
    private Integer isActive;
    @Column(name = "is_standard_price_book")
    private Integer isStandardPriceBook;

}

