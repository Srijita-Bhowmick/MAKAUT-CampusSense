// Complete generator script for design.pen with all 10 non-overlapping frames
const fs = require('fs');
const path = require('path');

let idCounter = 10000;
function uid(prefix = 'n') {
  return prefix + (++idCounter).toString(36) + Math.random().toString(36).substring(2, 5);
}

function textNode(content, fontSize, fontWeight, fill, fontFamily = 'Inter', extra = {}) {
  const safeSize = Math.max(13, fontSize); // Enforce strictly >= 13px rule
  return {
    type: 'text',
    id: uid('t'),
    name: extra.name || (content.length > 24 ? content.substring(0, 24) + '...' : content),
    content,
    fontFamily,
    fontSize: safeSize,
    fontWeight: fontWeight || '400',
    fill,
    letterSpacing: extra.letterSpacing !== undefined ? extra.letterSpacing : (fontSize >= 24 ? -0.5 : 0),
    ...extra
  };
}

function iconNode(iconName, width, height, fill, extra = {}) {
  return {
    type: 'icon',
    id: uid('i'),
    name: extra.name || `${iconName} icon`,
    icon: iconName,
    library: 'lucide',
    width: width || 16,
    height: height || 16,
    fill,
    ...extra
  };
}

function frameNode(name, props = {}) {
  const node = {
    type: 'frame',
    id: uid('f'),
    name,
    layout: props.layout || 'horizontal',
    alignItems: props.alignItems || 'start',
    children: props.children || []
  };

  if (props.x !== undefined) node.x = props.x;
  if (props.y !== undefined) node.y = props.y;
  if (props.width !== undefined) node.width = props.width;
  if (props.height !== undefined) node.height = props.height;
  if (props.fill !== undefined) node.fill = props.fill;
  if (props.stroke !== undefined) node.stroke = props.stroke;
  if (props.strokeWidth !== undefined) node.strokeWidth = props.strokeWidth;
  if (props.strokeAlignment !== undefined) node.strokeAlignment = props.strokeAlignment;
  if (props.gap !== undefined) node.gap = props.gap;
  if (props.padding !== undefined) node.padding = props.padding;
  if (props.justifyContent !== undefined) node.justifyContent = props.justifyContent;
  if (props.cornerRadius !== undefined) node.cornerRadius = props.cornerRadius;
  if (props.clip !== undefined) node.clip = props.clip;
  if (props.opacity !== undefined) node.opacity = props.opacity;

  return node;
}

function rectNode(name, width, height, fill, cornerRadius = 0) {
  return {
    type: 'rectangle',
    id: uid('r'),
    name,
    width,
    height,
    fill,
    cornerRadius
  };
}

function ellipseNode(name, size, fill) {
  return {
    type: 'ellipse',
    id: uid('e'),
    name,
    width: size,
    height: size,
    fill
  };
}

// Tokens
const DARK = {
  bg: '#090A0F',
  surface1: '#111319',
  surface2: '#171A23',
  border: '#232734',
  borderSubtle: '#1D212C',
  textPrimary: '#F8FAFC',
  textSecondary: '#94A3B8',
  textMuted: '#64748B',
  mono: '#CBD5E1',
  indigo: '#4F46E5',
  indigoAccent: '#6366F1',
  emerald: '#10B981',
  amber: '#F59E0B',
  rose: '#EF4444'
};

const LIGHT = {
  bg: '#F8FAFC',
  surface1: '#FFFFFF',
  surface2: '#F1F5F9',
  border: '#E2E8F0',
  borderSubtle: '#CBD5E1',
  textPrimary: '#0F172A',
  textSecondary: '#475569',
  textMuted: '#64748B',
  mono: '#334155',
  indigo: '#4F46E5',
  indigoAccent: '#6366F1',
  emerald: '#059669',
  amber: '#D97706',
  rose: '#DC2626'
};

// -------------------------------------------------------------
// Component Builders
// -------------------------------------------------------------

function buildNavbar(theme, isLight = false) {
  return frameNode('Navbar', {
    width: 'fill_container',
    height: 56,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: { bottom: 1 },
    padding: [0, 40],
    layout: 'horizontal',
    justifyContent: 'space_between',
    alignItems: 'center',
    children: [
      // Left
      frameNode('Nav Left', {
        layout: 'horizontal',
        alignItems: 'center',
        gap: 12,
        children: [
          frameNode('Brand Monogram', {
            width: 28,
            height: 28,
            cornerRadius: 6,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              textNode('CS', 13, '700', theme.indigoAccent, 'Inter', { name: 'Logo' })
            ]
          }),
          textNode('CampusSense', 18, '700', theme.textPrimary, 'Inter', { letterSpacing: -0.3 }),
          frameNode('Version Badge', {
            cornerRadius: 4,
            padding: [2, 6],
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            children: [
              textNode('v2.6', 13, '500', theme.textMuted, 'Geist Mono')
            ]
          })
        ]
      }),
      // Center
      frameNode('Nav Center', {
        layout: 'horizontal',
        alignItems: 'center',
        gap: 20,
        children: [
          frameNode('Nav Curriculum', {
            padding: [8, 12],
            cornerRadius: 6,
            children: [textNode('Curriculum', 14, '500', theme.textPrimary)]
          }),
          frameNode('Nav PYQ', {
            padding: [8, 12],
            cornerRadius: 6,
            children: [textNode('PYQ Archives', 14, '500', theme.textSecondary)]
          }),
          frameNode('Nav Panic', {
            padding: [8, 12],
            cornerRadius: 6,
            children: [textNode('🔥 Panic Pack', 14, '500', theme.textSecondary)]
          })
        ]
      }),
      // Right
      frameNode('Nav Right', {
        layout: 'horizontal',
        alignItems: 'center',
        gap: 12,
        children: [
          frameNode('Theme Toggle', {
            width: 36,
            height: 36,
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              iconNode(isLight ? 'moon' : 'sun', 16, 16, theme.textSecondary)
            ]
          }),
          frameNode('Saved Deck Badge', {
            height: 36,
            cornerRadius: 8,
            padding: [0, 14],
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            layout: 'horizontal',
            alignItems: 'center',
            gap: 8,
            children: [
              iconNode('bookmark', 14, 14, theme.textSecondary),
              textNode('Saved Deck (4)', 13, '500', theme.mono, 'Inter')
            ]
          }),
          frameNode('Sign In Link', {
            height: 36,
            padding: [0, 10],
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              textNode('Sign In', 14, '500', theme.textSecondary)
            ]
          }),
          frameNode('Sign Up Button', {
            height: 44,
            cornerRadius: 8,
            padding: [0, 18],
            fill: theme.indigo,
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              textNode('Sign Up', 14, '600', '#FFFFFF')
            ]
          })
        ]
      })
    ]
  });
}

function buildStatusStrip(theme) {
  return frameNode('Academic Status Strip', {
    width: 'fill_container',
    height: 36,
    fill: theme.bg,
    stroke: theme.border,
    strokeWidth: { bottom: 1 },
    layout: 'horizontal',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    children: [
      ellipseNode('Status Dot', 6, theme.emerald),
      textNode('134 Subjects Indexed • 2,924 PYQ Papers • 6 Engineering Streams • MAKAUT 2018/2023 Curriculum Sync', 13, '500', theme.textSecondary, 'Geist Mono')
    ]
  });
}

