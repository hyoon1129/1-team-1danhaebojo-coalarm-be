package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AlertListResponse {
    private List<AlertResponse> contents;
    private int offset;
    private int limit;
    private long totalElements;
    private boolean hasNext;
}