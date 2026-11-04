package com.makaut.campussense.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/api/resources")
@Tag(name = "Dynamic Resource Catalog", description = "Dynamic file scanning and real-time catalog discovery for 1st Year and 3rd to 8th Semester PYQs and Syllabi")
@SuppressWarnings("null")
public class ResourceCatalogController {

    private final AtomicReference<Map<String, Object>> cachedCatalog = new AtomicReference<>();
    private volatile long lastScanTimestamp = 0;

    private static final Map<String, String[]> FIRST_YEAR_CODES = Map.of(
            "bee", new String[]{"ES-EE101", "Basic Electrical Engineering"},
            "chemistry", new String[]{"BS-CH101", "Chemistry 1"},
            "english", new String[]{"HM-HU201", "English"},
            "mathematics_1a", new String[]{"BS-M101", "Mathematics 1A"},
            "mathematics_1b", new String[]{"BS-M102", "Mathematics 1B"},
            "mathematics_2a", new String[]{"BS-M201", "Mathematics 2A"},
            "mathematics_2b", new String[]{"BS-M202", "Mathematics 2B"},
            "physics", new String[]{"BS-PH101", "Physics 1"},
            "pps", new String[]{"ES-CS201", "Programming for Problem Solving"}
    );

    private static final Map<String, String> DEPT_NAMES = Map.of(
            "cse", "Computer Science & Engineering",
            "it", "Information Technology",
            "cse-aiml", "Computer Science and Artificial Intelligence and Machine Learning",
            "cscs", "Computer Science and Cyber Security",
            "csds", "Computer Science and Data Science",
            "csbs", "Computer Science and Business Systems"
    );

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("Initializing dynamic filesystem scanner for CampusSense academic documents...");
        scanAndRefreshCatalog();
    }

    @GetMapping
    @Operation(summary = "Get dynamic resource catalog", description = "Returns real-time indexed syllabi and PYQs from the docs directory")
    public ResponseEntity<Map<String, Object>> getCatalog(@RequestParam(required = false, defaultValue = "false") boolean force) {
        if (force || cachedCatalog.get() == null || (System.currentTimeMillis() - lastScanTimestamp > 30000)) {
            scanAndRefreshCatalog();
        }
        return ResponseEntity.ok(cachedCatalog.get());
    }

    @PostMapping("/rescan")
    @Operation(summary = "Force re-scan academic directories", description = "Scans docs/1st year and docs/3rd to 8th sem for newly dropped PYQs and updates catalog")
    public ResponseEntity<Map<String, Object>> forceRescan() {
        Map<String, Object> catalog = scanAndRefreshCatalog();
        return ResponseEntity.ok(catalog);
    }

    public synchronized Map<String, Object> scanAndRefreshCatalog() {
        List<Map<String, Object>> resources = new ArrayList<>();
        int sylIdx = 1;
        int pyqIdx = 1;

        Path docsPath = Paths.get("docs");
        if (!Files.exists(docsPath)) {
            docsPath = Paths.get("../docs");
        }

        if (Files.exists(docsPath)) {
            // 1. Scan 1st year syllabus
            Path firstYearSyl = docsPath.resolve("1st year").resolve("syllabus");
            if (Files.isDirectory(firstYearSyl)) {
                File[] files = firstYearSyl.toFile().listFiles((d, name) -> name.endsWith(".pdf"));
                if (files != null) {
                    Arrays.sort(files, Comparator.comparing(File::getName));
                    for (File f : files) {
                        String subName = f.getName().replace(".pdf", "");
                        String rel = "1st year/syllabus/" + f.getName();
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", "syl_" + sylIdx++);
                        item.put("stream", "FIRST_YEAR");
                        item.put("semester", 1);
                        item.put("subjectCode", subName);
                        item.put("subjectName", subName + " Official Syllabus");
                        item.put("resourceType", "SYLLABUS");
                        item.put("year", 2024);
                        item.put("title", "MAKAUT Official Syllabus - " + subName);
                        item.put("fileUrl", rel);
                        item.put("canonicalPath", rel);
                        item.put("fileSize", f.length());
                        item.put("sha256", getSha256(f));
                        resources.add(item);
                    }
                }
            }

            // 2. Scan 3rd to 8th sem department syllabi
            Path deptRoot = docsPath.resolve("3rd to 8th sem");
            if (Files.isDirectory(deptRoot)) {
                File[] depts = deptRoot.toFile().listFiles(File::isDirectory);
                if (depts != null) {
                    Arrays.sort(depts, Comparator.comparing(File::getName));
                    for (File dept : depts) {
                        File sylDir = new File(dept, "syllabus");
                        if (sylDir.isDirectory()) {
                            File[] sFiles = sylDir.listFiles((d, name) -> name.endsWith(".pdf"));
                            if (sFiles != null) {
                                Arrays.sort(sFiles, Comparator.comparing(File::getName));
                                for (File sf : sFiles) {
                                    String deptKey = dept.getName().toUpperCase();
                                    String rel = "3rd to 8th sem/" + dept.getName() + "/syllabus/" + sf.getName();
                                    Map<String, Object> item = new LinkedHashMap<>();
                                    item.put("id", "syl_" + sylIdx++);
                                    item.put("stream", deptKey);
                                    item.put("semester", 3);
                                    item.put("subjectCode", deptKey + "_Syllabus");
                                    item.put("subjectName", deptKey + " Official Syllabus (Semesters 3 to 8)");
                                    item.put("resourceType", "SYLLABUS");
                                    item.put("year", 2024);
                                    item.put("title", "MAKAUT Official Syllabus - " + DEPT_NAMES.getOrDefault(dept.getName(), deptKey));
                                    item.put("fileUrl", rel);
                                    item.put("canonicalPath", rel);
                                    item.put("fileSize", sf.length());
                                    item.put("sha256", getSha256(sf));
                                    resources.add(item);
                                }
                            }
                        }
                    }
                }
            }

            // 3. Scan 1st year PYQs
            Path firstYearPyq = docsPath.resolve("1st year").resolve("pyqs");
            if (Files.isDirectory(firstYearPyq)) {
                File[] subDirs = firstYearPyq.toFile().listFiles(File::isDirectory);
                if (subDirs != null) {
                    Arrays.sort(subDirs, Comparator.comparing(File::getName));
                    for (File sd : subDirs) {
                        String[] codeName = FIRST_YEAR_CODES.getOrDefault(sd.getName().toLowerCase(), new String[]{sd.getName().toUpperCase(), sd.getName()});
                        File[] pFiles = sd.listFiles((d, name) -> name.endsWith(".pdf"));
                        if (pFiles != null) {
                            Arrays.sort(pFiles, Comparator.comparing(File::getName));
                            for (File pf : pFiles) {
                                int year = extractYear(pf.getName());
                                int sem = extractSemester(pf.getName(), 1);
                                String rel = "1st year/pyqs/" + sd.getName() + "/" + pf.getName();
                                Map<String, Object> item = new LinkedHashMap<>();
                                item.put("id", "pyq_" + pyqIdx++);
                                item.put("stream", "FIRST_YEAR");
                                item.put("semester", sem);
                                item.put("subjectCode", codeName[0]);
                                item.put("subjectName", codeName[1]);
                                item.put("resourceType", "PYQ");
                                item.put("year", year);
                                item.put("title", cleanTitle(pf.getName()));
                                item.put("fileUrl", rel);
                                item.put("canonicalPath", rel);
                                item.put("fileSize", pf.length());
                                item.put("sha256", getSha256(pf));
                                resources.add(item);
                            }
                        }
                    }
                }
            }

            // 4. Scan 3rd to 8th sem PYQs
            if (Files.isDirectory(deptRoot)) {
                File[] depts = deptRoot.toFile().listFiles(File::isDirectory);
                if (depts != null) {
                    Arrays.sort(depts, Comparator.comparing(File::getName));
                    for (File dept : depts) {
                        File pyqDir = new File(dept, "pyqs");
                        if (!pyqDir.isDirectory()) continue;
                        String deptKey = dept.getName().toUpperCase();

                        File[] semDirs = pyqDir.listFiles(File::isDirectory);
                        if (semDirs == null) continue;
                        Arrays.sort(semDirs, Comparator.comparing(File::getName));

                        for (File semDir : semDirs) {
                            int semNum = extractNumber(semDir.getName(), 3);
                            File[] subDirs = semDir.listFiles(File::isDirectory);
                            if (subDirs == null) continue;
                            Arrays.sort(subDirs, Comparator.comparing(File::getName));

                            for (File subDir : subDirs) {
                                String[] codeName = parseSubjectFromFolder(subDir.getName());
                                File[] pFiles = subDir.listFiles((d, name) -> name.endsWith(".pdf"));
                                if (pFiles == null) continue;
                                Arrays.sort(pFiles, Comparator.comparing(File::getName));

                                for (File pf : pFiles) {
                                    int year = extractYear(pf.getName());
                                    int finalSem = extractSemester(pf.getName(), semNum);
                                    String rel = "3rd to 8th sem/" + dept.getName() + "/pyqs/" + semDir.getName() + "/" + subDir.getName() + "/" + pf.getName();

                                    Map<String, Object> item = new LinkedHashMap<>();
                                    item.put("id", "pyq_" + pyqIdx++);
                                    item.put("stream", deptKey);
                                    item.put("semester", finalSem);
                                    item.put("subjectCode", codeName[0]);
                                    item.put("subjectName", codeName[1]);
                                    item.put("resourceType", "PYQ");
                                    item.put("year", year);
                                    item.put("title", cleanTitle(pf.getName()));
                                    item.put("fileUrl", rel);
                                    item.put("canonicalPath", rel);
                                    item.put("fileSize", pf.length());
                                    item.put("sha256", getSha256(pf));
                                    resources.add(item);
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("syllabusCount", sylIdx - 1);
        summary.put("pyqCount", pyqIdx - 1);

        Map<String, Object> catalog = new LinkedHashMap<>();
        catalog.put("generatedAt", Instant.now().toString());
        catalog.put("version", "1.1.0");
        catalog.put("totalResources", resources.size());
        catalog.put("summary", summary);
        catalog.put("resources", resources);

        cachedCatalog.set(catalog);
        lastScanTimestamp = System.currentTimeMillis();
        log.info("Catalog scan completed. Indexed {} total resources ({} syllabi, {} pyqs)", resources.size(), sylIdx - 1, pyqIdx - 1);
        return catalog;
    }

    private static int extractYear(String filename) {
        Matcher m = Pattern.compile("-(20\\d\\d)\\.pdf$", Pattern.CASE_INSENSITIVE).matcher(filename);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return 2024;
    }

    private static int extractSemester(String filename, int defaultSem) {
        Matcher m = Pattern.compile("-([1-8])-sem-", Pattern.CASE_INSENSITIVE).matcher(filename);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return defaultSem;
    }

    private static int extractNumber(String name, int defaultVal) {
        Matcher m = Pattern.compile("([1-8])").matcher(name);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return defaultVal;
    }

    private static String[] parseSubjectFromFolder(String folderName) {
        Matcher m = Pattern.compile("^([A-Z0-9()]+(?:-[A-Z0-9()]+)*)_(.+)$").matcher(folderName);
        if (m.find()) {
            return new String[]{m.group(1).replace("_", " "), m.group(2).replace("_", " ")};
        }
        return new String[]{folderName.replace("_", " "), folderName.replace("_", " ")};
    }

    private static String cleanTitle(String filename) {
        String base = filename.replace(".pdf", "");
        String[] parts = base.split("[-_]+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private static String getSha256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file.toPath());
            byte[] hash = digest.digest(bytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
