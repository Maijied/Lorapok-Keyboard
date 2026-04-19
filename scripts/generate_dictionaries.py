import json
import os

def generate_english():
    # Top English words
    words = [
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
        "are", "is", "was", "were", "been", "has", "had", "doing", "does", "did",
        "hello", "hi", "thanks", "thank", "sorry", "yes", "please", "maybe", "always",
        "never", "today", "tomorrow", "yesterday", "morning", "night", "love", "great",
        "probably", "problem", "program", "project", "provide", "public", "question",
        "really", "right", "room", "school", "should", "since", "small", "something",
        "state", "still", "student", "system", "thing", "though", "three", "through",
        "under", "water", "where", "while", "without", "woman", "word", "world", "write"
    ]
    
    # Assign frequency based on index
    freq_dict = {word: max(1, 1000 - i) for i, word in enumerate(words)}
    
    out_path = os.path.join("Lorapok_Keyboard", "app", "src", "main", "assets", "english_dictionary.json")
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(freq_dict, f, ensure_ascii=False, indent=2)

def generate_bengali():
    # Bengali phonetic mappings (latin -> bengali -> frequency)
    # This allows direct lookup of typed prefixes
    mappings = {
        "ami": "আমি", "tumi": "তুমি", "apni": "আপনি", "se": "সে", "tara": "তারা",
        "amra": "আমরা", "tomra": "তোমরা", "apnara": "আপনারা", "bhalo": "ভালো",
        "kemon": "কেমন", "achi": "আছি", "acho": "আছো", "achen": "আছেন", "hae": "হ্যাঁ",
        "na": "না", "ki": "কি", "kothay": "কোথায়", "keno": "কেন", "kokhon": "কখন",
        "koto": "কত", "ke": "কে", "shob": "সব", "ektu": "একটু", "aro": "আরো",
        "shudhu": "শুধু", "kintu": "কিন্তু", "abong": "এবং", "ba": "বা", "theke": "থেকে",
        "jonno": "জন্য", "kore": "করে", "hoy": "হয়", "ache": "আছে", "nai": "নাই",
        "pari": "পারি", "jani": "জানি", "chai": "চাই", "boli": "বলি", "jai": "যাই",
        "ashi": "আসি", "khai": "খাই", "dei": "দেই", "nei": "নেই", "shikhi": "শিখি",
        "likhi": "লিখি", "pori": "পড়ি", "dekhi": "দেখি", "shuni": "শুনি", "bujhi": "বুঝি",
        "dhonnobad": "ধন্যবাদ", "shuvo": "শুভ", "shokal": "সকাল", "dupur": "দুপুর",
        "bikel": "বিকেল", "rat": "রাত", "din": "দিন", "mash": "মাস", "bochor": "বছর",
        "aj": "আজ", "kal": "কাল", "agamikal": "আগামীকাল", "gotokal": "গতকাল",
        "bari": "বাড়ি", "school": "স্কুল", "office": "অফিস", "bazar": "বাজার",
        "rasta": "রাস্তা", "gari": "গাড়ি", "manush": "মানুষ", "boi": "বই",
        "khabar": "খাবার", "pani": "পানি", "bhat": "ভাত", "mach": "মাছ", "taka": "টাকা",
        "phone": "ফোন", "computer": "কম্পিউটার", "internet": "ইন্টারনেট",
        "bangladesh": "বাংলাদেশ", "dhaka": "ঢাকা", "bangla": "বাংলা",
        "shagotom": "স্বাগতম", "assalamualaikum": "আসসালামুআলাইকুম", "nomoskar": "নমস্কার",
        "khoda": "খোদা", "hafez": "হাফেজ", "bhalobashi": "ভালোবাসি", "shundor": "সুন্দর",
        "boro": "বড়", "choto": "ছোট", "notun": "নতুন", "purano": "পুরানো",
        "valo": "ভালো", "kharap": "খারাপ", "thik": "ঠিক", "bhul": "ভুল",
        "shotti": "সত্যি", "mittha": "মিথ্যা", "amar": "আমার", "tomar": "তোমার",
        "apnar": "আপনার", "tar": "তার", "tader": "তাদের", "amader": "আমাদের",
        "ajke": "আজকে", "kalke": "কালকে", "ekhon": "এখন", "tokhon": "তখন",
        "jokhon": "যখন", "kokhono": "কখনো", "keu": "কেউ", "kichu": "কিছু",
        "onek": "অনেক", "beshi": "বেশি", "kom": "কম", "sathe": "সাথে", "pase": "পাশে",
        "samne": "সামনে", "pichone": "পিছনে", "upore": "উপরে", "niche": "নিচে",
        "vitore": "ভিতরে", "baire": "বাইরে", "kache": "কাছে", "dure": "দূরে",
        "prediction": "প্রেডিকশন", "ai": "এআই", "english": "ইংলিশ", "working": "ওয়ার্কিং",
        "hello": "হ্যালো", "sir": "স্যার", "how": "হাউ", "what": "হোয়াট"
    }
    
    # Format: {"latin": {"bengali": freq}}
    out_dict = {}
    for i, (latin, bengali) in enumerate(mappings.items()):
        out_dict[latin] = {bengali: max(1, 1000 - i)}
    
    out_path = os.path.join("Lorapok_Keyboard", "app", "src", "main", "assets", "bengali_dictionary.json")
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(out_dict, f, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    generate_english()
    generate_bengali()
    print("Dictionaries generated successfully.")
