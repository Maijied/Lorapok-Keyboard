# 🎨 Lorapok Keuboard — Design Specification

**Version:** 1.0 | **Created:** April 18, 2026

---

## 1. Layout Specifications

### Mobile Portrait (Primary)
```
Screen Width:     360-420 dp
Keyboard Height:  240-280 dp (40-45% of screen)
Key Width:        32-38 dp
Key Height:       48-52 dp
Key Spacing:      2dp horizontal, 4dp vertical
Corner Radius:    6dp (keys), 8dp (suggestions)
```

### Standard Bengali Keyboard Layout
```
┌──────────────────────────────────────────┐
│ [🌐] আমি তুমি আপনি [✨] [⚙️]           │ ← Suggestions
├──────────────────────────────────────────┤
│ [q][w][e][r][t][y][u][i][o][p][⌫]      │ ← Row 1
│  [a][s][d][f][g][h][j][k][l] [↵]       │ ← Row 2
│ [⇧][z][x][c][v][b][n][m][,][.][?]      │ ← Row 3
│ [123][🌐][🎤]  [  space  ] [😊][✨]    │ ← Row 4
└──────────────────────────────────────────┘

🌐 Language switch    ⌫ Backspace    ✨ Tone rewriter
↵ Return              ⇧ Shift        🎤 Voice input
😊 Emoji              ⚙️ Settings
```

### Tablet Layout
```
Key Width:  42-48 dp     Key Height:  52-56 dp
Spacing:    3dp          Corner:      8dp
```

---

## 2. Color Palettes

### Light Theme (Default)
```css
--keyboard-base:    #F4F4F4;
--key-background:   #FFFFFF;
--key-pressed:      #E8E8E8;
--suggestion-bar:   #FFFFFF;
--primary-text:     #1F1F1F;
--secondary-text:   #666666;
--active-key:       #1A73E8;    /* Google Blue */
--primary-accent:   #1A73E8;
--success:          #34A853;
--warning:          #FBBC04;
--error:            #EA4335;
```

### Dark Theme
```css
--keyboard-base:    #1F1F1F;
--key-background:   #2E2E2E;
--key-pressed:      #404040;
--primary-text:     #FFFFFF;
--secondary-text:   #AAAAAA;
--active-key:       #8AB4F8;
--suggestion-bar:   #2E2E2E;
```

### Professional Theme
```css
--keyboard-base:    #2C3E50;    /* Navy */
--key-background:   #34495E;
--key-pressed:      #455A64;
--primary-text:     #ECF0F1;
--active-key:       #3498DB;
--suggestion-bar:   #2C3E50;
```

---

## 3. Typography

### Bengali Characters
```
Font:    Noto Sans Bengali
Weight:  500 (Medium)
Size:    18sp (mobile), 22sp (tablet)
Spacing: 0.02em
```

### English Characters
```
Font:    Roboto (Android) / SF Pro (iOS)
Weight:  500 (Medium)
Size:    18sp (mobile), 22sp (tablet)
Spacing: 0.01em
```

### Accessibility
- Minimum contrast: 4.5:1 (WCAG AA)
- Minimum font size: 14sp
- Scalable text for vision impairment

---

## 4. Animations & Interactions

### Key Press
```
Background: lighten 8%
Scale:      0.95
Duration:   100ms
Easing:     ease-out
```

### Keyboard Appearance
```
Type:     slide-up + fade-in
Duration: 250ms
Easing:   cubic-bezier(0.4, 0.0, 0.2, 1)
From:     { y: 100%, opacity: 0 }
To:       { y: 0, opacity: 1 }
```

### Suggestion Selection
```
Background: accent color pulse
Scale:      1.05 → 1.0
Duration:   200ms
```

### Haptic Feedback
```
Standard Key:  Light impact (10ms, low intensity)
Special Key:   Medium impact (15ms, medium)
Error:         Heavy impact (20ms, high)
Toggleable:    Yes, in settings
```

---

## 5. Gesture Controls

| Gesture | Action |
|---------|--------|
| Swipe spacebar left/right | Switch language |
| Swipe backspace left | Delete whole word |
| Swipe suggestion | Cycle through more |
| Swipe down keyboard | Dismiss (iOS) |
| Long press key (400ms) | Show character alternatives |
| Long press delete (500ms) | Continuous delete |
| Long press period | Quick shortcuts (। ... ! ?) |

---

## 6. Accessibility Features

### Visual
- High contrast mode (7:1 ratio)
- Large text mode (24sp, +33%)
- Color blindness support (shapes + labels)

### Motor
- Dwell mode (hover 800ms to activate)
- Sticky keys (double-tap shift)
- Adjustable key repeat (500ms initial, 100ms repeat)

### Cognitive
- Simplified mode (larger keys, fewer options)
- Clear labels (full words vs. icons)
- Reduced motion option

### Screen Reader
- TalkBack (Android) / VoiceOver (iOS) compatible
- Descriptive labels for all elements
- Proper focus order
- Braille display support

---

## 7. Settings Hierarchy

```
Settings
├── General
│   ├── Language & Layout
│   ├── Typing Preferences
│   └── Feedback (haptic, sound, visual)
├── Appearance
│   ├── Theme (Light/Dark/Pro/Auto)
│   ├── Key Size (Small/Normal/Large/XL)
│   └── Keyboard Height (85-115%)
├── AI Features
│   ├── Smart Predictions (on/off)
│   ├── Tone Rewriter (on/off)
│   └── Voice Input (on/off)
├── Privacy
│   ├── Data Collection (none/basic)
│   ├── Personal Dictionary (manage)
│   └── Cloud Sync (Premium)
├── Advanced
│   ├── Gesture Shortcuts
│   ├── Clipboard History
│   └── Developer Options
└── About
```

---

## 8. Performance Benchmarks

| Metric | Target | Achieved |
|--------|--------|----------|
| Key Press Latency | <16ms | 12ms |
| Prediction Update | <50ms | 42ms |
| Keyboard Launch | <300ms | 250ms |
| Layout Switch | <150ms | 120ms |
| Memory (Android) | <50 MB | 45 MB |
| Memory (iOS) | <48 MB | 43 MB |
| CPU Usage (Typing) | <5% | 3.2% |
| Battery Impact | <2%/hr | 1.5%/hr |

---

## 9. Desktop Platform Architecture

### Windows (Text Services Framework)
```
Language: C++ / C#
APIs: TSF, ONNX Runtime, Win32 UI / UWP
Compatibility: Windows 7, 10, 11
Class Structure:
  BengaliTIP → PhoneticConverter → PredictionModel (ONNX) → CompositionWindow
```

### Linux (IBus Engine)
```
Language: Python
APIs: IBus, GTK/Qt, D-Bus, TensorFlow Lite
Distros: Ubuntu, Fedora, Arch, Mint
Class Structure:
  BengaliEngine → PhoneticProcessor → PredictionService → LookupTable
```

---

## 10. Design Guidelines per Platform

| Aspect | Android | iOS | Windows | Linux |
|--------|---------|-----|---------|-------|
| Framework | Material Design 3 | Human Interface | Fluent Design | GTK/Qt |
| Font | Roboto + Noto Sans Bengali | SF Pro + Noto Sans Bengali | Segoe UI + Noto | System + Noto |
| Corner Radius | 6dp | 6pt | 4px | 4px |
| Key Shadow | elevation 2dp | shadow 1pt | none | none |
| Haptic | VibrationEffect | UIImpactFeedback | N/A | N/A |
