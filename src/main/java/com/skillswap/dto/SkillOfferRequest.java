package com.skillswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SkillOfferRequest {

    @NotBlank(message = "Skill name is required")
    private String skillName;

    @NotBlank(message = "Skill description is required")
    private String description;

    @NotNull(message = "Available hours are required")
    @Positive(message = "Available hours must be greater than zero")
    private Integer hoursAvailable;

    @NotNull(message = "Provider ID is required")
    private Long providerId;

    public SkillOfferRequest() {
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getHoursAvailable() {
        return hoursAvailable;
    }

    public void setHoursAvailable(Integer hoursAvailable) {
        this.hoursAvailable = hoursAvailable;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }
}