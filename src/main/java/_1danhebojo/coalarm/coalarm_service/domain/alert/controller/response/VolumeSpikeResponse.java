package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.VolumeSpikeAlert;
import lombok.Getter;

@Getter
public class VolumeSpikeResponse {
    private boolean tradingVolumeSoaring;

    public VolumeSpikeResponse(VolumeSpikeAlert volumeSpike) {
        if (volumeSpike != null) {
            this.tradingVolumeSoaring = volumeSpike.isTradingVolumeSoaring();
        }
    }
}
