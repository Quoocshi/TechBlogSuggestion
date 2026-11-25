package com.example.techblogsuggestion.domain.repository;

import com.example.techblogsuggestion.domain.model.DailyDigest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DailyDigestRepository extends JpaRepository<DailyDigest, Long> {
    boolean existsByDigestDate(LocalDate digestDate);
}