import React, { useState } from 'react';
import { Dashboard } from './pages/Dashboard';
import { CreateOrder } from './pages/CreateOrder';
import { Auth } from './pages/Auth';
import { LayoutDashboard, ShoppingCart, Package, Warehouse, Truck, Map, LogOut } from 'lucide-react';
import { useAuthStore } from './store/useAuthStore';

type ViewStrategy = 'dashboard' | 'orders' | 'products' | 'warehouses' | 'shipments' | 'routes' | 'create_order';

const NAV = [
  { section: 'Operación', items: [
    { id: 'dashboard', label: 'Vista general', icon: <LayoutDashboard className="ico" /> },
    { id: 'orders', label: 'Órdenes', icon: <ShoppingCart className="ico" /> },
  ]},
  { section: 'Inventario', items: [
    { id: 'products', label: 'Productos', icon: <Package className="ico" /> },
    { id: 'warehouses', label: 'Bodegas', icon: <Warehouse className="ico" /> },
  ]},
  { section: 'Logística', items: [
    { id: 'shipments', label: 'Envíos', icon: <Truck className="ico" /> },
    { id: 'routes', label: 'Rutas', icon: <Map className="ico" /> },
  ]},
];

export default function App() {
  const [route, setRoute] = useState<ViewStrategy>('dashboard');
  const { token, clearSession } = useAuthStore();

  if (!token) {
    return <Auth />;
  }

  const renderContent = () => {
    switch (route) {
      case 'dashboard':
        return <Dashboard onCreateOrder={() => setRoute('create_order')} />;
      case 'create_order':
        return <CreateOrder onBack={() => setRoute('dashboard')} />;
      default:
        return (
          <div className="page-body">
            <div className="empty">
              <h4>Módulo en construcción</h4>
              <p>Esta sección estará disponible próximamente.</p>
            </div>
          </div>
        );
    }
  };

  return (
    <div className="app layout-sidebar">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">SL</div>
          <span className="brand-text">SmartLogix</span>
        </div>
        
        <nav>
          {NAV.map(sec => (
            <React.Fragment key={sec.section}>
              <div className="nav-section-label">{sec.section}</div>
              {sec.items.map(it => (
                <button 
                  key={it.id} 
                  className={`nav-item ${route === it.id || (route === 'create_order' && it.id === 'orders') ? 'active' : ''}`} 
                  onClick={() => setRoute(it.id as ViewStrategy)}
                >
                  {it.icon}
                  <span className="nav-item-text">{it.label}</span>
                </button>
              ))}
            </React.Fragment>
          ))}
        </nav>

        <div className="footer">
          <div className="avatar">AD</div>
          <div className="footer-text" style={{ flex: 1, minWidth: 0, fontSize: 12 }}>
            <div style={{ fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              Admin
            </div>
            <div style={{ color: 'var(--text-tertiary)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              SmartLogix
            </div>
          </div>
          <button className="btn btn-ghost btn-icon footer-text" onClick={clearSession} title="Cerrar sesión">
            <LogOut className="ico" />
          </button>
        </div>
      </aside>

      <main className="main">
        {renderContent()}
      </main>
    </div>
  );
}
