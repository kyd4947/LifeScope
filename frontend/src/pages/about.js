import { el } from '../utils/dom.js';
import { pageHeader, dataTable } from '../components/ui.js';
import { triggerSync } from '../api/client.js';
import { toast } from '../components/toast.js';
import { formatNumber } from '../utils/format.js';

const SOURCES = [
  {
    name: '통계청 KOSIS',
    desc: '소비자물가지수(CPI) · 평균임금',
    detail: '물가는 2020년=100 기준 지수를 사용하며 월 단위로 수집합니다. 임금은 시/도 단위 연 평균이며 월 평균으로 환산해 비교합니다.',
    tone: 'accent',
  },
  {
    name: '국토교통부',
    desc: '아파트 매매 · 전세 실거래가',
    detail: '월 단위 실거래 기록에서 지역 평균가를 집계합니다. 전세는 전월세 전환율 4%를 적용해 월 환산 금액으로 비교에 사용합니다.',
    tone: 'decor',
  },
  {
    name: 'Oracle Cloud',
    desc: 'Always Free 인프라',
    detail: 'ARM 기반 상시 가동 환경에서 API와 PostgreSQL, Redis를 운영합니다. 배치 스케줄러가 유휴 회수 없이 동작하도록 구성했습니다.',
    tone: 'warn',
  },
];

const METHOD_ROWS = [
  ['물가 비교', 'CPI 비율 (2020=100)'],
  ['임금 비교', '연 평균 임금 (시/도 단위)'],
  ['주거비 비교', '전세 평균가, 전월세 전환율 4%/12'],
  ['실수령액', '4대보험 + 간이 누진 소득세'],
];

const STACK_ROWS = [
  ['백엔드', 'Java 21 · Spring Boot 4.1'],
  ['데이터', 'PostgreSQL 16 · Flyway'],
  ['캐시', 'Redis 7 (Lettuce)'],
  ['내성', 'Resilience4j (Retry · CircuitBreaker)'],
  ['프론트', 'Vite · Vanilla JS · Chart.js'],
  ['배포', 'Docker · Nginx · Oracle Cloud'],
];

const CREDITS = [
  ['서울 스카이라인 (히어로 배경)', 'CC0 1.0 · Wikimedia Commons'],
];

export function renderAbout(root) {
  const syncResult = el('div', { class: 'stack' });

  const syncButton = el('button', {
    class: 'btn btn-cta',
    type: 'button',
    text: '데이터 동기화 실행',
    onClick: runSync,
  });

  async function runSync() {
    syncButton.disabled = true;
    syncButton.textContent = '동기화 중';
    syncResult.replaceChildren(
      el('div', { class: 'skeleton skeleton-block', style: 'height: 4rem' }),
    );

    try {
      const result = await triggerSync();
      const rows = [
        ['CPI', `${formatNumber(result.cpiCount)} 건`],
        ['주거비', `${formatNumber(result.housingCount)} 건`],
        ['임금', `${formatNumber(result.wageCount)} 건`],
      ];

      syncResult.replaceChildren(
        el('div', { class: 'stack' }, [
          dataTable({ headers: ['구분', '수집 건수'], rows }),
          result.errors.length > 0
            ? el('div', { class: 'stack' }, [
                el('span', { class: 'label danger', text: '오류' }),
                ...result.errors.map((error) => el('p', { class: 'danger', text: `- ${error}` })),
              ])
            : el('p', { class: 'decor', text: '오류 없음' }),
        ]),
      );
      toast('데이터 동기화가 완료되었습니다.', 'info');
    } catch (error) {
      syncResult.replaceChildren(el('p', { class: 'danger', text: error.message }));
      toast('동기화에 실패했습니다.', 'error');
    } finally {
      syncButton.disabled = false;
      syncButton.textContent = '데이터 동기화 실행';
    }
  }

  root.replaceChildren(
    pageHeader({
      eyebrow: 'ABOUT',
      title: '소개',
      description:
        '전국 시 단위 생활비 비교 서비스. 통계청과 국토교통부 공개 데이터를 기반으로 같은 기준에서 환산합니다.',
    }),

    // ---- 데이터 출처 ----
    el('section', { class: 'section' }, [
      el('div', { class: 'container stack' }, [
        sectionLabel('데이터 출처'),
        el('div', { class: 'grid-asym stagger' },
          SOURCES.map((source) =>
            el('article', { class: 'card col-4 stack' }, [
              el('span', { class: `label ${source.tone}`, text: source.name }),
              el('h2', { class: 'card-title', text: source.desc }),
              el('p', { text: source.detail }),
            ]),
          ),
        ),
      ]),
    ]),

    // ---- 계산 방법 / 기술 구성 ----
    el('section', { class: 'section' }, [
      el('div', { class: 'container grid-asym' }, [
        el('div', { class: 'card col-7 stack' }, [
          el('span', { class: 'duct-tape', text: 'METHOD' }),
          el('h2', { class: 'card-title', text: '계산 방법' }),
          dataTable({ headers: ['항목', '기준'], rows: METHOD_ROWS }),
        ]),
        el('div', { class: 'card col-5 stack' }, [
          el('span', { class: 'duct-tape', text: 'STACK' }),
          el('h2', { class: 'card-title', text: '기술 구성' }),
          dataTable({ headers: ['영역', '기술'], rows: STACK_ROWS }),
        ]),
      ]),
    ]),

    // ---- 이미지 출처 ----
    el('section', { class: 'section' }, [
      el('div', { class: 'container stack' }, [
        sectionLabel('이미지 출처'),
        el('div', { class: 'surface-paper' }, [
          dataTable({ headers: ['이미지', '라이선스'], rows: CREDITS }),
        ]),
      ]),
    ]),

    // ---- 관리자 : 데이터 동기화 ----
    el('section', { class: 'section' }, [
      el('div', { class: 'container' }, [
        el('div', { class: 'surface-paper torn-edge stack' }, [
          el('span', { class: 'label', text: 'ADMIN' }),
          el('h2', { class: 'card-title', text: '데이터 동기화' }),
          el('p', {
            text: '공공 API에서 CPI·주거비·임금 데이터를 직접 수집합니다. API 키가 설정되지 않은 항목은 오류로 표시됩니다.',
          }),
          el('div', {}, [syncButton]),
          syncResult,
        ]),
      ]),
    ]),
  );
}

function sectionLabel(text) {
  return el('div', { class: 'row', style: 'gap: 1rem; align-items: center' }, [
    el('hr', { class: 'divider', style: 'flex: 1' }),
    el('span', { class: 'label', text }),
  ]);
}
