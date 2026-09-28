/**
 * ============================================================================
 * SKILLSWAP MAIN APP CONTROLLER
 * Full Lifecycle Routing, Dynamic View Renderers, Event Handlers & Modals
 * Real User Action -> Real HTTP -> Spring Boot -> MySQL -> UI Update
 * Strict Data Ownership: Personal Views Only Show Current Member's Data
 * ============================================================================
 */

let activeRequestSubTab = 'incoming'; // 'incoming' | 'learning'

document.addEventListener('DOMContentLoaded', async () => {
  // Initialize State from Real Backend Data
  await store.init();
  setupNavigation();
  setupMemberSwitcher();
  setupModals();
  setupEventListeners();
  renderCurrentView();

  // Subscribe to state changes
  store.subscribe((event, data) => {
    updatePersistentHeader();
    renderCurrentView();
  });
});

/**
 * Navigation Setup
 */
function setupNavigation() {
  const navLinks = document.querySelectorAll('.nav-link, .mobile-nav-item');
  navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const targetView = link.getAttribute('data-view');
      if (targetView) {
        navigateTo(targetView);
      }
    });
  });
}

function navigateTo(viewName) {
  store.setView(viewName);

  // Update active classes on nav
  document.querySelectorAll('.nav-link, .mobile-nav-item').forEach(el => {
    if (el.getAttribute('data-view') === viewName) {
      el.classList.add('active');
    } else {
      el.classList.remove('active');
    }
  });

  // Toggle view visibility
  document.querySelectorAll('.view-section').forEach(sec => {
    if (sec.id === `${viewName}-view`) {
      sec.classList.add('active-view');
    } else {
      sec.classList.remove('active-view');
    }
  });

  // Update breadcrumb in topbar
  const crumb = document.getElementById('topbar-crumb');
  if (crumb) {
    const titles = {
      'dashboard': 'My Time Exchange',
      'discover': 'Discover Skills',
      'my-skills': 'My Skills Offered',
      'requests': 'Requests Queue',
      'sessions': 'Sessions Timeline',
      'ledger': 'Time Ledger Journal',
      'profile': 'Member Profile'
    };
    crumb.textContent = titles[viewName] || 'Exchange';
  }

  renderCurrentView();
}

/**
 * Member Switcher Setup (Dropdown & Active Member Profile)
 */
function setupMemberSwitcher() {
  const toggleBtn = document.getElementById('member-switcher-toggle');
  const menu = document.getElementById('member-switcher-menu');

  if (toggleBtn && menu) {
    toggleBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      menu.classList.toggle('active');
    });

    document.addEventListener('click', (e) => {
      if (!menu.contains(e.target) && e.target !== toggleBtn) {
        menu.classList.remove('active');
      }
    });
  }
}

function updatePersistentHeader() {
  const member = store.currentMember;
  if (!member) return;

  // Topbar Switcher Button
  const nameEl = document.getElementById('active-member-name');
  const balanceEl = document.getElementById('active-member-balance');
  const avatarEl = document.getElementById('active-member-avatar');
  const sidebarBalanceEl = document.getElementById('sidebar-time-balance');

  if (nameEl) nameEl.textContent = member.name;
  if (balanceEl) balanceEl.textContent = `⏱ ${(Number(member.timeCreditBalance) || 0).toFixed(1)}h`;
  if (avatarEl) {
    avatarEl.textContent = member.initials || 'SS';
  }
  if (sidebarBalanceEl) {
    sidebarBalanceEl.textContent = (Number(member.timeCreditBalance) || 0).toFixed(1);
  }

  // Populate switcher options in dropdown
  const menu = document.getElementById('member-switcher-menu');
  if (menu) {
    menu.innerHTML = `
      <div class="menu-header-label">Switch Active Participant (Real MySQL)</div>
      ${store.state.members.map(m => `
        <button class="member-option ${m.id === member.id ? 'selected' : ''}" data-member-id="${m.id}">
          <div class="member-option-info">
            <div class="member-avatar-sm">${m.initials || 'SS'}</div>
            <div class="member-meta-sm">
              <span class="member-name-sm">${m.name}</span>
              <span class="member-balance-sm time-num">⏱ ${(Number(m.timeCreditBalance) || 0).toFixed(1)} Hours</span>
            </div>
          </div>
          ${m.id === member.id ? '<span style="color: var(--accent-amber); font-size: 0.8rem;">●</span>' : ''}
        </button>
      `).join('')}
      <div style="border-top: 1px solid var(--border-subtle); margin-top: 6px; padding-top: 6px;">
        <button id="add-member-menu-btn" class="btn btn-secondary btn-sm" style="width: 100%;">+ Register New Member</button>
      </div>
    `;

    menu.querySelectorAll('.member-option').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-member-id');
        menu.classList.remove('active');
        await store.setMember(id);
        UIComponents.showToast(`Active account switched to ${store.currentMember.name}`, 'info');
      });
    });

    const addMemBtn = document.getElementById('add-member-menu-btn');
    if (addMemBtn) {
      addMemBtn.addEventListener('click', () => {
        menu.classList.remove('active');
        openModal('create-member-modal');
      });
    }
  }

  // Update badge counts for incoming pending requests
  const pendingIncomingCount = store.state.providerSessions.filter(s => s.status === 'PENDING').length;
  const requestsBadge = document.getElementById('requests-badge');
  if (requestsBadge) {
    requestsBadge.textContent = pendingIncomingCount;
    requestsBadge.style.display = pendingIncomingCount > 0 ? 'inline-block' : 'none';
  }
}

