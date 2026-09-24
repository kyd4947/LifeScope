import { el } from '../utils/dom.js';
import { pageHeader, dataTable } from '../components/ui.js';
import { triggerSync } from '../api/client.js';
import { toast } from '../components/toast.js';
import { formatNumber } from '../utils/format.js';

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
    syncResult.replaceChildren(el('div', { class: 'skeleton skeleton-block', style: 'height: 4rem' }));

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
      description: '전국 시 단위 생활비 비교 서비스.统计数据청·국토교통부 공개 데이터를 기반으로 합니다.',
    }),
    el('section', { class: 'section' }, [
      el('div', { class: 'container grid-asym' }, [
        el('div', { class: 'card col-7 stack' }, [
          el('span', { class: 'duct-tape', text: 'METHOD' }),
          el('h2', { class: 'card-title', text: '계산 방법' }),
          dataTable({
            headers: ['항목', '기준'],
            rows: [
              ['물가', 'CPI 비율 (2020=100)'],
              ['임금', '연 평균 임금 (시/도 단위)'],
              ['주거비', '전세 평균가, 전월세 전환율 4%/12'],
              ['실수령액', '4대보험 + 간이 누진소득세'],
            ],
          }),
        ]),
        el('div', { class: 'card col-5 stack' }, [
          el('span', { class: 'duct-tape', text: 'STACK' }),
          el('h2', { class: 'card-title', text: '기술 구성' }),
          dataTable({
            headers: ['영역', '기술'],
            rows: [
              ['백엔드', 'Java 21 · Spring Boot 4.1'],
              ['데이터', 'PostgreSQL 16 · Flyway'],
              ['캐시', 'Redis 7 (Lettuce)'],
              ['내성', 'Resilience4j (Retry · CircuitBreaker)'],
              ['프론트', 'Vite · Vanilla JS · Chart.js'],
              ['배포', 'Docker · Nginx · Oracle Cloud'],
            ],
          }),
        ]),
      ]),
    ]),
    el('section', { class: 'section' }, [
      el('div', { class: 'container stack' }, [
        el('div', { class: 'surface-paper torn-edge stack' }, [
          el('span', { class: 'label', text: 'ADMIN' }),
          el('h2', { class: 'card-title', text: '데이터 동기화' }),
          el('p', { class: 'muted' }, [
            document.createTextNode(
              '공공 API에서 CPI·주거비·임금 데이터를 직접 수집합니다. API 키가 설정되지 않은 항목은 오류로 표시됩니다.',
            ),
          ]),
          el('div', {}, [syncButton]),
          syncResult,
        ]),
      ]),
    ]),
  );
}
