package net.jacobpeterson.alpaca;

import devcsrj.okhttp3.logging.HttpLoggingInterceptor;
import net.jacobpeterson.alpaca.model.properties.DataAPIType;
import net.jacobpeterson.alpaca.model.properties.EndpointAPIType;
import net.jacobpeterson.alpaca.properties.AlpacaProperties;
import net.jacobpeterson.alpaca.rest.AlpacaClient;
import net.jacobpeterson.alpaca.rest.endpoint.AlpacaEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.account.AccountEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.accountactivities.AccountActivitiesEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.accountconfiguration.AccountConfigurationEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.assets.AssetsEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.calendar.CalendarEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.clock.ClockEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.marketdata.crypto.CryptoMarketDataEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.marketdata.stock.StockMarketDataEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.orders.OrdersEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.portfoliohistory.PortfolioHistoryEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.positions.PositionsEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.screener.MostActiveStocksEndpoint;
import net.jacobpeterson.alpaca.rest.endpoint.watchlist.WatchlistEndpoint;
import net.jacobpeterson.alpaca.websocket.AlpacaWebsocket;
import net.jacobpeterson.alpaca.websocket.marketdata.MarketDataWebsocketInterface;
import net.jacobpeterson.alpaca.websocket.marketdata.crypto.CryptoMarketDataWebsocket;
import net.jacobpeterson.alpaca.websocket.marketdata.stock.StockMarketDataWebsocket;
import net.jacobpeterson.alpaca.websocket.streaming.StreamingWebsocket;
import net.jacobpeterson.alpaca.websocket.streaming.StreamingWebsocketInterface;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@link AlpacaAPI} class contains several instances of various {@link AlpacaEndpoint}s and
 * {@link AlpacaWebsocket}s to interface with Alpaca. You will generally only need one instance of this class in your
 * application. Note that many methods inside the various {@link AlpacaEndpoint}s allow <code>null<code/> to be passed
 * in as a parameter if it is optional.
 *
 * @see <a href="https://docs.alpaca.markets/api-documentation/api-v2/">Alpaca API Documentation</a>
 */
