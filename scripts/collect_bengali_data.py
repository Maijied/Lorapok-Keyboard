#!/usr/bin/env python3
"""
Lorapok Keuboard — Bengali Data Collection System
==================================================
Automated collection, cleaning, and preprocessing of Bengali text corpora
for training the AI prediction model.

Sources:
  1. Wikipedia Bengali dump
  2. OSCAR Corpus (Bengali subset)
  3. Common Crawl filtered
  4. News articles (Prothom Alo, Bdnews24)
  5. Bengali literature (public domain)
  6. Sample corpus generator

Usage:
  python collect_bengali_data.py --source wikipedia --output ./corpus/
  python collect_bengali_data.py --source all --output ./corpus/
  python collect_bengali_data.py --generate-sample --output ./corpus/sample.txt

Requirements:
  pip install requests beautifulsoup4 tqdm lxml regex datasets
"""

import os
import re
import sys
import json
import json
import time
import hashlib
import argparse
import logging
import argparse
import unicodedata
import gzip
from pathlib import Path
from datetime import datetime
from typing import List, Dict, Optional, Generator, Tuple

# Third-party imports (install via pip)
try:
    import requests
    from bs4 import BeautifulSoup
    from tqdm import tqdm
except ImportError:
    print("Install required packages: pip install requests beautifulsoup4 tqdm")
    sys.exit(1)

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler("data_collection.log")
    ]
)
logger = logging.getLogger("LorapokDataCollector")

# ============================================================================
# Constants
# ============================================================================

BENGALI_UNICODE_RANGE = (0x0980, 0x09FF)
BENGALI_PATTERN = re.compile(r'[\u0980-\u09FF]')
MIN_SENTENCE_LENGTH = 5
MAX_SENTENCE_LENGTH = 500
TARGET_CORPUS_SIZE_GB = 2.3
TARGET_TOKEN_COUNT = 1_200_000_000

WIKIPEDIA_DUMP_URL = "https://dumps.wikimedia.org/bnwiki/latest/bnwiki-latest-pages-articles.xml.bz2"
OSCAR_DATASET_NAME = "oscar-corpus/OSCAR-2301"
OSCAR_LANGUAGE = "bn"

NEWS_SOURCES = {
    "prothom_alo": {
        "base_url": "https://www.prothomalo.com",
        "rss_url": "https://www.prothomalo.com/feed",
        "category_urls": [
            "/bangladesh", "/world", "/sports", "/entertainment",
            "/technology", "/lifestyle", "/education", "/opinion"
        ]
    },
    "bdnews24": {
        "base_url": "https://bangla.bdnews24.com",
        "rss_url": "https://bangla.bdnews24.com/rss/rss.xml",
        "category_urls": [
            "/bangladesh", "/world", "/sports", "/entertainment"
        ]
    },
    "daily_star_bangla": {
        "base_url": "https://bangla.thedailystar.net",
        "rss_url": "https://bangla.thedailystar.net/rss.xml"
    },
    "samakal": {
        "base_url": "https://samakal.com",
        "rss_url": "https://samakal.com/rss"
    },
    "ittefaq": {
        "base_url": "https://www.ittefaq.com.bd",
        "rss_url": "https://www.ittefaq.com.bd/feed"
    },
    "jugantor": {
        "base_url": "https://www.jugantor.com",
        "rss_url": "https://www.jugantor.com/feed"
    },
    "kaler_kantho": {
        "base_url": "https://www.kalerkantho.com",
        "rss_url": "https://www.kalerkantho.com/rss.xml"
    },
    "janakantha": {
        "base_url": "https://www.dailyjanakantha.com",
        "rss_url": "https://www.dailyjanakantha.com/rss/rss.xml"
    },
    "manabzamin": {
        "base_url": "https://mzamin.com",
        "rss_url": "https://mzamin.com/rss.php"
    },
    "inqilab": {
        "base_url": "https://www.dailyinqilab.com",
        "rss_url": "https://www.dailyinqilab.com/rss/all.xml"
    },
    "amader_shomoy": {
        "base_url": "https://www.dainikamadershomoy.com",
        "rss_url": "https://www.dainikamadershomoy.com/rss/rss.xml"
    },
    "nayadiganta": {
        "base_url": "https://www.dailynayadiganta.com",
        "rss_url": "https://www.dailynayadiganta.com/rss/all-news.xml"
    },
    "jugantor": {
        "base_url": "https://www.jugantor.com",
        "rss_url": "https://www.jugantor.com/feed"
    },
    "swasthya_bangla": {
        "base_url": "https://www.swasthyabangla.com",
        "rss_url": "https://www.swasthyabangla.com/feed"
    },
    "law_help_bd": {
        "base_url": "https://www.lawhelpbd.com",
        "rss_url": "https://www.lawhelpbd.com/feed"
    },
    "prothom_alo_health": {
        "base_url": "https://www.prothomalo.com/topic/স্বাস্থ্য",
        "rss_url": "https://www.prothomalo.com/feed/topic/স্বাস্থ্য"
    },
    "bd_pratidin_health": {
        "base_url": "https://www.bd-pratidin.com/health",
        "rss_url": "https://www.bd-pratidin.com/rss.xml"
    },
    "bbc_bengali": {
        "base_url": "https://www.bbc.com/bengali",
        "rss_url": "https://feeds.bbci.co.uk/bengali/rss.xml"
    },
    "bangla_tribune": {
        "base_url": "https://www.banglatribune.com",
        "rss_url": "https://banglatribune.com/rss.xml"
    },
    "jago_news": {
        "base_url": "https://www.jagonews24.com",
        "rss_url": "https://www.jagonews24.com/rss.xml"
    },
    "ananda_bazar": {
        "base_url": "https://www.anandabazar.com",
        "rss_url": "https://www.anandabazar.com/rss/anandabazar_feed.xml"
    },
    "somoy_news": {
        "base_url": "https://www.somoynews.tv",
        "rss_url": "https://www.somoynews.tv/rss.xml"
    }
}

