package com.skillswap;

import com.skillswap.entity.CreditLedger;
import com.skillswap.entity.Member;
import com.skillswap.entity.SessionRequest;
import com.skillswap.entity.SkillOffer;
import com.skillswap.enums.LedgerEntryType;
import com.skillswap.enums.SessionRequestStatus;
import com.skillswap.enums.SkillOfferStatus;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SkillSwapIntegrationTests {

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

    // =========================================================
    // HAPPY PATH END-TO-END TEST
    // =========================================================

    @Test
    @DisplayName("Complete Happy Path: Member creation -> Skill Offer -> Request -> Confirm -> Complete -> Ledger Check")
    void testCompleteHappyPath() throws Exception {
        // STEP 1: Create Provider (initial balance = 0)
        String providerReq = "{\"name\":\"Alice Provider\",\"email\":\"alice@example.com\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(providerReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Alice Provider")))
                .andExpect(jsonPath("$.email", is("alice@example.com")))
                .andExpect(jsonPath("$.timeCreditBalance", is(0)));

        Member provider = memberRepository.findByEmail("alice@example.com").orElseThrow();
        Long providerId = provider.getId();

        // STEP 2: Create Requester
        String requesterReq = "{\"name\":\"Bob Requester\",\"email\":\"bob@example.com\",\"phone\":\"0987654321\"}";

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requesterReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.timeCreditBalance", is(0)));

        Member requester = memberRepository.findByEmail("bob@example.com").orElseThrow();
        Long requesterId = requester.getId();

        // STEP 3: Create Skill Offer for Provider
        String offerReq = String.format("{\"skillName\":\"Java Programming\",\"description\":\"Learn Java 17 and Spring Boot\",\"hoursAvailable\":10,\"providerId\":%d}", providerId);

        mockMvc.perform(post("/api/skill-offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerReq))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.skillName", is("Java Programming")))
                .andExpect(jsonPath("$.hoursAvailable", is(10)))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.providerId", is(providerId.intValue())))
                .andExpect(jsonPath("$.providerName", is("Alice Provider")));

        List<SkillOffer> offers = skillOfferRepository.findByProviderId(providerId);
        assertEquals(1, offers.size());
        Long skillOfferId = offers.get(0).getId();

        // STEP 4: Give Requester enough credits for testing (e.g. 5 credits)
        requester.setTimeCreditBalance(5);
        memberRepository.save(requester);

        // STEP 5: Create Session Request (request 3 hours)
        String sessionCreate = String.format("{\"requesterId\":%d,\"skillOfferId\":%d,\"requestedHours\":3}", requesterId, skillOfferId);

        mockMvc.perform(post("/api/session-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionCreate))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.requestedHours", is(3)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.requesterId", is(requesterId.intValue())))
                .andExpect(jsonPath("$.requesterName", is("Bob Requester")))
                .andExpect(jsonPath("$.skillOfferId", is(skillOfferId.intValue())))
                .andExpect(jsonPath("$.skillName", is("Java Programming")))
                .andExpect(jsonPath("$.providerId", is(providerId.intValue())))
                .andExpect(jsonPath("$.providerName", is("Alice Provider")));

        List<SessionRequest> requests = sessionRequestRepository.findByRequesterId(requesterId);
        assertEquals(1, requests.size());
        Long sessionRequestId = requests.get(0).getId();

        // Verify balances remain unchanged at request creation
        assertEquals(5, memberRepository.findById(requesterId).orElseThrow().getTimeCreditBalance());
        assertEquals(0, memberRepository.findById(providerId).orElseThrow().getTimeCreditBalance());

        // STEP 6: Provider Confirms Session
        String confirmReq = String.format("{\"providerId\":%d}", providerId);

        mockMvc.perform(post("/api/session-requests/" + sessionRequestId + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sessionRequestId.intValue())))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        // Verify balances remain unchanged at confirmation
        assertEquals(5, memberRepository.findById(requesterId).orElseThrow().getTimeCreditBalance());
        assertEquals(0, memberRepository.findById(providerId).orElseThrow().getTimeCreditBalance());

        // STEP 7: Provider Completes Session (deliveredHours = 2)
        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":2}", providerId);

        mockMvc.perform(post("/api/session-requests/" + sessionRequestId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(sessionRequestId.intValue())))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.deliveredHours", is(2)))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

        // Verify balances updated: Requester: 5 - 2 = 3, Provider: 0 + 2 = 2
        assertEquals(3, memberRepository.findById(requesterId).orElseThrow().getTimeCreditBalance());
        assertEquals(2, memberRepository.findById(providerId).orElseThrow().getTimeCreditBalance());

        // Verify exactly 2 ledger entries created
        List<CreditLedger> ledgerEntries = creditLedgerRepository.findBySessionRequestId(sessionRequestId);
        assertEquals(2, ledgerEntries.size());

        // Verify Requester Ledger API
        mockMvc.perform(get("/api/ledger/member/" + requesterId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entryType", is("DEBIT")))
                .andExpect(jsonPath("$[0].amount", is(2)))
                .andExpect(jsonPath("$[0].memberId", is(requesterId.intValue())))
                .andExpect(jsonPath("$[0].sessionRequestId", is(sessionRequestId.intValue())));

        // Verify Provider Ledger API
        mockMvc.perform(get("/api/ledger/member/" + providerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].entryType", is("CREDIT")))
                .andExpect(jsonPath("$[0].amount", is(2)))
                .andExpect(jsonPath("$[0].memberId", is(providerId.intValue())))
                .andExpect(jsonPath("$[0].sessionRequestId", is(sessionRequestId.intValue())));
    }

    // =========================================================
    // ALL 18 EDGE CASES
    // =========================================================

    @Test
    @DisplayName("TEST 1: Invalid member ID returns 404")
    void test1_invalidMemberId() throws Exception {
        mockMvc.perform(get("/api/members/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Member not found")));
    }

    @Test
    @DisplayName("TEST 2: Duplicate email returns 400")
    void test2_duplicateEmail() throws Exception {
        String req1 = "{\"name\":\"User One\",\"email\":\"dup@example.com\",\"phone\":\"1111111111\"}";
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req1))
                .andExpect(status().isCreated());

        String req2 = "{\"name\":\"User Two\",\"email\":\"dup@example.com\",\"phone\":\"2222222222\"}";
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    @DisplayName("TEST 3: Invalid email returns 400")
    void test3_invalidEmail() throws Exception {
        String req = "{\"name\":\"User\",\"email\":\"not-a-valid-email\",\"phone\":\"1234567890\"}";
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("email")));
    }

    @Test
    @DisplayName("TEST 4: Invalid skill provider ID returns 404")
    void test4_invalidSkillProvider() throws Exception {
        String offerReq = "{\"skillName\":\"Python\",\"description\":\"Learn Python\",\"hoursAvailable\":5,\"providerId\":88888}";
        mockMvc.perform(post("/api/skill-offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerReq))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Provider not found")));
    }

    @Test
    @DisplayName("TEST 5: Inactive skill offer cannot be requested (400)")
    void test5_inactiveSkillOffer() throws Exception {
        Member provider = createTestMember("Provider", "prov@test.com", 0);
        Member requester = createTestMember("Requester", "req@test.com", 10);
        SkillOffer offer = createTestOffer("Inactive Skill", 5, SkillOfferStatus.INACTIVE, provider);

        String sessionReq = String.format("{\"requesterId\":%d,\"skillOfferId\":%d,\"requestedHours\":2}", requester.getId(), offer.getId());

        mockMvc.perform(post("/api/session-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("not active")));
    }

    @Test
    @DisplayName("TEST 6: Requester tries to request own skill returns 400")
    void test6_requesterRequestsOwnSkill() throws Exception {
        Member member = createTestMember("Self User", "self@test.com", 10);
        SkillOffer offer = createTestOffer("Cooking", 5, SkillOfferStatus.ACTIVE, member);

        String sessionReq = String.format("{\"requesterId\":%d,\"skillOfferId\":%d,\"requestedHours\":2}", member.getId(), offer.getId());

        mockMvc.perform(post("/api/session-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("cannot request their own skill offer")));
    }

    @Test
    @DisplayName("TEST 7: Requested hours exceed available hours returns 400")
    void test7_requestedHoursExceedAvailable() throws Exception {
        Member provider = createTestMember("Provider", "prov7@test.com", 0);
        Member requester = createTestMember("Requester", "req7@test.com", 20);
        SkillOffer offer = createTestOffer("Design", 3, SkillOfferStatus.ACTIVE, provider);

        String sessionReq = String.format("{\"requesterId\":%d,\"skillOfferId\":%d,\"requestedHours\":5}", requester.getId(), offer.getId());

        mockMvc.perform(post("/api/session-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("exceed available skill hours")));
    }

    @Test
    @DisplayName("TEST 8: Requester has insufficient credits at request creation returns 400")
    void test8_insufficientCreditsAtRequest() throws Exception {
        Member provider = createTestMember("Provider", "prov8@test.com", 0);
        Member requester = createTestMember("Requester", "req8@test.com", 1);
        SkillOffer offer = createTestOffer("Design", 5, SkillOfferStatus.ACTIVE, provider);

        String sessionReq = String.format("{\"requesterId\":%d,\"skillOfferId\":%d,\"requestedHours\":3}", requester.getId(), offer.getId());

        mockMvc.perform(post("/api/session-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sessionReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Insufficient time credits")));
    }

    @Test
    @DisplayName("TEST 9: Non-provider tries to confirm returns 400")
    void test9_nonProviderTriesToConfirm() throws Exception {
        Member provider = createTestMember("Provider", "prov9@test.com", 0);
        Member requester = createTestMember("Requester", "req9@test.com", 5);
        Member stranger = createTestMember("Stranger", "stranger9@test.com", 0);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 2, SessionRequestStatus.PENDING);

        String confirmReq = String.format("{\"providerId\":%d}", stranger.getId());

        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Only the skill provider can confirm")));
    }

    @Test
    @DisplayName("TEST 10: Non-provider tries to complete returns 400")
    void test10_nonProviderTriesToComplete() throws Exception {
        Member provider = createTestMember("Provider", "prov10@test.com", 0);
        Member requester = createTestMember("Requester", "req10@test.com", 5);
        Member stranger = createTestMember("Stranger", "stranger10@test.com", 0);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 2, SessionRequestStatus.CONFIRMED);

        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":2}", stranger.getId());

        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Only the skill provider can complete")));
    }

    @Test
    @DisplayName("TEST 11: Trying to complete PENDING request returns 400")
    void test11_completePendingRequest() throws Exception {
        Member provider = createTestMember("Provider", "prov11@test.com", 0);
        Member requester = createTestMember("Requester", "req11@test.com", 5);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 2, SessionRequestStatus.PENDING);

        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":2}", provider.getId());

        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Only confirmed sessions can be completed")));
    }

    @Test
    @DisplayName("TEST 12: Delivered hours = 0 returns 400")
    void test12_deliveredHoursZero() throws Exception {
        Member provider = createTestMember("Provider", "prov12@test.com", 0);
        Member requester = createTestMember("Requester", "req12@test.com", 5);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 2, SessionRequestStatus.CONFIRMED);

        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":0}", provider.getId());

        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("TEST 13: Delivered hours greater than requested hours returns 400")
    void test13_deliveredHoursExceedRequested() throws Exception {
        Member provider = createTestMember("Provider", "prov13@test.com", 0);
        Member requester = createTestMember("Requester", "req13@test.com", 10);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 2, SessionRequestStatus.CONFIRMED);

        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":4}", provider.getId());

        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Delivered hours cannot exceed requested hours")));
    }

    @Test
    @DisplayName("TEST 14: Requester has insufficient balance at completion time returns 400 and rolls back")
    void test14_insufficientBalanceAtCompletion() throws Exception {
        Member provider = createTestMember("Provider", "prov14@test.com", 0);
        Member requester = createTestMember("Requester", "req14@test.com", 1);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 3, SessionRequestStatus.CONFIRMED);

        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":2}", provider.getId());

        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Requester does not have enough time credits")));

        // Verify balances unchanged
        assertEquals(1, memberRepository.findById(requester.getId()).orElseThrow().getTimeCreditBalance());
        assertEquals(0, memberRepository.findById(provider.getId()).orElseThrow().getTimeCreditBalance());
        // Verify no ledger entries created
        assertEquals(0, creditLedgerRepository.findBySessionRequestId(session.getId()).size());
        // Verify status remains CONFIRMED
        assertEquals(SessionRequestStatus.CONFIRMED, sessionRequestRepository.findById(session.getId()).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("TEST 15: Double completion attempt returns 400")
    void test15_doubleCompletionProtection() throws Exception {
        Member provider = createTestMember("Provider", "prov15@test.com", 0);
        Member requester = createTestMember("Requester", "req15@test.com", 5);
        SkillOffer offer = createTestOffer("Guitar", 5, SkillOfferStatus.ACTIVE, provider);
        SessionRequest session = createTestSession(requester, offer, 2, SessionRequestStatus.CONFIRMED);

        String completeReq = String.format("{\"providerId\":%d,\"deliveredHours\":2}", provider.getId());

        // First completion succeeds
        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        // Second completion attempt must fail
        mockMvc.perform(post("/api/session-requests/" + session.getId() + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("already completed")));
    }

    @Test
    @DisplayName("TEST 16: Missing required fields returns 400")
    void test16_missingRequiredFields() throws Exception {
        String req = "{}";

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(req))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("TEST 17: Negative numeric values return 400")
    void test17_negativeNumericValues() throws Exception {
        String offerReq = "{\"skillName\":\"Painting\",\"description\":\"Oil painting\",\"hoursAvailable\":-5,\"providerId\":1}";

        mockMvc.perform(post("/api/skill-offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerReq))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("TEST 18: Invalid/non-existent resource IDs return 404")
    void test18_invalidResourceIds() throws Exception {
        mockMvc.perform(get("/api/skill-offers/77777"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        mockMvc.perform(get("/api/session-requests/88888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        mockMvc.perform(get("/api/ledger/member/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private Member createTestMember(String name, String email, int balance) {
        Member m = new Member();
        m.setName(name);
        m.setEmail(email);
        m.setPhone("1234567890");
        m.setTimeCreditBalance(balance);
        return memberRepository.save(m);
    }

    private SkillOffer createTestOffer(String name, int hours, SkillOfferStatus status, Member provider) {
        SkillOffer offer = new SkillOffer();
        offer.setSkillName(name);
        offer.setDescription("Test Description for " + name);
        offer.setHoursAvailable(hours);
        offer.setStatus(status);
        offer.setProvider(provider);
        return skillOfferRepository.save(offer);
    }

    private SessionRequest createTestSession(Member requester, SkillOffer offer, int hours, SessionRequestStatus status) {
        SessionRequest session = new SessionRequest();
        session.setRequester(requester);
        session.setSkillOffer(offer);
        session.setRequestedHours(hours);
        session.setStatus(status);
        return sessionRequestRepository.save(session);
    }
}