function buildHeroSection(theme) {
  return frameNode('Hero Section', {
    width: 'fill_container',
    padding: [48, 40, 28, 40],
    layout: 'vertical',
    alignItems: 'center',
    gap: 18,
    children: [
      frameNode('Eyebrow Pill', {
        height: 32,
        padding: [0, 14],
        cornerRadius: 16,
        fill: theme.surface2,
        stroke: theme.border,
        strokeWidth: 1,
        layout: 'horizontal',
        alignItems: 'center',
        gap: 8,
        children: [
          ellipseNode('Eyebrow Dot', 6, theme.indigoAccent),
          textNode('MAKAUT B.Tech Academic Portal', 13, '600', theme.indigoAccent, 'Geist Mono')
        ]
      }),
      textNode('Syllabi, Past Papers & Exam Revision', 44, '700', theme.textPrimary, 'Inter', {
        letterSpacing: -1,
        textAlign: 'center'
      }),
      textNode('Curated MAKAUT B.Tech syllabus breakdowns, past examination paper archives, and student revision tools.', 16, '400', theme.textSecondary, 'Inter', {
        textAlign: 'center',
        width: 720
      }),
      // Command Search Bar
      frameNode('Command Search Bar', {
        width: 640,
        height: 48,
        cornerRadius: 10,
        fill: theme.surface2,
        stroke: theme.border,
        strokeWidth: 1,
        padding: [0, 16],
        layout: 'horizontal',
        alignItems: 'center',
        justifyContent: 'space_between',
        children: [
          frameNode('Search Input Content', {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 10,
            children: [
              iconNode('search', 18, 18, theme.textMuted),
              textNode('Search course code, subject, or topic (e.g. PCC-CS501, Automata)...', 14, '400', theme.textMuted)
            ]
          }),
          frameNode('Kbd Shortcut', {
            height: 24,
            padding: [0, 8],
            cornerRadius: 4,
            fill: theme.surface1,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              textNode('⌘K', 13, '600', theme.textSecondary, 'Geist Mono')
            ]
          })
        ]
      }),
      // Stream Filter Pills
      frameNode('Stream Filters', {
        layout: 'horizontal',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 8,
        children: [
          frameNode('Pill 1st Year', {
            height: 34,
            padding: [0, 14],
            cornerRadius: 17,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            children: [textNode('1st Year Common', 13, '500', theme.textSecondary)]
          }),
          frameNode('Pill CSE Active', {
            height: 34,
            padding: [0, 14],
            cornerRadius: 17,
            fill: theme.indigo,
            alignItems: 'center',
            children: [textNode('CSE', 13, '600', '#FFFFFF')]
          }),
          frameNode('Pill IT', {
            height: 34,
            padding: [0, 14],
            cornerRadius: 17,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            children: [textNode('IT', 13, '500', theme.textSecondary)]
          }),
          frameNode('Pill ECE', {
            height: 34,
            padding: [0, 14],
            cornerRadius: 17,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            children: [textNode('ECE', 13, '500', theme.textSecondary)]
          }),
          frameNode('Pill EE', {
            height: 34,
            padding: [0, 14],
            cornerRadius: 17,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            children: [textNode('EE', 13, '500', theme.textSecondary)]
          }),
          frameNode('Pill AIML', {
            height: 34,
            padding: [0, 14],
            cornerRadius: 17,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            alignItems: 'center',
            children: [textNode('CSE-AIML', 13, '500', theme.textSecondary)]
          })
        ]
      })
    ]
  });
}

function buildUpcomingExamBanner(theme) {
  return frameNode('Upcoming Exam Schedule Target Banner', {
    width: 1120,
    height: 76,
    cornerRadius: 12,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: 1,
    padding: [0, 24],
    layout: 'horizontal',
    justifyContent: 'space_between',
    alignItems: 'center',
    children: [
      frameNode('Banner Left', {
        layout: 'horizontal',
        alignItems: 'center',
        gap: 14,
        children: [
          iconNode('clock', 20, 20, theme.amber),
          textNode('T - 08d : 14h : 22m : 23s', 16, '700', theme.amber, 'Geist Mono'),
          rectNode('Banner Divider', 1, 24, theme.border),
          frameNode('Target Label Group', {
            layout: 'vertical',
            gap: 2,
            children: [
              textNode('MAKAUT Odd Semester Theory Examinations', 14, '600', theme.textPrimary),
              textNode('Official Routine Sync • 2024-25 Academic Session', 13, '400', theme.textSecondary)
            ]
          })
        ]
      }),
      frameNode('Banner Right Actions', {
        layout: 'horizontal',
        alignItems: 'center',
        gap: 10,
        children: [
          frameNode('Download Schedule Button', {
            height: 44,
            padding: [0, 16],
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            layout: 'horizontal',
            alignItems: 'center',
            gap: 8,
            children: [
              iconNode('download', 14, 14, theme.textPrimary),
              textNode('Download Verified Schedule', 13, '500', theme.textPrimary)
            ]
          }),
          frameNode('Open Revision Pack Button', {
            height: 44,
            padding: [0, 16],
            cornerRadius: 8,
            fill: theme.indigo,
            layout: 'horizontal',
            alignItems: 'center',
            gap: 8,
            children: [
              iconNode('arrow-right', 14, 14, '#FFFFFF'),
              textNode('Open 48-Hour Revision Pack', 13, '600', '#FFFFFF')
            ]
          })
        ]
      })
    ]
  });
}

function buildCourseCard(theme, code, codeColor, title, units) {
  return frameNode(`Card ${code}`, {
    width: 548,
    cornerRadius: 12,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: 1,
    padding: [22, 22, 0, 22],
    layout: 'vertical',
    gap: 14,
    children: [
      // Top Bar
      frameNode('Card Top Bar', {
        width: 'fill_container',
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('Code Badge', {
            padding: [4, 10],
            cornerRadius: 6,
            fill: theme.surface2,
            stroke: codeColor,
            strokeWidth: 1,
            children: [
              textNode(code, 13, '700', codeColor, 'Geist Mono')
            ]
          }),
          frameNode('Meta Pills', {
            layout: 'horizontal',
            gap: 8,
            alignItems: 'center',
            children: [
              frameNode('Credits Pill', {
                padding: [3, 8],
                cornerRadius: 12,
                fill: theme.surface2,
                children: [textNode('3.0 Credits', 13, '500', theme.textSecondary, 'Geist Mono')]
              }),
              textNode('3L:0T:0P', 13, '500', theme.textMuted, 'Geist Mono')
            ]
          })
        ]
      }),
      // Subject Title
      textNode(title, 18, '600', theme.textPrimary),
      // Divider
      rectNode('Divider', 'fill_container', 1, theme.border),
      // 4-unit syllabus checklist
      frameNode('Syllabus Checklist', {
        width: 'fill_container',
        layout: 'vertical',
        gap: 8,
        children: units.map((u, i) =>
          frameNode(`Unit ${i + 1}`, {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 10,
            children: [
              frameNode('Checkbox', {
                width: 18,
                height: 18,
                cornerRadius: 4,
                fill: theme.surface2,
                stroke: theme.border,
                strokeWidth: 1,
                alignItems: 'center',
                justifyContent: 'center',
                children: [
                  iconNode('check', 12, 12, codeColor)
                ]
              }),
              textNode(u, 13, '400', theme.textSecondary)
            ]
          })
        )
      }),
      // Flush bottom button
      frameNode('Syllabus Link Bar', {
        width: 'fill_container',
        height: 44,
        stroke: theme.border,
        strokeWidth: { top: 1 },
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          textNode('Open Syllabus Blueprint →', 13, '600', codeColor),
          ellipseNode('Link Dot', 6, codeColor)
        ]
      })
    ]
  });
}

