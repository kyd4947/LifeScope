import { el } from '../utils/dom.js';
import { ROUTES } from '../constants/index.js';

const FEATURES = [
  {
    size: 'col-7',
    tape: 'MULTI CITY',
    title: '2~5개 지역 나란히',
    text: '기준 도시를 정해 물가·임금·주거비를 한 화면에 붙여 놓습니다. CPI 비율로 월급 환산까지 계산합니다.',
    cta: { label: '지역 비교 열기', href: ROUTES.COMPARE },
  },
  {
    size: 'col-5',
    tape: 'CALCULATOR',
    title: '실수령액부터 실질 구매력까지',
    text: '연봉에서 4대보험과소득세를 뺀 실제到手 금액, 전세 차이를 반영한 구매력까지 계산합니다.',
    cta: { label: '계산기 열기', href: ROUTES.CALCULATOR },
  },
  {
    size: 'col-5',
    tape: 'REGIONS',
    title: '시 단위 데이터',
    text: '통계청 CPI, 고용노동부 임금, 국토교통부 실거래가. 전국 시/도 17곳을 기준으로 모았습니다.',
    cta: { label: '지역 정보 보기', href: ROUTES.REGIONS },
  },
];

const SOURCES = [
  { name: '통계청 KOSIS', desc: '소비자물가지수 · 평균임금', tone: 'accent' },
  { name: '국토교통부', desc: '아파트 매매 · 전세 실거래가', tone: 'decor' },
  { name: 'Oracle Cloud', desc: 'Always Free 인프라', tone: 'warn' },
];

export function renderHome(root) {
  root.replaceChildren(
    el('div', { class: 'on-dark' }, [
      // ---- Hero : 비대칭 배치 ----
      el('section', { class: 'section' }, [
        el('div', { class: 'container grid-asym' }, [
          el('div', { class: 'col-7 stack entry' }, [
            el('span', { class: 'duct-tape', text: '전국 시 단위 생활비 분석' }),
            el('h1', { class: 'hero-title' }, [
              el('span', { class: 'misregister', 'data-text': '어디로', text: '어디로' }),
              document.createTextNode(' 이직할지,'),
            ]),
            el('h1', { class: 'hero-title accent', text: '숫자로 비교하세요' }),
            el('p', { class: 'muted', style: 'max-width: 58ch; margin-top: 1rem' }, [
              document.createTextNode(
                '월급이 같아도 사는 비용은 다릅니다. 물가, 임금, 주거비를 같은 기준에 놓고 여러 도시를 나란히 놓습니다.',
              ),
            ]),
            el('div', { class: 'row', style: 'margin-top: 2rem' }, [
              el('a', {
                class: 'btn btn-cta',
                href: ROUTES.COMPARE,
                text: '지역 비교 시작',
              }),
              el('a', {
                class: 'btn btn-ghost',
                href: ROUTES.CALCULATOR,
                text: '계산기 열기',
              }),
            ]),
          ]),
          el('div', { class: 'col-5 entry', style: 'align-self: end' }, [
            el('div', { class: 'surface-paper torn-edge stack' }, [
              el('span', { class: 'label', text: '왜 비교가 필요한가' }),
              el('p', {
                text: '같은 연봉 4,000만 원을 서울과 부산에서 받더라도, 살 수 있는 생활은 다릅니다. CPI 비율로 환산한 실질 구매력이 답입니다.',
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

      // ---- Feature : 비대칭 지그재그 ----
      el('section', { class: 'section' }, [
        el('div', { class: 'container stack' }, [
          el('div', { class: 'row', style: 'gap: 1rem; align-items: center' }, [
            el('hr', { class: 'divider', style: 'flex: 1' }),
            el('span', { class: 'label', text: '기능' }),
          ]),
          el('div', { class: 'grid-asym stagger' },
            FEATURES.map((feature) =>
              el('article', { class: `card ${feature.size}` }, [
                el('span', { class: 'duct-tape', text: feature.tape }),
                el('h2', { class: 'card-title', style: 'margin-top: 1rem', text: feature.title }),
                el('p', { class: 'muted', text: feature.text }),
                el('a', {
                  class: 'btn btn-ghost',
                  href: feature.cta.href,
                  text: feature.cta.label,
                  style: 'margin-top: 1.25rem; align-self: flex-start',
                }),
              ]),
            ),
          ),
        ]),
      ]),

      // ---- Sources ----
      el('section', { class: 'section' }, [
        el('div', { class: 'container stack' }, [
          el('div', { class: 'row', style: 'gap: 1rem; align-items: center' }, [
            el('hr', { class: 'divider', style: 'flex: 1' }),
            el('span', { class: 'label', text: '출처' }),
          ]),
          el('div', { class: 'grid-asym' }, [
            el('div', { class: 'col-8 surface-dark' }, [
              el('div', { class: 'grid-asym', style: 'gap: 1.5rem' },
                SOURCES.map((source) =>
                  el('div', { class: 'col-4 stack' }, [
                    el('span', { class: `label ${source.tone}`, text: source.name }),
                    el('span', { text: source.desc }),
                  ]),
                ),
              ),
            ]),
            el('div', { class: 'col-4 surface-dark stack' }, [
              el('span', { class: 'label', text: '방법론' }),
              el('p', {
                text: '물가는 CPI 비율로 환산하고, 임금은 연 평균을 월 평균으로 나눕니다. 주거비는 전세 기준으로 비교합니다.',
              }),
              el('a', { class: 'btn btn-ghost', href: ROUTES.ABOUT, text: '자세히' }),
            ]),
          ]),
        ]),
      ]),
    ]),
  );
}
