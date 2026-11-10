// ============================================================
// MEMBER   : IT26101136 - [Name]
// MODULE   : Shared UI
// FILE     : site.js
// WHAT IT DOES (simple words): Adds optional interactions while keeping normal links and forms working.
// REAL-LIFE EXAMPLE: Shared signs help everyone find their way around the school.
// OOP USED : Not applicable (shared presentation)
// CRUD     : Displays existing data; does not save records
// ============================================================
// Sets up the existing navigation and forms once the page is ready.
document.addEventListener('DOMContentLoaded', () => {
  // Opens the browser's normal print dialog; report CSS prepares a white paper layout.
  document.querySelectorAll('[data-print]').forEach((button) => {
    button.addEventListener('click', printReport);
  });

  const nav = document.querySelector('#primary-navigation');
  prepareNavigation(nav);
  prepareDropdownMotion();
  prepareDialogMotion();
  prepareCinematicScroll();
  prepareSkipLink();

  if (nav) {
    const path = window.location.pathname.replace(/;jsessionid=[^/]+/g, '').replace(/\/$/, '') || '/';
    document.querySelectorAll('#primary-navigation a[href], .ds-dashboard-sidebar a[href], .ds-dashboard-drawer a[href]').forEach((link) => {
      const target = new URL(link.href, window.location.origin).pathname.replace(/;jsessionid=[^/]+/g, '').replace(/\/$/, '') || '/';
      const matches = !new URL(link.href, window.location.origin).hash && (path === target || (target !== '/' && path.startsWith(`${target}/`)));
      if (matches) link.setAttribute('aria-current', 'page');
      else link.removeAttribute('aria-current');
    });
    document.querySelectorAll('.ds-dashboard-sidebar, .ds-dashboard-drawer').forEach((menu) => {
      let current = null;
      let longestPath = 0;
      menu.querySelectorAll('a[href]').forEach((link) => {
        const url = new URL(link.href, window.location.origin);
        const target = url.pathname.replace(/;jsessionid=[^/]+/g, '').replace(/\/$/, '') || '/';
        link.removeAttribute('aria-current');
        if (!url.hash && (path === target || path.startsWith(`${target}/`)) && target.length > longestPath) {
          current = link;
          longestPath = target.length;
        }
      });
      if (current) current.setAttribute('aria-current', 'page');
    });
  }

  const packageCards = [...document.querySelectorAll('[data-package-card]')];
  const packageFilters = [...document.querySelectorAll('[data-package-filter]')];
  if (packageCards.length && packageFilters.length) {
    const empty = document.querySelector('.ds-filter-empty');
    packageFilters.forEach((button) => button.addEventListener('click', () => {
      const selected = button.dataset.packageFilter;
      let visibleCount = 0;
      packageFilters.forEach((filter) => {
        const active = filter === button;
        filter.classList.toggle('is-active', active);
        filter.setAttribute('aria-pressed', String(active));
      });
      packageCards.forEach((card) => {
        const show = selected === 'all' || card.dataset.packageType.toUpperCase() === selected;
        card.hidden = !show;
        if (show) visibleCount += 1;
      });
      if (empty) empty.hidden = visibleCount > 0;
    }));
  }

  // Native dialog supplies keyboard focus containment and Escape handling.
  const confirmDialog = document.querySelector('#ds-confirm-dialog');
  const confirmMessage = document.querySelector('#ds-confirm-message');
  const confirmReasonWrap = document.querySelector('#ds-confirm-reason-wrap');
  const confirmReason = document.querySelector('#ds-confirm-reason');
  const confirmContinue = document.querySelector('#ds-confirm-continue');
  let pendingForm = null;
  if (confirmDialog) {
    // Open the reason dialog from the button click before native `required`
    // validation runs; the existing named field is populated before resubmit.
    document.querySelectorAll('form[data-confirm-reason="true"]').forEach((form) => {
      form.querySelectorAll('button[type="submit"]').forEach((button) => {
        button.addEventListener('click', (event) => {
          if (form.dataset.confirmed === 'true') return;
          event.preventDefault();
          pendingForm = form;
          if (confirmMessage) confirmMessage.textContent = form.dataset.confirm;
          if (confirmContinue) confirmContinue.textContent = form.querySelector('button[type="submit"]')?.textContent.trim() || 'Confirm';
          if (confirmReasonWrap) confirmReasonWrap.hidden = false;
          if (confirmReason) confirmReason.value = '';
          if (confirmContinue) confirmContinue.disabled = true;
          confirmDialog.returnValue = '';
          confirmDialog.showModal();
        });
      });
    });

    document.querySelectorAll('form[data-confirm]').forEach((form) => {
      form.addEventListener('submit', (event) => {
        if (form.dataset.confirmed === 'true') {
          form.dataset.confirmed = 'false';
          return;
        }
        event.preventDefault();
        pendingForm = form;
        if (confirmMessage) confirmMessage.textContent = form.dataset.confirm;
          if (confirmContinue) confirmContinue.textContent = form.querySelector('button[type="submit"]')?.textContent.trim() || 'Confirm';
        const reasonRequired = form.dataset.confirmReason === 'true';
        if (confirmReasonWrap) confirmReasonWrap.hidden = !reasonRequired;
        if (confirmReason) confirmReason.value = '';
        if (confirmContinue) confirmContinue.disabled = reasonRequired;
        confirmDialog.returnValue = '';
        confirmDialog.showModal();
      });
    });
    confirmDialog.addEventListener('close', () => {
      const form = pendingForm;
      pendingForm = null;
      if (confirmDialog.returnValue !== 'confirm' || !form) return;
      if (form.dataset.confirmReason === 'true') {
        const existingReason = form.querySelector('[name="reason"]');
        if (existingReason && confirmReason) existingReason.value = confirmReason.value.trim();
      }
      form.dataset.confirmed = 'true';
      if (form.requestSubmit) form.requestSubmit();
      else form.submit();
    });
    confirmReason?.addEventListener('input', () => {
      if (confirmContinue) confirmContinue.disabled = confirmReason.value.trim().length === 0;
    });
  }

  // Wait until submission is accepted and named button values have been collected.
  document.querySelectorAll('form').forEach((form) => {
    form.addEventListener('submit', (event) => {
      if (event.defaultPrevented || form.dataset.allowRepeat === 'true') return;
      if (form.dataset.submitting === 'true') {
        event.preventDefault();
        return;
      }
      form.dataset.submitting = 'true';
      window.setTimeout(() => showSubmitLoading(form, event), 0);
    });
  });

  document.querySelectorAll('[data-password-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const field = document.getElementById(button.dataset.passwordToggle);
      if (!field) return;
      const reveal = field.type === 'password';
      field.type = reveal ? 'text' : 'password';
      button.setAttribute('aria-pressed', String(reveal));
      // Tell screen-reader users whether this button will show or hide their password.
      const passwordLabel = button.getAttribute('aria-label') || 'Show password';
      button.setAttribute('aria-label', passwordLabel.replace(/^(Show|Hide)/, reveal ? 'Hide' : 'Show'));
      button.textContent = reveal ? 'Hide' : 'Show';
    });
  });

  document.querySelectorAll('[data-password-strength]').forEach((field) => {
    const hint = document.getElementById(field.dataset.passwordStrength);
    if (!hint) return;
    field.addEventListener('input', () => {
      const value = field.value;
      const okay = value.length >= 8 && /[A-Za-z]/.test(value) && /\d/.test(value);
      hint.textContent = okay ? 'Your password includes a letter and a number.' : 'Use at least 8 characters, including a letter and a number.';
      hint.classList.toggle('text-emerald-300', okay);
      hint.classList.toggle('text-slate-300', !okay);
    });
  });

  // Success messages leave quietly; errors wait for the user to dismiss them.
  document.querySelectorAll('.ds-toast:not(.ds-toast-error)').forEach((toast) => {
    window.setTimeout(() => dismissToast(toast), 6500);
  });
  document.querySelectorAll('main > p[role="status"]').forEach((statusAlert) => {
    window.setTimeout(() => {
      statusAlert.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
      statusAlert.style.opacity = '0';
      statusAlert.style.transform = 'translateY(-4px)';
      window.setTimeout(() => statusAlert.remove(), 400);
    }, 6500);
  });
  document.querySelectorAll('.ds-toast-dismiss').forEach((button) => {
    button.addEventListener('click', () => dismissToast(button.closest('.ds-toast')));
  });
});

