package com.skillswap.entity;

import com.skillswap.enums.SkillOfferStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
@Table(name = "skill_offers")
public class SkillOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Skill name is required")
    @Column(nullable = false)
    private String skillName;

    @NotBlank(message = "Skill description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Positive(message = "Available hours must be greater than zero")
    @Column(nullable = false)
    private Integer hoursAvailable;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillOfferStatus status = SkillOfferStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Member provider;

    public SkillOffer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public SkillOfferStatus getStatus() {
        return status;
    }

    public void setStatus(SkillOfferStatus status) {
        this.status = status;
    }

    public Member getProvider() {
        return provider;
    }

    public void setProvider(Member provider) {
        this.provider = provider;
    }
}