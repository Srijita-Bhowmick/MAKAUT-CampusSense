// Dynamic Subject-Wise PYQ Filtering & Rendering Engine
let currentDept = 'ALL';
let currentSem = 'ALL';

function setDeptFilter(dept, element) {
  document.querySelectorAll('.pyq-dept-chip').forEach(el => el.classList.remove('active'));
  if (element) element.classList.add('active');
  currentDept = dept;
  renderFilteredPYQs();
}

function setSemFilter(sem, element) {
  document.querySelectorAll('.pyq-sem-chip').forEach(el => el.classList.remove('active'));
  if (element) element.classList.add('active');
  currentSem = sem;
  renderFilteredPYQs();
}

function resolveCatalogPdfUrl(rawPath) {
  if (typeof window.resolveAppUrl === 'function') {
    return window.resolveAppUrl(rawPath);
  }
  const clean = (rawPath || '').trim().replace(/^\/+/, '').replace(/^MAKAUT-CampusSense\//i, '');
  const isGhPages = typeof window !== 'undefined' && window.location.pathname.toLowerCase().includes('makaut-campussense');
  return (isGhPages ? '/MAKAUT-CampusSense/' : '') + clean;
}

function escapeJsStr(str) {
  return String(str || '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function renderFilteredPYQs() {
  const query = (document.getElementById('pyqSearchInput')?.value || '').toLowerCase().trim();
  const grid = document.getElementById('dynamicPyqGrid');
  const noResults = document.getElementById('noPyqResults');
  const catalog = (typeof PYQ_CATALOG !== 'undefined' ? PYQ_CATALOG : (window.PYQ_CATALOG || []));
  if (!grid) return;

  const matches = catalog.filter(item => {
    const matchesDept = (currentDept === 'ALL' || item.dept === currentDept);
    const matchesSem = (currentSem === 'ALL' || item.sem === currentSem);
    const matchesQuery = !query ||
      item.title.toLowerCase().includes(query) ||
      item.code.toLowerCase().includes(query) ||
      item.dept.toLowerCase().includes(query);
    return matchesDept && matchesSem && matchesQuery;
  });

  if (matches.length === 0) {
    grid.innerHTML = '';
    if (noResults) noResults.style.display = 'block';
    return;
  }

  if (noResults) noResults.style.display = 'none';
  grid.innerHTML = matches.map(item => {
    const safeHref = encodeURI(resolveCatalogPdfUrl(item.path));
    const dlName = `MAKAUT_${item.code.replace(/[^a-zA-Z0-9_-]/g, '_')}_PYQ.pdf`;
    const safeJsTitle = escapeJsStr(`${item.code}: ${item.title} PYQ`);
    const safeJsPath = escapeJsStr(item.path);
    return `
      <div class="pyq-card" data-semester="${item.sem}" data-dept="${item.dept}">
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
            <span class="subject-badge">${item.code}</span>
            <span style="font-size: 0.75rem; color: #64748B; font-weight: 600;">${item.dept} · Sem ${item.sem}</span>
          </div>
          <h4 style="font-size: 1.15rem; font-weight: 700; color: #0F172A; margin: 0 0 0.4rem 0;">${item.title}</h4>
          <p style="font-size: 0.85rem; color: #475569; margin: 0; line-height: 1.4;">
            Verified Archive Series: ${item.years}
          </p>
        </div>
        <div class="card-actions">
          <a href="${safeHref}" target="_blank" onclick="event.preventDefault(); if (typeof openLandingPdfPreview === 'function') { openLandingPdfPreview('${safeJsTitle}', '${safeJsPath}'); } else { window.open('${safeHref}', '_blank'); }" class="btn-preview">Preview</a>
          <a href="${safeHref}" download="${dlName}" onclick="event.preventDefault(); event.stopPropagation(); if (typeof downloadLandingSyllabus === 'function') { downloadLandingSyllabus('${safeJsPath}', '${dlName}'); }" class="btn-download">Download</a>
        </div>
      </div>
    `;
  }).join('');
}

window.setDeptFilter = setDeptFilter;
window.setSemFilter = setSemFilter;
window.renderFilteredPYQs = renderFilteredPYQs;

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', renderFilteredPYQs);
} else {
  renderFilteredPYQs();
}