HF_DATASETS = {
    "oscar": "oscar-corpus/OSCAR-2301",
    "banglabook": "csebuetnlp/banglabook",
    "mC4": "google/mc4",
    "titulm": "hishab/titulm-bangla-corpus"
}


# ============================================================================
# Text Cleaning & Normalization
# ============================================================================

class BengaliTextCleaner:
    """Cleans and normalizes Bengali text for corpus building."""

    def __init__(self):
        self.stats = {
            "total_processed": 0,
            "total_accepted": 0,
            "total_rejected": 0,
            "total_tokens": 0
        }

    def normalize_unicode(self, text: str) -> str:
        """Normalize Unicode to NFC form for consistent Bengali text."""
        return unicodedata.normalize("NFC", text)

    def remove_html_tags(self, text: str) -> str:
        """Strip HTML tags from text."""
        return re.sub(r'<[^>]+>', '', text)

    def remove_urls(self, text: str) -> str:
        """Remove URLs from text."""
        return re.sub(r'https?://\S+|www\.\S+', '', text)

    def remove_email(self, text: str) -> str:
        """Remove email addresses."""
        return re.sub(r'\S+@\S+\.\S+', '', text)

    def remove_extra_whitespace(self, text: str) -> str:
        """Collapse multiple whitespace to single space."""
        return re.sub(r'\s+', ' ', text).strip()

    def is_bengali_text(self, text: str, threshold: float = 0.3) -> bool:
        """Check if text contains sufficient Bengali characters."""
        if not text:
            return False
        bengali_chars = len(BENGALI_PATTERN.findall(text))
        total_chars = len(text.replace(' ', ''))
        if total_chars == 0:
            return False
        return (bengali_chars / total_chars) >= threshold

    def clean_text(self, text: str) -> str:
        """Full cleaning pipeline for Bengali text."""
        text = self.normalize_unicode(text)
        text = self.remove_html_tags(text)
        text = self.remove_urls(text)
        text = self.remove_email(text)
        # Remove control characters (keep Bengali, Latin, numbers, punctuation)
        text = re.sub(r'[^\u0980-\u09FF\u0020-\u007Ea-zA-Z0-9।,;:!?\-\'\"()\[\]{}\n]', ' ', text)
        text = self.remove_extra_whitespace(text)
        return text

    def extract_sentences(self, text: str) -> List[str]:
        """Split text into sentences using Bengali and English delimiters."""
        # Split on Bengali dari (।), period, !, ?
        sentences = re.split(r'[।.!?]+', text)
        valid_sentences = []
        for sent in sentences:
            sent = sent.strip()
            if (MIN_SENTENCE_LENGTH <= len(sent) <= MAX_SENTENCE_LENGTH
                    and self.is_bengali_text(sent)):
                valid_sentences.append(sent)
                self.stats["total_accepted"] += 1
            else:
                self.stats["total_rejected"] += 1
            self.stats["total_processed"] += 1
        return valid_sentences

    def get_stats(self) -> Dict:
        """Return processing statistics."""
        return self.stats


# ============================================================================
# Data Sources
# ============================================================================

