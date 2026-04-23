#!/usr/bin/env python3
import json
import os
import re
from collections import Counter

# Basic mapping for generating Latin keys for Bengali words
BN_TO_LATIN = {
    'অ': 'o', 'আ': 'a', 'ই': 'i', 'ঈ': 'i', 'উ': 'u', 'ঊ': 'u', 'ঋ': 'ri',
    'এ': 'e', 'ঐ': 'oi', 'ও': 'o', 'ঔ': 'ou',
    'ক': 'k', 'খ': 'kh', 'গ': 'g', 'ঘ': 'gh', 'ঙ': 'ng',
    'চ': 'c', 'ছ': 'ch', 'জ': 'j', 'ঝ': 'jh', 'ঞ': 'ng',
    'ট': 't', 'ঠ': 'th', 'ড': 'd', 'ঢ': 'dh', 'ণ': 'n',
    'ত': 't', 'থ': 'th', 'দ': 'd', 'ধ': 'dh', 'ন': 'n',
    'প': 'p', 'ফ': 'f', 'ব': 'b', 'ভ': 'bh', 'ম': 'm',
    'য': 'z', 'র': 'r', 'ল': 'l', 'শ': 'sh', 'ষ': 'sh', 'স': 's', 'হ': 'h',
    'ড়': 'r', 'ঢ়': 'rh', 'য়': 'y', 'ৎ': 't', 'ং': 'ng', 'ঃ': 'h', 'ঁ': '',
    'া': 'a', 'ি': 'i', 'ী': 'i', 'ু': 'u', 'ূ': 'u', 'ৃ': 'ri', 'ে': 'e', 'ৈ': 'oi', 'ো': 'o', 'ৌ': 'ou', '্': ''
}

# Add more common phonetic mappings
BN_TO_LATIN.update({
    'য': 'z', 'জ': 'j', 'জ': 'z', # Common variations
})

def generate_latin_key(bn_word):
    # Simplified transliteration for indexing
    key = ""
    for char in bn_word:
        key += BN_TO_LATIN.get(char, "")
    return key.lower()

def expand_dictionary(corpus_path, output_path, limit=50000):
    print(f"Expanding dictionary from {corpus_path}...")
    
    with open(corpus_path, 'r', encoding='utf-8') as f:
        words = re.findall(r'[\u0980-\u09FF]+', f.read())
    
    counts = Counter(words)
    print(f"Total unique Bengali words found: {len(counts)}")
    
    dictionary = {}
    
    # Process top words
    for bn_word, freq in counts.most_common(limit):
        if len(bn_word) < 2: continue
        
        latin_key = generate_latin_key(bn_word)
        if latin_key not in dictionary:
            dictionary[latin_key] = {}
        
        dictionary[latin_key][bn_word] = freq
        
    # Add common English/Latin transliterations manually for accuracy
    manual_fixes = {
        "bangla": {"বাংলা": 10000},
        "bangladesh": {"বাংলাদেশ": 10000},
        "ami": {"আমি": 10000},
        "tumi": {"তুমি": 10000},
        "apni": {"আপনি": 10000},
        "dhaka": {"ঢাকা": 10000},
        "bhalo": {"ভালো": 10000},
        "nam": {"নাম": 10000},
        "kalke": {"কালকে": 10000},
        "dekha": {"দেখা": 10000},
        "hobe": {"হবে": 10000}
    }
    
    for key, val in manual_fixes.items():
        if key not in dictionary:
            dictionary[key] = val
        else:
            dictionary[key].update(val)

    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(dictionary, f, ensure_ascii=False, indent=2)
        
    print(f"Expanded dictionary saved to {output_path} ({len(dictionary)} keys)")

if __name__ == "__main__":
    corpus = "./data/corpus/merged_corpus.txt"
    output = "./Lorapok_Keyboard/app/src/main/assets/bengali_dictionary.json"
    expand_dictionary(corpus, output)
