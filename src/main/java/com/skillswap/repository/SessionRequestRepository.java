package com.skillswap.repository;

import com.skillswap.entity.SessionRequest;
import com.skillswap.enums.SessionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {

    List<SessionRequest> findByRequesterId(Long requesterId);

    List<SessionRequest> findByStatus(SessionRequestStatus status);

    List<SessionRequest> findBySkillOfferProviderId(Long providerId);
}