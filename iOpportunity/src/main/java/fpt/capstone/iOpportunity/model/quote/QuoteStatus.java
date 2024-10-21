package fpt.capstone.iOpportunity.model.quote;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="quote_status")
public class QuoteStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quote_status_id")
    private Integer quoteStatusId;
    @Column(name = "quote_status_name", length = 50)
    private String quoteStatusName;
    @Column(name = "quote_order")
    private Integer quoteOrder;
}
