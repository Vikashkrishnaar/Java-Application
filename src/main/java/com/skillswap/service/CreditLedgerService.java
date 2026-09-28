package com.skillswap.service;

import com.skillswap.dto.CreditLedgerResponse;
import com.skillswap.entity.CreditLedger;
import com.skillswap.entity.Member;
import com.skillswap.entity.SessionRequest;
import com.skillswap.enums.LedgerEntryType;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.CreditLedgerRepository;
import com.skillswap.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditLedgerService {

    private final CreditLedgerRepository creditLedgerRepository;
    private final MemberRepository memberRepository;

    public CreditLedgerService(
            CreditLedgerRepository creditLedgerRepository,
            MemberRepository memberRepository) {
        this.creditLedgerRepository = creditLedgerRepository;
        this.memberRepository = memberRepository;
    }

    public void recordTransaction(
            Member member,
            Integer amount,
            LedgerEntryType entryType,
            String description,
            SessionRequest sessionRequest) {

        CreditLedger ledger = new CreditLedger();

        ledger.setMember(member);
        ledger.setAmount(amount);
        ledger.setEntryType(entryType);
        ledger.setDescription(description);
        ledger.setSessionRequest(sessionRequest);

        creditLedgerRepository.save(ledger);
    }

    public List<CreditLedgerResponse> getMemberLedger(Long memberId) {

        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with ID: " + memberId);
        }

        return creditLedgerRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CreditLedgerResponse toResponse(CreditLedger ledger) {
        return new CreditLedgerResponse(
                ledger.getId(),
                ledger.getAmount(),
                ledger.getEntryType(),
                ledger.getDescription(),
                ledger.getCreatedAt(),
                ledger.getMember() != null ? ledger.getMember().getId() : null,
                ledger.getMember() != null ? ledger.getMember().getName() : null,
                ledger.getSessionRequest() != null ? ledger.getSessionRequest().getId() : null
        );
    }
}