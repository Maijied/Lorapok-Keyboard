import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useParams, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Keyboard, 
  Cpu, 
  Database, 
  BrainCircuit, 
  Settings, 
  BookOpen, 
  ChevronRight, 
  Menu, 
  X,
  ExternalLink,
  Code,
  Zap,
  Activity,
  Layers,
  ShieldCheck
} from 'lucide-react';

// --- Layout Components ---

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const navLinks = [
    { name: 'System Architecture', path: '/docs/architecture', icon: Cpu },
    { name: 'Data Pipeline', path: '/docs/data-collection', icon: Database },
    { name: 'ML Intelligence', path: '/docs/ml-process', icon: BrainCircuit },
    { name: 'Development', path: '/docs/development', icon: Code },
  ];

  return (
    <nav className={`fixed top-0 left-0 w-full z-50 transition-all duration-300 ${scrolled ? 'glass py-3 mx-0 shadow-2xl' : 'py-6 bg-transparent'}`}>
      <div className="container flex items-center justify-between">
        <Link to="/" className="flex items-center gap-3 group !no-underline !text-white">
          <div className="w-11 h-11 bg-primary rounded-xl flex items-center justify-center group-hover:rotate-12 transition-transform shadow-[0_0_20px_var(--primary-glow)]">
            <Keyboard className="text-black" size={24} />
          </div>
          <div className="flex flex-col">
            <span className="text-xl font-bold tracking-tight !no-underline">LORAPOK</span>
            <span className="text-[10px] font-bold text-primary tracking-[0.3em] -mt-1">INTELLIGENCE</span>
          </div>
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-12">
          {navLinks.map((link) => (
            <Link 
              key={link.path} 
              to={link.path}
              className={`nav-link !no-underline ${location.pathname === link.path ? 'active' : ''}`}
            >
              {link.name}
            </Link>
          ))}
          <a href="https://github.com/Maijied/Lorapok-Keyboard" target="_blank" className="btn-primary py-2.5 px-6 text-xs">
            <ExternalLink size={16} /> REPOSITORY
          </a>
        </div>

        {/* Mobile Toggle */}
        <button className="md:hidden text-text p-2 glass" onClick={() => setIsOpen(!isOpen)}>
          {isOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {isOpen && (
          <motion.div 
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="absolute top-full left-0 w-full glass p-6 md:hidden flex flex-col gap-6 overflow-hidden"
          >
            {navLinks.map((link) => (
              <Link 
                key={link.path} 
                to={link.path} 
                onClick={() => setIsOpen(false)}
                className="flex items-center gap-4 text-lg font-bold"
              >
                <link.icon size={22} className="text-primary" />
                {link.name}
              </Link>
            ))}
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};

// --- Page Components ---

const Hero = () => (
  <section className="section min-h-screen flex items-center relative pt-32 overflow-hidden">
    <div className="container relative z-10">
      <div className="max-w-4xl">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.23, 1, 0.32, 1] }}
        >
          <div className="flex items-center gap-3 mb-8">
            <span className="badge">Project Status: Production</span>
            <span className="w-2 h-2 rounded-full bg-primary animate-pulse shadow-[0_0_10px_var(--primary)]" />
          </div>
          
          <h1 className="text-6xl md:text-8xl mb-8 leading-tight">
            High-Scale <br />
            <span className="text-gradient">Bengali Language</span> <br />
            Intelligence Engine.
          </h1>
          
          <p className="text-text-dim max-w-2xl text-xl mb-12 leading-relaxed">
            The market-leading keyboard solution featuring a production-grade **2.3M+ vocabulary**, 
            Avro-optimized phonetic mapping, and real-time linguistic transformation.
          </p>
          
          <div className="flex flex-wrap gap-6">
            <Link to="/docs/architecture" className="btn-primary px-10 py-5">
              EXPLORE ARCHITECTURE <ChevronRight size={18} />
            </Link>
            <a href="https://github.com/Maijied/Lorapok-Keyboard" className="btn-secondary px-10 py-5">
              VIEW ON GITHUB
            </a>
          </div>
        </motion.div>
      </div>

      {/* Hero Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-6 mt-24">
        {[
          { label: 'Vocabulary', val: '2.34M+', icon: Zap },
          { label: 'Latency', val: '<1ms', icon: Activity },
          { label: 'Corpus', val: '1.43GB', icon: Database },
          { label: 'Sentences', val: '4.2M+', icon: Layers },
        ].map((stat, i) => (
          <motion.div 
            key={stat.label}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.6 + i * 0.1 }}
            className="glass p-8 relative group overflow-hidden"
          >
            <div className="absolute top-0 left-0 w-1 h-full bg-primary transform -translate-x-full group-hover:translate-x-0 transition-transform" />
            <stat.icon className="text-primary mb-4 opacity-50 group-hover:opacity-100 transition-opacity" size={24} />
            <div className="text-3xl font-black mb-1">{stat.val}</div>
            <div className="text-[10px] text-primary font-bold uppercase tracking-widest">{stat.label}</div>
          </motion.div>
        ))}
      </div>
    </div>
  </section>
);

const Features = () => (
  <section className="section border-t border-white/5 relative">
    <div className="container">
      <div className="text-center mb-20">
        <span className="text-mono">Core Capabilities</span>
        <h2 className="text-4xl md:text-5xl mt-4">Engineered for <span className="text-primary">Performance</span>.</h2>
      </div>

      <div className="grid md:grid-cols-3 gap-8">
        {[
          { 
            title: 'Statistical Inference', 
            desc: 'SQLite-backed bigram models optimized for ultra-low memory usage on mobile devices.', 
            icon: BrainCircuit 
          },
          { 
            title: 'Phonetic Accuracy', 
            desc: 'High-fidelity script conversion using advanced rule-based transliteration with 45,000+ key maps.', 
            icon: Zap 
          },
          { 
            title: 'Linguistic Tone', 
            desc: 'Real-time transformation between 8 tones, ensuring grammatical and formal consistency.', 
            icon: ShieldCheck 
          }
        ].map((feat, i) => (
          <div key={i} className="glass-card p-10 rounded-3xl">
            <div className="w-14 h-14 glass flex items-center justify-center mb-8 rounded-2xl">
              <feat.icon className="text-primary" size={28} />
            </div>
            <h3 className="text-2xl mb-4 font-bold">{feat.title}</h3>
            <p className="text-text-dim text-sm leading-relaxed">{feat.desc}</p>
          </div>
        ))}
      </div>
    </div>
  </section>
);

const Article = () => {
  const { id } = useParams();
  const articles = {
    'architecture': {
      title: 'System Architecture',
      subtitle: 'The Engineering Core of Lorapok',
      content: (
        <div className="space-y-8">
          <p className="text-lg text-text-dim leading-relaxed">
            Lorapok Keyboard is designed with a modular architecture that separates the input processing, phonetic conversion, and prediction logic.
          </p>
          <div className="grid gap-6">
            <div className="glass p-8">
              <span className="text-mono mb-2 block">Layer 01</span>
              <h4 className="text-xl font-bold mb-4 text-primary">Input Method Service</h4>
              <p className="text-sm text-text-dim">The entry point for Android. It manages UI state, haptic feedback, and raw key events using a lightweight Kotlin implementation.</p>
            </div>
            <div className="glass p-8">
              <span className="text-mono mb-2 block">Layer 02</span>
              <h4 className="text-xl font-bold mb-4 text-primary">Phonetic Engine</h4>
              <p className="text-sm text-text-dim">Handles real-time transliteration from Romanized input to Bengali script. Optimized for complex conjuncts and vowel positioning.</p>
            </div>
            <div className="glass p-8">
              <span className="text-mono mb-2 block">Layer 03</span>
              <h4 className="text-xl font-bold mb-4 text-primary">Prediction Engine</h4>
              <p className="text-sm text-text-dim">Consults a local SQLite database containing transition probabilities for over 2.3M unique words.</p>
            </div>
          </div>
        </div>
      )
    },
    'data-collection': {
      title: 'Massive Data Scaling',
      subtitle: 'Building a 2.3M+ Word Vocabulary',
      content: (
        <div className="space-y-8">
          <p className="text-lg text-text-dim">We leveraged multiple high-scale data channels to ensure linguistic diversity and coverage.</p>
          <div className="glass p-8 border-l-4 border-primary">
            <h4 className="font-bold mb-2 uppercase tracking-wider text-sm">Key Data Sources</h4>
            <ul className="text-sm text-text-dim space-y-3 mt-4">
              <li>&bull; **mC4 (Multilingual Common Crawl)**: Millions of high-quality web tokens.</li>
              <li>&bull; **TituLM**: Cleaned 50GB dataset for Bengali LLM training.</li>
              <li>&bull; **Bengali Wikipedia**: Comprehensive encyclopedic baseline.</li>
              <li>&bull; **Scraped Literature & Blogs**: capturing colloquial and formal variety.</li>
            </ul>
          </div>
        </div>
      )
    },
    'ml-process': {
      title: 'Statistical Modeling',
      subtitle: 'From Bigrams to Neural Inference',
      content: (
        <div className="space-y-8 text-text-dim">
          <p>Our model training pipeline involves massive-scale deduplication and tokenization of 88 million tokens.</p>
          <div className="glass p-8">
            <h4 className="text-white font-bold mb-4">Training Stages</h4>
            <div className="space-y-6">
              <div className="flex gap-4">
                <div className="w-1 h-12 bg-primary/20 flex-shrink-0" />
                <div>
                  <div className="text-white font-bold text-sm uppercase mb-1">01. Normalization</div>
                  <p className="text-xs">Unicode NFC normalization and cleaning of non-Bengali noise.</p>
                </div>
              </div>
              <div className="flex gap-4">
                <div className="w-1 h-12 bg-primary/40 flex-shrink-0" />
                <div>
                  <div className="text-white font-bold text-sm uppercase mb-1">02. Frequency Extraction</div>
                  <p className="text-xs">Ranked analysis of unigrams and bigrams across 4.2M sentences.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )
    },
    'development': {
      title: 'Engineering Workflow',
      subtitle: 'Modern Standards & CI/CD',
      content: (
        <div className="space-y-8 text-text-dim">
          <p>Stability and professional UX are the core priorities of our development process.</p>
          <div className="grid md:grid-cols-2 gap-4">
            <div className="glass p-6">
              <h5 className="text-white font-bold mb-2 uppercase text-xs tracking-widest">Stack</h5>
              <p className="text-sm">Kotlin, Python, SQLite, React</p>
            </div>
            <div className="glass p-6">
              <h5 className="text-white font-bold mb-2 uppercase text-xs tracking-widest">CI/CD</h5>
              <p className="text-sm">GitHub Actions, Automated Deployment</p>
            </div>
          </div>
        </div>
      )
    }
  };

  const article = articles[id] || articles['architecture'];

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="container min-h-screen pt-40 pb-20"
    >
      <div className="max-w-3xl mx-auto">
        <Link to="/" className="text-mono flex items-center gap-2 mb-12 hover:text-white transition-colors">
          <ChevronRight className="rotate-180" size={14} /> Back to Hub
        </Link>
        <span className="text-mono mb-4 block">Documentation</span>
        <h1 className="text-5xl md:text-6xl mb-4 font-black">{article.title}</h1>
        <p className="text-xl text-primary font-bold mb-16 uppercase tracking-widest text-[10px]">
          {article.subtitle}
        </p>
        {article.content}
      </div>
    </motion.div>
  );
};

const Footer = () => (
  <footer className="section border-t border-white/5 relative overflow-hidden">
    <div className="container relative z-10">
      <div className="flex flex-col md:flex-row justify-between gap-12">
        <div className="max-w-sm">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center">
              <Keyboard className="text-black" size={20} />
            </div>
            <span className="text-lg font-bold">LORAPOK</span>
          </div>
          <p className="text-text-dim text-sm leading-relaxed mb-8">
            Advanced Bengali linguistic engine and professional input solution. Engineered for the next generation of digital communication.
          </p>
          <div className="flex gap-4">
            <a href="https://github.com/Maijied/Lorapok-Keyboard" className="w-10 h-10 glass flex items-center justify-center rounded-lg hover:text-primary transition-colors">
              <ExternalLink size={18} />
            </a>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-20">
          <div>
            <h4 className="text-mono mb-6">Resources</h4>
            <ul className="space-y-3 text-sm text-text-dim font-bold">
              <li><Link to="/docs/architecture" className="hover:text-primary transition-colors">System Design</Link></li>
              <li><Link to="/docs/data-collection" className="hover:text-primary transition-colors">Data Engine</Link></li>
              <li><a href="https://github.com/Maijied/Lorapok-Keyboard" className="hover:text-primary transition-colors">Source Code</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-mono mb-6">Author</h4>
            <ul className="space-y-3 text-sm text-text-dim font-bold">
              <li><a href="https://github.com/Maijied" className="hover:text-primary transition-colors">Maijied</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">LinkedIn</a></li>
              <li><a href="#" className="hover:text-primary transition-colors">Research Gate</a></li>
            </ul>
          </div>
        </div>
      </div>
      <div className="mt-20 pt-8 border-t border-white/5 flex flex-col md:flex-row justify-between gap-4 text-[10px] text-text-dim uppercase tracking-widest font-bold">
        <span>&copy; 2026 Lorapok Intelligence Project.</span>
        <span>Made with Intelligence in Bangladesh.</span>
      </div>
    </div>
  </footer>
);

// --- Main App ---

function App() {
  const { pathname } = useLocation();

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  return (
    <div className="relative selection:bg-primary selection:text-black">
      <div className="bg-ambient" />
      <div className="bg-blob blob-1" />
      <div className="bg-blob blob-2" />
      <Navbar />
      <Routes>
        <Route path="/" element={<><Hero /><Features /></>} />
        <Route path="/docs/:id" element={<Article />} />
      </Routes>
      <Footer />
    </div>
  );
}

const Root = () => (
  <Router basename="/Lorapok-Keyboard">
    <App />
  </Router>
);

export default Root;
