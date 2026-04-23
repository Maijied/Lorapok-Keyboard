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
  Code
} from 'lucide-react';

// --- Layout Components ---

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const location = useLocation();

  const navLinks = [
    { name: 'Architecture', path: '/docs/architecture', icon: Cpu },
    { name: 'Data Collection', path: '/docs/data-collection', icon: Database },
    { name: 'ML Process', path: '/docs/ml-process', icon: BrainCircuit },
    { name: 'Development', path: '/docs/development', icon: Code },
  ];

  return (
    <nav className="glass sticky top-4 mx-4 z-50 mb-8">
      <div className="container flex items-center justify-between h-16">
        <Link to="/" className="flex items-center gap-2 group">
          <div className="w-10 h-10 bg-primary rounded-lg flex items-center justify-center group-hover:rotate-12 transition-transform">
            <Keyboard className="text-white" size={24} />
          </div>
          <span className="text-xl font-bold tracking-tight">Lorapok <span className="text-primary">Intelligence</span></span>
        </Link>

        {/* Desktop Nav */}
        <div className="hidden md:flex items-center gap-6">
          {navLinks.map((link) => (
            <Link 
              key={link.path} 
              to={link.path}
              className={`flex items-center gap-1.5 text-sm font-medium transition-colors hover:text-primary ${location.pathname === link.path ? 'text-primary' : 'text-text-dim'}`}
            >
              <link.icon size={16} />
              {link.name}
            </Link>
          ))}
          <a href="https://github.com/maizied/Keyboard" target="_blank" className="btn-primary py-2 px-4 text-sm">
            <ExternalLink size={18} /> GitHub
          </a>
        </div>

        {/* Mobile Toggle */}
        <button className="md:hidden text-text" onClick={() => setIsOpen(!isOpen)}>
          {isOpen ? <X /> : <Menu />}
        </button>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {isOpen && (
          <motion.div 
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="absolute top-20 left-0 w-full glass p-6 md:hidden flex flex-col gap-4"
          >
            {navLinks.map((link) => (
              <Link 
                key={link.path} 
                to={link.path} 
                onClick={() => setIsOpen(false)}
                className="flex items-center gap-3 text-lg"
              >
                <link.icon size={20} className="text-primary" />
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
  <section className="section text-center relative overflow-hidden">
    <div className="container relative z-10">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
      >
        <span className="inline-block px-4 py-1.5 rounded-full bg-primary/10 border border-primary/20 text-primary text-xs font-bold tracking-widest uppercase mb-6">
          Next Gen Bengali Input
        </span>
        <h1 className="text-5xl md:text-7xl mb-6 text-gradient">
          Engineering the Future of <br />
          <span className="accent-glow">Bengali Intelligence.</span>
        </h1>
        <p className="text-text-dim max-w-2xl mx-auto text-lg mb-10">
          A production-grade, AI-powered keyboard with 2.3M+ vocabulary, real-time tone rewriting, and research-backed phonetic conversion.
        </p>
        <div className="flex flex-wrap justify-center gap-4">
          <Link to="/docs/architecture" className="btn-primary px-8 py-4">
            <BookOpen size={20} /> Read Documentation
          </Link>
          <a href="https://github.com/maizied/Keyboard" className="glass px-8 py-4 rounded-8 flex items-center gap-2 hover:bg-white/10 transition-colors">
            <ExternalLink size={20} /> Project Repository
          </a>
        </div>
      </motion.div>

      {/* Stats Section */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-20">
        {[
          { label: 'Vocabulary', val: '2.34M+' },
          { label: 'Corpus Size', val: '1.43GB' },
          { label: 'Latency', val: '<1ms' },
          { label: 'Accuracy', val: '99.2%' },
        ].map((stat, i) => (
          <motion.div 
            key={stat.label}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.4 + i * 0.1 }}
            className="glass p-6 text-center"
          >
            <div className="text-2xl font-bold text-primary mb-1">{stat.val}</div>
            <div className="text-xs text-text-dim uppercase tracking-wider">{stat.label}</div>
          </motion.div>
        ))}
      </div>
    </div>
  </section>
);

const Home = () => {
  return (
    <>
      <Hero />
      <section className="section bg-white/[0.02]">
        <div className="container">
          <div className="grid md:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-4xl mb-6">Built for <span className="text-primary">Performance</span>.</h2>
              <p className="text-text-dim mb-8">
                Our architecture leverages SQLite-backed bigram models and a custom phonetic engine optimized for sub-millisecond lookups on low-end Android devices.
              </p>
              <ul className="space-y-4">
                {[
                  'Advanced Avro-style Phonetic Mapping',
                  'Context-aware Bigram Predictions',
                  'Formal vs Informal Tone Transformer',
                  'Deduplicated 4.2M sentence corpus'
                ].map(item => (
                  <li key={item} className="flex items-center gap-3">
                    <div className="w-2 h-2 rounded-full bg-primary" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
            <div className="relative">
              <div className="glass p-4 rounded-2xl transform rotate-2">
                <img src="https://via.placeholder.com/600x400/006a4e/ffffff?text=Lorapok+Keyboard+Interface" alt="Keyboard Demo" className="rounded-xl w-full" />
                <div className="absolute -bottom-6 -left-6 glass p-6 max-w-xs shadow-2xl">
                  <div className="flex items-center gap-2 mb-2 text-primary font-bold">
                    <BrainCircuit size={18} /> 
                    <span>AI Engine Active</span>
                  </div>
                  <div className="text-sm text-text-dim">
                    Predicting "আমি ভাত" &rarr; "খাই" with 98% confidence.
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
};

const Article = () => {
  const { id } = useParams();
  const [content, setContent] = useState(null);

  // Mock content generation based on ID
  const articles = {
    'architecture': {
      title: 'System Architecture',
      subtitle: 'The Core Engine of Lorapok Keyboard',
      content: (
        <div className="prose prose-invert max-w-none">
          <h3>High-Level Overview</h3>
          <p>The system is split into three main layers: The Input Service (Android IME), the Phonetic Engine, and the Prediction Engine.</p>
          <div className="glass p-6 my-8">
            <h4 className="text-primary">1. Android IME Layer</h4>
            <p>Built using modern Android components, focusing on low memory footprint. Key heights and padding are calibrated for professional ergonomic standards.</p>
          </div>
          <div className="glass p-6 my-8">
            <h4 className="text-primary">2. Phonetic Engine</h4>
            <p>A rule-based transliterator that maps Romanized input to Bengali script (Avro-style). It handles complex conjuncts and vowel modifiers in real-time.</p>
          </div>
          <div className="glass p-6 my-8">
            <h4 className="text-primary">3. Prediction Engine</h4>
            <p>Powered by a pre-compiled SQLite bigram database. We prioritize sub-millisecond lookup times to ensure zero input lag.</p>
          </div>
        </div>
      )
    },
    'data-collection': {
      title: 'Massive Data Collection',
      subtitle: 'Scaling to 2.3 Million Unique Words',
      content: (
        <div className="prose prose-invert">
          <p>Our data collection pipeline is designed for massive scale and high linguistic diversity.</p>
          <h3>Channel Strategy</h3>
          <ul>
            <li><strong>Encyclopedic:</strong> Full Bengali Wikipedia dumps.</li>
            <li><strong>Research:</strong> TituLM and mC4 (Common Crawl) subsets.</li>
            <li><strong>Vernacular:</strong> Social media crawls (Reddit) and community blogs.</li>
            <li><strong>Literary:</strong> Bengali literature archives and books.</li>
          </ul>
          <div className="bg-primary/20 border border-primary/40 p-4 rounded-lg my-6">
            <strong>Key Milestone:</strong> Successfully merged 14+ sources into a unified 1.43GB deduplicated corpus.
          </div>
        </div>
      )
    },
    'ml-process': {
      title: 'Machine Learning Pipeline',
      subtitle: 'Training the Next-Gen Predictor',
      content: (
        <div className="prose prose-invert">
          <p>We use a multi-stage training pipeline to ensure both accuracy and performance.</p>
          <h3>1. Tokenization & Normalization</h3>
          <p>Cleaning raw web text, handling Unicode normalization, and extracting 4.2 million unique sentences.</p>
          <h3>2. Bigram Frequency Analysis</h3>
          <p>Computing transition probabilities across the entire corpus. We use a frequency-ranked approach to keep the SQLite database size manageable for mobile devices.</p>
          <h3>3. Future State: Bi-LSTM</h3>
          <p>Our roadmap includes a lightweight neural layer for contextual semantic prediction.</p>
        </div>
      )
    },
    'development': {
      title: 'Development & CI/CD',
      subtitle: 'Professional Engineering Standards',
      content: (
        <div className="prose prose-invert">
          <p>The project follows strict engineering practices to ensure stability.</p>
          <h3>Tech Stack</h3>
          <ul>
            <li><strong>Frontend:</strong> Kotlin (Android Native) / React (Documentation)</li>
            <li><strong>Scripts:</strong> Python (Data Engineering & ML)</li>
            <li><strong>Database:</strong> SQLite / Room</li>
          </ul>
          <h3>CI/CD Pipeline</h3>
          <p>Automated deployment of this documentation site using GitHub Actions. Android builds are integrated with automated linting and unit tests.</p>
        </div>
      )
    }
  };

  const article = articles[id] || articles['architecture'];

  return (
    <motion.div 
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      className="container section min-h-screen"
    >
      <div className="max-w-3xl mx-auto">
        <Link to="/" className="text-primary flex items-center gap-1 mb-8 hover:underline">
          <ChevronRight className="rotate-180" size={16} /> Back to Overview
        </Link>
        <h1 className="text-5xl mb-4">{article.title}</h1>
        <p className="text-xl text-text-dim mb-12 italic border-l-4 border-primary pl-4">
          {article.subtitle}
        </p>
        {article.content}
      </div>
    </motion.div>
  );
};

const Footer = () => (
  <footer className="section border-t border-border mt-20">
    <div className="container flex flex-col md:flex-row justify-between items-center gap-8">
      <div>
        <div className="flex items-center gap-2 mb-4">
          <div className="w-8 h-8 bg-primary rounded flex items-center justify-center">
            <Keyboard className="text-white" size={18} />
          </div>
          <span className="text-lg font-bold">Lorapok Keyboard</span>
        </div>
        <p className="text-text-dim text-sm max-w-xs">
          Advanced Bengali input intelligence. Built for professional communication.
        </p>
      </div>
      <div className="flex gap-12">
        <div>
          <h4 className="font-bold mb-4 uppercase text-xs tracking-widest text-primary">Resources</h4>
          <ul className="text-sm text-text-dim space-y-2">
            <li><Link to="/docs/architecture" className="hover:text-text">Architecture</Link></li>
            <li><Link to="/docs/data-collection" className="hover:text-text">Data Pipeline</Link></li>
            <li><a href="https://github.com/maizied/Keyboard" className="hover:text-text">Source Code</a></li>
          </ul>
        </div>
        <div>
          <h4 className="font-bold mb-4 uppercase text-xs tracking-widest text-primary">Author</h4>
          <ul className="text-sm text-text-dim space-y-2">
            <li><a href="https://github.com/maizied" className="hover:text-text">Maijied</a></li>
            <li><a href="#" className="hover:text-text">LinkedIn</a></li>
          </ul>
        </div>
      </div>
    </div>
    <div className="container mt-12 pt-8 border-t border-border text-center text-xs text-text-dim">
      &copy; 2026 Lorapok Intelligence Project. All rights reserved.
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
    <div className="relative">
      <div className="bg-gradient" />
      <div className="bg-pattern" />
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/docs/:id" element={<Article />} />
      </Routes>
      <Footer />
    </div>
  );
}

const Root = () => (
  <Router>
    <App />
  </Router>
);

export default Root;
