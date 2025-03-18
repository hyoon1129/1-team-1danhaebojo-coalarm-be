package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TargetPriceAlertRequest.class, name = "TARGET_PRICE"),
        @JsonSubTypes.Type(value = GoldenCrossAlertRequest.class, name = "GOLDEN_CROSS"),
        @JsonSubTypes.Type(value = VolumeSpikeAlertRequest.class, name = "VOLUME_SPIKE")
})
public abstract class BaseAlertRequest {
    @Null
    private Long alertId; // 알림 ID

    private String type; // 알람 타입 (TARGET_PRICE, GOLDEN_CROSS, VOLUME_SPIKE)

    private String title; // 알람 제목

    private Boolean isTradingVolumeSoaring; // boolean 기본값은 false
    private Boolean isTargetPrice;
    private Boolean isGoldenCross;

    @JsonProperty("status")
    private Boolean active= true;

    @JsonProperty("coin_id")
    private Long coinId; // 코인 ID

    @JsonProperty("user_id") // 중복 문제 해결
    private Long userId; // 유저 ID
}

