package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.TargetPriceAlert;
import lombok.Getter;

@Getter
public class TargetPriceResponse {
    private double price;
    private int percentage;

    public TargetPriceResponse(TargetPriceAlert targetPrice) {
        if (targetPrice != null) {
            this.price = targetPrice.getPrice();
            this.percentage = targetPrice.getPercentage();
        }
    }
}
