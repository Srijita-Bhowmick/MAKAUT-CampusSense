// MAKAUT CampusSense Frontend Engine
document.addEventListener('DOMContentLoaded', () => {
    // 1. Session Management
    let currentSessionId = localStorage.getItem('campussense_session_id');
    if (!currentSessionId) {
        currentSessionId = crypto.randomUUID ? crypto.randomUUID() : 'cs-' + Date.now();
        localStorage.setItem('campussense_session_id', currentSessionId);
    }
    const displaySessionEl = document.getElementById('display-session-id');
    if (displaySessionEl) {
        displaySessionEl.textContent = currentSessionId;
    }

    const btnNewSession = document.getElementById('btn-new-session');
    if (btnNewSession) {
        btnNewSession.addEventListener('click', () => {
            currentSessionId = crypto.randomUUID ? crypto.randomUUID() : 'cs-' + Date.now();
            localStorage.setItem('campussense_session_id', currentSessionId);
            displaySessionEl.textContent = currentSessionId;
            const chatMessages = document.getElementById('chat-messages');
            chatMessages.innerHTML = `
                <div class="message assistant welcome">
                    <div class="msg-avatar">🎓</div>
                    <div class="msg-body">
                        <div class="msg-header">
                            <span class="sender-name">MAKAUT CampusSense AI</span>
                            <span class="badge verified-badge">New Session</span>
                        </div>
                        <div class="msg-text">Started a new conversation session. How can I assist you with MAKAUT curriculum, MAR points, or college placements?</div>
                    </div>
                </div>
            `;
        });
    }

    // 2. Tab Navigation
    const tabButtons = document.querySelectorAll('.tab-btn');
    const viewSections = document.querySelectorAll('.view-section');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => b.classList.remove('active'));
            viewSections.forEach(s => s.classList.remove('active'));

            btn.classList.add('active');
            const targetViewId = btn.getAttribute('data-tab');
            const targetSection = document.getElementById(targetViewId);
            if (targetSection) {
                targetSection.classList.add('active');
            }

            if (targetViewId === 'resources-view') {
                loadResources();
            } else if (targetViewId === 'placements-view') {
                loadPlacements();
            }
        });
    });

    // 3. Quick Prompt Chips
    document.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            const query = chip.getAttribute('data-query');
            const queryInput = document.getElementById('query-input');
            if (queryInput) {
                queryInput.value = query;
                document.getElementById('chat-form').dispatchEvent(new Event('submit'));
            }
        });
    });

    // 4. Chat Submission Handler
    const chatForm = document.getElementById('chat-form');
    const queryInput = document.getElementById('query-input');
    const chatMessages = document.getElementById('chat-messages');
    const btnSend = document.getElementById('btn-send');

    chatForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const query = queryInput.value.trim();
        if (!query) return;

        const stream = document.getElementById('filter-stream').value || null;
        const semVal = document.getElementById('filter-semester').value;
        const semester = semVal ? parseInt(semVal, 10) : null;

        // Append User Message
        appendMessage('user', query);
        queryInput.value = '';

        // Disable button & show typing indicator
        btnSend.disabled = true;
        const typingEl = document.createElement('div');
        typingEl.className = 'message assistant';
        typingEl.id = 'typing-indicator';
        typingEl.innerHTML = `
            <div class="msg-avatar">🎓</div>
            <div class="msg-body">
                <div class="msg-header">
                    <span class="sender-name">MAKAUT CampusSense AI</span>
                    <span class="badge">Searching Syllabus & Vectors...</span>
                </div>
                <div class="msg-text">Analyzing MAKAUT curriculum and computing vector similarity...</div>
            </div>
        `;
        chatMessages.appendChild(typingEl);
        chatMessages.scrollTop = chatMessages.scrollHeight;

        try {
            const res = await fetch('/api/v1/chat/query', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    sessionId: currentSessionId,
                    query: query,
                    stream: stream,
                    semester: semester
                })
            });

            const data = await res.json();
            typingEl.remove();

            if (res.ok) {
                appendAssistantResponse(data);
            } else {
                appendMessage('assistant', `⚠️ Error: ${data.message || 'Unable to process query'}`);
            }
        } catch (err) {
            typingEl.remove();
            appendMessage('assistant', `⚠️ Connection Error: Unable to reach MAKAUT CampusSense backend (${err.message}).`);
        } finally {
            btnSend.disabled = false;
        }
    });

    function appendMessage(role, text) {
        const msg = document.createElement('div');
        msg.className = `message ${role}`;
        msg.innerHTML = `
            <div class="msg-avatar">${role === 'user' ? '👤' : '🎓'}</div>
            <div class="msg-body">
                <div class="msg-header">
                    <span class="sender-name">${role === 'user' ? 'You' : 'MAKAUT CampusSense AI'}</span>
                </div>
                <div class="msg-text">${escapeHtml(text)}</div>
            </div>
        `;
        chatMessages.appendChild(msg);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function appendAssistantResponse(data) {
        const msg = document.createElement('div');
        msg.className = 'message assistant';

        let sourcesHtml = '';
        if (data.sources && data.sources.length > 0) {
            sourcesHtml = `
                <div class="msg-sources">
                    <div class="sources-title">Verified MAKAUT Citations:</div>
                    <div class="source-badges">
                        ${data.sources.map(s => `
                            <a href="${s.fileUrl || '#'}" target="_blank" class="source-pill" title="Similarity: ${(s.similarity * 100).toFixed(1)}%">
                                📄 ${escapeHtml(s.subjectCode)} - ${escapeHtml(s.moduleOrSection)}
                                <span style="opacity: 0.75;">(${(s.similarity * 100).toFixed(0)}%)</span>
                            </a>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        const verifiedBadge = data.verified
            ? `<span class="badge verified-badge">✓ Verified (${data.executionTimeMs}ms)</span>`
            : `<span class="badge unverified-badge">⚠ Unverified Guardrail</span>`;

        msg.innerHTML = `
            <div class="msg-avatar">🎓</div>
            <div class="msg-body">
                <div class="msg-header">
                    <span class="sender-name">MAKAUT CampusSense AI</span>
                    <span class="badge" style="background: rgba(99, 102, 241, 0.2); color: #a5b4fc;">${escapeHtml(data.detectedIntent)}</span>
                    ${verifiedBadge}
                </div>
                <div class="msg-text">${formatMarkdown(data.response)}</div>
                ${sourcesHtml}
            </div>
        `;

        chatMessages.appendChild(msg);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // 5. Resources Loader
    async function loadResources() {
        const grid = document.getElementById('resources-grid');
        const stream = document.getElementById('res-stream-select').value;
        const semester = document.getElementById('res-sem-select').value;
        const type = document.getElementById('res-type-select').value;
        const query = document.getElementById('res-search-input').value.trim();

        grid.innerHTML = '<div class="loading-spinner">Loading resources...</div>';

        const params = new URLSearchParams();
        if (stream) params.append('stream', stream);
        if (semester) params.append('semester', semester);
        if (type) params.append('type', type);
        if (query) params.append('subjectCode', query);

        try {
            const res = await fetch(`/api/v1/resources/search?${params.toString()}`);
            const data = await res.json();
            const resources = data.content || [];

            if (resources.length === 0) {
                grid.innerHTML = '<div class="empty-state">No matching curriculum documents found.</div>';
                return;
            }

            grid.innerHTML = resources.map(r => `
                <div class="resource-card">
                    <div class="card-top">
                        <span class="res-code">${escapeHtml(r.subjectCode)}</span>
                        <span class="res-type-badge">${escapeHtml(r.resourceType)}</span>
                    </div>
                    <div class="res-title">${escapeHtml(r.subjectName)}</div>
                    <div class="res-desc">${escapeHtml(r.rawText ? r.rawText.substring(0, 180) + '...' : 'Official university document')}</div>
                    <div class="res-footer">
                        <span>Sem: ${r.semester > 0 ? r.semester : 'General'} • Stream: ${escapeHtml(r.stream)}</span>
                        <a href="${r.fileUrl || '#'}" target="_blank" class="nav-link" style="padding: 0.2rem 0.5rem; font-size: 0.72rem;">View PDF</a>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            grid.innerHTML = `<div class="error-msg">Failed to load resources: ${e.message}</div>`;
        }
    }

    const btnFilter = document.getElementById('btn-filter-resources');
    if (btnFilter) {
        btnFilter.addEventListener('click', loadResources);
    }

    // 6. Placements Loader
    async function loadPlacements() {
        const collegeId = document.getElementById('placement-college-select').value;
        const year = document.getElementById('placement-year-select').value;

        const params = new URLSearchParams();
        if (collegeId) params.append('collegeId', collegeId);
        if (year) params.append('batchYear', year);

        try {
            const res = await fetch(`/api/v1/placements?${params.toString()}`);
            const data = await res.json();

            // Populate metrics
            document.getElementById('metric-highest').textContent = `${data.maxCtc || 0} LPA`;
            document.getElementById('metric-avg').textContent = `${data.avgCtc || 0} LPA`;
            document.getElementById('metric-offers').textContent = data.totalOffers || 0;
            document.getElementById('metric-companies').textContent = data.totalCompanies || 0;

            // Populate table
            const tbody = document.getElementById('placement-table-body');
            const records = data.records || [];

            if (records.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center">No placement records found for filter.</td></tr>';
                return;
            }

            tbody.innerHTML = records.map(r => `
                <tr>
                    <td><strong>${escapeHtml(r.college.name)}</strong></td>
                    <td><span class="badge" style="background: rgba(255,255,255,0.06);">${r.batchYear}</span></td>
                    <td>${escapeHtml(r.companyName)}</td>
                    <td style="color: #38bdf8; font-weight: 700;">${r.highestCtc ? r.highestCtc + ' LPA' : '--'}</td>
                    <td>${r.averageCtc ? r.averageCtc + ' LPA' : '--'}</td>
                    <td>${r.totalOffers || '--'}</td>
                    <td><a href="${r.sourceUrl || '#'}" target="_blank" style="color: #818cf8; text-decoration: none;">Report ↗</a></td>
                </tr>
            `).join('');

        } catch (e) {
            console.error('Error fetching placement stats:', e);
        }
    }

    const btnPlacements = document.getElementById('btn-load-placements');
    if (btnPlacements) {
        btnPlacements.addEventListener('click', loadPlacements);
    }

    // 7. Ingestion Form Handler
    const ingestForm = document.getElementById('ingest-text-form');
    if (ingestForm) {
        ingestForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btn-submit-ingest');
            const resultBox = document.getElementById('ingest-result');

            btn.disabled = true;
            btn.textContent = 'Ingesting & Computing Embeddings...';
            resultBox.style.display = 'none';

            const payload = {
                subjectCode: document.getElementById('ingest-code').value.trim(),
                subjectName: document.getElementById('ingest-name').value.trim(),
                stream: document.getElementById('ingest-stream').value,
                semester: parseInt(document.getElementById('ingest-semester').value, 10),
                text: document.getElementById('ingest-text').value.trim(),
                resourceType: 'SYLLABUS',
                year: 2024
            };

            try {
                const res = await fetch('/api/v1/admin/ingest/text', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const data = await res.json();
                resultBox.style.display = 'block';
                if (res.ok) {
                    resultBox.innerHTML = `✅ Successfully ingested resource #${data.resourceId} (${data.subjectCode}) into vector database. Generated ${data.chunkCount} sliding-window chunks with 1536-dim embeddings.`;
                    ingestForm.reset();
                } else {
                    resultBox.innerHTML = `⚠️ Ingestion Error: ${data.message}`;
                }
            } catch (err) {
                resultBox.style.display = 'block';
                resultBox.innerHTML = `⚠️ Network Error: ${err.message}`;
            } finally {
                btn.disabled = false;
                btn.textContent = '⚡ Ingest & Generate Embeddings';
            }
        });
    }

    // Helpers
    function escapeHtml(text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatMarkdown(text) {
        if (!text) return '';
        let escaped = escapeHtml(text);
        // Headers
        escaped = escaped.replace(/^### (.*$)/gim, '<h4 style="color: #e2e8f0; margin: 0.5rem 0 0.3rem;">$1</h4>');
        // Bold
        escaped = escaped.replace(/\*\*(.*?)\*\*/g, '<strong style="color: #f1f5f9;">$1</strong>');
        // Italic
        escaped = escaped.replace(/\*(.*?)\*/g, '<em>$1</em>');
        // Bullet list
        escaped = escaped.replace(/^- (.*$)/gim, '<div style="display: flex; gap: 0.5rem; margin: 0.2rem 0;"><span>•</span><span>$1</span></div>');
        return escaped;
    }
});
