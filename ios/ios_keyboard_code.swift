import UIKit

// MARK: - Lorapok Keuboard — iOS Keyboard Extension
// KeyboardViewController: Main UIInputViewController for Bengali AI keyboard
// Features: Phonetic conversion, AI prediction, language switching, tone rewriting
// Min iOS: 13.0 | Memory: <48 MB

class KeyboardViewController: UIInputViewController {

    // MARK: - Properties
    private var phoneticEngine: PhoneticEngine!
    private var predictionEngine: PredictionEngine!
    private var userLearning: UserLearningSystem!

    private var isBengaliMode = true
    private var isShiftActive = false
    private var phoneticBuffer = ""
    private var suggestionButtons: [UIButton] = []

    private let keyboardBackground = UIColor(red: 0.957, green: 0.957, blue: 0.957, alpha: 1.0) // #F4F4F4
    private let keyColor = UIColor.white
    private let specialKeyColor = UIColor(red: 0.373, green: 0.388, blue: 0.404, alpha: 1.0) // #5F6368
    private let accentColor = UIColor(red: 0.102, green: 0.451, blue: 0.910, alpha: 1.0) // #1A73E8
    private let textColor = UIColor(red: 0.122, green: 0.122, blue: 0.122, alpha: 1.0) // #1F1F1F

    private let qwertyRows: [[String]] = [
        ["q","w","e","r","t","y","u","i","o","p"],
        ["a","s","d","f","g","h","j","k","l"],
        ["z","x","c","v","b","n","m"]
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

        // QWERTY rows
        for (index, row) in qwertyRows.enumerated() {
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

        let numBtn = createSpecialKey(title: "123") { /* TODO: number layout */ }
        numBtn.widthAnchor.constraint(equalToConstant: 44).isActive = true
        stack.addArrangedSubview(numBtn)

        let langBtn = createSpecialKey(title: "🌐") { [weak self] in self?.toggleLanguage() }
        langBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(langBtn)

        let spaceBtn = UIButton(type: .system)
        spaceBtn.setTitle(isBengaliMode ? "বাংলা" : "English", for: .normal)
        spaceBtn.titleLabel?.font = UIFont.systemFont(ofSize: 14)
        spaceBtn.setTitleColor(textColor, for: .normal)
        spaceBtn.backgroundColor = keyColor
        spaceBtn.layer.cornerRadius = 6
        spaceBtn.addTarget(self, action: #selector(spaceTapped), for: .touchUpInside)
        stack.addArrangedSubview(spaceBtn)

        let emojiBtn = createSpecialKey(title: "😊") { /* TODO: emoji */ }
        emojiBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(emojiBtn)

        let toneBtn = createSpecialKey(title: "✨") { /* TODO: tone rewriter */ }
        toneBtn.widthAnchor.constraint(equalToConstant: 40).isActive = true
        stack.addArrangedSubview(toneBtn)

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
        guard let title = sender.title(for: .normal) else { return }
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

        if isShiftActive { isShiftActive = false }
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

    private func hapticFeedback() {
        let generator = UIImpactFeedbackGenerator(style: .light)
        generator.impactOccurred()
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
        // Minimal defaults if JSON not found
        commonWords = [
            "ami": "আমি", "tumi": "তুমি", "apni": "আপনি",
            "bhalo": "ভালো", "achi": "আছি", "kemon": "কেমন",
            "bangladesh": "বাংলাদেশ", "dhaka": "ঢাকা",
            "dhonnobad": "ধন্যবাদ", "shagotom": "স্বাগতম"
        ]
        consonants = [
            "k": "ক", "kh": "খ", "g": "গ", "gh": "ঘ",
            "c": "চ", "ch": "ছ", "j": "জ", "jh": "ঝ",
            "t": "ত", "th": "থ", "d": "দ", "dh": "ধ",
            "n": "ন", "p": "প", "ph": "ফ", "f": "ফ",
            "b": "ব", "bh": "ভ", "m": "ম", "r": "র",
            "l": "ল", "sh": "শ", "s": "স", "h": "হ",
            "ng": "ং", "y": "য়"
        ]
        vowels = [
            "a": "অ", "aa": "আ", "i": "ই", "ii": "ঈ",
            "u": "উ", "uu": "ঊ", "e": "এ", "o": "ও",
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
