/* global React, Icons, useAuth, useToast, initials,
   Dashboard, Products, Inventory, Warehouses, Orders, Shipments, Routes,
   useTweaks, TweaksPanel, TweakSection, TweakRadio, TweakToggle, TweakColor */
const { useState, useEffect } = React;

const NAV = [
  { section: 'Operación', items: [
    { id: 'dashboard', label: 'Vista general', icon: Icons.Dashboard, comp: () => <Dashboard/> },
    { id: 'orders', label: 'Órdenes', icon: Icons.Order, comp: () => <Orders/> },
  ]},
  { section: 'Inventario', items: [
    { id: 'products', label: 'Productos', icon: Icons.Box, comp: () => <Products/> },
    { id: 'inventory', label: 'Stock', icon: Icons.Stock, comp: () => <Inventory/> },
    { id: 'warehouses', label: 'Bodegas', icon: Icons.Warehouse, comp: () => <Warehouses/> },
  ]},
  { section: 'Logística', items: [
    { id: 'shipments', label: 'Envíos', icon: Icons.Truck, comp: () => <Shipments/> },
    { id: 'routes', label: 'Rutas', icon: Icons.Route, comp: () => <Routes/> },
  ]},
];

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "layout": "hybrid",
  "accentHue": "150",
  "denseTables": false,
  "showConnBanner": true,
  "darkMode": false
}/*EDITMODE-END*/;

