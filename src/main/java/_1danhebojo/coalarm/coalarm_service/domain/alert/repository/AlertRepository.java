package _1danhebojo.coalarm.coalarm_service.domain.alert.repository;

import _1danhebojo.coalarm.coalarm_service.domain.dashboard.repository.entity.TickerEntity;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.AlertEntity;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.GoldenCrossEntity;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.TargetPriceEntity;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.VolumeSpikeEntity;
import _1danhebojo.coalarm.coalarm_service.domain.coin.repository.entity.CoinEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface AlertRepository {
    Optional<AlertEntity> findById(Long alertId);
    Optional<AlertEntity> findByIdWithCoin(Long alertId);
    void deleteById(Long alertId);
    void deleteByUserId(Long userId);
    AlertEntity save(AlertEntity alert);
    List<Long> findAlertIdsByUserId(Long userId);
    List<AlertEntity> findAllActiveAlerts();
    Page<AlertEntity> findAllUserAlerts(Long userId, String symbol, Boolean active, String sort, int offset, int limit);
    Optional<CoinEntity> findCoinBySymbol(String symbol);
    boolean findAlertsByUserIdAndSymbolAndAlertType(Long userId, String symbol, String alertType, Long alarmCountLimit);
    List<TickerEntity> findLatestTickersBySymbolList(List<String> symbolList);
}
