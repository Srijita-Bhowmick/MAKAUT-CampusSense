-- V2__seed_makaut_data.sql
-- Seed MAKAUT Colleges, Academic Resources, and Placement Statistics

-- 1. Insert Colleges
INSERT INTO colleges (name, code, location) VALUES
('Institute of Engineering & Management (IEM)', '109', 'Sector V, Salt Lake, Kolkata'),
('Heritage Institute of Technology (HITK)', '126', 'Anandapur, Kolkata'),
('Techno Main Salt Lake (TMSL)', '104', 'Sector V, Salt Lake, Kolkata'),
('Haldia Institute of Technology (HIT)', '103', 'Haldia, Purba Medinipur'),
('Netaji Subhash Engineering College (NSEC)', '108', 'Garia, Kolkata')
ON CONFLICT (code) DO NOTHING;

-- 2. Insert Academic Resources
INSERT INTO academic_resources (stream, semester, subject_code, subject_name, resource_type, year, file_url, raw_text, metadata) VALUES
(
    'CSE', 5, 'PCC-CS501', 'Operating Systems', 'SYLLABUS', 2023,
    'https://makautexam.net/syllabus/BTech_CSE_5th_Sem_PCC-CS501.pdf',
    'Module 1: Introduction: Concept of Operating Systems, Generations of Operating systems, Types of Operating Systems, OS Services, System Calls, Structure of an OS - Layered, Monolithic, Microkernel.
Module 2: Processes: Definition, Process Relationship, Different states of a Process, Process State transitions, Process Control Block (PCB), Context switching. Thread: Definition, Various states, Benefits of threads, Types of threads, Concept of multithreads. Process Scheduling: Foundation and Scheduling objectives, Types of Schedulers, Scheduling criteria: CPU utilization, Throughput, Turnaround Time, Waiting Time, Response Time; Scheduling algorithms: Pre-emptive and Non pre-emptive, FCFS, SJF, RR; Multiprocessor scheduling.
Module 3: Inter-process Communication: Critical Section, Race Conditions, Mutual Exclusion, Hardware Solution, Strict Alternation, Peterson Solution, The Producer Consumer Problem, Semaphores, Event Counters, Monitors, Message Passing, Classical IPC Problems: Reader Writer Problem, Dining Philosopher Problem.
Module 4: Deadlocks: Definition, Necessary and sufficient conditions for Deadlock, Deadlock Prevention, Deadlock Avoidance: Banker algorithm, Deadlock detection and Recovery.
Module 5: Memory Management: Basic concept, Logical and Physical address map, Memory allocation: Contiguous Memory allocation - Fixed and variable partition- Internal and External fragmentation and Compaction; Paging: Principle of operation - Page allocation - Hardware support for paging, Protection and sharing, Disadvantages of paging. Virtual Memory: Basics of Virtual Memory - Hardware and control structures - Locality of reference, Page fault, Working Set, Dirty page/Dirty bit - Demand paging, Page Replacement algorithms: Optimal, First in First Out (FIFO), Second Chance (SC), Not recently used (NRU) and Least Recently used (LRU).
Module 6: I/O Hardware: I/O devices, Device controllers, Direct Memory Access Principles of I/O Software: Goals of Interrupt handlers, Device drivers, Device independent I/O software. Secondary-Storage Structure: Disk structure, Disk scheduling algorithms (FCFS, SSTF, SCAN, C-SCAN).',
    '{"regulations": "AICTE CBCS 2018", "credits": 3, "contact_hours": "3L:0T:0P", "internal_assessment_marks": 30, "end_sem_marks": 70}'::jsonb
),
(
    'CSE', 5, 'PCC-CS501', 'Operating Systems', 'PYQ', 2022,
    'https://makautexam.net/pyq/2022/PCC-CS501_2022.pdf',
    'MAKAUT B.TECH 5TH SEMESTER EXAMINATION 2022 - OPERATING SYSTEMS (PCC-CS501)
GROUP A (Multiple Choice Questions)
1. Which of the following scheduling algorithms is non-preemptive? (a) Round Robin (b) FCFS (c) SRTF (d) Priority Scheduling with preemption.
GROUP B (Short Answer Questions)
2. Explain the difference between internal and external fragmentation with suitable diagrams.
3. What are the four necessary conditions for a deadlock to occur?
GROUP C (Long Answer Questions)
4. (a) What is the Producer-Consumer problem? How can it be solved using counting semaphores? Explain with code snippet. (b) Solve Banker Algorithm for given allocation and max matrices: determine if the system is in safe state and calculate safe sequence.
5. Explain Demand Paging and evaluate Page Replacement algorithms (FIFO, LRU, Optimal) for reference string: 7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2 with 3 page frames.',
    '{"exam_year": 2022, "paper_code": "PCC-CS501", "total_marks": 70, "duration": "3 hours"}'::jsonb
),
(
    'CSE', 5, 'PCC-CS502', 'Database Management Systems', 'SYLLABUS', 2023,
    'https://makautexam.net/syllabus/BTech_CSE_5th_Sem_PCC-CS502.pdf',
    'Module 1: Database System Concepts & Architecture: Data Models, Schemas & Instances, Three-Schema Architecture & Data Independence, Database languages & Interfaces, Centralized & Client/Server Architectures.
Module 2: Entity-Relationship Model: Entity Types, Entity Sets, Attributes and Keys, Relationship Types, Weak Entity Types, Enhanced E-R (EER) Modeling.
Module 3: Relational Model & Relational Algebra: Relational Model Concepts, Relational Constraints, Relational Database Schemas, Relational Algebra operations: SELECT, PROJECT, JOIN, DIVISION. SQL: DDL, DML, Complex queries, Triggers, Views.
Module 4: Relational Database Design: Functional Dependencies, Normal Forms: 1NF, 2NF, 3NF, BCNF, 4NF, Lossless Join Decomposition, Dependency Preservation.
Module 5: Transaction Processing & Concurrency Control: ACID properties, Transaction states, Concurrent Executions, Serializability, Lock-based Protocols, Two-Phase Locking (2PL), Timestamp-based Protocols, Deadlock handling.
Module 6: Storage and Indexing: RAID, File Organization, B-trees, B+ Trees indexing techniques.',
    '{"regulations": "AICTE CBCS 2018", "credits": 3, "contact_hours": "3L:0T:0P", "internal_assessment_marks": 30, "end_sem_marks": 70}'::jsonb
),
(
    'GENERAL', 0, 'MAR-100', 'Mandatory Additional Requirements (MAR)', 'REGULATION', 2023,
    'https://makautexam.net/notices/MAR_Guidelines_2023.pdf',
    'MAKAUT MANDATORY ADDITIONAL REQUIREMENTS (MAR) ACTIVITY GUIDELINES:
Every student admitted to 4-year B.Tech program under MAKAUT from 2018-19 onwards must earn a minimum of 100 MAR Activity Points over 4 years to qualify for the Degree (75 points for lateral entry students admitted in 3rd semester).
MAR Categories and Points Breakdown:
1. Massive Open Online Courses (MOOCs) with Certificate: 20 points per course.
2. Participation in Tech Fest / Teachers Day / Engineers Day: 5 points per event (Organizing: 10 points).
3. Tree Plantation / Environmental Protection Drive: 10 points per tree planted and nurtured with geotagged photo.
4. Blood Donation / Free Medical Camp Participation: 8 points per camp.
5. Debate / Extempore / Quiz / Essay Competition: 10 points (State/National Level: 20 points).
6. Sports / Games Participation: Inter-college: 10 points; University/State: 20 points.
7. Publication of Paper in Journal / Conference: 15 points per paper.
8. Hackathon / Coding Contest: Participation: 10 points; Winner: 20 points.
Submission Procedure: Upload documentary proofs on student MAKAUT portal (makautexam.net) before end of each even semester for college MAR coordinator verification.',
    '{"category": "Degree Requirement", "mandatory_points": 100, "lateral_points": 75}'::jsonb
),
(
    'GENERAL', 0, 'MOOCS-HONOURS', 'MOOCs for B.Tech Honours', 'REGULATION', 2023,
    'https://makautexam.net/notices/MOOCS_Honours_Guidelines.pdf',
    'MAKAUT GUIDELINES FOR B.TECH (HONOURS) DEGREE VIA MOOCS:
In accordance with AICTE model curriculum, students seeking B.Tech with Honours degree must acquire an additional 20 credits beyond the 160 credits of regular curriculum.
Key Rules:
1. Courses must be completed through SWAYAM / NPTEL portal from the university-notified approved course basket.
2. Credit distribution: Minimum of 20 credits to be earned between 3rd to 8th semester (recommended: 8-week courses = 2 credits, 12-week courses = 3 credits).
3. Minimum Qualifying Mark: Student must secure Certificate of Completion with at least 40% (or Elite / Silver / Gold).
4. No backlogs allowed at the time of graduation to remain eligible for Honours degree.
5. MOOCs credits cannot be double-counted for regular elective requirements.',
    '{"category": "Honours Degree", "required_credits": 20, "platform": "SWAYAM/NPTEL"}'::jsonb
);

