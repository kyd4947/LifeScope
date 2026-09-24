import { el } from '../utils/dom.js';
import {
  calculateRealIncome,
  calculateCostAdjust,
  calculateHousingAdjusted,
} from '../api/client.js';
import { createCityPicker } from '../components/cityPicker.js';
import { pageHeader, dataTable, statBlock } from '../components/ui.js';
import { errorState, noDataState } from '../components/states.js';
import { toast } from '../components/toast.js';
import { TRADE_TYPE_OPTIONS } from '../constants/index.js';
import { formatWon, formatWonShort, formatCpi, formatManWon, formatRatio } from '../utils/format.js';

const TABS = [
  { id: 'real-income', label: '실수령액' },
  { id: 'cost-adjust', label: '물가 보정' },
  { id: 'housing', label: '주거비 보정' },
];

export function renderCalculator(root) {
  const active = { tab: 'real-income' };
  const panel = el('div', { class: 'stack' });

  const tabs = el(
    'div',
    { class: 'row', role: 'tablist' },
    TABS.map((tab) =>
      el('button', {
        class: 'btn btn-ghost',
        type: 'button',
        role: 'tab',
        text: tab.label,
        onClick: () => {
          active.tab = tab.id;
          syncTabs();
          renderPanel();
        },
      }),
    ),
  );

  function syncTabs() {
    tabs.querySelectorAll('button').forEach((button) => {
      const tab = TABS.find((t) => t.label === button.textContent);
      button.className = tab?.id === active.tab ? 'btn btn-primary' : 'btn btn-ghost';
      button.setAttribute('aria-selected', String(tab?.id === active.tab));
    });
  }

  function renderPanel() {
    if (active.tab === 'real-income') panel.replaceChildren(realIncomePanel());
    else if (active.tab === 'cost-adjust') panel.replaceChildren(costAdjustPanel());
    else panel.replaceChildren(housingPanel());
  }

  root.replaceChildren(
    pageHeader({
      eyebrow: 'CALCULATOR',
      title: '계산기',
      description: '월급의 실제 손에 남는 돈부터, 이 도시에서 살 수 있는 수준까지 계산합니다.',
      children: el('div', { style: 'margin-top: 1.5rem' }, [tabs]),
    }),
    el('section', { class: 'section' }, [
      el('div', { class: 'container' }, [panel]),
    ]),
  );

  syncTabs();
  renderPanel();
}

// ---- 1. 실수령액 ----

function realIncomePanel() {
  const input = el('input', {
    class: 'input',
    type: 'number',
    min: '0',
    step: '1000000',
    placeholder: '예: 50000000',
  });

  const result = el('div', { class: 'stack' });

  async function calculate() {
    const annual = Number(input.value);
    if (!annual || annual <= 0) {
      toast('연봉을 입력하세요.', 'warn');
      return;
    }
    result.replaceChildren(el('div', { class: 'skeleton skeleton-block' }));
    try {
      const data = await calculateRealIncome(annual);
      result.replaceChildren(
        el('div', { class: 'grid-asym entry' }, [
          el('div', { class: 'card col-7 stack' }, [
            el('span', { class: 'label', text: '월 실수령액' }),
            el('span', {
              class: 'stat-value accent',
              style: 'font-size: 2.25rem',
              text: formatWon(data.monthlyNet),
            }),
            el('span', { class: 'label', text: `총 공제 ${formatWon(data.totalDeduction)}` }),
          ]),
          el('div', { class: 'card col-5' }, [
            dataTable({
              headers: ['항목', '금액'],
              rows: [
                ['연봉', formatWon(data.annualSalary)],
                ['월 총액', formatWon(data.monthlyGross)],
                ['국민연금', formatWon(data.pension)],
                ['건강보험', formatWon(data.health)],
                ['장기요양', formatWon(data.care)],
                ['고용보험', formatWon(data.employment)],
                ['소득세', formatWon(data.incomeTax)],
              ],
            }),
          ]),
        ]),
      );
    } catch (error) {
      result.replaceChildren(errorState({ message: error.message, onRetry: calculate }));
    }
  }

  return el('div', { class: 'stack' }, [
    el('div', { class: 'surface-paper torn-edge stack' }, [
      el('span', { class: 'duct-tape', text: '연봉 →到手' }),
      el('div', { class: 'field', style: 'max-width: 320px' }, [
        el('label', { class: 'field-label', for: 'annual', text: '연봉 (원)' }),
        input,
      ]),
      el('div', {}, [
        el('button', { class: 'btn btn-cta', type: 'button', text: '계산하기', onClick: calculate }),
      ]),
    ]),
    result,
  ]);
}

// ---- 2. 물가 보정 ----

