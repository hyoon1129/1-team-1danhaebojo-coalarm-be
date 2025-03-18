package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "golden_cross")
public class GoldenCrossAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goldenCrossId;

    @Column(nullable = false)
    private Long shortMa;  // 단기 이동평균선

    @Column(nullable = false)
    private Long longMa;   // 장기 이동평균선

    @OneToOne  // ✅ 기존 @OneToOne을 @ManyToOne으로 변경
    @JoinColumn(name = "alert_id", nullable = false, unique = true)
    private Alert alert;  // Alert 엔티티와 FK 관계 설정
}

