package fpt.capstone.iOpportunity.model.quote;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="address_information")
public class AddressInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_information_id")
    private Long addressInformationId ;
    @Column(name = "street")
    private String street;
    @Column(name = "city")
    private String city ;
    @Column(name = "province")
    private String province;
    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "country")
    private String country ;


}
