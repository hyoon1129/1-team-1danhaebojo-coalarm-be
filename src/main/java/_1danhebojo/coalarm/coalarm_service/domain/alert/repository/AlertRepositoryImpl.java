package _1danhebojo.coalarm.coalarm_service.domain.alert.repository;

import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.GoldenCrossAlertRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.TargetPriceAlertRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request.VolumeSpikeAlertRequest;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.*;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa.AlertJpaRepository;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa.GoldenCrossJpaRepository;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa.TargetPriceJpaRepository;
import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.jpa.VolumeSpikeJpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class AlertRepositoryImpl {

    private final AlertJpaRepository alertJpaRepository;
    private final TargetPriceJpaRepository targetPriceJpaRepository;
    private final GoldenCrossJpaRepository goldenCrossJpaRepository;
    private final VolumeSpikeJpaRepository volumeSpikeJpaRepository;

    @PersistenceContext
    private EntityManager entityManager; // ★ EntityManager 추가

    public AlertRepositoryImpl(AlertJpaRepository alertJpaRepository,
                               TargetPriceJpaRepository targetPriceJpaRepository,
                               GoldenCrossJpaRepository goldenCrossJpaRepository,
                               VolumeSpikeJpaRepository volumeSpikeJpaRepository) {
        this.alertJpaRepository = alertJpaRepository;
        this.targetPriceJpaRepository = targetPriceJpaRepository;
        this.goldenCrossJpaRepository = goldenCrossJpaRepository;
        this.volumeSpikeJpaRepository = volumeSpikeJpaRepository;
    }

    @Transactional
    public Long saveTargetPriceAlert(TargetPriceAlertRequest request) {
        TargetPriceAlert targetPriceAlert = new TargetPriceAlert();
        targetPriceAlert.setPrice(request.getPrice());
        targetPriceAlert.setPercentage(request.getPercentage());

        Alert alert = new Alert();
        alert.setAlertId(request.getAlertId());
        targetPriceAlert.setAlert(alert);

        TargetPriceAlert savedTargetPriceAlert = targetPriceJpaRepository.save(targetPriceAlert);
        entityManager.flush();
        return savedTargetPriceAlert.getTargetPriceId();
    }

    @Transactional
    public Long saveGoldenCrossAlert(GoldenCrossAlertRequest request) {
        GoldenCrossAlert goldenCrossAlert = new GoldenCrossAlert();
        goldenCrossAlert.setLongMa(request.getLongMa());
        goldenCrossAlert.setShortMa(request.getShortMa());

        Alert alert = new Alert();
        alert.setAlertId(request.getAlertId());
        goldenCrossAlert.setAlert(alert);

        GoldenCrossAlert savedGoldenCrossAlert = goldenCrossJpaRepository.save(goldenCrossAlert);
        entityManager.flush();  // ★ 즉시 반영
        return savedGoldenCrossAlert.getGoldenCrossId();
    }

    @Transactional
    public Long saveVolumeSpikeAlert(VolumeSpikeAlertRequest request) {
        VolumeSpikeAlert volumeSpikeAlert = new VolumeSpikeAlert();
        volumeSpikeAlert.setTradingVolumeSoaring(request.getTradingVolumeSoaring());

        Alert alert = new Alert();
        alert.setAlertId(request.getAlertId());
        volumeSpikeAlert.setAlert(alert);

        VolumeSpikeAlert savedVolumeSpikeAlert = volumeSpikeJpaRepository.save(volumeSpikeAlert);
        entityManager.flush();  // ★ 즉시 반영
        return savedVolumeSpikeAlert.getMarketAlertId();
    }

    public List<Alert> findAll() {
        return alertJpaRepository.findAll();
    }

    public Optional<Alert> findById(Long alertId) {
        return alertJpaRepository.findById(alertId);
    }

    @Transactional
    public void deleteById(Long alertId) {
        targetPriceJpaRepository.deleteByAlertId(alertId);
        goldenCrossJpaRepository.deleteByAlertId(alertId);
        volumeSpikeJpaRepository.deleteByAlertId(alertId);

        alertJpaRepository.deleteById(alertId);
    }

    public Alert save(Alert alert) {
        Alert savedAlert = alertJpaRepository.save(alert);
        entityManager.flush();  // ★ 즉시 DB 반영하여 ID 생성
        return savedAlert;
    }

    @Transactional
    public Page<Alert> findAlertsByFilter(Boolean active, String filter, Pageable pageable) {
        return alertJpaRepository.findAlertsByFilter(active, filter, pageable);
    }

}

