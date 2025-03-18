package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertFilterRequest extends PaginationRequest {

    @Null
    private Boolean active;  // 활성화된 알람 여부

    @NotNull
    private String filter;  // 코인 심볼 (BTC 등)

    @NotNull
    private String sort;  // 정렬 방식 (LATEST / OLDEST)
}
