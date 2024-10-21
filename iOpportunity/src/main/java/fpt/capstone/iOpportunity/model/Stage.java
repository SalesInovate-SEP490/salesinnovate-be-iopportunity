package fpt.capstone.iOpportunity.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="stage_oppor")
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_oppor_id")
    private Long stageId ;
    @Column(name = "stage_oppor_name")
    private String stageName ;
    @Column(name = "probability")
    private Float probability ;
    @Column(name = "stage_index")
    private Integer index ;
    @Column(name = "is_close")
    private Integer isClose ;
}