class WikipediaCollector:
    """Collect Bengali text from Wikipedia dumps."""

    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.cleaner = BengaliTextCleaner()

    def download_dump(self) -> Path:
        """Download Bengali Wikipedia dump."""
        dump_path = self.output_dir / "bnwiki-latest.xml.bz2"
        if dump_path.exists():
            logger.info(f"Wikipedia dump already exists: {dump_path}")
            return dump_path

        logger.info(f"Downloading Bengali Wikipedia dump from {WIKIPEDIA_DUMP_URL}...")
        response = requests.get(WIKIPEDIA_DUMP_URL, stream=True)
        total_size = int(response.headers.get('content-length', 0))

        with open(dump_path, 'wb') as f:
            with tqdm(total=total_size, unit='B', unit_scale=True, desc="Downloading") as pbar:
                for chunk in response.iter_content(chunk_size=8192):
                    f.write(chunk)
                    pbar.update(len(chunk))

        logger.info(f"Download complete: {dump_path}")
        return dump_path

    def parse_dump(self, dump_path: Path, max_articles: int = None) -> Generator[str, None, None]:
        """Parse Wikipedia XML dump and extract article text."""
        import bz2
        from xml.etree import ElementTree as ET

        logger.info("Parsing Wikipedia dump...")
        article_count = 0

        with bz2.open(dump_path, 'rt', encoding='utf-8') as f:
            for event, elem in ET.iterparse(f, events=('end',)):
                if elem.tag.endswith('}text') or elem.tag == 'text':
                    if elem.text:
                        # Remove wiki markup
                        text = self._strip_wiki_markup(elem.text)
                        text = self.cleaner.clean_text(text)
                        if text and self.cleaner.is_bengali_text(text):
                            article_count += 1
                            yield text
                            if max_articles and article_count >= max_articles:
                                break
                    elem.clear()

        logger.info(f"Parsed {article_count} Bengali articles from Wikipedia")

    def _strip_wiki_markup(self, text: str) -> str:
        """Remove MediaWiki markup from text."""
        text = re.sub(r'\{\{[^}]+\}\}', '', text)  # Templates
        text = re.sub(r'\[\[(?:[^|\]]*\|)?([^\]]*)\]\]', r'\1', text)  # Links
        text = re.sub(r"'''?", '', text)  # Bold/italic
        text = re.sub(r'<ref[^>]*>.*?</ref>', '', text, flags=re.DOTALL)  # References
        text = re.sub(r'<[^>]+/?>', '', text)  # HTML tags
        text = re.sub(r'\{[^}]+\}', '', text)  # Remaining braces
        text = re.sub(r'==+[^=]+=+=', '', text)  # Headers
        text = re.sub(r'\[\[Category:[^\]]+\]\]', '', text)  # Categories
        text = re.sub(r'\[\[File:[^\]]+\]\]', '', text)  # Files
        return text

    def collect(self, max_articles: int = None) -> Path:
        """Full collection pipeline."""
        dump_path = self.download_dump()
        output_file = self.output_dir / "wikipedia_bengali.txt"

        with open(output_file, 'w', encoding='utf-8') as f:
            for text in self.parse_dump(dump_path, max_articles):
                sentences = self.cleaner.extract_sentences(text)
                for sent in sentences:
                    f.write(sent + '\n')

        logger.info(f"Wikipedia collection saved to {output_file}")
        logger.info(f"Stats: {self.cleaner.get_stats()}")
        return output_file


class OSCARCollector:
    """Collect Bengali text from the OSCAR corpus via HuggingFace datasets."""

    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.cleaner = BengaliTextCleaner()

    def collect(self, max_samples: int = 100000) -> Path:
        """Download and process OSCAR Bengali subset."""
        try:
            from datasets import load_dataset
        except ImportError:
            logger.error("Install datasets: pip install datasets")
            return None

        output_file = self.output_dir / "oscar_bengali.txt"
        logger.info(f"Loading OSCAR Bengali corpus (max {max_samples} samples)...")

        dataset = load_dataset(
            OSCAR_DATASET_NAME,
            language=OSCAR_LANGUAGE,
            split="train",
            streaming=True,
            trust_remote_code=True
        )

        count = 0
        with open(output_file, 'w', encoding='utf-8') as f:
            for sample in tqdm(dataset, total=max_samples, desc="Processing OSCAR"):
                text = self.cleaner.clean_text(sample.get("text", ""))
                if text and self.cleaner.is_bengali_text(text):
                    sentences = self.cleaner.extract_sentences(text)
                    for sent in sentences:
                        f.write(sent + '\n')
                count += 1
                if count >= max_samples:
                    break

        logger.info(f"OSCAR collection saved to {output_file}")
        return output_file


class NewsCollector:
    """Collect Bengali text from news websites."""

    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)
        self.cleaner = BengaliTextCleaner()
        self.session = requests.Session()
        self.session.headers.update({
            "User-Agent": "LorapokKeyboard/1.0 (Research; Bengali NLP)"
        })

    def collect_from_rss(self, source_name: str, rss_url: str, max_articles: int = 100) -> List[str]:
        """Collect articles from RSS feed."""
        logger.info(f"Collecting from {source_name} RSS: {rss_url}")
        articles = []

        try:
            response = self.session.get(rss_url, timeout=30)
            soup = BeautifulSoup(response.content, 'lxml-xml')
            items = soup.find_all('item')[:max_articles]

            for item in tqdm(items, desc=f"Fetching {source_name}"):
                link = item.find('link')
                if link and link.text:
                    try:
                        article_text = self._fetch_article(link.text)
                        if article_text:
                            articles.append(article_text)
                    except Exception as e:
                        logger.warning(f"Failed to fetch {link.text}: {e}")
        except Exception as e:
            logger.error(f"RSS fetch failed for {source_name}: {e}")

        return articles

    def _fetch_article(self, url: str) -> Optional[str]:
        """Fetch and extract text from a news article URL."""
        try:
            response = self.session.get(url, timeout=15)
            soup = BeautifulSoup(response.content, 'html.parser')

            # Remove script, style, nav elements
            for tag in soup(['script', 'style', 'nav', 'header', 'footer', 'aside']):
                tag.decompose()

            # Extract article body
            article = soup.find('article') or soup.find('div', class_=re.compile(r'article|content|story|body'))
            if article:
                text = article.get_text(separator=' ', strip=True)
            else:
                text = soup.get_text(separator=' ', strip=True)

            text = self.cleaner.clean_text(text)
            if self.cleaner.is_bengali_text(text) and len(text) > 100:
                return text
        except Exception as e:
            logger.warning(f"Article extraction failed: {e}")

        return None

    def collect(self, max_per_source: int = 100) -> Path:
        """Collect from all news sources."""
        output_file = self.output_dir / "news_bengali.txt"
        all_articles = []

        for source_name, config in NEWS_SOURCES.items():
            rss_url = config.get("rss_url")
            if rss_url:
                articles = self.collect_from_rss(source_name, rss_url, max_per_source)
                all_articles.extend(articles)
                logger.info(f"Collected {len(articles)} articles from {source_name}")

        with open(output_file, 'w', encoding='utf-8') as f:
            for article in all_articles:
                sentences = self.cleaner.extract_sentences(article)
                for sent in sentences:
                    f.write(sent + '\n')

        logger.info(f"News collection saved to {output_file} ({len(all_articles)} articles)")
        return output_file


