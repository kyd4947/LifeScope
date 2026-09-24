import { el } from '../utils/dom.js';
import { compareCities, getProvinces } from '../api/client.js';
import { COMPARE_MAX_CITIES, COMPARE_MIN_CITIES } from '../constants/index.js';
import { pageHeader, dataTable, statBlock, sectionRule } from '../components/ui.js';
import { skeletonCards, noDataState, errorState } from '../components/states.js';
import { toast } from '../components/toast.js';
import {
  formatWon,
  formatWonShort,
  formatRatio,
  formatRatioDelta,
  formatManWon,
  formatCpi,
  formatYearMonth,
} from '../utils/format.js';

let chart = null;

export function renderCompare(root) {
  destroyChart();

  const state = { codes: [], salary: 4000000, result: null, loading: false };

  const cityList = el('div', { class: 'row' });
  const resultBox = el('div', { class: 'stack' });

  const salaryInput = el('input', {
    class: 'input',
    type: 'number',
    min: '1',
    step: '100000',
    value: String(state.salary),
    onInput: (e) => {
      state.salary = Number(e.target.value);
    },
  });

  async function loadProvinces() {
    cityList.replaceChildren(
      el('span', { class: 'label', text: '시/도 불러오는 중' }),
    );
    try {
      const provinces = await getProvinces();
      cityList.replaceChildren(
        ...provinces.map((province) => {
          const chip = el('button', {
            class: 'btn btn-ghost',
            type: 'button',
            text: province.name,
            onClick: () => toggleCity(province),
          });
          chip.dataset.code = province.code;
          return chip;
        }),
      );
      syncChips();
    } catch (error) {
      cityList.replaceChildren(el('span', { class: 'label danger', text: error.message }));
    }
  }

  function toggleCity(city) {
    const index = state.codes.indexOf(city.code);
    if (index >= 0) {
      state.codes.splice(index, 1);
    } else {
      if (state.codes.length >= COMPARE_MAX_CITIES) {
        toast(`비교할 지역은 최대 ${COMPARE_MAX_CITIES}개입니다.`, 'warn');
        return;
      }
      state.codes.push(city.code);
    }
    syncChips();
  }

  function syncChips() {
    cityList.querySelectorAll('button').forEach((chip) => {
      const index = state.codes.indexOf(chip.dataset.code);
      if (index === 0) {
        chip.className = 'btn btn-cta';
        chip.textContent = `${chip.textContent.replace(' 기준', '')} 기준`;
      } else if (index > 0) {
        chip.className = 'btn btn-primary';
      } else {
        chip.className = 'btn btn-ghost';
        chip.textContent = chip.textContent.replace(' 기준', '');
      }
    });
    submitButton.disabled = state.codes.length < COMPARE_MIN_CITIES;
  }

  async function submit() {
    if (state.codes.length < COMPARE_MIN_CITIES) {
      toast(`지역을 ${COMPARE_MIN_CITIES}개 이상 선택하세요.`, 'warn');
      return;
    }
    if (!state.salary || state.salary <= 0) {
      toast('월급을 입력하세요.', 'warn');
      return;
    }

    state.loading = true;
    resultBox.replaceChildren(skeletonCards(2));

    try {
      state.result = await compareCities(state.codes, state.salary);
      renderResult();
    } catch (error) {
      resultBox.replaceChildren(errorState({ message: error.message, onRetry: submit }));
    } finally {
      state.loading = false;
    }
  }

  const submitButton = el('button', {
    class: 'btn btn-cta',
    type: 'button',
    text: '비교하기',
    disabled: true,
    onClick: submit,
  });

  function renderResult() {
    const { baseCity, inputMonthlySalary, cities } = state.result;
    destroyChart();

    const hasCpi = cities.some((c) => c.cpiRatio !== null);
    const hasWage = cities.some((c) => c.wageRatio !== null);
    const hasHousing = cities.some((c) => c.housingRatio !== null);

    if (!hasCpi && !hasWage && !hasHousing) {
      resultBox.replaceChildren(
        noDataState({
          cityName: baseCity.name,
          onRetry: submit,
        }),
      );
      return;
    }

    resultBox.replaceChildren(
      // 요약 카드 (비대칭)
      el('div', { class: 'grid-asym entry' }, [
        el('div', { class: 'card col-7 stack' }, [
          el('span', { class: 'label', text: '기준 지역' }),
          el('h2', { text: baseCity.name }),
          el('div', { class: 'row', style: 'gap: 2rem' }, [
            statBlock({ label: '입력 월급', value: formatWonShort(inputMonthlySalary), unit: '원' }),
            statBlock({ label: '비교 지역', value: String(cities.length), unit: '곳' }),
          ]),
        ]),
        el('div', { class: 'card col-5 stack' }, [
          el('span', { class: 'label', text: '물가 환산 월급' }),
          ...cities.map((c) =>
            el('div', { class: 'row', style: 'justify-content: space-between' }, [
              el('span', { text: c.city.name }),
              el('span', {
                class: 'mono-value',
                text:
                  c.adjustedMonthlySalary !== null
                    ? `${formatWonShort(c.adjustedMonthlySalary)}원`
                    : '-',
              }),
            ]),
          ),
        ]),
      ]),

      // 차트
      hasCpi
        ? el('div', { class: 'stack entry' }, [
            sectionRule('물가 비율'),
            el('div', { class: 'chart-frame' }, [el('canvas', { id: 'chart-cpi' })]),
          ])
        : null,
      hasWage
        ? el('div', { class: 'stack entry' }, [
            sectionRule('임금 비율'),
            el('div', { class: 'chart-frame' }, [el('canvas', { id: 'chart-wage' })]),
          ])
        : null,

      // 표
      el('div', { class: 'stack entry' }, [
        sectionRule('전체 데이터'),
        buildTable(cities, baseCity),
      ]),
    );

    if (hasCpi) drawChart('chart-cpi', cities, 'cpiRatio', '물가 비율 (기준 1.0000)', '#3b5998');
    if (hasWage) drawChart('chart-wage', cities, 'wageRatio', '임금 비율 (기준 1.0000)', '#6b8e23');
  }

  root.replaceChildren(
    pageHeader({
      eyebrow: 'MULTI CITY COMPARE',
      title: '지역 비교',
      description: `지역을 ${COMPARE_MIN_CITIES}~${COMPARE_MAX_CITIES}개 선택하세요. 첫 번째로 선택한 지역이 기준이 됩니다.`,
    }),
    el('section', { class: 'section' }, [
      el('div', { class: 'container stack' }, [
        // 선택 영역
        el('div', { class: 'surface-paper torn-edge stack' }, [
          el('span', { class: 'label', text: '지역 선택' }),
          cityList,
          el('div', { class: 'field', style: 'max-width: 280px' }, [
            el('label', { class: 'field-label', for: 'salary', text: '기준 월급 (원)' }),
            salaryInput,
          ]),
          el('div', {}, [submitButton]),
        ]),
        // 결과
        resultBox,
      ]),
    ]),
  );

  loadProvinces();
}

