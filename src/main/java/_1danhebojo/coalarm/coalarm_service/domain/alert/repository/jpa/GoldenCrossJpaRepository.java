package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.GoldenCrossAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GoldenCrossJpaRepository extends JpaRepository<GoldenCrossAlert, Long> {
    @Modifying
    @Query("DELETE FROM TargetPriceAlert t WHERE t.alert.alertId = :alertId")
    void deleteByAlertId(@Param("alertId") Long alertId);

}