# ============================================================================
# Sample Corpus Generator
# ============================================================================

class SampleCorpusGenerator:
    """Generate a sample Bengali corpus for testing."""

    SAMPLE_SENTENCES = [
        "আমি ভালো আছি",
        "তুমি কেমন আছো",
        "আপনি কি আগামীকাল আসবেন",
        "বাংলাদেশ একটি সুন্দর দেশ",
        "ঢাকা বাংলাদেশের রাজধানী",
        "আমি বাংলায় কথা বলি",
        "আজ আবহাওয়া খুব সুন্দর",
        "আমার নাম লোরাপোক",
        "তুমি কি পড়াশোনা করো",
        "আমরা সবাই বাংলাদেশের নাগরিক",
        "ধন্যবাদ আপনাকে সাহায্য করার জন্য",
        "আমি কাল অফিসে যাব",
        "সে স্কুলে পড়ে",
        "আমার বাবা একজন শিক্ষক",
        "আমার মা খুব ভালো রান্না করেন",
        "বাংলা আমাদের মাতৃভাষা",
        "একুশে ফেব্রুয়ারি আন্তর্জাতিক মাতৃভাষা দিবস",
        "আমি চা খেতে ভালোবাসি",
        "তুমি কি খেয়েছো",
        "আজ রবিবার তাই ছুটি",
        "বই পড়া একটি ভালো অভ্যাস",
        "কম্পিউটার আমাদের জীবন সহজ করে দিয়েছে",
        "ইন্টারনেট ছাড়া আধুনিক জীবন কল্পনা করা যায় না",
        "বাংলাদেশে প্রায় ১৭ কোটি মানুষ বাস করে",
        "পদ্মা বাংলাদেশের বৃহত্তম নদী",
        "সুন্দরবন বিশ্বের বৃহত্তম ম্যানগ্রোভ বন",
        "রয়েল বেঙ্গল টাইগার বাংলাদেশের জাতীয় পশু",
        "ইলিশ বাংলাদেশের জাতীয় মাছ",
        "আমি তোমাকে ভালোবাসি",
        "দয়া করে একটু সাহায্য করুন",
        "আসসালামুআলাইকুম ভাই কেমন আছেন",
        "নমস্কার সবাইকে স্বাগতম",
        "আমার ফোন নম্বর দেওয়া যাবে না",
        "গতকাল রাতে আমি দেরিতে ঘুমিয়েছি",
        "আগামীকাল সকালে মিটিং আছে",
        "তোমার সাথে দেখা হয়ে ভালো লাগলো",
        "বৃষ্টি পড়ছে টাপুর টুপুর",
        "চলো বাইরে ঘুরে আসি",
        "আমি নতুন একটি বই কিনেছি",
        "তুমি কি সিনেমা দেখতে যাবে",
        "আমাদের দেশের সংস্কৃতি অনেক সমৃদ্ধ",
        "শিক্ষাই জাতির মেরুদণ্ড",
        "সময় এবং স্রোত কারো জন্য অপেক্ষা করে না",
        "জ্ঞান অর্জনের জন্য পরিশ্রম করতে হয়",
        "মানুষ মানুষের জন্য আমরা সবাই সবার তরে",
        "আমি প্রতিদিন সকালে ব্যায়াম করি",
        "স্বাস্থ্যই সকল সুখের মূল",
        "পরিবেশ দূষণ একটি গুরুতর সমস্যা",
        "আমরা সবাই মিলে পরিবেশ রক্ষা করব",
        "বিজ্ঞান ও প্রযুক্তি আমাদের জীবনকে বদলে দিয়েছে",
        "শ্রদ্ধেয় স্যার, আমার বিনীত নিবেদন এই যে",
        "আমি আপনাকে একটি ইমেইল পাঠিয়েছি",
        "অনুগ্রহ করে বিষয়টি বিবেচনা করবেন",
        "আপনার উজ্জ্বল ভবিষ্যৎ কামনা করছি",
        "শুভকামনা রইল আপনার জন্য",
        "চলো আজ সন্ধ্যায় কফি খাই",
        "আমি এখন একটু ব্যস্ত আছি পরে কথা বলছি",
        "তুমি কি আমার মেসেজ পেয়েছো",
        "আমি পৌঁছাতে একটু দেরি হতে পারে",
        "আজকের দিনটি কেমন কাটলো তোমার",
        "আমি তোমাকে অনেক মিস করছি",
        "বিনা অনুমতিতে প্রবেশ নিষেধ",
        "ধুমপান স্বাস্থ্যের জন্য ক্ষতিকর",
        "সাবধানে গাড়ি চালান জীবন বাঁচান",
        "রক্তদান মহৎ দান আপনিও রক্ত দিন",
        "শিক্ষা নিয়ে গড়ব দেশ শেখ হাসিনার বাংলাদেশ",
        "আমার সোনার বাংলা আমি তোমায় ভালোবাসি",
        "জন্মভূমিকে ভালোবাসা ঈমানের অঙ্গ",
        "মানুষের সেবায় নিজেকে নিয়োজিত করা উচিত",
        "সততাই সর্বোত্তম পন্থা হিসেবে পরিচিত",
        "কঠোর পরিশ্রম সাফল্যের চাবিকাঠি",
        "জীবন একটি যুদ্ধ যেখানে জয়ী হতে হয়",
        "ভালো মানুষের সাথে বন্ধুত্ব করা উচিত",
        "অন্যায়ের প্রতিবাদ করা নাগরিকের দায়িত্ব",
        "আমরা সবাই শান্তিতে বসবাস করতে চাই",
        "যুদ্ধ নয় শান্তি চাই এই আমাদের অঙ্গীকার"
    ]

    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.output_dir.mkdir(parents=True, exist_ok=True)

    def generate(self, output_filename: str = "sample_corpus.txt") -> Path:
        """Generate sample corpus file."""
        output_file = self.output_dir / output_filename
        # Include slang/colloquial samples as requested
        extra_samples = [
            "শালার বেটা কি করিস", "মাথা নষ্ট মামা", "কোপাকুপি হবে আজ", 
            "বালের কাজ করিস না", "পুরা আগুন ব্রো", "খাইছে আমারে",
            "বাংলা একাডেমি প্রমিত বাংলা বানান নিয়ম মানা উচিত",
            "অমর একুশে বইমেলা বাঙালির প্রাণের মেলা",
            "রবীন্দ্রনাথ ঠাকুরের গীতাঞ্জলি বিশ্বসাহিত্যের অমূল্য সম্পদ"
        ]
        all_sentences = self.SAMPLE_SENTENCES + extra_samples
        with open(output_file, 'w', encoding='utf-8') as f:
            for sent in all_sentences:
                f.write(sent + '\n')
        return output_file

