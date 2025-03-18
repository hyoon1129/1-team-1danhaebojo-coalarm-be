package _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.AlertHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertHistoryJpaRepository extends JpaRepository<AlertHistory, Long> {

    // ✅ alert, coin 정보를 한 번에 가져오도록 fetch join 설정
    @Query("SELECT ah FROM AlertHistory ah " +
            "JOIN FETCH ah.alert a " +
            "JOIN FETCH a.coin c " + // ✅ Coin 정보까지 함께 가져오기
            "WHERE ah.userId = :userId")
    List<AlertHistory> findByUserId(@Param("userId") Long userId);
}
