import { el } from '../utils/dom.js';
import { ROUTES } from '../constants/index.js';

const NAV_ITEMS = [
  { href: ROUTES.HOME, label: '홈' },
  { href: ROUTES.COMPARE, label: '지역 비교' },
  { href: ROUTES.CALCULATOR, label: '계산기' },
  { href: ROUTES.REGIONS, label: '지역 정보' },
  { href: ROUTES.ABOUT, label: '소개' },
];

export function createNav(currentHash) {
  const links = NAV_ITEMS.map((item) =>
    el('a', {
      class: `nav-link${isActive(item.href, currentHash) ? ' is-active' : ''}`,
      href: item.href,
      text: item.label,
    }),
  );

  const linksBox = el('nav', { class: 'nav-links', id: 'nav-links' }, links);

  const toggle = el('button', {
    class: 'nav-toggle',
    type: 'button',
    'aria-label': '메뉴 열기',
    'aria-expanded': 'false',
    text: 'MENU',
    onClick: () => {
      const open = linksBox.classList.toggle('is-open');
      toggle.setAttribute('aria-expanded', String(open));
    },
  });

  return el('header', { class: 'nav' }, [
    el('div', { class: 'container nav-inner' }, [
      el('a', { class: 'nav-brand', href: ROUTES.HOME }, [
        document.createTextNode('LifeScope'),
        el('span', { text: 'v0.1' }),
      ]),
      toggle,
      linksBox,
    ]),
  ]);
}

function isActive(href, currentHash) {
  if (href === ROUTES.HOME) return currentHash === ROUTES.HOME || !currentHash;
  return currentHash?.startsWith(href);
}

export function createFooter() {
  return el('footer', { class: 'section on-dark' }, [
    el('div', { class: 'container stack' }, [
      el('hr', { class: 'divider' }),
      el('div', { class: 'row', style: 'justify-content: space-between' }, [
        el('span', { class: 'label', text: 'LIFESCOPE / 2026' }),
        el('span', {
          class: 'label',
          text: '데이터: 통계청 KOSIS · 국토교통부',
        }),
      ]),
    ]),
  ]);
}
