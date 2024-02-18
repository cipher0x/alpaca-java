package net.jacobpeterson.alpaca.rest.endpoint.screener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockChangeInfo {
    String symbol;
    Double percent_change;
    Double change;
    Double price;
}
