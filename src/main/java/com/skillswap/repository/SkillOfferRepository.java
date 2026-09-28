package com.skillswap.repository;

import com.skillswap.entity.SkillOffer;
import com.skillswap.enums.SkillOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillOfferRepository extends JpaRepository<SkillOffer, Long> {

    List<SkillOffer> findByStatus(SkillOfferStatus status);

    List<SkillOffer> findByProviderId(Long providerId);
}