package _1danhebojo.coalarm.coalarm_service.domain.alert.controller;

import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.AlertFilterRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.BaseAlertRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.BaseAlertStatusRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.PaginationRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.AlertIdResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.AlertListResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.alertHistory.AlertHistoryListResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.alertHistory.AlertHistoryResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Alert;
import _1danhebojo.coalarm.coalarm_service.domain.alert.service.AlertHistoryService;
import _1danhebojo.coalarm.coalarm_service.domain.alert.service.AlertSSEService;
import _1danhebojo.coalarm.coalarm_service.domain.alert.service.AlertService;
import _1danhebojo.coalarm.coalarm_service.global.api.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertService alertService;
    private final AlertHistoryService alertHistoryService;
    private final AlertSSEService alertSSEService;

    public AlertController(AlertService alertService, AlertHistoryService alertHistoryService, AlertSSEService alertSSEService) {
        this.alertService = alertService;
        this.alertHistoryService = alertHistoryService;
        this.alertSSEService = alertSSEService;
    }

    // <editor-fold desc="알람 추가/수정/삭제/조회 관련 메서드">
    // 알람 추가
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<?>> addAlert(@Valid @RequestBody BaseAlertRequest request) {
        log.debug("Received Alert Request: {}", request);
        alertService.addAlert(request);
        return ResponseEntity.ok(BaseResponse.success());
    }

    // 알람 활성화 수정
    @PatchMapping("/{alert_id}/status")
    public ResponseEntity<BaseResponse<?>> updateAlert(@PathVariable("alert_id") Long alertId,
                                                       @RequestBody BaseAlertStatusRequest request) {
        Long saveAlertId = alertService.updateAlertStatus(alertId, request.isActive());
        return ResponseEntity.ok(BaseResponse.success(new AlertIdResponse(saveAlertId)));
    }

    // 알람 삭제
    @DeleteMapping("/{alert_id}")
    public ResponseEntity<BaseResponse<?>> deleteAlert(@PathVariable("alert_id") Long alertId) {
        alertService.deleteAlert(alertId);
        return ResponseEntity.ok(BaseResponse.success());
    }

    // 알람 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getAlertList(
        @RequestParam int offset,  // 가져올 데이터의 시작 인덱스
        @RequestParam int limit,   // 가져올 데이터 개수
        @RequestParam(required = false) Boolean active,  // 활성화된 알람만 조회 여부
        @RequestParam String filter,  // 코인 심볼 필터링 (e.g. BTC)
        @RequestParam String sort  // 정렬 기준 (LATEST, OLDEST)
    ) {
        AlertFilterRequest request = new AlertFilterRequest();
        request.setOffset(offset);
        request.setLimit(limit);
        request.setActive(active);
        request.setFilter(filter);
        request.setSort(sort);

        AlertListResponse alertList = alertService.getAllAlerts(request);
        return ResponseEntity.ok(BaseResponse.success(alertList));
    }
    // </editor-fold>

    // <editor-fold desc="알람 히스토리 관련 메서드">
    @GetMapping("/history")
    public ResponseEntity<BaseResponse<?>> getAlertHistoryList(
            @RequestParam int offset,
            @RequestParam int limit
    ) {
        //Long userId = AuthUtil.getCurrentUserId();
        Long userId = 1L;

        PaginationRequest paginationRequest = new PaginationRequest();
        paginationRequest.setOffset(offset);
        paginationRequest.setLimit(limit);

        AlertHistoryListResponse alertHistoryList = alertHistoryService.getAlertHistoryList(userId, paginationRequest);
        return ResponseEntity.ok(BaseResponse.success(alertHistoryList));
    }

    @GetMapping("/history/{alertHistoryId}")
    public ResponseEntity<BaseResponse<?>>  getAlertHistoryDetail(
            @PathVariable Long alertHistoryId) {
        AlertHistoryResponse alertHistory = alertHistoryService.getAlertHistory(alertHistoryId);
        return ResponseEntity.ok(BaseResponse.success(alertHistory));
    }

    @PostMapping("/history/{alert_id}")
    public ResponseEntity<?> addAlertHistory(@PathVariable("alert_id") Long alertId) {
        //Long userId = AuthUtil.getCurrentUserId();
        Long userId = 1L;

        alertHistoryService.addAlertHistory(alertId, userId);
        return ResponseEntity.ok(BaseResponse.success());
    }
    // </editor-fold>

}