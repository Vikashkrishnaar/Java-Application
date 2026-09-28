/**
 * ============================================================================
 * SKILLSWAP UI COMPONENT RENDERERS
 * Bespoke Visual Language: Time Dial, Exchange Flow, Time Ledger, Human Cards
 * All renderers use safe view model attributes with complete fallback guards
 * ============================================================================
 */

class UIComponents {
  /**
   * Signature Component 1: TIME DIAL SVG GAUGE
   * Renders a circular dial representing available time hours
   */
  static renderTimeDial(balanceHours = 0, earnedHours = 0, spentHours = 0) {
    const maxCapacity = 20; // dial scale reference
    const radius = 60;
    const circumference = 2 * Math.PI * radius; // ~377
    const clampedBalance = Math.max(0, Math.min(Number(balanceHours) || 0, maxCapacity));
    const offset = circumference - (clampedBalance / maxCapacity) * circumference;

    return `
      <div class="time-dial-card">
        <div class="time-dial-svg-wrapper">
          <svg class="time-dial-svg" viewBox="0 0 140 140">
            <defs>
              <linearGradient id="dialGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#c56a3a" />
                <stop offset="100%" stop-color="#89947a" />
              </linearGradient>
            </defs>
            <circle class="time-dial-circle-bg" cx="70" cy="70" r="${radius}" />
            <circle class="time-dial-circle-fill" cx="70" cy="70" r="${radius}"
              style="stroke-dasharray: ${circumference}; stroke-dashoffset: ${offset};" />
          </svg>
          <div class="time-dial-inner-text">
            <span class="dial-value time-num">${(Number(balanceHours) || 0).toFixed(1)}</span>
            <span class="dial-unit">Hours</span>
          </div>
        </div>
        
        <div class="time-dial-stats">
          <div class="dial-stat-item">
            <span class="dial-stat-label">
              <span class="dial-stat-dot" style="background-color: var(--color-earned);"></span>
              Time Earned
            </span>
            <span class="dial-stat-value time-num" style="color: var(--color-earned);">+${(Number(earnedHours) || 0).toFixed(1)}h</span>
          </div>
          <div class="dial-stat-item">
            <span class="dial-stat-label">
              <span class="dial-stat-dot" style="background-color: var(--color-spent);"></span>
              Time Spent
            </span>
            <span class="dial-stat-value time-num" style="color: var(--color-spent);">-${(Number(spentHours) || 0).toFixed(1)}h</span>
          </div>
          <div class="dial-stat-item">
            <span class="dial-stat-label">
              <span class="dial-stat-dot" style="background-color: var(--accent-amber);"></span>
              Net Reserve
            </span>
            <span class="dial-stat-value time-num" style="color: var(--accent-amber);">${(Number(balanceHours) || 0).toFixed(1)}h</span>
          </div>
        </div>
      </div>
    `;
  }

  /**
   * Signature Component 2: EXCHANGE FLOW DIAGRAM
   * Shows: Provider → Skill → Requester → Time Credits
   */
  static renderExchangeFlow(providerName, skillTitle, requesterName, hours = 2, isCompleted = false) {
    return `
      <div class="exchange-flow-banner">
        <div class="exchange-flow-steps">
          <div class="flow-node">
            <span class="flow-node-label">Teacher (Provider)</span>
            <span class="flow-node-name">${providerName || 'Provider'}</span>
            <span class="flow-node-credit plus">+${hours}h ${isCompleted ? 'Earned' : 'Upon Completion'}</span>
          </div>

          <div class="flow-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
            <span>Teaches</span>
          </div>

          <div class="diagram-skill-pill">
            ${skillTitle || 'Skill Exchange'}
          </div>

          <div class="flow-arrow">
            <svg viewBox="0 0 24 24" fill="none" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14M12 5l7 7-7 7"/>
            </svg>
            <span>Learns</span>
          </div>

          <div class="flow-node">
            <span class="flow-node-label">Learner (Requester)</span>
            <span class="flow-node-name">${requesterName || 'Requester'}</span>
            <span class="flow-node-credit minus">-${hours}h ${isCompleted ? 'Debited' : 'Upon Completion'}</span>
          </div>
        </div>
      </div>
    `;
  }

