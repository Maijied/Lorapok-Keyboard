# Lorapok Keyboard — AI Training & Project Progress
> **This is the canonical reference document for the Lorapok Keyboard project.**
> It tracks every phase of development, the AI training pipeline, and the data collection strategy.
> **Any AI agent or developer reading this can understand the full situation and continue the work.**
> ⚠️ DO NOT delete completed entries. Add new entries below existing ones.

---

## 📌 Project Overview

| Field | Value |
|---|---|
| **Project** | Lorapok Keyboard — A professional Bengali/English AI keyboard |
| **Platform** | Android (Primary), iOS (Deferred until Android is complete) |
| **AI Target** | 1,000,000+ unique Bengali word vocabulary |
| **Engine** | SQLite-backed Bigram Prediction + Phonetic Transliteration (Avro-style) |
| **Project Root** | `/home/maizied/Desktop/Personal_Projects/Keyboard/` |
| **Android Source** | `Lorapok_Keyboard/app/src/main/java/com/lorapok/keyboard/` |
| **Scripts** | `scripts/` |
| **Data** | `data/corpus/` (production), `data/research_corpus/` (research baseline) |

---

## 🗺️ High-Level Roadmap

```
Phase 1: MVP (✅ COMPLETE)
Phase 2: Advanced Intelligence - Android (🔨 IN PROGRESS)
Phase 3: 1M Vocabulary Dataset (🔨 IN PROGRESS)
Phase 4: iOS Feature Parity (⏸️ DEFERRED)
Phase 5: Bi-LSTM Neural Prediction (⏸️ FUTURE)
Phase 6: Premium Tone Features (⏸️ FUTURE)
```

---

## ✅ Phase 1: MVP — COMPLETE

**Goal:** A working Bengali/English keyboard with basic prediction.

| Task | Status | Notes |
|---|---|---|
| Android keyboard layout (QWERTY) | ✅ Done | Gboard-style uniform key sizes |
| Bengali mode (script layout) | ✅ Done | |
| Symbols layout | ✅ Done | Bengali numerals (০-৯) in Bengali mode |
| Phonetic input (Avro-style) | ✅ Done | `ami` → আমি |
| Period (.) and Comma (,) keys | ✅ Done | Bottom row |
| Backspace long-press (serial delete) | ✅ Done | |
| Language toggle (Space bar long-press) | ✅ Done | 🌐 বাংলা/English |
| Gear icon (⚙) → SettingsActivity | ✅ Done | Fixed manifest registration |
| SettingsActivity (Compose UI) | ✅ Done | Haptic, Sound, Autocorrect, Theme toggles |
| Haptic & Sound feedback | ✅ Done | Unified `feedback()` system |
| Autocorrect (1-edit Levenshtein Trie) | ✅ Done | `Trie.kt` fuzzy search |
| Bengali dictionary (433 words) | ✅ Done | `bengali_dictionary.json` |
| English dictionary (421 words) | ✅ Done | `english_dictionary.json` |
| Tone Rewriter (✨ button) | ✅ Done | 8 tones: Formal, Friendly, Respectful, Concise, Detailed + 3 premium placeholders |
| Key height (58dp) + bottom padding (24dp) | ✅ Done | Professional layout |
| Commit & push to Git | ✅ Done | Phase 1 commit pushed |

---

## 🔨 Phase 2: Advanced Intelligence (Android) — IN PROGRESS

**Goal:** Upgrade the prediction engine to production grade with 250k→1M vocabulary.

### 2a. AI Prediction Engine

| Task | Status | Notes |
|---|---|---|
| Migrate from JSON to SQLite (`bigram_model.db`) | ✅ Done | `PredictionEngine.kt` |
| Sub-millisecond indexed bigram lookup | ✅ Done | Indexed SQLite queries |
| Generate bigram model script | ✅ Done | `scripts/generate_bigram_model.py` |
| 191,154 bigram entries in production DB | ✅ Done | 15MB SQLite file |
| Bigram DB in Android assets | ✅ Done | `assets/bigram_model.db` |

### 2b. Phonetic Engine

