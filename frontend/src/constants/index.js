// 거래 유형 및 지역 계층 상수
export const TRADE_TYPES = {
  M: { code: 'M', label: '매매' },
  J: { code: 'J', label: '전세' },
  W: { code: 'W', label: '월세' },
};

export const TRADE_TYPE_OPTIONS = Object.values(TRADE_TYPES);

export const CITY_LEVEL = {
  PROVINCE: 1,
  TOWN: 2,
};

// 다중 비교 API 제한
export const COMPARE_MIN_CITIES = 2;
export const COMPARE_MAX_CITIES = 5;

// 연월 (YYYYMM) 포맷 검증
export const YEAR_MONTH_PATTERN = /^\d{6}$/;

export const ROUTES = {
  HOME: '#/',
  COMPARE: '#/compare',
  CALCULATOR: '#/calculator',
  REGIONS: '#/regions',
  ABOUT: '#/about',
};
