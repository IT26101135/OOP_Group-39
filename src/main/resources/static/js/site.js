// Module owner: IT26101136
document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.ds-nav-toggle');
  const nav = document.querySelector('#primary-navigation');

  if (toggle && nav) {
    toggle.addEventListener('click', () => {
      const expanded = toggle.getAttribute('aria-expanded') === 'true';
      toggle.setAttribute('aria-expanded', String(!expanded));
      toggle.setAttribute('aria-label', expanded ? 'Open navigation menu' : 'Close navigation menu');
      nav.classList.toggle('is-open', !expanded);
    });
    nav.addEventListener('click', (event) => {
      if (event.target.closest('a, button[type="submit"]') && window.matchMedia('(max-width: 1023px)').matches) {
        nav.classList.remove('is-open');
        toggle.setAttribute('aria-expanded', 'false');
        toggle.setAttribute('aria-label', 'Open navigation menu');
      }
    });
  }

  if (nav) {
    const path = window.location.pathname.replace(/\/$/, '') || '/';
    document.querySelectorAll('#primary-navigation a[href], .ds-dashboard-sidebar a[href], .ds-dashboard-drawer a[href]').forEach((link) => {
      const target = new URL(link.href, window.location.origin).pathname.replace(/\/$/, '') || '/';
      const matches = path === target || (target !== '/' && path.startsWith(`${target}/`));
      if (matches) link.setAttribute('aria-current', 'page');
      else link.removeAttribute('aria-current');
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

  document.querySelectorAll('form').forEach((form) => {
    form.addEventListener('submit', () => {
      if (form.dataset.allowRepeat === 'true') return;
      if (form.dataset.confirm && form.dataset.confirmed !== 'true') return;
      const submit = form.querySelector('button[type="submit"], input[type="submit"]');
      if (!submit || submit.disabled) return;
      submit.dataset.originalText = submit.textContent || submit.value || '';
      submit.disabled = true;
      submit.setAttribute('aria-busy', 'true');
      if (submit.tagName === 'BUTTON') submit.textContent = 'Please wait\u2026';
    });
  });

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
          if (confirmReasonWrap) confirmReasonWrap.hidden = false;
          if (confirmReason) confirmReason.value = '';
          if (confirmContinue) confirmContinue.disabled = true;
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
        const reasonRequired = form.dataset.confirmReason === 'true';
        if (confirmReasonWrap) confirmReasonWrap.hidden = !reasonRequired;
        if (confirmReason) confirmReason.value = '';
        if (confirmContinue) confirmContinue.disabled = reasonRequired;
        confirmDialog.showModal();
      });
    });
    confirmDialog.addEventListener('close', () => {
      if (confirmDialog.returnValue !== 'confirm' || !pendingForm) return;
      const form = pendingForm;
      pendingForm = null;
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

  document.querySelectorAll('[data-password-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const field = document.getElementById(button.dataset.passwordToggle);
      if (!field) return;
      const reveal = field.type === 'password';
      field.type = reveal ? 'text' : 'password';
      button.setAttribute('aria-pressed', String(reveal));
      button.textContent = reveal ? 'Hide' : 'Show';
    });
  });

  document.querySelectorAll('[data-password-strength]').forEach((field) => {
    const hint = document.getElementById(field.dataset.passwordStrength);
    if (!hint) return;
    field.addEventListener('input', () => {
      const value = field.value;
      const okay = value.length >= 8 && /[A-Za-z]/.test(value) && /\d/.test(value);
      hint.textContent = okay ? 'Good password: length, letter and number included.' : 'Use at least 8 characters, including a letter and a number.';
      hint.classList.toggle('text-emerald-300', okay);
      hint.classList.toggle('text-slate-300', !okay);
    });
  });

  document.querySelectorAll('.ds-toast:not(.ds-toast-error)').forEach((toast) => {
    window.setTimeout(() => toast.remove(), 6500);
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
    button.addEventListener('click', () => button.closest('.ds-toast')?.remove());
  });
});