function costAdjustPanel() {
  const fromPicker = createCityPicker();
  const toPicker = createCityPicker();
  const salaryInput = el('input', {
    class: 'input',
    type: 'number',
    min: '1',
    step: '100000',
    value: '4000000',
  });
  const result = el('div', { class: 'stack' });

  async function calculate() {
    const from = fromPicker.getCity();
    const to = toPicker.getCity();
    const monthlySalary = Number(salaryInput.value);

    if (!from || !to) {
      toast('기준 지역과 대상 지역을 모두 선택하세요.', 'warn');
      return;
    }
    if (from.code === to.code) {
      toast('서로 다른 지역을 선택하세요.', 'warn');
      return;
    }

    result.replaceChildren(el('div', { class: 'skeleton skeleton-block' }));
    try {
      const data = await calculateCostAdjust({ fromCode: from.code, toCode: to.code, monthlySalary });
      result.replaceChildren(renderCostAdjust(data, monthlySalary));
    } catch (error) {
      if (error.status === 404) {
        result.replaceChildren(noDataState({ cityName: from.name }));
      } else {
        result.replaceChildren(errorState({ message: error.message, onRetry: calculate }));
      }
    }
  }

  return el('div', { class: 'stack' }, [
    el('div', { class: 'grid-asym' }, [
      el('div', { class: 'card col-6 stack' }, [
        el('span', { class: 'duct-tape', text: '기준 지역' }),
        fromPicker.element,
      ]),
      el('div', { class: 'card col-6 stack' }, [
        el('span', { class: 'duct-tape', text: '이사가려는 지역' }),
        toPicker.element,
      ]),
    ]),
    el('div', { class: 'surface-paper stack' }, [
      el('div', { class: 'field', style: 'max-width: 280px' }, [
        el('label', { class: 'field-label', for: 'salary', text: '기준 지역 월급 (원)' }),
        salaryInput,
      ]),
      el('div', {}, [
        el('button', { class: 'btn btn-cta', type: 'button', text: '환산하기', onClick: calculate }),
      ]),
    ]),
    result,
  ]);
}

function renderCostAdjust(data, monthlySalary) {
  return el('div', { class: 'grid-asym entry' }, [
    el('div', { class: 'card col-7 stack' }, [
      el('span', { class: 'label', text: `${data.toCity.name} 에서 필요한 월급` }),
      el('span', { class: 'stat-value accent', style: 'font-size: 2.25rem' }, [
        document.createTextNode(formatWon(data.adjustedSalary)),
      ]),
      el('p', { class: 'muted', text: `${data.fromCity.name} 물가 기준` }),
    ]),
    el('div', { class: 'card col-5 stack' }, [
      statBlock({ label: '입력 월급', value: formatWonShort(monthlySalary), unit: '원' }),
      statBlock({ label: '물가 비율', value: formatRatio(data.cpiRatio) }),
    ]),
  ]);
}

// ---- 3. 주거비 보정 ----

function housingPanel() {
  const fromPicker = createCityPicker();
  const toPicker = createCityPicker();
  const salaryInput = el('input', {
    class: 'input',
    type: 'number',
    min: '1',
    step: '100000',
    value: '4000000',
  });

  const tradeSelect = el(
    'select',
    { class: 'select' },
    TRADE_TYPE_OPTIONS.map((type) => el('option', { value: type.code, text: type.label })),
  );

  const result = el('div', { class: 'stack' });

  async function calculate() {
    const from = fromPicker.getCity();
    const to = toPicker.getCity();
    const monthlySalary = Number(salaryInput.value);

    if (!from || !to) {
      toast('기준 지역과 대상 지역을 모두 선택하세요.', 'warn');
      return;
    }
    if (from.code === to.code) {
      toast('서로 다른 지역을 선택하세요.', 'warn');
      return;
    }

    result.replaceChildren(el('div', { class: 'skeleton skeleton-block' }));
    try {
      const data = await calculateHousingAdjusted({
        fromCode: from.code,
        toCode: to.code,
        monthlySalary,
        tradeType: tradeSelect.value,
      });
      result.replaceChildren(renderHousing(data));
    } catch (error) {
      if (error.status === 404) {
        result.replaceChildren(noDataState({ cityName: toPicker.getCity()?.name }));
      } else {
        result.replaceChildren(errorState({ message: error.message, onRetry: calculate }));
      }
    }
  }

  return el('div', { class: 'stack' }, [
    el('div', { class: 'grid-asym' }, [
      el('div', { class: 'card col-6 stack' }, [
        el('span', { class: 'duct-tape', text: '현재 거주 지역' }),
        fromPicker.element,
      ]),
      el('div', { class: 'card col-6 stack' }, [
        el('span', { class: 'duct-tape', text: '이사 목표 지역' }),
        toPicker.element,
      ]),
    ]),
    el('div', { class: 'surface-paper stack' }, [
      el('div', { class: 'field', style: 'max-width: 280px' }, [
        el('label', { class: 'field-label', for: 'salary', text: '월급 (원)' }),
        salaryInput,
      ]),
      el('div', { class: 'field', style: 'max-width: 280px' }, [
        el('label', { class: 'field-label', for: 'trade', text: '거래 유형' }),
        tradeSelect,
      ]),
      el('div', {}, [
        el('button', { class: 'btn btn-cta', type: 'button', text: '실질 구매력 계산', onClick: calculate }),
      ]),
    ]),
    result,
  ]);
}

function renderHousing(data) {
  return el('div', { class: 'grid-asym entry' }, [
    el('div', { class: 'card col-7 stack' }, [
      el('span', { class: 'label', text: `${data.toCity.name} 이동 후 실질 구매력` }),
      el('span', { class: 'stat-value accent', style: 'font-size: 2.25rem' }, [
        document.createTextNode(formatWon(data.realPurchasingPower)),
      ]),
      el('p', { class: 'muted', text: `${data.tradeTypeLabel} 기준` }),
    ]),
    el('div', { class: 'card col-5' }, [
      dataTable({
        headers: ['항목', data.fromCity.name, data.toCity.name],
        rows: [
          ['평균가(만원)', formatManWon(data.fromHousingPrice), formatManWon(data.toHousingPrice)],
          ['월 주거비', formatWon(data.fromMonthlyHousingCost), formatWon(data.toMonthlyHousingCost)],
        ],
      }),
    ]),
  ]);
}