-- 3. Insert Placement Records
-- IEM Kolkata (Code 109)
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Amazon', 44.00, 44.00, 18, 'https://iem.edu.in/placements/2023' FROM colleges WHERE code = '109';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Adobe', 42.50, 42.50, 6, 'https://iem.edu.in/placements/2023' FROM colleges WHERE code = '109';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'PwC India', 9.00, 7.50, 85, 'https://iem.edu.in/placements/2023' FROM colleges WHERE code = '109';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'TCS (Ninja & Digital)', 7.50, 4.50, 320, 'https://iem.edu.in/placements/2023' FROM colleges WHERE code = '109';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Overall IEM Summary', 72.00, 8.50, 1150, 'https://iem.edu.in/placements/2023' FROM colleges WHERE code = '109';

INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2024, 'Overall IEM Summary', 56.00, 8.20, 1080, 'https://iem.edu.in/placements/2024' FROM colleges WHERE code = '109';

-- Heritage Institute of Technology (Code 126)
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Microsoft', 42.00, 42.00, 4, 'https://heritageit.edu/placements/2023' FROM colleges WHERE code = '126';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Amazon', 38.00, 38.00, 12, 'https://heritageit.edu/placements/2023' FROM colleges WHERE code = '126';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Cognizant', 6.75, 4.25, 240, 'https://heritageit.edu/placements/2023' FROM colleges WHERE code = '126';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Overall Heritage Summary', 42.00, 7.20, 920, 'https://heritageit.edu/placements/2023' FROM colleges WHERE code = '126';

INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2024, 'Overall Heritage Summary', 45.00, 7.50, 890, 'https://heritageit.edu/placements/2024' FROM colleges WHERE code = '126';

-- Techno Main Salt Lake (Code 104)
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Amazon AWS', 36.00, 36.00, 8, 'https://tmsl.edu.in/placements/2023' FROM colleges WHERE code = '104';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Capgemini', 7.50, 4.50, 210, 'https://tmsl.edu.in/placements/2023' FROM colleges WHERE code = '104';
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Overall TMSL Summary', 36.00, 6.20, 850, 'https://tmsl.edu.in/placements/2023' FROM colleges WHERE code = '104';

INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2024, 'Overall TMSL Summary', 38.00, 6.50, 810, 'https://tmsl.edu.in/placements/2024' FROM colleges WHERE code = '104';

-- Haldia Institute of Technology (Code 103)
INSERT INTO placement_stats (college_id, batch_year, company_name, highest_ctc, average_ctc, total_offers, source_url)
SELECT id, 2023, 'Overall HIT Summary', 32.00, 5.80, 780, 'https://hithaldia.in/placements/2023' FROM colleges WHERE code = '103';