function buildCoreSubjectGrid(theme) {
  const cards = [
    {
      code: 'PCC-CS501',
      color: '#60A5FA',
      title: 'Formal Language & Automata Theory',
      units: [
        'Finite Automata & Regular Languages',
        'Context-Free Grammars & Pushdown Automata',
        'Turing Machines & Decidability',
        'Complexity Classes P vs NP'
      ]
    },
    {
      code: 'PCC-CS502',
      color: '#A78BFA',
      title: 'Database Management Systems',
      units: [
        'Relational Model & SQL Fundamentals',
        'Normalization: 1NF → BCNF',
        'Transaction Management & ACID Properties',
        'Indexing, B+ Trees & Query Optimization'
      ]
    },
    {
      code: 'PCC-CS503',
      color: '#34D399',
      title: 'Operating Systems',
      units: [
        'Process Management & CPU Scheduling',
        'Memory Management & Virtual Paging',
        'File Systems & Storage Architecture',
        'Deadlocks & Concurrency Synchronization'
      ]
    },
    {
      code: 'PCC-CS504',
      color: '#FBBF24',
      title: 'Object-Oriented Programming with Java',
      units: [
        'Classes, Objects & Data Encapsulation',
        'Inheritance, Polymorphism & Interfaces',
        'Exception Handling & Collections Framework',
        'Multithreading, Concurrency & I/O Streams'
      ]
    }
  ];

  return frameNode('Core Subjects Section', {
    width: 1120,
    layout: 'vertical',
    gap: 16,
    children: [
      frameNode('Core Section Header', {
        layout: 'vertical',
        gap: 4,
        children: [
          textNode('5th Semester Core Subject Blueprints', 22, '700', theme.textPrimary),
          textNode('Curated module breakdowns, credit hours, and exam units verified against MAKAUT curriculum', 14, '400', theme.textSecondary)
        ]
      }),
      // Row 1
      frameNode('Cards Row 1', {
        layout: 'horizontal',
        gap: 24,
        children: [
          buildCourseCard(theme, cards[0].code, cards[0].color, cards[0].title, cards[0].units),
          buildCourseCard(theme, cards[1].code, cards[1].color, cards[1].title, cards[1].units)
        ]
      }),
      // Row 2
      frameNode('Cards Row 2', {
        layout: 'horizontal',
        gap: 24,
        children: [
          buildCourseCard(theme, cards[2].code, cards[2].color, cards[2].title, cards[2].units),
          buildCourseCard(theme, cards[3].code, cards[3].color, cards[3].title, cards[3].units)
        ]
      })
    ]
  });
}

function buildPanicPackSection(theme) {
  const drills = [
    {
      q: 'State and prove the Pumping Lemma for regular languages with an application contradiction.',
      marks: '15 Marks',
      freq: 'Seen 5x in Sem Exams',
      color: theme.rose,
      preview: 'Step 1: Pigeonhole principle on finite state sequence | Step 2: Decomposition into xyz | Step 3: Pumping iteration contradiction'
    },
    {
      q: 'Explain BCNF decomposition and demonstrate lossless join and dependency preservation.',
      marks: '10 Marks',
      freq: 'Seen 4x in Sem Exams',
      color: theme.amber,
      preview: 'Step 1: Check non-trivial FDs X → Y | Step 2: Determine if X is superkey | Step 3: Project and decompose R into R1 and R2'
    },
    {
      q: "Banker's Algorithm for Deadlock Avoidance: Safety and Resource-Request state proofs.",
      marks: '15 Marks',
      freq: 'Seen 5x in Sem Exams',
      color: theme.rose,
      preview: 'Step 1: Initialize Work and Finish allocation arrays | Step 2: Find index i with Need_i ≤ Work | Step 3: Update Work and verify safe state'
    }
  ];

  return frameNode('48-Hour Exam Panic Pack Section', {
    width: 1120,
    cornerRadius: 12,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: 1,
    padding: [24, 28],
    layout: 'vertical',
    gap: 16,
    children: [
      frameNode('Panic Header', {
        width: 'fill_container',
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('Panic Title Group', {
            layout: 'vertical',
            gap: 4,
            children: [
              textNode('🔥 High-Yield 48-Hour Exam Drill (Top 20% Repeated Questions)', 20, '700', theme.textPrimary),
              textNode('Exam frequency and syllabus citations indexed from past 10-year official papers', 13, '400', theme.textSecondary)
            ]
          }),
          frameNode('Ranked Pill', {
            padding: [4, 10],
            cornerRadius: 12,
            fill: theme.surface2,
            stroke: theme.amber,
            strokeWidth: 1,
            children: [
              textNode('Frequency-Ranked', 13, '600', theme.amber, 'Geist Mono')
            ]
          })
        ]
      }),
      // Question List
      frameNode('Panic Questions List', {
        width: 'fill_container',
        layout: 'vertical',
        gap: 12,
        children: drills.map((d, i) =>
          frameNode(`Question ${i + 1}`, {
            width: 'fill_container',
            padding: [16, 18],
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            layout: 'vertical',
            gap: 8,
            children: [
              frameNode('Q Top', {
                width: 'fill_container',
                layout: 'horizontal',
                justifyContent: 'space_between',
                alignItems: 'center',
                children: [
                  textNode(d.q, 15, '600', theme.textPrimary),
                  frameNode('Q Badges', {
                    layout: 'horizontal',
                    gap: 8,
                    children: [
                      frameNode('Freq Badge', {
                        padding: [3, 8],
                        cornerRadius: 4,
                        fill: theme.surface1,
                        children: [textNode(d.freq, 13, '600', d.color, 'Geist Mono')]
                      }),
                      frameNode('Marks Badge', {
                        padding: [3, 8],
                        cornerRadius: 4,
                        fill: theme.surface1,
                        children: [textNode(d.marks, 13, '700', theme.textPrimary, 'Geist Mono')]
                      })
                    ]
                  })
                ]
              }),
              textNode(d.preview, 13, '400', theme.textSecondary, 'Geist Mono')
            ]
          })
        )
      })
    ]
  });
}

function buildPYQArchivesSection(theme) {
  const papers = [
    { code: 'PCC-CS501', title: 'Formal Language & Automata Theory', year: '2023 Regular', pages: '4 Pages PDF' },
    { code: 'PCC-CS502', title: 'Database Management Systems', year: '2023 Regular', pages: '4 Pages PDF' },
    { code: 'PCC-CS503', title: 'Operating Systems', year: '2022 Regular', pages: '4 Pages PDF' },
    { code: 'PCC-CS504', title: 'Object-Oriented Programming with Java', year: '2022 Regular', pages: '4 Pages PDF' }
  ];

  return frameNode('10-Year Past Examination Papers Section', {
    width: 1120,
    layout: 'vertical',
    gap: 16,
    children: [
      frameNode('PYQ Header', {
        width: 'fill_container',
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('PYQ Title Group', {
            layout: 'vertical',
            gap: 4,
            children: [
              textNode('10-Year Past Examination Papers (PYQ Archives)', 20, '700', theme.textPrimary),
              textNode('Filterable by semester, stream, and academic year with in-browser previewer', 13, '400', theme.textSecondary)
            ]
          }),
          // Semester tabs
          frameNode('Semester Tabs', {
            layout: 'horizontal',
            gap: 6,
            children: ['Sem 1', 'Sem 2', 'Sem 3', 'Sem 4', 'Sem 5', 'Sem 6', 'Sem 7', 'Sem 8'].map(s =>
              frameNode(`Tab ${s}`, {
                height: 32,
                padding: [0, 12],
                cornerRadius: 6,
                fill: s === 'Sem 5' ? theme.indigo : theme.surface2,
                stroke: theme.border,
                strokeWidth: 1,
                alignItems: 'center',
                children: [
                  textNode(s, 13, s === 'Sem 5' ? '600' : '500', s === 'Sem 5' ? '#FFFFFF' : theme.textSecondary)
                ]
              })
            )
          })
        ]
      }),
      // Paper Grid
      frameNode('PYQ Grid', {
        layout: 'horizontal',
        gap: 16,
        children: papers.map(p =>
          frameNode(`Paper ${p.code}`, {
            width: 268,
            cornerRadius: 10,
            fill: theme.surface1,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [16, 16],
            layout: 'vertical',
            gap: 12,
            children: [
              frameNode('Paper Top', {
                width: 'fill_container',
                layout: 'horizontal',
                justifyContent: 'space_between',
                alignItems: 'center',
                children: [
                  textNode(p.code, 13, '700', theme.indigoAccent, 'Geist Mono'),
                  frameNode('Year Badge', {
                    padding: [2, 6],
                    cornerRadius: 4,
                    fill: theme.surface2,
                    children: [textNode(p.year, 13, '500', theme.textSecondary, 'Geist Mono')]
                  })
                ]
              }),
              textNode(p.title, 15, '600', theme.textPrimary),
              textNode(p.pages, 13, '400', theme.textMuted, 'Geist Mono'),
              frameNode('Paper Actions', {
                width: 'fill_container',
                layout: 'horizontal',
                gap: 8,
                children: [
                  frameNode('Preview Button', {
                    height: 36,
                    padding: [0, 10],
                    cornerRadius: 6,
                    fill: theme.surface2,
                    stroke: theme.border,
                    strokeWidth: 1,
                    alignItems: 'center',
                    children: [textNode('Preview Paper', 13, '500', theme.textPrimary)]
                  }),
                  frameNode('Download Button', {
                    height: 36,
                    padding: [0, 10],
                    cornerRadius: 6,
                    fill: theme.indigo,
                    alignItems: 'center',
                    children: [textNode('Download', 13, '600', '#FFFFFF')]
                  })
                ]
              })
            ]
          })
        )
      })
    ]
  });
}