// Fades a message away before removing it; reduced motion removes it immediately.
function dismissToast(toast) {
  if (!toast) return;
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    toast.remove();
    return;
  }
  toast.inert = true;
  toast.setAttribute('aria-hidden', 'true');
  toast.classList.add('ds-toast-leaving');
  window.setTimeout(() => toast.remove(), motionCloseTime());
}

// Restores buttons when Back returns to a page cached during form submission.
function restoreSubmitButtons() {
  document.querySelectorAll('form[data-submitting="true"]').forEach((form) => {
    delete form.dataset.submitting;
    const button = form.querySelector('[data-original-text]');
    if (!button) return;
    button.disabled = false;
    button.removeAttribute('aria-busy');
    if (button.tagName === 'BUTTON') button.textContent = button.dataset.originalText;
  });
}
window.addEventListener('pageshow', restoreSubmitButtons);

// Shows the chosen panel; all panels remain readable when JavaScript is off.
function selectTab(group, selected, updateLocation = false) {
  const tabs = group.querySelectorAll('[data-tab-target]');
  tabs.forEach((tab) => {
    const active = tab === selected;
    tab.setAttribute('aria-selected', String(active));
    tab.tabIndex = active ? 0 : -1;
    const panel = document.getElementById(tab.dataset.tabTarget);
    if (panel) panel.hidden = !active;
  });
  // Keep anchor links and the visible panel in agreement without reloading the page.
  if (updateLocation) window.history.replaceState(window.history.state, '', '#' + selected.dataset.tabTarget);
}

