// Spring Boot API 통신 래퍼
// 백엔드 에러 형식 : { status, error, message, timestamp, path }

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

class ApiError extends Error {
  constructor(message, status, path) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.path = path;
  }

  // 데이터 미수집 상태 (KOSIS/MOLIT 키 발급 전)
  get isNotFound() {
    return this.status === 404;
  }

  // 검증 오류 (백엔드 GlobalExceptionHandler 가 400 으로 변환)
  get isBadRequest() {
    return this.status === 400;
  }

  get isServerError() {
    return this.status >= 500;
  }
}

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;

  let response;
  try {
    response = await fetch(url, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
      },
      ...options,
    });
  } catch (cause) {
    throw new ApiError('서버에 연결할 수 없습니다. 네트워크 상태를 확인하세요.', 0, path);
  }

  if (!response.ok) {
    let message = `요청에 실패했습니다 (HTTP ${response.status})`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      // JSON 파싱 실패 시 기본 메시지 유지
    }
    throw new ApiError(message, response.status, path);
  }

  if (response.status === 204) return null;
  return response.json();
}

function query(params) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== '') {
      search.append(key, value);
    }
  });
  const qs = search.toString();
  return qs ? `?${qs}` : '';
}

// ---- 지역 ----

export function getProvinces() {
  return request('/cities');
}

export function getCity(code) {
  return request(`/cities/${encodeURIComponent(code)}`);
}

export function getTowns(parentCode) {
  return request(`/cities/${encodeURIComponent(parentCode)}/towns`);
}

export function searchCities(name) {
  return request(`/cities/search${query({ name })}`);
}

// ---- 임금 ----

export function getWage(cityCode, year) {
  return request(`/cities/${encodeURIComponent(cityCode)}/wage${query({ year })}`);
}

export function getWageRanking(year) {
  return request(`/wages${query({ year })}`);
}

// ---- CPI ----

export function getCpi(cityCode, from, to) {
  return request(`/cities/${encodeURIComponent(cityCode)}/cpi${query({ from, to })}`);
}

// ---- 주거비 ----

export function getHousing(cityCode, tradeType, from, to) {
  return request(
    `/cities/${encodeURIComponent(cityCode)}/housing${query({ tradeType, from, to })}`,
  );
}

// ---- 비교 ----

// 다중 비교 (2~5개, 첫 번째가 기준)
export function compareCities(cityCodes, salary) {
  return request(`/compare${query({ cities: cityCodes.join(','), salary })}`);
}

// 두 도시 비교 (기존 API)
export function compareTwo(from, to, salary) {
  return request(`/comparison${query({ from, to, salary })}`);
}

// ---- 계산기 ----

export function calculateRealIncome(annualSalary) {
  return request('/calculator/real-income', {
    method: 'POST',
    body: JSON.stringify({ annualSalary }),
  });
}

export function calculateCostAdjust({ fromCode, toCode, monthlySalary }) {
  return request('/calculator/cost-adjust', {
    method: 'POST',
    body: JSON.stringify({ fromCode, toCode, monthlySalary }),
  });
}

export function calculateHousingAdjusted({ fromCode, toCode, monthlySalary, tradeType }) {
  return request('/calculator/housing-adjusted', {
    method: 'POST',
    body: JSON.stringify({ fromCode, toCode, monthlySalary, tradeType }),
  });
}

// ---- 관리자 ----

export function triggerSync() {
  return request('/admin/sync', { method: 'POST' });
}

export { ApiError };
