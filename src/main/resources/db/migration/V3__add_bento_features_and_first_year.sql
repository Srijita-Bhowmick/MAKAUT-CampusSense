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

-- 6. Seed First-Year Common Academic Resources (Verified 2026 MAKAUT Syllabus - All 9 Subjects)
INSERT INTO academic_resources (stream, semester, subject_code, subject_name, resource_type, year, file_url, raw_text, metadata)
VALUES
('COMMON', 1, 'BS-M101', 'Mathematics - I', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Maths-1.pdf',
 'Module I: Calculus of a Single Variable - Rolle theorem, Mean Value Theorems, Taylor and Maclaurin expansions, indeterminate forms, definite and improper integrals.
  Module II: Calculus of Several Variables - Partial derivatives, Euler theorem, total derivative, maxima and minima, Lagrange multipliers.
  Module III: Multiple Integrals - Double and triple integrals, change of order, polar coordinates, area and volume.
  Module IV: Matrices and Linear Algebra - Rank, linear equations, eigenvalues and eigenvectors, Cayley-Hamilton theorem, diagonalization.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-BS-M101', 'Mathematics - I', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Maths-1.pdf',
 'Module I: Calculus of a Single Variable - Rolle theorem, Mean Value Theorems, Taylor and Maclaurin expansions, indeterminate forms, definite and improper integrals.
  Module II: Calculus of Several Variables - Partial derivatives, Euler theorem, total derivative, maxima and minima, Lagrange multipliers.
  Module III: Multiple Integrals - Double and triple integrals, change of order, polar coordinates, area and volume.
  Module IV: Matrices and Linear Algebra - Rank, linear equations, eigenvalues and eigenvectors, Cayley-Hamilton theorem, diagonalization.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'BS-PH101', 'Physics', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Physics.pdf',
 'Module I: Oscillations and Waves - Simple harmonic motion, damped and forced oscillations, resonance, wave equation.
  Module II: Wave Optics - Huygens principle, interference, Young double slit, Newton rings, thin film interference, Fraunhofer diffraction, diffraction grating.
  Module III: Electromagnetism - Gauss law, Ampere law, Faraday law, Maxwell equations, Poynting vector.
  Module IV: Quantum Physics - De Broglie hypothesis, Heisenberg uncertainty principle, Schrodinger equation, particle in a box.
  Module V: Solid-State and Modern Physics - Band theory of solids, semiconductors, lasers, optical fibers.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-BS-PH101', 'Physics', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Physics.pdf',
 'Module I: Oscillations and Waves - Simple harmonic motion, damped and forced oscillations, resonance, wave equation.
  Module II: Wave Optics - Huygens principle, interference, Young double slit, Newton rings, thin film interference, Fraunhofer diffraction, diffraction grating.
  Module III: Electromagnetism - Gauss law, Ampere law, Faraday law, Maxwell equations, Poynting vector.
  Module IV: Quantum Physics - De Broglie hypothesis, Heisenberg uncertainty principle, Schrodinger equation, particle in a box.
  Module V: Solid-State and Modern Physics - Band theory of solids, semiconductors, lasers, optical fibers.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'ES-EE101', 'Basic Electrical and Electronics Engineering', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/BEE.pdf',
 'Module I: Basics of Electrical Wiring and Safety - LT switchgear, MCB, ELCB, cables, earthing.
  Module II: Direct-Current Circuits - KVL, KCL, mesh and nodal analysis, Thevenin and Norton theorems.
  Module III: Alternating-Current Circuits - Phasor representation, RLC resonance, power factor.
  Module IV: Transformers and Rotating Machines - Single phase transformer, 3-phase induction motor.
  Module V: Semiconductor Fundamentals and Diode Applications - PN diode, rectifiers, filters.
  Module VI: Transistors and Operational Amplifiers - BJT, Op-Amp inverting and non-inverting.',
 '{"credits": 4, "contact_hours": "3L:1T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-ES-EE101', 'Basic Electrical and Electronics Engineering', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/BEE.pdf',
 'Module I: Basics of Electrical Wiring and Safety - LT switchgear, MCB, ELCB, cables, earthing.
  Module II: Direct-Current Circuits - KVL, KCL, mesh and nodal analysis, Thevenin and Norton theorems.
  Module III: Alternating-Current Circuits - Phasor representation, RLC resonance, power factor.
  Module IV: Transformers and Rotating Machines - Single phase transformer, 3-phase induction motor.
  Module V: Semiconductor Fundamentals and Diode Applications - PN diode, rectifiers, filters.
  Module VI: Transistors and Operational Amplifiers - BJT, Op-Amp inverting and non-inverting.',
 '{"credits": 4, "contact_hours": "3L:1T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'BS-CH101', 'Chemistry', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Chemistry.pdf',
 'Module I: Atomic and Molecular Structure - Schrodinger equation, particle in a box, MO theory.
  Module II: Thermodynamics, Equilibrium and Kinetics - Free energy, equilibria, reaction kinetics.
  Module III: Electrochemistry and Corrosion - Nernst equation, corrosion mechanisms and prevention.
  Module IV: Engineering Materials and Water - Polymers, composites, water softening.
  Module V: Instrumental Methods of Analysis - UV-Vis, IR, conductometry, potentiometry.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-BS-CH101', 'Chemistry', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Chemistry.pdf',
 'Module I: Atomic and Molecular Structure - Schrodinger equation, particle in a box, MO theory.
  Module II: Thermodynamics, Equilibrium and Kinetics - Free energy, equilibria, reaction kinetics.
  Module III: Electrochemistry and Corrosion - Nernst equation, corrosion mechanisms and prevention.
  Module IV: Engineering Materials and Water - Polymers, composites, water softening.
  Module V: Instrumental Methods of Analysis - UV-Vis, IR, conductometry, potentiometry.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'ES-CS101', 'Programming for Problem Solving and Introduction to AI', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/PPS.pdf',
 'Module I: Introduction to Computer - Memory, CPU, OS.
  Module II: Generation of Computers - Generations, von Neumann architecture.
  Module III: Problem Solving and Computational Thinking - Algorithms, flowcharts.
  Module IV: Fundamentals of C - Variables, operators, control flow.
  Module V: Functions and Program Structure - Functions, recursion.
  Module VI: Arrays, Strings and Pointers - Arrays, pointers, dynamic memory.
  Module VII: Structures, Files and Algorithm Efficiency - Structures, files, complexity.
  Module VIII: Introduction to Artificial Intelligence - AI foundations, agents, search, ML basics.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-ES-CS201', 'Programming for Problem Solving and Introduction to AI', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/PPS.pdf',
 'Module I: Introduction to Computer - Memory, CPU, OS.
  Module II: Generation of Computers - Generations, von Neumann architecture.
  Module III: Problem Solving and Computational Thinking - Algorithms, flowcharts.
  Module IV: Fundamentals of C - Variables, operators, control flow.
  Module V: Functions and Program Structure - Functions, recursion.
  Module VI: Arrays, Strings and Pointers - Arrays, pointers, dynamic memory.
  Module VII: Structures, Files and Algorithm Efficiency - Structures, files, complexity.
  Module VIII: Introduction to Artificial Intelligence - AI foundations, agents, search, ML basics.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'HM-HU101', 'English for Communication Skills', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/English.pdf',
 'Module I: The Grammar and Vocabulary of Technical English - Word formation, technical vocabulary.
  Module II: Reading and Comprehension - Skimming, scanning, critical reading.
  Module III: Technical Writing - Cohesion, formal letters, reports, CV.
  Module IV: Listening, Speaking and Employability - Phonetics, presentations, GD, interviews.',
 '{"credits": 2, "contact_hours": "2L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-AEC-101', 'English for Communication Skills', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/English.pdf',
 'Module I: The Grammar and Vocabulary of Technical English - Word formation, technical vocabulary.
  Module II: Reading and Comprehension - Skimming, scanning, critical reading.
  Module III: Technical Writing - Cohesion, formal letters, reports, CV.
  Module IV: Listening, Speaking and Employability - Phonetics, presentations, GD, interviews.',
 '{"credits": 2, "contact_hours": "2L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'BS-BIO201', 'Biology for Engineers', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Biology.pdf',
 'Module I: The Cell and the Molecules of Life - Biomolecules, enzymes, cell structure.
  Module II: Genetics and the Flow of Information - Mendelian genetics, central dogma.
  Module III: Biomaterials, Biomechanics and Bio-inspired Design - Biomaterials, biomechanics.
  Module IV: Biotechnology, Biosensors and Applications - rDNA, biosensors, bio-computing.',
 '{"credits": 2, "contact_hours": "2L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-BS-BI201', 'Biology for Engineers', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Biology.pdf',
 'Module I: The Cell and the Molecules of Life - Biomolecules, enzymes, cell structure.
  Module II: Genetics and the Flow of Information - Mendelian genetics, central dogma.
  Module III: Biomaterials, Biomechanics and Bio-inspired Design - Biomaterials, biomechanics.
  Module IV: Biotechnology, Biosensors and Applications - rDNA, biosensors, bio-computing.',
 '{"credits": 2, "contact_hours": "2L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, 'MC-ES201', 'Environmental Science', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/EVS.pdf',
 'Module I: The Environment, Ecosystems and Biodiversity - Ecosystems, biodiversity conservation.
  Module II: Environmental Pollution and the Global Challenges - Pollution, climate change, waste.
  Module III: India\'s Sustainability Policy and Legal Framework - EPA 1986, NGT, treaties.
  Module IV: Mission LiFE and Behavioural Change - Lifestyle for Environment, circular economy.
  Module V: The Seven Themes of Mission LiFE, and Practice - Energy, water, plastic, e-waste.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 1, '26-VAC-201', 'Environmental Science', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/EVS.pdf',
 'Module I: The Environment, Ecosystems and Biodiversity - Ecosystems, biodiversity conservation.
  Module II: Environmental Pollution and the Global Challenges - Pollution, climate change, waste.
  Module III: India\'s Sustainability Policy and Legal Framework - EPA 1986, NGT, treaties.
  Module IV: Mission LiFE and Behavioural Change - Lifestyle for Environment, circular economy.
  Module V: The Seven Themes of Mission LiFE, and Practice - Energy, water, plastic, e-waste.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 2, 'BS-M201', 'Mathematics - II', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Maths-2.pdf',
 'Module I: Ordinary Differential Equations - ODEs, variation of parameters.
  Module II: Laplace Transform - Laplace transform, inverse Laplace, ODE solution.
  Module III: Vector Calculus - Gradient, divergence, curl, Stokes theorem.
  Module IV: Complex Variables and Fourier Series - Analytic functions, Fourier series.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}'),

('COMMON', 2, '26-BS-M201', 'Mathematics - II', 'SYLLABUS', 2026, 'assets/resources/1st_year/syllabus/Maths-2.pdf',
 'Module I: Ordinary Differential Equations - ODEs, variation of parameters.
  Module II: Laplace Transform - Laplace transform, inverse Laplace, ODE solution.
  Module III: Vector Calculus - Gradient, divergence, curl, Stokes theorem.
  Module IV: Complex Variables and Fourier Series - Analytic functions, Fourier series.',
 '{"credits": 3, "contact_hours": "3L:0T:0P", "department": "1st Year Common"}')
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
