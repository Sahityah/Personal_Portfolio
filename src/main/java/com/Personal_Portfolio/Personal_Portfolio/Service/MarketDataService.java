package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.DTO.CandlestickDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.IndexDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.SecurityDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap; // Import for ConcurrentHashMap
import java.util.stream.Collectors;

@Service
public class MarketDataService {

    private final Random random = new Random();

    // Add this map to simulate prices for getLatestPrice and getLatestPricesForSymbols
    private final Map<String, Double> dummyPrices = new ConcurrentHashMap<>();

    public MarketDataService() {
        // Initialize dummy prices in the constructor
        dummyPrices.put("RELIANCE", 2900.0);
        dummyPrices.put("TCS", 3800.0);
        dummyPrices.put("INFY", 1500.0);
        dummyPrices.put("HDFCBANK", 1600.0);
        dummyPrices.put("ICICIBANK", 1100.0); // Added for completeness based on getCurrentLTP
        dummyPrices.put("NIFTY", 22500.0);
        dummyPrices.put("BANKNIFTY", 48000.0);
        dummyPrices.put("FINNIFTY", 21000.0); // Added for completeness based on getIndices
        dummyPrices.put("TATAMOTORS", 950.0);
        dummyPrices.put("SBIN", 800.0);
    }

    public List<IndexDataDto> getIndices() {
        // Simulate real-time index data
        return Arrays.asList(
                new IndexDataDto("NIFTY 50", 22500.0 + random.nextDouble() * 100, (random.nextDouble() * 2 - 1)), // -1% to +1%
                new IndexDataDto("BANKNIFTY", 48000.0 + random.nextDouble() * 200, (random.nextDouble() * 2 - 1.2)),
                new IndexDataDto("FINNIFTY", 21000.0 + random.nextDouble() * 150, (random.nextDouble() * 2 - 0.8))
        );
    }

    public List<CandlestickDataDto> getChartData(String symbol, String interval) {
        // Simulate fetching candlestick data for a symbol and interval
        // In a real app, this would hit a historical data API
        int days = 60;
        switch (interval) {
            case "1m": days = 1; break; // A day's worth of 1-min data
            case "5m": days = 2; break;
            case "15m": days = 5; break;
            case "30m": days = 10; break;
            case "1h": days = 20; break;
            case "1w": days = 90; break;
            case "1M": days = 365; break; // A year's worth of monthly data
            case "1d":
            default: days = 60; break; // 60 days of daily data
        }
        return generateCandlestickData(days);
    }

    /**
     * Retrieves the latest price for a single trading symbol.
     * In a real application, this would call an external market data API.
     *
     * @param symbol The trading symbol (e.g., "RELIANCE").
     * @return The latest traded price (LTP) for the symbol, or a default value if not found.
     */
    public Double getLatestPrice(String symbol) {
        // In a real scenario, this would call an external API
        // For now, return a slightly randomized version of dummy price
        Double basePrice = dummyPrices.getOrDefault(symbol.toUpperCase(), 100.0); // Default if not found
        return basePrice * (1 + (Math.random() - 0.5) * 0.01); // +/- 0.5% random fluctuation
    }

    /**
     * Retrieves the latest prices for a list of trading symbols.
     * This method is called by PortfolioService to get LTPs for all positions.
     *
     * @param symbols A list of trading symbols.
     * @return A Map where keys are symbols and values are their latest traded prices.
     */
    public Map<String, Double> getLatestPricesForSymbols(List<String> symbols) {
        return symbols.stream()
                .distinct() // Ensure unique symbols
                .collect(Collectors.toMap(
                        symbol -> symbol,
                        this::getLatestPrice // Use the method to get price for each symbol
                ));
    }

    public Double getCurrentLTP(String symbol) {
        // This method also exists and serves a similar purpose to getLatestPrice.
        // It's good to keep it if there's a distinction in your actual API.
        // For now, it will use the hardcoded random values.
        switch (symbol.toUpperCase()) {
            case "RELIANCE": return 2900.0 + random.nextDouble() * 50;
            case "TCS": return 3800.0 + random.nextDouble() * 40;
            case "HDFCBANK": return 1500.0 + random.nextDouble() * 20;
            case "INFY": return 1600.0 + random.nextDouble() * 30;
            case "ICICIBANK": return 1100.0 + random.nextDouble() * 15;
            case "NIFTY": return 22500.0 + random.nextDouble() * 100;
            case "BANKNIFTY": return 48000.0 + random.nextDouble() * 200;
            default: return 1000.0 + random.nextDouble() * 100; // Default for unknown symbols
        }
    }

    public List<SecurityDto> searchSecurities(String query) {
        // Simulate a security search. In real app, query a large list of symbols/names.
        List<SecurityDto> allSecurities = Arrays.asList(
                new SecurityDto("RELIANCE", "Reliance Industries", "equity"),
                new SecurityDto("TCS", "Tata Consultancy Services", "equity"),
                new SecurityDto("HDFCBANK", "HDFC Bank", "equity"),
                new SecurityDto("INFY", "Infosys Ltd", "equity"),
                new SecurityDto("ICICIBANK", "ICICI Bank", "equity"),
                new SecurityDto("SBIN", "State Bank of India", "equity"),
                new SecurityDto("NIFTY25APR24C22000", "NIFTY 25 APR 24 CALL 22000", "fno"),
                new SecurityDto("BANKNIFTY25APR24P48000", "BANKNIFTY 25 APR 24 PUT 48000", "fno"),
                new SecurityDto("NIFTYFUT", "NIFTY Futures", "fno"),
                new SecurityDto("NIFTY", "NIFTY Index", "index"),
                new SecurityDto("BANKNIFTY", "BANKNIFTY Index", "index")
        );

        String lowerCaseQuery = query.toLowerCase();
        return allSecurities.stream()
                .filter(s -> s.getSymbol().toLowerCase().contains(lowerCaseQuery) || s.getName().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
    }

    private List<CandlestickDataDto> generateCandlestickData(int days) {
        List<CandlestickDataDto> data = new ArrayList<>();
        LocalDate today = LocalDate.now();
        double price = 18500 + random.nextDouble() * 500;

        for (int i = days; i > 0; i--) {
            LocalDate date = today.minusDays(i);
            double volatility = random.nextDouble() * 2 + 0.1;
            double open = price;
            double high = open + random.nextDouble() * 100 * volatility;
            double low = Math.max(open - random.nextDouble() * 100 * volatility, open * 0.95);
            double close = random.nextDouble() > 0.5
                    ? open + random.nextDouble() * (high - open)
                    : open - random.nextDouble() * (open - low);

            data.add(new CandlestickDataDto(date.format(DateTimeFormatter.ISO_DATE), open, high, low, close));
            price = close;
        }
        return data;
    }
}