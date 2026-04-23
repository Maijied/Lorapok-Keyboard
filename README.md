# Lorapok Keyboard: Production-Grade Bengali Intelligence 🚀

[![Deployment Status](https://github.com/Maijied/Lorapok-Keyboard/actions/workflows/deploy.yml/badge.svg)](https://maijied.github.io/Lorapok-Keyboard/)
[![Vocabulary Size](https://img.shields.io/badge/Vocabulary-2.34M+-006a4e.svg)](https://maijied.github.io/Lorapok-Keyboard/docs/data-collection)
[![License](https://img.shields.io/badge/License-MIT-f42a41.svg)](LICENSE)
[![Documentation](https://img.shields.io/badge/Documentation-Live-blue.svg)](https://maijied.github.io/Lorapok-Keyboard/)

**Lorapok Keyboard** is a professional, high-performance Bengali input system engineered for the Android platform. It bridges the gap between traditional phonetic typing and modern AI-driven communication, featuring a market-leading 2.3M+ word vocabulary and real-time context-aware prediction.

---

## 🌐 [View Official Documentation & Project Roadmap](https://maijied.github.io/Lorapok-Keyboard/)

---

## ✨ Core Features

### 🧠 Massive Linguistic Intelligence
- **2.34 Million Unique Words**: Powered by a unified corpus of 1.43GB (88M+ tokens) from mC4, TituLM, Wikipedia, and specialized research datasets.
- **Sub-millisecond Latency**: Optimized SQLite-backed bigram engine ensures zero input lag even on entry-level hardware.
- **Phonetic Precision**: Advanced Avro-style transliteration handling complex Bengali conjuncts and vowel modifiers.

### 🎭 Real-Time Tone Rewriting
- Transform your message instantly between **8 distinct tones** (Formal, Friendly, Respectful, Poetic, etc.).
- Intelligent pronoun and verb-ending transformation (তুই/তুমি/আপনি consistency).

### 🎨 Professional UI/UX
- **Ergonomic Design**: 58dp key height and 24dp bottom padding for comfortable, error-free typing.
- **Seamless Interaction**: Long-press serial deletion and intuitive symbol layouts.
- **Theming**: Premium dark-mode glassmorphic aesthetics.

---

## 🏗️ System Architecture

The Lorapok system is built on three main pillars:
1.  **Input Service (Android Native)**: A low-memory footprint IME written in Kotlin.
2.  **Phonetic Engine**: A rule-based transliterator for high-fidelity script conversion.
3.  **Prediction Engine**: A statistical model trained on a 4.2M sentence deduplicated corpus.

For a deep dive into the engineering behind Lorapok, visit our [Architecture Articles](https://maijied.github.io/Lorapok-Keyboard/docs/architecture).

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 17+.
- Python 3.10+ (for data collection scripts).

### Installation
```bash
# Clone the repository
git clone https://github.com/Maijied/Lorapok-Keyboard.git

# Initialize the data environment
./venv/bin/python3 scripts/collect_bengali_data.py --source all
```

---

## 📊 Project Progress

| Phase | Milestone | Status |
|---|---|---|
| **Phase 1** | MVP — Core Keyboard & Phonetic Engine | ✅ COMPLETED |
| **Phase 2** | Scaling — 1M+ Vocabulary Target | ✅ EXCEEDED (2.34M) |
| **Phase 3** | Documentation — Web Hub & CI/CD | ✅ COMPLETED |
| **Phase 4** | Advanced — Bi-LSTM Neural Prediction | ⏸️ ROADMAP |

---

## 🤝 Contributing & Research

This project is part of an ongoing research effort into large-scale Bengali NLP. We welcome contributions, whether in data collection, UI refinement, or model optimization.

- **Author**: [Maijied](https://github.com/maizied)
- **Status**: Production-Ready / Active Research

---

&copy; 2026 Lorapok Intelligence Project. Professional communication, redefined.