// Adds keyboard tabs while retaining the same native anchor links as the fallback.
function prepareTabs(group) {
  const tabs = [...group.querySelectorAll('[data-tab-target]')];
  if (!tabs.length) return;
  group.setAttribute('role', 'tablist');
  tabs.forEach((tab, index) => prepareTab(group, tabs, tab, index));
  selectTab(group, tabs[0]);
  selectTabFromLocation(group, tabs);
  window.addEventListener('hashchange', () => selectTabFromLocation(group, tabs));
}

// Connects one tab to its panel and makes the panel reachable with the Tab key.
function prepareTab(group, tabs, tab, index) {
  const panel = document.getElementById(tab.dataset.tabTarget);
  if (!panel) return;
  tab.setAttribute('role', 'tab');
  tab.id = tab.id || panel.id + '-tab';
  tab.setAttribute('aria-controls', panel.id);
  panel.setAttribute('role', 'tabpanel');
  panel.setAttribute('aria-labelledby', tab.id);
  panel.tabIndex = 0;
  tab.addEventListener('click', (event) => {
    event.preventDefault();
    selectTab(group, tab, true);
  });
  tab.addEventListener('keydown', (event) => moveTab(event, group, tabs, index));
}

// Opens a panel named in the address or sidebar instead of leaving its anchor hidden.
function selectTabFromLocation(group, tabs) {
  for (const tab of tabs) {
    if ('#' + tab.dataset.tabTarget !== window.location.hash) continue;
    selectTab(group, tab);
    document.getElementById(tab.dataset.tabTarget).scrollIntoView({ block: 'start' });
    return;
  }
}

// The skip link moves keyboard focus as well as scrolling to the page's main content.
function prepareSkipLink() {
  const main = document.querySelector('#main-content');
  const skipLink = document.querySelector('.skip-link');
  if (!main || !skipLink) return;
  main.tabIndex = -1;
  skipLink.addEventListener('click', () => main.focus({ preventScroll: true }));
}

// Arrow keys move between tabs, while Home and End jump to either end.
function moveTab(event, group, tabs, index) {
  let next = index;
  if (event.key === 'ArrowRight') next = (index + 1) % tabs.length;
  else if (event.key === 'ArrowLeft') next = (index + tabs.length - 1) % tabs.length;
  else if (event.key === 'Home') next = 0;
  else if (event.key === 'End') next = tabs.length - 1;
  else return;
  event.preventDefault();
  selectTab(group, tabs[next], true);
  tabs[next].focus();
}