/**
 * Main Dynamic View Render Dispatcher
 */
function renderCurrentView() {
  const view = store.state.activeView;
  switch (view) {
    case 'dashboard':
      renderDashboardView();
      break;
    case 'discover':
      renderDiscoverView();
      break;
    case 'my-skills':
      renderMySkillsView();
      break;
    case 'requests':
      renderRequestsView();
      break;
    case 'sessions':
      renderSessionsView();
      break;
    case 'ledger':
      renderLedgerView();
      break;
    case 'profile':
      renderProfileView();
      break;
  }
}

/**
 * 1. DASHBOARD RENDERER ("My Time Exchange")
 * Tells the complete SkillSwap story: Balance, Teaching vs Learning, Flow, Actions
 */
function renderDashboardView() {
  const member = store.currentMember;
  if (!member) return;

  const container = document.getElementById('dashboard-view');
  if (!container) return;

  // Real calculations from active member's ledger
  let earned = 0;
  let spent = 0;
  store.state.ledgerEntries.forEach(e => {
    if (e.entryType === 'CREDIT') earned += Number(e.amount || 0);
    if (e.entryType === 'DEBIT') spent += Number(e.amount || 0);
  });
  const net = earned - spent;

  // Real Data: What I Teach (My active skill offers)
  const myOffers = store.state.skillOffers.filter(s => s.providerId === member.id);

  // Real Data: What I Learn (My learning sessions requested from others)
  const myLearning = store.state.requesterSessions;

  // Real Data: Incoming teaching requests
  const incomingPending = store.state.providerSessions.filter(s => s.status === 'PENDING');
  const incomingConfirmed = store.state.providerSessions.filter(s => s.status === 'CONFIRMED');

  // Real Data for Exchange Flow Banner
  const latestSession = [...store.state.providerSessions, ...store.state.requesterSessions][0];
  let flowTeacher = member.name;
  let flowSkill = myOffers.length > 0 ? myOffers[0].skillName : 'Skill Exchange';
  let flowLearner = 'Community Member';
  let flowHours = 2;
  let flowCompleted = false;

  if (latestSession) {
    flowTeacher = latestSession.providerName;
    flowSkill = latestSession.skillName;
    flowLearner = latestSession.requesterName;
    flowHours = latestSession.deliveredHours || latestSession.requestedHours;
    flowCompleted = latestSession.status === 'COMPLETED';
  }

  container.innerHTML = `
    <!-- Editorial Hero Welcome -->
    <div class="dashboard-hero-welcome">
      <div class="dash-welcome-text">
        <span class="hero-tag">Community Time Exchange</span>
        <h2>Good Day, ${member.name}</h2>
        <p>Exchange skills peer-to-peer. Teach to earn hours, learn to spend hours.</p>
        <div style="margin-top: 16px; display: flex; gap: 12px; flex-wrap: wrap;">
          <button class="btn btn-primary btn-sm" onclick="navigateTo('discover')">Explore Skills</button>
          <button class="btn btn-secondary btn-sm" onclick="openOfferModal()">+ Offer a Skill</button>
        </div>
      </div>

      <div class="dash-balance-highlight">
        <span class="dash-balance-sub">Available Time Credits</span>
        <span class="dash-balance-num time-num">${(Number(member.timeCreditBalance) || 0).toFixed(1)}</span>
        <span class="dash-balance-sub">Hours Available to Spend</span>
      </div>
    </div>

    <!-- Dual Role Panels: WHAT I TEACH vs WHAT I LEARN -->
    <div class="teach-learn-grid" style="margin-top: 24px;">
      <!-- Panel 1: I TEACH (Skills I Offer) -->
      <div class="teach-learn-panel">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="panel-header-badge teach">📚 I Teach (${myOffers.length} Active Offers)</span>
          <button class="btn btn-secondary btn-sm" style="padding: 3px 8px; font-size: 0.75rem;" onclick="openOfferModal()">+ Add</button>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 8px;">
          Skills you offer to the community to earn time credits:
        </p>

        ${myOffers.length === 0 ? `
          <div style="padding: 16px; background: var(--bg-surface-elevated); border-radius: var(--radius-sm); border: 1px dashed var(--border-muted); text-align: center;">
            <p style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 8px;">You haven't offered any skills yet.</p>
            <button class="btn btn-primary btn-sm" onclick="openOfferModal()">Offer Your First Skill</button>
          </div>
        ` : `
          <div class="mini-exchange-list">
            ${myOffers.map(o => `
              <div class="mini-exchange-item">
                <div>
                  <div class="mini-item-title">${o.skillName}</div>
                  <div class="mini-item-sub">Capacity: ${o.hoursAvailable} Hours Available</div>
                </div>
                <span class="status-badge status-completed"><span class="status-dot"></span> Active</span>
              </div>
            `).join('')}
          </div>
        `}
      </div>

      <!-- Panel 2: I LEARN (Sessions I Requested) -->
      <div class="teach-learn-panel">
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span class="panel-header-badge learn">🎯 I Learn (${myLearning.length} Sessions)</span>
          <button class="btn btn-secondary btn-sm" style="padding: 3px 8px; font-size: 0.75rem;" onclick="navigateTo('discover')">Explore</button>
        </div>
        <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 8px;">
          Learning sessions you have requested from other community members:
        </p>

        ${myLearning.length === 0 ? `
          <div style="padding: 16px; background: var(--bg-surface-elevated); border-radius: var(--radius-sm); border: 1px dashed var(--border-muted); text-align: center;">
            <p style="font-size: 0.82rem; color: var(--text-muted); margin-bottom: 8px;">No active learning requests.</p>
            <button class="btn btn-primary btn-sm" onclick="navigateTo('discover')">Discover Skills to Learn</button>
          </div>
        ` : `
          <div class="mini-exchange-list">
            ${myLearning.slice(0, 3).map(l => `
              <div class="mini-exchange-item">
                <div>
                  <div class="mini-item-title">${l.skillName}</div>
                  <div class="mini-item-sub">Teacher: <strong>${l.providerName}</strong> (${l.requestedHours}h)</div>
                </div>
                <span class="status-badge ${l.status === 'COMPLETED' ? 'status-completed' : (l.status === 'PENDING' ? 'status-pending' : 'status-confirmed')}">
                  <span class="status-dot"></span> ${l.status}
                </span>
              </div>
            `).join('')}
          </div>
        `}
      </div>
    </div>

    <!-- Time Flow Metrics -->
    <div class="time-flow-summary">
      <div class="flow-card">
        <span class="flow-card-label">Time Earned (Teaching)</span>
        <span class="flow-card-val earned time-num">+${earned.toFixed(1)} h</span>
      </div>
      <div class="flow-card">
        <span class="flow-card-label">Time Spent (Learning)</span>
        <span class="flow-card-val spent time-num">-${spent.toFixed(1)} h</span>
      </div>
      <div class="flow-card">
        <span class="flow-card-label">Net Lifetime Flow</span>
        <span class="flow-card-val net time-num">${net >= 0 ? '+' : ''}${net.toFixed(1)} h</span>
      </div>
    </div>

    <!-- Signature Visuals: Time Dial & Real Exchange Flow -->
    <div class="dashboard-grid">
      <div>
        <div class="section-header-row">
          <div class="section-title-group">
            <h3>Time Bank Reserve</h3>
            <p>Your live credit balance and lifetime circulation</p>
          </div>
        </div>
        ${UIComponents.renderTimeDial(member.timeCreditBalance, earned, spent)}
      </div>

      <div>
        <div class="section-header-row">
          <div class="section-title-group">
            <h3>Live Exchange Flow</h3>
            <p>Peer-to-peer time transfer mechanics</p>
          </div>
        </div>
        ${UIComponents.renderExchangeFlow(flowTeacher, flowSkill, flowLearner, flowHours, flowCompleted)}
      </div>
    </div>

    <!-- Actionable Requests to Teach (Incoming Requests for this Member) -->
    ${incomingPending.length > 0 || incomingConfirmed.length > 0 ? `
      <div style="margin-top: 16px; margin-bottom: 28px;">
        <div class="section-header-row">
          <div class="section-title-group">
            <h3>Incoming Requests to Teach</h3>
            <p>Community members waiting for your session confirmation or completion</p>
          </div>
          <button class="btn btn-secondary btn-sm" onclick="navigateTo('requests')">View All in Queue</button>
        </div>
        <div class="requests-queue">
          ${[...incomingPending, ...incomingConfirmed].slice(0, 2).map(UIComponents.renderIncomingRequestCard).join('')}
        </div>
      </div>
    ` : ''}

    <!-- Recent Sessions -->
    <div style="margin-top: 16px;">
      <div class="section-header-row">
        <div class="section-title-group">
          <h3>Recent Session Exchanges</h3>
          <p>Exchanges you are participating in as teacher or learner</p>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="navigateTo('sessions')">View All Sessions</button>
      </div>

      ${[...store.state.requesterSessions, ...store.state.providerSessions].length === 0 ? 
        UIComponents.renderEmptyState('No recorded exchanges yet', 'Offer a skill or request a learning session to begin exchanging time.', 'Explore Skills', "navigateTo('discover')")
        : `<div class="sessions-timeline-container">${[...store.state.requesterSessions, ...store.state.providerSessions].slice(0, 3).map(s => UIComponents.renderSessionTimelineCard(s, s.providerId === member.id)).join('')}</div>`
      }
    </div>
  `;

  attachSessionActionListeners(container);
}

