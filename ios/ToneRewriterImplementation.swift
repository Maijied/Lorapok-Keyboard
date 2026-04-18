import UIKit
import SwiftUI

// MARK: - Lorapok Keuboard — iOS Tone Rewriter Implementation
// 8 Bengali tone transformations with rule-based engine
// Min iOS: 13.0 | Memory: <48 MB

// MARK: - Tone Type

enum ToneType: String, CaseIterable {
    case formal = "formal"
    case friendly = "friendly"
    case respectful = "respectful"
    case concise = "concise"
    case detailed = "detailed"
    case poetic = "poetic"
    case humorous = "humorous"
    case empathetic = "empathetic"

    var bengaliName: String {
        switch self {
        case .formal: return "আনুষ্ঠানিক"
        case .friendly: return "বন্ধুত্বপূর্ণ"
        case .respectful: return "শ্রদ্ধাশীল"
        case .concise: return "সংক্ষিপ্ত"
        case .detailed: return "বিস্তারিত"
        case .poetic: return "কাব্যিক"
        case .humorous: return "হাস্যরসাত্মক"
        case .empathetic: return "সহানুভূতিশীল"
        }
    }

    var emoji: String {
        switch self {
        case .formal: return "🎩"
        case .friendly: return "😊"
        case .respectful: return "🙏"
        case .concise: return "⚡"
        case .detailed: return "📝"
        case .poetic: return "📚"
        case .humorous: return "😄"
        case .empathetic: return "💝"
        }
    }

    var isPremium: Bool {
        return [.poetic, .humorous, .empathetic].contains(self)
    }

    var displayName: String { "\(emoji) \(bengaliName)" }
}

// MARK: - Tone Result

struct ToneResult {
    let originalText: String
    let rewrittenText: String
    let tone: ToneType
    let confidence: Float
    let processingTimeMs: Int64
}

// MARK: - Bengali Tone Rules

struct BengaliToneRules {

    static let pronounUpgrade: [String: String] = [
        "তুই": "তুমি", "তুমি": "আপনি", "তোর": "তোমার",
        "তোমার": "আপনার", "তোকে": "তোমাকে", "তোমাকে": "আপনাকে",
        "তোরা": "তোমরা", "তোমরা": "আপনারা"
    ]

    static let pronounDowngrade: [String: String] = [
        "আপনি": "তুমি", "আপনার": "তোমার", "আপনাকে": "তোমাকে",
        "আপনারা": "তোমরা", "তুমি": "তুই", "তোমার": "তোর"
    ]

    static let verbFormal: [String: String] = [
        "আসবে": "উপস্থিত থাকবেন", "যাবে": "যাবেন",
        "করবে": "করবেন", "বলবে": "বলবেন",
        "পারবে": "পারবেন", "আসো": "আসুন",
        "যাও": "যান", "করো": "করুন",
        "আসবি": "আসবেন", "পারবি": "পারবেন"
    ]

    static let verbCasual: [String: String] = [
        "উপস্থিত থাকবেন": "আসবে", "যাবেন": "যাবে",
        "করবেন": "করবে", "বলবেন": "বলবে",
        "পারবেন": "পারবে", "আসুন": "আসো",
        "যান": "যাও", "করুন": "করো"
    ]

    static let wordFormal: [String: String] = [
        "কাল": "আগামীকাল", "অফিস": "কার্যালয়",
        "মিটিং": "সভা", "হ্যাঁ": "জি"
    ]

    static let wordCasual: [String: String] = [
        "আগামীকাল": "কাল", "কার্যালয়": "অফিস",
        "সভা": "মিটিং", "জি": "হ্যাঁ"
    ]

    static let fillerWords: [String] = [
        "আমি মনে করি", "সম্ভবত", "হয়তো", "আসলে", "মূলত"
    ]

    static func applyReplacements(_ text: String, rules: [String: String]) -> String {
        var result = text
        for (from, to) in rules {
            result = result.replacingOccurrences(of: from, with: to)
        }
        return result
    }

    static func removeFillers(_ text: String) -> String {
        var result = text
        for filler in fillerWords {
            result = result.replacingOccurrences(of: filler, with: "")
        }
        // Collapse whitespace
        result = result.components(separatedBy: .whitespaces)
            .filter { !$0.isEmpty }.joined(separator: " ")
        return result
    }
}

// MARK: - Tone Rewriting Engine

class ToneRewritingEngine {

    func rewrite(text: String, targetTone: ToneType) -> ToneResult {
        let start = DispatchTime.now()

        let rewritten: String
        switch targetTone {
        case .formal:
            rewritten = toFormal(text)
        case .friendly:
            rewritten = toFriendly(text)
        case .respectful:
            rewritten = toRespectful(text)
        case .concise:
            rewritten = toConcise(text)
        case .detailed:
            rewritten = toDetailed(text)
        case .poetic:
            rewritten = toPoetic(text)
        case .humorous:
            rewritten = toHumorous(text)
        case .empathetic:
            rewritten = toEmpathetic(text)
        }

        let end = DispatchTime.now()
        let elapsed = Int64(end.uptimeNanoseconds - start.uptimeNanoseconds) / 1_000_000

        return ToneResult(
            originalText: text, rewrittenText: rewritten,
            tone: targetTone, confidence: 0.85, processingTimeMs: elapsed
        )
    }

