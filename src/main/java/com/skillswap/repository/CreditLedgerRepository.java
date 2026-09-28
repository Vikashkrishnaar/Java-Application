package com.skillswap.repository;

import com.skillswap.entity.CreditLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditLedgerRepository extends JpaRepository<CreditLedger, Long> {

    List<CreditLedger> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<CreditLedger> findBySessionRequestId(Long sessionRequestId);
}