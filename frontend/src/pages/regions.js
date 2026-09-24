import { el } from '../utils/dom.js';
import { getProvinces, getWage, getCpi, getHousing } from '../api/client.js';
import { pageHeader, dataTable } from '../components/ui.js';
import { skeletonLines, noDataState, errorState } from '../components/states.js';
import { TRADE_TYPES } from '../constants/index.js';
import {
  formatWon,
  formatWonShort,
  formatCpi,
  formatManWon,
  formatYearMonth,
  formatYear,
} from '../utils/format.js';

export function renderRegions(root) {
  const detailBox = el('div', { class: 'stack' });
  const provinceList = el('div', { class: 'grid-asym stagger' });

  const searchInput = el('input', {
    class: 'input',
    type: 'search',
    placeholder: '지역명 검색',
    onInput: (e) => filter(e.target.value.trim()),
  });

  function filter(keyword) {
    provinceList.querySelectorAll('[data-name]').forEach((card) => {
      const name = card.dataset.name;
      card.classList.toggle('hidden', keyword && !name.includes(keyword));
    });
  }

  async function loadProvinces() {
    provinceList.replaceChildren(skeletonLines(4));
    try {
      const provinces = await getProvinces();
      provinceList.replaceChildren(
        ...provinces.map((province) =>
          el('article', {
            class: 'card col-4 stack',
            dataset: { name: province.name },
          }, [
            el('span', { class: 'label', text: province.code }),
            el('h2', { class: 'card-title', text: province.name }),
            el('div', { class: 'row' }, [
              el('button', {
                class: 'btn btn-ghost',
                type: 'button',
                text: '상세',
                onClick: () => loadDetail(province),
              }),
            ]),
          ]),
        ),
      );
    } catch (error) {
      provinceList.replaceChildren(errorState({ message: error.message, onRetry: loadProvinces }));
    }
  }

  async function loadDetail(province) {
    detailBox.replaceChildren(
      el('div', { class: 'surface-paper stack' }, [
        el('span', { class: 'label', text: '불러오는 중' }),
        skeletonLines(5),
      ]),
    );
    detailBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

    const [wage, cpi, jeonse] = await Promise.allSettled([
      getWage(province.code),
      getCpi(province.code),
      getHousing(province.code, TRADE_TYPES.J.code),
    ]);

    const rows = [];
    if (wage.status === 'fulfilled') {
      rows.push([
        '평균 임금',
        formatWonShort(wage.value.wageAvg),
        '연',
        formatYear(wage.value.year),
      ]);
    }
    if (cpi.status === 'fulfilled') {
      rows.push([
        '소비자물가지수',
        formatCpi(cpi.value.cpiValue),
        '2020=100',
        formatYearMonth(cpi.value.yearMonth),
      ]);
    }
    if (jeonse.status === 'fulfilled') {
      rows.push([
        '전세 평균가',
        formatManWon(jeonse.value.avgPrice),
        `${jeonse.value.dealCount}건`,
        formatYearMonth(jeonse.value.yearMonth),
      ]);
    }

    detailBox.replaceChildren(
      el('div', { class: 'surface-paper torn-edge stack entry' }, [
        el('div', { class: 'row', style: 'justify-content: space-between' }, [
          el('div', { class: 'stack' }, [
            el('span', { class: 'label', text: province.code }),
            el('h2', { text: province.name }),
          ]),
          el('button', {
            class: 'btn btn-ghost',
            type: 'button',
            text: '닫기',
            onClick: () => detailBox.replaceChildren(),
          }),
        ]),
        rows.length > 0
          ? dataTable({ headers: ['항목', '값', '단위', '기준'], rows })
          : noDataState({ cityName: province.name }),
      ]),
    );
  }

  root.replaceChildren(
    pageHeader({
      eyebrow: 'REGIONS',
      title: '지역 정보',
      description: '전국 시/도 17곳의 임금·물가·전세 데이터를 확인합니다.',
    }),
    el('section', { class: 'section' }, [
      el('div', { class: 'container stack' }, [
        el('div', { class: 'field', style: 'max-width: 320px' }, [
          el('label', { class: 'field-label', for: 'region-search', text: '검색' }),
          searchInput,
        ]),
        provinceList,
        el('div', { class: 'row', style: 'gap: 1rem; align-items: center' }, [
          el('hr', { class: 'divider', style: 'flex: 1' }),
          el('span', { class: 'label', text: '상세' }),
        ]),
        detailBox,
      ]),
    ]),
  );

  loadProvinces();
}
