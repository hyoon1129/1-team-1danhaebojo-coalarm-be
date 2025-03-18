package _1danhebojo.coalarm.coalarm_service.domain.alert.controller.response;

import _1danhebojo.coalarm.coalarm_service.domain.alert.repository.entity.Coin;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinResponse {
    private Long id;
    private String name;
    private String symbol;

    public CoinResponse(Coin coin) {
        if (coin != null) {
            this.id = coin.getCoinId();
            this.name = coin.getName();
            this.symbol = coin.getSymbol();
        }
    }
}

