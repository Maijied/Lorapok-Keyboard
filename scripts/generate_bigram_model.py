#!/usr/bin/env python3
import json
import os
import re
import sqlite3
from collections import Counter, defaultdict

def generate_bigram_model(corpus_path, output_path, min_freq=2, top_k=5, format='json'):
    print(f"Generating bigram model from {corpus_path}...")
    
    bigrams = defaultdict(Counter)
    unigrams = Counter()
    
    with open(corpus_path, 'r', encoding='utf-8') as f:
        for line in f:
            words = line.strip().split()
            if not words:
                continue
            
            unigrams.update(words)
            for i in range(len(words) - 1):
                w1, w2 = words[i], words[i+1]
                bigrams[w1][w2] += 1
                
    if format == 'json':
        # Filter and format
        model = {}
        for w1, successors in bigrams.items():
            top_successors = [w for w, count in successors.most_common(top_k) if count >= min_freq]
            if top_successors:
                model[w1] = top_successors
        
        common_words = [w for w, count in unigrams.most_common(20)]
        model["_common"] = common_words
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(model, f, ensure_ascii=False, indent=2)
    
    elif format == 'sqlite':
        if os.path.exists(output_path):
            os.remove(output_path)
            
        conn = sqlite3.connect(output_path)
        cursor = conn.cursor()
        cursor.execute("CREATE TABLE bigrams (w1 TEXT, w2 TEXT, freq INTEGER)")
        cursor.execute("CREATE INDEX idx_w1 ON bigrams(w1)")
        
        data = []
        for w1, successors in bigrams.items():
            for w2, freq in successors.most_common(top_k):
                if freq >= min_freq:
                    data.append((w1, w2, freq))
        
        cursor.executemany("INSERT INTO bigrams VALUES (?, ?, ?)", data)
        
        # Add common words
        cursor.execute("CREATE TABLE common (word TEXT, freq INTEGER)")
        common_data = [(w, freq) for w, freq in unigrams.most_common(50)]
        cursor.executemany("INSERT INTO common VALUES (?, ?)", common_data)
        
        conn.commit()
        conn.close()
        
    print(f"Model saved to {output_path}")
    if format == 'sqlite':
        print(f"Total bigram entries: {len(data)}")

if __name__ == "__main__":
    corpus = "./data/corpus/merged_corpus.txt"
    # Generate both for transition
    generate_bigram_model(corpus, "./Lorapok_Keyboard/app/src/main/assets/bigram_model.json", format='json')
    generate_bigram_model(corpus, "./Lorapok_Keyboard/app/src/main/assets/bigram_model.db", format='sqlite')
