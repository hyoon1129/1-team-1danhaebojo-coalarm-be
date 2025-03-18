package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseAlertStatusRequest {
    @JsonProperty("status")
    private boolean active;
}