/**
 * 2. DISCOVER VIEW (Skills Offered by OTHER Members)
 * Self-booking is prevented: user's own skills are marked / non-requestable
 */
function renderDiscoverView() {
  const container = document.getElementById('discover-view');
  if (!container) return;

  const categories = ['ALL', 'CODE', 'DESIGN', 'LANGUAGES', 'MUSIC', 'ACADEMICS', 'CAREER', 'BUSINESS'];
  const activeCat = store.state.selectedCategory;
  const member = store.currentMember;

  // Fetch active skills from database
  let skills = store.state.skillOffers.filter(s => s.active);
  if (activeCat !== 'ALL') {
    skills = skills.filter(s => (s.category || '').toUpperCase() === activeCat);
  }
  if (store.state.searchQuery) {
    const q = store.state.searchQuery.toLowerCase();
    skills = skills.filter(s => 
      (s.skillName && s.skillName.toLowerCase().includes(q)) || 
      (s.description && s.description.toLowerCase().includes(q)) || 
      (s.providerName && s.providerName.toLowerCase().includes(q))
    );
  }

  container.innerHTML = `
    <div class="section-header-row" style="margin-bottom: 24px;">
      <div class="section-title-group">
        <h2>Discover Skill Exchanges</h2>
        <p>Browse skills offered by other community members and exchange your time</p>
      </div>
      <button class="btn btn-primary" onclick="openOfferModal()">+ Offer a Skill</button>
    </div>

    <!-- Search & Category Filters -->
    <div style="display: flex; gap: 16px; margin-bottom: 18px; align-items: center; flex-wrap: wrap;">
      <input type="text" id="skill-search-input" class="form-input" style="max-width: 340px; flex: 1;" 
        placeholder="Search skills, topics, or members..." value="${store.state.searchQuery}" />
    </div>

    <div class="category-filter-bar">
      ${categories.map(cat => `
        <button class="cat-pill ${activeCat === cat ? 'active' : ''}" data-cat="${cat}">${cat}</button>
      `).join('')}
    </div>

    <!-- Skills Grid -->
    ${skills.length === 0 ? 
      UIComponents.renderEmptyState('No skills found matching your criteria', 'Be the first to offer a skill in this category or try another search.', 'Offer a Skill', 'openOfferModal()')
      : `<div class="skills-grid">${skills.map(s => UIComponents.renderSkillCard(s, member)).join('')}</div>`
    }
  `;

  // Bind Search & Filters
  const searchInput = container.querySelector('#skill-search-input');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      store.setSearchQuery(e.target.value);
    });
  }

  container.querySelectorAll('.cat-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      const cat = pill.getAttribute('data-cat');
      store.setCategory(cat);
    });
  });

  // Bind Request Skill Buttons
  container.querySelectorAll('.request-skill-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const skillId = btn.getAttribute('data-id');
      const skillTitle = decodeURIComponent(btn.getAttribute('data-title'));
      const providerName = decodeURIComponent(btn.getAttribute('data-provider'));
      const providerId = btn.getAttribute('data-provider-id');
      const availableHours = btn.getAttribute('data-hours');
      openRequestModal(skillId, skillTitle, providerName, providerId, availableHours);
    });
  });
}

