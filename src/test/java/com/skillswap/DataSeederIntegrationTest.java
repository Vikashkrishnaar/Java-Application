package com.skillswap;

import com.skillswap.entity.CreditLedger;
import com.skillswap.entity.SessionRequest;
import com.skillswap.enums.LedgerEntryType;
import com.skillswap.enums.SessionRequestStatus;
import com.skillswap.repository.CreditLedgerRepository;
import com.skillswap.repository.MemberRepository;
import com.skillswap.repository.SessionRequestRepository;
import com.skillswap.repository.SkillOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DataSeederIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SkillOfferRepository skillOfferRepository;

    @Autowired
    private SessionRequestRepository sessionRequestRepository;

    @Autowired
    private CreditLedgerRepository creditLedgerRepository;

    @BeforeEach
    void setUp() {
        creditLedgerRepository.deleteAll();
        sessionRequestRepository.deleteAll();
        skillOfferRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("Verify Data Seeding: Exactly 8 members, 10 skill offers, 7 session requests, 6 ledger entries")
    void testDataSeedingMechanism() throws Exception {
        // Run seed with force=true
        mockMvc.perform(post("/api/dev/seed")
                        .param("force", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.membersCreated", is(8)))
                .andExpect(jsonPath("$.skillOffersCreated", is(10)))
                .andExpect(jsonPath("$.sessionRequestsCreated", is(7)))
                .andExpect(jsonPath("$.creditLedgerEntriesCreated", is(6)));

        // Verify Database Counts
        assertEquals(8, memberRepository.count());
        assertEquals(10, skillOfferRepository.count());
        assertEquals(7, sessionRequestRepository.count());
        assertEquals(6, creditLedgerRepository.count());

        // Verify Session Request States
        List<SessionRequest> completedRequests = sessionRequestRepository.findByStatus(SessionRequestStatus.COMPLETED);
        List<SessionRequest> confirmedRequests = sessionRequestRepository.findByStatus(SessionRequestStatus.CONFIRMED);
        List<SessionRequest> pendingRequests = sessionRequestRepository.findByStatus(SessionRequestStatus.PENDING);

        assertEquals(3, completedRequests.size());
        assertEquals(2, confirmedRequests.size());
        assertEquals(2, pendingRequests.size());

        // Verify Credit Ledger Records (3 Debits, 3 Credits)
        List<CreditLedger> allLedgers = creditLedgerRepository.findAll();
        long debitCount = allLedgers.stream().filter(l -> l.getEntryType() == LedgerEntryType.DEBIT).count();
        long creditCount = allLedgers.stream().filter(l -> l.getEntryType() == LedgerEntryType.CREDIT).count();
        assertEquals(3, debitCount);
        assertEquals(3, creditCount);

        // Verify REST API endpoints return seeded data
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(8)));

        mockMvc.perform(get("/api/skill-offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(10)));

        mockMvc.perform(get("/api/skill-offers/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(10)));

        // Verify Idempotency (calling seed without force skips)
        mockMvc.perform(post("/api/dev/seed")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SKIPPED")));

        assertEquals(8, memberRepository.count());
    }
}
