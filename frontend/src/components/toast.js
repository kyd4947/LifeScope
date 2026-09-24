import { el } from '../utils/dom.js';

// 전역 토스트 영역 (z-index 500)
let region = null;

function ensureRegion() {
  if (!region) {
    region = el('div', { class: 'toast-region', 'aria-live': 'polite' });
    document.body.appendChild(region);
  }
  return region;
}

export function toast(message, variant = 'info', duration = 4200) {
  const node = el('div', {
    class: `toast${variant === 'error' ? ' is-error' : ''}${variant === 'warn' ? ' is-warn' : ''}`,
    role: variant === 'error' ? 'alert' : 'status',
    text: message,
  });

  ensureRegion().appendChild(node);

  setTimeout(() => {
    node.style.opacity = '0';
    node.style.transform = 'translateY(8px)';
    node.style.transition = 'opacity 200ms, transform 200ms';
    setTimeout(() => node.remove(), 220);
  }, duration);

  return node;
}