  /**
   * SKILL CARD COMPONENT (Human Skill Offer)
   */
  static renderSkillCard(offer, currentMember) {
    if (!offer) return '';
    const skillId = offer.id;
    const skillName = offer.skillName || offer.title || 'Untitled Skill';
    const providerId = offer.providerId || offer.memberId;
    const providerName = offer.providerName || offer.memberName || 'Community Member';
    const hoursAvailable = offer.hoursAvailable != null ? offer.hoursAvailable : (offer.availableHours || 0);
    const category = offer.category || 'General';
    const description = offer.description || 'Offering skill exchange and hands-on guidance.';
    const isOwnSkill = currentMember && providerId === currentMember.id;
    const initials = offer.initials || (providerName.split(' ').map(n => n[0]).join('').toUpperCase() || 'SS');

    return `
      <div class="skill-card" data-id="${skillId}">
        <div>
          <div class="skill-card-header">
            <span class="skill-cat-tag">${category}</span>
            <span class="status-badge ${offer.active ? 'status-completed' : 'status-cancelled'}">
              <span class="status-dot"></span> ${offer.active ? 'Active Offer' : 'Paused'}
            </span>
          </div>
          
          <h4 class="skill-card-title">${skillName}</h4>
          
          <div class="skill-provider-meta">
            <div class="skill-provider-avatar">${initials}</div>
            <div class="skill-provider-name">Offered by <strong>${providerName}</strong></div>
          </div>
          
          <p class="skill-card-desc">"${description}"</p>
        </div>

        <div class="skill-card-footer">
          <div class="skill-time-available">
            <span class="time-avail-label">Available Capacity</span>
            <span class="time-avail-num time-num">${hoursAvailable} <span>Hours</span></span>
          </div>

          ${isOwnSkill ? `
            <span class="btn btn-secondary btn-sm" style="opacity: 0.7; cursor: default;">Your Offer</span>
          ` : `
            <button class="btn btn-primary btn-sm request-skill-btn" 
              data-id="${skillId}" 
              data-title="${encodeURIComponent(skillName)}" 
              data-provider="${encodeURIComponent(providerName)}"
              data-provider-id="${providerId}"
              data-hours="${hoursAvailable}">
              Request Session
            </button>
          `}
        </div>
      </div>
    `;
  }

  /**
   * INCOMING REQUEST CARD (Provider Teaching Queue)
   */
  static renderIncomingRequestCard(session) {
    if (!session) return '';
    const isPending = session.status === 'PENDING';
    const isConfirmed = session.status === 'CONFIRMED';
    const isCompleted = session.status === 'COMPLETED';
    const initials = session.requesterInitials || 'RQ';

    return `
      <div class="request-card" data-session-id="${session.id}">
        <div class="req-left">
          <div class="req-avatar">${initials}</div>
          <div class="req-info">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="req-skill-title">${session.skillName}</span>
              <span class="status-badge ${isCompleted ? 'status-completed' : (isPending ? 'status-pending' : 'status-confirmed')}">
                <span class="status-dot"></span> ${session.status}
              </span>
            </div>
            <span class="req-members-sub">Learner: <strong>${session.requesterName}</strong> wants to learn from you</span>
            <span class="req-time-block time-num">⏱ ${session.requestedHours} Hours Requested</span>
          </div>
        </div>

        <div class="req-actions">
          ${isPending ? `
            <button class="btn btn-primary btn-sm confirm-session-btn" data-id="${session.id}">
              ✓ Accept Request
            </button>
          ` : ''}

          ${isConfirmed ? `
            <button class="btn btn-sage btn-sm complete-session-btn" 
              data-id="${session.id}" 
              data-req-hours="${session.requestedHours}"
              data-skill="${encodeURIComponent(session.skillName)}"
              data-requester="${encodeURIComponent(session.requesterName)}">
              Log Delivery & Earn +${session.requestedHours}h
            </button>
          ` : ''}

          ${isCompleted ? `
            <span class="time-num" style="font-size: 0.85rem; color: var(--color-earned); font-weight: 700;">
              ✓ Completed (+${session.deliveredHours || session.requestedHours}h Earned)
            </span>
          ` : ''}
        </div>
      </div>
    `;
  }

