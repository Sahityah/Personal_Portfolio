package com.Personal_Portfolio.Personal_Portfolio.Integrator;

import com.Personal_Portfolio.Personal_Portfolio.Entity.StockSymbol;
import com.Personal_Portfolio.Personal_Portfolio.Service.StockSymbolService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NseMarketDataFetcher {

    private final WebClient.Builder webClientBuilder; // Use builder for custom configurations
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final StockSymbolService stockSymbolService; // To get list of active symbols

    @Value("${market.data.api.base-url}")
    private String marketDataBaseUrl;

    @Value("${market.data.api.key}")
    private String marketDataApiKey;

    private static final String MARKET_DATA_TOPIC = "market-data-feed";

    // This method will be scheduled to run periodically
    @Scheduled(fixedRateString = "${market.data.fetch.interval}") // e.g., every 5 seconds
    public void fetchAndPublishMarketData() {
        // In a real application, you'd fetch symbols for which users have open positions
        // For simplicity, fetching for a few predefined symbols or all loaded symbols.
        // It's better to only fetch data for symbols that are actively being tracked by users.
        List<String> symbolsToFetch = stockSymbolService.getAllStockSymbols().stream()
                .map(StockSymbol::getSymbol)
                .limit(20) // Limit the number of symbols to fetch in one go to avoid API rate limits
                .collect(Collectors.toList());

        if (symbolsToFetch.isEmpty()) {
            System.out.println("No symbols to fetch market data for.");
            return;
        }

        System.out.println("Fetching market data for: " + symbolsToFetch);

        for (String symbol : symbolsToFetch) {
            // Construct the API URL for the specific market data provider
            // THIS IS A PLACEHOLDER. You MUST replace this with your actual NSE data API endpoint and parameters.
            // Example for a generic API:
            String apiUrl = marketDataBaseUrl + "/quote?symbol=" + symbol + "&apikey=" + marketDataApiKey;

            webClientBuilder.build().get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(Map.class) // Expecting a JSON map response
                    .subscribe(
                            response -> {
                                // Parse the response based on your chosen API's structure
                                // THIS IS A PLACEHOLDER PARSING LOGIC
                                if (response != null && response.containsKey("ltp")) {
                                    Map<String, Object> marketData = new HashMap<>();
                                    marketData.put("symbol", symbol);
                                    marketData.put("ltp", ((Number)response.get("ltp")).doubleValue());
                                    marketData.put("timestamp", System.currentTimeMillis());
                                    // Add other relevant data like open, high, low, close, volume, etc. if available
                                    // marketData.put("open", ((Number)response.get("open")).doubleValue());
                                    // marketData.put("high", ((Number)response.get("high")).doubleValue());

                                    kafkaTemplate.send(MARKET_DATA_TOPIC, symbol, marketData);
                                    // System.out.println("Published market data for " + symbol + ": " + marketData.get("ltp"));
                                } else {
                                    System.err.println("No LTP found in response for symbol: " + symbol + " | Response: " + response);
                                }
                            },
                            error -> System.err.println("Error fetching market data for " + symbol + ": " + error.getMessage())
                    );
        }
    }


}