function buildFooter(theme) {
  return frameNode('Footer', {
    width: 'fill_container',
    height: 90,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: { top: 1 },
    padding: [0, 40],
    layout: 'horizontal',
    justifyContent: 'space_between',
    alignItems: 'center',
    children: [
      frameNode('Footer Left', {
        layout: 'vertical',
        gap: 4,
        children: [
          textNode('CampusSense Academic Engineering Intelligence • v2.6', 14, '600', theme.textPrimary),
          textNode('Independent academic initiative. Not officially affiliated with or endorsed by Maulana Abul Kalam Azad University of Technology (MAKAUT, WB).', 13, '400', theme.textMuted)
        ]
      }),
      frameNode('Footer Links', {
        layout: 'horizontal',
        gap: 16,
        children: [
          textNode('Privacy Policy', 13, '500', theme.textSecondary),
          textNode('Syllabus Index', 13, '500', theme.textSecondary),
          textNode('Exam Regulations', 13, '500', theme.textSecondary),
          textNode('Student Workspace', 13, '500', theme.textSecondary)
        ]
      })
    ]
  });
}

// -------------------------------------------------------------
// Screen 1 & 4: Public Academic Portal (1440x2200)
// -------------------------------------------------------------
function buildScreen1(coords, theme, isLight = false) {
  return frameNode(isLight ? 'Frame 4 - Public Academic Portal (Light Mode)' : 'Frame 1 - Public Academic Portal (Dark Mode)', {
    x: coords.x,
    y: coords.y,
    width: 1440,
    height: 2200,
    clip: true,
    fill: theme.bg,
    layout: 'vertical',
    alignItems: 'center',
    gap: 32,
    children: [
      buildNavbar(theme, isLight),
      buildStatusStrip(theme),
      buildHeroSection(theme),
      buildUpcomingExamBanner(theme),
      buildCoreSubjectGrid(theme),
      buildPanicPackSection(theme),
      buildPYQArchivesSection(theme),
      buildFooter(theme)
    ]
  });
}