  /**
   * OUTGOING LEARNING REQUEST CARD (Requester Learning Queue)
   */
  static renderOutgoingRequestCard(session) {
    if (!session) return '';
    const isPending = session.status === 'PENDING';
    const isConfirmed = session.status === 'CONFIRMED';
    const isCompleted = session.status === 'COMPLETED';
    const initials = session.providerInitials || 'PR';

    return `
      <div class="request-card" data-session-id="${session.id}">
        <div class="req-left">
          <div class="req-avatar">${initials}</div>
          <div class="req-info">
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="req-skill-title">${session.skillName}</span>
              <span class="status-badge ${isCompleted ? 'status-completed' : (isPending ? 'status-pending' : 'status-confirmed')}">
                <span class="status-dot"></span> ${session.status}
              </span>
            </div>
            <span class="req-members-sub">Teacher: <strong>${session.providerName}</strong></span>
            <span class="req-time-block time-num">⏱ ${session.requestedHours} Hours Requested</span>
          </div>
        </div>

        <div class="req-actions">
          ${isPending ? `
            <span style="font-size: 0.82rem; color: var(--text-muted); font-style: italic;">
              ⏳ Waiting for provider confirmation
            </span>
          ` : ''}

          ${isConfirmed ? `
            <span style="font-size: 0.85rem; color: var(--accent-amber); font-weight: 700;">
              ✓ Provider accepted! Session confirmed
            </span>
          ` : ''}

          ${isCompleted ? `
            <span class="time-num" style="font-size: 0.85rem; color: var(--color-spent); font-weight: 700;">
              ✓ Completed (-${session.deliveredHours || session.requestedHours}h Debited)
            </span>
          ` : ''}
        </div>
      </div>
    `;
  }

  /**
   * SESSION TIMELINE CARD
   */
  static renderSessionTimelineCard(session, isProvider) {
    if (!session) return '';
    const skillName = session.skillName || session.skillTitle || 'Skill Exchange Session';
    const requesterName = session.requesterName || 'Community Member';
    const providerName = session.providerName || 'Community Teacher';
    const requestedHours = session.requestedHours || 0;
    const deliveredHours = session.deliveredHours != null ? session.deliveredHours : session.actualHours;
    const status = session.status || 'PENDING';

    const isPending = status === 'PENDING';
    const isConfirmed = status === 'CONFIRMED';
    const isCompleted = status === 'COMPLETED';

    const statusBadgeClass = isCompleted ? 'status-completed' : (isPending ? 'status-pending' : (isConfirmed ? 'status-confirmed' : 'status-cancelled'));

    return `
      <div class="timeline-card" data-session-id="${session.id}">
        <div class="section-header-row">
          <div>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span class="status-badge ${statusBadgeClass}">
                <span class="status-dot"></span> ${status}
              </span>
              <span class="status-badge" style="background: var(--bg-surface-elevated); border: 1px solid var(--border-muted); color: var(--text-secondary);">
                ${isProvider ? 'ROLE: TEACHER' : 'ROLE: LEARNER'}
              </span>
            </div>
            <h4 style="margin-top: 8px; font-size: 1.15rem;">${skillName}</h4>
          </div>
          <div style="text-align: right;">
            <span class="time-avail-label">Exchange Duration</span>
            <div class="time-num" style="font-size: 1.15rem; font-weight: 700; color: var(--accent-amber);">
              ${deliveredHours != null ? deliveredHours : requestedHours} Hours
            </div>
          </div>
        </div>

        <!-- Visual 3-step Timeline -->
        <div class="timeline-tracker">
          <div class="timeline-step completed">
            <div class="timeline-node-circle">1</div>
            <span class="timeline-step-label">Requested</span>
          </div>
          <div class="timeline-step ${isConfirmed || isCompleted ? 'completed' : (isPending ? 'active' : '')}">
            <div class="timeline-node-circle">2</div>
            <span class="timeline-step-label">Confirmed</span>
          </div>
          <div class="timeline-step ${isCompleted ? 'completed' : ''}">
            <div class="timeline-node-circle">3</div>
            <span class="timeline-step-label">Delivered & Credited</span>
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border-subtle); padding-top: 14px; margin-top: 12px; flex-wrap: wrap; gap: 10px;">
          <div style="font-size: 0.85rem; color: var(--text-secondary);">
            Learner: <strong style="color: var(--text-primary);">${requesterName}</strong> | Teacher: <strong style="color: var(--text-primary);">${providerName}</strong>
          </div>

          <div style="display: flex; gap: 8px;">
            ${isProvider && isPending ? `
              <button class="btn btn-primary btn-sm confirm-session-btn" data-id="${session.id}">
                ✓ Accept Request
              </button>
            ` : ''}

            ${isProvider && isConfirmed ? `
              <button class="btn btn-sage btn-sm complete-session-btn" 
                data-id="${session.id}" 
                data-req-hours="${requestedHours}"
                data-skill="${encodeURIComponent(skillName)}"
                data-requester="${encodeURIComponent(requesterName)}">
                Log Delivery & Complete
              </button>
            ` : ''}

            ${!isProvider && isPending ? `
              <span style="font-size: 0.8rem; color: var(--text-muted); font-style: italic;">
                ⏳ Waiting for provider confirmation...
              </span>
            ` : ''}

            ${!isProvider && isConfirmed ? `
              <span style="font-size: 0.8rem; color: var(--accent-amber); font-weight: 600;">
                ✓ Provider accepted! Session ready for delivery.
              </span>
            ` : ''}

            ${isCompleted ? `
              <span class="time-num" style="font-size: 0.82rem; color: var(--color-earned); font-weight: 700;">
                ✓ ${deliveredHours || requestedHours}h transferred in ledger
              </span>
            ` : ''}
          </div>
        </div>
      </div>
    `;
  }