/**
 * 3. MY SKILLS VIEW (Skills Offered by CURRENT Member Only)
 */
function renderMySkillsView() {
  const container = document.getElementById('my-skills-view');
  if (!container) return;

  const member = store.currentMember;
  if (!member) return;

  // Strict Data Isolation: Only skills where providerId == currentMember.id
  const myOffers = store.state.skillOffers.filter(s => s.providerId === member.id);

  container.innerHTML = `
    <div class="section-header-row" style="margin-bottom: 24px;">
      <div class="section-title-group">
        <h2>My Active Skill Offers</h2>
        <p>Skills you teach to earn time credits from other community members</p>
      </div>
      <button class="btn btn-primary" onclick="openOfferModal()">+ Offer New Skill</button>
    </div>

    ${myOffers.length === 0 ? 
      UIComponents.renderEmptyState('You haven\'t offered any skills yet', 'Share your knowledge with the community to start earning time credits.', 'Offer Your First Skill', 'openOfferModal()')
      : `<div class="skills-grid">${myOffers.map(s => UIComponents.renderSkillCard(s, member)).join('')}</div>`
    }
  `;
}

/**
 * 4. REQUESTS QUEUE VIEW
 * Distinctly partitions INCOMING (Requests to Teach) vs OUTGOING (My Learning Requests)
 */
