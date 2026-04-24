package com.vaul.vaul.repositories;

import com.vaul.vaul.entities.AccountClosureRequest;
import com.vaul.vaul.enums.account.ClosureRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountClosureRequestRepository extends JpaRepository<AccountClosureRequest, Long> {
    boolean existsByAccountIdAndStatus(Long accountId, ClosureRequestStatus status);
}
