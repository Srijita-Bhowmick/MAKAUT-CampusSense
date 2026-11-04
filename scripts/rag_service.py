#!/usr/bin/env python3
"""
scripts/rag_service.py
Conversational RAG Inference Engine & Strict Negative Grounding Guardrail Service

Implements:
  1. Hybrid Search (1536-d semantic vector similarity + exact course code/module/keyword boosting)
  2. Strict Negative Grounding Guardrail:
     - If query is out-of-domain, unverified, or below strict similarity threshold, strictly returns:
       "This information is not verified by official MAKAUT curriculum data."
  3. Resilient Local Deterministic Synthesizer (when external LLM API key is absent)
  4. Dynamic LLM Integration (OpenAI / Gemini) via .env.local or environment variables
  5. Dual Interface: Direct CLI query runner and standalone HTTP microservice with CORS
"""

import os
import re
import sys
import json
import time
import math
import hashlib
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import parse_qs, urlparse

WORKSPACE_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VECTOR_STORE_PATH = os.path.join(WORKSPACE_ROOT, '.makaut_rag', 'vector_store.json')
KNOWLEDGE_INDEX_PATH = os.path.join(WORKSPACE_ROOT, 'docs', 'makaut_knowledge_index.json')

MANDATORY_UNVERIFIED_MESSAGE = "This information is not verified by official MAKAUT curriculum data."
SIMILARITY_THRESHOLD = 0.45
EMBEDDING_DIMENSIONS = 1536

# Load .env.local if present
ENV_LOCAL_PATH = os.path.join(WORKSPACE_ROOT, '.env.local')
if os.path.isfile(ENV_LOCAL_PATH):
    try:
        from dotenv import load_dotenv
        load_dotenv(ENV_LOCAL_PATH)
    except Exception:
        pass

# Check for LLM keys dynamically
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "").strip()
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", os.getenv("GOOGLE_API_KEY", "")).strip()

OUT_OF_DOMAIN_PATTERNS = [
    r'\b(cook|cooking|recipe|recipes|cake|pizza|pasta|baking|chef|culinary)\b',
    r'\b(weather|temperature|forecast|climate in)\b',
    r'\b(football|soccer|fifa|world cup|cricket score|ipl|basketball|nba)\b',
    r'\b(movie|cinema|actor|actress|hollywood|bollywood|netflix|song|celebrity)\b',
    r'\b(mit|harvard|stanford|oxford|cambridge|caltech)\b(?!\s*makaut)',
    r'\b(aeronautics|astrology|horoscope|zodiac|tarot)\b',
    r'\b(crypto|bitcoin|ethereum|doge|forex trading|stock tips)\b',
    r'\b(capital of|population of|president of|prime minister of)\b'
]

DOMAIN_KEYWORDS = {
    'makaut', 'syllabus', 'module', 'credit', 'credits', 'semester', 'curriculum',
    'physics', 'chemistry', 'mathematics', 'calculus', 'matrix', 'matrices',
    'bee', 'electrical', 'circuits', 'thevenin', 'norton', 'resonance',
    'pps', 'programming', 'pointers', 'arrays', 'malloc', 'calloc', 'recursion',
    'english', 'communication', 'biology', 'biomolecules', 'evs', 'environmental',
    'dsa', 'structures', 'algorithms', 'co', 'organisation', 'ca', 'architecture',
    'discrete', 'flata', 'toc', 'automata', 'daa', 'compiler', 'operating',
    'deadlock', 'semaphore', 'banker', 'oops', 'database', 'dbms', 'sql',
    'networks', 'sgpa', 'ygpa', 'dgpa', 'ca1', 'ca2', 'ca3', 'ca4', 'pca1', 'pca2',
    'moocs', 'swayam', 'nptel', 'honours', 'iem', 'heritage', 'tmsl', 'hit', 'nsec',
    'placement', 'package', 'ctc', 'panic', 'pyq', 'exam', 'derivation'
}

