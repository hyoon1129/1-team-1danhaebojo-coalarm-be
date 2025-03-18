package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Alert;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertResponse {
    private Long alertId;
    private Long userId;
    private Boolean active;
    private String title;
    private CoinResponse coin;
    private String alertType;
    private GoldenCrossResponse goldenCross;
    private VolumeSpikeResponse volumeSpike;
    private TargetPriceResponse targetPrice;

    public AlertResponse(Alert alert) {
        this.alertId = alert.getAlertId();
        this.title = alert.getTitle();
        this.coin = new CoinResponse(alert.getCoin());
        this.userId = alert.getUserId();
        this.active = alert.isActive();
        // `is_*` 값 중 `true`인 값을 기준으로 alertType 설정
        if (alert.isGoldenCross()) {
            this.alertType = "GOLDEN_CROSS";
            this.goldenCross = new GoldenCrossResponse(alert.getGoldenCross());
        } else if (alert.isTargetPrice()) {
            this.alertType = "TARGET_PRICE";
            this.targetPrice = new TargetPriceResponse(alert.getTargetPrice());
        } else if (alert.isVolumeSpike()) {
            this.alertType = "VOLUME_SPIKE";
            this.volumeSpike = new VolumeSpikeResponse(alert.getVolumeSpike());
        }
    }
}

