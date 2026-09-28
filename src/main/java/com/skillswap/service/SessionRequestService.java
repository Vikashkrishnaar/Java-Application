package com.skillswap.service;

import com.skillswap.dto.SessionRequestCreate;
import com.skillswap.dto.SessionRequestResponse;
import com.skillswap.entity.Member;
import com.skillswap.entity.SessionRequest;
import com.skillswap.entity.SkillOffer;
import com.skillswap.enums.LedgerEntryType;
import com.skillswap.enums.SessionRequestStatus;
import com.skillswap.enums.SkillOfferStatus;
import com.skillswap.exception.BusinessException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.SessionRequestRepository;
import com.skillswap.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessionRequestService {

    private final SessionRequestRepository sessionRequestRepository;
    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final CreditLedgerService creditLedgerService;

    public SessionRequestService(
            SessionRequestRepository sessionRequestRepository,
            MemberRepository memberRepository,
            SkillOfferRepository skillOfferRepository,
            CreditLedgerService creditLedgerService) {

        this.sessionRequestRepository = sessionRequestRepository;
        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.creditLedgerService = creditLedgerService;
    }

    // =========================================================
    // CREATE SESSION REQUEST
    // =========================================================

    public SessionRequestResponse createRequest(SessionRequestCreate request) {

        Member requester = memberRepository
                .findById(request.getRequesterId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Requester not found with ID: "
                                        + request.getRequesterId()));

        SkillOffer skillOffer = skillOfferRepository
                .findById(request.getSkillOfferId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Skill offer not found with ID: "
                                        + request.getSkillOfferId()));

        // Check whether skill offer is active
        if (skillOffer.getStatus() != SkillOfferStatus.ACTIVE) {
            throw new BusinessException("This skill offer is not active");
        }

        // A member cannot request their own skill
        if (skillOffer.getProvider().getId().equals(requester.getId())) {
            throw new BusinessException("A member cannot request their own skill offer");
        }

        // Requested hours validation
        if (request.getRequestedHours() == null || request.getRequestedHours() <= 0) {
            throw new BusinessException("Requested hours must be greater than zero");
        }

        // Requested hours cannot exceed available hours
        if (request.getRequestedHours() > skillOffer.getHoursAvailable()) {
            throw new BusinessException("Requested hours exceed available skill hours");
        }

        // Requester must have enough credits
        if (requester.getTimeCreditBalance() < request.getRequestedHours()) {
            throw new BusinessException("Insufficient time credits");
        }

        // Create session request in PENDING status
        SessionRequest sessionRequest = new SessionRequest();
        sessionRequest.setRequester(requester);
        sessionRequest.setSkillOffer(skillOffer);
        sessionRequest.setRequestedHours(request.getRequestedHours());
        sessionRequest.setStatus(SessionRequestStatus.PENDING);

        SessionRequest savedRequest = sessionRequestRepository.save(sessionRequest);
        return toResponse(savedRequest);
    }

    // =========================================================
    // CONFIRM SESSION REQUEST
    // =========================================================

    public SessionRequestResponse confirmSession(
            Long sessionRequestId,
            Long providerId) {

        if (providerId == null) {
            throw new BusinessException("Provider ID is required");
        }

        SessionRequest sessionRequest = sessionRequestRepository.findById(sessionRequestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session request not found with ID: " + sessionRequestId));

        if (!memberRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }

        // Check whether the person confirming is the actual skill provider
        if (!sessionRequest.getSkillOffer().getProvider().getId().equals(providerId)) {
            throw new BusinessException("Only the skill provider can confirm this request");
        }

        // Check current status
        if (sessionRequest.getStatus() == SessionRequestStatus.COMPLETED) {
            throw new BusinessException("Session is already completed");
        }

        if (sessionRequest.getStatus() != SessionRequestStatus.PENDING) {
            throw new BusinessException("Only pending requests can be confirmed");
        }

        // Change status to CONFIRMED
        sessionRequest.setStatus(SessionRequestStatus.CONFIRMED);

        SessionRequest savedRequest = sessionRequestRepository.save(sessionRequest);
        return toResponse(savedRequest);
    }

    // =========================================================
    // COMPLETE SESSION
    // =========================================================

    @Transactional
    public SessionRequestResponse completeSession(
            Long sessionRequestId,
            Long providerId,
            Integer deliveredHours) {

        if (providerId == null) {
            throw new BusinessException("Provider ID is required");
        }

        if (deliveredHours == null || deliveredHours <= 0) {
            throw new BusinessException("Delivered hours must be greater than zero");
        }

        SessionRequest sessionRequest = sessionRequestRepository.findById(sessionRequestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session request not found with ID: " + sessionRequestId));

        if (!memberRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }

        // Check whether the person completing is actually the skill provider
        if (!sessionRequest.getSkillOffer().getProvider().getId().equals(providerId)) {
            throw new BusinessException("Only the skill provider can complete this session");
        }

        // Double completion protection & state check
        if (sessionRequest.getStatus() == SessionRequestStatus.COMPLETED) {
            throw new BusinessException("Session is already completed");
        }

        if (sessionRequest.getStatus() != SessionRequestStatus.CONFIRMED) {
            throw new BusinessException("Only confirmed sessions can be completed");
        }

        // Delivered hours cannot exceed requested hours
        if (deliveredHours > sessionRequest.getRequestedHours()) {
            throw new BusinessException("Delivered hours cannot exceed requested hours");
        }

        Member requester = sessionRequest.getRequester();
        Member provider = sessionRequest.getSkillOffer().getProvider();

        // Check requester balance again before transferring credits
        if (requester.getTimeCreditBalance() < deliveredHours) {
            throw new BusinessException("Requester does not have enough time credits");
        }

        // Deduct credits from requester
        requester.setTimeCreditBalance(requester.getTimeCreditBalance() - deliveredHours);

        // Add credits to provider
        provider.setTimeCreditBalance(provider.getTimeCreditBalance() + deliveredHours);

        // Update session details
        sessionRequest.setDeliveredHours(deliveredHours);
        sessionRequest.setStatus(SessionRequestStatus.COMPLETED);
        sessionRequest.setCompletedAt(LocalDateTime.now());

        // Save updated balances
        memberRepository.save(requester);
        memberRepository.save(provider);

        // Create debit ledger entry for requester
        creditLedgerService.recordTransaction(
                requester,
                deliveredHours,
                LedgerEntryType.DEBIT,
                "Time credits spent for completed session",
                sessionRequest
        );

        // Create credit ledger entry for provider
        creditLedgerService.recordTransaction(
                provider,
                deliveredHours,
                LedgerEntryType.CREDIT,
                "Time credits earned from completed session",
                sessionRequest
        );

        // Save completed session
        SessionRequest savedRequest = sessionRequestRepository.save(sessionRequest);
        return toResponse(savedRequest);
    }

    // =========================================================
    // GET REQUESTS BY REQUESTER
    // =========================================================

    public List<SessionRequestResponse> getRequestsByRequester(Long requesterId) {

        if (!memberRepository.existsById(requesterId)) {
            throw new ResourceNotFoundException("Requester not found with ID: " + requesterId);
        }

        return sessionRequestRepository
                .findByRequesterId(requesterId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET REQUESTS BY PROVIDER
    // =========================================================

    public List<SessionRequestResponse> getRequestsByProvider(Long providerId) {

        if (!memberRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }

        return sessionRequestRepository
                .findBySkillOfferProviderId(providerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET REQUEST BY ID
    // =========================================================

    public SessionRequestResponse getSessionRequestById(Long id) {

        SessionRequest sessionRequest = sessionRequestRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Session request not found with ID: " + id));

        return toResponse(sessionRequest);
    }

    // =========================================================
    // HELPER MAPPER
    // =========================================================

    private SessionRequestResponse toResponse(SessionRequest request) {

        Member requester = request.getRequester();
        SkillOffer skillOffer = request.getSkillOffer();
        Member provider = skillOffer != null ? skillOffer.getProvider() : null;

        return new SessionRequestResponse(
                request.getId(),
                request.getRequestedHours(),
                request.getDeliveredHours(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getCompletedAt(),
                requester != null ? requester.getId() : null,
                requester != null ? requester.getName() : null,
                skillOffer != null ? skillOffer.getId() : null,
                skillOffer != null ? skillOffer.getSkillName() : null,
                provider != null ? provider.getId() : null,
                provider != null ? provider.getName() : null
        );
    }
}