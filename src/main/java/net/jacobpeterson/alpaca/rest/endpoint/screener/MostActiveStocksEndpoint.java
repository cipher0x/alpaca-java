package net.jacobpeterson.alpaca.rest.endpoint.screener;

import net.jacobpeterson.alpaca.rest.AlpacaClient;
import net.jacobpeterson.alpaca.rest.AlpacaClientException;
import net.jacobpeterson.alpaca.rest.endpoint.AlpacaEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.screener.dto.ActiveBy;
import net.jacobpeterson.alpaca.rest.endpoint.screener.dto.MarketMoversResponse;
import net.jacobpeterson.alpaca.rest.endpoint.screener.dto.MostActiveResponse;
import okhttp3.HttpUrl;
import okhttp3.Request;

import static com.google.common.base.Preconditions.checkNotNull;

public class MostActiveStocksEndpoint extends AlpacaEndpoint {

    public MostActiveStocksEndpoint(AlpacaClient alpacaClient) {
        super(alpacaClient, "screener");
    }

    /**
     * Instantiates a new {@link AlpacaEndpoint}.
     *
     * @param alpacaClient        the {@link AlpacaClient}
     * @param endpointPathSegment the endpoint path segment relative to {@link AlpacaClient#getBaseURL()}
     */
    public MostActiveStocksEndpoint(AlpacaClient alpacaClient, String endpointPathSegment) {
        super(alpacaClient, endpointPathSegment);
    }

    public MostActiveResponse getMostActiveStocks(ActiveBy activeBy, Integer limit) throws AlpacaClientException {
        checkNotNull(activeBy);
        checkNotNull(limit);

        HttpUrl httpUrl = alpacaClient.urlBuilder()
                .addPathSegment(endpointPathSegment)
                .addPathSegment("stocks")
                .addPathSegment("most-actives")
                .addQueryParameter("by", activeBy.toString())
                .addQueryParameter("top", limit.toString()).build();
        Request request = alpacaClient.requestBuilder(httpUrl)
                .get()
                .build();
        System.out.println(httpUrl);
         return alpacaClient.requestObject(request, MostActiveResponse.class);
    }

    public MarketMoversResponse getMarketMoverStocks(Integer limit) throws AlpacaClientException {
        checkNotNull(limit);

        HttpUrl httpUrl = alpacaClient.urlBuilder()
                .addPathSegment(endpointPathSegment)
                .addPathSegment("stocks")
                .addPathSegment("movers")
                .addQueryParameter("top", limit.toString()).build();
        Request request = alpacaClient.requestBuilder(httpUrl)
                .get()
                .build();
        System.out.println(httpUrl);
        return alpacaClient.requestObject(request, MarketMoversResponse.class);
    }
}
