import { el } from '../utils/dom.js';

// 통계 수치 블록
export function statBlock({ label, value, unit, variant = '' }) {
  return el('div', { class: 'stat' }, [
    el('span', { class: 'stat-label', text: label }),
    el('span', { class: `stat-value ${variant}`.trim() }, [
      document.createTextNode(String(value)),
      unit ? el('span', { class: 'stat-unit', text: ` ${unit}` }) : null,
    ]),
  ]);
}

// 페이지 헤더
export function pageHeader({ eyebrow, title, description, children }) {
  return el('header', { class: 'section entry' }, [
    el('div', { class: 'container stack' }, [
      eyebrow ? el('span', { class: 'label', text: eyebrow }) : null,
      el('h1', { class: 'hero-title', text: title }),
      description
        ? el('p', { class: 'muted', style: 'max-width: 62ch', text: description })
        : null,
      children || null,
    ]),
  ]);
}

// 섹션 구분선 + 라벨
export function sectionRule(label) {
  return el('div', { class: 'row', style: 'gap: 1rem; align-items: center' }, [
    el('hr', { class: 'divider', style: 'flex: 1' }),
    el('span', { class: 'label', text: label }),
  ]);
}

// 데이터 테이블
export function dataTable({ headers, rows }) {
  return el('div', { class: 'table-scroll' }, [
    el('table', { class: 'data-table' }, [
      el('thead', {}, [el('tr', {}, headers.map((h) => el('th', { text: h })))]),
      el(
        'tbody',
        {},
        rows.map((row) =>
          el(
            'tr',
            {},
            row.map((cell, i) =>
              i === 0
                ? el('th', { scope: 'row', style: 'font-weight: 500', text: String(cell) })
                : el('td', { text: String(cell) }),
            ),
          ),
        ),
      ),
    ]),
  ]);
}