// Counts toward a real displayed number; it never invents dashboard statistics.
function animateCounter(element) {
  const target = Number(element.dataset.counter);
  if (!Number.isFinite(target)) return;
  const started = performance.now();
  // Each frame moves a little closer to the number already supplied by the server.
  function draw(now) {
    const progress = Math.min((now - started) / 900, 1);
    element.textContent = Math.round(target * progress).toLocaleString();
    if (progress < 1) window.requestAnimationFrame(draw);
  }
  window.requestAnimationFrame(draw);
}

// Reveals each card once; unsupported browsers simply keep everything visible.
function prepareMotion() {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;
  if (!('IntersectionObserver' in window)) return;
  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('ds-visible');
      if (entry.target.hasAttribute('data-counter')) animateCounter(entry.target);
      observer.unobserve(entry.target);
    });
  }, { threshold: 0.08 });
  document.querySelectorAll('.ds-feature-card, [data-reveal], [data-counter], [data-progress], [data-chart]').forEach((element, index) => {
    element.style.setProperty('--ds-stagger', (index % 4) * 70 + 'ms');
    element.classList.add('ds-motion-ready');
    observer.observe(element);
  });
}

// Starts optional shared components after their fallback HTML is ready.
document.addEventListener('DOMContentLoaded', () => {
  document.documentElement.classList.add('ds-js');
  document.querySelectorAll('[data-tabs]').forEach(prepareTabs);
  prepareMotion();
});

// Locks the button after normal form submission, leaving cancelled dialogs alone.
function showSubmitLoading(form, event) {
  if (event.defaultPrevented || form.dataset.allowRepeat === 'true') return;
  const button = event.submitter || form.querySelector('button[type="submit"], input[type="submit"]');
  if (!button || button.disabled) return;
  button.dataset.originalText = button.textContent || button.value || '';
  form.dataset.submitting = 'true';
  button.disabled = true;
  button.setAttribute('aria-busy', 'true');
  if (button.tagName === 'BUTTON') button.textContent = 'Please wait\u2026';
}

// Uses the browser printer so the same report can be saved as a PDF or printed.
function printReport() {
  window.print();
}

// Keeps the mobile overlay optional; native links and details remain the fallback.
function prepareNavigation(nav) {
  if (!nav) return;
  const toggle = document.querySelector('.ds-nav-toggle');
  toggle.addEventListener('click', () => setNavigationOpen(nav, true));
  nav.querySelector('.ds-nav-close').addEventListener('click', () => setNavigationOpen(nav, false));
  nav.addEventListener('keydown', (event) => handleNavigationKey(event, nav));
  nav.addEventListener('click', (event) => {
    if (event.target.closest('a[href]') && !event.target.closest('.ds-package-trigger') && nav.classList.contains('is-open')) setNavigationOpen(nav, false);
  });
  window.matchMedia('(max-width: 1023px)').addEventListener('change', () => setNavigationOpen(nav, false));
  document.addEventListener('click', (event) => {
    nav.querySelectorAll('.ds-nav-group[open]').forEach((group) => {
      if (!group.contains(event.target)) closeAnimatedDropdown(group);
    });
  });
}

// Blocks background focus while the mobile menu is open, then restores the menu button.
function setNavigationOpen(nav, open) {
  const wasOpen = nav.classList.contains('is-open');
  const toggle = document.querySelector('.ds-nav-toggle');
  nav.classList.toggle('is-open', open);
  document.documentElement.classList.toggle('ds-menu-open', open);
  toggle.setAttribute('aria-expanded', String(open));
  document.querySelectorAll('main, .ds-footer').forEach((section) => { section.inert = open; });
  if (open) {
    nav.setAttribute('role', 'dialog');
    nav.setAttribute('aria-modal', 'true');
    nav.querySelector('.ds-nav-close').focus();
  } else {
    nav.removeAttribute('role');
    nav.removeAttribute('aria-modal');
    if (wasOpen) toggle.focus();
  }
}

