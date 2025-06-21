package com.Personal_Portfolio.Personal_Portfolio.Repository;

import com.Personal_Portfolio.Personal_Portfolio.Entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByUserId(Long userId);
}