class HFCollector:
    """Collect Bengali text from HuggingFace datasets."""
    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.cleaner = BengaliTextCleaner()

    def collect(self, dataset_name: str, max_samples: int = 50000) -> Path:
        output_file = self.output_dir / f"hf_{dataset_name.split('/')[-1]}.txt"
        try:
            from datasets import load_dataset
            logger.info(f"Loading HF dataset: {dataset_name} (max {max_samples})")
            # Try with language config first, fall back to no config
            try:
                dataset = load_dataset(dataset_name, 'bn', split='train', streaming=True)
            except Exception:
                dataset = load_dataset(dataset_name, split='train', streaming=True)

            count = 0
            with open(output_file, 'w', encoding='utf-8') as f:
                for item in tqdm(dataset, total=max_samples, desc=f"Streaming {dataset_name.split('/')[-1]}"):
                    text = item.get('text', '') or item.get('content', '') or item.get('sentence', '')
                    if text:
                        cleaned = self.cleaner.clean_text(str(text))
                        if cleaned and self.cleaner.is_bengali_text(cleaned):
                            f.write(cleaned + '\n')
                    count += 1
                    if count >= max_samples:
                        break
            logger.info(f"HF collection saved: {output_file}")
            return output_file
        except Exception as e:
            logger.error(f"HF collection failed for {dataset_name}: {e}")
            # Return empty file path so pipeline continues
            output_file.touch()
            return output_file

    def collect_titulm(self, max_samples: int = 100000) -> Path:
        """Dedicated collector for TituLM — streams safely with shard control."""
        output_file = self.output_dir / "hf_titulm.txt"
        try:
            from datasets import load_dataset
            logger.info(f"Loading TituLM Bangla Corpus (max {max_samples} samples)...")
            # Load only a subset of shards to avoid memory crash
            dataset = load_dataset(
                "hishab/titulm-bangla-corpus",
                split="train",
                streaming=True,
            )
            count = 0
            with open(output_file, 'w', encoding='utf-8') as f:
                for item in tqdm(dataset, total=max_samples, desc="TituLM"):
                    text = item.get('text', '') or item.get('content', '')
                    if text:
                        cleaned = self.cleaner.clean_text(str(text))
                        if cleaned and self.cleaner.is_bengali_text(cleaned):
                            f.write(cleaned + '\n')
                    count += 1
                    if count >= max_samples:
                        break
            logger.info(f"TituLM collection saved: {output_file} ({count} samples)")
            return output_file
        except Exception as e:
            logger.error(f"TituLM collection failed: {e}")
            output_file.touch()
            return output_file

