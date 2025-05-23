package _1danhebojo.coalarm.coalarm_service.domain.dashboard.repository.jpa;

import _1danhebojo.coalarm.coalarm_service.domain.dashboard.repository.entity.CoinIndicatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoinIndicatorJpaRepository extends JpaRepository<CoinIndicatorEntity,Long> {
    Optional<CoinIndicatorEntity> findTopByCoinIdOrderByRegDtDesc(Long coinId);
}
