package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.alertHistory;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Alert;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Coin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.AlertHistory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AlertHistoryListResponse {
    private final List<AlertHistoryContent> contents;
    private final int offset;
    private final int limit;

    private final long totalElements;
    private final boolean hasNext;

    public AlertHistoryListResponse(List<AlertHistoryContent> contents, int offset, int limit, long totalElements, boolean hasNext) {
        this.contents = contents;
        this.offset = offset;
        this.limit = limit;
        this.totalElements = totalElements;
        this.hasNext = hasNext;
    }

    @Getter
    public static class AlertHistoryContent {
        private final Long alertHistoryId;
        private final Long userId;
        private final Coin coin;
        private final AlertInfo alert;
        private final LocalDateTime registeredDate;

        public AlertHistoryContent(AlertHistory alertHistory) {
            this.alertHistoryId = alertHistory.getAlertHistoryId();
            this.userId = alertHistory.getUserId();
            this.coin = alertHistory.getAlert().getCoin(); // ✅ 코인 정보 포함
            this.alert = new AlertInfo(alertHistory.getAlert());
            this.registeredDate = alertHistory.getRegisteredDate();
        }
    }

    @Getter
    private static class AlertInfo {
        private Long alertId;
        private String title;

        public AlertInfo(_1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Alert alert) {
            this.alertId = alert.getAlertId();
            this.title = alert.getTitle();
        }
    }
}