  /**
   * Signature Component 3: TIME LEDGER JOURNAL ENTRY
   */
  static renderLedgerEntry(entry) {
    if (!entry) return '';
    const isCredit = (entry.entryType || entry.type) === 'CREDIT';
    const amount = Number(entry.amount != null ? entry.amount : entry.hours) || 0;
    const amountSign = isCredit ? '+' : '-';
    const indicatorClass = isCredit ? 'credit' : 'debit';
    const description = entry.description || 'Time Credit Exchange';
    const sessionId = entry.sessionRequestId != null ? entry.sessionRequestId : entry.sessionId;
    const dateFormatted = entry.createdAt ? new Date(entry.createdAt).toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }) : 'Recent';

    return `
      <div class="ledger-row">
        <div class="ledger-left">
          <div class="ledger-type-indicator ${indicatorClass}">
            ${isCredit ? '↓' : '↑'}
          </div>
          <div class="ledger-details">
            <span class="ledger-title">${description}</span>
            <span class="ledger-subtitle">
              <span>Session #${sessionId || 'N/A'}</span>
              <span>•</span>
              <span>${isCredit ? 'Time Earned (Teaching)' : 'Time Spent (Learning)'}</span>
            </span>
          </div>
        </div>

        <div class="ledger-right">
          <span class="ledger-amount ${indicatorClass} time-num">
            ${amountSign}${amount.toFixed(1)} h
          </span>
          <span class="ledger-date">${dateFormatted}</span>
        </div>
      </div>
    `;
  }

  /**
   * CONTEXTUAL EMPTY STATE
   */
  static renderEmptyState(title, description, ctaText = null, ctaAction = null) {
    return `
      <div class="empty-state">
        <div class="empty-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
        </div>
        <h4 class="empty-title">${title}</h4>
        <p class="empty-desc">${description}</p>
        ${ctaText ? `<button class="btn btn-primary btn-sm empty-cta-btn" onclick="${ctaAction}">${ctaText}</button>` : ''}
      </div>
    `;
  }

  /**
   * TOAST NOTIFICATION
   */
  static showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.className = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
      <span style="font-size: 1.1rem;">${type === 'success' ? '✓' : (type === 'error' ? '⚠' : '⏱')}</span>
      <span class="toast-msg">${message}</span>
    `;

    container.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(20px)';
      toast.style.transition = 'all 200ms ease';
      setTimeout(() => toast.remove(), 200);
    }, 4500);
  }
}

window.UIComponents = UIComponents;
