package com.skillswap.dto;

import com.skillswap.enums.LedgerEntryType;

import java.time.LocalDateTime;

public class CreditLedgerResponse {

    private Long id;
    private Integer amount;
    private LedgerEntryType entryType;
    private String description;
    private LocalDateTime createdAt;
    private Long memberId;
    private String memberName;
    private Long sessionRequestId;

    public CreditLedgerResponse() {
    }

    public CreditLedgerResponse(
            Long id,
            Integer amount,
            LedgerEntryType entryType,
            String description,
            LocalDateTime createdAt,
            Long memberId,
            String memberName,
            Long sessionRequestId) {

        this.id = id;
        this.amount = amount;
        this.entryType = entryType;
        this.description = description;
        this.createdAt = createdAt;
        this.memberId = memberId;
        this.memberName = memberName;
        this.sessionRequestId = sessionRequestId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(LedgerEntryType entryType) {
        this.entryType = entryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Long getSessionRequestId() {
        return sessionRequestId;
    }

    public void setSessionRequestId(Long sessionRequestId) {
        this.sessionRequestId = sessionRequestId;
    }
}
