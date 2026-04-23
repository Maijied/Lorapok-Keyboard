import UIKit

// MARK: - Lorapok Keuboard — iOS Keyboard Extension
// KeyboardViewController: Main UIInputViewController for Bengali AI keyboard
// Features: Phonetic conversion, AI prediction, language switching, tone rewriting
// Min iOS: 13.0 | Memory: <48 MB

enum KeyboardState {
    case qwerty
    case symbols
}

class KeyboardViewController: UIInputViewController {

    // MARK: - Properties
    private var phoneticEngine: PhoneticEngine!
    private var predictionEngine: PredictionEngine!
    private var userLearning: UserLearningSystem!

    private var isBengaliMode = true
    private var isShiftActive = false
    private var currentState: KeyboardState = .qwerty
    private var phoneticBuffer = ""
    private var suggestionButtons: [UIButton] = []

    // Settings (from UserDefaults)
    private var hapticEnabled: Bool { UserDefaults.standard.object(forKey: "kbd_haptic") as? Bool ?? true }
    private var soundEnabled: Bool { UserDefaults.standard.object(forKey: "kbd_sound") as? Bool ?? true }

    private let keyboardBackground = UIColor(red: 0.957, green: 0.957, blue: 0.957, alpha: 1.0)
    private let keyColor = UIColor.white
    private let specialKeyColor = UIColor(red: 0.373, green: 0.388, blue: 0.404, alpha: 1.0)
    private let accentColor = UIColor(red: 0.102, green: 0.451, blue: 0.910, alpha: 1.0)
    private let textColor = UIColor(red: 0.122, green: 0.122, blue: 0.122, alpha: 1.0)

    private let qwertyRows: [[String]] = [
        ["q","w","e","r","t","y","u","i","o","p"],
        ["a","s","d","f","g","h","j","k","l"],
        ["z","x","c","v","b","n","m"]
    ]

