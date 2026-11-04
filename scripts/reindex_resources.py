#!/usr/bin/env python3
"""
scripts/reindex_resources.py
Automated catalog generator and resource indexer for MAKAUT CampusSense.

Scans the filesystem directories:
  - docs/1st year/ (syllabus, pyqs)
  - docs/3rd to 8th sem/ (csbs, cscs, csds, cse, cse-aiml, it)
Parses file metadata (stream, semester, subjectCode, subjectName, year, sha256 checksum, size).
Generates:
  - docs/resources.json
  - src/main/resources/static/resources.json
"""

import os
import re
import json
import hashlib
from datetime import datetime, timezone

WORKSPACE_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS_DIR = os.path.join(WORKSPACE_ROOT, 'docs')
STATIC_DIR = os.path.join(WORKSPACE_ROOT, 'src', 'main', 'resources', 'static')

FIRST_YEAR_CODES = {
    'bee': ('ES-EE101', 'Basic Electrical Engineering'),
    'chemistry': ('BS-CH101', 'Chemistry 1'),
    'english': ('HM-HU201', 'English'),
    'mathematics_1a': ('BS-M101', 'Mathematics 1A'),
    'mathematics_1b': ('BS-M102', 'Mathematics 1B'),
    'mathematics_2a': ('BS-M201', 'Mathematics 2A'),
    'mathematics_2b': ('BS-M202', 'Mathematics 2B'),
    'physics': ('BS-PH101', 'Physics 1'),
    'pps': ('ES-CS201', 'Programming for Problem Solving')
}

CSE_FOLDER_CODES = {
    'ANALOG & DIGITAL': ('ESC 301', 'Analog & Digital Electronics'),
    'DSA': ('PCC-CS301', 'Data Structure & Algorithms'),
    'COMPUTER ORGANISATION': ('PCC-CS302', 'Computer Organisation'),
    'MATHS 3': ('BSC 301', 'Mathematics-III (Differential Calculus)'),
    'ECONOMICS': ('HSMC 301', 'Economics for Engineers (Humanities-II)'),
    'DISCRETE MATHS': ('PCC-CS401', 'Discrete Mathematics'),
    'COMPUTER ARCHITECTURE': ('PCC-CS402', 'Computer Architecture'),
    'TOC or FLAT': ('PCC-CS403', 'Formal Language & Automata Theory'),
    'DAA': ('PCC-CS404', 'Design & Analysis of Algorithms'),
    'SOFTWARE ENGG': ('ESC 501', 'Software Engineering'),
    'COMPILER DESIGN': ('PCC-CS501', 'Compiler Design'),
    'OS': ('PCC-CS502', 'Operating Systems'),
    'OOPS': ('PCC-CS503', 'Object Oriented Programming'),
    'DATABASE': ('PCC-CS601', 'Database Management Systems'),
    'COMPUTER NETWORKS': ('PCC-CS602', 'Computer Networks'),
    'ADVANCED ALGORITHMS': ('PEC-CS601A', 'Advanced Algorithms'),
    'IMAGE PROCESSING': ('PEC-CS701A', 'Digital Image Processing'),
    'CLOUD COMPUTING': ('PEC-CS701B', 'Cloud Computing'),
    'NEURAL NETWORKS': ('PEC-CS702B', 'Neural Networks and Deep Learning'),
    'CYBER LAW': ('OEC-CS701A', 'Cyber Law and Ethics'),
    'MOBILE COMPUTING': ('PEC-CS701C', 'Mobile Computing')
}

DEPARTMENT_NAMES = {
    'cse': 'Computer Science & Engineering',
    'it': 'Information Technology',
    'cse-aiml': 'Computer Science and Artificial Intelligence and Machine Learning',
    'cscs': 'Computer Science and Cyber Security',
    'csds': 'Computer Science and Data Science',
    'csbs': 'Computer Science and Business Systems'
}

def compute_sha256(filepath):
    h = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()

def clean_title_from_filename(filename):
    name = os.path.splitext(filename)[0]
    words = re.split(r'[-_]+', name)
    # capitalize words cleanly
    title = ' '.join(w.capitalize() if not w.isupper() else w for w in words)
    return title

