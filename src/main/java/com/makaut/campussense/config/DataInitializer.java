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
public class DataInitializer implements CommandLineRunner {

    private final CollegeRepository collegeRepository;
    private final CourseResourceRepository courseResourceRepository;
    private final DocumentEmbeddingRepository documentEmbeddingRepository;
    private final PlacementRecordRepository placementRecordRepository;
    private final SyllabusUnitRepository syllabusUnitRepository;
    private final FrequentQuestionRepository frequentQuestionRepository;
    private final UserBookmarkRepository userBookmarkRepository;
    private final UserSyllabusProgressRepository userSyllabusProgressRepository;
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

        College nsec = collegeRepository.save(College.builder()
                .name("Netaji Subhash Engineering College (NSEC)")
                .code("108")
                .location("Garia, Kolkata")
                .build());

        // 2. First-Year Common & Core Academic Resources
        // BS-M101
        CourseResource math101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-M101")
                .subjectName("Mathematics 1A")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/BS-M101.pdf")
                .rawText("""
                        Module 1: Calculus: Evolutes and involutes; Evaluation of definite and improper integrals; Beta and Gamma functions; Applications of definite integrals to evaluate surface areas and volumes of revolutions; Rolle theorem, Mean value theorems, Taylor and Maclaurin theorems with remainders.
                        Module 2: Multivariable Calculus: Limit, continuity and partial derivatives, directional derivatives, total derivative; Tangent plane and normal line; Maxima, minima and saddle points; Method of Lagrange multipliers.
                        Module 3: Matrices: Inverse and rank of a matrix, rank-nullity theorem; System of linear equations; Symmetric, skew-symmetric and orthogonal matrices; Determinants; Eigenvalues and eigenvectors; Diagonalization of matrices; Cayley-Hamilton Theorem.
                        Module 4: Vector Spaces: Vector Space, linear dependence and independence of vectors, basis, dimension; Linear transformations (maps), range and kernel of a linear map, rank and nullity, Inverse of a linear transformation, rank-nullity theorem.
                        """)
                .metadata("{\"credits\": 4, \"contact_hours\": \"3L:1T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        // BS-PH101
        CourseResource physics101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-PH101")
                .subjectName("Physics")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/BS-PH101.pdf")
                .rawText("""
                        Module 1: Oscillations and Waves: Simple harmonic motion, damped and forced oscillations, resonance, wave equation.
                        Module 2: Wave Optics: Huygens principle, superposition of waves, interference of light, Young double slit experiment, Newton rings, thin film interference; Fraunhofer diffraction, diffraction grating; Polarization, Brewster law.
                        Module 3: Electromagnetism: Gauss law, Ampere law, Faraday law of induction; Maxwell equations in differential and integral forms, displacement current; Poynting vector.
                        Module 4: Quantum Physics: Blackbody radiation, Planck distribution, photoelectric effect, Compton effect; de Broglie hypothesis; Heisenberg uncertainty principle; Wave function, Schrodinger equation; Particle in a 1D box.
                        Module 5: Solid-State and Modern Physics: Band structure of solids, conductors, semiconductors, insulators, Fermi level; Lasers: Einstein coefficients, population inversion; Optical fibers, numerical aperture.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        // BS-CH101
        CourseResource chemistry101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("BS-CH101")
                .subjectName("Chemistry")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/BS-CH101.pdf")
                .rawText("""
                        Module 1: Atomic and Molecular Structure: Schrodinger equation, particle in a box; Molecular orbitals of diatomic molecules; Band structure of solids.
                        Module 2: Spectroscopic Techniques and Applications: Principles of spectroscopy, selection rules; UV-Visible and Fluorescence spectroscopy; Infrared and Raman spectroscopy.
                        Module 3: Intermolecular Forces and Potential Energy Surfaces: Ionic, dipolar and van der Waals interactions; Equations of state of real gases, critical phenomena; Potential energy surfaces.
                        Module 4: Use of Free Energy in Chemical Equilibria: Thermodynamic functions: energy, entropy and free energy; Cell potentials, Nernst equation and applications, acid-base and oxidation-reduction equilibria.
                        Module 5: Periodic Properties: Effective nuclear charge, penetration, shielding; Electronic configuration, atomic and ionic sizes, ionization energies, electron affinity, electronegativity, oxidation states.
                        Module 6: Stereochemistry and Organic Reactions: Structural isomers, stereoisomers, configurations, symmetry and chirality; Synthesis of commonly used drug molecules (Aspirin, Paracetamol).
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        // ES-EE101
        CourseResource ee101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("ES-EE101")
                .subjectName("Basic Electrical Engineering")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/ES-EE101.pdf")
                .rawText("""
                        Module 1: DC Circuits: Electrical circuit elements (R, L, and C), voltage and current sources, Kirchhoff current and voltage laws, analysis of simple circuits with DC excitation; Superposition, Thevenin and Norton Theorems; Maximum Power Transfer theorem; Star-Delta conversion.
                        Module 2: AC Circuits: Representation of sinusoidal waveforms, peak and RMS values, phasor representation, real power, reactive power, apparent power, power factor; Analysis of single-phase AC circuits consisting of R, L, C, RL, RC, RLC combinations (series and parallel), series and parallel resonance.
                        Module 3: Transformers: Magnetic materials, BH characteristics, ideal and practical transformer, equivalent circuit, losses in transformers, regulation and efficiency; Auto-transformer and three-phase transformer connections.
                        Module 4: Electrical Machines: Generation of rotating magnetic fields, Construction and working of a three-phase induction motor, significance of torque-slip characteristic; Loss components and efficiency, starting and speed control; Single-phase induction motor; Construction and working of synchronous machines and DC machines.
                        """)
                .metadata("{\"credits\": 4, \"contact_hours\": \"3L:1T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        // ES-CS101
        CourseResource cs101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("ES-CS101")
                .subjectName("Programming for Problem Solving")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/ES-CS101.pdf")
                .rawText("""
                        Module 1: Introduction to Programming: Idea of Algorithm: steps to solve logical and numerical problems; Representation of Algorithm: Flowchart/Pseudocode with examples; From algorithms to programs; source code, variables, data types and sizes; Compilation, execution, and debugging.
                        Module 2: Arithmetic Expressions and Precedence: Conditional Branching and Loops: Writing and evaluation of conditionals and consequent branching; Iteration and loops (for, while, do-while); Nested loops, break and continue statements.
                        Module 3: Arrays & Strings: Arrays (1-D, 2-D), Number representation and character representation, Strings; Character arrays and manipulation, matrix addition and multiplication.
                        Module 4: Functions & Recursion: Functions (including using built in libraries), Parameter passing in functions, call by value, Passing arrays to functions: idea of call by reference; Recursion, as a different way of solving problems; Example programs: finding Factorial, Fibonacci series, Ackerman function, Tower of Hanoi.
                        Module 5: Pointers & Structures: Idea of pointers, Defining pointers, Use of Pointers in self-referential structures, notion of linked list (no implementation); Dynamic memory allocation: malloc, calloc, realloc, free; Structures, defining structures and Array of Structures.
                        """)
                .metadata("{\"credits\": 3, \"contact_hours\": \"3L:0T:0P\", \"department\": \"1st Year Common\"}")
                .build());

        // HM-HU101
        CourseResource english101 = courseResourceRepository.save(CourseResource.builder()
                .stream(StreamType.COMMON)
                .semester(1)
                .subjectCode("HM-HU101")
                .subjectName("English")
                .resourceType(ResourceType.SYLLABUS)
                .year(2023)
                .fileUrl("https://makautexam.net/syllabus/HM-HU101.pdf")
                .rawText("""
                        Module 1: Vocabulary Building: The concept of Word Formation; Root words from foreign languages and their use in English; Acquaintance with prefixes and suffixes; Synonyms, antonyms, and standard abbreviations.
                        Module 2: Basic Writing Skills: Sentence Structures; Use of phrases and clauses in sentences; Importance of proper punctuation; Creating coherence; Organizing principles of paragraphs in documents; Techniques for writing precisely.
                        Module 3: Identifying Common Errors in Writing: Subject-verb agreement; Noun-pronoun agreement; Misplaced modifiers; Articles; Prepositions; Redundancies; Clichés.
                        Module 4: Nature and Style of sensible Writing: Describing, defining, classifying; Providing examples or evidence; Writing introduction and conclusion; Comprehension; Précis Writing; Essay Writing.
                        Module 5: Technical Communication: Formal letters and email correspondence; Curriculum Vitae and Resume writing; Technical report writing.
                        """)
                .metadata("{\"credits\": 2, \"contact_hours\": \"2L:0T:0P\", \"department\": \"1st Year Common\"}")
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

        // 4. Populate Syllabus Units for Interactive Checklists
        List<SyllabusUnit> units = new ArrayList<>();
        // Math 101
        units.add(SyllabusUnit.builder().courseResource(math101).unitNumber(1).unitTitle("Unit 1: Calculus & Evolutes").subtopics("Evolutes, involutes, definite integrals, Beta/Gamma functions, Rolle and Mean Value Theorems").hours(10).build());
        units.add(SyllabusUnit.builder().courseResource(math101).unitNumber(2).unitTitle("Unit 2: Multivariable Calculus").subtopics("Partial derivatives, directional derivatives, maxima and minima, Lagrange multipliers").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(math101).unitNumber(3).unitTitle("Unit 3: Matrices & Linear Algebra").subtopics("Inverse, rank, rank-nullity theorem, Cayley-Hamilton theorem, eigenvalues and eigenvectors").hours(10).build());
        units.add(SyllabusUnit.builder().courseResource(math101).unitNumber(4).unitTitle("Unit 4: Vector Spaces").subtopics("Vector spaces, linear independence, basis, dimension, linear transformations and kernel").hours(8).build());

        // Physics 101 (5 Modules, 36 Hours)
        units.add(SyllabusUnit.builder().courseResource(physics101).unitNumber(1).unitTitle("Module I: Oscillations and Waves").subtopics("Simple harmonic motion, damped and forced oscillations, resonance, wave equation, transverse and longitudinal waves").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(physics101).unitNumber(2).unitTitle("Module II: Wave Optics").subtopics("Huygens principle, interference of light, Young double slit, Newton rings, thin films, Fraunhofer diffraction, diffraction grating").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(physics101).unitNumber(3).unitTitle("Module III: Electromagnetism").subtopics("Gradient, divergence and curl, Gauss divergence theorem, Stokes theorem, Gauss law, Ampere law, Faraday law, Maxwell equations").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(physics101).unitNumber(4).unitTitle("Module IV: Quantum Physics").subtopics("Wave-particle duality, de Broglie hypothesis, Heisenberg uncertainty principle, Schrodinger equation, particle in a box").hours(7).build());
        units.add(SyllabusUnit.builder().courseResource(physics101).unitNumber(5).unitTitle("Module V: Solid-State and Modern Physics").subtopics("Free electron theory of metals, band theory of solids, intrinsic and extrinsic semiconductors, lasers and optical fibers").hours(5).build());

        // Chemistry 101 (6 Modules, 36 Hours)
        units.add(SyllabusUnit.builder().courseResource(chemistry101).unitNumber(1).unitTitle("Module I: Atomic and Molecular Structure").subtopics("Schrodinger equation, particle in a box, MO theory for homonuclear and heteronuclear diatomics, band structure of solids").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(chemistry101).unitNumber(2).unitTitle("Module II: Spectroscopic Techniques and Applications").subtopics("Principles of spectroscopy, selection rules, UV-Vis, fluorescence, vibrational and rotational spectroscopy, NMR").hours(7).build());
        units.add(SyllabusUnit.builder().courseResource(chemistry101).unitNumber(3).unitTitle("Module III: Intermolecular Forces and Potential Energy Surfaces").subtopics("Ionic, dipolar and van der Waals forces, real gases, equations of state, potential energy surfaces of simple molecules").hours(4).build());
        units.add(SyllabusUnit.builder().courseResource(chemistry101).unitNumber(4).unitTitle("Module IV: Use of Free Energy in Chemical Equilibria").subtopics("Thermodynamic functions: energy, entropy and free energy, cell potentials, Nernst equation and applications, acid-base and oxidation-reduction").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(chemistry101).unitNumber(5).unitTitle("Module V: Periodic Properties").subtopics("Effective nuclear charge, penetration, atomic and ionic sizes, ionization energies, electron affinity, electronegativity, polarizability").hours(4).build());
        units.add(SyllabusUnit.builder().courseResource(chemistry101).unitNumber(6).unitTitle("Module VI: Stereochemistry and Organic Reactions").subtopics("Representations of 3D structures, structural isomers and stereoisomers, chirality, enantiomers, diastereomers, synthesis of Aspirin and Paracetamol").hours(7).build());

        // Electrical 101
        units.add(SyllabusUnit.builder().courseResource(ee101).unitNumber(1).unitTitle("Unit 1: DC Circuits & Theorems").subtopics("Kirchhoff laws, Superposition, Thevenin, Norton, Maximum Power Transfer theorems, Star-Delta").hours(9).build());
        units.add(SyllabusUnit.builder().courseResource(ee101).unitNumber(2).unitTitle("Unit 2: AC Circuits & Resonance").subtopics("Sinusoidal waveforms, phasor representation, series/parallel RLC circuits, series resonance, Q-factor").hours(9).build());
        units.add(SyllabusUnit.builder().courseResource(ee101).unitNumber(3).unitTitle("Unit 3: Transformers").subtopics("BH characteristics, ideal and practical transformer, equivalent circuit, losses, efficiency").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(ee101).unitNumber(4).unitTitle("Unit 4: Electrical Machines").subtopics("Rotating magnetic fields, 3-phase induction motor, torque-slip curve, DC machines").hours(8).build());

        // Programming 101
        units.add(SyllabusUnit.builder().courseResource(cs101).unitNumber(1).unitTitle("Unit 1: Intro to C & Algorithms").subtopics("Flowcharts, pseudocode, compilation, data types, arithmetic operators").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(cs101).unitNumber(2).unitTitle("Unit 2: Conditionals & Loops").subtopics("if-else, switch, for, while, do-while, nested loops, break, continue").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(cs101).unitNumber(3).unitTitle("Unit 3: Arrays & Strings").subtopics("1D and 2D arrays, matrix multiplication, character strings, string library functions").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(cs101).unitNumber(4).unitTitle("Unit 4: Functions & Recursion").subtopics("Call by value, call by reference, recursive functions: Factorial, Fibonacci, Tower of Hanoi").hours(8).build());
        units.add(SyllabusUnit.builder().courseResource(cs101).unitNumber(5).unitTitle("Unit 5: Pointers & Dynamic Memory").subtopics("Pointers, pointer arithmetic, malloc, calloc, free, structures and unions").hours(8).build());

        // English 101
        units.add(SyllabusUnit.builder().courseResource(english101).unitNumber(1).unitTitle("Unit 1: Vocabulary Building").subtopics("Word formation, prefixes, suffixes, root words, synonyms and antonyms").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(english101).unitNumber(2).unitTitle("Unit 2: Writing & Paragraph Coherence").subtopics("Sentence structures, punctuation, coherence, précis writing, essay drafting").hours(6).build());
        units.add(SyllabusUnit.builder().courseResource(english101).unitNumber(3).unitTitle("Unit 3: Professional Communication").subtopics("Technical reports, formal letters, email etiquette, CV and resume design").hours(8).build());

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
