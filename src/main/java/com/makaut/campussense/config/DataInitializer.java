package com.makaut.campussense.config;

import com.makaut.campussense.aggregation.parsers.TextChunker;
import com.makaut.campussense.model.College;
import com.makaut.campussense.model.CourseResource;
import com.makaut.campussense.model.DocumentEmbedding;
import com.makaut.campussense.model.PlacementRecord;
import com.makaut.campussense.model.enums.ResourceType;
import com.makaut.campussense.model.enums.StreamType;
import com.makaut.campussense.repository.CollegeRepository;
import com.makaut.campussense.repository.CourseResourceRepository;
import com.makaut.campussense.repository.DocumentEmbeddingRepository;
import com.makaut.campussense.repository.PlacementRecordRepository;
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
    private final EmbeddingService embeddingService;
    private final TextChunker textChunker;

    @Override
    @Transactional
    public void run(String... args) {
        if (collegeRepository.count() > 0) {
            log.info("Dev database already populated. Skipping initialization.");
            return;
        }

        log.info("Seeding in-memory dev database with MAKAUT colleges, curriculum, and placement data...");

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

        // 2. Academic Resources
        CourseResource osSyllabus = CourseResource.builder()
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
                .metadata("{\"credits\": 3, \"regulation\": \"AICTE CBCS 2018\"}")
                .build();
        osSyllabus = courseResourceRepository.save(osSyllabus);

        CourseResource osPyq2022 = CourseResource.builder()
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
                .build();
        osPyq2022 = courseResourceRepository.save(osPyq2022);

        CourseResource dbmsSyllabus = CourseResource.builder()
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
                .metadata("{\"credits\": 3, \"regulation\": \"AICTE CBCS 2018\"}")
                .build();
        dbmsSyllabus = courseResourceRepository.save(dbmsSyllabus);

        CourseResource marGuide = CourseResource.builder()
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
                .build();
        marGuide = courseResourceRepository.save(marGuide);

        CourseResource moocsGuide = CourseResource.builder()
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
                .build();
        moocsGuide = courseResourceRepository.save(moocsGuide);

        // 3. Document Embeddings
        List<CourseResource> allResources = List.of(osSyllabus, osPyq2022, dbmsSyllabus, marGuide, moocsGuide);
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

        // 4. Placements
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

        log.info("Dev database initialization completed: {} colleges, {} resources, {} embeddings, {} placement records.",
                collegeRepository.count(), courseResourceRepository.count(), documentEmbeddingRepository.count(), placementRecordRepository.count());
    }
}
