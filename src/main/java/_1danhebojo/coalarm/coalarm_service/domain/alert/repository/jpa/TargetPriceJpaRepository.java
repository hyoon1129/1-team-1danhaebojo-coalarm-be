package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.TargetPriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TargetPriceJpaRepository extends JpaRepository<TargetPriceEntity,Long> {
    @Modifying
    @Query("DELETE FROM TargetPriceEntity t WHERE t.alert.id IN :alertIds")
    void deleteByAlertIdIn(@Param("alertIds") List<Long> alertIds);
}