function buildTable(cities, baseCity) {
  const headers = [
    '지역',
    'CPI',
    'CPI 비율',
    '환산 월급',
    '연 임금',
    '임금 비율',
    '전세',
    '전세 비율',
  ];

  const rows = cities.map((c) => [
    `${c.city.name}${c.city.code === baseCity.code ? ' (기준)' : ''}`,
    c.cpi ? formatCpi(c.cpi.cpiValue) : '-',
    c.cpiRatio !== null ? formatRatio(c.cpiRatio) : '-',
    c.adjustedMonthlySalary !== null ? formatWon(c.adjustedMonthlySalary) : '-',
    c.wage ? formatWonShort(c.wage.wageAvg) : '-',
    c.wageRatio !== null ? `${formatRatio(c.wageRatio)} (${formatRatioDelta(c.wageRatio)})` : '-',
    c.housing ? formatManWon(c.housing.avgPrice) : '-',
    c.housingRatio !== null ? `${formatRatio(c.housingRatio)} (${formatRatioDelta(c.housingRatio)})` : '-',
  ]);

  return el('div', { class: 'surface-paper' }, [dataTable({ headers, rows })]);
}

function drawChart(canvasId, cities, field, title, color) {
  const canvas = document.getElementById(canvasId);
  if (!canvas) return;

  // 동적 import — 첫 화면 로딩 비용 절약
  import('chart.js/auto').then(({ default: Chart }) => {
    if (chart) chart.destroy();

    // 종이 배경(밝은 컨텍스트)에 맞춘 차트 테마
    Chart.defaults.font.family = "'Courier Prime', 'Courier New', monospace";
    Chart.defaults.font.size = 12;
    Chart.defaults.color = '#2e2e2e';

    const palette = ['#3b5998', '#6b8e23', '#a84700', '#8c2727', '#b5a642'];

    chart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: cities.map((c) => c.city.name),
        datasets: [
          {
            label: title,
            data: cities.map((c) => c[field]),
            backgroundColor: cities.map((_, i) => palette[i % palette.length]),
            borderColor: '#2e2e2e',
            borderWidth: 1.5,
            borderRadius: 2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => `${formatRatio(ctx.parsed.y)} (${formatRatioDelta(ctx.parsed.y)})`,
            },
          },
        },
        scales: {
          y: {
            title: { display: true, text: title, color: '#2e2e2e' },
            grid: { color: 'rgba(46,46,46,0.14)' },
            ticks: { color: '#2e2e2e' },
          },
          x: {
            grid: { display: false },
            ticks: { color: '#2e2e2e' },
          },
        },
      },
    });
  });
}

function destroyChart() {
  if (chart) {
    chart.destroy();
    chart = null;
  }
}
