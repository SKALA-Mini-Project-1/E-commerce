// 원래 API URL 그대로 사용 (브라우저에서 직접 호출)
const API_URL = 'https://jsonplaceholder.typicode.com/posts';
const ROWS_PER_PAGE = 10;

const $flexList = document.getElementById('flex-list');
const $cardList = document.getElementById('card-list');
const $viewSelect = document.getElementById('view-select');
const $pagination = document.getElementById('pagination');
const $loader = document.getElementById('loader');
const $error = document.getElementById('error');
const $prevFloating = document.getElementById('prev-floating');
const $nextFloating = document.getElementById('next-floating');

let posts = [];
let currentPage = 1;
let currentView = 'flex';

init();

function init() {
  bindEvents();
  showLoader(true);

  // CORS 이슈 해결을 위한 fetch 옵션 설정
  fetch(API_URL, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    }
  })
    .then(res => {
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      return res.json();
    })
    .then(data => {
      posts = data; // 100개 제공됨
      render();
    })
    .catch(err => showError(err))
    .finally(() => showLoader(false));
}

function bindEvents() {
  $viewSelect.addEventListener('change', e => {
    currentView = e.target.value;
    render();
  });

  $prevFloating.addEventListener('click', () => gotoPage(currentPage - 1));
  $nextFloating.addEventListener('click', () => gotoPage(currentPage + 1));

  window.addEventListener('resize', updateFloatingVisibility);
}

function gotoPage(page) {
  const totalPages = Math.ceil(posts.length / ROWS_PER_PAGE) || 1;
  if (page < 1 || page > totalPages) return;
  currentPage = page;
  render();
}

function render() {
  renderList();
  renderPagination();
  updateFloatingVisibility();
}

function renderList() {
  const start = (currentPage - 1) * ROWS_PER_PAGE;
  const end = start + ROWS_PER_PAGE;
  const pageItems = posts.slice(start, end);

  // reset
  $flexList.classList.add('hidden');
  $cardList.classList.add('hidden');

  if (currentView === 'flex') {
    $flexList.innerHTML = pageItems.map(p => flexItemTemplate(p)).join('');
    $flexList.classList.remove('hidden');
  } else {
    $cardList.innerHTML = pageItems.map(p => cardItemTemplate(p)).join('');
    $cardList.classList.remove('hidden');
  }
}

function renderPagination() {
  const totalPages = Math.ceil(posts.length / ROWS_PER_PAGE) || 1;
  if (totalPages <= 1) {
    $pagination.innerHTML = '';
    return;
  }

  const MAX_PAGE_BUTTONS = 10;
  const group = Math.floor((currentPage - 1) / MAX_PAGE_BUTTONS);
  const start = group * MAX_PAGE_BUTTONS + 1;
  const end = Math.min(start + MAX_PAGE_BUTTONS - 1, totalPages);

  const buttons = [];
  if (start > 1) buttons.push(createPageButton('«', 1));
  if (currentPage > 1) buttons.push(createPageButton('‹', currentPage - 1));

  for (let i = start; i <= end; i++) {
    buttons.push(createPageButton(i, i, false, i === currentPage));
  }

  if (currentPage < totalPages) buttons.push(createPageButton('›', currentPage + 1));
  if (end < totalPages) buttons.push(createPageButton('»', totalPages));

  $pagination.innerHTML = '';
  buttons.forEach(b => $pagination.appendChild(b));
}

function updateFloatingVisibility() {
  const totalPages = Math.ceil(posts.length / ROWS_PER_PAGE) || 1;
  const needPaging = totalPages > 1 || document.body.scrollHeight > window.innerHeight;

  if (!needPaging) {
    $prevFloating.classList.add('hidden');
    $nextFloating.classList.add('hidden');
    return;
  }

  $prevFloating.classList.toggle('hidden', currentPage === 1);
  $nextFloating.classList.toggle('hidden', currentPage === totalPages);
}

function createPageButton(text, page, disabled = false, active = false) {
  const btn = document.createElement('button');
  btn.textContent = text;
  btn.disabled = disabled;
  if (active) btn.classList.add('active');
  btn.addEventListener('click', () => gotoPage(page));
  return btn;
}

function flexItemTemplate(p) {
  return `
    <article class="post-row">
      <div class="post-row__meta">#${p.id} · user ${p.userId}</div>
      <h2 class="post-row__title">${escapeHtml(p.title)}</h2>
      <p class="post-row__body">${escapeHtml(p.body)}</p>
    </article>
  `;
}

function cardItemTemplate(p) {
  return `
    <article class="post-card">
      <div class="post-card__meta">#${p.id} · user ${p.userId}</div>
      <h2 class="post-card__title">${escapeHtml(p.title)}</h2>
      <p class="post-card__body">${escapeHtml(p.body)}</p>
    </article>
  `;
}

function showLoader(show) {
  $loader.classList.toggle('hidden', !show);
}

function showError(err) {
  console.error(err);
  $error.textContent = `데이터를 불러오는 중 오류가 발생했습니다: ${err.message}`;
  $error.classList.remove('hidden');
}

function escapeHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
