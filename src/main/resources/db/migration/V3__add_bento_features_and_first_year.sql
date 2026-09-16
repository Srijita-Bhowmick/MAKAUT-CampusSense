-- V3__add_bento_features_and_first_year.sql
-- MAKAUT CampusSense: Bento Features, 1st-Year Common Ingestion & Panic Pack

-- 1. Syllabus Units for Checklists
CREATE TABLE IF NOT EXISTS syllabus_units (
    id BIGSERIAL PRIMARY KEY,
    course_resource_id BIGINT NOT NULL REFERENCES academic_resources(id) ON DELETE CASCADE,
    unit_number INT NOT NULL,
    unit_title VARCHAR(255) NOT NULL,
    subtopics TEXT,
    hours INT DEFAULT 8
);
CREATE INDEX IF NOT EXISTS idx_syllabus_units_res ON syllabus_units(course_resource_id);

-- 2. Frequent Questions (Panic Pack)
CREATE TABLE IF NOT EXISTS frequent_questions (
    id BIGSERIAL PRIMARY KEY,
    subject_code VARCHAR(50) NOT NULL,
    topic_name VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    marks INT NOT NULL,
    frequency_count INT NOT NULL DEFAULT 2,
    exam_years TEXT NOT NULL,
    group_type VARCHAR(50) NOT NULL,
    model_answer_snippet TEXT
);
CREATE INDEX IF NOT EXISTS idx_freq_q_subject ON frequent_questions(subject_code);

