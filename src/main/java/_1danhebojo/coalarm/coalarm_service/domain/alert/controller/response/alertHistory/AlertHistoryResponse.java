package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.alertHistory;

import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.AlertResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.CoinResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.AlertHistory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AlertHistoryResponse {
    private Long alertHistoryId;
    private Long userId;
    private AlertResponse alert;
    private LocalDateTime registeredDate;

    public AlertHistoryResponse(AlertHistory alertHistory) {
        this.alertHistoryId = alertHistory.getAlertHistoryId();
        this.userId = alertHistory.getAlert().getUserId();
        this.alert = new AlertResponse(alertHistory.getAlert()); // ✅ Alert 정보 포함
        this.registeredDate = alertHistory.getRegisteredDate();
    }
}
