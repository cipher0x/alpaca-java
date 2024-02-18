package net.jacobpeterson.alpaca.rest.endpoint.screener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
public class MostActiveStock {
    private String symbol;
    private String volume;
    private Integer trade_count;
}

