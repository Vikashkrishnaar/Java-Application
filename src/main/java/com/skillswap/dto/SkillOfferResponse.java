package com.skillswap.dto;

import com.skillswap.enums.SkillOfferStatus;

public class SkillOfferResponse {

    private Long id;
    private String skillName;
    private String description;
    private Integer hoursAvailable;
    private SkillOfferStatus status;
    private Long providerId;
    private String providerName;

    public SkillOfferResponse() {
    }

    public SkillOfferResponse(
            Long id,
            String skillName,
            String description,
            Integer hoursAvailable,
            SkillOfferStatus status,
            Long providerId,
            String providerName) {

        this.id = id;
        this.skillName = skillName;
        this.description = description;
        this.hoursAvailable = hoursAvailable;
        this.status = status;
        this.providerId = providerId;
        this.providerName = providerName;
    }

    public Long getId() {
        return id;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getDescription() {
        return description;
    }

    public Integer getHoursAvailable() {
        return hoursAvailable;
    }

    public SkillOfferStatus getStatus() {
        return status;
    }

    public Long getProviderId() {
        return providerId;
    }

    public String getProviderName() {
        return providerName;
    }
}