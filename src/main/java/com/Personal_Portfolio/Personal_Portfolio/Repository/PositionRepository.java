package com.Personal_Portfolio.Personal_Portfolio.Repository;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import com.Personal_Portfolio.Personal_Portfolio.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
    // All positions for a user
    List<Position> findByUser(User user);

    // Paginated positions for a user
    Page<Position> findByUser(User user, Pageable pageable);

    // Positions for a user by status
    List<Position> findByUserAndStatus(User user, String status);

    // Positions for a user by segment
    List<Position> findByUserAndSegment(User user, String segment);

    // Positions for a user by status and segment
    List<Position> findByUserAndStatusAndSegment(User user, String status, String segment);

    // Find position by id ensuring it belongs to the user
    Optional<Position> findByIdAndUser(Long id, User user);

    // Find positions for a user by symbol
    List<Position> findByUserAndSymbol(User user, String symbol);

    // Count positions for a user by status
    long countByUserAndStatus(User user, String status);
}
