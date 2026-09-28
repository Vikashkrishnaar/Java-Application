/**
 * ============================================================================
 * SKILLSWAP STATE CONTAINER - Reactive State Management
 * Connects directly to backend API and utilizes DataMappers for safe rendering
 * ============================================================================
 */

class AppState {
  constructor() {
    this.state = {
      activeView: 'dashboard',
      members: [],
      currentMemberId: null,
      skillOffers: [],
      requesterSessions: [],
      providerSessions: [],
      ledgerEntries: [],
      selectedCategory: 'ALL',
      searchQuery: '',
      loading: false,
      error: null
    };

    this.listeners = new Set();
  }

  subscribe(callback) {
    this.listeners.add(callback);
    return () => this.listeners.delete(callback);
  }

  notify(event, data) {
    this.listeners.forEach(cb => {
      try {
        cb(event, data, this.state);
      } catch (err) {
        console.error('State notification error:', err);
      }
    });
  }

  get currentMember() {
    const member = this.state.members.find(m => m.id === this.state.currentMemberId);
    return member || null;
  }

  async setMember(memberId) {
    this.state.currentMemberId = Number(memberId);
    await this.refreshMemberData();
    this.notify('MEMBER_CHANGED', this.currentMember);
  }

  async init() {
    this.state.loading = true;
    this.notify('LOADING_START');

    try {
      // 1. Fetch all members from real MySQL database
      const rawMembers = await ApiService.getMembers();
      this.state.members = (rawMembers || []).map(DataMappers.mapMember).filter(Boolean);

      // 2. Select initial member if available (default to Diya Patel or first member)
      if (this.state.members.length > 0) {
        const defaultMember = this.state.members.find(m => m.name.toLowerCase().includes('diya')) || this.state.members[0];
        this.state.currentMemberId = defaultMember.id;
      }

      // 3. Fetch active skill offers from real MySQL database
      const rawOffers = await ApiService.getActiveSkillOffers();
      this.state.skillOffers = (rawOffers || []).map(DataMappers.mapSkillOffer).filter(Boolean);

      // 4. Fetch active member specific data
      if (this.state.currentMemberId) {
        await this.refreshMemberData();
      }

    } catch (err) {
      console.error('Initial state load failed:', err);
      this.state.error = err.message;
      this.notify('ERROR', err);
    } finally {
      this.state.loading = false;
      this.notify('INITIAL_LOAD_COMPLETE', this.state);
    }
  }

  async refreshMemberData() {
    if (!this.state.currentMemberId) return;

    try {
      const [rawMember, rawReqSessions, rawProvSessions, rawLedger] = await Promise.all([
        ApiService.getMemberById(this.state.currentMemberId),
        ApiService.getRequesterSessions(this.state.currentMemberId),
        ApiService.getProviderSessions(this.state.currentMemberId),
        ApiService.getMemberLedger(this.state.currentMemberId)
      ]);

      const updatedMember = DataMappers.mapMember(rawMember);

      // Update in members list
      const idx = this.state.members.findIndex(m => m.id === this.state.currentMemberId);
      if (idx !== -1 && updatedMember) {
        this.state.members[idx] = updatedMember;
      }

      this.state.requesterSessions = (rawReqSessions || []).map(DataMappers.mapSession).filter(Boolean);
      this.state.providerSessions = (rawProvSessions || []).map(DataMappers.mapSession).filter(Boolean);
      this.state.ledgerEntries = (rawLedger || []).map(DataMappers.mapLedger).filter(Boolean);

      this.notify('MEMBER_DATA_REFRESHED', {
        member: updatedMember,
        requesterSessions: this.state.requesterSessions,
        providerSessions: this.state.providerSessions,
        ledger: this.state.ledgerEntries
      });
    } catch (err) {
      console.error('Failed to refresh member data:', err);
      this.notify('ERROR', err);
    }
  }

  async refreshAllMembers() {
    try {
      const rawMembers = await ApiService.getMembers();
      this.state.members = (rawMembers || []).map(DataMappers.mapMember).filter(Boolean);
      this.notify('MEMBERS_REFRESHED', this.state.members);
    } catch (err) {
      console.error('Failed to refresh members:', err);
    }
  }

  async refreshSkills() {
    try {
      const rawOffers = await ApiService.getActiveSkillOffers();
      this.state.skillOffers = (rawOffers || []).map(DataMappers.mapSkillOffer).filter(Boolean);
      this.notify('SKILLS_REFRESHED', this.state.skillOffers);
    } catch (err) {
      console.error('Failed to refresh skills:', err);
    }
  }

  setView(viewName) {
    this.state.activeView = viewName;
    this.notify('VIEW_CHANGED', viewName);
  }

  setCategory(category) {
    this.state.selectedCategory = category;
    this.notify('CATEGORY_CHANGED', category);
  }

  setSearchQuery(query) {
    this.state.searchQuery = query;
    this.notify('SEARCH_CHANGED', query);
  }
}

window.store = new AppState();
