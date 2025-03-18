package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "coin_alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id", unique = true, nullable = false)
    private Long alertId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean isGoldenCross;

    @Column(nullable = false)
    private boolean isTargetPrice;

    @Column(nullable = false)
    private boolean isVolumeSpike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coin_id", referencedColumnName = "coin_id")
    private Coin coin;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt = LocalDateTime.now();

    @Column(name = "chg_dt")
    private LocalDateTime chgDt;

    // 개별 이벤트 테이블과의 연결
    @OneToOne(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private TargetPriceAlert targetPrice;

    @OneToOne(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private GoldenCrossAlert goldenCross;

    @OneToOne(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private VolumeSpikeAlert volumeSpike;

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<AlertHistory> alertHistories = new ArrayList<>();
}

