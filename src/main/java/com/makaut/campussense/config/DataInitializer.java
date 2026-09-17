package com.makaut.campussense.config;

import com.makaut.campussense.aggregation.parsers.TextChunker;
import com.makaut.campussense.model.*;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import com.makaut.campussense.repository.*;
import com.makaut.campussense.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private final CollegeRepository collegeRepository;
    private final CourseResourceRepository courseResourceRepository;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final PlacementRecordRepository placementRecordRepository;
    private final SyllabusUnitRepository syllabusUnitRepository;
    private final FrequentQuestionRepository frequentQuestionRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final EmbeddingService embeddingService;
    private final TextChunker textChunker;

    @Override
    @Transactional
    public void run(String... args) {
        if (collegeRepository.count() > 0) {
            log.info("Dev database already populated. Skipping initialization.");
            return;
        }

        log.info("Seeding in-memory dev database with MAKAUT colleges, 1st-year and core curriculum, bento units, and panic pack...");

        // 1. Colleges
        College iem = collegeRepository.save(College.builder()
                .name("Institute of Engineering & Management (IEM)")
                .code("109")
                .location("Sector V, Salt Lake, Kolkata")
                .build());

        College heritage = collegeRepository.save(College.builder()
                .name("Heritage Institute of Technology (HITK)")
                .code("126")
                .location("Anandapur, Kolkata")
                .build());

        College tmsl = collegeRepository.save(College.builder()
                .name("Techno Main Salt Lake (TMSL)")
                .code("104")
                .location("Sector V, Salt Lake, Kolkata")
                .build());

        College hit = collegeRepository.save(College.builder()
                .name("Haldia Institute of Technology (HIT)")
                .code("103")
                .location("Haldia, Purba Medinipur")
                .build());

        collegeRepository.save(College.builder()
                .name("Netaji Subhash Engineering College (NSEC)")
                .code("108")
                .location("Garia, Kolkata")
                .build());

        // 2. First-Year Common & Core Academic Resources
        // 2. First-Year Common & Core Academic Resources (Verified 2026 MAKAUT Syllabus - All 9 Subjects)
        // Mathematics - I (BS-M101 / 26-BS-M101)
        CourseResource math101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-M101")
                .subjectName("Mathematics - I")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Maths-1.pdf")
                .rawText("""
                        Module I: Calculus of a Single Variable: Rolle theorem, Mean Value Theorems, Taylor and Maclaurin theorems with remainders, indeterminate forms, L'Hospital rule, evaluation of definite and improper integrals.
                        Module II: Calculus of Several Variables: Limit, continuity, partial derivatives, Euler theorem, total derivative, tangent plane, maxima and minima of two variables, Lagrange multipliers.
                        Module III: Multiple Integrals: Double and triple integrals, change of order of integration, change of variables (Cartesian to polar), evaluation of area, volume, and surface area of revolution.
                        Module IV: Matrices and Linear Algebra: Rank of matrix, system of linear equations, inverse, eigenvalues and eigenvectors, Cayley-Hamilton theorem, orthogonal transformation, diagonalization.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource math101_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-BS-M101")
                .subjectName("Mathematics - I")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Maths-1.pdf")
                .rawText(math101.getRawText())
                .metadata(math101.getMetadata())
                .build());

        // Physics (BS-PH101 / 26-BS-PH101)
        CourseResource physics101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-PH101")
                .subjectName("Physics")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Physics.pdf")
                .rawText("""
                        Module I: Oscillations and Waves: Simple harmonic motion, damped and forced oscillations, resonance, wave equation.
                        Module II: Wave Optics: Huygens principle, interference of light, Young double slit, Newton rings, thin film interference, Fraunhofer diffraction, diffraction grating.
                        Module III: Electromagnetism: Gauss law, Ampere law, Faraday law, Maxwell equations, Poynting vector.
                        Module IV: Quantum Physics: De Broglie hypothesis, Heisenberg uncertainty principle, Schrodinger equation, particle in a box.
                        Module V: Solid-State and Modern Physics: Band theory of solids, semiconductors, lasers, optical fibers.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource physics101_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-BS-PH101")
                .subjectName("Physics")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Physics.pdf")
                .rawText(physics101.getRawText())
                .metadata(physics101.getMetadata())
                .build());

        // Chemistry (BS-CH101 / 26-BS-CH101)
        CourseResource chemistry101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-CH101")
                .subjectName("Chemistry")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Chemistry.pdf")
                .rawText("""
                        Module I: Atomic and Molecular Structure: Schrodinger equation, particle in a box, MO theory for homonuclear and heteronuclear diatomics, band structure of solids.
                        Module II: Thermodynamics, Equilibrium and Kinetics: First and second laws of thermodynamics, free energy, chemical equilibria, reaction kinetics, rate laws, catalysis.
                        Module III: Electrochemistry and Corrosion: Electrode potential, Nernst equation, electrochemical cells, reference electrodes, batteries, corrosion mechanisms and prevention methods.
                        Module IV: Engineering Materials and Water: Polymers, composites, lubricants, water hardness, softening processes, reverse osmosis, boiler troubles and water specifications.
                        Module V: Instrumental Methods of Analysis: Principles and applications of UV-Visible spectroscopy, IR spectroscopy, conductometry, potentiometry, and chromatography.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource chemistry101_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-BS-CH101")
                .subjectName("Chemistry")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Chemistry.pdf")
                .rawText(chemistry101.getRawText())
                .metadata(chemistry101.getMetadata())
                .build());

        // Basic Electrical and Electronics Engineering (ES-EE101 / 26-ES-EE101)
        CourseResource ee101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("ES-EE101")
                .subjectName("Basic Electrical and Electronics Engineering")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/BEE.pdf")
                .rawText("""
                        Module I: Basics of Electrical Wiring and Safety: Components of LT switchgear, switch fuse unit (SFU), MCB, ELCB, MCCB, types of wires and cables, earthing and electrical safety.
                        Module II: Direct-Current Circuits: KVL, KCL, mesh and nodal analysis, Superposition theorem, Thevenin theorem, Norton theorem, Maximum Power Transfer theorem.
                        Module III: Alternating-Current Circuits: Sinusoidal waveforms, phasor representation, RLC series and parallel circuits, resonance, power factor, 3-phase star and delta connections.
                        Module IV: Transformers and Rotating Machines: Magnetic materials, single phase transformer construction, EMF equation, equivalent circuit, 3-phase induction motor principle, DC machines.
                        Module V: Semiconductor Fundamentals and Diode Applications: P-N junction diode, V-I characteristics, Zener diode, half wave and full wave rectifiers, filters, clippers and clampers.
                        Module VI: Transistors and Operational Amplifiers: BJT operation, configurations (CE, CB, CC), biasing, Op-Amp ideal characteristics, inverting and non-inverting amplifiers, integrator, differentiator.
                        """)
                .metadata("{\"credits\": 4, \"contact_hours\": \"3L:1T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource ee101_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-ES-EE101")
                .subjectName("Basic Electrical and Electronics Engineering")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/BEE.pdf")
                .rawText(ee101.getRawText())
                .metadata(ee101.getMetadata())
                .build());

        // Programming for Problem Solving and Introduction to AI (ES-CS101 / 26-ES-CS201)
        CourseResource cs101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("ES-CS101")
                .subjectName("Programming for Problem Solving and Introduction to AI")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/PPS.pdf")
                .rawText("""
                        Module I: Introduction to Computer: Block diagram of digital computer, memory organization, CPU, system software, application software, operating system basics.
                        Module II: Generation of Computers: Evolution of computer hardware and software generations, classification of computing devices, von Neumann architecture.
                        Module III: Problem Solving and Computational Thinking: Algorithm design, pseudo-code, flowcharts, decomposition, pattern recognition, abstraction, error classification.
                        Module IV: Fundamentals of C: Constants, variables, data types, arithmetic operators, expressions, operator precedence, conditional statements (if-else, switch), loops (for, while, do-while).
                        Module V: Functions and Program Structure: Function declaration, definition, parameter passing (call by value, call by reference), recursion, scope and storage classes.
                        Module VI: Arrays, Strings and Pointers: 1D and 2D arrays, string operations, pointer concepts, pointer arithmetic, dynamic memory allocation (malloc, calloc, free).
                        Module VII: Structures, Files and Algorithm Efficiency: Structures, unions, file I/O operations (fopen, fclose, fprintf, fscanf), introduction to time and space complexity, Big-O notation.
                        Module VIII: Introduction to Artificial Intelligence: What is AI, history and foundations, Turing test, intelligent agents, search algorithms, overview of machine learning and natural language processing.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource cs101_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-ES-CS201")
                .subjectName("Programming for Problem Solving and Introduction to AI")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/PPS.pdf")
                .rawText(cs101.getRawText())
                .metadata(cs101.getMetadata())
                .build());

        // English for Communication Skills (HM-HU101 / 26-AEC-101)
        CourseResource english101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("HM-HU101")
                .subjectName("English for Communication Skills")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/English.pdf")
                .rawText("""
                        Module I: The Grammar and Vocabulary of Technical English: Word formation, technical terminology, sentence structures, concord, modal verbs, passive voice, and common grammatical pitfalls.
                        Module II: Reading and Comprehension: Techniques of skimming, scanning, critical reading, interpreting technical texts, and précis writing.
                        Module III: Technical Writing: Paragraph cohesion, formal letters, email etiquette, technical reports, proposals, and resume / CV preparation.
                        Module IV: Listening, Speaking and Employability: Phonetics, oral presentations, active listening, group discussions, and technical interview skills.
                        """)
                .metadata("{\"credits\": 2, \"contact_hours\": \"2L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource english101_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-AEC-101")
                .subjectName("English for Communication Skills")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/English.pdf")
                .rawText(english101.getRawText())
                .metadata(english101.getMetadata())
                .build());

        // Mathematics - II (BS-M201 / 26-BS-M201)
        CourseResource math201 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(2)
                .subjectCode("BS-M201")
                .subjectName("Mathematics - II")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Maths-2.pdf")
                .rawText("""
                        Module I: Ordinary Differential Equations: First order ODEs, exact equations, higher order linear differential equations with constant coefficients, method of variation of parameters, Cauchy-Euler equations.
                        Module II: Laplace Transform: Definition, existence, properties of Laplace transform, inverse Laplace, shifting theorems, convolution theorem, solution of ODEs using Laplace transform.
                        Module III: Vector Calculus: Scalar and vector fields, gradient, directional derivative, divergence, curl, line, surface and volume integrals, Green, Gauss divergence, and Stokes theorems.
                        Module IV: Complex Variables and Fourier Series: Analytic functions, Cauchy-Riemann equations, Cauchy integral theorem, Taylor and Laurent series, periodic functions, Fourier series expansion.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource math201_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(2)
                .subjectCode("26-BS-M201")
                .subjectName("Mathematics - II")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Maths-2.pdf")
                .rawText(math201.getRawText())
                .metadata(math201.getMetadata())
                .build());

        // Biology for Engineers (BS-BIO201 / 26-BS-BI201)
        CourseResource bio201 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-BIO201")
                .subjectName("Biology for Engineers")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Biology.pdf")
                .rawText("""
                        Module I: The Cell and the Molecules of Life: Cellular architecture, prokaryotic vs eukaryotic cells, structure and function of proteins, carbohydrates, lipids, nucleic acids, and enzymes.
                        Module II: Genetics and the Flow of Information: Mendelian genetics, structure of DNA, central dogma: replication, transcription, translation, and genetic code.
                        Module III: Biomaterials, Biomechanics and Bio-inspired Design: Properties of biomaterials, biocompatibility, biomechanical principles, bio-inspired engineering designs, and biomimetics.
                        Module IV: Biotechnology, Biosensors and Applications: Recombinant DNA technology, PCR, biosensors, bio-imaging, bio-computing, environmental and industrial biotechnology.
                        """)
                .metadata("{\"credits\": 2, \"contact_hours\": \"2L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource bio201_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-BS-BI201")
                .subjectName("Biology for Engineers")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/Biology.pdf")
                .rawText(bio201.getRawText())
                .metadata(bio201.getMetadata())
                .build());

        // Environmental Science (MC-ES201 / 26-VAC-201)
        CourseResource evs201 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("MC-ES201")
                .subjectName("Environmental Science")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/EVS.pdf")
                .rawText("""
                        Module I: The Environment, Ecosystems and Biodiversity: Ecosystem structure and function, energy flow, food webs, ecological succession, biodiversity hotspots, threats, and conservation.
                        Module II: Environmental Pollution and the Global Challenges: Air, water, soil, noise, thermal pollution, climate change, greenhouse effect, ozone depletion, acid rain, municipal and electronic waste.
                        Module III: India's Sustainability Policy and Legal Framework: Environment Protection Act 1986, Air and Water Acts, National Green Tribunal (NGT), EIA, international treaties (Kyoto, Paris, Montreal).
                        Module IV: Mission LiFE and Behavioural Change: Concept of Mission LiFE (Lifestyle for Environment), individual actions, circular economy, resource conservation, sustainable consumption.
                        Module V: The Seven Themes of Mission LiFE, and Practice: Energy saving, water saving, reducing single-use plastic, sustainable food systems, waste reduction, healthy lifestyles, e-waste adoption.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        CourseResource evs201_2026 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("26-VAC-201")
                .subjectName("Environmental Science")
                .resourceType(ResourceType.SYLLABUS)
                .year(2026)
                .fileUrl("assets/resources/1st_year/syllabus/EVS.pdf")
                .rawText(evs201.getRawText())
                .metadata(evs201.getMetadata())
                .build());

        // 3. Core 2nd/3rd Year Subjects (CSE Core)
        CourseResource osSyllabus = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.CSE)
                .semester(5)
                .subjectCode("PCC-CS501")
                .subjectName("Operating Systems")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/PCC-CS501.pdf")
                .rawText("""
                        Module 1: Introduction: Concept of Operating Systems, Generations of Operating systems, Types of Operating Systems, OS Services, System Calls, Structure of an OS - Layered, Monolithic, Microkernel.
                        Module 2: Processes: Definition, Process Relationship, Different states of a Process, Process State transitions, Process Control Block (PCB), Context switching. Thread: Definition, Various states, Benefits of threads, Types of threads, Concept of multithreads. Process Scheduling: Foundation and Scheduling objectives, Types of Schedulers, Scheduling criteria: CPU utilization, Throughput, Turnaround Time, Waiting Time, Response Time; Scheduling algorithms: Pre-emptive and Non pre-emptive, FCFS, SJF, RR; Multiprocessor scheduling.
                        Module 3: Inter-process Communication: Critical Section, Race Conditions, Mutual Exclusion, Hardware Solution, Strict Alternation, Peterson Solution, The Producer Consumer Problem, Semaphores, Event Counters, Monitors, Message Passing, Classical IPC Problems: Reader Writer Problem, Dining Philosopher Problem.
                        Module 4: Deadlocks: Definition, Necessary and sufficient conditions for Deadlock, Deadlock Prevention, Deadlock Avoidance: Banker algorithm, Deadlock detection and Recovery.
                        Module 5: Memory Management: Basic concept, Logical and Physical address map, Memory allocation: Contiguous Memory allocation - Fixed and variable partition- Internal and External fragmentation and Compaction; Paging: Principle of operation - Page allocation - Hardware support for paging, Protection and sharing, Disadvantages of paging. Virtual Memory: Basics of Virtual Memory - Hardware and control structures - Locality of reference, Page fault, Working Set, Dirty page/Dirty bit - Demand paging, Page Replacement algorithms: Optimal, First in First Out (FIFO), Second Chance (SC), Not recently used (NRU) and Least Recently used (LRU).
                        Module 6: I/O Hardware: I/O devices, Device controllers, Direct Memory Access Principles of I/O Software: Goals of Interrupt handlers, Device drivers, Device independent I/O software. Secondary-Storage Structure: Disk structure, Disk scheduling algorithms (FCFS, SSTF, SCAN, C-SCAN).
                        """)
                .metadata("{\"credits\": 3, \"department\": \"Computer Science & Engineering\"}")
                .build());

        CourseResource osPyq2022 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.CSE)
                .semester(5)
                .subjectCode("PCC-CS501")
                .subjectName("Operating Systems")
                .resourceType(ResourceType.PYQ)
                .year(2022)
                .fileUrl("https://makautexam.net/pyq/2022/PCC-CS501.pdf")
                .rawText("""
                        MAKAUT B.TECH 5TH SEMESTER EXAMINATION 2022 - OPERATING SYSTEMS (PCC-CS501)
                        GROUP A: Multiple choice questions on CPU scheduling, FCFS, Round Robin, and semaphores.
                        GROUP B: Q2. Explain internal vs external fragmentation with diagrams. Q3. State the 4 necessary conditions for deadlock.
                        GROUP C: Q4. (a) Producer-Consumer problem using counting semaphores. (b) Banker's Algorithm safety state and safe sequence calculation. Q5. Demand paging with FIFO and LRU page replacement evaluation on reference string: 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2.
                        """)
                .metadata("{\"exam_year\": 2022, \"marks\": 70}")
                .build());

        CourseResource dbmsSyllabus = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.CSE)
                .semester(5)
                .subjectCode("PCC-CS502")
                .subjectName("Database Management Systems")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/PCC-CS502.pdf")
                .rawText("""
                        Module 1: Database System Concepts & Architecture: Data Models, Schemas & Instances, Three-Schema Architecture, Data Independence, DDL, DML.
                        Module 2: Entity-Relationship Model: Entity Types, Entity Sets, Attributes and Keys, Relationship Types, Weak Entity Types, Enhanced E-R Modeling.
                        Module 3: Relational Model & Relational Algebra: Relational Model Concepts, Relational Constraints, Relational Algebra: SELECT, PROJECT, JOIN, DIVISION.
                        Module 4: Relational Database Design: Functional Dependencies, Normal Forms: 1NF, 2NF, 3NF, BCNF, Lossless Join Decomposition, Dependency Preservation.
                        Module 5: Transaction Processing & Concurrency Control: ACID properties, Serializability, Two-Phase Locking (2PL), Deadlock handling.
                        Module 6: Storage and Indexing: RAID, B-Trees, B+ Trees indexing techniques.
                        """)
                .metadata("{\"credits\": 3, \"department\": \"Computer Science & Engineering\"}")
                .build());

        CourseResource marGuide = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.GENERAL)
                .semester(0)
                .subjectCode("MAR-100")
                .subjectName("Mandatory Additional Requirements (MAR)")
                .resourceType(ResourceType.REGULATION)
                .year(2023)
                .fileUrl("https://makautexam.net/notices/MAR_Guidelines.pdf")
                .rawText("""
                        MAKAUT MANDATORY ADDITIONAL REQUIREMENTS (MAR) ACTIVITY GUIDELINES:
                        Every student admitted to the 4-year B.Tech program under MAKAUT from 2018-19 onwards must earn a minimum of 100 MAR Activity Points over 4 years to qualify for the Degree (75 points for lateral entry students admitted in 3rd semester).
                        MAR Categories and Points Breakdown:
                        1. Massive Open Online Courses (MOOCs) with Certificate: 20 points per course.
                        2. Participation in Tech Fest / Teachers Day / Engineers Day: 5 points per event (Organizing: 10 points).
                        3. Tree Plantation / Environmental Protection Drive: 10 points per tree planted and nurtured with geotagged photo.
                        4. Blood Donation / Free Medical Camp Participation: 8 points per camp.
                        5. Debate / Extempore / Quiz / Essay Competition: 10 points (State/National Level: 20 points).
                        6. Sports / Games Participation: Inter-college: 10 points; University/State: 20 points.
                        7. Publication of Paper in Journal / Conference: 15 points per paper.
                        8. Hackathon / Coding Contest: Participation: 10 points; Winner: 20 points.
                        Submission: Upload documentary proofs on student MAKAUT portal (makautexam.net) before end of each even semester for college coordinator verification.
                        """)
                .metadata("{\"mandatory_points\": 100, \"lateral_points\": 75}")
                .build());

        CourseResource moocsGuide = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.GENERAL)
                .semester(0)
                .subjectCode("MOOCS-HONOURS")
                .subjectName("MOOCs for B.Tech Honours")
                .resourceType(ResourceType.REGULATION)
                .year(2023)
                .fileUrl("https://makautexam.net/notices/MOOCS_Honours.pdf")
                .rawText("""
                        MAKAUT GUIDELINES FOR B.TECH (HONOURS) DEGREE VIA MOOCS:
                        In accordance with AICTE model curriculum, students seeking B.Tech with Honours degree must acquire an additional 20 credits beyond the 160 credits of regular curriculum.
                        Key Rules:
                        1. Courses must be completed through SWAYAM / NPTEL portal from the university-notified approved course basket.
                        2. Credit distribution: Minimum of 20 credits between 3rd to 8th semester (8-week courses = 2 credits, 12-week courses = 3 credits).
                        3. Minimum Qualifying Mark: Certificate with at least 40% (Elite / Silver / Gold).
                        4. No backlogs allowed at graduation.
                        5. MOOCs credits cannot be double-counted for regular elective requirements.
                        """)
                .metadata("{\"required_credits\": 20, \"platform\": \"SWAYAM/NPTEL\"}")
                .build());

        // 4. Populate Syllabus Units for Interactive Checklists (Verified 2026 MAKAUT Syllabus)
        List<SyllabusUnit> units = new ArrayList<>();

        for (CourseResource res : List.of(math101, math101_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: Calculus of a Single Variable").subtopics("Rolle theorem, Mean Value Theorems, Taylor and Maclaurin theorems with remainders, indeterminate forms, L'Hospital rule, evaluation of definite and improper integrals").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Calculus of Several Variables").subtopics("Limit, continuity, partial derivatives, Euler theorem, total derivative, tangent plane, maxima and minima of two variables, Lagrange multipliers").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Multiple Integrals").subtopics("Double and triple integrals, change of order of integration, change of variables (Cartesian to polar), evaluation of area, volume, and surface area of revolution").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Matrices and Linear Algebra").subtopics("Rank of matrix, system of linear equations, inverse, eigenvalues and eigenvectors, Cayley-Hamilton theorem, orthogonal transformation, diagonalization").hours(10).build());
        }

        for (CourseResource res : List.of(physics101, physics101_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: Oscillations and Waves").subtopics("Simple harmonic motion, damped and forced oscillations, resonance, wave equation, transverse and longitudinal waves").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Wave Optics").subtopics("Huygens principle, superposition of waves, interference of light, Young double slit, Newton rings, thin films, Fraunhofer diffraction, diffraction grating").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Electromagnetism").subtopics("Gradient, divergence and curl, Gauss divergence theorem, Stokes theorem, Gauss law, Ampere law, Faraday law, Maxwell equations").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Quantum Physics").subtopics("Wave-particle duality, de Broglie hypothesis, Heisenberg uncertainty principle, Schrodinger equation, particle in a box").hours(7).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(5).unitTitle("Module V: Solid-State and Modern Physics").subtopics("Free electron theory of metals, band theory of solids, intrinsic and extrinsic semiconductors, lasers and optical fibers").hours(5).build());
        }

        for (CourseResource res : List.of(chemistry101, chemistry101_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: Atomic and Molecular Structure").subtopics("Schrodinger equation, particle in a box, MO theory for homonuclear and heteronuclear diatomics, band structure of solids").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Thermodynamics, Equilibrium and Kinetics").subtopics("First and second laws of thermodynamics, free energy, chemical equilibria, reaction kinetics, rate laws, catalysis").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Electrochemistry and Corrosion").subtopics("Electrode potential, Nernst equation, electrochemical cells, reference electrodes, batteries, corrosion mechanisms and prevention methods").hours(7).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Engineering Materials and Water").subtopics("Polymers, composites, lubricants, water hardness, softening processes, reverse osmosis, boiler troubles and water specifications").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(5).unitTitle("Module V: Instrumental Methods of Analysis").subtopics("Principles and applications of UV-Visible spectroscopy, IR spectroscopy, conductometry, potentiometry, and chromatography").hours(5).build());
        }

        for (CourseResource res : List.of(ee101, ee101_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: Basics of Electrical Wiring and Safety").subtopics("Components of LT switchgear, switch fuse unit (SFU), MCB, ELCB, MCCB, types of wires and cables, earthing and electrical safety").hours(3).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Direct-Current Circuits").subtopics("KVL, KCL, mesh and nodal analysis, Superposition theorem, Thevenin theorem, Norton theorem, Maximum Power Transfer theorem").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Alternating-Current Circuits").subtopics("Sinusoidal waveforms, phasor representation, RLC series and parallel circuits, resonance, power factor, 3-phase star and delta connections").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Transformers and Rotating Machines").subtopics("Magnetic materials, single phase transformer construction, EMF equation, equivalent circuit, 3-phase induction motor principle, DC machines").hours(6).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(5).unitTitle("Module V: Semiconductor Fundamentals and Diode Applications").subtopics("P-N junction diode, V-I characteristics, Zener diode, half wave and full wave rectifiers, filters, clippers and clampers").hours(7).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(6).unitTitle("Module VI: Transistors and Operational Amplifiers").subtopics("BJT operation, configurations (CE, CB, CC), biasing, Op-Amp ideal characteristics, inverting and non-inverting amplifiers, integrator, differentiator").hours(11).build());
        }

        for (CourseResource res : List.of(cs101, cs101_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: Introduction to Computer").subtopics("Block diagram of digital computer, memory organization, CPU, system software, application software, operating system basics").hours(4).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Generation of Computers").subtopics("Evolution of computer hardware and software generations, classification of computing devices, von Neumann architecture").hours(2).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Problem Solving and Computational Thinking").subtopics("Algorithm design, pseudo-code, flowcharts, decomposition, pattern recognition, abstraction, error classification").hours(4).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Fundamentals of C").subtopics("Constants, variables, data types, arithmetic operators, expressions, operator precedence, conditional statements (if-else, switch), loops (for, while, do-while)").hours(5).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(5).unitTitle("Module V: Functions and Program Structure").subtopics("Function declaration, definition, parameter passing (call by value, call by reference), recursion, scope and storage classes").hours(5).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(6).unitTitle("Module VI: Arrays, Strings and Pointers").subtopics("1D and 2D arrays, string operations, pointer concepts, pointer arithmetic, dynamic memory allocation (malloc, calloc, free)").hours(6).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(7).unitTitle("Module VII: Structures, Files and Algorithm Efficiency").subtopics("Structures, unions, file I/O operations (fopen, fclose, fprintf, fscanf), introduction to time and space complexity, Big-O notation").hours(4).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(8).unitTitle("Module VIII: Introduction to Artificial Intelligence").subtopics("What is AI, history and foundations, Turing test, intelligent agents, search algorithms, overview of machine learning and natural language processing").hours(6).build());
        }

        for (CourseResource res : List.of(english101, english101_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: The Grammar and Vocabulary of Technical English").subtopics("Word formation, technical terminology, sentence structures, concord, modal verbs, passive voice, and common grammatical pitfalls").hours(6).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Reading and Comprehension").subtopics("Techniques of skimming, scanning, critical reading, interpreting technical texts, and précis writing").hours(6).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Technical Writing").subtopics("Paragraph cohesion, formal letters, email etiquette, technical reports, proposals, and resume / CV preparation").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Listening, Speaking and Employability").subtopics("Phonetics, oral presentations, active listening, group discussions, and technical interview skills").hours(6).build());
        }

        for (CourseResource res : List.of(math201, math201_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: Ordinary Differential Equations").subtopics("First order ODEs, exact equations, higher order linear differential equations with constant coefficients, method of variation of parameters, Cauchy-Euler equations").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Laplace Transform").subtopics("Definition, existence, properties of Laplace transform, inverse Laplace, shifting theorems, convolution theorem, solution of ODEs using Laplace transform").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Vector Calculus").subtopics("Scalar and vector fields, gradient, directional derivative, divergence, curl, line, surface and volume integrals, Green, Gauss divergence, and Stokes theorems").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Complex Variables and Fourier Series").subtopics("Analytic functions, Cauchy-Riemann equations, Cauchy integral theorem, Taylor and Laurent series, periodic functions, Fourier series expansion").hours(9).build());
        }

        for (CourseResource res : List.of(bio201, bio201_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: The Cell and the Molecules of Life").subtopics("Cellular architecture, prokaryotic vs eukaryotic cells, structure and function of proteins, carbohydrates, lipids, nucleic acids, and enzymes").hours(7).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Genetics and the Flow of Information").subtopics("Mendelian genetics, structure of DNA, central dogma: replication, transcription, translation, and genetic code").hours(6).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: Biomaterials, Biomechanics and Bio-inspired Design").subtopics("Properties of biomaterials, biocompatibility, biomechanical principles, bio-inspired engineering designs, and biomimetics").hours(7).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Biotechnology, Biosensors and Applications").subtopics("Recombinant DNA technology, PCR, biosensors, bio-imaging, bio-computing, environmental and industrial biotechnology").hours(6).build());
        }

        for (CourseResource res : List.of(evs201, evs201_2026)) {
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(1).unitTitle("Module I: The Environment, Ecosystems and Biodiversity").subtopics("Ecosystem structure and function, energy flow, food webs, ecological succession, biodiversity hotspots, threats, and conservation").hours(9).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(2).unitTitle("Module II: Environmental Pollution and the Global Challenges").subtopics("Air, water, soil, noise, thermal pollution, climate change, greenhouse effect, ozone depletion, acid rain, municipal and electronic waste").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(3).unitTitle("Module III: India's Sustainability Policy and Legal Framework").subtopics("Environment Protection Act 1986, Air and Water Acts, National Green Tribunal (NGT), EIA, international treaties (Kyoto, Paris, Montreal)").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(4).unitTitle("Module IV: Mission LiFE and Behavioural Change").subtopics("Concept of Mission LiFE (Lifestyle for Environment), individual actions, circular economy, resource conservation, sustainable consumption").hours(8).build());
            units.add(SyllabusUnit.builder().courseResource(res).unitNumber(5).unitTitle("Module V: The Seven Themes of Mission LiFE, and Practice").subtopics("Energy saving, water saving, reducing single-use plastic, sustainable food systems, waste reduction, healthy lifestyles, e-waste adoption").hours(9).build());
        }

        // OS PCC-CS501
        units.add(SyllabusUnit.builder().courseResource(osSyllabus).unitNumber(1).unitTitle("Unit 1: OS Concepts & Structures").subtopics("Generations, OS types, system calls, Monolithic vs Microkernel architecture").hours(5).build());
        units.add(SyllabusUnit.builder().courseResource(osSyllabus).unitNumber(2).unitTitle("Unit 2: Processes & Scheduling").subtopics("PCB, context switching, FCFS, SJF, Round Robin, Multiprocessor scheduling").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(osSyllabus).unitNumber(3).unitTitle("Unit 3: Inter-Process Communication").subtopics("Critical sections, Peterson solution, semaphores, Producer-Consumer, Dining Philosophers").hours(7).build());
        units.add(SyllabusUnit.builder().courseResource(osSyllabus).unitNumber(4).unitTitle("Unit 4: Deadlocks").subtopics("4 conditions, Prevention, Banker algorithm for avoidance, Detection and recovery").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(osSyllabus).unitNumber(5).unitTitle("Unit 5: Memory Management").subtopics("Paging, segmentation, TLB, Virtual memory, page replacement: FIFO, LRU, Optimal").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(osSyllabus).unitNumber(6).unitTitle("Unit 6: Storage & Disk Scheduling").subtopics("I/O hardware, disk scheduling: FCFS, SSTF, SCAN, C-SCAN, RAID levels").hours(5).build());

        // DBMS PCC-CS502
        units.add(SyllabusUnit.builder().courseResource(dbmsSyllabus).unitNumber(1).unitTitle("Unit 1: Architecture & Data Models").subtopics("Three-schema architecture, data independence, DDL, DML, relational model").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(dbmsSyllabus).unitNumber(2).unitTitle("Unit 2: E-R Modeling & Relational Algebra").subtopics("Entities, relationships, weak entity sets, Relational Algebra operations").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(dbmsSyllabus).unitNumber(3).unitTitle("Unit 3: SQL & Relational Design").subtopics("Functional dependencies, Normalization: 1NF, 2NF, 3NF, BCNF, lossless join").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(dbmsSyllabus).unitNumber(4).unitTitle("Unit 4: Concurrency & Transactions").subtopics("ACID properties, serializability, two-phase locking (2PL), deadlock resolution").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(dbmsSyllabus).unitNumber(5).unitTitle("Unit 5: Indexing & Storage").subtopics("File organization, B-Trees, B+ Trees indexing, hashing").hours(6).build());

        syllabusUnitRepository.saveAll(units);

        // 5. Populate Frequent Questions (Panic Pack)
        List<FrequentQuestion> questions = List.of(
                // Chemistry (BS-CH101)
                FrequentQuestion.builder()
                        .subjectCode("BS-CH101")
                        .topicName("Electrochemistry & Nernst Equation")
                        .questionText("Derive the Nernst equation for single electrode potential and cell EMF. Calculate the EMF of the cell: Zn | Zn2+(0.01M) || Cu2+(0.1M) | Cu at 298 K (Given E°_cell = 1.10 V).")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2019, 2021, 2022, 2023, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("From Gibbs free energy Delta G = Delta G° + RT ln Q and Delta G = -nFE, we obtain E = E° - (2.303 RT / nF) log Q. At 298 K, E = E° - (0.0591 / n) log([Zn2+]/[Cu2+]) = 1.10 - (0.0591 / 2) log(0.01/0.1) = 1.10 + 0.0295 = 1.1295 V.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("BS-CH101")
                        .topicName("Conductometric Titrations")
                        .questionText("Discuss the principle of conductometric titration. Sketch and explain the conductometric titration curves for: (i) Strong acid vs Strong base, and (ii) Weak acid vs Strong base.")
                        .marks(5)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2023")
                        .groupType("Group B")
                        .modelAnswerSnippet("Conductance depends on number and mobility of ions. For HCl vs NaOH: Conductance decreases sharply as fast H+ is replaced by slower Na+, reaches minimum at equivalence, then rises steeply due to excess fast OH-. For CH3COOH vs NaOH: Initial decrease, then steady rise due to CH3COONa formation, and sharp rise post-equivalence.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("BS-CH101")
                        .topicName("Crystal Field Theory (CFT)")
                        .questionText("Explain the crystal field splitting of d-orbitals in octahedral and tetrahedral complexes with orbital energy diagrams. Calculate CFSE for d5 and d6 octahedral complexes in high-spin and low-spin states.")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2018, 2020, 2022, 2023, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("Under octahedral field, degeneracy of five d-orbitals is lifted into lower t2g (dxy, dyz, dzx) at -0.4 Delta_o and higher eg (dx2-y2, dz2) at +0.6 Delta_o. In tetrahedral field, splitting is reversed into lower e (-0.6 Delta_t) and higher t2 (+0.4 Delta_t) where Delta_t = (4/9) Delta_o.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("BS-CH101")
                        .topicName("Organic Synthesis (Drugs)")
                        .questionText("Outline the chemical synthesis of: (i) Aspirin from salicylic acid, and (ii) Paracetamol from p-nitrophenol. Give reactions, reaction conditions, and their medicinal mechanisms.")
                        .marks(5)
                        .frequencyCount(4)
                        .examYears("2020, 2022, 2023, 2024")
                        .groupType("Group B")
                        .modelAnswerSnippet("Aspirin: Salicylic acid + acetic anhydride in presence of conc. H2SO4 at 50-60°C gives acetylsalicylic acid (aspirin) + acetic acid. Acts as irreversible COX inhibitor. Paracetamol: p-nitrophenol reduced (Fe/HCl) to p-aminophenol, then acetylated with acetic anhydride/glacial acetic acid to give acetaminophen (paracetamol). Acts as antipyretic/analgesic.")
                        .build(),

                // Electrical
                FrequentQuestion.builder()
                        .subjectCode("ES-EE101")
                        .topicName("Network Theorems")
                        .questionText("State Thevenin theorem. For a given bridge circuit with 10V DC source, find the Thevenin equivalent voltage (Vth) and Thevenin resistance (Rth) across load terminals AB.")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2019, 2021, 2022, 2023, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("Thevenin Theorem: Any linear bilateral DC network across two terminals can be replaced by an equivalent voltage source Vth in series with resistance Rth. Vth is open-circuit voltage across AB. Rth is equivalent resistance looking into AB with independent sources deactivated.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("ES-EE101")
                        .topicName("AC Resonance")
                        .questionText("Derive the condition for series resonance in an RLC circuit. Show that the resonant frequency is fr = 1 / (2 * pi * sqrt(L * C)). Calculate the Q-factor.")
                        .marks(5)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2023")
                        .groupType("Group B")
                        .modelAnswerSnippet("At series resonance, inductive reactance equals capacitive reactance (X_L = X_C). Hence omega * L = 1 / (omega * C) => omega = 1 / sqrt(LC). Quality factor Q = (omega0 * L) / R = 1 / (R * sqrt(C/L)).")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("ES-EE101")
                        .topicName("Transformers")
                        .questionText("Explain the losses in a single phase transformer. Derive the condition for maximum efficiency of a transformer.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2018, 2020, 2022, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("Transformer losses include Iron Losses (Hysteresis + Eddy Current) and Copper Losses (I^2 * R). Maximum efficiency occurs when variable copper losses equal constant iron losses: P_cu = P_i.")
                        .build(),

                // Programming C
                FrequentQuestion.builder()
                        .subjectCode("ES-CS101")
                        .topicName("Dynamic Memory Allocation")
                        .questionText("Explain the differences between malloc() and calloc() in C. Write a program to dynamically allocate memory for an array of n integers and find their average.")
                        .marks(5)
                        .frequencyCount(5)
                        .examYears("2018, 2019, 2021, 2022, 2023")
                        .groupType("Group B")
                        .modelAnswerSnippet("malloc(size): allocates single contiguous block of specified bytes with garbage values. calloc(n, size): allocates n blocks of size bytes initialized to zero. Both return void pointer or NULL on failure.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("ES-CS101")
                        .topicName("Recursion")
                        .questionText("Write a recursive C function to solve the Tower of Hanoi problem for n disks. Trace the function call tree for n = 3 disks.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("void towerOfHanoi(int n, char from, char to, char aux) { if (n == 1) { printf(\"Move disk 1 from %c to %c\\n\", from, to); return; } towerOfHanoi(n - 1, from, aux, to); printf(\"Move disk %d from %c to %c\\n\", n, from, to); towerOfHanoi(n - 1, aux, to, from); } Total moves: 2^n - 1 = 7.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("ES-CS101")
                        .topicName("Pointers & Arrays")
                        .questionText("What is a dangling pointer? Explain with an example how it differs from a void pointer and a NULL pointer.")
                        .marks(5)
                        .frequencyCount(3)
                        .examYears("2020, 2022, 2023")
                        .groupType("Group B")
                        .modelAnswerSnippet("A dangling pointer points to a memory location that has been deallocated (e.g. after free(ptr)). A NULL pointer points to 0. A void pointer (void*) is a generic pointer that can hold the address of any data type.")
                        .build(),

                // Mathematics
                FrequentQuestion.builder()
                        .subjectCode("BS-M101")
                        .topicName("Cayley-Hamilton Theorem")
                        .questionText("State Cayley-Hamilton Theorem. Verify it for matrix A = [[1, 2], [3, 4]] and hence compute A^-1.")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2018, 2019, 2021, 2022, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("Statement: Every square matrix satisfies its own characteristic equation: det(A - lambda * I) = 0. Characteristic polynomial: lambda^2 - tr(A)*lambda + det(A) = 0. Substituting A gives A^2 - 5A - 2I = 0. Multiplying by A^-1 gives A^-1 = (A - 5I) / 2.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("BS-M101")
                        .topicName("Multivariable Extrema")
                        .questionText("Find the maximum and minimum distances from the origin to the surface xyz = 8 using the method of Lagrange multipliers.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("Objective function: f(x, y, z) = x^2 + y^2 + z^2. Constraint: g(x, y, z) = xyz - 8 = 0. Form Lagrangian L = f - lambda*g. Setting partial derivatives to 0 gives x^2 = y^2 = z^2 = lambda*xyz / 2. Yields stationary points at x = y = z = 2, distance = 2*sqrt(3).")
                        .build(),

                // Physics
                FrequentQuestion.builder()
                        .subjectCode("BS-PH101")
                        .topicName("Newton Rings")
                        .questionText("Explain the formation of Newton rings in reflected light. Prove that the diameters of dark rings are proportional to square roots of natural numbers.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("Newton rings are formed by interference between light reflected from upper surface of glass plate and lower surface of plano-convex lens. Path difference for reflected ray: Delta = 2*mu*t + lambda/2. For dark rings, 2*t = n*lambda. Since t = r^2 / (2R), D_n = sqrt(4*n*lambda*R) proportional to sqrt(n).")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("BS-PH101")
                        .topicName("Quantum Mechanics")
                        .questionText("Derive the time-independent Schrodinger wave equation for a free particle. Find the energy eigenvalues for a particle trapped in an infinite 1D potential well of width L.")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2018, 2019, 2021, 2022, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("Starting from total energy E = p^2/(2m) + V and substituting quantum momentum operator p = -i*hbar*d/dx gives -hbar^2/(2m) * d^2(psi)/dx^2 + V*psi = E*psi. Boundary conditions psi(0) = psi(L) = 0 yield E_n = (n^2 * pi^2 * hbar^2) / (2 * m * L^2).")
                        .build(),

                // English
                FrequentQuestion.builder()
                        .subjectCode("HM-HU101")
                        .topicName("Technical Report Writing")
                        .questionText("Discuss the structure and key components of a formal technical report. Draft an outline of a report on 'Campus Smart Waste Management'.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("Standard technical report sections: 1. Title Page, 2. Executive Summary/Abstract, 3. Table of Contents, 4. Introduction & Scope, 5. Methodology & Technical Discussion, 6. Findings & Cost Analysis, 7. Conclusions & Recommendations, 8. References & Appendices.")
                        .build(),

                // OS PCC-CS501
                FrequentQuestion.builder()
                        .subjectCode("PCC-CS501")
                        .topicName("Banker Algorithm")
                        .questionText("Explain Banker Algorithm for Deadlock Avoidance. Given Allocation, Max, and Available matrices for 5 processes and 3 resource types, determine if the system is in a safe state and compute the safe sequence.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("Banker Algorithm simulates resource allocation safety: 1. Calculate Need = Max - Allocation. 2. Find process where Need <= Available. 3. Simulate process execution and release its resources to Available. 4. If all processes complete, the state is safe with an identified Safe Sequence e.g. <P1, P3, P4, P0, P2>.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("PCC-CS501")
                        .topicName("Process Synchronization")
                        .questionText("Define Critical Section. What are the three requirements for a valid critical section solution? Explain how counting semaphores solve the Producer-Consumer problem.")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2018, 2019, 2021, 2022, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("Requirements: 1. Mutual Exclusion, 2. Progress, 3. Bounded Waiting. Producer-Consumer solution uses three semaphores: mutex = 1 (mutual exclusion for buffer access), empty = N (tracks empty slots), full = 0 (tracks filled slots).")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("PCC-CS501")
                        .topicName("Page Replacement")
                        .questionText("Compare FIFO and LRU page replacement algorithms on the reference string: 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2 for 3 frames. Calculate the number of page faults for each.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2019, 2021, 2022, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("Trace shows FIFO yields 15 page faults, while LRU yields 12 page faults. FIFO suffers from Belady Anomaly where more frames can lead to more faults; LRU is a stack algorithm and immune to Belady Anomaly.")
                        .build(),

                // DBMS PCC-CS502
                FrequentQuestion.builder()
                        .subjectCode("PCC-CS502")
                        .topicName("Database Normalization")
                        .questionText("Explain Boyce-Codd Normal Form (BCNF). How does BCNF differ from 3NF? Provide a relation that is in 3NF but not in BCNF.")
                        .marks(10)
                        .frequencyCount(5)
                        .examYears("2019, 2020, 2022, 2023, 2024")
                        .groupType("Group C")
                        .modelAnswerSnippet("A relation is in 3NF if for every FD X -> A, X is superkey OR A is prime attribute. In BCNF, X must strictly be a superkey for every non-trivial FD. Relation R(Student, Subject, Teacher) with FDs {Student, Subject} -> Teacher and Teacher -> Subject is in 3NF but not in BCNF.")
                        .build(),

                FrequentQuestion.builder()
                        .subjectCode("PCC-CS502")
                        .topicName("Concurrency Control")
                        .questionText("Explain the Two-Phase Locking (2PL) protocol. Prove that Strict 2PL guarantees conflict serializability and prevents cascading aborts.")
                        .marks(10)
                        .frequencyCount(4)
                        .examYears("2018, 2019, 2021, 2023")
                        .groupType("Group C")
                        .modelAnswerSnippet("2PL has Growing Phase (locks acquired, none released) and Shrinking Phase (locks released, none acquired). Strict 2PL holds all exclusive locks until transaction commits or aborts, ensuring no other transaction reads uncommitted data, preventing cascading rollbacks.")
                        .build()
        );
        frequentQuestionRepository.saveAll(questions);

        // 6. Preload initial sample Bookmark & Progress for default session
        if (!questions.isEmpty()) {
            userBookmarkRepository.save(UserBookmark.builder()
                    .userIdentifier("guest-default")
                    .question(questions.get(0))
                    .build());
            userBookmarkRepository.save(UserBookmark.builder()
                    .userIdentifier("guest-default")
                    .question(questions.get(3))
                    .build());
        }

        // 7. Document Embeddings (Vectors for semantic search across all subjects)
        List<CourseResource> allResources = List.of(math101, physics101, ee101, cs101, english101, osSyllabus, osPyq2022, dbmsSyllabus, marGuide, moocsGuide);
        List<DocumentEmbedding> embeddings = new ArrayList<>();

        for (CourseResource cr : allResources) {
            List<TextChunker.Chunk> chunks = textChunker.chunkText(cr.getRawText());
            for (TextChunker.Chunk chunk : chunks) {
                float[] vector = embeddingService.generateEmbedding(chunk.getContent());
                String vecStr = embeddingService.vectorToString(vector);

                embeddings.add(DocumentEmbedding.builder()
                        .resource(cr)
                        .chunkIndex(chunk.getIndex())
                        .content(chunk.getContent())
                        .embedding(vecStr)
                        .build());
            }
        }
        documentEmbeddingRepository.saveAll(embeddings);

        // 8. Placements
        // IEM
        placementRecordRepository.save(PlacementRecord.builder().college(iem).batchYear(2023).companyName("Amazon").highestCtc(BigDecimal.valueOf(44.00)).averageCtc(BigDecimal.valueOf(44.00)).totalOffers(18).sourceUrl("https://iem.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(iem).batchYear(2023).companyName("Adobe").highestCtc(BigDecimal.valueOf(42.50)).averageCtc(BigDecimal.valueOf(42.50)).totalOffers(6).sourceUrl("https://iem.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(iem).batchYear(2023).companyName("PwC India").highestCtc(BigDecimal.valueOf(9.00)).averageCtc(BigDecimal.valueOf(7.50)).totalOffers(85).sourceUrl("https://iem.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(iem).batchYear(2023).companyName("TCS Ninja & Digital").highestCtc(BigDecimal.valueOf(7.50)).averageCtc(BigDecimal.valueOf(4.50)).totalOffers(320).sourceUrl("https://iem.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(iem).batchYear(2023).companyName("Overall IEM Summary").highestCtc(BigDecimal.valueOf(72.00)).averageCtc(BigDecimal.valueOf(8.50)).totalOffers(1150).sourceUrl("https://iem.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(iem).batchYear(2024).companyName("Overall IEM Summary").highestCtc(BigDecimal.valueOf(56.00)).averageCtc(BigDecimal.valueOf(8.20)).totalOffers(1080).sourceUrl("https://iem.edu.in").build());

        // Heritage
        placementRecordRepository.save(PlacementRecord.builder().college(heritage).batchYear(2023).companyName("Microsoft").highestCtc(BigDecimal.valueOf(42.00)).averageCtc(BigDecimal.valueOf(42.00)).totalOffers(4).sourceUrl("https://heritageit.edu").build());
        placementRecordRepository.save(PlacementRecord.builder().college(heritage).batchYear(2023).companyName("Amazon").highestCtc(BigDecimal.valueOf(38.00)).averageCtc(BigDecimal.valueOf(38.00)).totalOffers(12).sourceUrl("https://heritageit.edu").build());
        placementRecordRepository.save(PlacementRecord.builder().college(heritage).batchYear(2023).companyName("Cognizant").highestCtc(BigDecimal.valueOf(6.75)).averageCtc(BigDecimal.valueOf(4.25)).totalOffers(240).sourceUrl("https://heritageit.edu").build());
        placementRecordRepository.save(PlacementRecord.builder().college(heritage).batchYear(2023).companyName("Overall Heritage Summary").highestCtc(BigDecimal.valueOf(42.00)).averageCtc(BigDecimal.valueOf(7.20)).totalOffers(920).sourceUrl("https://heritageit.edu").build());
        placementRecordRepository.save(PlacementRecord.builder().college(heritage).batchYear(2024).companyName("Overall Heritage Summary").highestCtc(BigDecimal.valueOf(45.00)).averageCtc(BigDecimal.valueOf(7.50)).totalOffers(890).sourceUrl("https://heritageit.edu").build());

        // TMSL
        placementRecordRepository.save(PlacementRecord.builder().college(tmsl).batchYear(2023).companyName("Amazon AWS").highestCtc(BigDecimal.valueOf(36.00)).averageCtc(BigDecimal.valueOf(36.00)).totalOffers(8).sourceUrl("https://tmsl.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(tmsl).batchYear(2023).companyName("Capgemini").highestCtc(BigDecimal.valueOf(7.50)).averageCtc(BigDecimal.valueOf(4.50)).totalOffers(210).sourceUrl("https://tmsl.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(tmsl).batchYear(2023).companyName("Overall TMSL Summary").highestCtc(BigDecimal.valueOf(36.00)).averageCtc(BigDecimal.valueOf(6.20)).totalOffers(850).sourceUrl("https://tmsl.edu.in").build());
        placementRecordRepository.save(PlacementRecord.builder().college(tmsl).batchYear(2024).companyName("Overall TMSL Summary").highestCtc(BigDecimal.valueOf(38.00)).averageCtc(BigDecimal.valueOf(6.50)).totalOffers(810).sourceUrl("https://tmsl.edu.in").build());

        // HIT
        placementRecordRepository.save(PlacementRecord.builder().college(hit).batchYear(2023).companyName("Overall HIT Summary").highestCtc(BigDecimal.valueOf(32.00)).averageCtc(BigDecimal.valueOf(5.80)).totalOffers(780).sourceUrl("https://hithaldia.in").build());

        log.info("Dev database initialization completed: {} colleges, {} resources, {} embeddings, {} syllabus units, {} panic questions, {} placement records.",
                collegeRepository.count(), courseResourceRepository.count(), documentEmbeddingRepository.count(), syllabusUnitRepository.count(), frequentQuestionRepository.count(), placementRecordRepository.count());
    }
}
