/* global React, Icons, fmtCLP, fmtDate, ORDER_STATUS, DELIVERY_STATUS, ROUTE_STATUS, useToast */
const { useState, useEffect, useMemo } = React;

// ======= DASHBOARD =======
function Dashboard() {
  const [data, setData] = useState({ orders: [], shipments: [], inventory: [], routes: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      SmartlogixAPI.listOrders(),
      SmartlogixAPI.listShipments(),
      SmartlogixAPI.listInventory(),
      SmartlogixAPI.listRoutes(),
    ]).then(([orders, shipments, inventory, routes]) => {
      setData({ orders, shipments, inventory, routes });
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const kpis = useMemo(() => ({
    pending: data.orders.filter(o => o.status === 'PENDIENTE').length,
    inTransit: data.shipments.filter(s => s.deliveryStatus === 'DISPATCHED' || s.deliveryStatus === 'ASSIGNED').length,
    deliveredToday: data.shipments.filter(s => s.deliveryStatus === 'DELIVERED').length,
    lowStock: data.inventory.filter(i => i.stockAvailable < 50).length,
    totalRevenue: data.orders.reduce((s, o) => s + Number(o.total || 0), 0),
  }), [data]);

  const last7Days = useMemo(() => {
    const days = Array.from({ length: 7 }).map((_, i) => {
      const d = new Date(); d.setDate(d.getDate() - (6 - i));
      return { label: d.toLocaleDateString('es-CL', { weekday: 'short' }).slice(0, 3), value: 0 };
    });
    days.forEach((d, i) => { d.value = 8 + Math.round(Math.sin(i * 1.2) * 6) + (i === 6 ? data.orders.length : i * 2); });
    return days;
  }, [data.orders]);
  const maxBar = Math.max(...last7Days.map(d => d.value), 1);

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Operación</div>
          <h1>Vista general</h1>
          <p>Resumen de tu actividad logística de hoy.</p>
        </div>
        <div className="page-actions">
          <button className="btn"><span>Últimos 7 días</span>{Icons.ChevronRight}</button>
          <button className="btn btn-accent">{Icons.Plus}<span>Nueva orden</span></button>
        </div>
      </div>
      <div className="page-body" style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <div className="kpi-grid">
          <KpiCard label="Órdenes pendientes" value={kpis.pending} delta="+3 hoy" trend="up" />
          <KpiCard label="Envíos en ruta" value={kpis.inTransit} delta="2 con retraso" trend="down" />
          <KpiCard label="Entregadas" value={kpis.deliveredToday} delta="+12% vs ayer" trend="up" />
          <KpiCard label="SKUs con stock bajo" value={kpis.lowStock} delta="Atención requerida" trend="down" />
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 16 }}>
          <div className="card">
            <div className="card-header">
              <div>
                <h3>Órdenes por día</h3>
                <div style={{ fontSize: 11, color: 'var(--text-tertiary)', marginTop: 2 }}>Últimos 7 días</div>
              </div>
              <span className="badge green"><span className="dot"/>+18% vs semana anterior</span>
            </div>
            <div className="card-body">
              <div className="bars">
                {last7Days.map((d, i) => (
                  <div key={i} className="bar" style={{ height: `${(d.value / maxBar) * 100}%` }} title={`${d.label}: ${d.value}`}/>
                ))}
              </div>
              <div className="bar-labels">
                {last7Days.map((d, i) => <span key={i}>{d.label}</span>)}
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header">
              <h3>Estado de envíos</h3>
            </div>
            <div className="card-body" style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {Object.entries(DELIVERY_STATUS).map(([k, v]) => {
                const count = data.shipments.filter(s => s.deliveryStatus === k).length;
                const pct = data.shipments.length ? (count / data.shipments.length) * 100 : 0;
                return (
                  <div key={k}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12, marginBottom: 4 }}>
                      <span style={{ color: 'var(--text-secondary)' }}>{v.label}</span>
                      <span className="mono">{count}</span>
                    </div>
                    <div style={{ height: 4, background: 'var(--bg-subtle)', borderRadius: 2, overflow: 'hidden' }}>
                      <div style={{ width: `${pct}%`, height: '100%', background: 'var(--accent)' }}/>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header">
            <h3>Actividad reciente</h3>
            <button className="btn btn-ghost btn-sm">Ver todo</button>
          </div>
          <div className="activity-list">
            {[
              { icon: Icons.Order, text: <><b>Nueva orden</b> <span className="mono" style={{ color: 'var(--text-tertiary)' }}>ord_5021</span> de Juan Pérez por {fmtCLP(24580)}</>, time: 'hace 2 min' },
              { icon: Icons.Truck, text: <><b>SLX-92841-AT</b> despachado desde CD Antofagasta hacia Diego Ramírez</>, time: 'hace 18 min' },
              { icon: Icons.Check, text: <><b>SLX-92840-RM</b> entregado a Verónica Lazo</>, time: 'hace 1 h' },
              { icon: Icons.Stock, text: <>Stock bajo: <b>SKU-B012</b> · 14 unidades en Sucursal Providencia</>, time: 'hace 2 h' },
              { icon: Icons.Route, text: <><b>Ruta rt_02</b> iniciada · 3 envíos asignados</>, time: 'hace 3 h' },
            ].map((a, i) => (
              <div className="activity-item" key={i}>
                <div className="activity-icon">{a.icon}</div>
                <div className="activity-content">
                  <div>{a.text}</div>
                  <div className="activity-time">{a.time}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  );
}

function KpiCard({ label, value, delta, trend }) {
  return (
    <div className="kpi">
      <div className="kpi-label">{label}</div>
      <div className="kpi-value">{value}</div>
      <div className={`kpi-delta ${trend}`}>
        {trend === 'up' ? Icons.ArrowUp : Icons.ArrowDown}
        <span>{delta}</span>
      </div>
    </div>
  );
}

// ======= PRODUCTS =======
function Products() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showNew, setShowNew] = useState(false);
  const toast = useToast();

  const reload = () => {
    setLoading(true);
    SmartlogixAPI.listProducts().then(d => { setItems(d); setLoading(false); }).catch(() => setLoading(false));
  };
  useEffect(reload, []);

  const filtered = items.filter(p =>
    !search || p.name.toLowerCase().includes(search.toLowerCase()) || p.sku.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Inventario</div>
          <h1>Productos</h1>
          <p>Catálogo de SKUs disponibles para tu operación.</p>
        </div>
        <div className="page-actions">
          <button className="btn">Importar CSV</button>
          <button className="btn btn-accent" onClick={() => setShowNew(true)}>{Icons.Plus}<span>Nuevo producto</span></button>
        </div>
      </div>
      <div className="page-body">
        <div className="toolbar">
          <div className="search">{Icons.Search}<input placeholder="Buscar por SKU o nombre…" value={search} onChange={e => setSearch(e.target.value)}/></div>
          <button className="btn">{Icons.Filter}<span>Estado</span></button>
          <div style={{ marginLeft: 'auto', fontSize: 12, color: 'var(--text-tertiary)' }}>{filtered.length} de {items.length}</div>
        </div>
        <div className="table-wrap">
          <table className="data">
            <thead><tr><th>SKU</th><th>Nombre</th><th className="num">Precio</th><th>Estado</th><th></th></tr></thead>
            <tbody>
              {loading && <tr><td colSpan="5" style={{ padding: 32, textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando…</td></tr>}
              {!loading && filtered.length === 0 && <tr><td colSpan="5"><div className="empty"><h4>Sin resultados</h4><p>Intenta con otra búsqueda o crea un producto.</p></div></td></tr>}
              {filtered.map(p => (
                <tr key={p.id}>
                  <td><span className="mono" style={{ fontSize: 12 }}>{p.sku}</span></td>
                  <td>{p.name}</td>
                  <td className="num mono">{fmtCLP(p.price)}</td>
                  <td><span className={`badge ${p.status === 'ACTIVE' ? 'green' : 'neutral'}`}><span className="dot"/>{p.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}</span></td>
                  <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                    <button className="btn btn-ghost btn-icon">{Icons.Pencil}</button>
                    <button className="btn btn-ghost btn-icon btn-danger">{Icons.Trash}</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {showNew && <NewProductModal onClose={() => setShowNew(false)} onCreated={() => { reload(); toast('Producto creado', 'success'); setShowNew(false); }}/>}
    </>
  );
}

function NewProductModal({ onClose, onCreated }) {
  const [data, setData] = useState({ sku: '', name: '', price: 0, status: 'ACTIVE' });
  const [saving, setSaving] = useState(false);
  const set = (k, v) => setData(d => ({ ...d, [k]: v }));
  const submit = async (e) => {
    e.preventDefault(); setSaving(true);
    try { await SmartlogixAPI.createProduct({ ...data, companyId: 'cmp_01' }); onCreated(); }
    finally { setSaving(false); }
  };
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" onClick={e => e.stopPropagation()} onSubmit={submit}>
        <div className="modal-header"><h2>Nuevo producto</h2><button type="button" className="btn btn-ghost btn-icon" onClick={onClose}>{Icons.X}</button></div>
        <div className="modal-body">
          <div className="field"><label>SKU</label><input className="input" required value={data.sku} onChange={e => set('sku', e.target.value)} placeholder="SKU-A001"/></div>
          <div className="field"><label>Nombre</label><input className="input" required value={data.name} onChange={e => set('name', e.target.value)}/></div>
          <div className="field"><label>Precio (CLP)</label><input className="input" type="number" required value={data.price} onChange={e => set('price', Number(e.target.value))}/></div>
          <div className="field"><label>Estado</label>
            <select className="select" value={data.status} onChange={e => set('status', e.target.value)}>
              <option value="ACTIVE">Activo</option>
              <option value="INACTIVE">Inactivo</option>
            </select>
          </div>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-accent" disabled={saving}>{saving ? 'Creando…' : 'Crear producto'}</button>
        </div>
      </form>
    </div>
  );
}

// ======= INVENTORY =======
function Inventory() {
  const [items, setItems] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    Promise.all([SmartlogixAPI.listInventory(), SmartlogixAPI.listWarehouses()])
      .then(([inv, wh]) => { setItems(inv); setWarehouses(wh); setLoading(false); });
  }, []);

  const filtered = items.filter(i => filter === 'all' || i.warehouseId === filter);

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Inventario</div>
          <h1>Stock por bodega</h1>
          <p>Niveles de inventario en tiempo real.</p>
        </div>
        <div className="page-actions">
          <button className="btn">Movimientos</button>
          <button className="btn btn-accent">{Icons.Plus}<span>Ajustar stock</span></button>
        </div>
      </div>
      <div className="page-body">
        <div className="toolbar">
          <select className="select" style={{ width: 'auto', minWidth: 200 }} value={filter} onChange={e => setFilter(e.target.value)}>
            <option value="all">Todas las bodegas</option>
            {warehouses.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
          </select>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <span className="badge green"><span className="dot"/>{filtered.filter(i => i.stockAvailable >= 50).length} OK</span>
            <span className="badge amber"><span className="dot"/>{filtered.filter(i => i.stockAvailable < 50 && i.stockAvailable > 0).length} bajo</span>
            <span className="badge red"><span className="dot"/>{filtered.filter(i => i.stockAvailable === 0).length} sin stock</span>
          </div>
        </div>
        <div className="table-wrap">
          <table className="data">
            <thead><tr><th>SKU</th><th>Producto</th><th>Bodega</th><th className="num">Disponible</th><th className="num">Reservado</th><th>Última act.</th></tr></thead>
            <tbody>
              {loading && <tr><td colSpan="6" style={{ padding: 32, textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando…</td></tr>}
              {filtered.map(i => {
                const lvl = i.stockAvailable === 0 ? 'red' : i.stockAvailable < 50 ? 'amber' : 'green';
                return (
                  <tr key={i.id}>
                    <td><span className="mono" style={{ fontSize: 12 }}>{i.sku}</span></td>
                    <td>{i.productName}</td>
                    <td><span style={{ color: 'var(--text-secondary)' }}>{i.warehouseName}</span></td>
                    <td className="num mono"><span className={`badge ${lvl}`} style={{ fontFamily: 'var(--font-mono)' }}>{i.stockAvailable}</span></td>
                    <td className="num mono" style={{ color: 'var(--text-tertiary)' }}>{i.stockReserved}</td>
                    <td style={{ color: 'var(--text-tertiary)', fontSize: 12 }}>{fmtDate(i.lastUpdated)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}

// ======= WAREHOUSES =======
function Warehouses() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => { SmartlogixAPI.listWarehouses().then(d => { setItems(d); setLoading(false); }); }, []);

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Inventario</div>
          <h1>Bodegas</h1>
          <p>Centros de distribución y puntos de venta.</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-accent">{Icons.Plus}<span>Nueva bodega</span></button>
        </div>
      </div>
      <div className="page-body">
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 12 }}>
          {loading && <div style={{ color: 'var(--text-tertiary)' }}>Cargando…</div>}
          {items.map(w => (
            <div key={w.id} className="card" style={{ padding: 16 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                <div style={{ width: 32, height: 32, borderRadius: 6, background: 'var(--accent-subtle)', color: 'var(--accent-text)', display: 'grid', placeItems: 'center' }}>{Icons.Warehouse}</div>
                <span className={`badge ${w.type === 'WAREHOUSE' ? 'blue' : 'neutral'}`}>{w.type === 'WAREHOUSE' ? 'CD' : 'Tienda'}</span>
              </div>
              <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 4 }}>{w.name}</div>
              <div style={{ fontSize: 12, color: 'var(--text-tertiary)', marginBottom: 12 }}>{w.locationAddress}</div>
              <div style={{ display: 'flex', gap: 16, fontSize: 12, paddingTop: 12, borderTop: '1px solid var(--border-subtle)' }}>
                <div><div style={{ color: 'var(--text-tertiary)', fontSize: 11 }}>SKUs</div><div style={{ fontWeight: 600 }}>{Math.floor(Math.random() * 50) + 20}</div></div>
                <div><div style={{ color: 'var(--text-tertiary)', fontSize: 11 }}>Stock total</div><div style={{ fontWeight: 600 }} className="mono">{(Math.random() * 5000).toFixed(0)}</div></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </>
  );
}

window.Dashboard = Dashboard;
window.Products = Products;
window.Inventory = Inventory;
window.Warehouses = Warehouses;
