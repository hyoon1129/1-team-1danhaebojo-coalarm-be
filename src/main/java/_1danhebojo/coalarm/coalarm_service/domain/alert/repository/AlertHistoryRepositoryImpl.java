package _1danhebojo.coalarm.coalarm_service.domain.alert.repository;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.AlertHistory;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa.AlertHistoryJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AlertHistoryRepositoryImpl {

    private final AlertHistoryJpaRepository alertHistoryJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Page<AlertHistory> findAlertHistoryByFilter(Long userId, Pageable pageable) {
        List<AlertHistory> alertHistories = alertHistoryJpaRepository.findByUserId(userId);

        // PageImpl을 이용해 Pageable 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), alertHistories.size());

        return new PageImpl<>(alertHistories.subList(start, end), pageable, alertHistories.size());
    }

    @Transactional
    public void save(AlertHistory alertHistory) {
        alertHistoryJpaRepository.save(alertHistory);
    }

    public Optional<AlertHistory> findById(Long alertHistoryId) {
        return alertHistoryJpaRepository.findById(alertHistoryId);
    }
}