function renderRequestsView() {
  const container = document.getElementById('requests-view');
  if (!container) return;

  const member = store.currentMember;
  if (!member) return;

  const incomingRequests = store.state.providerSessions; // provider == member
  const outgoingRequests = store.state.requesterSessions; // requester == member

  const pendingIncomingCount = incomingRequests.filter(s => s.status === 'PENDING').length;

  container.innerHTML = `
    <div class="section-header-row" style="margin-bottom: 20px;">
      <div class="section-title-group">
        <h2>Session Requests Queue</h2>
        <p>Manage incoming teaching requests from learners and track your own learning requests</p>
      </div>
    </div>

    <!-- Sub-tabs: Incoming vs Outgoing -->
    <div class="view-subtabs-bar">
      <button class="subtab-btn ${activeRequestSubTab === 'incoming' ? 'active' : ''}" id="tab-btn-incoming">
        <span>📥 Requests to Teach (Incoming)</span>
        <span class="subtab-badge">${incomingRequests.length}</span>
      </button>
      <button class="subtab-btn ${activeRequestSubTab === 'learning' ? 'active' : ''}" id="tab-btn-learning">
        <span>📤 My Learning Requests (Outgoing)</span>
        <span class="subtab-badge">${outgoingRequests.length}</span>
      </button>
    </div>

    <!-- Sub-tab 1: Incoming Requests to Teach -->
    <div id="subtab-incoming-content" style="${activeRequestSubTab === 'incoming' ? 'display: block;' : 'display: none;'}">
      ${incomingRequests.length === 0 ? 
        UIComponents.renderEmptyState('No incoming requests yet', 'When another member discovers one of your skills and requests time, their request will appear here.')
        : `<div class="requests-queue">${incomingRequests.map(UIComponents.renderIncomingRequestCard).join('')}</div>`
      }
    </div>

    <!-- Sub-tab 2: Outgoing Learning Requests -->
    <div id="subtab-learning-content" style="${activeRequestSubTab === 'learning' ? 'display: block;' : 'display: none;'}">
      ${outgoingRequests.length === 0 ? 
        UIComponents.renderEmptyState('No learning requests made', 'Browse the Discover page to find skills you want to learn from community members.', 'Discover Skills', "navigateTo('discover')")
        : `<div class="requests-queue">${outgoingRequests.map(UIComponents.renderOutgoingRequestCard).join('')}</div>`
      }
    </div>
  `;

  // Bind Subtab switching
  const tabIncoming = container.querySelector('#tab-btn-incoming');
  const tabLearning = container.querySelector('#tab-btn-learning');

  if (tabIncoming && tabLearning) {
    tabIncoming.addEventListener('click', () => {
      activeRequestSubTab = 'incoming';
      renderRequestsView();
    });
    tabLearning.addEventListener('click', () => {
      activeRequestSubTab = 'learning';
      renderRequestsView();
    });
  }

  attachSessionActionListeners(container);
}

/**
 * 5. SESSIONS VIEW (Timeline of All Teaching & Learning Exchanges)
 */
function renderSessionsView() {
  const container = document.getElementById('sessions-view');
  if (!container) return;

  const member = store.currentMember;
  if (!member) return;

  const allSessions = [...store.state.requesterSessions, ...store.state.providerSessions];

  container.innerHTML = `
    <div class="section-header-row" style="margin-bottom: 24px;">
      <div class="section-title-group">
        <h2>Exchange Sessions Timeline</h2>
        <p>Complete lifecycle tracking of your teaching and learning sessions</p>
      </div>
    </div>

    ${allSessions.length === 0 ? 
      UIComponents.renderEmptyState('No exchange sessions found', 'Request a skill exchange or offer a skill to get started.', 'Explore Skills', "navigateTo('discover')")
      : `<div class="sessions-timeline-container">${allSessions.map(s => UIComponents.renderSessionTimelineCard(s, s.providerId === member.id)).join('')}</div>`
    }
  `;

  attachSessionActionListeners(container);
}

/**
 * 6. TIME LEDGER JOURNAL VIEW (Current Member Ledger Only)
 */