// Escape closes the overlay; Tab stays among its currently visible links and controls.
function handleNavigationKey(event, nav) {
  if (!nav.classList.contains('is-open')) return;
  if (event.key === 'Escape') {
    event.preventDefault();
    setNavigationOpen(nav, false);
    return;
  }
  if (event.key !== 'Tab') return;
  const controls = visibleNavigationControls(nav);
  const first = controls[0];
  const last = controls[controls.length - 1];
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
}

// One scheduled frame updates the header and at most two feature backgrounds during scrolling.
function prepareCinematicScroll() {
  const header = document.querySelector('.ds-header');
  const features = [...document.querySelectorAll('[data-parallax]')].slice(0, 2);
  let scheduled = false;
  const refresh = () => {
    if (scheduled) return;
    scheduled = true;
    window.requestAnimationFrame(() => {
      updateCinematicScroll(header, features);
      scheduled = false;
    });
  };
  window.addEventListener('scroll', refresh, { passive: true });
  window.addEventListener('resize', refresh);
  window.matchMedia('(prefers-reduced-motion: reduce)').addEventListener('change', refresh);
  refresh();
}

// A forty-pixel scroll makes navigation solid; reduced motion keeps feature art still.
function updateCinematicScroll(header, features) {
  if (header) header.classList.toggle('is-scrolled', window.scrollY > 40);
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  features.forEach((section) => {
    const bounds = section.getBoundingClientRect();
    let offset = 0;
    if (!reduced) offset = Math.max(-36, Math.min(36, (window.innerHeight / 2 - bounds.top - bounds.height / 2) * .06));
    section.style.setProperty('--ds-parallax', offset + 'px');
  });
  if (reduced) document.querySelectorAll('.ds-motion-ready').forEach((item) => item.classList.add('ds-visible'));
}

// Closed native details may still report rectangles, so exclude their hidden links explicitly.
function visibleNavigationControls(nav) {
  return [...nav.querySelectorAll('a[href], button, summary')].filter((item) => {
    const closedGroup = item.closest('details:not([open])');
    return item.getClientRects().length && (!closedGroup || item.tagName === 'SUMMARY');
  });
}

// Reads the existing public page so the home page never exposes moderation or private review data.
async function prepareHomeReviews() {
  const section = document.querySelector('[data-home-reviews]');
  if (!section) return;
  try {
    const response = await fetch(section.dataset.reviewsUrl, { credentials: 'same-origin' });
    if (!response.ok) throw new Error('Public reviews are unavailable.');
    const page = new DOMParser().parseFromString(await response.text(), 'text/html');
    const cards = page.querySelectorAll('.ds-review-card');
    showHomeReviews(section, cards);
  } catch (error) {
    // The normal public link remains usable if the request cannot finish.
    section.querySelector('[data-review-fallback]').textContent = 'Visit our reviews page to read approved learner feedback.';
  }
}

// Copies text only, so saved review comments cannot introduce HTML into this page.
function makeHomeReview(card) {
  const slide = document.createElement('article');
  const quote = document.createElement('blockquote');
  quote.textContent = card.querySelector('blockquote')?.textContent.trim() || '';
  const attribution = document.createElement('p');
  attribution.className = 'ds-review-attribution';
  const rating = card.querySelector('.ds-star-display')?.getAttribute('aria-label') || '';
  attribution.textContent = (card.querySelector('p')?.textContent.trim() || 'DriveSpark learner') + ' · ' + rating;
  slide.append(quote, attribution);
  return slide;
}

// Shows one approved review at a time; reader-controlled buttons avoid automatic motion.
function showHomeReviews(section, cards) {
  if (!cards.length) {
    section.querySelector('[data-review-fallback]').textContent = 'Approved learner feedback will appear here when available.';
    return;
  }
  const container = section.querySelector('[data-review-slides]');
  for (const card of cards) container.append(makeHomeReview(card));
  section.querySelector('[data-review-fallback]').hidden = true;
  section.querySelector('.ds-review-controls').hidden = cards.length < 2;
  section.querySelector('[data-review-previous]').addEventListener('click', () => moveHomeReview(section, -1));
  section.querySelector('[data-review-next]').addEventListener('click', () => moveHomeReview(section, 1));
  moveHomeReview(section, 0);
}

