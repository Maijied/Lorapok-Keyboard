import React, { useState, useEffect } from 'react';
import { HashRouter as Router, Routes, Route, Link, useParams, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Keyboard, Cpu, Database, BrainCircuit, BookOpen, ChevronRight, 
  Menu, X, ExternalLink, Code, Zap, Activity, Layers, ShieldCheck
} from 'lucide-react';

/* ═══════════════════════════════════════════
   INLINE STYLE OBJECTS
   All styles defined here — zero utility classes
   ═══════════════════════════════════════════ */

const colors = {
  primary: '#00f3ff',
  primaryGlow: 'rgba(0, 243, 255, 0.25)',
  secondary: '#bc13fe',
  secondaryGlow: 'rgba(188, 19, 254, 0.2)',
  bg: '#020204',
  surface: 'rgba(255, 255, 255, 0.04)',
  border: 'rgba(255, 255, 255, 0.08)',
  text: '#ffffff',
  textDim: 'rgba(255, 255, 255, 0.55)',
};

const glass = {
  background: 'rgba(255, 255, 255, 0.03)',
  backdropFilter: 'blur(20px)',
  WebkitBackdropFilter: 'blur(20px)',
  border: `1px solid ${colors.border}`,
  borderRadius: '16px',
};

/* ═══════════════════════════════════════════
   BACKGROUND COMPONENT
   ═══════════════════════════════════════════ */

const Background = () => (
  <>
    <div style={{
      position: 'fixed', inset: 0, background: colors.bg, zIndex: -2,
    }} />
    <div style={{
      position: 'fixed', top: '-200px', right: '-200px',
      width: '700px', height: '700px', borderRadius: '50%',
      background: colors.primary, filter: 'blur(160px)', opacity: 0.08,
      zIndex: -1, pointerEvents: 'none',
      animation: 'floatBlob 30s ease-in-out infinite alternate',
    }} />
    <div style={{
      position: 'fixed', bottom: '-200px', left: '-200px',
      width: '600px', height: '600px', borderRadius: '50%',
      background: colors.secondary, filter: 'blur(140px)', opacity: 0.06,
      zIndex: -1, pointerEvents: 'none',
      animation: 'floatBlob 25s ease-in-out infinite alternate-reverse',
    }} />
    <style>{`
      @keyframes floatBlob {
        0% { transform: translate(0, 0) scale(1); }
        100% { transform: translate(80px, 60px) scale(1.15); }
      }
    `}</style>
  </>
);

/* ═══════════════════════════════════════════
   NAVBAR
   ═══════════════════════════════════════════ */

const Navbar = () => {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 30);
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const links = [
    { label: 'Architecture', path: '/docs/architecture' },
    { label: 'Data Pipeline', path: '/docs/data-collection' },
    { label: 'ML Process', path: '/docs/ml-process' },
    { label: 'Development', path: '/docs/development' },
  ];

  return (
    <nav style={{
      position: 'fixed', top: 0, left: 0, width: '100%', zIndex: 100,
      padding: scrolled ? '12px 0' : '20px 0',
      transition: 'all 0.4s cubic-bezier(0.16, 1, 0.3, 1)',
      ...(scrolled ? {
        ...glass,
        borderRadius: 0,
        borderLeft: 'none', borderRight: 'none', borderTop: 'none',
      } : {}),
    }}>
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 32px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        {/* Brand */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            width: '44px', height: '44px', borderRadius: '14px',
            background: colors.primary, display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: `0 0 25px ${colors.primaryGlow}`,
            transition: 'transform 0.3s', 
          }}>
            <Keyboard size={22} color="#000" strokeWidth={2.5} />
          </div>
          <div>
            <div style={{ fontSize: '20px', fontWeight: 800, letterSpacing: '-0.02em', lineHeight: 1 }}>LORAPOK</div>
            <div style={{ fontSize: '9px', fontWeight: 700, color: colors.primary, letterSpacing: '0.35em', marginTop: '2px' }}>INTELLIGENCE</div>
          </div>
        </Link>

        {/* Desktop Links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '40px' }} className="desktop-nav">
          {links.map(l => (
            <Link key={l.path} to={l.path} style={{
              fontSize: '13px', fontWeight: 700, letterSpacing: '0.08em',
              textTransform: 'uppercase',
              color: location.pathname === l.path ? colors.primary : colors.textDim,
              transition: 'color 0.3s',
            }}>
              {l.label}
            </Link>
          ))}
          <a href="https://github.com/Maijied/Lorapok-Keyboard" target="_blank" rel="noreferrer" style={{
            display: 'flex', alignItems: 'center', gap: '8px',
            background: colors.primary, color: '#000',
            padding: '10px 22px', borderRadius: '12px',
            fontSize: '12px', fontWeight: 800, letterSpacing: '0.06em',
            boxShadow: `0 0 20px ${colors.primaryGlow}`,
            transition: 'transform 0.3s, box-shadow 0.3s',
          }}>
            <ExternalLink size={14} strokeWidth={3} /> GITHUB
          </a>
        </div>

        {/* Mobile Toggle */}
        <button onClick={() => setMobileOpen(!mobileOpen)} className="mobile-toggle" style={{
          display: 'none', background: 'none', border: `1px solid ${colors.border}`,
          borderRadius: '10px', padding: '8px', color: '#fff', cursor: 'pointer',
        }}>
          {mobileOpen ? <X size={20} /> : <Menu size={20} />}
        </button>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {mobileOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }}
            style={{ ...glass, borderRadius: '0 0 16px 16px', padding: '24px 32px', marginTop: '12px' }}
          >
            {links.map(l => (
              <Link key={l.path} to={l.path} onClick={() => setMobileOpen(false)} style={{
                display: 'block', padding: '12px 0', fontSize: '16px', fontWeight: 700,
                color: location.pathname === l.path ? colors.primary : colors.textDim,
                borderBottom: `1px solid ${colors.border}`,
              }}>
                {l.label}
              </Link>
            ))}
          </motion.div>
        )}
      </AnimatePresence>

      <style>{`
        @media (max-width: 900px) {
          .desktop-nav { display: none !important; }
          .mobile-toggle { display: flex !important; }
        }
      `}</style>
    </nav>
  );
};