function renderLedgerView() {
  const container = document.getElementById('ledger-view');
  if (!container) return;

  const member = store.currentMember;
  if (!member) return;

  // Strict Data Isolation: Only ledger entries where memberId == currentMember.id
  const entries = store.state.ledgerEntries;

  let totalEarned = 0;
  let totalSpent = 0;
  entries.forEach(e => {
    if (e.entryType === 'CREDIT') totalEarned += Number(e.amount || 0);
    if (e.entryType === 'DEBIT') totalSpent += Number(e.amount || 0);
  });

  container.innerHTML = `
    <div class="section-header-row" style="margin-bottom: 24px;">
      <div class="section-title-group">
        <h2>Time Credit Ledger</h2>
        <p>Chronological journal of earned and spent time credits</p>
      </div>
      <div style="display: flex; gap: 12px; align-items: center;">
        <span class="time-num" style="font-size: 1.1rem; color: var(--accent-amber); font-weight: 700;">
          Reserve: ${(Number(member.timeCreditBalance) || 0).toFixed(1)} Hours
        </span>
      </div>
    </div>

    <div class="time-flow-summary" style="margin-bottom: 24px;">
      <div class="flow-card">
        <span class="flow-card-label">Total Time Earned (Teaching)</span>
        <span class="flow-card-val earned time-num">+${totalEarned.toFixed(1)} h</span>
      </div>
      <div class="flow-card">
        <span class="flow-card-label">Total Time Spent (Learning)</span>
        <span class="flow-card-val spent time-num">-${totalSpent.toFixed(1)} h</span>
      </div>
      <div class="flow-card">
        <span class="flow-card-label">Current Reserve Balance</span>
        <span class="flow-card-val net time-num">${(Number(member.timeCreditBalance) || 0).toFixed(1)} h</span>
      </div>
    </div>

    ${entries.length === 0 ? 
      UIComponents.renderEmptyState('No ledger entries yet', 'Completed sessions will automatically record debit and credit transfers here in your personal journal.')
      : `<div class="ledger-journal">${entries.map(UIComponents.renderLedgerEntry).join('')}</div>`
    }
  `;
}

/**
 * 7. PROFILE VIEW
 */
function renderProfileView() {
  const container = document.getElementById('profile-view');
  if (!container) return;

  const member = store.currentMember;
  if (!member) return;

  const myOffers = store.state.skillOffers.filter(s => s.providerId === member.id);
  const completedSessions = [...store.state.requesterSessions, ...store.state.providerSessions].filter(s => s.status === 'COMPLETED');

  container.innerHTML = `
    <div class="profile-grid">
      <div class="profile-identity-card">
        <div class="profile-avatar-lg">${member.initials || 'SS'}</div>
        <h3 class="profile-name">${member.name}</h3>
        <span class="profile-role-tag">Community Exchange Member</span>

        <div class="profile-contact-list">
          <div>Email: <strong>${member.email}</strong></div>
          <div>Phone: <strong>${member.phone || 'N/A'}</strong></div>
          <div>Member ID: <strong>#${member.id}</strong></div>
        </div>

        <div class="profile-stat-blocks">
          <div class="p-stat-box">
            <span class="p-stat-label">Available Time</span>
            <div class="p-stat-val" style="color: var(--accent-amber);">${(Number(member.timeCreditBalance) || 0).toFixed(1)}h</div>
          </div>
          <div class="p-stat-box">
            <span class="p-stat-label">Completed Exchanges</span>
            <div class="p-stat-val">${completedSessions.length}</div>
          </div>
        </div>
      </div>

      <div>
        <div class="section-header-row">
          <div class="section-title-group">
            <h3>Skills I Teach</h3>
            <p>Your active offerings to the SkillSwap network</p>
          </div>
          <button class="btn btn-primary btn-sm" onclick="openOfferModal()">+ Offer Skill</button>
        </div>

        ${myOffers.length === 0 ? 
          UIComponents.renderEmptyState('No skills offered yet', 'Add your knowledge and skills to earn credits.', 'Offer a Skill', 'openOfferModal()')
          : `<div class="skills-grid">${myOffers.map(s => UIComponents.renderSkillCard(s, member)).join('')}</div>`
        }
      </div>
    </div>
  `;
}

/**
 * Action Listeners: Confirm and Complete Session Buttons
 */