-- 3. User Bookmarks (Saved Revision Deck)
CREATE TABLE IF NOT EXISTS user_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_identifier VARCHAR(100) NOT NULL,
    question_id BIGINT REFERENCES frequent_questions(id) ON DELETE CASCADE,
    resource_id BIGINT REFERENCES academic_resources(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_bookmarks_user ON user_bookmarks(user_identifier);

-- 4. User Syllabus Progress Tracking
CREATE TABLE IF NOT EXISTS user_syllabus_progress (
    id BIGSERIAL PRIMARY KEY,
    user_identifier VARCHAR(100) NOT NULL,
    syllabus_unit_id BIGINT NOT NULL REFERENCES syllabus_units(id) ON DELETE CASCADE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_syllabus_unit UNIQUE (user_identifier, syllabus_unit_id)
);
CREATE INDEX IF NOT EXISTS idx_syllabus_progress_user ON user_syllabus_progress(user_identifier);

-- 5. Feedback Submissions
CREATE TABLE IF NOT EXISTS feedback_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_identifier VARCHAR(100),
    category VARCHAR(50) NOT NULL,
    subject_code VARCHAR(50),
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Seed First-Year Common Academic Resources
INSERT INTO academic_resources (stream, semester, subject_code, subject_name, resource_type, year, file_url, raw_text, metadata)
VALUES
('COMMON', 1, 'BS-M101', 'Mathematics 1A (Calculus & Linear Algebra)', 'SYLLABUS', 2023, 'https://makautexam.net/syllabus/BS-M101.pdf',
 'Module 1: Calculus - Evolutes, involutes, Rolle theorem, Mean Value Theorems, Taylor and Maclaurin theorems with remainders.
  Module 2: Multivariable Calculus - Partial derivatives, Euler theorem on homogeneous functions, total derivative, maxima and minima of functions of two variables, Lagrange multipliers.
  Module 3: Matrices - Rank of matrix, system of linear equations, symmetric, skew-symmetric, orthogonal matrices, eigenvalues and eigenvectors, Cayley-Hamilton Theorem, diagonalization of matrices.
  Module 4: Vector Spaces - Vector space, linear dependence and independence of vectors, basis, dimension, linear transformations.',
 '{"credits": 4, "department": "1st Year Common"}'),

('COMMON', 1, 'BS-PH101', 'Physics (Optics & Quantum Mechanics)', 'SYLLABUS', 2023, 'https://makautexam.net/syllabus/BS-PH101.pdf',
 'Module 1: Wave Optics - Huygens principle, interference, Young double slit, Newton rings, thin film interference, Fraunhofer diffraction, diffraction grating, polarization.
  Module 2: Quantum Mechanics - Blackbody radiation, Planck law, photoelectric effect, de Broglie hypothesis, Heisenberg uncertainty principle, wave function, Schrodinger equation, particle in a box.
  Module 3: Lasers - Einstein coefficients, population inversion, Ruby laser, He-Ne laser, optical fibers, numerical aperture.
  Module 4: Electromagnetism - Gauss law, Ampere law, Faraday law, Maxwell equations in vacuum and dielectric media, Poynting vector.',
 '{"credits": 4, "department": "1st Year Common"}'),

('COMMON', 1, 'ES-EE101', 'Basic Electrical Engineering', 'SYLLABUS', 2023, 'https://makautexam.net/syllabus/ES-EE101.pdf',
 'Module 1: DC Circuits - Kirchhoff laws, mesh and nodal analysis, superposition theorem, Thevenin theorem, Norton theorem, Maximum Power Transfer theorem.
  Module 2: AC Circuits - Representation of sinusoidal waveforms, peak, RMS, average values, phasor representation of R, L, C, series and parallel RLC circuits, resonance.
  Module 3: Transformers - Magnetic materials, BH characteristics, ideal and practical single-phase transformer, equivalent circuit, losses, regulation and efficiency.
  Module 4: Electrical Machines - Generation of rotating magnetic fields, construction and principle of 3-phase induction motor, DC machines, torque-speed characteristics.',
 '{"credits": 4, "department": "1st Year Common"}'),

('COMMON', 1, 'BS-CH101', 'Chemistry-I (Chemical Principles & Spectroscopy)', 'SYLLABUS', 2023, 'https://makautexam.net/syllabus/BS-CH101.pdf',
 'Module 1: Atomic and Molecular Structure - Schrodinger equation, particle in a box, MO theory, band structure of solids.
  Module 2: Spectroscopic Techniques - Principles of spectroscopy, selection rules, UV-Visible, Fluorescence, IR and Raman spectroscopy.
  Module 3: Intermolecular Forces - Real gases, equations of state, potential energy surfaces.
  Module 4: Periodic Properties - Effective nuclear charge, ionization energies, electron affinity, electronegativity, oxidation states.
  Module 5: Stereochemistry and Organic Synthesis - Chirality, isomers, synthesis of Aspirin and Paracetamol.',
 '{"credits": 4, "contact_hours": "3L:1T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'ES-CS101', 'Programming for Problem Solving', 'SYLLABUS', 2023, 'https://makautexam.net/syllabus/ES-CS101.pdf',
 'Module 1: Introduction to Programming - Computer architecture, algorithms, flowcharts, compilation, variables, data types, operators.
  Module 2: Control Structures - Conditional branching (if, else-if, switch), iteration and loops (for, while, do-while), break and continue.
  Module 3: Arrays & Strings - 1D and 2D arrays, matrix operations, string handling functions, character arrays.
  Module 4: Functions & Recursion - Parameter passing (call by value, call by reference), scope, storage classes, recursive functions (GCD, Factorial, Tower of Hanoi).
  Module 5: Pointers & Structures - Pointer arithmetic, pointers and arrays, dynamic memory allocation (malloc, calloc, free), structures, union.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'HM-HU101', 'English & Professional Communication', 'SYLLABUS', 2023, 'https://makautexam.net/syllabus/HM-HU101.pdf',
 'Module 1: Vocabulary Building - Root words, synonyms, antonyms, prefixes and suffixes, standard abbreviations.
  Module 2: Basic Writing Skills - Sentence structures, punctuation, coherence, paragraph writing, precis writing.
  Module 3: Professional Communication - Formal letter writing, email etiquette, CV and resume design, technical report writing.
  Module 4: Oral Communication - Phonetics, listening comprehension, presentation skills, group discussions.',
 '{"credits": 2, "contact_hours": "2L:0T:0P", "department": "1st Year Common"}')
ON CONFLICT DO NOTHING;

-- 7. Seed Panic Pack High-Yield Questions
INSERT INTO frequent_questions (subject_code, topic_name, question_text, marks, frequency_count, exam_years, group_type, model_answer_snippet)
VALUES
('BS-CH101', 'Electrochemistry & Nernst Equation', 'Derive the Nernst equation for single electrode potential and cell EMF. Calculate the EMF of the cell: Zn | Zn2+(0.01M) || Cu2+(0.1M) | Cu at 298 K (Given E°_cell = 1.10 V).', 10, 5, '2019, 2021, 2022, 2023, 2024', 'Group C',
 'From Gibbs free energy Delta G = Delta G° + RT ln Q and Delta G = -nFE, we obtain E = E° - (2.303 RT / nF) log Q. At 298 K, E = E° - (0.0591 / n) log([Zn2+]/[Cu2+]) = 1.10 - (0.0591 / 2) log(0.01/0.1) = 1.10 + 0.0295 = 1.1295 V.'),

('BS-CH101', 'Conductometric Titrations', 'Discuss the principle of conductometric titration. Sketch and explain the conductometric titration curves for: (i) Strong acid vs Strong base, and (ii) Weak acid vs Strong base.', 5, 4, '2019, 2021, 2022, 2023', 'Group B',
 'Conductance depends on number and mobility of ions. For HCl vs NaOH: Conductance decreases sharply as fast H+ is replaced by slower Na+, reaches minimum at equivalence, then rises steeply due to excess fast OH-. For CH3COOH vs NaOH: Initial decrease, then steady rise due to CH3COONa formation, and sharp rise post-equivalence.'),

('BS-CH101', 'Crystal Field Theory (CFT)', 'Explain the crystal field splitting of d-orbitals in octahedral and tetrahedral complexes with orbital energy diagrams. Calculate CFSE for d5 and d6 octahedral complexes in high-spin and low-spin states.', 10, 5, '2018, 2020, 2022, 2023, 2024', 'Group C',
 'Under octahedral field, degeneracy of five d-orbitals is lifted into lower t2g (dxy, dyz, dzx) at -0.4 Delta_o and higher eg (dx2-y2, dz2) at +0.6 Delta_o. In tetrahedral field, splitting is reversed into lower e (-0.6 Delta_t) and higher t2 (+0.4 Delta_t) where Delta_t = (4/9) Delta_o.'),

('BS-CH101', 'Organic Synthesis (Drugs)', 'Outline the chemical synthesis of: (i) Aspirin from salicylic acid, and (ii) Paracetamol from p-nitrophenol. Give reactions, reaction conditions, and their medicinal mechanisms.', 5, 4, '2020, 2022, 2023, 2024', 'Group B',
 'Aspirin: Salicylic acid + acetic anhydride in presence of conc. H2SO4 at 50-60°C gives acetylsalicylic acid (aspirin) + acetic acid. Acts as irreversible COX inhibitor. Paracetamol: p-nitrophenol reduced (Fe/HCl) to p-aminophenol, then acetylated with acetic anhydride/glacial acetic acid to give acetaminophen (paracetamol). Acts as antipyretic/analgesic.'),

('ES-EE101', 'Network Theorems', 'State Thevenin theorem. For a given bridge circuit with 10V DC source, find the Thevenin equivalent voltage (Vth) and Thevenin resistance (Rth) across load terminals AB.', 10, 5, '2019, 2021, 2022, 2023, 2024', 'Group C',
 'Thevenin Theorem: Any linear bilateral DC network across two terminals can be replaced by an equivalent voltage source Vth in series with resistance Rth. Vth is open-circuit voltage across AB. Rth is equivalent resistance looking into AB with independent sources turned off.'),

('ES-EE101', 'AC Resonance', 'Derive the condition for series resonance in an RLC circuit. Show that the resonant frequency is fr = 1 / (2 * pi * sqrt(L * C)). Calculate the Q-factor.', 5, 4, '2019, 2021, 2022, 2023', 'Group B',
 'At series resonance, inductive reactance equals capacitive reactance (X_L = X_C). Hence omega * L = 1 / (omega * C) => omega = 1 / sqrt(LC). Quality factor Q = (omega0 * L) / R = 1 / (R * sqrt(C/L)).'),

('ES-CS101', 'Pointers & Dynamic Memory', 'Explain the differences between malloc() and calloc() in C. Write a program to dynamically allocate memory for an array of n integers and find their average.', 5, 5, '2018, 2019, 2021, 2022, 2023', 'Group B',
 'malloc(size): allocates single contiguous block of specified bytes with garbage values. calloc(n, size): allocates n blocks of size bytes initialized to zero. Return void pointer or NULL on failure.'),

('ES-CS101', 'Recursion & Strings', 'Write a recursive C function to solve the Tower of Hanoi problem for n disks. Trace the function call tree for n = 3 disks.', 10, 4, '2019, 2021, 2022, 2024', 'Group C',
 'void towerOfHanoi(int n, char from, char to, char aux) { if (n == 1) { printf("Move disk 1 from %c to %c\\n", from, to); return; } towerOfHanoi(n - 1, from, aux, to); printf("Move disk %d from %c to %c\\n", n, from, to); towerOfHanoi(n - 1, aux, to, from); } Total moves: 2^n - 1 = 7.'),

('BS-M101', 'Cayley-Hamilton Theorem', 'State Cayley-Hamilton Theorem. Verify it for matrix A = [[1, 2], [3, 4]] and hence compute A^-1.', 10, 5, '2018, 2019, 2021, 2022, 2023', 'Group C',
 'Statement: Every square matrix satisfies its own characteristic equation: det(A - lambda * I) = 0. Characteristic polynomial: lambda^2 - tr(A)*lambda + det(A) = 0. Substituting A gives A^2 - 5A - 2I = 0. Multiplying by A^-1 gives A^-1 = (A - 5I) / 2.'),

('BS-PH101', 'Newton Rings', 'Explain the formation of Newton rings in reflected light. Prove that the diameters of dark rings are proportional to square roots of natural numbers.', 10, 4, '2019, 2021, 2022, 2023', 'Group C',
 'Newton rings are formed by interference between light reflected from upper surface of glass plate and lower surface of plano-convex lens. Path difference for reflected ray: Delta = 2*mu*t + lambda/2. For dark rings, 2*t = n*lambda. Since t = r^2 / (2R), D_n = sqrt(4*n*lambda*R) proportional to sqrt(n).'),

('PCC-CS501', 'Banker Algorithm', 'Explain Banker Algorithm for Deadlock Avoidance. Given Allocation, Max, and Available matrices for 5 processes and 3 resource types, determine if the system is in a safe state and compute the safe sequence.', 10, 4, '2019, 2021, 2022, 2023', 'Group C',
 'Banker Algorithm tests safety by simulating allocation. Computes Need = Max - Allocation. Checks if Need <= Available for a process, simulates release of its resources to Available, and repeats until all processes finish in a Safe Sequence e.g. <P1, P3, P4, P0, P2>.')
ON CONFLICT DO NOTHING;