def generate_query_vector(text: str) -> list[float]:
    vector = [0.0] * EMBEDDING_DIMENSIONS
    clean = re.sub(r'[^a-z0-9\s]', ' ', text.lower())
    tokens = [t for t in clean.split() if len(t) >= 2]

    text_hash = hashlib.sha256(text.encode('utf-8')).digest()
    text_seed = int.from_bytes(text_hash[:8], byteorder='big', signed=False)

    s = text_seed
    for i in range(EMBEDDING_DIMENSIONS):
        s = (1103515245 * s + 12345) & 0x7FFFFFFF
        norm_val = ((s / 0x7FFFFFFF) - 0.5) * 0.05
        vector[i] += norm_val

    for token in tokens:
        token_hash = hashlib.sha256(token.encode('utf-8')).digest()
        weight = 3.5 if any(k in token for k in DOMAIN_KEYWORDS) else 1.0

        for h in range(4):
            val = (token_hash[h*4] << 24) | (token_hash[h*4+1] << 16) | (token_hash[h*4+2] << 8) | token_hash[h*4+3]
            dim_index = abs(val) % EMBEDDING_DIMENSIONS
            vector[dim_index] += weight

    sum_sq = sum(v * v for v in vector)
    norm = math.sqrt(sum_sq)
    if norm < 1e-9:
        norm = 1.0
    return [v / norm for v in vector]


def cosine_similarity(v1: list[float], v2: list[float]) -> float:
    return sum(a * b for a, b in zip(v1, v2))


