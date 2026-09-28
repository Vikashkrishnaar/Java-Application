package com.skillswap.service;

import com.skillswap.dto.SkillOfferRequest;
import com.skillswap.dto.SkillOfferResponse;
import com.skillswap.entity.Member;
import com.skillswap.entity.SkillOffer;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.enums.SkillOfferStatus;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillOfferService {

    private final SkillOfferRepository skillOfferRepository;
    private final MemberRepository memberRepository;

    public SkillOfferService(
            SkillOfferRepository skillOfferRepository,
            MemberRepository memberRepository) {

        this.skillOfferRepository = skillOfferRepository;
        this.memberRepository = memberRepository;
    }

    public SkillOfferResponse createSkillOffer(SkillOfferRequest request) {

        Member provider = memberRepository.findById(request.getProviderId())
                .orElseThrow(() ->
                        new  ResourceNotFoundException(
                                "Provider not found with ID: " + request.getProviderId()));

        SkillOffer skillOffer = new SkillOffer();

        skillOffer.setSkillName(request.getSkillName());
        skillOffer.setDescription(request.getDescription());
        skillOffer.setHoursAvailable(request.getHoursAvailable());
        skillOffer.setProvider(provider);
        skillOffer.setStatus(SkillOfferStatus.ACTIVE);

        SkillOffer savedOffer = skillOfferRepository.save(skillOffer);

        return toResponse(savedOffer);
    }

    public List<SkillOfferResponse> getAllSkillOffers() {

        return skillOfferRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<SkillOfferResponse> getActiveSkillOffers() {

        return skillOfferRepository.findByStatus(SkillOfferStatus.ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SkillOfferResponse getSkillOfferById(Long id) {

        SkillOffer skillOffer = skillOfferRepository.findById(id)
                .orElseThrow(() ->
                        new  ResourceNotFoundException(
                                "Skill offer not found with ID: " + id));

        return toResponse(skillOffer);
    }

    private SkillOfferResponse toResponse(SkillOffer skillOffer) {

        Member provider = skillOffer.getProvider();

        return new SkillOfferResponse(
                skillOffer.getId(),
                skillOffer.getSkillName(),
                skillOffer.getDescription(),
                skillOffer.getHoursAvailable(),
                skillOffer.getStatus(),
                provider.getId(),
                provider.getName()
        );
    }
}