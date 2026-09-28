package com.skillswap.dto;

import com.skillswap.enums.SessionRequestStatus;

import java.time.LocalDateTime;

public class SessionRequestResponse {

    private Long id;
    private Integer requestedHours;
    private Integer deliveredHours;
    private SessionRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private Long requesterId;
    private String requesterName;
    private Long skillOfferId;
    private String skillName;
    private Long providerId;
    private String providerName;

    public SessionRequestResponse() {
    }

    public SessionRequestResponse(
            Long id,
            Integer requestedHours,
            Integer deliveredHours,
            SessionRequestStatus status,
            LocalDateTime requestedAt,
            LocalDateTime completedAt,
            Long requesterId,
            String requesterName,
            Long skillOfferId,
            String skillName,
            Long providerId,
            String providerName) {

        this.id = id;
        this.requestedHours = requestedHours;
        this.deliveredHours = deliveredHours;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.skillOfferId = skillOfferId;
        this.skillName = skillName;
        this.providerId = providerId;
        this.providerName = providerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRequestedHours() {
        return requestedHours;
    }

    public void setRequestedHours(Integer requestedHours) {
        this.requestedHours = requestedHours;
    }

    public Integer getDeliveredHours() {
        return deliveredHours;
    }

    public void setDeliveredHours(Integer deliveredHours) {
        this.deliveredHours = deliveredHours;
    }

    public SessionRequestStatus getStatus() {
        return status;
    }

    public void setStatus(SessionRequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public Long getSkillOfferId() {
        return skillOfferId;
    }

    public void setSkillOfferId(Long skillOfferId) {
        this.skillOfferId = skillOfferId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
}
