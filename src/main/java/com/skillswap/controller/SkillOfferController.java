package com.skillswap.controller;

import com.skillswap.dto.SkillOfferRequest;
import com.skillswap.dto.SkillOfferResponse;
import com.skillswap.service.SkillOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill-offers")
public class SkillOfferController {

    private final SkillOfferService skillOfferService;

    public SkillOfferController(SkillOfferService skillOfferService) {
        this.skillOfferService = skillOfferService;
    }

    @PostMapping
    public ResponseEntity<SkillOfferResponse> createSkillOffer(
            @Valid @RequestBody SkillOfferRequest request) {

        SkillOfferResponse response =
                skillOfferService.createSkillOffer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<SkillOfferResponse>> getAllSkillOffers() {

        return ResponseEntity.ok(
                skillOfferService.getAllSkillOffers()
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<SkillOfferResponse>> getActiveSkillOffers() {

        return ResponseEntity.ok(
                skillOfferService.getActiveSkillOffers()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillOfferResponse> getSkillOfferById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                skillOfferService.getSkillOfferById(id)
        );
    }
}