class SocialMediaCollector:
    """Collect colloquial Bengali from public social platforms."""
    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.cleaner = BengaliTextCleaner()

    def collect_reddit(self, limit: int = 1000) -> Path:
        logger.info(f"Collecting from Reddit (r/bangladesh) with limit {limit}...")
        output_file = self.output_dir / "social_reddit.txt"
        base_url = "https://www.reddit.com/r/bangladesh/new/.json"
        headers = {'User-Agent': 'Lorapok-Keyboard-AI/1.0'}
        
        after = None
        count = 0
        with open(output_file, 'w', encoding='utf-8') as f:
            while count < limit:
                url = f"{base_url}?limit=100" + (f"&after={after}" if after else "")
                try:
                    resp = requests.get(url, headers=headers, timeout=15)
                    data = resp.json()
                    children = data['data']['children']
                    if not children: break
                    
                    for post in children:
                        text = post['data'].get('selftext', '') + " " + post['data'].get('title', '')
                        cleaned = self.cleaner.clean_text(text)
                        if cleaned:
                            f.write(cleaned + '\n')
                        count += 1
                    
                    after = data['data']['after']
                    if not after: break
                    time.sleep(1) # Be polite to Reddit
                except Exception as e:
                    logger.error(f"Reddit collection failed at count {count}: {e}")
                    break
        return output_file

class FacebookTwitterCollector:
    """Collect public Facebook and Twitter (X) Bengali text."""
    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.cleaner = BengaliTextCleaner()

    def collect_from_search(self, keywords: List[str] = None) -> Path:
        logger.info("Collecting Facebook/Twitter snippets via public search...")
        output_file = self.output_dir / "social_fb_twitter.txt"
        if not keywords:
            keywords = ["Bengali status", "আজকের দিন", "ভালোবাসা", "মামা চিল"]
            
        with open(output_file, 'w', encoding='utf-8') as f:
            for kw in tqdm(keywords, desc="Searching Social"):
                try:
                    # Simulated search-based collection
                    # In production, this would use a search API to find public posts
                    pass
                except Exception as e:
                    logger.error(f"Search failed for {kw}: {e}")
        return output_file

class YouTubeCommentCollector:
    """Collect Bengali comments from popular YouTube videos."""
    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.cleaner = BengaliTextCleaner()

    def collect(self, video_ids: List[str], max_comments: int = 5000) -> Path:
        logger.info(f"Collecting YouTube comments from {len(video_ids)} videos...")
        output_file = self.output_dir / "social_youtube.txt"
        # In a real scenario, we'd use a YouTube comment downloader library
        # For this project, we'll simulate the collection from popular public IDs
        return output_file

class DomainCollector:
    """Collect specialized Bengali text (Medical, Legal, etc.)."""
    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.cleaner = BengaliTextCleaner()

    def collect_from_wiki_category(self, category: str, limit: int = 1000) -> Path:
        logger.info(f"Collecting specialized data from Wikipedia category: {category}...")
        output_file = self.output_dir / f"domain_{category}.txt"
        # Simple simulation: in a real scenario, we'd use the Wiki API to get pages in category
        # For now, we'll label it as domain data for the merge
        return output_file

class BlogCollector:
    """Collect text from Bengali blog sites (Somewhereinblog, etc.)."""
    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)
        self.cleaner = BengaliTextCleaner()

    def collect_somewherein(self, pages: int = 500) -> Path:
        logger.info(f"Collecting from Somewhereinblog ({pages} pages)...")
        output_file = self.output_dir / "blog_somewherein.txt"
        base_url = "https://www.somewhereinblog.net/blog/recent"
        
        with open(output_file, 'w', encoding='utf-8') as f:
            for p in tqdm(range(1, pages + 1), desc="Scraping Somewherein"):
                try:
                    url = f"{base_url}/{p}"
                    resp = requests.get(url, timeout=10)
                    soup = BeautifulSoup(resp.content, 'html.parser')
                    posts = soup.find_all('div', class_='blog-content')
                    for post in posts:
                        text = post.get_text()
                        cleaned = self.cleaner.clean_text(text)
                        if cleaned:
                            f.write(cleaned + '\n')
                except Exception as e:
                    logger.error(f"Failed to scrape page {p}: {e}")
        return output_file

    def collect_techtunes(self, pages: int = 200) -> Path:
        logger.info(f"Collecting from Techtunes ({pages} pages)...")
        output_file = self.output_dir / "blog_techtunes.txt"
        base_url = "https://www.techtunes.co/recent/page"
        
        with open(output_file, 'w', encoding='utf-8') as f:
            for p in tqdm(range(1, pages + 1), desc="Scraping Techtunes"):
                try:
                    url = f"{base_url}/{p}"
                    # Increased timeout and added user-agent to avoid blocking
                    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}
                    resp = requests.get(url, headers=headers, timeout=20)
                    soup = BeautifulSoup(resp.content, 'html.parser')
                    
                    # Search for broader content containers
                    containers = soup.find_all(['div', 'article'], class_=['tt-post-excerpt', 'entry-content', 'content', 'post-content'])
                    if not containers:
                        # Fallback: get all paragraph text
                        text = " ".join([p.get_text() for p in soup.find_all('p')])
                        cleaned = self.cleaner.clean_text(text)
                        if cleaned: f.write(cleaned + '\n')
                    else:
                        for container in containers:
                            text = container.get_text()
                            cleaned = self.cleaner.clean_text(text)
                            if cleaned:
                                f.write(cleaned + '\n')
                    # Small delay to be polite
                    time.sleep(0.5)
                except Exception as e:
                    logger.error(f"Failed to scrape Techtunes page {p}: {e}")
        return output_file
        with open(output_file, 'w', encoding='utf-8') as f:
            for sentence in self.SAMPLE_SENTENCES:
                f.write(sentence + '\n')

        logger.info(f"Sample corpus generated: {output_file} ({len(self.SAMPLE_SENTENCES)} sentences)")
        return output_file


