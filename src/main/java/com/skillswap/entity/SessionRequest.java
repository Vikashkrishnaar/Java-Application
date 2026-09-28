package com.skillswap.entity;

import com.skillswap.enums.SessionRequestStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_requests")
public class SessionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "Requested hours must be greater than zero")
    @Column(nullable = false)
    private Integer requestedHours;

    @Positive(message = "Delivered hours must be greater than zero")
    @Column
    private Integer deliveredHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionRequestStatus status = SessionRequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_offer_id", nullable = false)
    private SkillOffer skillOffer;

    public SessionRequest() {
        this.requestedAt = LocalDateTime.now();
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

    public Member getRequester() {
        return requester;
    }

    public void setRequester(Member requester) {
        this.requester = requester;
    }

    public SkillOffer getSkillOffer() {
        return skillOffer;
    }

    public void setSkillOffer(SkillOffer skillOffer) {
        this.skillOffer = skillOffer;
    }
}