class RagEngine:
    def __init__(self):
        self.vector_store = []
        self.knowledge_chunks = {}
        self.load_knowledge_base()

    def load_knowledge_base(self):
        if not os.path.isfile(VECTOR_STORE_PATH) or not os.path.isfile(KNOWLEDGE_INDEX_PATH):
            print("Notice: Knowledge index not found, building now...")
            import subprocess
            subprocess.run([sys.executable, os.path.join(WORKSPACE_ROOT, 'scripts', 'ingest_makaut_knowledge.py')], check=True)

        with open(VECTOR_STORE_PATH, 'r', encoding='utf-8') as f:
            v_data = json.load(f)
            self.vector_store = v_data.get('chunks', [])

        with open(KNOWLEDGE_INDEX_PATH, 'r', encoding='utf-8') as f:
            k_data = json.load(f)
            for c in k_data.get('chunks', []):
                self.knowledge_chunks[c['chunk_id']] = c

    def query(self, user_query: str, stream: str = None, semester: int = None) -> dict:
        start_time = time.time()
        q_clean = user_query.strip()
        q_lower = q_clean.lower()

        # 1. Strict Negative Grounding Check for explicit out-of-domain patterns
        for pattern in OUT_OF_DOMAIN_PATTERNS:
            if re.search(pattern, q_lower):
                return {
                    "sessionId": None,
                    "response": MANDATORY_UNVERIFIED_MESSAGE,
                    "verified": False,
                    "detectedIntent": "OUT_OF_DOMAIN_REJECTION",
                    "sources": [],
                    "executionTimeMs": int((time.time() - start_time) * 1000)
                }

        # 2. Vector embedding & Hybrid Scoring
        q_vec = generate_query_vector(q_clean)
        scored_hits = []

        for item in self.vector_store:
            # Filters
            if stream and item.get('stream') and item.get('stream') != 'COMMON' and item.get('stream') != 'FIRST_YEAR':
                if item.get('stream').upper() != stream.upper():
                    continue
            if semester and item.get('semester') and item.get('semester') != 0:
                if item.get('semester') != semester:
                    continue

            sim = cosine_similarity(q_vec, item['vector'])

            # Keyword and Entity Boosts
            code_lower = item.get('course_code', '').lower()
            clean_code = re.sub(r'[^a-z0-9]', '', code_lower)
            clean_q = re.sub(r'[^a-z0-9]', '', q_lower)

            if code_lower in q_lower or (len(clean_code) >= 4 and clean_code in clean_q):
                sim += 0.35

            title_lower = item.get('title', '').lower()
            subject_lower = item.get('subject_name', '').lower()

            # Subject match
            if subject_lower in q_lower or (subject_lower.split()[0] in q_lower if subject_lower else False):
                sim += 0.20

            # Module match
            if re.search(r'\bmodule\s*([0-9ivx]+)\b', q_lower):
                m_match = re.search(r'\bmodule\s*([0-9ivx]+)\b', q_lower).group(1)
                if f"module {m_match}" in title_lower or f"module {m_match}" in item.get('content', '').lower():
                    sim += 0.25

            # Panic pack match
            if 'panic' in q_lower and item.get('category') == 'PanicPack':
                sim += 0.25

            # Credit match
            if ('credit' in q_lower or 'grading' in q_lower or 'sgpa' in q_lower) and item.get('category') == 'CreditMatrix':
                sim += 0.30

            cid = item['chunk_id']
            full_chunk = self.knowledge_chunks.get(cid, {})
            scored_hits.append((sim, full_chunk))

        scored_hits.sort(key=lambda x: x[0], reverse=True)

        # 3. Guardrail Threshold Evaluation
        if not scored_hits or scored_hits[0][0] < SIMILARITY_THRESHOLD:
            return {
                "sessionId": None,
                "response": MANDATORY_UNVERIFIED_MESSAGE,
                "verified": False,
                "detectedIntent": "LOW_SIMILARITY_GUARDRAIL",
                "sources": [],
                "executionTimeMs": int((time.time() - start_time) * 1000)
            }

        top_hits = [hit for hit in scored_hits[:3] if hit[0] >= (SIMILARITY_THRESHOLD - 0.05)]
        best_hit = top_hits[0][1]

        # 4. Construct Citations
        citations = []
        for sim, chunk in top_hits:
            citations.append({
                "title": chunk.get("title"),
                "subjectCode": chunk.get("course_code"),
                "subjectName": chunk.get("subject_name"),
                "moduleOrSection": chunk.get("module_or_section"),
                "category": chunk.get("category"),
                "similarity": round(float(sim), 3),
                "fileUrl": chunk.get("action_url") or chunk.get("source_file"),
                "pageUrl": chunk.get("action_url") or chunk.get("source_file")
            })

        # 5. Synthesis: LLM (OpenAI/Gemini) or Resilient Deterministic Mode
        if OPENAI_API_KEY:
            response_text = self._call_openai(q_clean, top_hits)
        elif GEMINI_API_KEY:
            response_text = self._call_gemini(q_clean, top_hits)
        else:
            response_text = self._synthesize_local(q_clean, top_hits)

        return {
            "sessionId": None,
            "response": response_text,
            "verified": True,
            "detectedIntent": "ACADEMIC_RAG",
            "sources": citations,
            "executionTimeMs": int((time.time() - start_time) * 1000)
        }

    def _synthesize_local(self, query: str, top_hits: list) -> str:
        primary = top_hits[0][1]
        cat = primary.get("category", "")
        code = primary.get("course_code", "MAKAUT")
        name = primary.get("subject_name", "")
        section = primary.get("module_or_section", "")
        content = primary.get("content", "")
        source_file = primary.get("source_file", "MAKAUT Academic Documents")

        lines = [
            f"### Verified MAKAUT Curriculum: {name} ({code})",
            f"**Unit / Topic:** {section}",
            "",
            content,
            "",
            f"*Verified from official MAKAUT documentation: `{source_file}`.*"
        ]

        if len(top_hits) > 1:
            second = top_hits[1][1]
            if second.get("module_or_section") != section:
                lines.append(f"\n**Additional Ingested Unit:** {second.get('module_or_section')} ({second.get('course_code')})")
                lines.append(f"{second.get('content')[:200]}...")

        return "\n".join(lines)

    def _call_openai(self, query: str, top_hits: list) -> str:
        try:
            import urllib.request
            context = "\n\n".join([f"[{h[1].get('course_code')} • {h[1].get('module_or_section')}]: {h[1].get('content')}" for h in top_hits])
            prompt = (
                "You are MAKAUT CampusSense AI, an academic advisor for Maulana Abul Kalam Azad University of Technology.\n"
                "Answer the user query strictly using ONLY the verified MAKAUT context below. Explicitly cite the course code and module.\n"
                "If the context does not answer the query, output strictly: 'This information is not verified by official MAKAUT curriculum data.'\n\n"
                f"CONTEXT:\n{context}\n\n"
                f"USER QUERY:\n{query}"
            )
            req = urllib.request.Request(
                "https://api.openai.com/v1/chat/completions",
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {OPENAI_API_KEY}"
                },
                data=json.dumps({
                    "model": "gpt-4o-mini",
                    "messages": [{"role": "user", "content": prompt}],
                    "temperature": 0.2
                }).encode('utf-8')
            )
            with urllib.request.urlopen(req, timeout=12) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                return data["choices"][0]["message"]["content"]
        except Exception:
            return self._synthesize_local(query, top_hits)

    def _call_gemini(self, query: str, top_hits: list) -> str:
        try:
            import urllib.request
            context = "\n\n".join([f"[{h[1].get('course_code')} • {h[1].get('module_or_section')}]: {h[1].get('content')}" for h in top_hits])
            prompt = (
                "You are MAKAUT CampusSense AI, an academic advisor for Maulana Abul Kalam Azad University of Technology.\n"
                "Answer the user query strictly using ONLY the verified MAKAUT context below. Explicitly cite the course code and module.\n"
                "If the context does not answer the query, output strictly: 'This information is not verified by official MAKAUT curriculum data.'\n\n"
                f"CONTEXT:\n{context}\n\n"
                f"USER QUERY:\n{query}"
            )
            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={GEMINI_API_KEY}"
            payload = {
                "contents": [{"parts": [{"text": prompt}]}],
                "generationConfig": {"temperature": 0.2}
            }
            req = urllib.request.Request(
                url,
                headers={"Content-Type": "application/json"},
                data=json.dumps(payload).encode('utf-8')
            )
            with urllib.request.urlopen(req, timeout=12) as resp:
                data = json.loads(resp.read().decode('utf-8'))
                return data["candidates"][0]["content"]["parts"][0]["text"]
        except Exception:
            return self._synthesize_local(query, top_hits)


