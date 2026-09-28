/**
 * ============================================================================
 * SKILLSWAP API CLIENT - Direct Spring Boot REST Integration
 * Strictly aligned with Java DTOs, Controllers & GlobalExceptionHandler
 * ============================================================================
 */

const API_BASE = '/api';

class ApiService {
  /**
   * Generic fetch wrapper with JSON handling, timeout, and structured error parsing
   */
  static async request(endpoint, options = {}) {
    const config = {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...(options.headers || {})
      },
      ...options
    };

    try {
      const response = await fetch(`${API_BASE}${endpoint}`, config);
      
      // Handle No-Content
      if (response.status === 204) {
        return null;
      }

      const data = await response.json().catch(() => null);

      if (!response.ok) {
        let errorMsg = `HTTP Error ${response.status}`;
        if (data && data.message) {
          errorMsg = data.message;
        } else if (data && data.errors) {
          errorMsg = Object.values(data.errors).join(', ');
        }
        const error = new Error(errorMsg);
        error.status = response.status;
        error.data = data;
        throw error;
      }

      return data;
    } catch (err) {
      console.error(`[SkillSwap API Error] ${options.method || 'GET'} ${endpoint}:`, err);
      throw err;
    }
  }

  /* Members API */
  static async getMembers() {
    return this.request('/members');
  }

  static async getMemberById(id) {
    return this.request(`/members/${id}`);
  }

  static async createMember({ name, email, phone }) {
    return this.request('/members', {
      method: 'POST',
      body: JSON.stringify({
        name: name.trim(),
        email: email.trim(),
        phone: phone.trim()
      })
    });
  }

  /* Skill Offers API */
  static async getSkillOffers() {
    return this.request('/skill-offers');
  }

  static async getActiveSkillOffers() {
    return this.request('/skill-offers/active');
  }

  static async getSkillOfferById(id) {
    return this.request(`/skill-offers/${id}`);
  }

  /**
   * Creates a Skill Offer adhering to SkillOfferRequest.java:
   * { skillName, description, hoursAvailable, providerId }
   */
  static async createSkillOffer({ skillName, description, hoursAvailable, providerId }) {
    return this.request('/skill-offers', {
      method: 'POST',
      body: JSON.stringify({
        skillName: skillName.trim(),
        description: description.trim(),
        hoursAvailable: Number(hoursAvailable),
        providerId: Number(providerId)
      })
    });
  }

  /* Session Requests API */
  static async getSessionRequestById(id) {
    return this.request(`/session-requests/${id}`);
  }

  static async getRequesterSessions(memberId) {
    return this.request(`/session-requests/requester/${memberId}`);
  }

  static async getProviderSessions(memberId) {
    return this.request(`/session-requests/provider/${memberId}`);
  }

  /**
   * Creates a Session Request adhering to SessionRequestCreate.java:
   * { requesterId, skillOfferId, requestedHours }
   */
  static async createSessionRequest({ requesterId, skillOfferId, requestedHours }) {
    return this.request('/session-requests', {
      method: 'POST',
      body: JSON.stringify({
        requesterId: Number(requesterId),
        skillOfferId: Number(skillOfferId),
        requestedHours: Number(requestedHours)
      })
    });
  }

  /**
   * Confirms Session Request adhering to SessionConfirmRequest.java:
   * { providerId }
   */
  static async confirmSessionRequest(sessionId, providerId) {
    return this.request(`/session-requests/${sessionId}/confirm`, {
      method: 'POST',
      body: JSON.stringify({
        providerId: Number(providerId)
      })
    });
  }

  /**
   * Completes Session Request adhering to SessionCompleteRequest.java:
   * { providerId, deliveredHours }
   */
  static async completeSessionRequest(sessionId, providerId, deliveredHours) {
    return this.request(`/session-requests/${sessionId}/complete`, {
      method: 'POST',
      body: JSON.stringify({
        providerId: Number(providerId),
        deliveredHours: Number(deliveredHours)
      })
    });
  }

  /* Credit Ledger API */
  static async getMemberLedger(memberId) {
    return this.request(`/ledger/member/${memberId}`);
  }

  /* Dev Seed Data Trigger */
  static async triggerDevSeed(force = true) {
    return this.request(`/dev/seed?force=${force}`, {
      method: 'POST'
    });
  }

  /* Dev Reset Clean Slate Trigger */
  static async triggerResetClean() {
    return this.request('/dev/reset-clean', {
      method: 'POST'
    });
  }
}

window.ApiService = ApiService;
