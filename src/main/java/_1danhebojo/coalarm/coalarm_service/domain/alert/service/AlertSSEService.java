package _1danhebojo.coalarm.coalarm_service.domain.alert.service;

import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response.AlertSSEResponse;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.AlertHistoryRepository;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.AlertRepository;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.AlertEntity;
import _1danhebojo.coalarm.coalarm_service.domain.dashboard.repository.entity.TickerEntity;
import _1danhebojo.coalarm.coalarm_service.domain.user.repository.entity.UserEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertSSEService {
    private final AlertHistoryService alertHistoryService;
    private final AlertRepository alertRepository;
    private final AlertHistoryRepository alertHistoryRepository;
    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final Map<Long, List<AlertEntity>> activeAlertList = new ConcurrentHashMap<>();
    private final DiscordService discordService;

    private final Map<Long, Queue<AlertEntity>> userAlertQueue = new ConcurrentHashMap<>();

    @Lazy
    @Autowired
    private GoldCrossAndTargetPriceService goldCrossAndTargetPriceService;

    @PostConstruct
    @Transactional(readOnly = true)
    public void init() {
        getActiveAlertsGroupedByUser();
    }

    //3초마다 큐에 있는값을 전송
    @Scheduled(fixedRateString = "#{@alarmProperties.sendQueueInterval}")
    public void sendAlertsSequentially() {
        userAlertQueue.forEach((userId, queue) -> {
            if (!queue.isEmpty()) {
                AlertEntity alert = queue.poll();
                sendAlertToUserSSE(userId, alert);
            }
        });
    }

    // 전체 활성화된 사용자의 알람 저장
    @Transactional(readOnly = true)
    public void getActiveAlertsGroupedByUser() {
        List<AlertEntity> activeAlerts = alertRepository.findAllActiveAlerts();

        // userId를 key로, List<Alert>을 value로 하는 Map 생성
        activeAlertList.clear(); // 기존 데이터 삭제
        activeAlertList.putAll(
                activeAlerts.stream()
                        .collect(Collectors.groupingBy(alert -> alert.getUser().getId()))
        );


        System.out.println("Active alerts count: " + activeAlertList.size());
    }

    // 중간중간 전체 알람 상태 재로딩
    @Scheduled(fixedRateString = "#{@alarmProperties.refreshActive}") // 3분마다 실행
    @Transactional(readOnly = true)
    public void refreshActiveAlerts() {
        log.info("전체 알람 상태 재로딩 시작");
        getActiveAlertsGroupedByUser();
    }

    // SSE 연결 유지를 위한 heartbeat 이벤트 주기적 전송 추가
    @Scheduled(fixedRateString = "#{@alarmProperties.sendHeartClient}") // 15초마다 실행
    public void sendHeartbeatToClients() {
        for (Map.Entry<Long, List<SseEmitter>> entry : userEmitters.entrySet()) {
            Long userId = entry.getKey();
            List<SseEmitter> emitters = entry.getValue();

            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("heartbeat")
                            .data("keep-alive")); // 클라이언트에선 로그로만 찍어도 OK
                } catch (IOException e) {
                    deadEmitters.add(emitter);
                    log.warn("heartbeat 전송 실패 - userId: " + userId);
                }
            }

            emitters.removeAll(deadEmitters);
            if (emitters == null || emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    @Scheduled(fixedRateString = "#{@alarmProperties.sendDiscordInterval}") // 1분마다 실행
    public void discordScheduler() {
        Map<Long, List<AlertEntity>> filteredAlerts = activeAlertList.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey, // 사용자 ID 유지 (key)
                        entry -> entry.getValue().stream() // value(알람 리스트) 필터링
                                .filter(alert -> alert.getIsTargetPrice() || alert.getIsGoldenCross())
                                .collect(Collectors.toList())
                ));
        filteredAlerts.forEach(this::sendAlertListToUserDiscord);
    }

    // 1초마다 긁어와서 queue에 추가
    @Scheduled(fixedRateString = "#{@alarmProperties.sendSubscription}") // 1초마다 실행
    @Transactional(readOnly = true)
    public void checkAlertsForSubscribedUsers() {
        // 티커 테이블에서 코인의 최신 값을 한번에 불러와서 조회 후 비교
        List<String> allSymbols = activeAlertList.values().stream()
                .flatMap(List::stream) // List<Alert> -> Alert
                .map(alert -> alert.getCoin().getSymbol())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<TickerEntity> tickerList = alertRepository.findLatestTickersBySymbolList(allSymbols);

        // 최근 알람 히스토리를 한 번에 조회
        LocalDateTime minutesAgo = LocalDateTime.now().minusSeconds(30);
        List<Long> recentAlertIds = alertHistoryRepository.findRecentHistories(minutesAgo);
        Set<Long> recentAlertIdSet = new HashSet<>(recentAlertIds);
        for (Long userId : userEmitters.keySet()) {
            List<AlertEntity> activeAlerts = new ArrayList<>(activeAlertList.getOrDefault(userId, Collections.emptyList()));

            // 유효성 추가
            if (activeAlerts == null || activeAlerts.isEmpty()) continue;

            // 활성화된 알람 SSE로 보내기

            for (AlertEntity alert : activeAlerts) {
                String symbol = alert.getCoin().getSymbol();

                TickerEntity ticker = tickerList.stream()
                        .filter(t -> t.getId().getBaseSymbol().equals(symbol))
                        .findFirst()
                        .orElse(null);
                if (ticker != null) {
                    if (goldCrossAndTargetPriceService.isPriceReached(alert, ticker)) {
                        if (goldCrossAndTargetPriceService.isPriceStillValid(alert, recentAlertIdSet)) {
                            log.info("조건 부합 : 1분" + alert);
                            Queue<AlertEntity> queue = userAlertQueue.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>());

                            boolean alreadyQueued = queue.stream()
                                    .anyMatch(a -> a.getId().equals(alert.getId()));

                            // 이미 보냈던 애를 중복처리


                            if (!alreadyQueued) {
                                queue.add(alert);
                            }
                        }
                    }
                }
            }
        }
    }

    // 로그인한 사용자가 실행
    public SseEmitter subscribe(Long userId) {
        if(userId == null) { return null;}

        // 이미 존재하는 emitter가 있으면 재사용
        List<SseEmitter> existingEmitters = userEmitters.get(userId);
        if (existingEmitters != null) {
            // 살아있는 emitter만 필터링
            for (SseEmitter emitter : existingEmitters) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data("alive-check"));
                    log.info("살아있는 emitter 반환 - userId: {}", userId);
                    return emitter;
                } catch (IOException e) {
                    // 죽은 emitter는 건너뜀 (removeEmitter에서 자동 제거되도록 할 수도 있음)
                    log.warn("기존 emitter 죽어있음 - userId: {}", userId);
                    removeSingleEmitter(userId, emitter);
                }
            }
        }

        // 새 emitter 생성
        SseEmitter emitter = new SseEmitter(0L);
        userEmitters.computeIfAbsent(userId, k -> new ArrayList<>()).add(emitter);

        // emitter 정리 로직
        emitter.onCompletion(() -> removeEmitter(userId));
        emitter.onTimeout(() -> removeEmitter(userId));
        emitter.onError((e) -> removeEmitter(userId));

        log.info("🧪 [subscribe] userId={} 의 emitter 초기화 완료 후 새 emitter 생성", userId);
        log.info("📊 [subscribe] 현재 전체 userEmitters 수: {}", userEmitters.size());
        log.info("📊 [subscribe] userId={} 의 emitter 수: {}", userId, userEmitters.get(userId).size());

        // 알림 전송
        sendUserAlerts(userId, emitter);

        return emitter;
    }

    // 사용자의 기존 알람을 새로운 Emitter에게 전송
    public void sendUserAlerts(Long userId, SseEmitter emitter) {
        // 먼저 트랜잭션 내에서 Alert 목록만 가져옴
        List<AlertEntity> alerts = getAlertsToSend(userId);

        // 이후 I/O는 트랜잭션 밖에서 수행
        try {
            List<AlertSSEResponse> responseList = alerts.stream()
                    .map(AlertSSEResponse::new)
                    .toList();

            emitter.send(SseEmitter.event()
                    .name("existing-alerts")
                    .data(responseList)
            );

            // 히스토리 저장 비동기로 전환
            alerts.forEach(alert ->
                    saveAlertHistoryAsync(alert.getId(), userId)
            );

        } catch (IOException e) {
            removeEmitter(userId);
        }
    }

    @Transactional(readOnly = true)
    public List<AlertEntity> getAlertsToSend(Long userId) {
        List<AlertEntity> alertList =  Optional.ofNullable(activeAlertList.get(userId))
                .orElse(Collections.emptyList())
                .stream()
                .filter(alert -> alert.getIsTargetPrice() || alert.getIsGoldenCross())
                .collect(Collectors.toList());
        return alertList;
    }

    @Async
    public void saveAlertHistoryAsync(Long alertId, Long userId) {
        alertHistoryService.addAlertHistory(alertId, userId);
    }

    // 사용자의 기존 알람 SSE 전송
    @Transactional
    public void sendAlertToUserSSE(Long userId, AlertEntity alert) {
        List<SseEmitter> emitters = userEmitters.get(userId);

        if (emitters != null) {
            List<SseEmitter> failedEmitters = new ArrayList<>();
            AlertSSEResponse response = new AlertSSEResponse(alert);

            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("alert")
                            .data(response));
                } catch (Exception e) {
                    // 예외가 발생한 Emitter는 제거할 목록에 추가
                    failedEmitters.add(emitter);
                }
            }

            // 전송 실패한 Emitter를 리스트에서 제거
            emitters.removeAll(failedEmitters);
        }

        // 더 이상 연결이 없는 유저에 대해서는 Map에서 아예 지워버리고, 연결이 남아 있는 경우만 최신 상태로 다시 저장한다."
        if (emitters != null && !emitters.isEmpty()) {
            userEmitters.remove(userId);
        }

        // 알람 히스토리 저장
        saveAlertHistoryAsync(alert.getId(), userId);
    }

    // 사용자의 기존 알람 discord 전송
    public void sendAlertToUserDiscord(Long userId, AlertEntity alert) {
        // 최종 메시지 생성
        if(alert == null) {
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("👤 사용자 닉네임: ").append(alert.getUser().getNickname()).append("\n");
        messageBuilder.append("📢(코인) ").append(alert.getCoin().getName());
        messageBuilder.append(", (제목) " + alert.getTitle()).append("\n");
        if (alert.getUser().getDiscordWebhook() != null && !alert.getUser().getDiscordWebhook().isEmpty()) {
            discordService.sendDiscordAlert(alert.getUser().getDiscordWebhook(), messageBuilder.toString());
        }
    }

    // 사용자의 알람 스케줄러 discord 전송
    public void sendAlertListToUserDiscord(Long userId, List<AlertEntity> alerts) {
        StringBuilder messageBuilder = new StringBuilder();

        if (alerts.isEmpty()) {
            return;
        }
        if (alerts.get(0).getUser() == null) {
            return;
        }
        String nickname = alerts.get(0).getUser().getNickname();
        messageBuilder.append("👤 사용자 닉네임: ").append(alerts.get(0).getUser().getNickname()).append("\n");

        alerts.forEach(alert -> {
            messageBuilder.append("📢(코인) ").append(alert.getCoin().getSymbol())
                    .append(", (제목) ").append(alert.getTitle()).append("\n");
        });

        String message = messageBuilder.toString();

        // 디스코드로 메시지 전송
        if (alerts.get(0).getUser().getDiscordWebhook() != null && !alerts.get(0).getUser().getDiscordWebhook().isEmpty()) {
            discordService.sendDiscordAlert(alerts.get(0).getUser().getDiscordWebhook(), message);
        }
    }

    // 새로운 알람 추가
    public void addEmitter(Long userId, AlertEntity alert) {
        SseEmitter emitter = new SseEmitter(0L);
        // 내부 동작
        userEmitters.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(emitter);
        activeAlertList.computeIfAbsent(userId, k -> Collections.synchronizedList(new ArrayList<>())).add(alert);

        emitter.onCompletion(() -> removeEmitter(userId));
        emitter.onTimeout(() -> removeEmitter(userId));
        emitter.onError((e) -> removeEmitter(userId));

        log.info("📢 사용자 " + userId + " 에 대한 새로운 SSE 구독 추가됨. 활성화된 알람 개수: " + activeAlertList.get(userId).size());
    }

    // SSE 알람 제거
    public void deleteEmitter(Long userId, AlertEntity alert) {
        // 사용자의 알람 리스트에서 해당 알람 제거
        activeAlertList.computeIfPresent(userId, (k, alerts) -> {
            alerts.removeIf(a -> a.getId().equals(alert.getId())); // ✅ alertId가 동일한 경우만 삭제
            return alerts.isEmpty() ? null : alerts; // 리스트가 비면 null 반환해서 Map에서 삭제
        });

        log.info("사용자 " + userId + " 의 알람 제거됨. 남은 알람 개수: "
                + (activeAlertList.containsKey(userId) ? activeAlertList.get(userId).size() : 0));
    }

    // SSE 구독 취소
    public void removeEmitter(Long userId) {
        List<SseEmitter> emitters = userEmitters.remove(userId); // 해당 userId의 모든 SSE 제거

        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.complete(); // 안전하게 종료
                } catch (Exception e) {
                    log.warn("emitter 종료 중 예외 발생: {}", e.getMessage());
                }
            }
        }
        log.info("사용자 " + userId + " 의 모든 SSE 구독 취소 완료");
    }

    public void removeSingleEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            try {
                emitter.complete();
            } catch (Exception ignored) {}

            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public void updateUserNicknameInAlerts(Long userId, String newNickname) {
        // 1. activeAlertList 내 수정
        List<AlertEntity> alerts = activeAlertList.get(userId);
        if (alerts != null) {
            for (AlertEntity alert : alerts) {
                if (alert.getUser() != null) {
                    alert.getUser().updateNickname(newNickname);
                }
            }
        }

        // 2. userAlertQueue 내 수정
        Queue<AlertEntity> alertQueue = userAlertQueue.get(userId);
        if (alertQueue != null) {
            for (AlertEntity alert : alertQueue) {
                if (alert.getUser() != null) {
                    alert.getUser().updateNickname(newNickname);
                }
            }
        }

        log.info("✅ 유저 닉네임 갱신 완료: userId={}, newNickname={}", userId, newNickname);
    }


    public void updateUserWebhookInAlerts(Long userId, String newWebhook) {
        // 1. activeAlertList 내 수정
        List<AlertEntity> alerts = activeAlertList.get(userId);
        if (alerts != null) {
            for (AlertEntity alert : alerts) {
                if (alert.getUser() != null) {
                    alert.getUser().updateDiscordWebhook(newWebhook);
                }
            }
        }

        // 2. userAlertQueue 내 수정
        Queue<AlertEntity> alertQueue = userAlertQueue.get(userId);
        if (alertQueue != null) {
            for (AlertEntity alert : alertQueue) {
                if (alert.getUser() != null) {
                    alert.getUser().updateDiscordWebhook(newWebhook);
                }
            }
        }

        log.info("✅ 유저 웹훅 갱신 완료: userId={}, newWebhook={}", userId, newWebhook);
    }
}

