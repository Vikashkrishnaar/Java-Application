package com.skillswap.dto;

import jakarta.validation.constraints.NotNull;

public class SessionConfirmRequest {

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    public SessionConfirmRequest() {
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }
}