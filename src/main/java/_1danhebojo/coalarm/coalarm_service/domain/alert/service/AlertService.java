package _1danhebojo.coalarm.coalarm_service.domain.alert.service;

import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.*;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.AlertListResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.AlertResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.alertHistory.AlertHistoryListResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.AlertRepositoryImpl;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Coin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Alert;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepositoryImpl alertRepository;

    // 알람 추가
    public void addAlert(BaseAlertRequest request) {
        Alert alert = convertToAlertEntity(request);
        Alert savedAlert = alertRepository.save(alert);

        Optional<Alert> checkAlert = alertRepository.findById(alert.getAlertId());
        if (checkAlert.isEmpty()) {
            throw new RuntimeException("🚨 flush() 후에도 저장 안 됨!");
        }

        Long alertId = savedAlert.getAlertId();
        if (alertId == null) {
            throw new RuntimeException("Alert 저장 실패");
        }

        switch (request.getType()) {
            case "TARGET_PRICE":
                TargetPriceAlertRequest targetPriceAlert = (TargetPriceAlertRequest) request;
                targetPriceAlert.setIsTargetPrice(true);
                targetPriceAlert.setAlertId(alertId);

                Long target = alertRepository.saveTargetPriceAlert(targetPriceAlert);
                if (target == null) {
                    throw new RuntimeException("Target Price Alert 저장 실패");
                }
                break;

            case "GOLDEN_CROSS":
                GoldenCrossAlertRequest goldenCrossAlert = (GoldenCrossAlertRequest) request;
                goldenCrossAlert.setIsGoldenCross(true);
                goldenCrossAlert.setAlertId(alertId);

                Long goldenCrossId = alertRepository.saveGoldenCrossAlert(goldenCrossAlert);
                if (goldenCrossId == null) {
                    throw new RuntimeException("Golden Cross Alert 저장 실패");
                }
                break;

            case "VOLUME_SPIKE":
                VolumeSpikeAlertRequest volumeSpikeAlert = (VolumeSpikeAlertRequest) request;
                volumeSpikeAlert.setAlertId(alertId);
                volumeSpikeAlert.setIsTradingVolumeSoaring(true);

                Long volumeSpikeId = alertRepository.saveVolumeSpikeAlert(volumeSpikeAlert);
                if (volumeSpikeId == null) {
                    throw new RuntimeException("Volume Spike Alert 저장 실패");
                }
                break;

            default:
                throw new IllegalArgumentException("잘못된 알람 타입: " + request.getType());
        }
    }

    // 알람 활성화 수정
    @Transactional
    public Long updateAlertStatus(Long alertId, boolean active) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setActive(active);
        Alert saveAlert = alertRepository.save(alert);
        return saveAlert.getAlertId();
    }

    // 알람 삭제
    @Transactional
    public void deleteAlert(Long alertId) {
        alertRepository.deleteById(alertId);
    }

    // 알람 목록 조회
    public AlertListResponse getAllAlerts(AlertFilterRequest request) {
        // ✅ 정렬 방식 설정
        Sort sort = request.getSort().equalsIgnoreCase("LATEST")
                ? Sort.by(Sort.Direction.DESC, "regDt")
                : Sort.by(Sort.Direction.ASC, "regDt");

        PageRequest pageRequest = PageRequest.of(request.getOffset(), request.getLimit(), sort);

        // `active`가 null이면 전체 조회, 아니면 필터링 적용
        Boolean active = request.getActive();

        Page<Alert> alerts = alertRepository.findAlertsByFilter(active, request.getFilter(), pageRequest);

        List<AlertResponse> alertResponses = alerts.getContent().stream()
                .map(AlertResponse::new)
                .collect(Collectors.toList());

        return new AlertListResponse(
                alertResponses,
                request.getOffset(),
                request.getLimit(),
                alerts.getTotalElements(),
                alerts.hasNext()
        );
    }

    private Alert convertToAlertEntity(BaseAlertRequest request) {
        Alert alert = new Alert();
        alert.setActive(request.getActive());
        alert.setTitle(request.getTitle());
        alert.setGoldenCross(request instanceof GoldenCrossAlertRequest);
        alert.setTargetPrice(request instanceof TargetPriceAlertRequest);
        alert.setVolumeSpike(request instanceof VolumeSpikeAlertRequest);
        if (request.getCoinId() != null) {
            Coin coin = new Coin();
            coin.setCoinId(request.getCoinId());
            alert.setCoin(coin);
        }
        alert.setUserId(request.getUserId());
        return alert;
    }

}