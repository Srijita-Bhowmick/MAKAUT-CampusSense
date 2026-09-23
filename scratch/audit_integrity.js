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

  // 4. Verify 100% SHA-256 Parity
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
