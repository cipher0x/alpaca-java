package net.jacobpeterson.alpaca.rest.endpoint.screener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MostActiveResponse {
private List<MostActiveStock> most_actives;
private String last_updated;
}
