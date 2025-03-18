package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VolumeSpikeAlertRequest extends BaseAlertRequest {
    @NotNull
    private Long marketId;

    @NotNull
    private Boolean tradingVolumeSoaring;
}