// -------------------------------------------------------------
// Screen 2 & 5: Student Workspace & SGPA Dashboard (1440x1800)
// -------------------------------------------------------------
function buildScreen2(coords, theme, isLight = false) {
  const rows = [
    { code: 'PCC-CS601', name: 'Compiler Design', cr: '3.0', internal: '23/25', grade: 'O (10)', pts: '30.0' },
    { code: 'PCC-CS602', name: 'Computer Networks', cr: '3.0', internal: '21/25', grade: 'E (9)', pts: '27.0' },
    { code: 'PEC-CS601A', name: 'Distributed Systems', cr: '3.0', internal: '22/25', grade: 'E (9)', pts: '27.0' },
    { code: 'OEC-CS601B', name: 'Cloud Computing', cr: '3.0', internal: '24/25', grade: 'O (10)', pts: '30.0' },
    { code: 'PCC-CS691', name: 'Compiler Design Lab', cr: '2.0', internal: '25/25', grade: 'O (10)', pts: '20.0' },
    { code: 'PROJ-CS601', name: 'Mini Project / Research', cr: '2.0', internal: '24/25', grade: 'O (10)', pts: '20.0' }
  ];

  return frameNode(isLight ? 'Frame 5 - Student Workspace & SGPA Dashboard (Light Mode)' : 'Frame 2 - Student Workspace & SGPA Dashboard', {
    x: coords.x,
    y: coords.y,
    width: 1440,
    height: 1800,
    clip: true,
    fill: theme.bg,
    layout: 'vertical',
    alignItems: 'center',
    gap: 28,
    children: [
      buildNavbar(theme, isLight),
      // Top Command Bar
      frameNode('Top Command Bar', {
        width: 1440,
        height: 64,
        fill: theme.surface1,
        stroke: theme.border,
        strokeWidth: { bottom: 1 },
        padding: [0, 40],
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('Summary Left', {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 16,
            children: [
              textNode('B.Tech CSE • 6th Semester', 16, '700', theme.textPrimary),
              frameNode('CGPA Pill', {
                padding: [4, 10],
                cornerRadius: 12,
                fill: theme.surface2,
                stroke: theme.emerald,
                strokeWidth: 1,
                children: [
                  textNode('Projected CGPA: 8.92', 13, '700', theme.emerald, 'Geist Mono')
                ]
              })
            ]
          }),
          frameNode('Dropdowns Right', {
            layout: 'horizontal',
            gap: 10,
            alignItems: 'center',
            children: [
              frameNode('Stream Dropdown', {
                height: 40,
                padding: [0, 14],
                cornerRadius: 8,
                fill: theme.surface2,
                stroke: theme.border,
                strokeWidth: 1,
                alignItems: 'center',
                children: [textNode('Stream: CSE', 13, '500', theme.textPrimary)]
              }),
              frameNode('Semester Dropdown', {
                height: 40,
                padding: [0, 14],
                cornerRadius: 8,
                fill: theme.surface2,
                stroke: theme.border,
                strokeWidth: 1,
                alignItems: 'center',
                children: [textNode('Semester 6', 13, '500', theme.textPrimary)]
              }),
              frameNode('Export Ledger Button', {
                height: 44,
                padding: [0, 16],
                cornerRadius: 8,
                fill: theme.indigo,
                alignItems: 'center',
                children: [textNode('Export Ledger PDF', 13, '600', '#FFFFFF')]
              })
            ]
          })
        ]
      }),
      // Tabular SGPA Grade Ledger
      frameNode('SGPA Ledger Container', {
        width: 1120,
        cornerRadius: 12,
        fill: theme.surface1,
        stroke: theme.border,
        strokeWidth: 1,
        padding: [22, 24],
        layout: 'vertical',
        gap: 14,
        children: [
          frameNode('Ledger Header Group', {
            width: 'fill_container',
            layout: 'horizontal',
            justifyContent: 'space_between',
            alignItems: 'center',
            children: [
              frameNode('Ledger Titles', {
                layout: 'vertical',
                gap: 2,
                children: [
                  textNode('6th Semester SGPA Projection Ledger', 18, '700', theme.textPrimary),
                  textNode('Interactive grade simulator following MAKAUT 10-point credit grading scale', 13, '400', theme.textSecondary)
                ]
              }),
              frameNode('Scale Pill', {
                padding: [4, 10],
                cornerRadius: 6,
                fill: theme.surface2,
                children: [textNode('10-Point Academic Scale', 13, '500', theme.textSecondary, 'Geist Mono')]
              })
            ]
          }),
          // Table Headers Row
          frameNode('Table Headers', {
            width: 'fill_container',
            height: 40,
            fill: theme.surface2,
            cornerRadius: 6,
            padding: [0, 16],
            layout: 'horizontal',
            alignItems: 'center',
            justifyContent: 'space_between',
            children: [
              textNode('CODE', 13, '700', theme.textMuted, 'Geist Mono', { width: 140 }),
              textNode('SUBJECT', 13, '700', theme.textMuted, 'Inter', { width: 320 }),
              textNode('CREDITS', 13, '700', theme.textMuted, 'Geist Mono', { width: 100 }),
              textNode('INTERNAL (25m)', 13, '700', theme.textMuted, 'Geist Mono', { width: 140 }),
              textNode('LETTER GRADE', 13, '700', theme.textMuted, 'Geist Mono', { width: 140 }),
              textNode('QUALITY POINTS', 13, '700', theme.textMuted, 'Geist Mono', { width: 140 })
            ]
          }),
          // Table Body Rows
          frameNode('Table Rows', {
            width: 'fill_container',
            layout: 'vertical',
            gap: 4,
            children: rows.map(r =>
              frameNode(`Row ${r.code}`, {
                width: 'fill_container',
                height: 48,
                stroke: theme.border,
                strokeWidth: { bottom: 1 },
                padding: [0, 16],
                layout: 'horizontal',
                alignItems: 'center',
                justifyContent: 'space_between',
                children: [
                  textNode(r.code, 13, '600', theme.indigoAccent, 'Geist Mono', { width: 140 }),
                  textNode(r.name, 14, '500', theme.textPrimary, 'Inter', { width: 320 }),
                  textNode(r.cr, 13, '500', theme.textSecondary, 'Geist Mono', { width: 100 }),
                  textNode(r.internal, 13, '500', theme.textSecondary, 'Geist Mono', { width: 140 }),
                  frameNode('Grade Pill', {
                    width: 140,
                    layout: 'horizontal',
                    children: [
                      frameNode('Dropdown Pill', {
                        padding: [4, 10],
                        cornerRadius: 6,
                        fill: theme.surface2,
                        stroke: theme.border,
                        strokeWidth: 1,
                        children: [textNode(r.grade, 13, '700', theme.emerald, 'Geist Mono')]
                      })
                    ]
                  }),
                  textNode(r.pts, 13, '700', theme.textPrimary, 'Geist Mono', { width: 140 })
                ]
              })
            )
          }),
          // Table Footer Summary
          frameNode('Table Footer', {
            width: 'fill_container',
            height: 52,
            cornerRadius: 8,
            fill: theme.surface2,
            padding: [0, 20],
            layout: 'horizontal',
            justifyContent: 'space_between',
            alignItems: 'center',
            children: [
              textNode('Total: 16.0 Credits • 154.0 Quality Points', 14, '600', theme.textPrimary, 'Geist Mono'),
              frameNode('Calculated SGPA Pill', {
                layout: 'horizontal',
                alignItems: 'center',
                gap: 10,
                children: [
                  textNode('Calculated SGPA: 9.63', 15, '700', theme.emerald, 'Geist Mono'),
                  frameNode('Pass Badge', {
                    padding: [3, 8],
                    cornerRadius: 4,
                    fill: theme.surface1,
                    stroke: theme.emerald,
                    strokeWidth: 1,
                    children: [textNode('Exemplary Pass', 13, '700', theme.emerald, 'Inter')]
                  })
                ]
              })
            ]
          })
        ]
      }),
      // Regulatory Progress Gauges
      frameNode('Regulatory Gauges Row', {
        width: 1120,
        layout: 'horizontal',
        gap: 24,
        children: [
          // MAR Tracker
          frameNode('MAR Points Card', {
            width: 548,
            cornerRadius: 12,
            fill: theme.surface1,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [24, 24],
            layout: 'vertical',
            gap: 16,
            children: [
              textNode('Mandatory Additional Requirements (MAR)', 16, '700', theme.textPrimary),
              frameNode('MAR Center Gauge', {
                width: 'fill_container',
                layout: 'horizontal',
                alignItems: 'center',
                gap: 20,
                children: [
                  frameNode('Gauge Visual Box', {
                    width: 90,
                    height: 90,
                    cornerRadius: 45,
                    fill: theme.surface2,
                    stroke: theme.indigoAccent,
                    strokeWidth: 6,
                    alignItems: 'center',
                    justifyContent: 'center',
                    children: [textNode('75%', 16, '700', theme.indigoAccent, 'Geist Mono')]
                  }),
                  frameNode('Gauge Text Group', {
                    layout: 'vertical',
                    gap: 6,
                    children: [
                      textNode('75 / 100 Pts', 24, '700', theme.textPrimary, 'Geist Mono'),
                      textNode('25 Pts Remaining for B.Tech Degree', 13, '500', theme.emerald),
                      textNode('Status: On Track • Verified from College Portal', 13, '400', theme.textMuted)
                    ]
                  })
                ]
              })
            ]
          }),
          // MOOCs Tracker
          frameNode('MOOCs Credits Card', {
            width: 548,
            cornerRadius: 12,
            fill: theme.surface1,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [24, 24],
            layout: 'vertical',
            gap: 16,
            children: [
              textNode('MOOCs / NPTEL Credits Tracker (Honours)', 16, '700', theme.textPrimary),
              frameNode('MOOCs Center Gauge', {
                width: 'fill_container',
                layout: 'horizontal',
                alignItems: 'center',
                gap: 20,
                children: [
                  frameNode('Gauge Visual Box', {
                    width: 90,
                    height: 90,
                    cornerRadius: 45,
                    fill: theme.surface2,
                    stroke: theme.emerald,
                    strokeWidth: 6,
                    alignItems: 'center',
                    justifyContent: 'center',
                    children: [textNode('80%', 16, '700', theme.emerald, 'Geist Mono')]
                  }),
                  frameNode('Gauge Text Group', {
                    layout: 'vertical',
                    gap: 6,
                    children: [
                      textNode('16 / 20 Credits', 24, '700', theme.textPrimary, 'Geist Mono'),
                      textNode('4 Credits Remaining for Honours Degree', 13, '500', theme.indigoAccent),
                      textNode('Status: Elite Silver Certificate Indexed', 13, '400', theme.textMuted)
                    ]
                  })
                ]
              })
            ]
          })
        ]
      }),
      buildFooter(theme)
    ]
  });
}

