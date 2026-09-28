package com.skillswap.controller;

import com.skillswap.dto.SessionCompleteRequest;
import com.skillswap.dto.SessionConfirmRequest;
import com.skillswap.dto.SessionRequestCreate;
import com.skillswap.dto.SessionRequestResponse;
import com.skillswap.service.SessionRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/session-requests")
public class SessionRequestController {

    private final SessionRequestService sessionRequestService;

    public SessionRequestController(SessionRequestService sessionRequestService) {
        this.sessionRequestService = sessionRequestService;
    }

    @PostMapping
    public ResponseEntity<SessionRequestResponse> createRequest(
            @Valid @RequestBody SessionRequestCreate request) {

        SessionRequestResponse response =
                sessionRequestService.createRequest(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionRequestResponse> getRequestById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                sessionRequestService.getSessionRequestById(id)
        );
    }

    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<SessionRequestResponse>> getRequestsByRequester(
            @PathVariable Long requesterId) {

        return ResponseEntity.ok(
                sessionRequestService.getRequestsByRequester(requesterId)
        );
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<SessionRequestResponse>> getRequestsByProvider(
            @PathVariable Long providerId) {

        return ResponseEntity.ok(
                sessionRequestService.getRequestsByProvider(providerId)
        );
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<SessionRequestResponse> confirmSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionConfirmRequest request) {

        SessionRequestResponse response =
                sessionRequestService.confirmSession(
                        id,
                        request.getProviderId()
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<SessionRequestResponse> completeSession(
            @PathVariable Long id,
            @Valid @RequestBody SessionCompleteRequest request) {

        SessionRequestResponse response =
                sessionRequestService.completeSession(
                        id,
                        request.getProviderId(),
                        request.getDeliveredHours()
                );

        return ResponseEntity.ok(response);
    }
}