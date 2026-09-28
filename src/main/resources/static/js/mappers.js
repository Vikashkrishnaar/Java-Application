/**
 * ============================================================================
 * SKILLSWAP DATA MAPPERS & VIEW MODEL TRANSFORMERS
 * Prevents undefined / null errors by transforming backend DTOs into safe View Models
 * ============================================================================
 */

class DataMappers {
  /**
   * Transforms backend MemberResponse -> MemberViewModel
   */
  static mapMember(m) {
    if (!m) return null;
    return {
      id: Number(m.id),
      name: m.name || 'Anonymous Member',
      email: m.email || '',
      phone: m.phone || 'N/A',
      timeCreditBalance: Number(m.timeCreditBalance || 0),
      initials: (m.name || 'SS')
        .split(' ')
        .map(n => n[0])
        .filter(Boolean)
        .join('')
        .toUpperCase()
        .substring(0, 2)
    };
  }

  /**
   * Transforms backend SkillOfferResponse -> SkillOfferViewModel
   */
  static mapSkillOffer(s) {
    if (!s) return null;
    const skillName = s.skillName || 'Untitled Skill';
    const description = s.description || 'Offering skill exchange and hands-on time.';
    const hoursAvailable = Number(s.hoursAvailable != null ? s.hoursAvailable : 0);
    const providerId = Number(s.providerId != null ? s.providerId : 0);
    const providerName = s.providerName || 'Community Member';
    const status = s.status || 'ACTIVE';

    // Infer category if not explicit
    let category = 'GENERAL';
    const lowerTitle = skillName.toLowerCase();
    const lowerDesc = description.toLowerCase();

    if (lowerTitle.includes('python') || lowerTitle.includes('java') || lowerTitle.includes('spring') || lowerTitle.includes('web') || lowerTitle.includes('sql') || lowerTitle.includes('git') || lowerTitle.includes('code')) {
      category = 'CODE';
    } else if (lowerTitle.includes('ui') || lowerTitle.includes('ux') || lowerTitle.includes('figma') || lowerTitle.includes('design')) {
      category = 'DESIGN';
    } else if (lowerTitle.includes('data') || lowerTitle.includes('pandas') || lowerTitle.includes('machine learning') || lowerTitle.includes('ml') || lowerTitle.includes('academic')) {
      category = 'ACADEMICS';
    } else if (lowerTitle.includes('speaking') || lowerTitle.includes('interview') || lowerTitle.includes('career') || lowerTitle.includes('resume')) {
      category = 'CAREER';
    } else if (lowerTitle.includes('excel') || lowerTitle.includes('business') || lowerTitle.includes('finance')) {
      category = 'BUSINESS';
    } else if (lowerTitle.includes('spanish') || lowerTitle.includes('french') || lowerTitle.includes('german') || lowerTitle.includes('language')) {
      category = 'LANGUAGES';
    } else if (lowerTitle.includes('music') || lowerTitle.includes('guitar') || lowerTitle.includes('piano')) {
      category = 'MUSIC';
    }

    return {
      id: Number(s.id),
      skillName: skillName,
      title: skillName, // alias for safe UI access
      description: description,
      hoursAvailable: hoursAvailable,
      availableHours: hoursAvailable, // alias for safe UI access
      status: status,
      active: status === 'ACTIVE',
      providerId: providerId,
      memberId: providerId, // alias for safe UI access
      providerName: providerName,
      memberName: providerName, // alias for safe UI access
      category: category,
      initials: providerName
        .split(' ')
        .map(n => n[0])
        .filter(Boolean)
        .join('')
        .toUpperCase()
        .substring(0, 2)
    };
  }

  /**
   * Transforms backend SessionRequestResponse -> SessionRequestViewModel
   */
  static mapSession(sr) {
    if (!sr) return null;
    const skillName = sr.skillName || 'Skill Exchange Session';
    const requestedHours = Number(sr.requestedHours || 0);
    const deliveredHours = sr.deliveredHours != null ? Number(sr.deliveredHours) : null;
    const status = sr.status || 'PENDING';
    const requesterName = sr.requesterName || 'Community Learner';
    const providerName = sr.providerName || 'Community Teacher';

    return {
      id: Number(sr.id),
      requestedHours: requestedHours,
      deliveredHours: deliveredHours,
      actualHours: deliveredHours, // alias
      status: status,
      requestedAt: sr.requestedAt,
      completedAt: sr.completedAt,
      requesterId: Number(sr.requesterId),
      requesterName: requesterName,
      skillOfferId: Number(sr.skillOfferId),
      skillName: skillName,
      skillTitle: skillName, // alias for safe UI access
      providerId: Number(sr.providerId),
      providerName: providerName,
      requesterInitials: requesterName
        .split(' ')
        .map(n => n[0])
        .filter(Boolean)
        .join('')
        .toUpperCase()
        .substring(0, 2),
      providerInitials: providerName
        .split(' ')
        .map(n => n[0])
        .filter(Boolean)
        .join('')
        .toUpperCase()
        .substring(0, 2)
    };
  }

  /**
   * Transforms backend CreditLedgerResponse -> CreditLedgerViewModel
   */
  static mapLedger(cl) {
    if (!cl) return null;
    const amount = Number(cl.amount || 0);
    const entryType = cl.entryType || (amount >= 0 ? 'CREDIT' : 'DEBIT');
    const description = cl.description || 'Time Credit Transaction';

    return {
      id: Number(cl.id),
      amount: amount,
      hours: amount, // alias
      entryType: entryType,
      type: entryType, // alias
      description: description,
      createdAt: cl.createdAt,
      memberId: Number(cl.memberId),
      memberName: cl.memberName || 'Member',
      sessionRequestId: cl.sessionRequestId != null ? Number(cl.sessionRequestId) : null,
      sessionId: cl.sessionRequestId != null ? Number(cl.sessionRequestId) : null // alias
    };
  }
}

window.DataMappers = DataMappers;
