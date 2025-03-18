package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginationRequest {

    @NotNull
    @Min(0)
    private Integer offset;  // 가져올 데이터 시작점

    @NotNull
    @Min(1)
    private Integer limit;  // 가져올 데이터 개수 (최소 1)
}
