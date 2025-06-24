package com.Personal_Portfolio.Personal_Portfolio.Service;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import com.Personal_Portfolio.Personal_Portfolio.Repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PositionService {

    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> getPositionsByUser(User user) {
        return positionRepository.findByUser(user);
    }

    public Page<Position> getPositionsByUser(User user, Pageable pageable) {
        return positionRepository.findByUser(user, pageable);
    }

    public Position addPosition(Position position, User user) {
        position.setUser(user);

        if (position.getLtp() == null) {
            position.setLtp(position.getEntryPrice());
        }
        if (position.getPnl() == null) {
            position.setPnl(0.0);
        }
        if (position.getMtm() == null) {
            position.setMtm(position.getQty() * position.getEntryPrice());
        }
        if (position.getStatus() == null || position.getStatus().isEmpty()) {
            position.setStatus("active");
        }

        // Business validations
        if ("EQ".equalsIgnoreCase(position.getType()) && position.getStrike() != null) {
            throw new IllegalArgumentException("Equity position should not have a strike price.");
        }

        if (("CALL".equalsIgnoreCase(position.getType()) ||
                "PUT".equalsIgnoreCase(position.getType()) ||
                "FUT".equalsIgnoreCase(position.getType())) &&
                (position.getStrike() == null || position.getStrike().isEmpty())) {
            throw new IllegalArgumentException(position.getType() + " position must have a strike price.");
        }

        return positionRepository.save(position);
    }

    public Optional<Position> updatePosition(Long positionId, Position updatedPosition, User user) {
        return positionRepository.findByIdAndUser(positionId, user)
                .map(existingPosition -> {
                    existingPosition.setSymbol(updatedPosition.getSymbol());
                    existingPosition.setStrike(updatedPosition.getStrike());
                    existingPosition.setType(updatedPosition.getType());
                    existingPosition.setSegment(updatedPosition.getSegment());
                    existingPosition.setQty(updatedPosition.getQty());
                    existingPosition.setEntryPrice(updatedPosition.getEntryPrice());
                    existingPosition.setLtp(updatedPosition.getLtp());
                    existingPosition.setPnl(updatedPosition.getPnl());
                    existingPosition.setMtm(updatedPosition.getMtm());
                    existingPosition.setStatus(updatedPosition.getStatus());
                    return positionRepository.save(existingPosition);
                });
    }

    public boolean deletePosition(Long positionId, User user) {
        return positionRepository.findByIdAndUser(positionId, user)
                .map(position -> {
                    positionRepository.delete(position);
                    return true;
                })
                .orElse(false);
    }

    public String determineSegment(String instrumentType) {
        return switch (instrumentType.toUpperCase()) {
            case "EQ" -> "equity";
            case "CALL", "PUT", "FUT" -> "fno";
            default -> "other";
        };
    }
}
