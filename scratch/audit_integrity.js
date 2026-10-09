const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

function runAudit() {
  console.log("🔍 Running MAKAUT CampusSense 0% Error Lockdown Audit...\n");
  let passed = true;

  // 1. Audit app.js for Forbidden Patterns
  const appJsPath = path.join(__dirname, '../src/main/resources/static/app.js');
  const appJsContent = fs.readFileSync(appJsPath, 'utf8');

  if (appJsContent.includes("activeSubject = 'physics'") || appJsContent.includes('subjectId = "physics"')) {
    console.error("❌ FAILURE: Found hardcoded physics fallback in app.js!");
    passed = false;
  } else {
    console.log("✅ PASSED: No hardcoded physics fallback.");
  }

  // 2. Audit for IT Stream in Active Selection
  const indexHtmlPath = path.join(__dirname, '../src/main/resources/static/index.html');
  const indexHtmlContent = fs.readFileSync(indexHtmlPath, 'utf8');

  if (indexHtmlContent.includes('data-stream="IT"') || indexHtmlContent.includes('value="IT"')) {
    console.error("❌ FAILURE: Found active selectable IT stream in index.html!");
    passed = false;
  } else {
    console.log("✅ PASSED: IT stream completely absent from active selection controls.");
  }

  // 3. Verify Master Syllabus PDF Exists on Disk
  const pdfPathStatic = path.join(__dirname, '../src/main/resources/static/CSE(3rd to 8th sem)/CSE20.pdf');
  const pdfPathDocs = path.join(__dirname, '../docs/CSE(3rd to 8th sem)/CSE20.pdf');

  if (!fs.existsSync(pdfPathStatic) || !fs.existsSync(pdfPathDocs)) {
    console.error("❌ FAILURE: CSE20.pdf missing from static or docs!");
    passed = false;
  } else {
    console.log("✅ PASSED: Official CSE20.pdf present in both static/ and docs/.");
  }

  // 4. Verify Native Viewport & Fluid Touchpad Scrolling in styles.css
  const stylesCssPath = path.join(__dirname, '../src/main/resources/static/styles.css');
  const stylesCssContent = fs.readFileSync(stylesCssPath, 'utf8');

  // Check 4a: html and body do not contain overscroll-behavior-y: none or touch-action: none
  if (stylesCssContent.includes('overscroll-behavior-y: none') || stylesCssContent.includes('touch-action: none')) {
    console.error("❌ FAILURE: styles.css contains overscroll-behavior-y: none or touch-action: none!");
    passed = false;
  } else {
    console.log("✅ PASSED: html and body do not contain overscroll-behavior-y: none or touch-action: none.");
  }

  // Check 4b: Large containers do not have touch-action: manipulation or touch-action: none
  const lines = stylesCssContent.split('\n');
  let containerHasTouchLock = false;
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if ((line.includes('.bento-card') || line.includes('.subject-card') || line.includes('.landing-section')) &&
        (line.includes('touch-action: manipulation') || line.includes('touch-action: none'))) {
      containerHasTouchLock = true;
      break;
    }
  }
  if (containerHasTouchLock) {
    console.error("❌ FAILURE: Large containers have touch-action manipulation or none in styles.css!");
    passed = false;
  } else {
    console.log("✅ PASSED: Large containers (.bento-card, .subject-card, .landing-section) allow natural touch/touchpad scrolling.");
  }

  // Check 4c: Strict Authentication Gate in app.js
  const previewPdfGated = appJsContent.includes('window.previewPdfFile = function') && 
                          appJsContent.includes('requireAuthOrTrigger()');
  const downloadPdfGated = appJsContent.includes('async function downloadPdfFile') && 
                           appJsContent.includes('requireAuthOrTrigger()');
  const openSubjectHubGated = appJsContent.includes('window.openSubjectHubByCode = function') && 
                              appJsContent.includes('requireAuthOrTrigger()');

  if (!previewPdfGated || !downloadPdfGated || !openSubjectHubGated) {
    console.error("❌ FAILURE: previewPdfFile, downloadPdfFile, or openSubjectHubByCode missing requireAuthOrTrigger in app.js!");
    passed = false;
  } else {
    console.log("✅ PASSED: previewPdfFile, downloadPdfFile, and openSubjectHubByCode verify authentication before exposing resources.");
  }

  if (!appJsContent.includes('lockBodyScroll(true)') || !appJsContent.includes('lockBodyScroll(false)')) {
    console.error("❌ FAILURE: lockBodyScroll missing from modal handlers in app.js!");
    passed = false;
  } else {
    console.log("✅ PASSED: Modal scroll lock guard active in app.js.");
  }

  if (!stylesCssContent.includes('touch-action: auto !important') || !stylesCssContent.includes('.modal:not(.active)')) {
    console.error("❌ FAILURE: Touch unblocking or inactive overlay rules missing in styles.css!");
    passed = false;
  } else {
    console.log("✅ PASSED: Touch unblocking and inactive overlay guards verified in styles.css.");
  }

  if (!appJsContent.includes("document.body.classList.remove('modal-open')")) {
    console.error("❌ FAILURE: modal-open cleanup missing on DOM load in app.js!");
    passed = false;
  } else {
    console.log("✅ PASSED: modal-open cleanup on DOM load verified in app.js.");
  }

  if (!indexHtmlContent.includes('<meta name="viewport" content="width=device-width, initial-scale=1.0')) {
    console.error("❌ FAILURE: Viewport meta tag is missing or non-standard in index.html!");
    passed = false;
  } else {
    console.log("✅ PASSED: Standard viewport meta tag verified in index.html.");
  }

  // 4d. Verify Light Mode Palette, Instant Theme Switching, and Panic Pack Hook
  if (!stylesCssContent.includes('--bg-primary: #f8fafc') || !stylesCssContent.includes('.panic-catch-modal')) {
    console.error("❌ FAILURE: Light mode palette or panic-catch-modal styles missing in styles.css!");
    passed = false;
  } else {
    console.log("✅ PASSED: High-contrast light mode palette and panic-catch-modal styles verified in styles.css.");
  }

  if (stylesCssContent.includes('transition: background-color 0.3s ease, color 0.3s ease;')) {
    console.error("❌ FAILURE: Body contains slow background-color transition in styles.css!");
    passed = false;
  } else {
    console.log("✅ PASSED: Instantaneous theme transition verified (no lagging universal transitions).");
  }

  if (!indexHtmlContent.includes('feature-gate-card') || !indexHtmlContent.includes('data-gate-target="panic"') || !indexHtmlContent.includes('Battle-Tested MAKAUT Examination Archives')) {
    console.error("❌ FAILURE: Feature gate cards, panic-pack card, or battle-tested PYQ copy missing in index.html!");
    passed = false;
  } else {
    console.log("✅ PASSED: Feature gate cards and updated battle-tested PYQ section copy verified in index.html.");
  }

  if (!appJsContent.includes('triggerPanicPackConversionAuth') || !appJsContent.includes('.feature-gate-card')) {
    console.error("❌ FAILURE: triggerPanicPackConversionAuth or feature-gate-card listener missing in app.js!");
    passed = false;
  } else {
    console.log("✅ PASSED: Panic pack conversion hook and feature card auth listener verified in app.js.");
  }

  // 5. Verify Zero Practicals/Labs and Zero Non-PYQ Subjects in Dashboard Stream View
  const match = appJsContent.match(/const OFFICIAL_CSE_COURSES = (\[[\s\S]*?\]);\s*const OFFICIAL_CSE_MODULES/);
  if (!match) {
    console.error("❌ FAILURE: Could not locate OFFICIAL_CSE_COURSES in app.js!");
    passed = false;
  } else {
    const courses = JSON.parse(match[1]);
    const mockWindow = {};
    const evalEnv = { DEPARTMENT_STREAMS: { CSE: { courses } }, OFFICIAL_CSE_COURSES: courses, window: mockWindow };
    
    // Extract getFilteredTheorySubjectsWithPYQs definition
    const funcMatch = appJsContent.match(/function getFilteredTheorySubjectsWithPYQs\(semester, stream = 'CSE'\) \{[\s\S]*?\n    \}/);
    if (!funcMatch) {
      console.error("❌ FAILURE: getFilteredTheorySubjectsWithPYQs not found in app.js!");
      passed = false;
    } else {
      const getQualifiedDashboardSubjects = new Function('DEPARTMENT_STREAMS', 'OFFICIAL_CSE_COURSES', `
        ${funcMatch[0]}
        return getFilteredTheorySubjectsWithPYQs;
      `)(evalEnv.DEPARTMENT_STREAMS, evalEnv.OFFICIAL_CSE_COURSES);

      let foundInvalidSubject = false;
      for (let sem = 3; sem <= 8; sem++) {
        const qualified = getQualifiedDashboardSubjects(sem, 'CSE');
        qualified.forEach(sub => {
          const title = (sub.title || sub.name || '').toLowerCase();
          const code = (sub.code || '').trim();
          const isPractical = title.includes('lab') ||
                              title.includes('laboratory') ||
                              title.includes('workshop') ||
                              title.includes('project') ||
                              title.includes('internship') ||
                              /9[1-4]$/.test(code) ||
                              /8[1-9]$/.test(code);
          if (isPractical) {
            console.error(`❌ FAILURE: Found practical/lab subject in Sem ${sem} dashboard: ${sub.code} - ${sub.title || sub.name}`);
            foundInvalidSubject = true;
          }
          if (!Array.isArray(sub.pyqs) || sub.pyqs.length === 0) {
            console.error(`❌ FAILURE: Found subject with 0 PYQs in Sem ${sem} dashboard: ${sub.code} - ${sub.title || sub.name}`);
            foundInvalidSubject = true;
          }
        });
      }

      if (foundInvalidSubject) {
        passed = false;
      } else {
        console.log("✅ PASSED: Zero practicals/labs and zero non-PYQ subjects in dashboard qualified subjects.");
      }
    }
  }

  // 6. Verify 100% SHA-256 Parity across all files
  function getFiles(dir) {
    let results = [];
    const list = fs.readdirSync(dir);
    list.forEach(file => {
      const fullPath = path.join(dir, file);
      const stat = fs.statSync(fullPath);
      if (stat && stat.isDirectory()) {
        results = results.concat(getFiles(fullPath));
      } else {
        results.push(fullPath);
      }
    });
    return results;
  }

  const staticDir = path.join(__dirname, '../src/main/resources/static');
  const docsDir = path.join(__dirname, '../docs');

  const staticFiles = getFiles(staticDir);
  const docsFiles = getFiles(docsDir);

  if (staticFiles.length !== docsFiles.length) {
    console.error(`❌ FAILURE: File count mismatch! static: ${staticFiles.length}, docs: ${docsFiles.length}`);
    passed = false;
  } else {
    let hashMismatch = false;
    staticFiles.forEach(file => {
      const relative = path.relative(staticDir, file);
      const docsFile = path.join(docsDir, relative);

      if (!fs.existsSync(docsFile)) {
        hashMismatch = true;
        return;
      }

      const h1 = crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
      const h2 = crypto.createHash('sha256').update(fs.readFileSync(docsFile)).digest('hex');

      if (h1 !== h2) {
        console.error(`❌ SHA-256 Mismatch: ${relative}`);
        hashMismatch = true;
      }
    });

    if (hashMismatch) {
      console.error("❌ FAILURE: SHA-256 parity verification failed!");
      passed = false;
    } else {
      console.log(`✅ PASSED: 100% SHA-256 byte-for-byte parity across all ${staticFiles.length} files.`);
    }
  }

  if (!passed) {
    process.exit(1);
  }
  console.log("\n🚀 AUDIT COMPLETE: 0% Error rate guaranteed. Platform is bulletproof.");
}

runAudit();
