package com.skillswap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SessionRequestCreate {

    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    @NotNull(message = "Skill offer ID is required")
    private Long skillOfferId;

    @NotNull(message = "Requested hours are required")
    @Positive(message = "Requested hours must be greater than zero")
    private Integer requestedHours;

    public SessionRequestCreate() {
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getSkillOfferId() {
        return skillOfferId;
    }

    public void setSkillOfferId(Long skillOfferId) {
        this.skillOfferId = skillOfferId;
    }

    public Integer getRequestedHours() {
        return requestedHours;
    }

    public void setRequestedHours(Integer requestedHours) {
        this.requestedHours = requestedHours;
    }
}