package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.DTO.ReportDto;
import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import com.Personal_Portfolio.Personal_Portfolio.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final PositionRepository positionRepository;

    public ReportDto getUserReport(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<Position> positions = positionRepository.findByUserId(userId);

        double totalPnl = positions.stream()
                .mapToDouble(Position::getPnl)
                .sum();

        double totalMtm = positions.stream()
                .mapToDouble(Position::getMtm)
                .sum();

        return ReportDto.builder()
                .totalPnl(totalPnl)
                .totalMtm(totalMtm)
                .networth(user.getNetworth())
                .marginAvailable(user.getMarginAvailable())
                .build();
    }
}

