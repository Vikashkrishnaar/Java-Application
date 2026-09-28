package com.skillswap.service;

import com.skillswap.dto.SessionCompleteRequest;
import com.skillswap.dto.SessionConfirmRequest;
import com.skillswap.dto.SessionRequestCreate;
import com.skillswap.dto.SessionRequestResponse;
import com.skillswap.dto.SkillOfferRequest;
import com.skillswap.dto.SkillOfferResponse;
import com.skillswap.entity.Member;
import com.skillswap.repository.CreditLedgerRepository;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.SessionRequestRepository;
import com.skillswap.repository.SkillOfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DataSeederService {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final SessionRequestRepository sessionRequestRepository;
    private final CreditLedgerRepository creditLedgerRepository;
    private final SkillOfferService skillOfferService;
    private final SessionRequestService sessionRequestService;

    public DataSeederService(
            MemberRepository memberRepository,
            SkillOfferRepository skillOfferRepository,
            SessionRequestRepository sessionRequestRepository,
            CreditLedgerRepository creditLedgerRepository,
            SkillOfferService skillOfferService,
            SessionRequestService sessionRequestService) {

        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.sessionRequestRepository = sessionRequestRepository;
        this.creditLedgerRepository = creditLedgerRepository;
        this.skillOfferService = skillOfferService;
        this.sessionRequestService = sessionRequestService;
    }

    @Transactional
    public Map<String, Object> seedDemoData(boolean force) {

        long existingMembers = memberRepository.count();
        if (existingMembers > 0 && !force) {
            log.info("Database already contains {} members. Seeding skipped. Use force=true to re-seed.", existingMembers);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status", "SKIPPED");
            result.put("message", "Database already contains data. Pass ?force=true to reset and re-seed.");
            result.put("existingMembers", existingMembers);
            return result;
        }

        if (force) {
            log.info("Force flag is true. Cleaning up existing demo data...");
            creditLedgerRepository.deleteAll();
            sessionRequestRepository.deleteAll();
            skillOfferRepository.deleteAll();
            memberRepository.deleteAll();
        }

        log.info("Starting SkillSwap demo data seeding...");

        // =========================================================
        // 1. CREATE MEMBERS (with realistic time credit balances)
        // =========================================================
        Member aarav = createMember("Aarav Sharma", "aarav.sharma@example.com", "9876543210", 10);
        Member diya = createMember("Diya Patel", "diya.patel@example.com", "9876543211", 12);
        Member rohan = createMember("Rohan Verma", "rohan.verma@example.com", "9876543212", 8);
        Member ananya = createMember("Ananya Iyer", "ananya.iyer@example.com", "9876543213", 15);
        Member vikram = createMember("Vikram Singh", "vikram.singh@example.com", "9876543214", 6);
        Member sneha = createMember("Sneha Reddy", "sneha.reddy@example.com", "9876543215", 10);
        Member karthik = createMember("Karthik Nair", "karthik.nair@example.com", "9876543216", 14);
        Member pooja = createMember("Pooja Mehta", "pooja.mehta@example.com", "9876543217", 9);

        // =========================================================
        // 2. CREATE SKILL OFFERS (via SkillOfferService)
        // =========================================================
        SkillOfferResponse offerJava = createSkillOffer(
                "Java & Spring Boot Mastery",
                "Hands-on mentoring in Java 17, Spring Boot REST APIs, Spring Data JPA, and Hibernate.",
                10,
                aarav.getId()
        );

        SkillOfferResponse offerPython = createSkillOffer(
                "Python for Automation & Scripting",
                "Learn Python fundamentals, file automation, web scraping, and API integrations.",
                8,
                diya.getId()
        );

        SkillOfferResponse offerDataScience = createSkillOffer(
                "Data Science & Pandas Fundamentals",
                "Data cleaning, exploratory data analysis, Pandas, NumPy, and data visualization.",
                12,
                rohan.getId()
        );

        SkillOfferResponse offerML = createSkillOffer(
                "Machine Learning Models with Scikit-Learn",
                "Supervised and unsupervised learning, regression, classification, and model evaluation.",
                6,
                ananya.getId()
        );

        SkillOfferResponse offerWebDev = createSkillOffer(
                "Modern Full-Stack Web Development",
                "Building responsive interfaces with HTML5, modern CSS, JavaScript, and REST APIs.",
                10,
                vikram.getId()
        );

        SkillOfferResponse offerSQL = createSkillOffer(
                "SQL Database Design & Optimization",
                "Relational schema design, normalization, complex joins, indexing, and query tuning.",
                8,
                sneha.getId()
        );

        SkillOfferResponse offerUIUX = createSkillOffer(
                "UI/UX Design & Prototyping with Figma",
                "User research, wireframing, high-fidelity UI design, component systems, and prototyping.",
                10,
                karthik.getId()
        );

        SkillOfferResponse offerPublicSpeaking = createSkillOffer(
                "Public Speaking & Interview Preparation",
                "Master confident communication, presentation storytelling, and behavioral interview skills.",
                6,
                pooja.getId()
        );

        SkillOfferResponse offerExcel = createSkillOffer(
                "Advanced Excel & Business Analytics",
                "Master VLOOKUP/XLOOKUP, pivot tables, complex formulas, and interactive dashboard creation.",
                8,
                aarav.getId()
        );

        SkillOfferResponse offerGit = createSkillOffer(
                "Git & GitHub Collaboration Workflows",
                "Branching strategies, pull requests, resolving merge conflicts, and team collaboration.",
                6,
                diya.getId()
        );

        // =========================================================
        // 3. CREATE SESSION REQUESTS (Through real Service Layer)
        // =========================================================

        // --- COMPLETED SESSION 1 ---
        // Diya requests Java from Aarav (Requested: 3 hrs, Completed: 2 hrs)
        SessionRequestResponse session1 = createSessionRequest(diya.getId(), offerJava.getId(), 3);
        sessionRequestService.confirmSession(session1.getId(), aarav.getId());
        sessionRequestService.completeSession(session1.getId(), aarav.getId(), 2);

        // --- COMPLETED SESSION 2 ---
        // Rohan requests Python from Diya (Requested: 4 hrs, Completed: 3 hrs)
        SessionRequestResponse session2 = createSessionRequest(rohan.getId(), offerPython.getId(), 4);
        sessionRequestService.confirmSession(session2.getId(), diya.getId());
        sessionRequestService.completeSession(session2.getId(), diya.getId(), 3);

        // --- COMPLETED SESSION 3 ---
        // Ananya requests Web Dev from Vikram (Requested: 2 hrs, Completed: 2 hrs)
        SessionRequestResponse session3 = createSessionRequest(ananya.getId(), offerWebDev.getId(), 2);
        sessionRequestService.confirmSession(session3.getId(), vikram.getId());
        sessionRequestService.completeSession(session3.getId(), vikram.getId(), 2);

        // --- CONFIRMED SESSION 4 ---
        // Vikram requests SQL from Sneha (Requested: 3 hrs) -> Confirmed, pending completion
        SessionRequestResponse session4 = createSessionRequest(vikram.getId(), offerSQL.getId(), 3);
        sessionRequestService.confirmSession(session4.getId(), sneha.getId());

        // --- CONFIRMED SESSION 5 ---
        // Pooja requests UI/UX from Karthik (Requested: 2 hrs) -> Confirmed, pending completion
        SessionRequestResponse session5 = createSessionRequest(pooja.getId(), offerUIUX.getId(), 2);
        sessionRequestService.confirmSession(session5.getId(), karthik.getId());

        // --- PENDING SESSION 6 ---
        // Sneha requests Public Speaking from Pooja (Requested: 2 hrs) -> Pending
        createSessionRequest(sneha.getId(), offerPublicSpeaking.getId(), 2);

        // --- PENDING SESSION 7 ---
        // Karthik requests Excel from Aarav (Requested: 4 hrs) -> Pending
        createSessionRequest(karthik.getId(), offerExcel.getId(), 4);

        log.info("Demo data seeding completed successfully!");

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("status", "SUCCESS");
        summary.put("message", "Demo data seeded successfully.");
        summary.put("membersCreated", memberRepository.count());
        summary.put("skillOffersCreated", skillOfferRepository.count());
        summary.put("sessionRequestsCreated", sessionRequestRepository.count());
        summary.put("creditLedgerEntriesCreated", creditLedgerRepository.count());

        return summary;
    }

    private Member createMember(String name, String email, String phone, int balance) {
        Member m = new Member();
        m.setName(name);
        m.setEmail(email);
        m.setPhone(phone);
        m.setTimeCreditBalance(balance);
        return memberRepository.save(m);
    }

    private SkillOfferResponse createSkillOffer(String skillName, String description, int hours, Long providerId) {
        SkillOfferRequest req = new SkillOfferRequest();
        req.setSkillName(skillName);
        req.setDescription(description);
        req.setHoursAvailable(hours);
        req.setProviderId(providerId);
        return skillOfferService.createSkillOffer(req);
    }

    private SessionRequestResponse createSessionRequest(Long requesterId, Long offerId, int hours) {
        SessionRequestCreate req = new SessionRequestCreate();
        req.setRequesterId(requesterId);
        req.setSkillOfferId(offerId);
        req.setRequestedHours(hours);
        return sessionRequestService.createRequest(req);
    }
}