// -------------------------------------------------------------
// Screen 3 & 6: Dedicated Authentication Page (1440x1200)
// -------------------------------------------------------------
function buildScreen3(coords, theme, isLight = false) {
  return frameNode(isLight ? 'Frame 6 - Dedicated Authentication Page (Light Mode)' : 'Frame 3 - Dedicated Authentication Page', {
    x: coords.x,
    y: coords.y,
    width: 1440,
    height: 1200,
    clip: true,
    fill: theme.bg,
    layout: 'vertical',
    alignItems: 'center',
    justifyContent: 'center',
    children: [
      frameNode('Auth Card', {
        width: 440,
        cornerRadius: 12,
        fill: theme.surface1,
        stroke: theme.border,
        strokeWidth: 1,
        padding: [36, 32],
        layout: 'vertical',
        gap: 18,
        children: [
          textNode('← Back to CampusSense', 13, '500', theme.textSecondary),
          frameNode('Auth Header', {
            layout: 'vertical',
            gap: 6,
            children: [
              textNode('Sign in to CampusSense', 24, '700', theme.textPrimary),
              textNode('Access verified academic blueprints, save past papers, and track your degree progress.', 14, '400', theme.textSecondary)
            ]
          }),
          // Google OAuth Button
          frameNode('Google OAuth Button', {
            width: 'fill_container',
            height: 48,
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            layout: 'horizontal',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 10,
            children: [
              frameNode('Google G Icon Box', {
                width: 20,
                height: 20,
                cornerRadius: 10,
                fill: '#EA4335',
                alignItems: 'center',
                justifyContent: 'center',
                children: [textNode('G', 13, '700', '#FFFFFF')]
              }),
              textNode('Continue with Google', 14, '600', theme.textPrimary)
            ]
          }),
          // Divider
          frameNode('Auth Divider', {
            width: 'fill_container',
            layout: 'horizontal',
            alignItems: 'center',
            gap: 12,
            children: [
              rectNode('Div L', 'fill_container', 1, theme.border),
              textNode('or continue with institutional email', 13, '400', theme.textMuted),
              rectNode('Div R', 'fill_container', 1, theme.border)
            ]
          }),
          // Email Input
          frameNode('Email Input Box', {
            width: 'fill_container',
            height: 44,
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [0, 14],
            layout: 'horizontal',
            alignItems: 'center',
            children: [
              textNode('student@college.edu.in', 14, '400', theme.textSecondary)
            ]
          }),
          // 6-digit OTP boxes
          frameNode('OTP Group', {
            layout: 'vertical',
            gap: 6,
            children: [
              textNode('One-Time Passcode (OTP)', 13, '500', theme.textSecondary),
              frameNode('OTP Boxes Row', {
                width: 'fill_container',
                layout: 'horizontal',
                justifyContent: 'space_between',
                children: ['4', '8', '2', '1', '9', '5'].map(d =>
                  frameNode(`OTP ${d}`, {
                    width: 52,
                    height: 48,
                    cornerRadius: 8,
                    fill: theme.surface2,
                    stroke: theme.border,
                    strokeWidth: 1,
                    alignItems: 'center',
                    justifyContent: 'center',
                    children: [textNode(d, 18, '700', theme.textPrimary, 'Geist Mono')]
                  })
                )
              })
            ]
          }),
          // Primary Action Button
          frameNode('Submit Button', {
            width: 'fill_container',
            height: 48,
            cornerRadius: 8,
            fill: theme.indigo,
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              textNode('Continue to Student Workspace →', 14, '600', '#FFFFFF')
            ]
          }),
          // Guest bypass
          frameNode('Guest Bypass', {
            width: 'fill_container',
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              textNode('Continue as Guest Student', 14, '500', theme.textSecondary)
            ]
          }),
          // Footer badge
          frameNode('Clerk Badge Group', {
            width: 'fill_container',
            layout: 'horizontal',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 6,
            children: [
              iconNode('shield-check', 14, 14, theme.textMuted),
              textNode('Secured by Clerk Authentication', 13, '500', theme.textMuted)
            ]
          })
        ]
      })
    ]
  });
}

// -------------------------------------------------------------
// Screen 7: Floating Resizable AI Chatbot Overlay (420x620)
// -------------------------------------------------------------
function buildScreen7(coords, theme) {
  return frameNode('Frame 7 - Floating Resizable AI Chatbot Overlay', {
    x: coords.x,
    y: coords.y,
    width: 420,
    height: 620,
    clip: true,
    cornerRadius: 12,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: 1,
    layout: 'vertical',
    children: [
      // Drag Header
      frameNode('Drag Header', {
        width: 'fill_container',
        height: 44,
        fill: theme.surface2,
        stroke: theme.border,
        strokeWidth: { bottom: 1 },
        padding: [0, 14],
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('Header Left', {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 10,
            children: [
              frameNode('Grip 6-dots', {
                layout: 'horizontal',
                gap: 3,
                children: [
                  frameNode('Col1', { layout: 'vertical', gap: 3, children: [ellipseNode('d1', 3, theme.textMuted), ellipseNode('d2', 3, theme.textMuted), ellipseNode('d3', 3, theme.textMuted)] }),
                  frameNode('Col2', { layout: 'vertical', gap: 3, children: [ellipseNode('d4', 3, theme.textMuted), ellipseNode('d5', 3, theme.textMuted), ellipseNode('d6', 3, theme.textMuted)] })
                ]
              }),
              textNode('CampusSense AI Assistant', 14, '600', theme.textPrimary),
              frameNode('Verified Badge', {
                padding: [2, 6],
                cornerRadius: 4,
                fill: theme.surface1,
                children: [textNode('CSE • Verified', 13, '600', theme.emerald, 'Geist Mono')]
              })
            ]
          }),
          frameNode('Header Controls', {
            layout: 'horizontal',
            gap: 8,
            alignItems: 'center',
            children: [
              iconNode('pin', 14, 14, theme.indigoAccent),
              iconNode('minus', 14, 14, theme.textSecondary),
              iconNode('x', 14, 14, theme.textSecondary)
            ]
          })
        ]
      }),
      // Q&A Feed
      frameNode('Chat Feed', {
        width: 'fill_container',
        layout: 'vertical',
        padding: [16, 16],
        gap: 12,
        children: [
          // User Message
          frameNode('User Row', {
            width: 'fill_container',
            layout: 'horizontal',
            justifyContent: 'end',
            children: [
              frameNode('User Bubble', {
                width: 320,
                cornerRadius: 10,
                fill: theme.indigo,
                padding: [10, 14],
                children: [
                  textNode('What are the key topics in Module 2 of PCC-CS501 Automata?', 14, '400', '#FFFFFF')
                ]
              })
            ]
          }),
          // Assistant Message
          frameNode('Assistant Row', {
            width: 'fill_container',
            layout: 'horizontal',
            children: [
              frameNode('Assistant Bubble', {
                width: 388,
                cornerRadius: 10,
                fill: theme.surface2,
                stroke: theme.border,
                strokeWidth: 1,
                padding: [12, 14],
                layout: 'vertical',
                gap: 8,
                children: [
                  textNode('Module 2 covers Context-Free Grammars (CFG), Chomsky Normal Form (CNF), and Pushdown Automata (PDA) with equivalence proofs.', 14, '400', theme.textPrimary),
                  frameNode('Citation Pill', {
                    padding: [3, 8],
                    cornerRadius: 4,
                    fill: theme.surface1,
                    stroke: theme.emerald,
                    strokeWidth: 1,
                    layout: 'horizontal',
                    alignItems: 'center',
                    gap: 6,
                    children: [
                      iconNode('check', 12, 12, theme.emerald),
                      textNode('Verified: PCC-CS501 • Mod 2 • p.4', 13, '600', theme.emerald, 'Geist Mono')
                    ]
                  })
                ]
              })
            ]
          }),
          // Negative Grounding Warning
          frameNode('Negative Grounding Callout', {
            width: 'fill_container',
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.rose,
            strokeWidth: 1,
            padding: [10, 14],
            layout: 'horizontal',
            alignItems: 'center',
            gap: 10,
            children: [
              iconNode('triangle-alert', 16, 16, theme.rose),
              textNode('This information is not verified by official MAKAUT curriculum data.', 13, '500', theme.rose)
            ]
          })
        ]
      }),
      // Input Bar
      frameNode('Bottom Input Bar', {
        width: 'fill_container',
        height: 56,
        fill: theme.surface2,
        stroke: theme.border,
        strokeWidth: { top: 1 },
        padding: [0, 14],
        layout: 'horizontal',
        alignItems: 'center',
        justifyContent: 'space_between',
        children: [
          frameNode('Input Left', {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 10,
            children: [
              iconNode('paperclip', 16, 16, theme.textMuted),
              textNode('Ask a question about MAKAUT syllabus...', 14, '400', theme.textMuted)
            ]
          }),
          frameNode('Send Button Box', {
            width: 36,
            height: 36,
            cornerRadius: 8,
            fill: theme.indigo,
            alignItems: 'center',
            justifyContent: 'center',
            children: [
              iconNode('send', 16, 16, '#FFFFFF')
            ]
          })
        ]
      })
    ]
  });
}

