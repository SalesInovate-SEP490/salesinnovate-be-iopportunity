package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="product_family")
public class ProductFamily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_family_id")
    private Long productFamilyId;
    @Column(name = "product_family_name")
    private String productFamilyName;
}