    private let symbolRows: [[String]] = [
        ["1","2","3","4","5","6","7","8","9","0"],
        ["-","/",":",";","(",")","$","&","@","\""],
        [".",",","?","!","'"]
    ]

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        phoneticEngine = PhoneticEngine()
        predictionEngine = PredictionEngine()
        userLearning = UserLearningSystem()
        setupKeyboard()
    }

    override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
    }

    override func textWillChange(_ textInput: UITextInput?) {
        // Called before text changes
    }

    override func textDidChange(_ textInput: UITextInput?) {
        // Update UI based on context
    }

    // MARK: - Keyboard Setup

    private func setupKeyboard() {
        guard let inputView = self.inputView else { return }
        inputView.backgroundColor = keyboardBackground
        inputView.subviews.forEach { $0.removeFromSuperview() }

        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 4
        stackView.translatesAutoresizingMaskIntoConstraints = false
        inputView.addSubview(stackView)

        NSLayoutConstraint.activate([
            stackView.leadingAnchor.constraint(equalTo: inputView.leadingAnchor, constant: 4),
            stackView.trailingAnchor.constraint(equalTo: inputView.trailingAnchor, constant: -4),
            stackView.topAnchor.constraint(equalTo: inputView.topAnchor, constant: 4),
            stackView.bottomAnchor.constraint(equalTo: inputView.bottomAnchor, constant: -8)
        ])

        // Suggestion bar
        let suggestionRow = createSuggestionBar()
        stackView.addArrangedSubview(suggestionRow)

        // Row content based on state
        let activeRows = (currentState == .qwerty) ? qwertyRows : symbolRows
        
        for (index, row) in activeRows.enumerated() {
            let rowStack = createKeyRow(keys: row, rowIndex: index)
            stackView.addArrangedSubview(rowStack)
        }

        // Bottom row
        let bottomRow = createBottomRow()
        stackView.addArrangedSubview(bottomRow)

        updateSuggestions(["আমি", "তুমি", "আপনি"])
    }

    // MARK: - Suggestion Bar

    private func createSuggestionBar() -> UIStackView {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.distribution = .fillEqually
        stack.spacing = 4
        stack.heightAnchor.constraint(equalToConstant: 44).isActive = true

        let bgView = UIView()
        bgView.backgroundColor = .white
        bgView.layer.cornerRadius = 8
        stack.insertSubview(bgView, at: 0)
        bgView.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            bgView.leadingAnchor.constraint(equalTo: stack.leadingAnchor),
            bgView.trailingAnchor.constraint(equalTo: stack.trailingAnchor),
            bgView.topAnchor.constraint(equalTo: stack.topAnchor),
            bgView.bottomAnchor.constraint(equalTo: stack.bottomAnchor)
        ])

        suggestionButtons = (0..<5).map { _ in
            let btn = UIButton(type: .system)
            btn.titleLabel?.font = UIFont.systemFont(ofSize: 16, weight: .medium)
            btn.setTitleColor(accentColor, for: .normal)
            btn.addTarget(self, action: #selector(suggestionTapped(_:)), for: .touchUpInside)
            stack.addArrangedSubview(btn)
            return btn
        }

        return stack
    }

    private func updateSuggestions(_ words: [String]) {
        for (i, btn) in suggestionButtons.enumerated() {
            if i < words.count {
                btn.setTitle(words[i], for: .normal)
                btn.isHidden = false
            } else {
                btn.isHidden = true
            }
        }
    }

    @objc private func suggestionTapped(_ sender: UIButton) {
        guard let word = sender.title(for: .normal) else { return }
        hapticFeedback()

        // Delete current buffer
        if !phoneticBuffer.isEmpty {
            for _ in 0..<phoneticBuffer.count {
                textDocumentProxy.deleteBackward()
            }
            phoneticBuffer = ""
        }
        textDocumentProxy.insertText(word + " ")
        userLearning.recordWord(word)
        updateSuggestions(predictionEngine.predict(currentWord: word))
    }

    // MARK: - Key Row Creation

    private func createKeyRow(keys: [String], rowIndex: Int) -> UIStackView {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.distribution = .fillEqually
        stack.spacing = 4
        stack.heightAnchor.constraint(equalToConstant: 48).isActive = true

        // Shift on row 3
        if rowIndex == 2 {
            let shiftBtn = createSpecialKey(title: "⇧") { [weak self] in self?.toggleShift() }
            shiftBtn.widthAnchor.constraint(equalToConstant: 44).isActive = true
            stack.addArrangedSubview(shiftBtn)
        }

        for key in keys {
            let btn = createCharacterKey(key)
            stack.addArrangedSubview(btn)
        }

        // Backspace on row 1
        if rowIndex == 0 {
            let delBtn = createSpecialKey(title: "⌫") { [weak self] in self?.handleBackspace() }
            delBtn.widthAnchor.constraint(equalToConstant: 44).isActive = true
            stack.addArrangedSubview(delBtn)
        }

        // Return on row 2
        if rowIndex == 1 {
            let retBtn = createSpecialKey(title: "↵") { [weak self] in self?.handleReturn() }
            retBtn.widthAnchor.constraint(equalToConstant: 52).isActive = true
            stack.addArrangedSubview(retBtn)
        }

        return stack
    }

    private func createBottomRow() -> UIStackView {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.spacing = 4
        stack.heightAnchor.constraint(equalToConstant: 48).isActive = true

        let numBtn = createSpecialKey(title: "123") { [weak self] in self?.toggleSymbols() }
        numBtn.widthAnchor.constraint(equalToConstant: 44).isActive = true
        stack.addArrangedSubview(numBtn)

        let langBtn = createSpecialKey(title: "🌐") { [weak self] in self?.toggleLanguage() }
        langBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(langBtn)

        let commaBtn = createCharacterKey(",")
        commaBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(commaBtn)

        let spaceBtn = UIButton(type: .system)
        spaceBtn.setTitle(isBengaliMode ? "🌐 বাংলা" : "🌐 English", for: .normal)
        spaceBtn.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        spaceBtn.setTitleColor(textColor, for: .normal)
        spaceBtn.backgroundColor = keyColor
        spaceBtn.layer.cornerRadius = 6
        spaceBtn.addTarget(self, action: #selector(spaceTapped), for: .touchUpInside)
        
        let longPress = UILongPressGestureRecognizer(target: self, action: #selector(handleSpaceLongPress(_:)))
        spaceBtn.addGestureRecognizer(longPress)
        
        stack.addArrangedSubview(spaceBtn)

        let periodLabel = isBengaliMode ? "।" : "."
        let periodBtn = createCharacterKey(periodLabel)
        periodBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(periodBtn)

        let toneBtn = createSpecialKey(title: "✨") { [weak self] in self?.handleToneRewrite() }
        toneBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(toneBtn)

        let gearBtn = createSpecialKey(title: "⚙") { [weak self] in self?.openSettings() }
        gearBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(gearBtn)

        let retBtn = createSpecialKey(title: "↵") { [weak self] in self?.handleReturn() }
        retBtn.widthAnchor.constraint(equalToConstant: 50).isActive = true
        stack.addArrangedSubview(retBtn)

        return stack
    }

    // MARK: - Key Creation Helpers

    private func createCharacterKey(_ key: String) -> UIButton {
        let btn = UIButton(type: .system)
        let displayKey = isShiftActive ? key.uppercased() : key
        btn.setTitle(displayKey, for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 18, weight: .medium)
        btn.setTitleColor(textColor, for: .normal)
        btn.backgroundColor = keyColor
        btn.layer.cornerRadius = 6
        btn.layer.shadowColor = UIColor.black.cgColor
        btn.layer.shadowOffset = CGSize(width: 0, height: 1)
        btn.layer.shadowOpacity = 0.1
        btn.layer.shadowRadius = 1
        btn.tag = Int(key.unicodeScalars.first?.value ?? 0)
        btn.addTarget(self, action: #selector(keyTapped(_:)), for: .touchUpInside)
        return btn
    }

    private func createSpecialKey(title: String, action: @escaping () -> Void) -> UIButton {
        let btn = CallbackButton(action: action)
        btn.setTitle(title, for: .normal)
        btn.titleLabel?.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        btn.setTitleColor(.white, for: .normal)
        btn.backgroundColor = specialKeyColor
        btn.layer.cornerRadius = 6
        return btn
    }

    // MARK: - Input Handling

    @objc private func keyTapped(_ sender: UIButton) {
        hapticFeedback()
        playClickSound()
        guard let title = sender.title(for: .normal) else { return }
        
        if currentState == .symbols {
            let commitStr = isBengaliMode ? convertToBengaliNumber(title) : title
            textDocumentProxy.insertText(commitStr)
            return
        }

        let char = isShiftActive ? title.uppercased() : title.lowercased()

        if isBengaliMode {
            phoneticBuffer += char.lowercased()
            if let converted = phoneticEngine.convert(phoneticBuffer) {
                // Delete buffer chars already in proxy
                let deleteCount = phoneticBuffer.count - 1
                for _ in 0..<deleteCount {
                    textDocumentProxy.deleteBackward()
                }
                textDocumentProxy.insertText(converted)
                let predictions = predictionEngine.predict(currentWord: converted)
                updateSuggestions(predictions)

                if phoneticEngine.isComplete(phoneticBuffer) {
                    phoneticBuffer = ""
                }
            } else {
                textDocumentProxy.insertText(char)
            }
        } else {
            textDocumentProxy.insertText(char)
            phoneticBuffer = ""
        }

        if isShiftActive {
            isShiftActive = false
            setupKeyboard()
        }
    }

    @objc private func spaceTapped() {
        hapticFeedback()
        if isBengaliMode && !phoneticBuffer.isEmpty {
            if let converted = phoneticEngine.convert(phoneticBuffer) {
                for _ in 0..<phoneticBuffer.count {
                    textDocumentProxy.deleteBackward()
                }
                textDocumentProxy.insertText(converted)
                userLearning.recordWord(converted)
            }
            phoneticBuffer = ""
        }
        textDocumentProxy.insertText(" ")
        updateSuggestions(predictionEngine.getCommonSuggestions())
    }

    private func handleBackspace() {
        hapticFeedback()
        if !phoneticBuffer.isEmpty {
            phoneticBuffer.removeLast()
        }
        textDocumentProxy.deleteBackward()
    }

    private func handleReturn() {
        hapticFeedback()
        phoneticBuffer = ""
        textDocumentProxy.insertText("\n")
    }

    private func toggleLanguage() {
        hapticFeedback()
        isBengaliMode.toggle()
        phoneticBuffer = ""
        setupKeyboard()
    }

    private func toggleShift() {
        hapticFeedback()
        isShiftActive.toggle()
        setupKeyboard()
    }

    private func handleToneRewrite() {
        hapticFeedback()
        let text = textDocumentProxy.documentContextBeforeInput ?? ""
        if !text.isEmpty {
            // Trigger ToneRewriter (placeholder for actual view presentation)
            print("Tone Rewrite triggered for: \(text)")
        }
    }

    private func openSettings() {
        hapticFeedback()
        // iOS doesn't allow opening the main app easily from keyboard extension 
        // unless using a custom URL scheme.
        print("Settings triggered")
    }

    @objc private func handleSpaceLongPress(_ gesture: UILongPressGestureRecognizer) {
        if gesture.state == .began {
            toggleLanguage()
        }
    }

    private func toggleSymbols() {
        hapticFeedback()
        currentState = (currentState == .qwerty) ? .symbols : .qwerty
        setupKeyboard()
    }

    private func convertToBengaliNumber(_ input: String) -> String {
        return input
            .replacingOccurrences(of: "0", with: "০")
            .replacingOccurrences(of: "1", with: "১")
            .replacingOccurrences(of: "2", with: "২")
            .replacingOccurrences(of: "3", with: "৩")
            .replacingOccurrences(of: "4", with: "৪")
            .replacingOccurrences(of: "5", with: "৫")
            .replacingOccurrences(of: "6", with: "৬")
            .replacingOccurrences(of: "7", with: "৭")
            .replacingOccurrences(of: "8", with: "৮")
            .replacingOccurrences(of: "9", with: "৯")
    }

    private func hapticFeedback() {
        guard hapticEnabled else { return }
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.impactOccurred()
    }

    private func playClickSound() {
        guard soundEnabled else { return }
        // AudioServicesPlaySystemSound(1104) // Tick sound
    }
}

// MARK: - Callback Button Helper

class CallbackButton: UIButton {
    private var callback: (() -> Void)?

    convenience init(action: @escaping () -> Void) {
        self.init(type: .system)
        self.callback = action
        self.addTarget(self, action: #selector(tapped), for: .touchUpInside)
    }

    @objc private func tapped() { callback?() }
}

// MARK: - Phonetic Engine (Swift)

class PhoneticEngine {

    private var vowels: [String: String] = [:]
    private var consonants: [String: String] = [:]
    private var conjuncts: [String: String] = [:]
    private var commonWords: [String: String] = [:]
    private var allRules: [(String, String)] = [] // sorted longest-first

    init() {
        loadRules()
    }

    private func loadRules() {
        guard let url = Bundle.main.url(forResource: "avro_phonetic_rules", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            loadDefaultRules()
            return
        }

        vowels = json["vowels"] as? [String: String] ?? [:]
        consonants = json["consonants"] as? [String: String] ?? [:]
        conjuncts = json["conjuncts"] as? [String: String] ?? [:]
        commonWords = json["commonWords"] as? [String: String] ?? [:]

        var merged: [String: String] = [:]
        merged.merge(conjuncts) { $1 }
        merged.merge(consonants) { $1 }
        merged.merge(vowels) { $1 }
        allRules = merged.sorted { $0.key.count > $1.key.count }
    }

    private func loadDefaultRules() {
        commonWords = [
            "ami": "আমি", "tumi": "তুমি", "apni": "আপনি", "tui": "তুই",
            "bhalo": "ভালো", "achi": "আছি", "kemon": "কেমন", "ki": "কি",
            "bangladesh": "বাংলাদেশ", "dhaka": "ঢাকা", "na": "না", "ha": "হ্যাঁ",
            "dhonnobad": "ধন্যবাদ", "shagotom": "স্বাগতম", "ekhon": "এখন",
            "khub": "খুব", "shob": "সব", "kaj": "কাজ", "aj": "আজ",
            "kalke": "কাল", "baba": "বাবা", "ma": "মা", "bon": "বোন"
        ]
        consonants = [
            "k": "ক", "kh": "খ", "g": "গ", "gh": "ঘ",
            "c": "চ", "ch": "ছ", "j": "জ", "jh": "ঝ",
            "T": "ট", "Th": "ঠ", "D": "ড", "Dh": "ঢ",
            "t": "ত", "th": "থ", "d": "দ", "dh": "ধ",
            "n": "ন", "p": "প", "ph": "ফ", "f": "ফ",
            "b": "ব", "bh": "ভ", "m": "ম", "z": "য", "r": "র",
            "l": "ল", "sh": "শ", "S": "ষ", "s": "স", "h": "হ",
            "R": "ড়", "Rh": "ঢ়", "y": "য়", "ng": "ং", "H": "ঃ"
        ]
        vowels = [
            "a": "অ", "aa": "আ", "A": "আ", "i": "ই", "I": "ঈ",
            "u": "উ", "U": "ঊ", "e": "এ", "o": "ও", "O": "ও",
            "oi": "ঐ", "ou": "ঔ"
        ]

        var merged: [String: String] = [:]
        merged.merge(consonants) { $1 }
        merged.merge(vowels) { $1 }
        allRules = merged.sorted { $0.key.count > $1.key.count }
    }

    func convert(_ input: String) -> String? {
        let lower = input.lowercased()
        if let word = commonWords[lower] { return word }

        var result = ""
        var i = lower.startIndex
        var matched = false

        while i < lower.endIndex {
            var found = false
            let remaining = lower.distance(from: i, to: lower.endIndex)

            for len in stride(from: min(5, remaining), through: 1, by: -1) {
                let end = lower.index(i, offsetBy: len)
                let substr = String(lower[i..<end])
                if let mapped = allRules.first(where: { $0.0 == substr })?.1 {
                    result += mapped
                    i = end
                    found = true
                    matched = true
                    break
                }
            }
            if !found {
                result += String(lower[i])
                i = lower.index(after: i)
            }
        }

        return matched ? result : nil
    }

    func isComplete(_ buffer: String) -> Bool {
        let lower = buffer.lowercased()
        if commonWords.keys.contains(lower) { return true }
        return !allRules.contains { $0.0.hasPrefix(lower) && $0.0.count > lower.count }
    }
}

// MARK: - Prediction Engine (Swift)

class PredictionEngine {

    private var bigramMap: [String: [String]] = [:]

    init() {
        bigramMap = [
            "আমি": ["ভালো", "যাব", "চাই", "করি", "আছি"],
            "তুমি": ["কেমন", "যাও", "এসো", "করো", "আছো"],
            "আপনি": ["কি", "কেমন", "আসুন", "করুন", "আছেন"],
            "সে": ["যায়", "আসে", "করে", "বলে", "থাকে"],
            "আমরা": ["সবাই", "যাব", "করব", "চাই", "আছি"],
            "ভালো": ["আছি", "লাগছে", "করো", "হয়েছে", "থাকো"],
            "কেমন": ["আছো", "আছেন", "হয়েছে", "লাগছে", "চলছে"],
            "ধন্যবাদ": ["আপনাকে", "ভাই", "অনেক", "সবাইকে", "তোমাকে"]
        ]
    }

    func predict(currentWord: String) -> [String] {
        return bigramMap[currentWord] ?? getCommonSuggestions()
    }

    func getCommonSuggestions() -> [String] {
        return ["আমি", "তুমি", "আপনি", "ভালো", "ধন্যবাদ"]
    }
}

// MARK: - User Learning System (Swift)

class UserLearningSystem {

    private var wordCounts: [String: Int] = [:]
    private let userDefaultsKey = "lorapok_word_counts"

    init() {
        loadData()
    }

    func recordWord(_ word: String) {
        wordCounts[word, default: 0] += 1
        if wordCounts.values.reduce(0, +) % 10 == 0 { saveData() }
    }

    func getTopWords(count: Int = 20) -> [String] {
        return wordCounts.sorted { $0.value > $1.value }
            .prefix(count)
            .map { $0.key }
    }

    private func saveData() {
        UserDefaults.standard.set(wordCounts, forKey: userDefaultsKey)
    }

    private func loadData() {
        if let saved = UserDefaults.standard.dictionary(forKey: userDefaultsKey) as? [String: Int] {
            wordCounts = saved
        }
    }
}

// MARK: - Tone Rewriting Engine (Swift)

enum ToneType: String, CaseIterable {
    case formal, friendly, respectful, concise, detailed, poetic, humorous, empathetic
    
    var displayName: String {
        switch self {
        case .formal: return "🎩 আনুষ্ঠানিক"
        case .friendly: return "😊 বন্ধুত্বপূর্ণ"
        case .respectful: return "🙏 শ্রদ্ধাশীল"
        case .concise: return "⚡ সংক্ষিপ্ত"
        case .detailed: return "📝 বিস্তারিত"
        case .poetic: return "📚 কাব্যিক"
        case .humorous: return "😄 হাস্যরসাত্মক"
        case .empathetic: return "💝 সহানুভূতিশীল"
        }
    }
}

class ToneRewritingEngine {
    func rewrite(text: String, targetTone: ToneType) -> String {
        // Implementation logic (rules-based)
        var result = text
        switch targetTone {
        case .formal:
            result = result.replacingOccurrences(of: "তুমি", with: "আপনি")
            result = result.replacingOccurrences(of: "করো", with: "করুন")
        case .friendly:
            result += " 😊"
        case .respectful:
            result = "শ্রদ্ধেয়, " + result.replacingOccurrences(of: "তুমি", with: "আপনি")
        default: break
        }
        return result
    }
}
