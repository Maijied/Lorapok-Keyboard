# 📋 Lorapok Keuboard — Development Roadmap
## 18-Month Plan: From Prototype to Multi-Platform Launch

**Project:** Lorapok Keuboard — AI-Powered Bengali Keyboard  
**Version:** 1.0 | **Created:** April 18, 2026

---

## Phase 1: MVP (Weeks 1-7) — Android Only

### Week 1-2: Core Development
- [x] Android keyboard service setup (InputMethodService)
- [x] Phonetic conversion engine (Avro-style: ami → আমি)
- [x] Basic UI with dark theme (Slate/Indigo)
- [x] 400+ common Bengali words dictionary
- [x] English ↔ Bengali language switching
- [x] Basic key press animations & haptic feedback

### Week 3-4: Features
- [x] AI predictions (frequency-based, bigram model)
- [x] Autocorrection for common mistakes (fuzzy Trie matching)
- [x] Emoji keyboard integration
- [x] Settings screen (theme, haptic, language, autocorrect)
- [x] User dictionary (learns new words)

### Week 5-6: Testing & Polish
- [ ] Beta testing with 20 users
- [ ] Bug fixes from feedback
- [ ] Performance optimization (<50MB, <16ms key latency)
- [ ] Play Store listing preparation (screenshots, description)

### Week 7: Launch
- [ ] Google Play submission ($25 one-time)
- [ ] Marketing materials (Bengali + English)
- [ ] Social media announcement
- [ ] Initial user support channel

**MVP Targets:** 30+ WPM, 100+ words, basic autocorrect, light theme, Android only

---

## Phase 2: Full v1.0 (Weeks 8-22) — Advanced Features

### Weeks 8-10: AI Model
- [ ] Collect Bengali corpus (2.3GB target)
- [ ] Train Bi-LSTM model (50K vocabulary)
- [ ] Quantize model (187MB → 48MB)
- [ ] Integrate TFLite into keyboard
- [ ] Achieve Top-3 accuracy >67%

### Weeks 11-14: Tone Rewriting
- [ ] Implement 5 free tones (Formal, Friendly, Respectful, Concise, Detailed)
- [ ] Implement 3 premium tones (Poetic, Humorous, Empathetic)
- [ ] Rule-based engine for simple tones (<100ms)
- [ ] Neural engine for complex tones (<500ms)
- [ ] Preview dialog UI

### Weeks 15-18: Polish & iOS
- [ ] Dark theme + Professional theme
- [ ] Voice input integration
- [ ] Cloud backup (premium)
- [ ] iOS keyboard extension development
- [ ] Core ML model conversion

### Weeks 19-22: Testing & Release
- [ ] 50-user beta test
- [ ] Performance benchmarks met
- [ ] iOS App Store submission ($99/year)
- [ ] v1.0 release on both platforms

---

## Phase 3: Multi-Platform (Months 6-9)

### Windows
- [ ] Text Services Framework (TSF) implementation
- [ ] ONNX Runtime for ML model
- [ ] Win 7/10/11 compatibility
- [ ] Windows signing certificate ($100-$300)

### Linux
- [ ] IBus engine implementation
- [ ] Fcitx compatibility
- [ ] GTK/Qt theming
- [ ] Ubuntu, Fedora, Arch packages

### macOS (Bonus)
- [ ] Input Method Kit
- [ ] Core ML integration
- [ ] macOS App Store submission

### Cross-Platform Sync
- [ ] Cloud sync service
- [ ] Unified settings
- [ ] Consistent UI/UX across platforms

---

## Phase 4: Scale & Research (Months 9-18)

### Research Track
- [ ] IRB approval for user study
- [ ] 150-participant study execution
- [ ] Data analysis & paper writing
- [ ] Submit to CHI/ACL conference

### Product Track
- [ ] 10,000+ downloads target
- [ ] Premium subscription model
- [ ] Enterprise partnerships
- [ ] Revenue: $5K-$10K/month target

### Growth
- [ ] 100,000+ downloads
- [ ] Media coverage
- [ ] Community building
- [ ] Version 2.0 planning

---

## Budget Breakdown

| Item | Solo | Small Team (5) |
|------|------|----------------|
| Google Play | $25 | $25 |
| Apple Developer | $99/yr | $99/yr |
| Windows Signing | $200 | $200 |
| Cloud Hosting | $10-50/mo | $50-200/mo |
| Testing Devices | $500-1K | $2K-5K |
| **Salaries** | **$0** | **$150K-300K** |
| **Total Year 1** | **~$1,000** | **~$200,000** |

**Minimum to launch:** $25 (Google Play fee only!)

---

## Team Structure (If Scaling)

| Role | Count | Focus |
|------|-------|-------|
| Android Developer | 1-2 | Keyboard + UI |
| iOS Developer | 1 | iOS port |
| ML Engineer | 1 | AI model + tone |
| Backend (optional) | 1 | Cloud sync |
| Designer | 1 | UI/UX + branding |
| QA Tester | 1 | Testing + beta |
| Project Manager | 1 | Coordination |

---

## Key Milestones

| When | Milestone | Success Metric |
|------|-----------|---------------|
| Month 1 | Working prototype | ami → আমি on phone |
| Month 2 | MVP launch | Google Play listing |
| Month 3 | 100+ users | 4+ star rating |
| Month 6 | iOS + v1.0 | 1,000+ total users |
| Month 9 | Desktop versions | All 5 platforms |
| Month 12 | Scale | 10,000+ users OR paper published |
| Month 18 | Mature | 100,000+ users, sustainable revenue |