    private func toFormal(_ text: String) -> String {
        var r = BengaliToneRules.applyReplacements(text, rules: BengaliToneRules.pronounUpgrade)
        r = BengaliToneRules.applyReplacements(r, rules: BengaliToneRules.verbFormal)
        r = BengaliToneRules.applyReplacements(r, rules: BengaliToneRules.wordFormal)
        return r
    }

    private func toFriendly(_ text: String) -> String {
        var r = BengaliToneRules.applyReplacements(text, rules: BengaliToneRules.pronounDowngrade)
        r = BengaliToneRules.applyReplacements(r, rules: BengaliToneRules.verbCasual)
        r = BengaliToneRules.applyReplacements(r, rules: BengaliToneRules.wordCasual)
        return r.hasSuffix("😊") ? r : r + " 😊"
    }

    private func toRespectful(_ text: String) -> String {
        var r = BengaliToneRules.applyReplacements(text, rules: BengaliToneRules.pronounUpgrade)
        r = BengaliToneRules.applyReplacements(r, rules: BengaliToneRules.verbFormal)
        r = r.replacingOccurrences(of: "আসতে", with: "পধারতে")
        if !r.hasPrefix("অনুগ্রহ করে") { r = "অনুগ্রহ করে " + r }
        return r
    }

    private func toConcise(_ text: String) -> String {
        var r = BengaliToneRules.removeFillers(text)
        r = r.replacingOccurrences(of: "উপস্থিত থাকবেন", with: "আসবেন")
        return r
    }

    private func toDetailed(_ text: String) -> String {
        if text.count < 50 {
            return text + "। বিস্তারিত বলতে গেলে, এটি একটি গুরুত্বপূর্ণ বিষয়"
        }
        return text
    }

    private func toPoetic(_ text: String) -> String {
        var r = text
        r = r.replacingOccurrences(of: "সুন্দর", with: "রূপের মাধুরী")
        r = r.replacingOccurrences(of: "আবহাওয়া", with: "প্রকৃতি")
        r = r.replacingOccurrences(of: "ভালো", with: "মনোরম")
        r = r.replacingOccurrences(of: "খুব", with: "অতীব")
        return r
    }

    private func toHumorous(_ text: String) -> String {
        return text.hasSuffix("😄") ? text : text + " 😄"
    }

    private func toEmpathetic(_ text: String) -> String {
        var r = text
        if !r.hasPrefix("আমি বুঝতে পারছি") { r = "আমি বুঝতে পারছি। " + r }
        return r.hasSuffix("💝") ? r : r + " 💝"
    }
}

// MARK: - SwiftUI Tone Rewriter View

@available(iOS 14.0, *)
struct ToneRewriterView: View {
    let originalText: String
    let onAccept: (String) -> Void
    let onDismiss: () -> Void

    @State private var selectedTone: ToneType? = nil
    @State private var result: ToneResult? = nil

    private let engine = ToneRewritingEngine()

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            // Title
            Text("✨ টোন পরিবর্তন করুন")
                .font(.title2).bold()

            // Original
            Text("মূল: \(originalText)")
                .font(.subheadline).foregroundColor(.secondary)

            // Tone chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(ToneType.allCases, id: \.rawValue) { tone in
                        Button(action: {
                            selectedTone = tone
                            result = engine.rewrite(text: originalText, targetTone: tone)
                        }) {
                            Text(tone.displayName)
                                .font(.caption)
                                .padding(.horizontal, 12).padding(.vertical, 6)
                                .background(selectedTone == tone ? Color.blue.opacity(0.2) : Color.gray.opacity(0.1))
                                .cornerRadius(16)
                        }
                        .disabled(tone.isPremium)
                        .opacity(tone.isPremium ? 0.5 : 1.0)
                    }
                }
            }

            // Preview
            if let r = result {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(r.tone.emoji) পরিবর্তিত:")
                        .font(.caption).foregroundColor(.blue)
                    Text(r.rewrittenText)
                        .font(.body).bold()
                    Text("⏱️ \(r.processingTimeMs)ms")
                        .font(.caption2).foregroundColor(.secondary)
                }
                .padding(12)
                .background(Color.blue.opacity(0.05))
                .cornerRadius(8)
            } else {
                Text("☝️ উপরে একটি টোন নির্বাচন করুন")
                    .foregroundColor(.secondary).padding(12)
            }

            // Buttons
            HStack {
                Spacer()
                Button("বাতিল") { onDismiss() }
                    .foregroundColor(.secondary)
                Button("ব্যবহার করুন") {
                    if let r = result { onAccept(r.rewrittenText) }
                }
                .padding(.horizontal, 16).padding(.vertical, 8)
                .background(Color.blue).foregroundColor(.white)
                .cornerRadius(8)
            }
        }
        .padding(16)
    }
}

// MARK: - UIKit ViewController Wrapper

class ToneRewriterViewController: UIViewController {

    var originalText: String = ""
    var onAccept: ((String) -> Void)?

    private let engine = ToneRewritingEngine()
    private var currentResult: ToneResult?

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        setupUI()
    }

    private func setupUI() {
        let titleLabel = UILabel()
        titleLabel.text = "✨ টোন পরিবর্তন করুন"
        titleLabel.font = .boldSystemFont(ofSize: 20)
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(titleLabel)

        NSLayoutConstraint.activate([
            titleLabel.topAnchor.constraint(equalTo: view.safeAreaLayoutGuide.topAnchor, constant: 16),
            titleLabel.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 16)
        ])
    }
}