| Task | Status | Notes |
|---|---|---|
| Expand dictionary from 1,300 → 45,024 phonetic keys | ✅ Done | `scripts/expand_dictionary.py` |
| Frequency-ranked candidate suggestions | ✅ Done | |
| Homonym support (e.g., `tala` → তালা / তলা) | ✅ Done | |
| Hasanta (`,,`), Chandrabindu (`^`), Visarga (`:`) modifiers | ✅ Done | |
| Prefix-based autocomplete | ✅ Done | |

### 2c. Tone Rewriter

| Task | Status | Notes |
|---|---|---|
| Pronoun formality (তুই→তুমি→আপনি) | ✅ Done | |
| Verb formality (আসবি→আসবেন) | ✅ Done | |
| Contextual word upgrade (কাল→আগামীকাল) | ✅ Done | |
| Modern slang support (মামা, চিল, পেরা) | ✅ Done | Added to `ToneRewriter.kt` |
| Premium tone placeholders (Poetic, Humorous, Empathetic) | 🔨 Placeholder | Needs full implementation |

### 2d. Corpus & Training Data

| Task | Status | Notes |
|---|---|---|
| Bengali Wikipedia full dump (518MB) | ✅ Done | 671,127 lines extracted |
| News RSS scraping (Prothom Alo, Samakal, Janakantha, BD Pratidin, Jugantor) | ✅ Done | |
| Bangla literature / books (csebuetnlp/banglabook HF) | ✅ Done | |
| Somewhereinblog scraper (500 pages) | ✅ Done | |
| Techtunes scraper (improved, 200 pages) | ✅ Done | Timeout + selector fixes |
| Medical Bengali (Swasthya Bangla, Prothom Alo Health) | ✅ Done | Added to NEWS_SOURCES |
| Legal Bengali (LawHelpBD) | ✅ Done | Added to NEWS_SOURCES |
| Reddit r/bangladesh (social/conversational) | ✅ Done | Recursive pagination |
| Facebook/Twitter public data | 🔨 In Progress | Via search + ROOTS dataset attempt |
| Research baseline preserved | ✅ Done | `data/research_corpus/bengali_research_v1.txt` |
| **Current vocabulary** | **644,968 unique words** | As of 2026-04-23 |
| **Target vocabulary** | **1,000,000 unique words** | |

---

## 🗄️ Data Source Registry

### ✅ Currently Integrated Sources

| Source | Type | URL / Path | Status |
|---|---|---|---|
| Bengali Wikipedia | Encyclopedia | `dumps.wikimedia.org/bnwiki/` | ✅ Integrated |
| Prothom Alo | News RSS | `prothomalo.com/feed` | ✅ Integrated |
| Prothom Alo Health | News RSS | `prothomalo.com/feed/topic/স্বাস্থ্য` | ✅ Integrated |
| BD Pratidin | News RSS | `bd-pratidin.com/rss.xml` | ✅ Integrated |
| Samakal | News RSS | `samakal.com/feed` | ✅ Integrated |
| Jugantor | News RSS | `jugantor.com/feed` | ✅ Integrated |
| Janakantha | News RSS | `janakantha.com/feed` | ✅ Integrated |
| Swasthya Bangla | Medical RSS | `swasthyabangla.com/feed` | ✅ Integrated |
| LawHelpBD | Legal RSS | `lawhelpbd.com/feed` | ✅ Integrated |
| Somewhereinblog | Blog Scraper | `somewhereinblog.net` | ✅ Integrated |
| Techtunes | Blog Scraper | `techtunes.co/recent/page/N` | ✅ Integrated |
| Reddit r/bangladesh | Social | `reddit.com/r/bangladesh` | ✅ Integrated |
| banglabook (HuggingFace) | Literature | `csebuetnlp/banglabook` | ✅ Integrated |

### 🎯 HIGH-PRIORITY Sources to Add Next

These are from the user-provided resource list and should be integrated to push past 1M words:

| Source | Type | URL | Priority | Notes |
|---|---|---|---|---|
| **TituLM Bangla Corpus** | Web Crawl HF | `huggingface.co/datasets/hishab/titulm-bangla-corpus` | ✅ Integrated |

### 🎯 HIGH-PRIORITY Sources to Add Next

These are from the user-provided resource list and should be integrated to push past 1M words:

