# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Primary Audience:** MAKAUT (Maulana Abul Kalam Azad University of Technology) B.Tech engineering students across affiliated colleges (including IEM, Heritage Institute of Technology, Techno Main Salt Lake, Haldia Institute of Technology, and Netaji Subhash Engineering College).
- **Streams & Semesters:** 1st Year engineering foundation, along with core departmental branches: Computer Science & Engineering (CSE), Information Technology (IT), Computer Science & Business Systems (CSBS), Computer Science & Cyber Security (CSCS), Computer Science & Data Science (CSDS), and CSE (Artificial Intelligence & Machine Learning).
- **Core Situation & Job-to-be-Done:** Studying under tight semester deadlines and exam stress. Students need immediate, trustworthy access to official MAKAUT syllabi, authentic subject-wise past examination question papers (PYQs), focused "Panic Pack" revision drills, and verified degree compliance information for Mandatory Additional Requirements (MAR — 100 points) and MOOCs (SWAYAM/NPTEL — 20 credits for B.Tech Honours).

## Product Purpose

MAKAUT CampusSense is an authoritative academic aggregator and exam revision engine designed to eliminate academic uncertainty, exam panic, and unverified syllabus rumors for MAKAUT engineering students. It provides instant curriculum navigation, module-by-module question archives, and strict citation-grounded AI assistance. Success is achieved when a student can reliably retrieve accurate syllabus objectives, practice with authentic exam papers, and revise without encountering hallucinations or outdated regulations.

## Positioning

Unlike generic AI study chatbots or unstructured student drives, MAKAUT CampusSense guarantees strict domain grounding:
- **Zero-Hallucination Retrieval:** Every response from the conversational AI engine strictly cites official MAKAUT syllabus modules and verified exam years (e.g., `[Module 3: Inter-process Communication, PCC-CS501, MAKAUT 2022 Question Paper]`).
- **Negative Grounding Guardrail:** If an inquiry cannot be confirmed against verified curriculum documents, the engine explicitly declines with: *"This information is not verified by official MAKAUT curriculum data."*
- **Dual-Engine Architecture:** Operates with full-stack Spring Boot 3.3+ and PostgreSQL 16 `pgvector` for advanced retrieval, alongside an embedded client-side cosine similarity search engine enabling zero-dependency live distribution via GitHub Pages.

## Operating Context

- **Academic Cycles:** Mid-semester continuous evaluations and high-stakes end-semester university examinations.
- **Access Environments:** Mobile smartphones and laptops, often on low-bandwidth hostel connections or campus networks during transit and last-minute study sessions.
- **Degree Rituals & Milestones:** Earning 100 MAR points through certified extracurricular/co-curricular activities before graduation, and completing approved SWAYAM/NPTEL courses for the 20-credit B.Tech Honours degree.
- **Documents & Artifacts:** Official MAKAUT AICTE/CBCS syllabi PDFs, semester question paper archives, course codes (e.g., PCC-CS501, BSC-301), and verified college placement statistics.

## Capabilities and Constraints

- **Capabilities:**
  - Comprehensive resource directory with 1,474 indexed academic assets (15 official curriculum streams and 1,459 real past question papers).
  - Dual-mode conversational RAG with strict citation badges and cosine similarity scores.
  - Exam Panic Pack drills with targeted module-level revision guidance.
  - Academic context switcher (Stream • Semester) tailoring the UI to the student's active semester.
  - Placement statistics comparator across top MAKAUT-affiliated colleges.
- **Constraints:**
  - Strict grounding requirement: No speculative or unverified academic claims are permitted.
  - Dual-deployment parity: Core search, catalog browsing, and revision workflows must run seamlessly both on the full-stack Spring Boot server and on static GitHub Pages.
  - Open Decisions: Persistent cloud sync of personal revision notes and bookmarks beyond client-side storage is explicitly deferred to future milestones.

## Brand Commitments

- **Name:** MAKAUT CampusSense
- **Maintainer / Author:** Srijita Bhowmick
- **Voice & Tone:** Authoritative, reassuring, fast, academic, and student-centric.
- **Visual Identity:** Modern glassmorphic theme with deep dark-mode foundation (`#0b0f19`), vibrant indigo/cyan accents, and clean technical typography (Outfit, Plus Jakarta Sans, JetBrains Mono).

## Evidence on Hand

- `docs/resources.json` and `src/main/resources/static/resources.json`: Master index of 1,474 cataloged syllabi and past question papers with SHA256 checksums.
- `docs/` and `src/main/resources/static/`: Concrete directory structures housing genuine PDF syllabi and semester-wise PYQs across all supported engineering branches.
- `src/main/resources/db/migration/`: Flyway schema (`V1__init_schema.sql`) with `vector(1536)` HNSW indexes and verified seed data (`V2__seed_makaut_data.sql`).
- Absence Guardrail: The application possesses no administrative student records or university grading backdoors; future design and documentation must never fabricate private student data.

## Product Principles

1. **Uncompromising Grounding:** Never invent syllabus modules, exam patterns, or university rules. If official MAKAUT curriculum data does not verify it, state it plainly.
2. **Instant Relief Under Stress:** In high-anxiety study situations, students need relevant question papers and module breakdowns within two clicks. Eliminate clutter and friction.
3. **Dual-Surface Parity:** The standalone GitHub Pages web distribution must offer rich, dependable academic exploration and retrieval even when disconnected from the Spring Boot API.
4. **Actionable Structural Clarity:** Always anchor content to clear academic coordinates: Branch, Semester, Subject Code, Module Name, and Exam Year.

## Accessibility & Inclusion

- Responsive design optimized for mobile viewports (320px to 1440px+).
- Support for both dark and light modes with high contrast ratios for extended night-time reading.
- Accessible search and filter controls with ARIA labels and live status announcements.