// -------------------------------------------------------------
// Screen 8: 10-Year PYQ PDF In-Browser Previewer Modal (900x650)
// -------------------------------------------------------------
function buildScreen8(coords, theme) {
  return frameNode('Frame 8 - 10-Year PYQ PDF In-Browser Previewer Modal', {
    x: coords.x,
    y: coords.y,
    width: 900,
    height: 650,
    clip: true,
    cornerRadius: 12,
    fill: theme.surface1,
    stroke: theme.border,
    strokeWidth: 1,
    layout: 'vertical',
    children: [
      // Top Toolbar
      frameNode('Modal Toolbar', {
        width: 'fill_container',
        height: 52,
        fill: theme.surface2,
        stroke: theme.border,
        strokeWidth: { bottom: 1 },
        padding: [0, 20],
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('Doc Info', {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 10,
            children: [
              iconNode('file-text', 18, 18, theme.indigoAccent),
              textNode('PCC-CS501_Automata_Theory_2023_Regular.pdf', 14, '600', theme.textPrimary)
            ]
          }),
          frameNode('Zoom & Page Controls', {
            layout: 'horizontal',
            gap: 12,
            alignItems: 'center',
            children: [
              textNode('-', 14, '700', theme.textSecondary, 'Geist Mono'),
              textNode('100%', 13, '600', theme.textPrimary, 'Geist Mono'),
              textNode('+', 14, '700', theme.textSecondary, 'Geist Mono'),
              rectNode('Sep', 1, 16, theme.border),
              textNode('Page 1 of 4', 13, '500', theme.textSecondary, 'Geist Mono')
            ]
          }),
          frameNode('Toolbar Actions', {
            layout: 'horizontal',
            gap: 10,
            alignItems: 'center',
            children: [
              frameNode('Download Action', {
                height: 36,
                padding: [0, 14],
                cornerRadius: 6,
                fill: theme.indigo,
                layout: 'horizontal',
                alignItems: 'center',
                gap: 6,
                children: [
                  iconNode('download', 14, 14, '#FFFFFF'),
                  textNode('Download PDF', 13, '600', '#FFFFFF')
                ]
              }),
              iconNode('x', 18, 18, theme.textSecondary)
            ]
          })
        ]
      }),
      // Preview Pane (Realistic High-Contrast Exam Paper)
      frameNode('Preview Canvas', {
        width: 'fill_container',
        padding: [20, 40],
        fill: theme.bg,
        alignItems: 'center',
        children: [
          frameNode('Simulated Paper', {
            width: 820,
            height: 530,
            cornerRadius: 4,
            fill: '#FFFFFF',
            stroke: '#E2E8F0',
            strokeWidth: 1,
            padding: [24, 32],
            layout: 'vertical',
            gap: 12,
            children: [
              textNode('MAULANA ABUL KALAM AZAD UNIVERSITY OF TECHNOLOGY, WEST BENGAL', 15, '700', '#0F172A', 'Inter', { textAlign: 'center', width: 'fill_container' }),
              textNode('B.Tech 5th Semester Examination — 2023 | Course Code: PCC-CS501', 13, '600', '#475569', 'Inter', { textAlign: 'center', width: 'fill_container' }),
              textNode('Time Allotted: 3 Hours | Full Marks: 70', 13, '500', '#334155', 'Geist Mono', { textAlign: 'center', width: 'fill_container' }),
              rectNode('Paper Divider', 'fill_container', 1, '#CBD5E1'),
              // Sections
              frameNode('Paper Group A', {
                layout: 'vertical',
                gap: 4,
                children: [
                  textNode('GROUP – A (Multiple Choice Type Questions) — [Answer all questions: 10 × 1 = 10]', 13, '700', '#0F172A'),
                  textNode('1. (i) The language accepted by a finite state automaton with ε-transitions is: (a) Regular (b) Context-Free (c) Recursive (d) NP-Complete.', 13, '400', '#334155')
                ]
              }),
              frameNode('Paper Group B', {
                layout: 'vertical',
                gap: 4,
                children: [
                  textNode('GROUP – B (Short Answer Type Questions) — [Answer any three: 3 × 5 = 15]', 13, '700', '#0F172A'),
                  textNode('2. State the Pumping Lemma for Regular Languages. Prove that L = {0^n 1^n | n ≥ 1} is not regular.', 13, '400', '#334155')
                ]
              }),
              frameNode('Paper Group C', {
                layout: 'vertical',
                gap: 4,
                children: [
                  textNode('GROUP – C (Long Answer Type Questions) — [Answer any three: 3 × 15 = 45]', 13, '700', '#0F172A'),
                  textNode('3. (a) Design a PDA (Pushdown Automaton) for recognizing palindrome strings over {a, b} with a center marker. (b) Explain the Chomsky Hierarchy of formal grammars.', 13, '400', '#334155')
                ]
              })
            ]
          })
        ]
      })
    ]
  });
}

// -------------------------------------------------------------
// Screen 9: Editorial Privacy Policy & Disclaimer (1440x1400)
// -------------------------------------------------------------
function buildScreen9(coords, theme) {
  return frameNode('Frame 9 - Editorial Privacy Policy & Disclaimer', {
    x: coords.x,
    y: coords.y,
    width: 1440,
    height: 1400,
    clip: true,
    fill: theme.bg,
    layout: 'vertical',
    alignItems: 'center',
    gap: 32,
    children: [
      buildNavbar(theme, false),
      frameNode('Privacy Container', {
        width: 760,
        layout: 'vertical',
        gap: 24,
        children: [
          frameNode('Privacy Header', {
            layout: 'vertical',
            gap: 6,
            children: [
              textNode('Privacy Policy & Academic Disclaimer', 36, '700', theme.textPrimary),
              textNode('Updated October 2026 • MAKAUT B.Tech Academic Initiative', 13, '500', theme.textMuted, 'Geist Mono')
            ]
          }),
          rectNode('Priv Div', 'fill_container', 1, theme.border),
          frameNode('Section 1', {
            layout: 'vertical',
            gap: 8,
            children: [
              textNode('1. Institutional Non-Affiliation', 20, '700', theme.textPrimary),
              textNode('MAKAUT CampusSense is an independent academic resource created by engineering students for educational purposes. This platform is not officially affiliated with, endorsed by, or sponsored by Maulana Abul Kalam Azad University of Technology (MAKAUT, West Bengal). All university names and course codes are used strictly for reference and organizational clarity.', 14, '400', theme.textSecondary, 'Inter', { lineHeight: 22 })
            ]
          }),
          frameNode('Section 2', {
            layout: 'vertical',
            gap: 8,
            children: [
              textNode('2. Academic Fair Use & Document Indexing', 20, '700', theme.textPrimary),
              textNode('All course syllabi, past examination papers (PYQs), and curriculum structures indexed on this platform are collected from publicly circulated university documents under academic fair-use principles to support undergraduate study and exam revision.', 14, '400', theme.textSecondary, 'Inter', { lineHeight: 22 })
            ]
          }),
          frameNode('Section 3', {
            layout: 'vertical',
            gap: 8,
            children: [
              textNode('3. Student Data Protection & Privacy', 20, '700', theme.textPrimary),
              textNode('CampusSense prioritizes student privacy. All SGPA calculations, course bookmarks, and revision notes remain stored locally in your browser by default. When signing in via Clerk, only essential authentication identifiers are processed securely. No personal data is sold or shared.', 14, '400', theme.textSecondary, 'Inter', { lineHeight: 22 })
            ]
          })
        ]
      }),
      buildFooter(theme)
    ]
  });
}