// Wraps at either end and announces the selected review without moving keyboard focus.
function moveHomeReview(section, direction) {
  const slides = section.querySelector('[data-review-slides]').children;
  const previous = Number(section.dataset.reviewIndex || 0);
  const index = (previous + direction + slides.length) % slides.length;
  section.dataset.reviewIndex = String(index);
  for (let position = 0; position < slides.length; position += 1) slides[position].hidden = position !== index;
  section.querySelector('[data-review-position]').textContent = (index + 1) + ' / ' + slides.length;
}

// Opens the chosen lesson type with the catalog's existing filters; no server route changes.
function prepareLessonChoice() {
  const selectedType = new URLSearchParams(window.location.search).get('type');
  if (!selectedType) return;
  for (const button of document.querySelectorAll('[data-package-filter]')) {
    if (button.dataset.packageFilter === selectedType.toUpperCase()) button.click();
  }
}

// Starts the home-only review enhancement and honours lesson links after existing filters are ready.
document.addEventListener('DOMContentLoaded', () => {
  prepareHomeReviews();
  prepareLessonChoice();
});

// Enhances a normal link; without scripts every visitor can still open the packages page.
(function preparePackageMenu() {
  const trigger = document.querySelector('.ds-package-trigger');
  const panel = document.querySelector('#package-panel');
  if (!trigger || !panel) return;
  const group = trigger.closest('.ds-package-menu');
  prepareAnimatedPanel(panel);
  const desktopMouse = window.matchMedia('(min-width: 1024px) and (hover: hover) and (pointer: fine)');
  let restoring = false;
  let closeTimer;
  let previousWidth = window.innerWidth;
  // Keeps expanded state and visible content together without loading any extra data.
  function change(open, restore) {
    clearTimeout(closeTimer);
    setAnimatedPanel(panel, open);
    trigger.setAttribute('aria-expanded', String(open));
    if (open) document.querySelectorAll('.ds-nav-group[open]').forEach(item => closeAnimatedDropdown(item));
    if (restore) { restoring = true; trigger.focus(); restoring = false; }
  }
  trigger.addEventListener('click', event => { event.preventDefault(); change(true, false); });
  trigger.addEventListener('keydown', event => {
    if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); change(true, false); }
  });
  trigger.addEventListener('focus', () => { if (desktopMouse.matches && !restoring) change(true, false); });
  group.addEventListener('mouseenter', () => { if (desktopMouse.matches && !restoring) change(true, false); });
  // Give the pointer time to cross the small space between the link and its panel.
  function scheduleClose() {
    if (!desktopMouse.matches) return;
    clearTimeout(closeTimer);
    closeTimer = setTimeout(() => {
      if (!group.matches(':hover') && !panel.matches(':focus-within') && !trigger.matches(':focus-visible')) change(false, false);
    }, 220);
  }
  group.addEventListener('mouseleave', scheduleClose);
  panel.addEventListener('mouseenter', () => clearTimeout(closeTimer));
  panel.addEventListener('mouseleave', scheduleClose);
  // A fading panel cannot receive clicks, but moving back into its visible area cancels closing.
  document.addEventListener('pointermove', event => {
    if (!desktopMouse.matches || panel.hidden || trigger.getAttribute('aria-expanded') === 'true') return;
    const bounds = panel.getBoundingClientRect();
    if (event.clientX >= bounds.left && event.clientX <= bounds.right && event.clientY >= bounds.top && event.clientY <= bounds.bottom) change(true, false);
  });
  group.addEventListener('focusout', event => { if (!group.contains(event.relatedTarget)) change(false, false); });
  document.addEventListener('click', event => { if (!group.contains(event.target)) change(false, false); });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && trigger.getAttribute('aria-expanded') === 'true') { event.preventDefault(); event.stopImmediatePropagation(); change(false, true); }
  }, true);
  document.querySelectorAll('.ds-nav-group').forEach(item => item.addEventListener('toggle', () => { if (item.open) change(false, false); }));
  document.querySelectorAll('.ds-nav-toggle, .ds-nav-close').forEach(button => button.addEventListener('click', () => change(false, false)));
  // A phone's browser bar changes height while scrolling; only width changes alter this layout.
  window.addEventListener('resize', () => {
    if (window.innerWidth !== previousWidth) change(false, false);
    previousWidth = window.innerWidth;
  });
})();

