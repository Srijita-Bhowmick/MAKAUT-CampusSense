// Dynamic Year-Based Subject-Wise PYQ Filtering & Rendering Engine
let currentYear = 'ALL';

const YEAR_LABELS = {
  '1': '1st Year',
  '2': '2nd Year',
  '3': '3rd Year',
  '4': '4th Year'
};

function setYearFilter(year, element) {
  document.querySelectorAll('.pyq-year-chip').forEach(el => el.classList.remove('active'));
  if (element) element.classList.add('active');
  currentYear = String(year || 'ALL');
  renderFilteredPYQs();
}

function getAcademicYear(item) {
  if (item.year) return String(item.year);
  const s = parseInt(item.sem, 10);
  if (s <= 2) return '1';
  if (s <= 4) return '2';
  if (s <= 6) return '3';
  return '4';
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
    const itemYear = getAcademicYear(item);
    const yearLabel = (YEAR_LABELS[itemYear] || '').toLowerCase();
    const matchesYear = (currentYear === 'ALL' || itemYear === currentYear);
    const matchesQuery = !query ||
      item.title.toLowerCase().includes(query) ||
      item.code.toLowerCase().includes(query) ||
      item.dept.toLowerCase().includes(query) ||
      yearLabel.includes(query) ||
      `sem ${item.sem}`.includes(query);
    return matchesYear && matchesQuery;
  });

  if (matches.length === 0) {
    grid.innerHTML = '';
    if (noResults) noResults.style.display = 'block';
    return;
  }

  if (noResults) noResults.style.display = 'none';
  grid.innerHTML = matches.map(item => {
    const itemYear = getAcademicYear(item);
    const yearLabel = YEAR_LABELS[itemYear] || `Year ${itemYear}`;
    const safeHref = encodeURI(resolveCatalogPdfUrl(item.path));
    const dlName = `MAKAUT_${item.code.replace(/[^a-zA-Z0-9_-]/g, '_')}_PYQ.pdf`;
    const safeJsTitle = escapeJsStr(`${item.code}: ${item.title} PYQ`);
    const safeJsPath = escapeJsStr(item.path);
    return `
      <div class="pyq-card" data-year="${itemYear}" data-semester="${item.sem}">
        <div>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.45rem;">
            <span class="subject-badge">${item.code}</span>
            <span style="font-size: 0.75rem; color: #64748B; font-weight: 600;">${yearLabel} · Sem ${item.sem}</span>
          </div>
          <h4>${item.title}</h4>
          <p>Verified Archive Series: ${item.years}</p>
        </div>
        <div class="card-actions">
          <a href="${safeHref}" target="_blank" onclick="event.preventDefault(); if (typeof openLandingPdfPreview === 'function') { openLandingPdfPreview('${safeJsTitle}', '${safeJsPath}'); } else { window.open('${safeHref}', '_blank'); }" class="btn-preview">Preview</a>
          <a href="${safeHref}" download="${dlName}" onclick="event.preventDefault(); event.stopPropagation(); if (typeof downloadLandingSyllabus === 'function') { downloadLandingSyllabus('${safeJsPath}', '${dlName}'); }" class="btn-download">Download</a>
        </div>
      </div>
    `;
  }).join('');
}

window.setYearFilter = setYearFilter;
window.renderFilteredPYQs = renderFilteredPYQs;

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', renderFilteredPYQs);
} else {
  renderFilteredPYQs();
}
