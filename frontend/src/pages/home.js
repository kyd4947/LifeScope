import { el } from '../utils/dom.js';
import { ROUTES } from '../constants/index.js';

// 히어로 배경 이미지
// Wikimedia Commons · CC0 1.0 — 서울 스카이라인 2018 (mauveine.kim)
const IMAGES = {
  hero: {
    src: '/images/seoul-skyline.jpg',
    alt: '서울 야경 스카이라인',
  },
};

// 기능 카드 : 12열 그리드에서 4 + 4 + 4 로 나눠 한 행에 동일 크기로 배치
const FEATURES = [
  {
    tape: 'MULTI CITY',
    stamp: '2~5',
    title: '여러 도시를 한 번에',
    text: '기준 도시를 정해 물가·임금·주거비를 나란히 놓고, CPI 비율로 월급까지 환산합니다.',
    cta: { label: '지역 비교', href: ROUTES.COMPARE },
  },
  {
    tape: 'CALCULATOR',
    stamp: '3종',
    title: '손에 남는 돈까지',
    text: '4대보험과 소득세를 뺀 실수령액, 전세 차이를 반영한 실질 구매력까지 계산합니다.',
    cta: { label: '계산기', href: ROUTES.CALCULATOR },
  },
  {
    tape: 'REGIONS',
    stamp: '17',
    title: '시 단위 데이터',
    text: '통계청·고용노동부·국토교통부 데이터를 전국 17개 시/도로 모았습니다.',
    cta: { label: '지역 정보', href: ROUTES.REGIONS },
  },
];

export function renderHome(root) {
  root.replaceChildren(
    // ---- Hero : 사진 배경 + 오버레이 ----
    el('section', { class: 'section', style: 'padding-block: 0; position: relative; overflow: hidden' }, [
      photo(IMAGES.hero, 'photo-hero'),
      el('div', { class: 'container', style: 'position: relative; z-index: 2; padding-block: var(--section-gap)' }, [
        el('div', { class: 'grid-asym' }, [
          el('div', { class: 'col-7 stack entry' }, [
            el('span', { class: 'duct-tape', text: '전국 시 단위 생활비 분석' }),
            el('h1', { class: 'hero-title' }, [
              document.createTextNode('월급은 같아도,'),
            ]),
            el('h1', { class: 'hero-title accent' }, [
              el('span', { class: 'misregister', 'data-text': '사는 비용은 다르다', text: '사는 비용은 다르다' }),
            ]),
            el('p', { style: 'max-width: 56ch; margin-top: 0.5rem' }, [
              document.createTextNode(
                '물가, 임금, 주거비를 같은 기준에 놓고 여러 도시를 나란히 놓습니다. CPI 비율로 환산한 실질 구매력이 답입니다.',
              ),
            ]),
            el('div', { class: 'row', style: 'margin-top: 1.5rem' }, [
              el('a', { class: 'btn btn-cta', href: ROUTES.COMPARE, text: '지역 비교 시작' }),
              el('a', { class: 'btn btn-ghost', href: ROUTES.CALCULATOR, text: '계산기 열기' }),
            ]),
          ]),
          el('div', { class: 'col-5 entry', style: 'align-self: center' }, [
            el('div', { class: 'surface-paper stack' }, [
              el('span', { class: 'stamp', text: '왜 비교가 필요한가' }),
              el('p', {
                text: '같은 연봉 4,000만 원을 서울과 부산에서 받더라도, 살 수 있는 생활은 다릅니다. CPI 비율로 환산하면 그 차이가 바로 드러납니다.',
              }),
              el('hr', { class: 'divider' }),
              el('div', { class: 'row', style: 'justify-content: space-between' }, [
                el('span', { class: 'label', text: '데이터 갱신' }),
                el('span', { class: 'mono-value', text: '월 1회' }),
              ]),
              el('div', { class: 'row', style: 'justify-content: space-between' }, [
                el('span', { class: 'label', text: '커버 지역' }),
                el('span', { class: 'mono-value', text: '17 시/도' }),
              ]),
            ]),
          ]),
        ]),
      ]),
    ]),

    // ---- Feature : 1행 3개 압축 배치 ----
    el('section', { class: 'section', style: 'padding-block: var(--space-8)' }, [
      el('div', { class: 'container stack' }, [
        sectionLabel('기능'),
        el('div', { class: 'grid-asym stagger feature-row' },
          FEATURES.map((feature) =>
            el('article', { class: 'card card-compact col-4 stack' }, [
              el('div', { class: 'row', style: 'justify-content: space-between; align-items: center' }, [
                el('span', { class: 'duct-tape', text: feature.tape }),
                el('span', { class: 'stamp', text: feature.stamp }),
              ]),
              el('h2', { class: 'card-title', text: feature.title }),
              el('p', { text: feature.text }),
              el('a', {
                class: 'btn btn-ghost',
                href: feature.cta.href,
                text: feature.cta.label,
                style: 'align-self: flex-start',
              }),
            ]),
          ),
        ),
      ]),
    ]),

    // ---- CTA ----
    el('section', { class: 'section', style: 'padding-block: var(--space-8)' }, [
      el('div', { class: 'container' }, [
        el('div', { class: 'surface-dark torn-edge stack', style: 'align-items: flex-start; gap: 1.5rem' }, [
          el('span', { class: 'duct-tape', text: 'START HERE' }),
          el('h2', { class: 'card-title', style: 'font-size: 1.75rem', text: '두 도시를 골라 비교해 보세요' }),
          el('p', { text: '기준 도시를 정하면 나머지는 물가와 임금, 전세 비율이 자동으로 계산됩니다.' }),
          el('a', { class: 'btn btn-cta', href: ROUTES.COMPARE, text: '지역 비교 시작' }),
        ]),
      ]),
    ]),
  );
}

function photo(image, modifier = '') {
  return el('figure', { class: `photo ${modifier}`.trim() }, [
    el('img', { src: image.src, alt: image.alt, loading: 'lazy', decoding: 'async' }),
    // 그레인 레이어를 span 으로 분리 (::after 는 비네트 전용)
    el('span', { class: 'photo-grain', 'aria-hidden': 'true' }),
  ]);
}

function sectionLabel(text) {
  return el('div', { class: 'row', style: 'gap: 1rem; align-items: center' }, [
    el('hr', { class: 'divider', style: 'flex: 1' }),
    el('span', { class: 'label', text }),
  ]);
}
