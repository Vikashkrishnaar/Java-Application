package com.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SessionCompleteRequest {

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotNull(message = "Delivered hours are required")
    @Positive(message = "Delivered hours must be greater than zero")
    private Integer deliveredHours;

    public SessionCompleteRequest() {
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Integer getDeliveredHours() {
        return deliveredHours;
    }

    public void setDeliveredHours(Integer deliveredHours) {
        this.deliveredHours = deliveredHours;
    }
}