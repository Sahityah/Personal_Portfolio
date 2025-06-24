package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.DTO.ChartDataPointDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.PnLChartDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.PortfolioDataDto;
import com.Personal_Portfolio.Personal_Portfolio.DTO.PositionDto;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PortfolioRepository;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PositionRepository positionRepository;
    private final MarketDataService marketDataService;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository,
                            PositionRepository positionRepository,
                            MarketDataService marketDataService) {
        this.portfolioRepository = portfolioRepository;
        this.positionRepository = positionRepository;
        this.marketDataService = marketDataService;
    }

    /**
     * Retrieves the comprehensive portfolio data for a given user.
     * This includes total investment, current value, PnL, and margin utilization.
     *
     * @param user The authenticated user.
     * @return PortfolioDataDto containing aggregated portfolio information.
     */
    @Transactional(readOnly = true)
    public PortfolioDataDto getPortfolioData(User user) {
        List<Position> positions = positionRepository.findByUser(user);

        double totalInvestment = 0.0;
        double currentValue = 0.0;
        double totalPnL = 0.0;
        double dailyPnL = 0.0;

        Map<String, Double> latestPrices = marketDataService.getLatestPricesForSymbols(
                positions.stream()
                        .map(Position::getSymbol)
                        .distinct()
                        .collect(Collectors.toList())
        );

        for (Position position : positions) {
            Double ltp = latestPrices.getOrDefault(position.getSymbol(), position.getLtp());
            position.setLtp(ltp);

            double positionInvestment = position.getQty() * position.getEntryPrice();
            double positionCurrentValue = position.getQty() * ltp;
            double positionPnL = positionCurrentValue - positionInvestment;

            totalInvestment += positionInvestment;
            currentValue += positionCurrentValue;
            totalPnL += positionPnL;

            dailyPnL += (ltp - position.getEntryPrice()) * position.getQty();
        }

        double totalMargin = 1000000.0;
        double utilizedMargin = currentValue * 0.2;

        return new PortfolioDataDto(
                totalInvestment,
                currentValue,
                totalPnL,
                dailyPnL,
                utilizedMargin,
                totalMargin
        );
    }

    /**
     * Retrieves a list of positions for a given user, with optional filtering by status and segment.
     *
     * @param user The authenticated user.
     * @param status An optional status to filter positions (e.g., "active", "closed").
     * @param segment An optional segment to filter positions (e.g., "equity", "fno").
     * @return A list of PositionDto objects.
     */
    @Transactional(readOnly = true)
    public List<PositionDto> getPositions(User user, Optional<String> status, Optional<String> segment) {
        List<Position> positions;

        if (status.isPresent() && segment.isPresent()) {
            positions = positionRepository.findByUserAndStatusAndSegment(user, status.get(), segment.get());
        } else if (status.isPresent()) {
            positions = positionRepository.findByUserAndStatus(user, status.get());
        } else if (segment.isPresent()) {
            positions = positionRepository.findByUserAndSegment(user, segment.get());
        } else {
            positions = positionRepository.findByUser(user);
        }

        return positions.stream()
                .map(this::convertToPositionDto)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new position to the user's portfolio.
     *
     * @param user The authenticated user.
     * @param positionDto The DTO containing new position details.
     * @return The DTO of the newly added position.
     * @throws IllegalArgumentException if required fields are missing or invalid.
     */
    @Transactional
    public PositionDto addPosition(User user, PositionDto positionDto) {
        if (positionDto.getSymbol() == null || positionDto.getQty() == null || positionDto.getEntryPrice() == null) {
            throw new IllegalArgumentException("Symbol, quantity, and entry price are required.");
        }

        Position position = new Position();
        position.setUser(user);
        position.setSymbol(positionDto.getSymbol().toUpperCase());
        position.setQty(positionDto.getQty());
        position.setEntryPrice(positionDto.getEntryPrice());
        position.setType(positionDto.getType() != null ? positionDto.getType().toUpperCase() : "EQ");
        position.setSegment(determineSegment(position.getType()));
        position.setStatus("active");

        Double ltp = marketDataService.getLatestPrice(position.getSymbol());
        position.setLtp(ltp != null ? ltp : position.getEntryPrice());
        position.setPnl(0.0);
        position.setMtm(position.getQty() * position.getLtp());

        position.setStrike(positionDto.getStrike());

        Position savedPosition = positionRepository.save(position);

        return convertToPositionDto(savedPosition);
    }

    /**
     * Deletes a position from the user's portfolio.
     *
     * @param user The authenticated user.
     * @param positionIdString The ID of the position to delete (as a string, to handle UUIDs or Long).
     * @throws IllegalArgumentException if the position is not found or does not belong to the user.
     */
    @Transactional
    public void deletePosition(User user, String positionIdString) {
        Long positionId;
        try {
            positionId = Long.parseLong(positionIdString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid position ID format: " + positionIdString);
        }

        Optional<Position> positionOptional = positionRepository.findByIdAndUser(positionId, user);
        if (positionOptional.isPresent()) {
            positionRepository.delete(positionOptional.get());
        } else {
            throw new IllegalArgumentException("Position with ID " + positionId + " not found or does not belong to the user.");
        }
    }

    /**
     * Retrieves Profit & Loss chart data for a user's portfolio over a specified period.
     *
     * @param user   The authenticated user.
     * @return PnLChartDataDto containing data points for the chart.
     */
    @Transactional(readOnly = true)
    public PnLChartDataDto getPnLChartData(User user) {
        List<Position> positions = positionRepository.findByUser(user);

        return new PnLChartDataDto(
                generateChartData(positions, "daily"),
                generateChartData(positions, "weekly"),
                generateChartData(positions, "monthly"),
                generateChartData(positions, "yearly")
        );
    }


    private List<ChartDataPointDto> generateChartData(List<Position> positions, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period.toLowerCase()) {
            case "weekly":
                startDate = endDate.minusWeeks(1);
                break;
            case "monthly":
                startDate = endDate.minusMonths(1);
                break;
            case "yearly":
                startDate = endDate.minusYears(1);
                break;
            case "daily":
            default:
                startDate = endDate.minusDays(7);
                break;
        }

        List<ChartDataPointDto> dataPoints = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> {
                    double totalInvestment = positions.stream()
                            .mapToDouble(p -> p.getQty() * p.getEntryPrice())
                            .sum();

                    double simulatedPnL = (Math.random() - 0.5) * totalInvestment * 0.05;

                    return new ChartDataPointDto(date.atStartOfDay(), simulatedPnL);
                })
                .sorted(Comparator.comparing(ChartDataPointDto::getTimestamp))
                .collect(Collectors.toList());

        double cumulativePnL = 0.0;
        for (ChartDataPointDto dp : dataPoints) {
            cumulativePnL += dp.getValue();
            dp.setValue(cumulativePnL);
        }

        return dataPoints;
    }
    /**
     * Helper method to determine segment based on instrument type.
     */
    private String determineSegment(String instrumentType) {
        if (instrumentType == null) {
            return "equity";
        }
        return switch (instrumentType.toUpperCase()) {
            case "EQ" -> "equity";
            case "CALL", "PUT", "FUT" -> "fno";
            default -> "other";
        };
    }

    /**
     * Converts a Position entity to a PositionDto.
     */
    private PositionDto convertToPositionDto(Position position) {
        PositionDto dto = new PositionDto();
        dto.setId(position.getId() != null ? position.getId(): null);
        dto.setSymbol(position.getSymbol());
        dto.setQty(position.getQty());
        dto.setEntryPrice(position.getEntryPrice());
        dto.setLtp(position.getLtp());
        dto.setPnl(position.getPnl());
        dto.setMtm(position.getMtm());
        dto.setType(position.getType());
        dto.setSegment(position.getSegment());
        dto.setStatus(position.getStatus());
        dto.setStrike(position.getStrike());


        return dto;
    }
}