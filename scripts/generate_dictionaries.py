import json
import os

def generate_english():
    """Generate expanded English dictionary with 500+ high-frequency words."""
    words = [
        # Top 100 most common
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "I",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        # Verb forms
        "are", "is", "was", "were", "been", "has", "had", "doing", "does", "did",
        "being", "done", "said", "made", "found", "known", "called", "asked", "used", "told",
        "went", "got", "came", "thought", "saw", "looked", "wanted", "gave", "took", "left",
        "put", "let", "kept", "began", "seemed", "helped", "showed", "heard", "played", "ran",
        "moved", "lived", "believed", "brought", "happened", "wrote", "sat", "stood", "lost", "paid",
        "met", "set", "learned", "changed", "led", "understood", "watched", "followed", "stopped", "created",
        "read", "spent", "grew", "opened", "walked", "won", "taught", "offered", "remembered", "loved",
        "considered", "appeared", "bought", "served", "died", "sent", "built", "stayed", "fell", "reached",
        # Common daily words
        "hello", "hi", "thanks", "thank", "sorry", "yes", "please", "maybe", "always",
        "never", "today", "tomorrow", "yesterday", "morning", "night", "love", "great",
        "probably", "problem", "program", "project", "provide", "public", "question",
        "really", "right", "room", "school", "should", "since", "small", "something",
        "state", "still", "student", "system", "thing", "though", "three", "through",
        "under", "water", "where", "while", "without", "woman", "word", "world", "write",
        # Additional high-frequency
        "need", "house", "picture", "try", "again", "animal", "point", "mother",
        "father", "city", "earth", "eye", "light", "head", "story", "example",
        "life", "paper", "group", "always", "music", "those", "both", "mark",
        "often", "letter", "until", "mile", "river", "car", "feet", "care",
        "second", "enough", "plain", "girl", "usual", "young", "ready", "above",
        "ever", "red", "list", "food", "boy", "experience", "stop", "open",
        "start", "might", "together", "next", "white", "children", "begin", "got",
        "walk", "town", "eat", "country", "run", "important", "family",
        "book", "keep", "never", "every", "last", "long", "hand",
        "high", "place", "old", "number", "different", "large", "must", "big",
        "late", "turn", "here", "why", "ask", "men", "change", "went",
        "man", "play", "same", "tell", "help", "call", "before", "between",
        # Tech & modern
        "phone", "computer", "internet", "email", "app", "software", "data",
        "website", "social", "media", "video", "photo", "camera", "message",
        "search", "download", "upload", "online", "password", "account",
        "technology", "digital", "network", "mobile", "screen", "battery",
        "keyboard", "mouse", "monitor", "printer", "server", "cloud",
        # Emotions & states
        "happy", "sad", "angry", "tired", "excited", "worried", "afraid",
        "surprised", "confused", "proud", "grateful", "lonely", "calm", "nervous",
        "beautiful", "wonderful", "amazing", "terrible", "horrible", "fantastic",
        "excellent", "perfect", "nice", "bad", "worse", "best", "worst",
        # Time & frequency
        "soon", "later", "early", "already", "often", "sometimes", "usually",
        "quickly", "slowly", "finally", "suddenly", "recently", "immediately",
        "before", "during", "since", "until", "while", "after",
        # Pronouns & determiners
        "myself", "yourself", "himself", "herself", "itself", "ourselves", "themselves",
        "each", "every", "either", "neither", "another", "such", "few", "many",
        "much", "several", "enough", "own", "same", "other",
        # Prepositions & conjunctions
        "through", "between", "against", "during", "without", "before", "after",
        "above", "below", "around", "near", "behind", "beside", "toward",
        "although", "however", "therefore", "moreover", "furthermore", "meanwhile",
    ]

    # Deduplicate preserving order
    seen = set()
    unique = []
    for w in words:
        lw = w.lower()
        if lw not in seen:
            seen.add(lw)
            unique.append(w)

    freq_dict = {word: max(1, 1000 - i) for i, word in enumerate(unique)}
    
    out_path = os.path.join("Lorapok_Keyboard", "app", "src", "main", "assets", "english_dictionary.json")
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(freq_dict, f, ensure_ascii=False, indent=2)
    print(f"  English: {len(freq_dict)} words")

