package com.vaul.vaul.repositories;

import com.vaul.vaul.entities.KycCase;
import com.vaul.vaul.enums.kyc.KycCaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycCaseRepository extends JpaRepository<KycCase, Long> {
    Optional<KycCase> findFirstByUserIdAndStatusOrderBySubmittedAtDesc(Long userId, KycCaseStatus status);
}