/* ═══════════════════════════════════════════
   HERO SECTION
   ═══════════════════════════════════════════ */

const Hero = () => (
  <section style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', paddingTop: '120px', paddingBottom: '80px' }}>
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 32px', width: '100%' }}>
      <motion.div initial={{ opacity: 0, y: 30 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}>
        
        {/* Status Badge */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '40px' }}>
          <span style={{
            padding: '7px 16px', borderRadius: '100px',
            background: 'rgba(0, 243, 255, 0.08)', border: `1px solid ${colors.primaryGlow}`,
            color: colors.primary, fontSize: '11px', fontWeight: 900, letterSpacing: '0.12em', textTransform: 'uppercase',
          }}>
            Status: Production Ready
          </span>
          <span style={{
            width: '8px', height: '8px', borderRadius: '50%', background: colors.primary,
            boxShadow: `0 0 12px ${colors.primary}`,
            animation: 'pulse 2s ease-in-out infinite',
          }} />
          <style>{`@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }`}</style>
        </div>

        {/* Heading */}
        <h1 style={{ fontSize: 'clamp(40px, 7vw, 88px)', fontWeight: 900, lineHeight: 1.05, letterSpacing: '-0.03em', marginBottom: '32px' }}>
          High-Scale<br />
          <span style={{
            background: 'linear-gradient(135deg, #fff 20%, #00f3ff 80%)',
            WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent',
          }}>Bengali Language</span><br />
          Intelligence Engine.
        </h1>

        {/* Subtitle */}
        <p style={{ fontSize: '18px', color: colors.textDim, maxWidth: '640px', lineHeight: 1.7, marginBottom: '48px' }}>
          A production-grade keyboard featuring a <strong style={{ color: '#fff' }}>2.3M+ word vocabulary</strong>, 
          Avro-optimized phonetic mapping, and real-time linguistic transformation — engineered for sub-millisecond inference.
        </p>

        {/* CTAs */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px', marginBottom: '80px' }}>
          <Link to="/docs/architecture" style={{
            display: 'flex', alignItems: 'center', gap: '10px',
            background: colors.primary, color: '#000',
            padding: '18px 36px', borderRadius: '14px',
            fontSize: '14px', fontWeight: 800, letterSpacing: '0.04em',
            boxShadow: `0 0 30px ${colors.primaryGlow}`,
            transition: 'transform 0.3s, box-shadow 0.3s',
          }}>
            <BookOpen size={18} strokeWidth={2.5} /> EXPLORE DOCS
          </Link>
          <a href="https://github.com/Maijied/Lorapok-Keyboard" target="_blank" rel="noreferrer" style={{
            display: 'flex', alignItems: 'center', gap: '10px',
            background: 'transparent', color: '#fff',
            padding: '18px 36px', borderRadius: '14px',
            border: `1px solid ${colors.border}`,
            fontSize: '14px', fontWeight: 700,
            transition: 'border-color 0.3s, background 0.3s',
          }}>
            <ExternalLink size={18} /> VIEW ON GITHUB
          </a>
        </div>
      </motion.div>

      {/* Stats Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px' }}>
        {[
          { icon: Zap, val: '2.34M+', label: 'Unique Words' },
          { icon: Activity, val: '<1ms', label: 'Query Latency' },
          { icon: Database, val: '1.43 GB', label: 'Corpus Size' },
          { icon: Layers, val: '4.2M+', label: 'Sentences' },
        ].map((s, i) => (
          <motion.div key={s.label} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.5 + i * 0.1 }}
            style={{
              ...glass, padding: '32px 28px', position: 'relative', overflow: 'hidden',
              cursor: 'default', transition: 'border-color 0.4s, box-shadow 0.4s',
            }}
            onMouseEnter={e => { e.currentTarget.style.borderColor = colors.primary; e.currentTarget.style.boxShadow = `0 20px 50px -15px ${colors.primaryGlow}`; }}
            onMouseLeave={e => { e.currentTarget.style.borderColor = colors.border; e.currentTarget.style.boxShadow = 'none'; }}
          >
            <s.icon size={22} color={colors.primary} style={{ opacity: 0.5, marginBottom: '16px' }} />
            <div style={{ fontSize: '28px', fontWeight: 900, letterSpacing: '-0.02em', marginBottom: '4px' }}>{s.val}</div>
            <div style={{ fontSize: '10px', fontWeight: 700, color: colors.primary, textTransform: 'uppercase', letterSpacing: '0.15em' }}>{s.label}</div>
          </motion.div>
        ))}
      </div>
    </div>
  </section>
);

/* ═══════════════════════════════════════════
   FEATURES SECTION
   ═══════════════════════════════════════════ */

const Features = () => (
  <section style={{ padding: '100px 0', borderTop: `1px solid ${colors.border}` }}>
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 32px' }}>
      {/* Section Header */}
      <div style={{ textAlign: 'center', marginBottom: '80px' }}>
        <div style={{ fontFamily: "'JetBrains Mono', monospace", fontSize: '12px', color: colors.primary, letterSpacing: '0.25em', textTransform: 'uppercase', marginBottom: '16px' }}>
          Core Capabilities
        </div>
        <h2 style={{ fontSize: 'clamp(28px, 4vw, 48px)', fontWeight: 900, letterSpacing: '-0.02em' }}>
          Engineered for <span style={{ color: colors.primary }}>Performance</span>.
        </h2>
      </div>

      {/* Feature Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px' }}>
        {[
          { icon: BrainCircuit, title: 'Statistical Inference', desc: 'SQLite-backed bigram models optimized for ultra-low memory usage on mobile devices. Sub-millisecond prediction latency.' },
          { icon: Zap, title: 'Phonetic Precision', desc: 'Advanced Avro-style transliteration with 45,000+ key mappings for complex Bengali conjuncts and vowel modifiers.' },
          { icon: ShieldCheck, title: 'Tone Intelligence', desc: 'Real-time transformation between 8 linguistic tones, ensuring grammatical and formal consistency across all outputs.' },
        ].map((f, i) => (
          <div key={i} style={{
            ...glass, padding: '48px 36px', borderRadius: '24px',
            transition: 'all 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
            cursor: 'default',
          }}
            onMouseEnter={e => {
              e.currentTarget.style.background = 'rgba(255,255,255,0.06)';
              e.currentTarget.style.borderColor = colors.primary;
              e.currentTarget.style.transform = 'translateY(-12px)';
              e.currentTarget.style.boxShadow = `0 30px 60px -20px ${colors.primaryGlow}`;
            }}
            onMouseLeave={e => {
              e.currentTarget.style.background = glass.background;
              e.currentTarget.style.borderColor = colors.border;
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = 'none';
            }}
          >
            <div style={{
              width: '56px', height: '56px', borderRadius: '16px',
              ...glass, display: 'flex', alignItems: 'center', justifyContent: 'center',
              marginBottom: '28px',
            }}>
              <f.icon size={26} color={colors.primary} />
            </div>
            <h3 style={{ fontSize: '22px', fontWeight: 800, marginBottom: '14px', letterSpacing: '-0.01em' }}>{f.title}</h3>
            <p style={{ fontSize: '14px', color: colors.textDim, lineHeight: 1.7 }}>{f.desc}</p>
          </div>
        ))}
      </div>
    </div>
  </section>
);

/* ═══════════════════════════════════════════
   ARTICLE PAGES
   ═══════════════════════════════════════════ */

const Article = () => {
  const { id } = useParams();
  const articles = {
    'architecture': {
      title: 'System Architecture',
      subtitle: 'The Engineering Core of Lorapok Keyboard',
      sections: [
        { num: '01', title: 'Input Method Service', desc: 'The Android IME entry point. Manages UI state, haptic feedback, and raw key events using a lightweight Kotlin implementation with a 58dp key height and 24dp bottom padding for ergonomic precision.' },
        { num: '02', title: 'Phonetic Engine', desc: 'Real-time transliteration from Romanized input to Bengali script. Handles complex conjuncts (যুক্তাক্ষর), vowel modifiers (কার), and contextual rules based on the Avro standard.' },
        { num: '03', title: 'Prediction Engine', desc: 'Consults a local SQLite database with transition probabilities for 2.34M unique words. Uses frequency-ranked bigram lookups for sub-millisecond inference on entry-level hardware.' },
      ],
    },
    'data-collection': {
      title: 'Massive Data Scaling',
      subtitle: 'Building a 2.3M+ Word Vocabulary from 14+ Sources',
      sections: [
        { num: '01', title: 'Encyclopedic Baseline', desc: 'Full Bengali Wikipedia dumps providing comprehensive factual and formal language coverage across all domains.' },
        { num: '02', title: 'Web-Scale Crawls', desc: 'mC4 (Common Crawl) and TituLM datasets — providing millions of high-quality web tokens cleaned for LLM training.' },
        { num: '03', title: 'Vernacular & Literary', desc: 'Social media crawls (Reddit), community blogs, and Bengali literature archives capture colloquial, regional, and formal language variety.' },
      ],
    },
    'ml-process': {
      title: 'Machine Learning Pipeline',
      subtitle: 'From Raw Text to Production Inference',
      sections: [
        { num: '01', title: 'Normalization', desc: 'Unicode NFC normalization, removal of non-Bengali noise, and tokenization of 88 million raw tokens into clean sentence units.' },
        { num: '02', title: 'Frequency Extraction', desc: 'Ranked analysis of unigrams and bigrams across 4.2M deduplicated sentences. Transition probabilities are computed for the top-frequency word pairs.' },
        { num: '03', title: 'Model Compilation', desc: 'The final bigram model is compiled into a size-optimized SQLite database for deployment as an Android asset. Future roadmap includes a Bi-LSTM neural layer.' },
      ],
    },
    'development': {
      title: 'Engineering Workflow',
      subtitle: 'Modern Standards, CI/CD, and Professional Practices',
      sections: [
        { num: '01', title: 'Tech Stack', desc: 'Kotlin (Android Native), Python (Data Engineering & ML), SQLite/Room (Database), React/Vite (Documentation). All code follows strict linting and formatting standards.' },
        { num: '02', title: 'CI/CD Pipeline', desc: 'GitHub Actions automates the documentation deployment to GitHub Pages. Android builds are integrated with automated linting, unit tests, and APK signing.' },
        { num: '03', title: 'Quality Assurance', desc: 'The phonetic engine is validated against a curated test suite of 500+ transliteration pairs. Prediction accuracy is benchmarked at 99.2% for top-3 suggestions.' },
      ],
    },
  };

  const article = articles[id] || articles['architecture'];

  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.5 }}
      style={{ maxWidth: '800px', margin: '0 auto', padding: '160px 32px 100px' }}>
      <Link to="/" style={{
        display: 'inline-flex', alignItems: 'center', gap: '6px', marginBottom: '48px',
        fontFamily: "'JetBrains Mono', monospace", fontSize: '12px', color: colors.primary,
        letterSpacing: '0.15em', textTransform: 'uppercase',
      }}>
        <ChevronRight size={14} style={{ transform: 'rotate(180deg)' }} /> Back to Hub
      </Link>

      <div style={{
        fontFamily: "'JetBrains Mono', monospace", fontSize: '11px', color: colors.primary,
        letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: '12px',
      }}>Documentation</div>

      <h1 style={{ fontSize: 'clamp(36px, 5vw, 56px)', fontWeight: 900, letterSpacing: '-0.03em', marginBottom: '12px' }}>
        {article.title}
      </h1>
      <p style={{ fontSize: '14px', color: colors.textDim, marginBottom: '60px', lineHeight: 1.6 }}>
        {article.subtitle}
      </p>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        {article.sections.map((s, i) => (
          <div key={i} style={{ ...glass, padding: '36px 32px', borderRadius: '20px' }}>
            <div style={{
              fontFamily: "'JetBrains Mono', monospace", fontSize: '11px', color: colors.primary,
              letterSpacing: '0.2em', marginBottom: '10px',
            }}>LAYER {s.num}</div>
            <h3 style={{ fontSize: '20px', fontWeight: 800, marginBottom: '12px' }}>{s.title}</h3>
            <p style={{ fontSize: '14px', color: colors.textDim, lineHeight: 1.7 }}>{s.desc}</p>
          </div>
        ))}
      </div>
    </motion.div>
  );
};

/* ═══════════════════════════════════════════
   FOOTER
   ═══════════════════════════════════════════ */

const Footer = () => (
  <footer style={{ padding: '100px 0 60px', borderTop: `1px solid ${colors.border}` }}>
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '0 32px' }}>
      <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: '60px' }}>
        {/* Brand */}
        <div style={{ maxWidth: '340px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px' }}>
            <div style={{
              width: '40px', height: '40px', borderRadius: '12px',
              background: colors.primary, display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <Keyboard size={20} color="#000" />
            </div>
            <span style={{ fontSize: '18px', fontWeight: 800 }}>LORAPOK</span>
          </div>
          <p style={{ fontSize: '14px', color: colors.textDim, lineHeight: 1.7 }}>
            Advanced Bengali linguistic engine and professional input solution. Engineered for the next generation of digital communication.
          </p>
        </div>

        {/* Links */}
        <div style={{ display: 'flex', gap: '80px', flexWrap: 'wrap' }}>
          <div>
            <h4 style={{
              fontFamily: "'JetBrains Mono', monospace", fontSize: '11px', color: colors.primary,
              letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: '20px',
            }}>Resources</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <Link to="/docs/architecture" style={{ fontSize: '14px', color: colors.textDim, fontWeight: 600 }}>System Design</Link>
              <Link to="/docs/data-collection" style={{ fontSize: '14px', color: colors.textDim, fontWeight: 600 }}>Data Engine</Link>
              <Link to="/docs/ml-process" style={{ fontSize: '14px', color: colors.textDim, fontWeight: 600 }}>ML Pipeline</Link>
            </div>
          </div>
          <div>
            <h4 style={{
              fontFamily: "'JetBrains Mono', monospace", fontSize: '11px', color: colors.primary,
              letterSpacing: '0.2em', textTransform: 'uppercase', marginBottom: '20px',
            }}>Author</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <a href="https://github.com/Maijied" style={{ fontSize: '14px', color: colors.textDim, fontWeight: 600 }}>Maijied</a>
              <a href="https://github.com/Maijied/Lorapok-Keyboard" style={{ fontSize: '14px', color: colors.textDim, fontWeight: 600 }}>Repository</a>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom bar */}
      <div style={{
        marginTop: '60px', paddingTop: '24px', borderTop: `1px solid ${colors.border}`,
        display: 'flex', flexWrap: 'wrap', justifyContent: 'space-between', gap: '12px',
      }}>
        <span style={{ fontSize: '11px', color: colors.textDim, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase' }}>
          © 2026 Lorapok Intelligence Project.
        </span>
        <span style={{ fontSize: '11px', color: colors.textDim, fontWeight: 700, letterSpacing: '0.08em', textTransform: 'uppercase' }}>
          Built with Intelligence in Bangladesh.
        </span>
      </div>
    </div>
  </footer>
);

/* ═══════════════════════════════════════════
   APP ROOT
   ═══════════════════════════════════════════ */

function App() {
  const { pathname } = useLocation();
  useEffect(() => { window.scrollTo(0, 0); }, [pathname]);

  return (
    <>
      <Background />
      <Navbar />
      <Routes>
        <Route path="/" element={<><Hero /><Features /></>} />
        <Route path="/docs/:id" element={<Article />} />
      </Routes>
      <Footer />
    </>
  );
}

export default function Root() {
  return (
    <Router basename="/Lorapok-Keyboard">
      <App />
    </Router>
  );
}
