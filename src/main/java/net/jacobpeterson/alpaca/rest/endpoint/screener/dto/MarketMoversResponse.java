package net.jacobpeterson.alpaca.rest.endpoint.screener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MarketMoversResponse {
    List<StockChangeInfo> gainers;
    List<StockChangeInfo> losers;
    String market_type;
    String last_updated;
}