| Source | Type | URL | Priority | Notes |
|---|---|---|---|---|
| **BDNC** | National Corpus | `corpus.bangla.gov.bd` | 🔴 CRITICAL | 3B+ words, multiple domains |
| **Vacaspati Literature** | Literature | `bangla.iitk.ac.in` | 🔴 HIGH | 115M words, multi-genre |
| **Bangla Academy** | Lexicon | `banglaacademy.gov.bd` | 🔴 HIGH | Official formal Bengali |
| **eBangla Library** | Literature | `ebanglalibrary.com` | 🟡 MEDIUM | Books, long sentences |
| **BBC Bengali** | News | `bbc.com/bengali` | 🟡 MEDIUM | Add to NEWS_SOURCES |
| **Ittefaq** | News | `ittefaq.com.bd` | 🟡 MEDIUM | Add to NEWS_SOURCES |
| **Bangla Tribune** | News | `banglatribune.com` | 🟡 MEDIUM | Add to NEWS_SOURCES |
| **Jago News** | News | `jagonews24.com` | 🟡 MEDIUM | Add to NEWS_SOURCES |
| **Ananda Bazar** | News | `anandabazar.com` | 🟡 MEDIUM | West Bengal Bengali |
| **Kaler Kantho** | News | `kalerkantho.com` | 🟡 MEDIUM | Add to NEWS_SOURCES |
| **Somoy News** | News | `somoynews.tv` | 🟡 MEDIUM | Add to NEWS_SOURCES |
| **Amarblog** | Blog | `amarblog.com` | 🟡 MEDIUM | Blog scraper |
| **Istishon** | Blog | `istishon.com` | 🟡 MEDIUM | Blog scraper |
| **Bangla Tangla** | Blog | `banglatangla.com` | 🟡 MEDIUM | Blog scraper |
| **Archive.org Bengali Books** | Literature | `archive.org` (search: Bengali) | 🟡 MEDIUM | Public domain |
| **BNLP GitHub** | HF/Datasets | `github.com/sagorbrur/bnlp` | 🟡 MEDIUM | Ready-made datasets |
| **csebuetnlp GitHub** | Research NLP | `github.com/csebuetnlp` | 🟡 MEDIUM | Multiple datasets |
| **Bangla Regional Corpus** | Dialect | `data.mendeley.com/datasets/92r62h4k5k` | 🟢 LOW | Rangpur, Barisal, Khulna dialects |
| **Feni Dialect Dataset** | Dialect | `sciencedirect.com/...` | 🟢 LOW | Parallel dialect corpus |
| **BanglaBioMed** | Medical NLP | `aclanthology.org/2022.bionlp-1.31/` | 🟢 LOW | Biomedical NER |
| **Rokomari** | Books | `rokomari.com/book` | 🟢 LOW | Metadata only |

---

## 🔧 Key Scripts

| Script | Purpose | Command |
|---|---|---|
| `collect_bengali_data.py` | Collect all corpus data | `./venv/bin/python3 scripts/collect_bengali_data.py --source all --output ./data/corpus/` |
| `generate_bigram_model.py` | Build SQLite bigram DB | `./venv/bin/python3 scripts/generate_bigram_model.py --min_freq 5 --top_k 5 --format sqlite` |
| `expand_dictionary.py` | Expand phonetic dictionary | `./venv/bin/python3 scripts/expand_dictionary.py` |
| **See stats** | Show current vocabulary size | `./venv/bin/python3 scripts/collect_bengali_data.py --stats --output ./data/corpus/` |
| **Merge all** | Merge & deduplicate all sources | `./venv/bin/python3 scripts/collect_bengali_data.py --merge --output ./data/corpus/ --stats` |

### Available `--source` Options
```
wikipedia  → Bengali Wikipedia dump
oscar      → OSCAR corpus (GATED — requires HF_TOKEN)
news       → All news RSS sources
sample     → Built-in sample sentences
hf         → Any HuggingFace dataset (use --dataset NAME)
blog       → Somewhereinblog + Techtunes
social     → Reddit r/bangladesh + Facebook/Twitter search
titulm     → TituLM Bangla Corpus
all        → All public sources (skips gated datasets)
```

---

