package com.skillswap.service;

import com.skillswap.dto.MemberRequest;
import com.skillswap.dto.MemberResponse;
import com.skillswap.entity.Member;
import com.skillswap.exception.BusinessException;
import com.skillswap.exception.ResourceNotFoundException;
import com.skillswap.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberResponse createMember(MemberRequest request) {

        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email is already registered");
        }

        Member member = new Member();

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());

        // New members start with zero time credits
        member.setTimeCreditBalance(0);

        Member savedMember = memberRepository.save(member);

        return toResponse(savedMember);
    }

    public List<MemberResponse> getAllMembers() {

        return memberRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MemberResponse getMemberById(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() ->
                        new  ResourceNotFoundException("Member not found with ID: " + id));

        return toResponse(member);
    }

    private MemberResponse toResponse(Member member) {

        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.getTimeCreditBalance()
        );
    }
}