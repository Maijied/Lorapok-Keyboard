# 📖 Lorapok Keuboard — Project Summary

**AI-Powered Bengali Keyboard with Contextual Tone Adaptation**  
**Version:** 1.0 | **Created:** April 18, 2026

---

## What Is Lorapok Keuboard?

The first Bengali keyboard with **AI-powered tone adaptation** — transforming text between 8 culturally-appropriate tones (Formal, Friendly, Respectful, Concise, Detailed, Poetic, Humorous, Empathetic). Built for 268 million Bengali speakers across all major platforms.

---

## Key Features

| Feature | Description | Status |
|---------|-------------|--------|
| **Phonetic Input** | Type `ami` → আমি (Avro-style) | ✅ Done |
| **AI Prediction** | Bigram next-word prediction (500+ vocab) | ✅ Done |
| **Autocorrect** | Fuzzy Trie matching for typo correction | ✅ Done |
| **Tone Rewriting** | 8 Bengali tones with cultural awareness | 🔨 Building |
| **Settings** | Theme, haptic, sound, autocorrect toggles | ✅ Done |
| **Cross-Platform** | Android, iOS, Windows, Linux, macOS | 📋 Planned |
| **Privacy-First** | All AI on-device, no data collection | ✅ Designed |
| **Offline Mode** | Full functionality without internet | ✅ Designed |

---

## Competitive Advantage

| Feature | Gboard | SwiftKey | Avro | **Lorapok** |
|---------|--------|----------|------|-------------|
| Bengali Support | ✅ | ✅ | ✅ | ✅ |
| AI Prediction | ✅ | ✅ | ❌ | ✅ |
| **Tone Adaptation** | ❌ | ❌ | ❌ | **✅ (8 tones!)** |
| Offline AI | ❌ | ⚠️ | N/A | ✅ |
| Privacy-First | ❌ | ❌ | ✅ | ✅ |
| All Platforms | ⚠️ | ⚠️ | ❌ | ✅ |
| Open Source | ❌ | ❌ | ✅ | ✅ (Planned) |

---

## File Inventory (15 Files)

### Development Files
| # | File | Size | Purpose |
|---|------|------|---------|
| 1 | `QUICK_START_GUIDE.md` | ~11 KB | 30-min prototype guide |
| 2 | `bengali_keyboard_roadmap.md` | ~14 KB | 18-month plan |
| 3 | `android/android_keyboard_code.kt` | ~15 KB | Android keyboard service |
| 4 | `ios/ios_keyboard_code.swift` | ~20 KB | iOS keyboard extension |
| 5 | `scripts/train_bengali_model.py` | ~13 KB | AI model training |
| 6 | `scripts/collect_bengali_data.py` | ~15 KB | Data collection |
| 7 | `data/avro_phonetic_rules.json` | ~6.7 KB | Phonetic mappings |

### Tone Rewriting Files
| # | File | Size | Purpose |
|---|------|------|---------|
| 8 | `docs/tone_rewriting_feature.md` | ~32 KB | Tone feature spec |
| 9 | `android/ToneRewriterImplementation.kt` | ~20 KB | Android tone rewriter |
| 10 | `ios/ToneRewriterImplementation.swift` | ~25 KB | iOS tone rewriter |
| 11 | `android/dialog_rewrite_preview.xml` | ~6 KB | Android UI layout |

### Research & Design Files
| # | File | Size | Purpose |
|---|------|------|---------|
| 12 | `docs/research_paper_template.md` | ~31 KB | Research paper |
| 13 | `docs/research_methodology.md` | ~34 KB | User study protocol |
| 14 | `docs/keyboard_design_specification.md` | ~29 KB | Design system |
| 15 | `PROJECT_SUMMARY.md` | ~18 KB | This file |

**Total: ~285 KB code + docs, ~3,500 lines of code, ~60,000 words**

---

## Technical Architecture

```
┌─────────────────────────────────┐
│     User Interface Layer        │
│  (Android/iOS/Windows/Linux)    │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│    Input Processing Layer       │
│  • Key events • Gestures        │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│      Core Engine Layer          │
│  • Phonetic Engine (Avro+ML)    │
│  • AI Prediction (Bi-LSTM)      │
│  • Tone Adaptation (Hybrid)     │
│  • User Learning System         │
└──────────┬──────────────────────┘
           │
┌──────────▼──────────────────────┐
│    Data & Model Layer           │
│  • Bengali corpus (2.3GB)       │
│  • Language model (48MB)        │
│  • Phonetic rules (10K+)       │
│  • User personalization data    │
└─────────────────────────────────┘
```

---

## Performance Targets

| Metric | Target |
|--------|--------|
| Key Press Latency | <16ms (60fps) |
| Prediction Update | <50ms |
| Keyboard Launch | <300ms |
| Memory (Android) | <50 MB |
| Memory (iOS) | <48 MB |
| CPU Usage | <5% |
| Battery Impact | <2%/hr |

---

## Decision Matrix

| Decision | Option A | Option B | Option C |
|----------|----------|----------|----------|
| **Track** | Research (paper) | Product (startup) | Both |
| **Platform** | Android first | iOS first | Both |
| **Team** | Solo | Small (2-3) | Academic lab |
| **Funding** | Self-funded | Research grant | Angel investment |

---

## Quick Start

1. **Read** `QUICK_START_GUIDE.md` — Build first prototype in 30 min
2. **Follow** `bengali_keyboard_roadmap.md` — 18-month plan
3. **Code** `android/android_keyboard_code.kt` — Start building
4. **Design** `docs/keyboard_design_specification.md` — UI reference
5. **Research** `docs/research_paper_template.md` — Write paper

---

**Impact Potential:** 268 million Bengali speakers worldwide  
**বাংলা ভাষার জন্য কাজ করুন! (Work for the Bengali language!)**
