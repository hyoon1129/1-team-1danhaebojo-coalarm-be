package _1danhebojo.coalarm.coalarm_service.domain.dashboard.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class CandleCompositeKey implements Serializable {

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "exchange", nullable = false)
    private String exchange;

    @Column(name = "base_symbol", nullable = false)
    private String baseSymbol;

    @Column(name = "quote_symbol", nullable = false)
    private String quoteSymbol;

    @Column(name = "timeframe", nullable = false)
    private String timeframe;
}
