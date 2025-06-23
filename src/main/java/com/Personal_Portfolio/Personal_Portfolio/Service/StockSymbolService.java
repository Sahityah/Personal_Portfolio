package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.StockSymbol;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockSymbolService {

    private List<StockSymbol> allStockSymbols = new ArrayList<>();
    private static final String CSV_FILE_PATH = "stocks.csv"; // Ensure this matches your file name

    @PostConstruct
    public void loadStockSymbols() {
        try (InputStreamReader reader = new InputStreamReader(new ClassPathResource(CSV_FILE_PATH).getInputStream());
             CSVReader csvReader = new CSVReaderBuilder(reader).withSkipLines(1).build()) {

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length >= 2) {
                    allStockSymbols.add(new StockSymbol(
                            nextRecord[0].trim(),
                            nextRecord[1].trim(),
                            nextRecord.length > 2 && !nextRecord[2].trim().isEmpty() ? nextRecord[2].trim() : null,
                            nextRecord.length > 3 && !nextRecord[3].trim().isEmpty() ? nextRecord[3].trim() : null
                    ));
                }
            }
            System.out.println("Loaded " + allStockSymbols.size() + " stock symbols from " + CSV_FILE_PATH);

        } catch (IOException | com.opencsv.exceptions.CsvValidationException e) {
            System.err.println("Error loading stock symbols from CSV: " + e.getMessage());
        }
    }


    public List<StockSymbol> getAllStockSymbols() {
        return allStockSymbols;
    }


    public List<StockSymbol> searchSymbols(String query) {
        if (query == null || query.trim().isEmpty() || query.trim().length() < 1) { // Minimum 1 char for search
            return new ArrayList<>();
        }

        String lowerCaseQuery = query.trim().toLowerCase();

        return allStockSymbols.stream()
                .filter(stock -> stock.getSymbol().toLowerCase().contains(lowerCaseQuery) ||
                        stock.getCompanyName().toLowerCase().contains(lowerCaseQuery))
                .limit(10) // Limit results for performance and UI
                .collect(Collectors.toList());
    }

    public Optional<StockSymbol> getSymbolDetails(String symbol) {
        return allStockSymbols.stream()
                .filter(s -> s.getSymbol().equalsIgnoreCase(symbol))
                .findFirst();
    }
}