function attachSessionActionListeners(scope) {
  // Confirm Button (Provider Acceptance)
  scope.querySelectorAll('.confirm-session-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
      const sessionId = btn.getAttribute('data-id');
      const providerId = store.currentMember.id;
      btn.disabled = true;
      btn.textContent = 'Accepting...';

      try {
        await ApiService.confirmSessionRequest(sessionId, providerId);
        UIComponents.showToast('Session accepted! Request confirmed.', 'success');
        await store.refreshMemberData();
      } catch (err) {
        UIComponents.showToast(err.message || 'Failed to accept session', 'error');
        btn.disabled = false;
        btn.textContent = '✓ Accept Request';
      }
    });
  });

  // Complete Button (Provider Logging Delivered Hours)
  scope.querySelectorAll('.complete-session-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const sessionId = btn.getAttribute('data-id');
      const reqHours = btn.getAttribute('data-req-hours');
      const skillTitle = decodeURIComponent(btn.getAttribute('data-skill'));
      const requesterName = decodeURIComponent(btn.getAttribute('data-requester'));
      openCompleteModal(sessionId, reqHours, skillTitle, requesterName);
    });
  });
}

/**
 * MODALS SETUP & HANDLERS
 */
function setupModals() {
  // Modal Close buttons
  document.querySelectorAll('.modal-close-btn, .modal-cancel-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
    });
  });

  // Close when clicking overlay backdrop
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        overlay.classList.remove('active');
      }
    });
  });

  // 1. Request Session Form Submit
  const reqForm = document.getElementById('request-session-form');
  if (reqForm) {
    reqForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const skillId = Number(document.getElementById('req-modal-skill-id').value);
      const hours = Number(document.getElementById('req-modal-hours-input').value);
      const requesterId = store.currentMember.id;
      const submitBtn = document.getElementById('req-modal-submit-btn');

      if (!skillId || !hours || !requesterId) {
        UIComponents.showToast('Please specify valid session hours.', 'error');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Requesting...';

      try {
        await ApiService.createSessionRequest({
          skillOfferId: skillId,
          requesterId: requesterId,
          requestedHours: hours
        });

        UIComponents.showToast(`Requested ${hours} hours! Credits will transfer only upon delivery.`, 'success');
        closeModals();
        await store.refreshMemberData();
        activeRequestSubTab = 'learning';
        navigateTo('requests');
      } catch (err) {
        UIComponents.showToast(err.message || 'Failed to request session', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Confirm Request';
      }
    });
  }

  // 2. Complete Session Form Submit
  const completeForm = document.getElementById('complete-session-form');
  if (completeForm) {
    completeForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const sessionId = Number(document.getElementById('complete-modal-session-id').value);
      const deliveredHours = Number(document.getElementById('complete-modal-hours-input').value);
      const providerId = store.currentMember.id;
      const submitBtn = document.getElementById('complete-modal-submit-btn');

      if (!sessionId || !deliveredHours || !providerId) {
        UIComponents.showToast('Please enter valid delivered hours.', 'error');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Processing Transfer...';

      try {
        await ApiService.completeSessionRequest(sessionId, providerId, deliveredHours);
        UIComponents.showToast(`Session completed! +${deliveredHours}h credited to your account.`, 'success');
        closeModals();
        await store.refreshAllMembers();
        await store.refreshMemberData();
        navigateTo('ledger');
      } catch (err) {
        UIComponents.showToast(err.message || 'Failed to complete session', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Log & Transfer Time';
      }
    });
  }

  // 3. Offer Skill Form Submit (Exact SkillOfferRequest mapping)
  const offerForm = document.getElementById('offer-skill-form');
  if (offerForm) {
    offerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const skillName = document.getElementById('offer-modal-title').value;
      const description = document.getElementById('offer-modal-description').value;
      const hoursAvailable = Number(document.getElementById('offer-modal-hours').value);
      const providerId = store.currentMember ? store.currentMember.id : null;
      const submitBtn = document.getElementById('offer-modal-submit-btn');

      if (!providerId) {
        UIComponents.showToast('Please select an active participant first.', 'error');
        return;
      }

      if (!skillName.trim() || !description.trim() || hoursAvailable <= 0) {
        UIComponents.showToast('Please fill in all required fields with positive hours.', 'error');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Publishing...';

      try {
        // Sends exact DTO { skillName, description, hoursAvailable, providerId }
        await ApiService.createSkillOffer({
          skillName,
          description,
          hoursAvailable,
          providerId
        });

        UIComponents.showToast(`Skill offer "${skillName}" published successfully!`, 'success');
        closeModals();
        offerForm.reset();
        await store.refreshSkills();
        await store.refreshMemberData();
        navigateTo('my-skills');
      } catch (err) {
        UIComponents.showToast(err.message || 'Failed to offer skill', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Publish Skill Offer';
      }
    });
  }

  // 4. Create Member Form Submit
  const memberForm = document.getElementById('create-member-form');
  if (memberForm) {
    memberForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const name = document.getElementById('member-modal-name').value;
      const email = document.getElementById('member-modal-email').value;
      const phone = document.getElementById('member-modal-phone').value;
      const submitBtn = document.getElementById('member-modal-submit-btn');

      if (!name.trim() || !email.trim() || !phone.trim()) {
        UIComponents.showToast('Please fill in all member fields.', 'error');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Registering...';

      try {
        const newMember = await ApiService.createMember({
          name,
          email,
          phone
        });

        UIComponents.showToast(`Welcome ${newMember.name}!`, 'success');
        closeModals();
        memberForm.reset();
        await store.init();
        await store.setMember(newMember.id);
      } catch (err) {
        UIComponents.showToast(err.message || 'Failed to create member', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register Member';
      }
    });
  }
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('active');
}

function closeModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('active'));
}

window.openOfferModal = () => {
  openModal('offer-skill-modal');
};

function openRequestModal(skillId, skillTitle, providerName, providerId, maxHours) {
  const modal = document.getElementById('request-session-modal');
  if (!modal) return;

  const currentMember = store.currentMember;
  document.getElementById('req-modal-skill-id').value = skillId;
  document.getElementById('req-modal-skill-title').textContent = skillTitle;
  document.getElementById('req-modal-provider-name').textContent = providerName;
  
  const hoursInput = document.getElementById('req-modal-hours-input');
  hoursInput.max = maxHours;
  hoursInput.value = Math.min(2, maxHours);

  const updatePreview = () => {
    const requested = Number(hoursInput.value) || 0;
    const current = Number(currentMember ? currentMember.timeCreditBalance : 0) || 0;
    const remaining = current - requested;

    document.getElementById('calc-current-balance').textContent = `${current.toFixed(1)} h`;
    document.getElementById('calc-session-cost').textContent = `-${requested.toFixed(1)} h`;
    
    const remEl = document.getElementById('calc-remaining-balance');
    remEl.textContent = `${remaining.toFixed(1)} h`;
    remEl.style.color = remaining < 0 ? 'var(--color-spent)' : 'var(--text-primary)';

    const submitBtn = document.getElementById('req-modal-submit-btn');
    if (remaining < 0) {
      submitBtn.disabled = true;
      submitBtn.textContent = 'Insufficient Time Credits';
    } else {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Confirm Request';
    }
  };

  hoursInput.oninput = updatePreview;
  updatePreview();

  modal.classList.add('active');
}

function openCompleteModal(sessionId, requestedHours, skillTitle, requesterName) {
  const modal = document.getElementById('complete-session-modal');
  if (!modal) return;

  document.getElementById('complete-modal-session-id').value = sessionId;
  document.getElementById('complete-modal-skill-title').textContent = skillTitle;
  document.getElementById('complete-modal-requester-name').textContent = requesterName;
  
  const hoursInput = document.getElementById('complete-modal-hours-input');
  hoursInput.max = requestedHours;
  hoursInput.value = requestedHours;

  const updateDeliveryPreview = () => {
    const delivered = Number(hoursInput.value) || 0;
    document.getElementById('complete-calc-provider-gain').textContent = `+${delivered.toFixed(1)} h Earned`;
    document.getElementById('complete-calc-requester-debit').textContent = `-${delivered.toFixed(1)} h Debited`;
  };

  hoursInput.oninput = updateDeliveryPreview;
  updateDeliveryPreview();

  modal.classList.add('active');
}

/**
 * Global Event Listeners & Reset / Demo Controls
 */
function setupEventListeners() {
  // Clean Slate Button (Empty Skills/Sessions/Ledger + Clean Members)
  const cleanResetBtn = document.getElementById('clean-reset-btn');
  if (cleanResetBtn) {
    cleanResetBtn.addEventListener('click', async () => {
      cleanResetBtn.disabled = true;
      cleanResetBtn.textContent = 'Resetting...';
      try {
        await ApiService.triggerResetClean();
        UIComponents.showToast('Database reset to clean slate! Ready for live demonstration.', 'success');
        await store.init();
        navigateTo('dashboard');
      } catch (err) {
        UIComponents.showToast('Failed to reset: ' + err.message, 'error');
      } finally {
        cleanResetBtn.disabled = false;
        cleanResetBtn.textContent = '🧹 Clean Slate';
      }
    });
  }

  // Sample Pack Button
  const seedBtn = document.getElementById('dev-seed-btn');
  if (seedBtn) {
    seedBtn.addEventListener('click', async () => {
      seedBtn.disabled = true;
      seedBtn.textContent = 'Seeding...';
      try {
        await ApiService.triggerDevSeed(true);
        UIComponents.showToast('Sample demo pack loaded successfully from MySQL!', 'success');
        await store.init();
      } catch (err) {
        UIComponents.showToast('Failed to load sample pack: ' + err.message, 'error');
      } finally {
        seedBtn.disabled = false;
        seedBtn.textContent = '📦 Sample Pack';
      }
    });
  }
}

window.navigateTo = navigateTo;
window.openModal = openModal;
window.closeModals = closeModals;