function Shell() {
  const [route, setRoute] = useState('dashboard');
  const [hybridExpanded, setHybridExpanded] = useState(true);
  const { session, logout } = useAuth();
  const [conn, setConn] = useState(null);
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);

  // Apply accent hue tweak
  useEffect(() => {
    const hue = t.accentHue;
    document.documentElement.style.setProperty('--accent', `oklch(0.55 0.12 ${hue})`);
    document.documentElement.style.setProperty('--accent-hover', `oklch(0.50 0.12 ${hue})`);
    document.documentElement.style.setProperty('--accent-subtle', `oklch(0.95 0.04 ${hue})`);
    document.documentElement.style.setProperty('--accent-text', `oklch(0.40 0.12 ${hue})`);
    document.documentElement.style.setProperty('--accent-border', `oklch(0.78 0.10 ${hue})`);
  }, [t.accentHue]);

  useEffect(() => {
    SmartlogixAPI.ping().then(setConn);
  }, []);

  // Apply dark mode
  useEffect(() => {
    if (t.darkMode) document.documentElement.setAttribute('data-theme', 'dark');
    else document.documentElement.removeAttribute('data-theme');
  }, [t.darkMode]);

  const allItems = NAV.flatMap(s => s.items);
  const current = allItems.find(i => i.id === route) || allItems[0];

  // === Layout selection ===
  const layoutClass = t.layout === 'topbar' ? 'layout-topbar' : t.layout === 'hybrid' ? `layout-hybrid ${hybridExpanded ? 'expanded' : ''}` : 'layout-sidebar';

  return (
    <div className={`app ${layoutClass}`} data-screen-label={current.label}>
      {t.layout !== 'topbar' && (
        <aside className="sidebar">
          <div className="brand">
            <div className="brand-mark">SL</div>
            <span className="brand-text">SmartLogix</span>
            {t.layout === 'hybrid' && (
              <button className="btn btn-ghost btn-icon" style={{ marginLeft: 'auto', padding: 2 }} onClick={() => setHybridExpanded(x => !x)}>
                {hybridExpanded ? Icons.ChevronLeft : Icons.ChevronRight}
              </button>
            )}
          </div>
          <nav>
            {NAV.map(sec => (
              <React.Fragment key={sec.section}>
                <div className="nav-section-label">{sec.section}</div>
                {sec.items.map(it => (
                  <button key={it.id} className={`nav-item ${route === it.id ? 'active' : ''}`} onClick={() => setRoute(it.id)}>
                    {it.icon}
                    <span className="nav-item-text">{it.label}</span>
                  </button>
                ))}
              </React.Fragment>
            ))}
          </nav>
          <div className="footer">
            <div className="avatar">{initials(session?.user?.name || 'Usuario')}</div>
            <div className="footer-text" style={{ flex: 1, minWidth: 0, fontSize: 12 }}>
              <div style={{ fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{session?.user?.name || 'Usuario'}</div>
              <div style={{ color: 'var(--text-tertiary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{session?.user?.companyName || 'Empresa'}</div>
            </div>
            <button className="btn btn-ghost btn-icon footer-text" onClick={logout} title="Cerrar sesión">{Icons.Logout}</button>
          </div>
        </aside>
      )}

      {t.layout === 'topbar' && (
        <header className="topbar">
          <div className="brand"><div className="brand-mark">SL</div><span>SmartLogix</span></div>
          <nav>
            {allItems.map(it => (
              <button key={it.id} className={`nav-item ${route === it.id ? 'active' : ''}`} onClick={() => setRoute(it.id)}>
                {it.icon}
                <span>{it.label}</span>
              </button>
            ))}
          </nav>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <button className="btn btn-ghost btn-icon">{Icons.Bell}</button>
            <div className="avatar">{initials(session?.user?.name || 'U')}</div>
            <button className="btn btn-ghost btn-icon" onClick={logout}>{Icons.Logout}</button>
          </div>
        </header>
      )}

      <main className="main">
        {t.showConnBanner && conn && (
          <div className={`conn-banner ${!conn.mock ? 'ok' : ''}`}>
            <span style={{ width: 6, height: 6, borderRadius: '50%', background: 'currentColor' }}/>
            {conn.mock
              ? <>Backend no detectado en <span className="mono">{SmartlogixAPI.config.getBase()}</span> · usando datos de demostración. <a href="#" onClick={(e) => { e.preventDefault(); SmartlogixAPI.config.setMock(false); SmartlogixAPI.ping().then(setConn); }} style={{ marginLeft: 'auto' }}>Reintentar</a></>
              : <>Conectado a backend SmartLogix · <span className="mono">{SmartlogixAPI.config.getBase()}</span></>
            }
          </div>
        )}
        {current.comp()}
      </main>

      <SmartlogixTweaks t={t} setTweak={setTweak}/>
    </div>
  );
}

function SmartlogixTweaks({ t, setTweak }) {
  return (
    <TweaksPanel title="Tweaks">
      <TweakSection title="Layout" description="Cambia la estructura general de la app.">
        <TweakRadio value={t.layout} onChange={v => setTweak('layout', v)} options={[
          { value: 'sidebar', label: 'Sidebar' },
          { value: 'topbar', label: 'Topbar' },
          { value: 'hybrid', label: 'Hybrid' },
        ]}/>
      </TweakSection>
      <TweakSection title="Acento" description="Hue del color de acento (oklch, chroma fija).">
        <TweakColor value={t.accentHue} onChange={v => setTweak('accentHue', v)} options={[
          { value: '150', color: 'oklch(0.55 0.12 150)' },
          { value: '170', color: 'oklch(0.55 0.12 170)' },
          { value: '195', color: 'oklch(0.55 0.12 195)' },
          { value: '240', color: 'oklch(0.55 0.12 240)' },
          { value: '60', color: 'oklch(0.65 0.12 75)' },
        ]}/>
      </TweakSection>
      <TweakSection title="Densidad">
        <TweakToggle label="Tablas compactas" value={t.denseTables} onChange={v => setTweak('denseTables', v)}/>
      </TweakSection>
      <TweakSection title="Conexión">
        <TweakToggle label="Mostrar banner de estado" value={t.showConnBanner} onChange={v => setTweak('showConnBanner', v)}/>
      </TweakSection>
      <TweakSection title="Apariencia">
        <TweakToggle label="Modo oscuro" value={t.darkMode} onChange={v => setTweak('darkMode', v)}/>
      </TweakSection>
    </TweaksPanel>
  );
}

// ====== Root ======
function App() {
  const { session } = useAuth();
  return session ? <Shell/> : <AuthScreen/>;
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <ToastProvider>
    <AuthProvider>
      <App/>
    </AuthProvider>
  </ToastProvider>
);
