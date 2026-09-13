# MAKAUT CampusSense 🎓

> **AI Academic & Career Aggregator for Maulana Abul Kalam Azad University of Technology (MAKAUT)**
> Built with Java 21, Spring Boot 3.3+, PostgreSQL with `pgvector`, Apache PDFBox 3.0.3, JSoup, and Dual-Mode Conversational RAG.

[![GitHub Pages](https://img.shields.io/badge/Live%20Demo-GitHub%20Pages-4f46e5?style=for-the-badge&logo=github)](https://srijita-bhowmick.github.io/MAKAUT-CampusSense/)
[![CI Build & Test](https://github.com/Srijita-Bhowmick/MAKAUT-CampusSense/actions/workflows/ci.yml/badge.svg)](https://github.com/Srijita-Bhowmick/MAKAUT-CampusSense/actions)
[![Deploy Pages](https://github.com/Srijita-Bhowmick/MAKAUT-CampusSense/actions/workflows/deploy-pages.yml/badge.svg)](https://github.com/Srijita-Bhowmick/MAKAUT-CampusSense/actions)

🌐 **Live Demo Website**: [https://srijita-bhowmick.github.io/MAKAUT-CampusSense/](https://srijita-bhowmick.github.io/MAKAUT-CampusSense/)

---

## 🌟 Key Highlights

1. **Domain-Grounded Academic Knowledge Engine**:
   - Curates and structures official MAKAUT syllabi (CBCS / AICTE 2018+ regulations) across B.Tech engineering streams (CSE, IT, ECE, EE, ME, CE).
   - Indexes past examination question papers (PYQs) and module structures.
   - Comprehensive guidelines for **MAR (Mandatory Additional Requirements - 100 points)** and **MOOCs (SWAYAM / NPTEL - 20 credits for Honours)**.
   - Verified placement statistics across top affiliated engineering colleges (IEM, Heritage, TMSL, HIT, NSEC).

2. **Dual-Mode Vector Database Architecture**:
   - **PostgreSQL 16 + `pgvector` Profile (`postgres`)**: Production-ready relational & vector store with Flyway schema migrations, `vector(1536)` columns, and **HNSW cosine index** (`vector_cosine_ops`) for sub-100ms retrieval.
   - **Embedded Dev Profile (`dev`)**: Zero-configuration in-memory H2 database with deterministic normalized 1536-dimensional float vector embeddings and cosine similarity engine. Enables immediate testing and development with zero external dependencies.

3. **Hybrid RAG Retrieval with Structured Fallback**:
   - **Intent Classifier**: Detects career/placement inquiries (e.g., "highest package at IEM in 2023", "Heritage average CTC").
   - **Structured SQL Fallback**: Queries tabular placement records (`placement_stats` table) first, injects verified statistics into context, and complements with vector search.
   - **Semantic Top-K Cosine Retrieval**: Evaluates query embeddings against syllabus and PYQ chunks.

4. **Strict Grounding & Citation Guardrails**:
   - Every grounded answer explicitly cites syllabus modules and exam years (e.g., `[Module 3: Inter-process Communication, PCC-CS501, MAKAUT 2022 Question Paper]`).
   - **Negative Grounding Guardrail**: If an inquiry cannot be substantiated by verified curriculum data, the system strictly returns:
     > *"This information is not verified by official MAKAUT curriculum data."*

5. **Document Ingestion Pipeline**:
   - **Apache PDFBox 3.0.3**: Extracted using modern `Loader.loadPDF(...)` API for robust memory efficiency and table header extraction.
   - **TextChunker**: 500-token dynamic sliding window with 50-token overlap, preserving module and section header context.
   - **JSoup Scrapers**: Web crawlers for syllabus links (`MakautSyllabusScraper`) and university notices (`MakautNoticeScraper`).

6. **Interactive Glassmorphic Web Dashboard**:
   - Single Page Client served directly from Spring Boot at `http://localhost:8080/`.
   - Real-time RAG chat, verified citation badges with cosine similarity scores, curriculum catalog explorer, and placement analytics comparator.

---

## 🏗️ Project Architecture

```text
makaut-campussense/
├── src/main/java/com/makaut/campussense/
│   ├── aggregation/
│   │   ├── scrapers/          # JSoup scrapers (MakautSyllabusScraper, MakautNoticeScraper)
│   │   ├── parsers/           # Apache PDFBox 3.0.3 (PdfBoxParser, TextChunker)
│   │   └── IngestionService.java
│   ├── chat/
│   │   ├── ChatController.java     # POST /api/v1/chat/query
│   │   ├── RagService.java         # Top-K vector retrieval + SQL fallback
│   │   ├── PromptTemplates.java    # MAKAUT system prompt & citation rules
│   │   └── dto/                    # Request, Response, and Citation DTOs
│   ├── config/
│   │   ├── SecurityConfig.java     # Stateless Spring Security & CORS
│   │   ├── OpenApiConfig.java      # SpringDoc Swagger UI v3
│   │   └── DataInitializer.java    # Dev profile seed data
│   ├── controller/
│   │   ├── AcademicResourceController.java  # GET /api/v1/resources/search
│   │   ├── PlacementController.java         # GET /api/v1/placements
│   │   └── AdminIngestController.java       # POST /api/v1/admin/ingest/*
│   ├── exception/
│   │   └── GlobalExceptionHandler.java      # @RestControllerAdvice with rate-limit handling
│   ├── model/
│   │   ├── College.java
│   │   ├── CourseResource.java
│   │   ├── DocumentEmbedding.java
│   │   ├── PlacementRecord.java
│   │   ├── ChatSession.java
│   │   ├── ChatMessage.java
│   │   └── enums/
│   ├── repository/                         # Spring Data JPA repositories with native pgvector binding
│   └── service/                            # EmbeddingService, LlmService, PlacementService, AcademicResourceService
├── src/main/resources/
│   ├── application.yml                     # Base application config
│   ├── application-postgres.yml            # PostgreSQL + pgvector configuration
│   ├── application-dev.yml                 # H2 in-memory dev configuration
│   ├── db/migration/
│   │   ├── V1__init_schema.sql             # Flyway DDL with vector extension & HNSW index
│   │   └── V2__seed_makaut_data.sql        # Realistic MAKAUT curriculum and placement data
│   └── static/                             # Modern Web Client (HTML5 / Glassmorphic CSS / JS)
├── docs/                                   # Standalone GitHub Pages Live Web Distribution
│   ├── index.html                          # Live Web UI
│   ├── styles.css                          # Glassmorphic Design System
│   ├── app.js                              # Dual-Engine Client-Side RAG Simulator & Live API Client
│   └── .nojekyll                           # GitHub Pages Jekyll bypass
├── .github/workflows/
│   ├── ci.yml                              # Automated Maven CI & Test Runner
│   └── deploy-pages.yml                    # Automated GitHub Pages Deployment
├── docker-compose.yml                      # PostgreSQL 16 + pgvector container
└── pom.xml
```

---

## 🚀 Quick Start Guide

### Option A: Live Web Demo (Instant in Browser via GitHub Pages)

No local setup required! Visit:
👉 **[https://srijita-bhowmick.github.io/MAKAUT-CampusSense/](https://srijita-bhowmick.github.io/MAKAUT-CampusSense/)**

- **Interactive Conversational AI**: Real-time academic RAG grounded in official MAKAUT regulations.
- **Embedded Knowledge & Vector Engine**: Includes PCC-CS501 (OS), PCC-CS502 (DBMS), MAR (100 points), MOOCs (20 credits for Honours), and placement statistics (IEM, Heritage, TMSL, HIT, NSEC).
- **Zero Configuration**: Fully functional client-side cosine similarity search and responsive glassmorphic UI.

---

### Option B: In-Memory Dev Mode (Zero external dependencies)

The application starts by default in `dev` mode with an in-memory database pre-seeded with MAKAUT colleges (IEM, Heritage, TMSL, HIT), syllabi (PCC-CS501, PCC-CS502), PYQs, and placement statistics:

```powershell
# Run using the included Maven wrapper
.\mvnw.cmd spring-boot:run
```

Access the interactive client:
- **Web Dashboard**: `http://localhost:8080/`
- **Swagger OpenAPI Docs**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:makaut_dev`, User: `sa`, Password: empty)

---

### Option B: Production PostgreSQL 16 + pgvector Mode

1. **Start PostgreSQL with pgvector**:
   ```powershell
   docker compose up -d
   ```

2. **Run Spring Boot with `postgres` profile**:
   ```powershell
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
   ```
   Flyway will automatically execute `V1__init_schema.sql` (enabling `pgvector` and creating HNSW indexes) and `V2__seed_makaut_data.sql`.

3. **Optional External LLM & Embeddings Key**:
   ```powershell
   $env:OPENAI_API_KEY="your-api-key"
   $env:LLM_PROVIDER="openai"
   $env:EMBEDDING_PROVIDER="openai"
   .\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

---

## 📡 REST API Reference

### 1. Conversational RAG Query
**`POST /api/v1/chat/query`**

Accepts student inquiries, performs intent classification, queries structured SQL or semantic vector indices, and returns grounded answers.

```bash
curl -X POST http://localhost:8080/api/v1/chat/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "What are the topics in Module 3 of Operating Systems PCC-CS501?",
    "stream": "CSE",
    "semester": 5
  }'
```

**Response**:
```json
{
  "sessionId": "46914561-bd38-4e89-8d76-1f3c3ebfc844",
  "response": "### Operating Systems (PCC-CS501) - Module 3 Topics:\nBased on the official MAKAUT syllabus [Module 3: Inter-process Communication, PCC-CS501]:\n- Core Concepts: Critical Section, Race Conditions, Mutual Exclusion, Hardware Solutions, Strict Alternation, and Peterson Solution.\n- Synchronization: Semaphores, Event Counters, Monitors, Message Passing, Readers-Writers, Dining Philosophers.\n\n*Reference: MAKAUT AICTE CBCS 2018 Regulation Syllabus & 2022 Question Paper.*",
  "detectedIntent": "ACADEMIC_RAG",
  "sources": [
    {
      "subjectName": "Operating Systems",
      "subjectCode": "PCC-CS501",
      "resourceType": "SYLLABUS",
      "moduleOrSection": "Chunk #0",
      "year": 2023,
      "similarity": 0.884,
      "fileUrl": "https://makautexam.net/syllabus/PCC-CS501.pdf"
    }
  ],
  "verified": true,
  "executionTimeMs": 32
}
```

---

### 2. Search Academic Resources
**`GET /api/v1/resources/search`**

Filter syllabus documents, question papers, and regulations.

```bash
curl "http://localhost:8080/api/v1/resources/search?stream=CSE&semester=5&type=SYLLABUS"
```

---

### 3. Placement Statistics
**`GET /api/v1/placements`**

Retrieve aggregated placement records, highest CTC, and average CTC.

```bash
curl "http://localhost:8080/api/v1/placements?batchYear=2023"
```

---

### 4. Ingest Text / Syllabus into Vector Store
**`POST /api/v1/admin/ingest/text`**

```bash
curl -X POST http://localhost:8080/api/v1/admin/ingest/text \
  -H "Content-Type: application/json" \
  -d '{
    "subjectCode": "PCC-CS601",
    "subjectName": "Compiler Design",
    "stream": "CSE",
    "semester": 6,
    "resourceType": "SYLLABUS",
    "year": 2024,
    "text": "Module 1: Lexical Analysis, Token, Pattern, Lexeme. Module 2: Syntax Analysis, Context-Free Grammars, LL and LR parsing."
  }'
```

---

## 🧪 Running Automated Tests

Run the complete test suite:
```powershell
.\mvnw.cmd test
```

Results:
- `CampusSenseIntegrationTest`: 5/5 passed (Academic RAG, Placement Lookup, Resources, Placements, Ingestion)
- `RagServiceTest`: 3/3 passed (Placement intent routing, Academic syllabus citations, Negative grounding guardrails)
- `EmbeddingServiceTest`: 3/3 passed (1536-dim vector normalization, semantic cosine similarity ranking, vector serialization)
- `PdfBoxParserTest`: 1/1 passed (PDFBox 3.0.3 `Loader.loadPDF` text & module extraction)
- `TextChunkerTest`: 2/2 passed (500-token sliding window with 50-token overlap)

---

## 🌐 GitHub Pages Deployment Setup

To make the site live at **`https://srijita-bhowmick.github.io/MAKAUT-CampusSense/`**:

1. **Push Changes via GitHub Desktop**:
   - Open **GitHub Desktop**.
   - Click **Commit to main** (or view the committed changes) and press **Push origin**.
2. **Configure GitHub Pages in Repository Settings**:
   - Open your GitHub repository in your browser: `https://github.com/Srijita-Bhowmick/MAKAUT-CampusSense`
   - Navigate to **Settings** ➔ **Pages** (in the left sidebar).
   - Under **Build and deployment**:
     - **Option 1 (Automated GitHub Actions - Recommended)**: Under **Source**, select **GitHub Actions**. The included `.github/workflows/deploy-pages.yml` workflow will automatically build and publish your site upon every push.
     - **Option 2 (Branch Deploy)**: Under **Source**, select **Deploy from a branch**, set Branch to `main` and folder to `/docs`, then click **Save**.
3. **Enjoy your live site**:
   - Your site will immediately be live and accessible worldwide at:
   - 👉 **`https://srijita-bhowmick.github.io/MAKAUT-CampusSense/`**