// -------------------------------------------------------------
// Screen 10: Mobile Responsive Landing Page (375x1600)
// -------------------------------------------------------------
function buildScreen10(coords, theme) {
  return frameNode('Frame 10 - Mobile Responsive Landing Page', {
    x: coords.x,
    y: coords.y,
    width: 375,
    height: 1600,
    clip: true,
    fill: theme.bg,
    layout: 'vertical',
    gap: 16,
    children: [
      // Mobile Header
      frameNode('Mobile Header', {
        width: 'fill_container',
        height: 56,
        fill: theme.surface1,
        stroke: theme.border,
        strokeWidth: { bottom: 1 },
        padding: [0, 16],
        layout: 'horizontal',
        justifyContent: 'space_between',
        alignItems: 'center',
        children: [
          frameNode('Mobile Brand', {
            layout: 'horizontal',
            alignItems: 'center',
            gap: 8,
            children: [
              frameNode('Brand Monogram', {
                width: 28,
                height: 28,
                cornerRadius: 6,
                fill: theme.surface2,
                stroke: theme.border,
                strokeWidth: 1,
                alignItems: 'center',
                justifyContent: 'center',
                children: [textNode('CS', 13, '700', theme.indigoAccent)]
              }),
              textNode('CampusSense', 16, '700', theme.textPrimary)
            ]
          }),
          iconNode('menu', 20, 20, theme.textPrimary)
        ]
      }),
      // Mobile Hero
      frameNode('Mobile Hero', {
        padding: [16, 16],
        layout: 'vertical',
        gap: 12,
        children: [
          frameNode('Mobile Pill', {
            padding: [4, 10],
            cornerRadius: 12,
            fill: theme.surface2,
            children: [textNode('MAKAUT Academic Portal', 13, '600', theme.indigoAccent, 'Geist Mono')]
          }),
          textNode('Syllabi, Past Papers & Exam Revision', 28, '700', theme.textPrimary, 'Inter', { letterSpacing: -0.5 }),
          textNode('Curated MAKAUT B.Tech syllabus breakdowns, past papers, and revision tools.', 14, '400', theme.textSecondary),
          // Search input
          frameNode('Mobile Search', {
            width: 343,
            height: 48,
            cornerRadius: 8,
            fill: theme.surface2,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [0, 12],
            layout: 'horizontal',
            alignItems: 'center',
            gap: 8,
            children: [
              iconNode('search', 16, 16, theme.textMuted),
              textNode('Search subject or code...', 14, '400', theme.textMuted)
            ]
          }),
          // Stream Pills
          frameNode('Mobile Stream Pills', {
            layout: 'horizontal',
            gap: 6,
            children: ['Common', 'CSE', 'IT', 'ECE'].map((st, i) =>
              frameNode(`Pill ${st}`, {
                height: 32,
                padding: [0, 10],
                cornerRadius: 16,
                fill: i === 1 ? theme.indigo : theme.surface2,
                children: [textNode(st, 13, '600', i === 1 ? '#FFFFFF' : theme.textSecondary)]
              })
            )
          })
        ]
      }),
      // Mobile 5th Sem Cards
      frameNode('Mobile Cards Container', {
        padding: [0, 16],
        layout: 'vertical',
        gap: 14,
        children: [
          frameNode('Mob Card 1', {
            width: 343,
            cornerRadius: 10,
            fill: theme.surface1,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [16, 16],
            layout: 'vertical',
            gap: 10,
            children: [
              frameNode('Mob Top', {
                width: 'fill_container',
                layout: 'horizontal',
                justifyContent: 'space_between',
                children: [
                  textNode('PCC-CS501', 13, '700', '#60A5FA', 'Geist Mono'),
                  textNode('3.0 Credits', 13, '500', theme.textSecondary, 'Geist Mono')
                ]
              }),
              textNode('Formal Language & Automata Theory', 16, '600', theme.textPrimary),
              textNode('• Finite Automata & Regular Languages\n• CFG & Pushdown Automata\n• Turing Machines & Decidability', 13, '400', theme.textSecondary, 'Inter', { lineHeight: 18 }),
              textNode('Open Syllabus Blueprint →', 13, '600', '#60A5FA')
            ]
          }),
          frameNode('Mob Card 2', {
            width: 343,
            cornerRadius: 10,
            fill: theme.surface1,
            stroke: theme.border,
            strokeWidth: 1,
            padding: [16, 16],
            layout: 'vertical',
            gap: 10,
            children: [
              frameNode('Mob Top 2', {
                width: 'fill_container',
                layout: 'horizontal',
                justifyContent: 'space_between',
                children: [
                  textNode('PCC-CS502', 13, '700', '#A78BFA', 'Geist Mono'),
                  textNode('3.0 Credits', 13, '500', theme.textSecondary, 'Geist Mono')
                ]
              }),
              textNode('Database Management Systems', 16, '600', theme.textPrimary),
              textNode('• Relational Model & SQL\n• Normalization 1NF to BCNF\n• Transactions & ACID Properties', 13, '400', theme.textSecondary, 'Inter', { lineHeight: 18 }),
              textNode('Open Syllabus Blueprint →', 13, '600', '#A78BFA')
            ]
          })
        ]
      }),
      // Mobile Floating Action Pill
      frameNode('Mobile Bottom Action', {
        width: 375,
        padding: [16, 16],
        alignItems: 'center',
        justifyContent: 'center',
        children: [
          frameNode('Ask AI Pill Button', {
            height: 48,
            padding: [0, 20],
            cornerRadius: 24,
            fill: theme.indigo,
            layout: 'horizontal',
            alignItems: 'center',
            gap: 8,
            children: [
              iconNode('message-square', 16, 16, '#FFFFFF'),
              textNode('Ask AI Assistant (Verified)', 14, '600', '#FFFFFF')
            ]
          })
        ]
      })
    ]
  });
}

// -------------------------------------------------------------
// Assembly & Main Execution
// -------------------------------------------------------------
function generateMasterDesign() {
  console.log('Generating 10-screen Master Design for design.pen...');

  const existing = JSON.parse(fs.readFileSync('design.pen', 'utf8'));

  const screen1 = buildScreen1({ x: 0, y: 0 }, DARK, false);
  const screen2 = buildScreen2({ x: 1520, y: 0 }, DARK, false);
  const screen3 = buildScreen3({ x: 3040, y: 0 }, DARK, false);

  const screen4 = buildScreen1({ x: 0, y: 2300 }, LIGHT, true);
  const screen5 = buildScreen2({ x: 1520, y: 2300 }, LIGHT, true);
  const screen6 = buildScreen3({ x: 3040, y: 2300 }, LIGHT, true);

  const screen7 = buildScreen7({ x: 0, y: 4600 }, DARK);
  const screen8 = buildScreen8({ x: 500, y: 4600 }, DARK);
  const screen9 = buildScreen9({ x: 1520, y: 4600 }, DARK);
  const screen10 = buildScreen10({ x: 3040, y: 4600 }, DARK);

  const fullData = {
    version: '2.21',
    fileToken: existing.fileToken || 'c8ec5390-53ab-4f79-adb7-01d60798a7af',
    variables: existing.variables || {},
    children: [
      screen1,
      screen2,
      screen3,
      screen4,
      screen5,
      screen6,
      screen7,
      screen8,
      screen9,
      screen10
    ]
  };

  fs.writeFileSync('design.pen', JSON.stringify(fullData, null, 2), 'utf8');
  console.log('Successfully wrote design.pen with all 10 non-overlapping screens.');
}

generateMasterDesign();
