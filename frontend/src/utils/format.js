// 금액 / 비율 / 연월 포맷 유틸

// 원 단위 -> "4,000,000원"
export function formatWon(value) {
  if (value === null || value === undefined) return '-';
  return `${formatNumber(value)}원`;
}

// 원 단위 -> 조/만 축약 ("4,200만" / "1.2억")
export function formatWonShort(value) {
  if (value === null || value === undefined) return '-';
  const abs = Math.abs(value);
  if (abs >= 100_000_000) {
    return `${trimZero((value / 100_000_000).toFixed(2))}억`;
  }
  if (abs >= 10_000) {
    return `${trimZero((value / 10_000).toFixed(1))}만`;
  }
  return formatNumber(value);
}

// 만원 단위 -> "4억 8,000만"
export function formatManWon(value) {
  if (value === null || value === undefined) return '-';
  return `${formatWonShort(value * 10_000)}원`;
}

export function formatNumber(value) {
  if (value === null || value === undefined) return '-';
  return Number(value).toLocaleString('ko-KR');
}

// 비율 -> "1.0243" (기준 1.0 과golden 강조용)
export function formatRatio(value) {
  if (value === null || value === undefined) return '-';
  return Number(value).toFixed(4);
}

// 비율 -> 화면 표기용 증감률 ("+2.43%" / "-1.20%")
export function formatRatioDelta(value) {
  if (value === null || value === undefined) return '-';
  const delta = (Number(value) - 1) * 100;
  const sign = delta > 0 ? '+' : '';
  return `${sign}${delta.toFixed(2)}%`;
}

export function formatPercent(value, fractionDigits = 1) {
  if (value === null || value === undefined) return '-';
  return `${Number(value).toFixed(fractionDigits)}%`;
}

// CPI 값 (2020=100 기준 지수)
export function formatCpi(value) {
  if (value === null || value === undefined) return '-';
  return Number(value).toFixed(2);
}

// "202608" -> "2026년 8월"
export function formatYearMonth(ym) {
  if (!ym || String(ym).length !== 6) return ym || '-';
  const year = String(ym).slice(0, 4);
  const month = String(ym).slice(4, 6).replace(/^0/, '');
  return `${year}년 ${Number(month)}월`;
}

// "2024" -> "2024년"
export function formatYear(year) {
  if (year === null || year === undefined) return '-';
  return `${year}년`;
}

function trimZero(str) {
  return str.replace(/\.?0+$/, '');
}
