/* global React */
const { useState, useEffect, createContext, useContext, useMemo } = React;

// ===== Toast / notification context =====
const ToastCtx = createContext(null);
function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const push = (msg, kind = 'info') => {
    const id = Math.random().toString(36).slice(2);
    setToasts(t => [...t, { id, msg, kind }]);
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 3500);
  };
  return (
    <ToastCtx.Provider value={push}>
      {children}
      <div className="toast-stack">
        {toasts.map(t => <div key={t.id} className={`toast ${t.kind}`}>{t.msg}</div>)}
      </div>
    </ToastCtx.Provider>
  );
}
const useToast = () => useContext(ToastCtx);

// ===== Auth context =====
const AuthCtx = createContext(null);
function AuthProvider({ children }) {
  const [session, setSession] = useState(() => SmartlogixAPI.session.get());
  const login = async (creds) => { const s = await SmartlogixAPI.login(creds); setSession(s); return s; };
  const register = async (data) => { const s = await SmartlogixAPI.register(data); setSession(s); return s; };
  const logout = () => { SmartlogixAPI.logout(); setSession(null); };
  return <AuthCtx.Provider value={{ session, login, register, logout }}>{children}</AuthCtx.Provider>;
}
const useAuth = () => useContext(AuthCtx);

// ===== Icons (line, 16×16) =====
const Icon = ({ d, size = 16 }) => (
  <svg className="ico" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
    {d}
  </svg>
);
const Icons = {
  Dashboard: <Icon d={<><rect x="3" y="3" width="7" height="9" rx="1"/><rect x="14" y="3" width="7" height="5" rx="1"/><rect x="14" y="12" width="7" height="9" rx="1"/><rect x="3" y="16" width="7" height="5" rx="1"/></>} />,
  Box: <Icon d={<><path d="M21 8 12 3 3 8v8l9 5 9-5V8z"/><path d="M3 8l9 5 9-5"/><path d="M12 13v8"/></>} />,
  Warehouse: <Icon d={<><path d="M3 21V9l9-6 9 6v12"/><path d="M9 21V12h6v9"/></>} />,
  Stock: <Icon d={<><path d="M20 7H4M20 12H4M20 17H4"/><circle cx="8" cy="7" r="1"/><circle cx="14" cy="12" r="1"/><circle cx="10" cy="17" r="1"/></>} />,
  Order: <Icon d={<><path d="M9 11l3 3 8-8"/><path d="M20 12v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h9"/></>} />,
  Route: <Icon d={<><circle cx="6" cy="19" r="2"/><circle cx="18" cy="5" r="2"/><path d="M8 19h7a4 4 0 0 0 0-8H9a4 4 0 0 1 0-8h7"/></>} />,
  Truck: <Icon d={<><path d="M14 18V6h-9v12h2"/><path d="M14 8h4l3 4v6h-2"/><circle cx="7" cy="18" r="2"/><circle cx="17" cy="18" r="2"/></>} />,
  Info: <Icon d={<><circle cx="12" cy="12" r="9"/><path d="M12 8h.01"/><path d="M11 12h1v4h1"/></>} />,
  Search: <Icon d={<><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></>} />,
  Plus: <Icon d={<><path d="M12 5v14M5 12h14"/></>} />,
  Settings: <Icon d={<><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1A1.7 1.7 0 0 0 9 19.4a1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1A1.7 1.7 0 0 0 4.6 9a1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/></>} />,
  Bell: <Icon d={<><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10 21a2 2 0 0 0 4 0"/></>} />,
  Logout: <Icon d={<><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/></>} />,
  Check: <Icon d={<path d="m5 12 5 5 9-11"/>} />,
  X: <Icon d={<><path d="M18 6 6 18M6 6l12 12"/></>} />,
  Pencil: <Icon d={<><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5z"/></>} />,
  Trash: <Icon d={<><path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></>} />,
  ArrowUp: <Icon d={<path d="M12 19V5M5 12l7-7 7 7"/>} />,
  ArrowDown: <Icon d={<path d="M12 5v14M5 12l7 7 7-7"/>} />,
  Filter: <Icon d={<path d="M22 3H2l8 9.5V19l4 2v-8.5z"/>} />,
  ChevronRight: <Icon d={<path d="m9 18 6-6-6-6"/>} />,
  ChevronLeft: <Icon d={<path d="m15 18-6-6 6-6"/>} />,
  Menu: <Icon d={<><path d="M3 12h18M3 6h18M3 18h18"/></>} />,
};

// ===== Helpers =====
const fmtCLP = (n) => '$' + (Number(n) || 0).toLocaleString('es-CL');
const fmtDate = (s) => {
  if (!s) return '—';
  const d = new Date(s);
  return d.toLocaleDateString('es-CL', { day: '2-digit', month: 'short' }) + ' ' + d.toLocaleTimeString('es-CL', { hour: '2-digit', minute: '2-digit' });
};
const fmtDateOnly = (s) => s ? new Date(s).toLocaleDateString('es-CL', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
const initials = (name) => name.split(' ').map(n => n[0]).slice(0, 2).join('').toUpperCase();

const ORDER_STATUS = {
  PENDIENTE: { label: 'Pendiente', cls: 'amber' },
  APROBADO: { label: 'Aprobado', cls: 'blue' },
  RECHAZADO: { label: 'Rechazado', cls: 'red' },
  ENVIADO: { label: 'Enviado', cls: 'green' },
  ENTREGADO: { label: 'Entregado', cls: 'green' },
  CANCELADO: { label: 'Cancelado', cls: 'red' },
};
// Forward flow (each → next). Orders advance only up to ENVIADO from the orders UI;
// the final ENTREGADO is set automatically when the linked shipment is delivered.
const ORDER_FLOW = ['PENDIENTE', 'APROBADO', 'ENVIADO'];
// Shipment flow (forward): PENDING → ASSIGNED → DISPATCHED → DELIVERED
const SHIPMENT_FLOW = ['PENDING', 'ASSIGNED', 'DISPATCHED', 'DELIVERED'];
const DELIVERY_STATUS = {
  PENDING: { label: 'Pendiente', cls: 'neutral' },
  ASSIGNED: { label: 'Asignado', cls: 'blue' },
  DISPATCHED: { label: 'En ruta', cls: 'amber' },
  DELIVERED: { label: 'Entregado', cls: 'green' },
  FAILED: { label: 'Fallido', cls: 'red' },
  CANCELLED: { label: 'Cancelado', cls: 'red' },
};
const ROUTE_STATUS = {
  PLANNED: { label: 'Planificada', cls: 'neutral' },
  IN_PROGRESS: { label: 'En curso', cls: 'amber' },
  COMPLETED: { label: 'Completada', cls: 'green' },
  CANCELLED: { label: 'Cancelada', cls: 'red' },
};

Object.assign(window, { ToastProvider, useToast, AuthProvider, useAuth, Icons, fmtCLP, fmtDate, fmtDateOnly, initials, ORDER_STATUS, ORDER_FLOW, SHIPMENT_FLOW, DELIVERY_STATUS, ROUTE_STATUS });
