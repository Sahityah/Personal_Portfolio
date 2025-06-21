package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionService {

    private final PositionRepository positionRepository;

    public Position addPosition(Position position) {
        // Calculate P&L and MTM while adding
        position.setPnl((position.getLtp() - position.getEntryPrice()) * position.getQuantity());
        position.setMtm(position.getPnl());  // For now same as P&L
        return positionRepository.save(position);
    }

    public List<Position> getPositionsByUser(Long userId) {
        return positionRepository.findByUserId(userId);
    }

    public Optional<Position> getPosition(Long id) {
        return positionRepository.findById(id);
    }

    public void deletePosition(Long id) {
        positionRepository.deleteById(id);
    }
}

