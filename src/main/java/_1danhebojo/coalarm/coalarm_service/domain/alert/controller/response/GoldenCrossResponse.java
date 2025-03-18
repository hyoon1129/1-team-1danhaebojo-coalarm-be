package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.GoldenCrossAlert;
import lombok.Getter;

@Getter
public class GoldenCrossResponse {
    private long shortMa;
    private long longMa;

    public GoldenCrossResponse(GoldenCrossAlert goldenCross) {
        if (goldenCross != null) {
            this.shortMa = goldenCross.getShortMa();
            this.longMa = goldenCross.getLongMa();
        }
    }
}
