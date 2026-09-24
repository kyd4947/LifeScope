import { el } from '../utils/dom.js';
import { toast } from './toast.js';
import { getProvinces, getTowns, searchCities } from '../api/client.js';

// 지역 선택 필드 — 시/도 선택 후 시군구 선택 또는 지역명 검색
export function createCityPicker({ onChange } = {}) {
  const state = {
    province: null,
    town: null,
  };

  const provinceSelect = el('select', {
    class: 'select',
    id: 'province-select',
    disabled: true,
  });

  const townSelect = el('select', {
    class: 'select',
    id: 'town-select',
    disabled: true,
  });

  const searchInput = el('input', {
    class: 'input',
    id: 'city-search',
    type: 'search',
    placeholder: '지역명 검색 (예: 서울)',
    autocomplete: 'off',
  });

  const searchResults = el('div', { class: 'stack hidden' });

  const status = el('span', { class: 'label', text: '지역 목록 불러오는 중' });

  provinceSelect.addEventListener('change', async () => {
    const code = provinceSelect.value;
    state.province = code ? findProvince(code) : null;
    state.town = null;
    notify();
    if (code) await loadTowns(code);
  });

  townSelect.addEventListener('change', () => {
    state.town = townSelect.value ? findTown(townSelect.value) : null;
    notify();
  });

  let searchTimer = null;
  searchInput.addEventListener('input', () => {
    clearTimeout(searchTimer);
    const keyword = searchInput.value.trim();
    if (!keyword) {
      searchResults.classList.add('hidden');
      searchResults.replaceChildren();
      return;
    }
    searchTimer = setTimeout(() => runSearch(keyword), 260);
  });

  function notify() {
    const city = state.town || state.province;
    onChange?.(city);
  }

  async function loadTowns(parentCode) {
    townSelect.disabled = true;
    townSelect.replaceChildren(el('option', { value: '', text: '시군구 선택 (선택 사항)' }));
    try {
      const towns = await getTowns(parentCode);
      if (towns.length === 0) {
        townSelect.replaceChildren(el('option', { value: '', text: '하위 지역 데이터 없음' }));
        return;
      }
      townSelect.replaceChildren(
        el('option', { value: '', text: '시군구 선택 (선택 사항)' }),
        ...towns.map((town) => el('option', { value: town.code, text: town.name })),
      );
      townSelect.disabled = false;
    } catch {
      townSelect.replaceChildren(el('option', { value: '', text: '시군구 조회 실패' }));
      toast('시군구 목록을 불러오지 못했습니다.', 'error');
    }
  }

  async function runSearch(keyword) {
    try {
      const results = await searchCities(keyword);
      if (results.length === 0) {
        searchResults.replaceChildren(el('p', { class: 'label', text: '검색 결과 없음' }));
        searchResults.classList.remove('hidden');
        return;
      }
      searchResults.replaceChildren(
        ...results.slice(0, 10).map((city) =>
          el('button', {
            class: 'btn btn-ghost',
            type: 'button',
            style: 'justify-content: flex-start; width: 100%',
            onClick: () => {
              applyCity(city);
              searchResults.classList.add('hidden');
              searchInput.value = '';
            },
          }, `${city.name} (${city.code})`),
        ),
      );
      searchResults.classList.remove('hidden');
    } catch {
      searchResults.classList.add('hidden');
    }
  }

  // 검색 결과 선택 -> 시/도 / 시군구 select 동기화
  function applyCity(city) {
    state.town = null;
    if (city.level === 1) {
      state.province = city;
      provinceSelect.value = city.code;
    } else {
      state.province = findProvince(city.parentCode) || null;
      state.town = city;
    }
    notify();
    loadTowns(city.parentCode || city.code);
  }

  // 시/도 목록 로드
  loadProvinces();

  async function loadProvinces() {
    try {
      const provinces = await getProvinces();
      status.textContent = `시/도 ${provinces.length}개`;
      provinceSelect.replaceChildren(
        el('option', { value: '', text: '시/도 선택' }),
        ...provinces.map((province) =>
          el('option', { value: province.code, text: province.name }),
        ),
      );
      provinceSelect.disabled = false;
    } catch (error) {
      status.textContent = '지역 목록 조회 실패';
      toast(error.message, 'error');
    }
  }

  function findProvince(code) {
    return provinceSelect.querySelector(`option[value="${code}"]`)
      ? { code, name: provinceSelect.selectedOptions[0]?.textContent }
      : null;
  }

  function findTown(code) {
    const option = townSelect.querySelector(`option[value="${code}"]`);
    return option ? { code, name: option.textContent } : null;
  }

  return {
    element: el('div', { class: 'stack' }, [
      el('div', { class: 'field' }, [
        el('label', { class: 'field-label', for: 'province-select', text: '시/도' }),
        provinceSelect,
      ]),
      el('div', { class: 'field' }, [
        el('label', { class: 'field-label', for: 'town-select', text: '시군구 (선택)' }),
        townSelect,
      ]),
      el('div', { class: 'field' }, [
        el('label', { class: 'field-label', for: 'city-search', text: '지역명 검색' }),
        searchInput,
        searchResults,
      ]),
      status,
    ]),
    getCity: () => state.town || state.province,
    reset() {
      state.province = null;
      state.town = null;
      provinceSelect.value = '';
      townSelect.value = '';
      notify();
    },
  };
}