class RagHttpHandler(BaseHTTPRequestHandler):
    engine = None

    def _set_headers(self, status=200):
        self.send_response(status)
        self.send_header('Content-Type', 'application/json')
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.end_headers()

    def do_OPTIONS(self):
        self._set_headers(204)

    def do_GET(self):
        if self.path == '/health':
            self._set_headers(200)
            self.wfile.write(json.dumps({"status": "UP", "engine": "MAKAUT_RAG_V1"}).encode('utf-8'))
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"error": "Not Found"}).encode('utf-8'))

    def do_POST(self):
        if self.path in ('/api/v1/chat/query', '/chat/query'):
            content_length = int(self.headers.get('Content-Length', 0))
            body = self.rfile.read(content_length).decode('utf-8')
            try:
                payload = json.loads(body)
                query_text = payload.get('query', '')
                stream = payload.get('stream')
                semester = payload.get('semester')
                result = self.engine.query(query_text, stream=stream, semester=semester)
                self._set_headers(200)
                self.wfile.write(json.dumps(result).encode('utf-8'))
            except Exception as e:
                self._set_headers(400)
                self.wfile.write(json.dumps({"error": str(e)}).encode('utf-8'))
        else:
            self._set_headers(404)
            self.wfile.write(json.dumps({"error": "Endpoint not found"}).encode('utf-8'))


def main():
    engine = RagEngine()

    if len(sys.argv) > 1 and sys.argv[1] == '--serve':
        port = int(os.getenv("RAG_PORT", os.getenv("PORT", "8000")))
        if len(sys.argv) > 2 and sys.argv[2].isdigit():
            port = int(sys.argv[2])
        RagHttpHandler.engine = engine
        server = HTTPServer(('127.0.0.1', port), RagHttpHandler)
        print(f"[RAG SERVICE] Started MAKAUT RAG microservice on http://127.0.0.1:{port}")
        server.serve_forever()
    elif len(sys.argv) > 1:
        query_text = " ".join(sys.argv[1:])
        result = engine.query(query_text)
        print(json.dumps(result, indent=2))
    else:
        # Interactive test
        print("MAKAUT CampusSense RAG Engine Ready. Enter query:")
        for line in sys.stdin:
            q = line.strip()
            if not q:
                break
            res = engine.query(q)
            print(f"\nResponse:\n{res['response']}")
            print(f"Verified: {res['verified']} | Sources: {len(res['sources'])}\n")

if __name__ == '__main__':
    main()