## 🏗️ Architecture — Android

```
User Types
    ↓
LorapokKeyboardService.kt  ← Main keyboard service
    ├── PhoneticEngine.kt    ← Avro-style transliteration (45,024 keys)
    ├── PredictionEngine.kt  ← SQLite bigram next-word prediction
    ├── ToneRewriter.kt      ← 8-tone Bengali formality engine
    ├── Trie.kt              ← Fuzzy autocorrect (1-edit Levenshtein)
    └── UserLearningSystem.kt← Personalized frequency learning
```

**Key Assets:**
- `assets/bigram_model.db` → 40MB SQLite, 513k bigrams
- `assets/bengali_dictionary.json` → 433 words
- `assets/english_dictionary.json` → 421 words
- `assets/avro_phonetic_rules.json` → Phonetic mapping rules

---

## 📊 Current Corpus Stats (as of 2026-04-23)

| Metric | Value |
|---|---|
| Unique Vocabulary | **1,139,771 words** |
| Total Tokens | ~10,000,000+ |
| Cleaned Sentences | 132,965 unique lines |
| Corpus Size | 548.0 MB |
| Target Vocabulary | 1,000,000 words |
| Progress | **114.0% (Milestone Reached!)** |

---

## ⏸️ Phase 3: iOS Feature Parity — DEFERRED

**Wait until Android Phase 2+3 are 100% complete.**

| Task | Status |
|---|---|
| Port Advanced Tone Rewriter to Swift | ⏸️ Deferred |
| Add Settings UI (Theme, Haptic, Sound) | ⏸️ Deferred |
| Bengali numerals in symbol layout | ⏸️ Deferred |
| Sync button sizes with Android | ⏸️ Deferred |

---

## 🔮 Phase 4: Future Features — NOT STARTED

| Feature | Notes |
|---|---|
| Bi-LSTM Neural Model | Full sequence-to-sequence next-word prediction |
| Premium Tones (Poetic, Humorous, Empathetic) | Full rule implementation |
| User adaptation (learning from typing habits) | `UserLearningSystem.kt` already scaffolded |
| Cloud sync of user preferences | |
| Glide/swipe typing | |
| Voice input (Bengali STT) | |

---

## 📝 Change Log (Never Delete)

| Date | Change | Author |
|---|---|---|
| 2026-04 | Phase 1 MVP complete. Android keyboard with all basic features. | AI Agent |
| 2026-04 | Phase 2: SQLite PredictionEngine, 45k phonetic keys, 8-tone rewriter | AI Agent |
| 2026-04 | Wikipedia dump integrated (671k lines), 621k unique words reached | AI Agent |
| 2026-04 | Research corpus baseline saved to `data/research_corpus/` | AI Agent |
| 2026-04 | Medical/Legal Bengali sources added (Swasthya Bangla, LawHelpBD) | AI Agent |
| 2026-04 | Blog scrapers improved (Techtunes timeout/selector fix) | AI Agent |
| 2026-04 | Reddit social scraper added (recursive pagination) | AI Agent |
| 2026-04 | Facebook/Twitter collector added (search-based) | AI Agent |
| 2026-04 | Script error handling: each source wrapped in try/except, no crash on gated datasets | AI Agent |
| 2026-04 | Master progress doc `AI_TRAINING_PROGRESS.md` created | AI Agent |
| 2026-04 | TituLM corpus collected (100k samples, 566MB), new RSS added | AI Agent |
| 2026-04 | **1,139,771 unique words** reached! 1M milestone complete. | AI Agent |
| 2026-04 | Generated 40MB SQLite bigram DB with 513,925 entries. | AI Agent |

---

## 🚨 Known Issues / Blockers

| Issue | Impact | Resolution |
|---|---|---|
| OSCAR corpus (oscar-corpus/OSCAR-2301) is GATED | Cannot use in `--source all` | Use `--source oscar` with `HF_TOKEN` set |
| Techtunes intermittent timeouts | Partial data only | Timeout increased to 20s, fallback to `<p>` tags |
| `--stats` command needs `--output` flag | Minor UX | Always use `--stats --output ./data/corpus/` |

---

*Last updated: 2026-04-23 by AI Agent. Update this file after every significant change.*
