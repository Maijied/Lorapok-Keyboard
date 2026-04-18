# ✨ Lorapok Keuboard — Tone Rewriting Feature Specification

**Version:** 1.0 | **Created:** April 18, 2026

---

## Overview

The Tone Rewriting feature allows users to transform text messages between **8 culturally-appropriate Bengali tones**. This is the **first Bengali keyboard** with this capability.

---

## 8 Bengali Tones

| # | Tone ID | Bengali Name | Emoji | Use Case | Tier |
|---|---------|-------------|-------|----------|------|
| 1 | `formal` | আনুষ্ঠানিক | 🎩 | Business, official | Free |
| 2 | `friendly` | বন্ধুত্বপূর্ণ | 😊 | Close friends | Free |
| 3 | `respectful` | শ্রদ্ধাশীল | 🙏 | Elders, superiors | Free |
| 4 | `concise` | সংক্ষিপ্ত | ⚡ | Quick messages | Free |
| 5 | `detailed` | বিস্তারিত | 📝 | Explanations | Free |
| 6 | `poetic` | কাব্যিক | 📚 | Literary style | Premium |
| 7 | `humorous` | হাস্যরসাত্মক | 😄 | Jokes, light | Premium |
| 8 | `empathetic` | সহানুভূতিশীল | 💝 | Emotional support | Premium |

---

## Cultural Context

Bengali has complex formality levels:
- **আপনি** (formal/respectful) vs **তুমি** (semi-formal) vs **তুই** (very informal)
- Age hierarchy: elders always addressed with আপনি
- Professional context: workplace vs friends
- Regional: Bangladesh vs West Bengal variations

---

## Transformation Examples

### Casual → Formal
```
Input:  তুমি কাল আসবে?
Output: আপনি কি আগামীকাল উপস্থিত থাকবেন?

Changes: তুমি→আপনি, আসবে→উপস্থিত থাকবেন, কাল→আগামীকাল, added কি
```

### Formal → Friendly
```
Input:  আপনি কি আগামীকাল সভায় উপস্থিত থাকবেন?
Output: তুমি কি কাল মিটিং-এ আসবে? 😊

Changes: আপনি→তুমি, আগামীকাল→কাল, সভায়→মিটিং-এ, added emoji
```

### Any → Concise
```
Input:  আমি মনে করি যে আমরা সম্ভবত আগামীকাল অফিসে যেতে পারব না কারণ আবহাওয়া খারাপ
Output: কাল অফিসে যাব না - খারাপ আবহাওয়া
```

### Casual → Respectful
```
Input:  তুই কি কাল আসতে পারবি?
Output: আপনি কি আগামীকাল পধারতে পারবেন?
```

### Any → Poetic
```
Input:  আজ আবহাওয়া খুব সুন্দর
Output: আজ প্রকৃতি তার রূপের মাধুরী মেলে ধরেছে
```

---

## Technical Architecture

### Hybrid Approach

**1. Rule-Based Engine (Simple Tones: Formal, Friendly, Respectful, Concise)**
- Pronoun conversion tables (তুই↔তুমি↔আপনি)
- Verb conjugation rules
- Formality markers (দয়া করে, অনুগ্রহ করে)
- Processing: <100ms
- Accuracy: 85-90%

**2. Neural Engine (Complex Tones: Poetic, Humorous, Empathetic)**
- Transformer seq2seq (6-layer encoder/decoder)
- Trained on 500K tone-transformed pairs
- Processing: <500ms
- Accuracy: 91-95%

**Fallback:** If neural confidence < 0.7, use rule-based

---

## UI Flow

1. User types message → taps ✨ magic wand
2. Tone selector shows (chips/buttons)
3. Selected tone processes text
4. Preview shows original vs rewritten
5. User accepts (Use This) or tries another tone

---

## Accuracy Targets

| Metric | Target |
|--------|--------|
| BLEU Score | >65 |
| Semantic Similarity | >0.90 |
| Formality Classifier | >90% |
| Human Fluency (1-5) | >4.0 |
| Human Adequacy (1-5) | >4.0 |
| Human Appropriateness (1-5) | >4.5 |

---

## Privacy

- All tone processing on-device (rule-based)
- Neural processing optional (user consent)
- No text content stored or transmitted
- Premium tones unlocked via purchase, not data