def parse_subject_code_and_name(folder_name, filename, dept=None):
    # Check folder name first (e.g. PCC-CS301_Data_Structures_and_Algorithms)
    folder_m = re.match(r'^([A-Z0-9\(\)]+(?:-[A-Z0-9\(\)]+)*)_(.+)$', folder_name)
    if folder_m:
        code = folder_m.group(1).replace('_', ' ')
        name = folder_m.group(2).replace('_', ' ')
        return code, name

    # Check CSE folder dictionary
    if folder_name in CSE_FOLDER_CODES:
        return CSE_FOLDER_CODES[folder_name]

    # Check 1st year codes
    lower_folder = folder_name.lower()
    if lower_folder in FIRST_YEAR_CODES:
        return FIRST_YEAR_CODES[lower_folder]

    # Fallback: extract code from filename if present (e.g. pcc-cs401 or bsm101)
    code_m = re.search(r'(?:^|-)([a-z]{2,4}-?[a-z]{0,3}-?\d{3}[a-z]?)(?:-|$)', filename, re.I)
    if code_m:
        code = code_m.group(1).upper()
        name = folder_name.replace('_', ' ')
        return code, name

    return folder_name.replace('_', ' '), folder_name.replace('_', ' ')

def scan_catalog():
    resources = []
    syl_idx = 1
    pyq_idx = 1

    # 1. Scan 1st Year Syllabus
    first_year_syl_dir = os.path.join(DOCS_DIR, '1st year', 'syllabus')
    if os.path.isdir(first_year_syl_dir):
        for fname in sorted(os.listdir(first_year_syl_dir)):
            if fname.endswith('.pdf'):
                fpath = os.path.join(first_year_syl_dir, fname)
                sub_name = os.path.splitext(fname)[0]
                rel_path = f"1st year/syllabus/{fname}"
                resources.append({
                    "id": f"syl_{syl_idx}",
                    "stream": "FIRST_YEAR",
                    "semester": 1,
                    "subjectCode": sub_name,
                    "subjectName": f"{sub_name} Official Syllabus",
                    "resourceType": "SYLLABUS",
                    "year": 2024,
                    "title": f"MAKAUT Official Syllabus - {sub_name}",
                    "fileUrl": rel_path,
                    "canonicalPath": rel_path,
                    "fileSize": os.path.getsize(fpath),
                    "sha256": compute_sha256(fpath)
                })
                syl_idx += 1

    # 2. Scan 3rd to 8th sem Departmental Syllabi
    dept_root = os.path.join(DOCS_DIR, '3rd to 8th sem')
    if os.path.isdir(dept_root):
        for dept in sorted(os.listdir(dept_root)):
            syl_dir = os.path.join(dept_root, dept, 'syllabus')
            if os.path.isdir(syl_dir):
                for fname in sorted(os.listdir(syl_dir)):
                    if fname.endswith('.pdf'):
                        fpath = os.path.join(syl_dir, fname)
                        dept_key = dept.upper()
                        rel_path = f"3rd to 8th sem/{dept}/syllabus/{fname}"
                        resources.append({
                            "id": f"syl_{syl_idx}",
                            "stream": dept_key,
                            "semester": 3,
                            "subjectCode": f"{dept_key}_Syllabus",
                            "subjectName": f"{dept_key} Official Syllabus (Semesters 3 to 8)",
                            "resourceType": "SYLLABUS",
                            "year": 2024,
                            "title": f"MAKAUT Official Syllabus - {DEPARTMENT_NAMES.get(dept, dept_key)}",
                            "fileUrl": rel_path,
                            "canonicalPath": rel_path,
                            "fileSize": os.path.getsize(fpath),
                            "sha256": compute_sha256(fpath)
                        })
                        syl_idx += 1

    # 3. Scan 1st Year PYQs
    first_year_pyq_dir = os.path.join(DOCS_DIR, '1st year', 'pyqs')
    if os.path.isdir(first_year_pyq_dir):
        for sub_dir in sorted(os.listdir(first_year_pyq_dir)):
            sub_path = os.path.join(first_year_pyq_dir, sub_dir)
            if os.path.isdir(sub_path):
                sub_code, sub_name = FIRST_YEAR_CODES.get(sub_dir.lower(), (sub_dir.upper(), sub_dir))
                for fname in sorted(os.listdir(sub_path)):
                    if fname.endswith('.pdf'):
                        fpath = os.path.join(sub_path, fname)
                        year_m = re.search(r'-(20\d\d)\.pdf$', fname)
                        year = int(year_m.group(1)) if year_m else 2024
                        sem_m = re.search(r'-([12])-sem-', fname)
                        semester = int(sem_m.group(1)) if sem_m else 1

                        rel_path = f"1st year/pyqs/{sub_dir}/{fname}"
                        resources.append({
                            "id": f"pyq_{pyq_idx}",
                            "stream": "FIRST_YEAR",
                            "semester": semester,
                            "subjectCode": sub_code,
                            "subjectName": sub_name,
                            "resourceType": "PYQ",
                            "year": year,
                            "title": clean_title_from_filename(fname),
                            "fileUrl": rel_path,
                            "canonicalPath": rel_path,
                            "fileSize": os.path.getsize(fpath),
                            "sha256": compute_sha256(fpath)
                        })
                        pyq_idx += 1

    # 4. Scan 3rd to 8th sem PYQs
    if os.path.isdir(dept_root):
        for dept in sorted(os.listdir(dept_root)):
            dept_pyqs_dir = os.path.join(dept_root, dept, 'pyqs')
            if not os.path.isdir(dept_pyqs_dir):
                continue
            dept_key = dept.upper()
            for sem_folder in sorted(os.listdir(dept_pyqs_dir)):
                sem_dir = os.path.join(dept_pyqs_dir, sem_folder)
                if not os.path.isdir(sem_dir):
                    continue
                sem_num_m = re.search(r'([3-8])', sem_folder)
                sem_num = int(sem_num_m.group(1)) if sem_num_m else 3

                for sub_folder in sorted(os.listdir(sem_dir)):
                    sub_path = os.path.join(sem_dir, sub_folder)
                    if not os.path.isdir(sub_path):
                        continue
                    for fname in sorted(os.listdir(sub_path)):
                        if fname.endswith('.pdf'):
                            fpath = os.path.join(sub_path, fname)
                            code, name = parse_subject_code_and_name(sub_folder, fname, dept=dept)
                            year_m = re.search(r'-(20\d\d)\.pdf$', fname)
                            year = int(year_m.group(1)) if year_m else 2024
                            sem_m = re.search(r'-([3-8])-sem-', fname)
                            final_sem = int(sem_m.group(1)) if sem_m else sem_num

                            rel_path = f"3rd to 8th sem/{dept}/pyqs/{sem_folder}/{sub_folder}/{fname}"
                            resources.append({
                                "id": f"pyq_{pyq_idx}",
                                "stream": dept_key,
                                "semester": final_sem,
                                "subjectCode": code,
                                "subjectName": name,
                                "resourceType": "PYQ",
                                "year": year,
                                "title": clean_title_from_filename(fname),
                                "fileUrl": rel_path,
                                "canonicalPath": rel_path,
                                "fileSize": os.path.getsize(fpath),
                                "sha256": compute_sha256(fpath)
                            })
                            pyq_idx += 1

    syl_count = syl_idx - 1
    pyq_count = pyq_idx - 1
    total_count = len(resources)

    catalog = {
        "generatedAt": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
        "version": "1.1.0",
        "totalResources": total_count,
        "summary": {
            "syllabusCount": syl_count,
            "pyqCount": pyq_count
        },
        "resources": resources
    }

    # Write to docs/resources.json
    docs_json = os.path.join(DOCS_DIR, 'resources.json')
    with open(docs_json, 'w', encoding='utf-8') as f:
        json.dump(catalog, f, indent=2)
    print(f"Updated {docs_json} (Total: {total_count}, Syllabus: {syl_count}, PYQs: {pyq_count})")

    # Write to src/main/resources/static/resources.json
    static_json = os.path.join(STATIC_DIR, 'resources.json')
    if os.path.isdir(STATIC_DIR):
        with open(static_json, 'w', encoding='utf-8') as f:
            json.dump(catalog, f, indent=2)
        print(f"Updated {static_json}")

    return catalog

if __name__ == '__main__':
    catalog = scan_catalog()
    print("Catalog scan completed successfully.")
