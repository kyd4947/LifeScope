import './styles/main.css';
import { el, mount, clear } from './utils/dom.js';
import { ROUTES } from './constants/index.js';
import { createNav, createFooter } from './components/nav.js';
import { renderHome } from './pages/home.js';
import { renderCompare } from './pages/compare.js';
import { renderCalculator } from './pages/calculator.js';
import { renderRegions } from './pages/regions.js';
import { renderAbout } from './pages/about.js';

const PAGES = [
  { match: ROUTES.REGIONS, render: renderRegions },
  { match: ROUTES.COMPARE, render: renderCompare },
  { match: ROUTES.CALCULATOR, render: renderCalculator },
  { match: ROUTES.ABOUT, render: renderAbout },
  { match: ROUTES.HOME, render: renderHome },
];

const app = document.getElementById('app');

function resolve(hash) {
  return PAGES.find((page) => page.match === hash) || PAGES[PAGES.length - 1];
}

let navNode = null;

function router() {
  const hash = window.location.hash || ROUTES.HOME;
  const page = resolve(hash);

  if (!navNode) {
    navNode = createNav(hash);
  } else {
    // 활성 링크만 갱신
    clear(navNode);
    navNode.appendChild(createNav(hash));
  }

  const content = el('main', {});
  mount(app, [navNode, content, createFooter()]);

  page.render(content);

  window.scrollTo({ top: 0, behavior: 'instant' });
}

window.addEventListener('hashchange', router);
router();
