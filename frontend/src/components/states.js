import { el } from '../utils/dom.js';

// 스켈레톤 — 원형 스피너 대신 시머 효과 (design.md 규칙)
export function skeletonLines(count = 3) {
  return el(
    'div',
    { class: 'stack', 'aria-hidden': 'true' },
    Array.from({ length: count }, (_, i) =>
      el('div', {
        class: 'skeleton skeleton-line',
        style: `width: ${100 - i * 12}%`,
      }),
    ),
  );
}

export function skeletonBlock(height = '7rem') {
  return el('div', {
    class: 'skeleton skeleton-block',
    style: `height: ${height}`,
    'aria-hidden': 'true',
  });
}

export function skeletonCards(count = 3) {
  return el(
    'div',
    { class: 'grid-asym stagger' },
    Array.from({ length: count }, (_, i) =>
      el('div', { class: i === 0 ? 'card col-7' : 'card col-5' }, [
        skeletonLines(3),
      ]),
    ),
  );
}

export function loadingBlock(label = '불러오는 중') {
  return el('div', { class: 'stack', role: 'status' }, [
    el('span', { class: 'label', text: label }),
    skeletonBlock(),
  ]);
}

// 빈 상태 — 아이콘 + 설명 + 액션 (design.md 규칙, 이모지 금지)
export function emptyState({ title, text, actionLabel, onAction }) {
  return el('div', { class: 'empty' }, [
    iconLayers(),
    el('p', { class: 'empty-title', text: title }),
    text ? el('p', { class: 'empty-text', text }) : null,
    actionLabel && onAction
      ? el('button', {
          class: 'btn btn-ghost',
          type: 'button',
          text: actionLabel,
          onClick: onAction,
        })
      : null,
  ]);
}

// 데이터 미수집 상태 — KOSIS/MOLIT 키 발급 전 기본 화면
export function noDataState({ cityName, onRetry }) {
  return emptyState({
    title: '아직 수집된 데이터가 없습니다',
    text: cityName
      ? `${cityName}의 생활비 데이터가 아직 수집되지 않았습니다. 통계청·국토교통부 API 데이터를 동기화하면 표시됩니다.`
      : '통계청·국토교통부 API 데이터가 아직 수집되지 않았습니다. 데이터 동기화 후 표시됩니다.',
    actionLabel: onRetry ? '다시 시도' : null,
    onAction: onRetry,
  });
}

// 에러 상태
export function errorState({ message, onRetry }) {
  return emptyState({
    title: '요청을 처리하지 못했습니다',
    text: message,
    actionLabel: onRetry ? '다시 시도' : null,
    onAction: onRetry,
  });
}

function iconLayers() {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('class', 'empty-icon');
  svg.setAttribute('viewBox', '0 0 24 24');
  svg.setAttribute('fill', 'none');
  svg.setAttribute('stroke', 'currentColor');
  svg.setAttribute('stroke-width', '1.5');
  svg.innerHTML = `
    <path d="M12 3 3 7.5 12 12l9-4.5L12 3Z" />
    <path d="M3 12l9 4.5L21 12" />
    <path d="M3 16.5 12 21l9-4.5" />
  `;
  return svg;
}
