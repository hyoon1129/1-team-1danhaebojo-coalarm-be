package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "volume_spike")
public class VolumeSpikeAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long marketAlertId;

    @Column(nullable = false)
    private boolean tradingVolumeSoaring;

    @OneToOne  // 기존 @OneToOne을 @ManyToOne으로 변경
    @JoinColumn(name = "alert_id", nullable = false, unique = true)
    private Alert alert;  // Alert 엔티티와 FK 관계 설정
}