public class AlpacaAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlpacaAPI.class);

    private static final String VERSION_2_PATH_SEGMENT = "v2";
    private static final String VERSION_1_BETA_3_PATH_SEGMENT = "v1beta3";
    private static final String VERSION_1_BETA_1_PATH_SEGMENT = "v1beta1";

    private final OkHttpClient okHttpClient;
    private final AlpacaClient brokerClient;
    private final AlpacaClient cryptoDataClient;
    private final AlpacaClient stockDataClient;
    private final AlpacaClient screenerDataClient;

    // Ordering of fields/methods below are analogous to the ordering in the Alpaca documentation
    private final AccountEndpoint accountEndpoint;
    private final CryptoMarketDataEndpoint cryptoMarketDataEndpoint;
    private final StockMarketDataEndpoint stockMarketDataEndpoint;
    private final MostActiveStocksEndpoint screenerDataEndpoint;

    private final OrdersEndpoint ordersEndpoint;
    private final PositionsEndpoint positionsEndpoint;
    private final AssetsEndpoint assetsEndpoint;
    private final WatchlistEndpoint watchlistEndpoint;
    private final CalendarEndpoint calendarEndpoint;
    private final ClockEndpoint clockEndpoint;
    private final AccountConfigurationEndpoint accountConfigurationEndpoint;
    private final AccountActivitiesEndpoint accountActivitiesEndpoint;
    private final PortfolioHistoryEndpoint portfolioHistoryEndpoint;
    private final StreamingWebsocket streamingWebsocket;
    private final CryptoMarketDataWebsocket cryptoMarketDataWebsocket;
    private final StockMarketDataWebsocket stockMarketDataWebsocket;

    /**
     * Instantiates a new {@link AlpacaAPI} using properties specified in <code>alpaca.properties</code> file (or their
     * associated defaults).
     */
    public AlpacaAPI() {
        this(AlpacaProperties.KEY_ID,
                AlpacaProperties.SECRET_KEY,
                AlpacaProperties.ENDPOINT_API_TYPE,
                AlpacaProperties.DATA_API_TYPE);
    }

    /**
     * Instantiates a new {@link AlpacaAPI} using properties specified in the given {@link Builder}, otherwise from
     * <code>alpaca.properties</code> file (or their associated defaults).
     */
    private AlpacaAPI(Builder builder) {
        this(builder.keyID,
                builder.secretKey,
                builder.endpointAPIType,
                builder.dataAPIType);
    }

    /**
     * Instantiates a new {@link AlpacaAPI}.
     *
     * @param keyID     the key ID
     * @param secretKey the secret key
     */
    public AlpacaAPI(String keyID, String secretKey) {
        this(null, keyID, secretKey, null,
                AlpacaProperties.ENDPOINT_API_TYPE,
                AlpacaProperties.DATA_API_TYPE);
    }

    /**
     * Instantiates a new {@link AlpacaAPI}.
     *
     * @param keyID           the key ID
     * @param secretKey       the secret key
     * @param endpointAPIType the {@link EndpointAPIType}
     * @param dataAPIType     the {@link DataAPIType}
     */
    public AlpacaAPI(String keyID, String secretKey, EndpointAPIType endpointAPIType, DataAPIType dataAPIType) {
        this(null, keyID, secretKey, null, endpointAPIType, dataAPIType);
    }

    /**
     * Instantiates a new {@link AlpacaAPI}.
     *
     * @param oAuthToken the OAuth token. Note that the Data API v2 does not work with OAuth tokens.
     */
    public AlpacaAPI(String oAuthToken) {
        this(null, null, null, oAuthToken,
                AlpacaProperties.ENDPOINT_API_TYPE,
                AlpacaProperties.DATA_API_TYPE);
    }

    /**
     * Instantiates a new {@link AlpacaAPI}.
     *
     * @param okHttpClient    the {@link OkHttpClient} or <code>null</code> to create a default instance
     * @param keyID           the key ID
     * @param secretKey       the secret key
     * @param oAuthToken      the OAuth token
     * @param endpointAPIType the {@link EndpointAPIType}
     * @param dataAPIType     the {@link DataAPIType}
     */
    public AlpacaAPI(OkHttpClient okHttpClient, String keyID, String secretKey, String oAuthToken,
            EndpointAPIType endpointAPIType, DataAPIType dataAPIType) {
        checkArgument((keyID != null && secretKey != null) ^ oAuthToken != null,
                "You must specify a (KeyID (%s) and Secret Key (%s)) or an OAuthToken (%s)!",
                keyID, secretKey, oAuthToken);
        checkNotNull(endpointAPIType);
        checkNotNull(dataAPIType);

        // Create default 'okHttpClient'
        if (okHttpClient == null) {
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .cache(null); // Ensure response caching is disabled

            if (LOGGER.isDebugEnabled()) {
                clientBuilder.addInterceptor(new HttpLoggingInterceptor(LOGGER));
            }

            okHttpClient = clientBuilder.build();
        }

        this.okHttpClient = okHttpClient;

        String brokerHostSubdomain;
        switch (endpointAPIType) {
            case LIVE:
                brokerHostSubdomain = "api";
                break;
            case PAPER:
                brokerHostSubdomain = "paper-api";
                break;
            default:
                throw new UnsupportedOperationException();
        }

        if (oAuthToken == null) {
            brokerClient = new AlpacaClient(okHttpClient, keyID, secretKey,
                    brokerHostSubdomain, VERSION_2_PATH_SEGMENT);
            cryptoDataClient = new AlpacaClient(okHttpClient, keyID, secretKey, "data", VERSION_1_BETA_3_PATH_SEGMENT);
            stockDataClient = new AlpacaClient(okHttpClient, keyID, secretKey, "data", VERSION_2_PATH_SEGMENT);
            screenerDataClient = new AlpacaClient(okHttpClient, keyID, secretKey, "data", VERSION_1_BETA_1_PATH_SEGMENT);

        } else {
            brokerClient = new AlpacaClient(okHttpClient, oAuthToken, brokerHostSubdomain, VERSION_2_PATH_SEGMENT);
            cryptoDataClient = null;
            stockDataClient = null;
            screenerDataClient = null;
        }

        accountEndpoint = new AccountEndpoint(brokerClient);
        cryptoMarketDataEndpoint = cryptoDataClient == null ? null : new CryptoMarketDataEndpoint(cryptoDataClient);
        stockMarketDataEndpoint = stockDataClient == null ? null : new StockMarketDataEndpoint(stockDataClient);
        screenerDataEndpoint = screenerDataClient == null ? null : new MostActiveStocksEndpoint(screenerDataClient);
        ordersEndpoint = new OrdersEndpoint(brokerClient);
        positionsEndpoint = new PositionsEndpoint(brokerClient);
        assetsEndpoint = new AssetsEndpoint(brokerClient);
        watchlistEndpoint = new WatchlistEndpoint(brokerClient);
        calendarEndpoint = new CalendarEndpoint(brokerClient);
        clockEndpoint = new ClockEndpoint(brokerClient);
        accountConfigurationEndpoint = new AccountConfigurationEndpoint(brokerClient);
        accountActivitiesEndpoint = new AccountActivitiesEndpoint(brokerClient);
        portfolioHistoryEndpoint = new PortfolioHistoryEndpoint(brokerClient);

        streamingWebsocket = new StreamingWebsocket(okHttpClient, brokerHostSubdomain, keyID, secretKey, oAuthToken);
        cryptoMarketDataWebsocket = cryptoDataClient == null ? null :
                new CryptoMarketDataWebsocket(okHttpClient, keyID, secretKey);
        stockMarketDataWebsocket = stockDataClient == null ? null :
                new StockMarketDataWebsocket(okHttpClient, dataAPIType, keyID, secretKey);
    }

    /**
     * @return the {@link AccountEndpoint}
     */
    public AccountEndpoint account() {
        return accountEndpoint;
    }

    /**
     * @return the {@link CryptoMarketDataEndpoint}
     */
    public CryptoMarketDataEndpoint cryptoMarketData() {
        return cryptoMarketDataEndpoint;
    }

    /**
     * @return the {@link StockMarketDataEndpoint}
     */
    public StockMarketDataEndpoint stockMarketData() {
        return stockMarketDataEndpoint;
    }

    /**
     * @return the {@link OrdersEndpoint}
     */
    public OrdersEndpoint orders() {
        return ordersEndpoint;
    }

    /**
     * @return the {@link PositionsEndpoint}
     */
    public PositionsEndpoint positions() {
        return positionsEndpoint;
    }

    /**
     * @return the {@link AssetsEndpoint}
     */
    public AssetsEndpoint assets() {
        return assetsEndpoint;
    }

    /**
     * @return the {@link WatchlistEndpoint}
     */
    public WatchlistEndpoint watchlist() {
        return watchlistEndpoint;
    }

    /**
     * @return the {@link CalendarEndpoint}
     */
    public CalendarEndpoint calendar() {
        return calendarEndpoint;
    }

    /**
     * @return the {@link ClockEndpoint}
     */
    public ClockEndpoint clock() {
        return clockEndpoint;
    }

    /**
     * @return the {@link AccountConfigurationEndpoint}
     */
    public AccountConfigurationEndpoint accountConfiguration() {
        return accountConfigurationEndpoint;
    }

    /**
     * @return the {@link AccountActivitiesEndpoint}
     */
    public AccountActivitiesEndpoint accountActivities() {
        return accountActivitiesEndpoint;
    }

    /**
     * @return the {@link PortfolioHistoryEndpoint}
     */
    public PortfolioHistoryEndpoint portfolioHistory() {
        return portfolioHistoryEndpoint;
    }

    /**
     * @return the {@link StreamingWebsocketInterface}
     */
    public StreamingWebsocketInterface streaming() {
        return streamingWebsocket;
    }

    /**
     * @return the Crypto {@link MarketDataWebsocketInterface}
     */
    public MarketDataWebsocketInterface cryptoMarketDataStreaming() {
        return cryptoMarketDataWebsocket;
    }

    /**
     * @return the Stock {@link MarketDataWebsocketInterface}
     */
    public MarketDataWebsocketInterface stockMarketDataStreaming() {
        return stockMarketDataWebsocket;
    }

    public MostActiveStocksEndpoint mostActiveStocksEndpoint() {return screenerDataEndpoint;}
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public AlpacaClient getBrokerClient() {
        return brokerClient;
    }

    public AlpacaClient getCryptoDataClient() {
        return cryptoDataClient;
    }

    public AlpacaClient getStockDataClient() {
        return stockDataClient;
    }

    /**
     * Creates a {@link Builder} for {@link AlpacaAPI}.
     *
     * @return a {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder for {@link AlpacaAPI}
     */
    public static final class Builder {

        private String keyID;
        private String secretKey;
        private EndpointAPIType endpointAPIType;
        private DataAPIType dataAPIType;

        private Builder() {
            this.keyID = AlpacaProperties.KEY_ID;
            this.secretKey = AlpacaProperties.SECRET_KEY;
            this.endpointAPIType = AlpacaProperties.ENDPOINT_API_TYPE;
            this.dataAPIType = AlpacaProperties.DATA_API_TYPE;
        }

        public Builder withKeyID(String val) {
            keyID = val;
            return this;
        }

        public Builder withSecretKey(String val) {
            secretKey = val;
            return this;
        }

        public Builder withEndpointAPIType(EndpointAPIType val) {
            endpointAPIType = val;
            return this;
        }

        public Builder withDataAPIType(DataAPIType val) {
            dataAPIType = val;
            return this;
        }

        public AlpacaAPI build() {
            return new AlpacaAPI(this);
        }
    }
}