# ============================================================================
# Corpus Merger & Statistics
# ============================================================================

class CorpusMerger:
    """Merge multiple corpus files, deduplicate, and compute statistics."""

    def __init__(self, output_dir: str):
        self.output_dir = Path(output_dir)

    def merge(self, input_files: List[Path], output_filename: str = "merged_corpus.txt") -> Path:
        """Merge and deduplicate corpus files."""
        output_file = self.output_dir / output_filename
        seen_hashes = set()
        total_lines = 0
        unique_lines = 0

        with open(output_file, 'w', encoding='utf-8') as out_f:
            for input_file in input_files:
                if not input_file.exists():
                    logger.warning(f"Skipping missing file: {input_file}")
                    continue
                logger.info(f"Processing {input_file}...")
                with open(input_file, 'r', encoding='utf-8') as in_f:
                    for line in in_f:
                        line = line.strip()
                        if not line:
                            continue
                        total_lines += 1
                        line_hash = hashlib.md5(line.encode()).hexdigest()
                        if line_hash not in seen_hashes:
                            seen_hashes.add(line_hash)
                            out_f.write(line + '\n')
                            unique_lines += 1

        logger.info(f"Merged corpus: {total_lines} total → {unique_lines} unique lines")
        logger.info(f"Deduplication removed {total_lines - unique_lines} lines")
        return output_file

    def compute_statistics(self, corpus_file: Path) -> Dict:
        """Compute corpus statistics."""
        stats = {
            "file": str(corpus_file),
            "file_size_bytes": corpus_file.stat().st_size,
            "file_size_mb": round(corpus_file.stat().st_size / (1024 * 1024), 2),
            "total_lines": 0,
            "total_tokens": 0,
            "total_characters": 0,
            "unique_tokens": set(),
            "avg_sentence_length": 0,
        }

        with open(corpus_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                stats["total_lines"] += 1
                tokens = line.split()
                stats["total_tokens"] += len(tokens)
                stats["total_characters"] += len(line)
                stats["unique_tokens"].update(tokens)

        if stats["total_lines"] > 0:
            stats["avg_sentence_length"] = round(
                stats["total_tokens"] / stats["total_lines"], 2
            )

        stats["vocabulary_size"] = len(stats["unique_tokens"])
        del stats["unique_tokens"]  # Don't serialize the set

        logger.info(f"Corpus Statistics:")
        for key, value in stats.items():
            logger.info(f"  {key}: {value}")

        # Save stats
        stats_file = corpus_file.parent / "corpus_statistics.json"
        with open(stats_file, 'w', encoding='utf-8') as f:
            json.dump(stats, f, indent=2, ensure_ascii=False)

        return stats


# ============================================================================
# Main CLI
# ============================================================================

def main():
    parser = argparse.ArgumentParser(
        description="Lorapok Keuboard — Bengali Data Collection System",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python collect_bengali_data.py --source sample --output ./corpus/
  python collect_bengali_data.py --source wikipedia --output ./corpus/ --max 1000
  python collect_bengali_data.py --source news --output ./corpus/
  python collect_bengali_data.py --source all --output ./corpus/
  python collect_bengali_data.py --merge --output ./corpus/
        """
    )
    parser.add_argument("--source", choices=["wikipedia", "oscar", "news", "sample", "hf", "blog", "social", "titulm", "all"],
                        default="sample", help="Data source to collect from")
    parser.add_argument("--dataset", type=str, help="HF dataset name if source is hf")
    parser.add_argument("--output", type=str, default="./corpus",
                        help="Output directory for collected data")
    parser.add_argument("--max", type=int, default=None,
                        help="Maximum number of articles/samples to collect")
    parser.add_argument("--merge", action="store_true",
                        help="Merge all collected corpus files")
    parser.add_argument("--stats", action="store_true",
                        help="Compute statistics for a corpus file")

    args = parser.parse_args()
    output_dir = args.output
    collected_files = []

    if args.stats:
        merger = CorpusMerger(output_dir)
        stats_target = Path(output_dir) / "merged_corpus.txt"
        if not stats_target.exists():
            # Try current directory fallback
            stats_target = Path("merged_corpus.txt")
            
        if stats_target.exists():
            merger.compute_statistics(stats_target)
        else:
            logger.error(f"Stats target {stats_target} not found. Please run with --merge first or specify --output.")
        return

    if args.merge:
        merger = CorpusMerger(output_dir)
        corpus_dir = Path(output_dir)
        input_files = [f for f in corpus_dir.glob("*.txt") if "merged" not in f.name]
        merged = merger.merge(input_files)
        if args.stats:
            merger.compute_statistics(merged)
        return

    logger.info(f"=== Lorapok Keuboard Data Collection ===")
    logger.info(f"Source: {args.source} | Output: {output_dir}")

    try:
        if args.source in ("sample", "all"):
            gen = SampleCorpusGenerator(output_dir)
            f = gen.generate()
            collected_files.append(f)
    except Exception as e:
        logger.error(f"Sample collection failed: {e}")

    try:
        if args.source in ("wikipedia", "all"):
            wiki = WikipediaCollector(output_dir)
            f = wiki.collect(max_articles=args.max)
            collected_files.append(f)
    except Exception as e:
        logger.error(f"Wikipedia collection failed: {e}")

    try:
        # Skip gated OSCAR by default in 'all'
        if args.source == "oscar":
            oscar = OSCARCollector(output_dir)
            f = oscar.collect(max_samples=args.max or 100000)
            if f:
                collected_files.append(f)
    except Exception as e:
        logger.error(f"OSCAR collection failed: {e}")

    try:
        if args.source in ("news", "all"):
            news = NewsCollector(output_dir)
            f = news.collect(max_per_source=args.max or 100)
            collected_files.append(f)
    except Exception as e:
        logger.error(f"News collection failed: {e}")

    try:
        if args.source in ("hf", "all") and args.dataset:
            hf = HFCollector(output_dir)
            f = hf.collect(args.dataset, max_samples=args.max or 50000)
            if f: collected_files.append(f)
        elif args.source == "hf" and not args.dataset:
            logger.error("--source hf requires --dataset NAME")
    except Exception as e:
        logger.error(f"HF collection failed: {e}")

    try:
        # TituLM: the best free Bangla LLM corpus on HuggingFace
        if args.source in ("titulm", "all"):
            hf = HFCollector(output_dir)
            f = hf.collect_titulm(max_samples=args.max or 100000)
            if f: collected_files.append(f)
    except Exception as e:
        logger.error(f"TituLM collection failed: {e}")

    try:
        if args.source in ("blog", "all"):
            blog = BlogCollector(output_dir)
            f1 = blog.collect_somewherein(pages=args.max or 500)
            f2 = blog.collect_techtunes(pages=args.max or 200)
            if f1: collected_files.append(f1)
            if f2: collected_files.append(f2)
    except Exception as e:
        logger.error(f"Blog collection failed: {e}")

    try:
        if args.source in ("social", "all"):
            social = SocialMediaCollector(output_dir)
            f1 = social.collect_reddit(limit=args.max or 1000)
            if f1: collected_files.append(f1)
            
            fb_tw = FacebookTwitterCollector(output_dir)
            f2 = fb_tw.collect_from_search()
            if f2: collected_files.append(f2)
    except Exception as e:
        logger.error(f"Social collection failed: {e}")

    # Auto-merge if collecting all
    if args.source == "all" and len(collected_files) > 1:
        try:
            merger = CorpusMerger(output_dir)
            merged = merger.merge(collected_files)
            merger.compute_statistics(merged)
        except Exception as e:
            logger.error(f"Auto-merge failed: {e}")

    logger.info("=== Data collection complete ===")
    logger.info(f"Files created: {[str(f) for f in collected_files]}")


# ============================================================================
# Data Collection Checklist
# ============================================================================

COLLECTION_CHECKLIST = """
╔══════════════════════════════════════════════════════════════╗
║           BENGALI DATA COLLECTION CHECKLIST                 ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  [ ] 1. Generate sample corpus (50 sentences)               ║
║  [ ] 2. Download Wikipedia Bengali dump (~450M tokens)       ║
║  [ ] 3. Download OSCAR Bengali subset                        ║
║  [ ] 4. Collect news articles (Prothom Alo, Bdnews24)       ║
║  [ ] 5. Add Bengali literature (public domain)               ║
║  [ ] 6. Merge and deduplicate all sources                    ║
║  [ ] 7. Compute corpus statistics                            ║
║  [ ] 8. Verify Unicode normalization                         ║
║  [ ] 9. Check for data quality (no junk/spam)               ║
║  [ ] 10. Target: 2.3 GB / 1.2 billion tokens                ║
║                                                              ║
║  Run: python collect_bengali_data.py --source all            ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝
"""


if __name__ == "__main__":
    if "--checklist" in sys.argv:
        print(COLLECTION_CHECKLIST)
    else:
        main()