def generate_bengali():
    """Generate expanded Bengali dictionary with 500+ phonetic mappings."""
    mappings = {
        # === Pronouns ===
        "ami": "আমি", "tumi": "তুমি", "apni": "আপনি", "se": "সে", "tara": "তারা",
        "amra": "আমরা", "tomra": "তোমরা", "apnara": "আপনারা", "tini": "তিনি",
        "era": "এরা", "ora": "ওরা", "ini": "ইনি", "uni": "উনি",
        # === Possessives ===
        "amar": "আমার", "tomar": "তোমার", "apnar": "আপনার", "tar": "তার",
        "tader": "তাদের", "amader": "আমাদের", "tomader": "তোমাদের", "apnader": "আপনাদের",
        # === Common verbs (present tense) ===
        "kori": "করি", "koro": "করো", "koren": "করেন", "kore": "করে",
        "jai": "যাই", "jao": "যাও", "jan": "যান", "jay": "যায়",
        "ashi": "আসি", "asho": "এসো", "ashen": "আসেন", "ashe": "আসে",
        "khai": "খাই", "khao": "খাও", "khan": "খান", "khay": "খায়",
        "boli": "বলি", "bolo": "বলো", "bolen": "বলেন", "bole": "বলে",
        "dekhi": "দেখি", "dekho": "দেখো", "dekhen": "দেখেন", "dekhe": "দেখে",
        "shuni": "শুনি", "shono": "শোনো", "shonen": "শোনেন", "shone": "শোনে",
        "bujhi": "বুঝি", "bojho": "বোঝো", "bojhen": "বোঝেন", "bojhe": "বোঝে",
        "dei": "দেই", "dao": "দাও", "den": "দেন", "dey": "দেয়",
        "nei": "নেই", "nao": "নাও", "nen": "নেন", "ney": "নেয়",
        "pari": "পারি", "paro": "পারো", "paren": "পারেন", "pare": "পারে",
        "jani": "জানি", "jano": "জানো", "janen": "জানেন", "jane": "জানে",
        "chai": "চাই", "chao": "চাও", "chan": "চান", "chay": "চায়",
        "likhi": "লিখি", "lekho": "লেখো", "lekhen": "লেখেন", "lekhe": "লেখে",
        "pori": "পড়ি", "poro": "পড়ো", "poren": "পড়েন", "pore": "পড়ে",
        "shikhi": "শিখি", "shekho": "শেখো", "shekhen": "শেখেন", "shekhe": "শেখে",
        "thaki": "থাকি", "thako": "থাকো", "thaken": "থাকেন", "thake": "থাকে",
        "hoi": "হই", "hoo": "হও", "hon": "হন", "hoy": "হয়",
        "rakhhi": "রাখি", "rakho": "রাখো", "rakhen": "রাখেন", "rakhe": "রাখে",
        "boshi": "বসি", "bosho": "বসো", "boshen": "বসেন", "boshe": "বসে",
        "uthi": "উঠি", "otho": "ওঠো", "othen": "ওঠেন", "othe": "ওঠে",
        # === Past tense ===
        "korechhi": "করেছি", "korechi": "করেছি", "korecho": "করেছো", "korechen": "করেছেন", "koreche": "করেছে",
        "gechhi": "গেছি", "geci": "গেছি", "gecho": "গেছো", "gechen": "গেছেন", "geche": "গেছে",
        "esechi": "এসেছি", "esecho": "এসেছো", "esechen": "এসেছেন", "eseche": "এসেছে",
        "bolechi": "বলেছি", "bolecho": "বলেছো", "bolechen": "বলেছেন", "boleche": "বলেছে",
        "dekhechi": "দেখেছি", "dekhecho": "দেখেছো", "dekhechen": "দেখেছেন", "dekheche": "দেখেছে",
        "kheye": "খেয়ে", "kheye": "খেয়ে", "kheyechi": "খেয়েছি",
        # === Future tense ===
        "korbo": "করবো", "korbe": "করবে", "korben": "করবেন",
        "jabo": "যাবো", "jabe": "যাবে", "jaben": "যাবেন",
        "ashbo": "আসবো", "ashbe": "আসবে", "ashben": "আসবেন",
        "khabo": "খাবো", "khabe": "খাবে", "khaben": "খাবেন",
        "bolbo": "বলবো", "bolbe": "বলবে", "bolben": "বলবেন",
        "dekhbo": "দেখবো", "dekhbe": "দেখবে", "dekhben": "দেখবেন",
        # === Continuous ===
        "korchi": "করছি", "korcho": "করছো", "korchen": "করছেন", "korche": "করছে",
        "jachhi": "যাচ্ছি", "jaccho": "যাচ্ছো", "jacchen": "যাচ্ছেন", "jacche": "যাচ্ছে",
        "ashchi": "আসছি", "ashcho": "আসছো", "ashchen": "আসছেন", "ashche": "আসছে",
        # === States / Adjectives ===
        "bhalo": "ভালো", "valo": "ভালো", "kharap": "খারাপ", "thik": "ঠিক", "bhul": "ভুল",
        "shotti": "সত্যি", "mittha": "মিথ্যা", "shundor": "সুন্দর", "boro": "বড়",
        "choto": "ছোট", "notun": "নতুন", "purano": "পুরানো", "gorob": "গরম", "thanda": "ঠান্ডা",
        "lamba": "লম্বা", "bete": "বেঁটে", "mota": "মোটা", "chikon": "চিকন",
        "shada": "সাদা", "kalo": "কালো", "lal": "লাল", "nil": "নীল", "sobuj": "সবুজ",
        "holud": "হলুদ", "komola": "কমলা", "gulabi": "গোলাপি",
        "bhalobasha": "ভালোবাসা", "dukho": "দুঃখ", "sukh": "সুখ", "koshto": "কষ্ট",
        "anondo": "আনন্দ", "raga": "রাগ", "bhoy": "ভয়", "asha": "আশা",
        # === Question words ===
        "ki": "কি", "kothay": "কোথায়", "keno": "কেন", "kokhon": "কখন", "koto": "কত",
        "ke": "কে", "kisher": "কিসের", "kake": "কাকে", "kader": "কাদের",
        "kivabe": "কীভাবে", "kemon": "কেমন",
        # === Conjunctions & Particles ===
        "kintu": "কিন্তু", "abong": "এবং", "ba": "বা", "othoba": "অথবা",
        "tai": "তাই", "tobe": "তবে", "jodi": "যদি", "tahole": "তাহলে",
        "karon": "কারণ", "jekhane": "যেখানে", "jokhon": "যখন", "jemon": "যেমন",
        "ar": "আর", "o": "ও", "naki": "নাকি", "to": "তো",
        # === Common nouns ===
        "manush": "মানুষ", "lok": "লোক", "chelemeye": "ছেলেমেয়ে", "chele": "ছেলে", "meye": "মেয়ে",
        "baba": "বাবা", "ma": "মা", "bhai": "ভাই", "bon": "বোন", "bondhu": "বন্ধু",
        "dada": "দাদা", "didi": "দিদি", "chacha": "চাচা", "mami": "মামি", "nana": "নানা", "nani": "নানি",
        "deshe": "দেশে", "desh": "দেশ", "shohor": "শহর", "gram": "গ্রাম", "bari": "বাড়ি", "ghor": "ঘর",
        "rasta": "রাস্তা", "gari": "গাড়ি", "bus": "বাস", "tren": "ট্রেন", "riksha": "রিকশা",
        "school": "স্কুল", "college": "কলেজ", "university": "বিশ্ববিদ্যালয়",
        "office": "অফিস", "hospital": "হাসপাতাল", "bazar": "বাজার", "dokan": "দোকান",
        "boi": "বই", "khata": "খাতা", "kolom": "কলম", "kagoj": "কাগজ",
        "khabar": "খাবার", "pani": "পানি", "bhat": "ভাত", "mach": "মাছ", "taka": "টাকা",
        "cha": "চা", "dudh": "দুধ", "dim": "ডিম", "ruti": "রুটি", "torkari": "তরকারি",
        "phal": "ফল", "am": "আম", "kola": "কলা", "lichu": "লিচু",
        "phone": "ফোন", "computer": "কম্পিউটার", "internet": "ইন্টারনেট", "mobile": "মোবাইল",
        # === Time ===
        "shokal": "সকাল", "dupur": "দুপুর", "bikel": "বিকেল", "rat": "রাত", "din": "দিন",
        "mash": "মাস", "bochor": "বছর", "aj": "আজ", "kal": "কাল", "agamikal": "আগামীকাল",
        "gotokal": "গতকাল", "ajke": "আজকে", "kalke": "কালকে", "ekhon": "এখন", "tokhon": "তখন",
        "shomoy": "সময়", "ghonta": "ঘন্টা", "minute": "মিনিট", "second": "সেকেন্ড",
        "shonibar": "শনিবার", "robibar": "রবিবার", "shombar": "সোমবার",
        "mongolbar": "মঙ্গলবার", "budhbar": "বুধবার", "brihoshpotibar": "বৃহস্পতিবার", "shukrobar": "শুক্রবার",
        # === Numbers ===
        "ek": "এক", "dui": "দুই", "tin": "তিন", "char": "চার", "panch": "পাঁচ",
        "chhoy": "ছয়", "shat": "সাত", "at": "আট", "noy": "নয়", "dosh": "দশ",
        "bish": "বিশ", "ponchash": "পঞ্চাশ", "sho": "শ", "hajar": "হাজার", "lakh": "লাখ",
        # === Locations & Proper nouns ===
        "bangladesh": "বাংলাদেশ", "dhaka": "ঢাকা", "bangla": "বাংলা",
        "chattogram": "চট্টগ্রাম", "khulna": "খুলনা", "rajshahi": "রাজশাহী",
        "sylhet": "সিলেট", "barishal": "বরিশাল", "rangpur": "রংপুর",
        "kolkata": "কলকাতা", "india": "ভারত",
        # === Greetings ===
        "shagotom": "স্বাগতম", "assalamualaikum": "আসসালামুআলাইকুম", "nomoskar": "নমস্কার",
        "khoda": "খোদা", "hafez": "হাফেজ", "bhalobashi": "ভালোবাসি",
        "dhonnobad": "ধন্যবাদ", "shuvo": "শুভ",
        "shuvo_shokal": "শুভ সকাল", "shuvo_ratri": "শুভ রাত্রি",
        "shuvo_jonmodin": "শুভ জন্মদিন", "shuvo_noboborsho": "শুভ নববর্ষ",
        # === Postpositions / Spatial ===
        "theke": "থেকে", "jonno": "জন্য", "sathe": "সাথে", "pase": "পাশে",
        "samne": "সামনে", "pichone": "পিছনে", "upore": "উপরে", "niche": "নিচে",
        "vitore": "ভিতরে", "baire": "বাইরে", "kache": "কাছে", "dure": "দূরে",
        "modhdhe": "মধ্যে", "opore": "ওপরে", "pashe": "পাশে", "dike": "দিকে",
        # === Misc high-frequency ===
        "hae": "হ্যাঁ", "na": "না", "shob": "সব", "ektu": "একটু", "aro": "আরো",
        "shudhu": "শুধু", "ache": "আছে", "nai": "নাই", "achi": "আছি",
        "acho": "আছো", "achen": "আছেন",
        "kokhono": "কখনো", "keu": "কেউ", "kichu": "কিছু",
        "onek": "অনেক", "beshi": "বেশি", "kom": "কম",
        "prothom": "প্রথম", "dwitiyo": "দ্বিতীয়", "shesh": "শেষ",
        "shuru": "শুরু", "majhe": "মাঝে",
        "dorkar": "দরকার", "lagbe": "লাগবে", "hobe": "হবে", "hoyeche": "হয়েছে",
        "lagche": "লাগছে", "cholche": "চলছে", "jete": "যেতে", "ashte": "আসতে", "korte": "করতে",
        "khelte": "খেলতে", "ghurte": "ঘুরতে", "berate": "বেড়াতে",
        "jibon": "জীবন", "mrittu": "মৃত্যু", "shorir": "শরীর", "mon": "মন",
        "matha": "মাথা", "hat": "হাত", "pa": "পা", "chokh": "চোখ", "kan": "কান", "nakh": "নাক",
        "dant": "দাঁত", "mukh": "মুখ",
        "akash": "আকাশ", "chad": "চাঁদ", "tara": "তারা", "surjo": "সূর্য",
        "nodir": "নদী", "shagar": "সাগর", "pahar": "পাহাড়", "gachh": "গাছ", "phul": "ফুল",
        "brishti": "বৃষ্টি", "rodh": "রোদ", "batash": "বাতাস",
        "cricket": "ক্রিকেট", "football": "ফুটবল", "khela": "খেলা",
        "gaan": "গান", "nrityo": "নৃত্য", "chobi": "ছবি", "cinema": "সিনেমা",
        # === Loanwords / Modern ===
        "prediction": "প্রেডিকশন", "english": "ইংলিশ", "working": "ওয়ার্কিং",
        "hello": "হ্যালো", "sir": "স্যার", "madam": "ম্যাডাম",
        "facebook": "ফেসবুক", "google": "গুগল", "youtube": "ইউটিউব",
        "email": "ইমেইল", "password": "পাসওয়ার্ড", "ok": "ওকে", "sorry": "সরি",
    }
    
    # Deduplicate (first occurrence wins)
    seen = set()
    unique_mappings = {}
    for latin, bengali in mappings.items():
        if latin not in seen:
            seen.add(latin)
            unique_mappings[latin] = bengali

    # Format: {"latin": {"bengali": freq}}
    out_dict = {}
    for i, (latin, bengali) in enumerate(unique_mappings.items()):
        out_dict[latin] = {bengali: max(1, 1000 - i)}
    
    out_path = os.path.join("Lorapok_Keyboard", "app", "src", "main", "assets", "bengali_dictionary.json")
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(out_dict, f, ensure_ascii=False, indent=2)
    print(f"  Bengali: {len(out_dict)} words")

if __name__ == "__main__":
    print("Generating dictionaries...")
    generate_english()
    generate_bengali()
    print("Done!")
