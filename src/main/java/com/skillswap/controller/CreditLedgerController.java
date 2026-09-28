package com.skillswap.controller;

import com.skillswap.dto.CreditLedgerResponse;
import com.skillswap.service.CreditLedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class CreditLedgerController {

    private final CreditLedgerService creditLedgerService;

    public CreditLedgerController(CreditLedgerService creditLedgerService) {
        this.creditLedgerService = creditLedgerService;
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CreditLedgerResponse>> getMemberLedger(
            @PathVariable Long memberId) {

        return ResponseEntity.ok(
                creditLedgerService.getMemberLedger(memberId)
        );
    }
}