// Reveal the backdrop if a photo disappears after the server checked its existence.
document.addEventListener('error', function hideUnavailablePhoto(event) {
  if (event.target instanceof HTMLImageElement && event.target.hasAttribute('data-local-photo')) {
    const shade = event.target.nextElementSibling;
    if (shade && shade.classList.contains('ds-local-photo-shade')) shade.remove();
    event.target.remove();
  }
}, true);

// Keep closed content out of the keyboard order and accessibility tree during its fade.
function prepareAnimatedPanel(panel) {
  panel.classList.add('ds-animated-panel');
  panel.hidden = true;
  panel.inert = true;
  panel.setAttribute('aria-hidden', 'true');
}

// CSS reverses an interrupted transition from its current position, avoiding flicker.
function setAnimatedPanel(panel, open) {
  clearTimeout(panel.hideTimer);
  if (open && panel.hidden) {
    panel.hidden = false;
    panel.getBoundingClientRect();
  }
  panel.inert = !open;
  panel.setAttribute('aria-hidden', String(!open));
  panel.classList.toggle('is-visible', open);
  if (!open) panel.hideTimer = setTimeout(() => { panel.hidden = true; }, motionCloseTime());
}

// Keep native details open until the submenu has finished fading away.
function prepareDropdownMotion() {
  document.querySelectorAll('.ds-nav-group').forEach(group => {
    const summary = group.querySelector('summary');
    const panel = group.querySelector('.ds-nav-submenu');
    if (!summary || !panel) return;
    prepareAnimatedPanel(panel);
    summary.setAttribute('aria-expanded', 'false');
    summary.addEventListener('click', event => {
      event.preventDefault();
      clearTimeout(group.closeTimer);
      const open = summary.getAttribute('aria-expanded') !== 'true';
      if (open) group.open = true;
      summary.setAttribute('aria-expanded', String(open));
      setAnimatedPanel(panel, open);
      if (!open) group.closeTimer = setTimeout(() => { group.open = false; }, motionCloseTime());
    });
    group.addEventListener('toggle', () => {
      if (!group.open) { summary.setAttribute('aria-expanded', 'false'); setAnimatedPanel(panel, false); }
    });
    group.addEventListener('keydown', event => {
      if (event.key === 'Escape') { event.preventDefault(); summary.setAttribute('aria-expanded', 'false'); setAnimatedPanel(panel, false); group.closeTimer = setTimeout(() => { group.open = false; summary.focus(); }, motionCloseTime()); }
    });
  });
}

// Delay native dismissal so the modal can fade out while retaining its focus trap.
function prepareDialogMotion() {
  const dialog = document.querySelector('#ds-confirm-dialog');
  if (!dialog) return;
  let closing = false;
  function dismiss(value) {
    if (closing) return;
    closing = true;
    dialog.classList.add('is-leaving');
    setTimeout(() => { dialog.close(value); dialog.classList.remove('is-leaving'); closing = false; }, motionCloseTime());
  }
  dialog.addEventListener('cancel', event => { event.preventDefault(); dismiss('cancel'); });
  dialog.querySelector('form[method="dialog"]').addEventListener('submit', event => {
    event.preventDefault();
    dismiss(event.submitter?.value || 'cancel');
  });
}

// Close native dropdowns only after their shared fade, including outside clicks.
function closeAnimatedDropdown(group) {
  const panel = group.querySelector('.ds-nav-submenu');
  if (!panel) { group.open = false; return; }
  clearTimeout(group.closeTimer);
  group.querySelector('summary').setAttribute('aria-expanded', 'false');
  setAnimatedPanel(panel, false);
  group.closeTimer = setTimeout(() => { group.open = false; }, motionCloseTime());
}

// The CSS token is in milliseconds, so changing it also changes when faded content is hidden.
function motionCloseTime() {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return 20;
  const token = getComputedStyle(document.documentElement).getPropertyValue('--ds-close-time');
  const milliseconds = parseFloat(token);
  if (Number.isNaN(milliseconds)) return 180;
  return milliseconds;
}