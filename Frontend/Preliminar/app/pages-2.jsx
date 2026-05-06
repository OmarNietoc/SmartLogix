/* global React, Icons, fmtCLP, fmtDate, fmtDateOnly, ORDER_STATUS, ORDER_FLOW, SHIPMENT_FLOW, DELIVERY_STATUS, ROUTE_STATUS, useToast */
const { useState, useEffect, useMemo } = React;

// ======= ORDERS =======
function Orders() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('all');
  const [search, setSearch] = useState('');
  const [selected, setSelected] = useState(null);
  const [showNew, setShowNew] = useState(false);
  const toast = useToast();

  const reload = () => {
    setLoading(true);
    SmartlogixAPI.listOrders().then(d => { setItems(d); setLoading(false); });
  };
  useEffect(reload, []);

  const filtered = items.filter(o =>
    (statusFilter === 'all' || o.status === statusFilter) &&
    (!search || o.id.includes(search.toLowerCase()) || o.customerName.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Operación</div>
          <h1>Órdenes</h1>
          <p>Gestiona las órdenes desde su creación hasta la entrega.</p>
        </div>
        <div className="page-actions">
          <button className="btn">Exportar</button>
          <button className="btn btn-accent" onClick={() => setShowNew(true)}>{Icons.Plus}<span>Nueva orden</span></button>
        </div>
      </div>
      <div className="page-tabs">
        {[['all', 'Todas'], ...Object.entries(ORDER_STATUS).map(([k, v]) => [k, v.label])].map(([k, l]) => (
          <button key={k} className={`page-tab ${statusFilter === k ? 'active' : ''}`} onClick={() => setStatusFilter(k)}>
            {l} <span style={{ color: 'var(--text-tertiary)', marginLeft: 4 }} className="mono">{k === 'all' ? items.length : items.filter(o => o.status === k).length}</span>
          </button>
        ))}
      </div>
      <div className="page-body">
        <div className="toolbar">
          <div className="search">{Icons.Search}<input placeholder="Buscar por ID o cliente…" value={search} onChange={e => setSearch(e.target.value)}/></div>
        </div>
        <div className="table-wrap">
          <table className="data">
            <thead><tr><th>ID</th><th>Cliente</th><th>Dirección</th><th className="num">Items</th><th className="num">Total</th><th>Estado</th><th>Fecha</th></tr></thead>
            <tbody>
              {loading && <tr><td colSpan="7" style={{ padding: 32, textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando…</td></tr>}
              {filtered.map(o => (
                <tr key={o.id} onClick={() => setSelected(o)} className={selected?.id === o.id ? 'selected' : ''}>
                  <td><span className="mono" style={{ fontSize: 12 }}>{o.id}</span></td>
                  <td>{o.customerName}<div style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{o.customerEmail}</div></td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: 12, maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{o.shippingAddress}</td>
                  <td className="num mono">{o.items?.length || 0}</td>
                  <td className="num mono" style={{ fontWeight: 500 }}>{fmtCLP(o.total)}</td>
                  <td><span className={`badge ${ORDER_STATUS[o.status]?.cls || 'neutral'}`}><span className="dot"/>{ORDER_STATUS[o.status]?.label || o.status}</span></td>
                  <td style={{ color: 'var(--text-tertiary)', fontSize: 12 }}>{fmtDate(o.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {selected && <OrderDrawer order={selected} onClose={() => setSelected(null)} onUpdate={() => { reload(); }}/>}
      {showNew && <NewOrderModal onClose={() => setShowNew(false)} onCreated={() => { reload(); toast('Orden creada', 'success'); setShowNew(false); }}/>}
    </>
  );
}

function OrderDrawer({ order, onClose, onUpdate }) {
  const toast = useToast();
  const [updating, setUpdating] = useState(false);

  const changeStatus = async (newStatus) => {
    setUpdating(true);
    try { await SmartlogixAPI.updateOrderStatus(order.id, newStatus); toast(`Estado actualizado: ${ORDER_STATUS[newStatus].label}`, 'success'); onUpdate(); onClose(); }
    finally { setUpdating(false); }
  };

  // Determine prev/next using forward flow. Special states (RECHAZADO/CANCELADO/ENTREGADO) are terminal.
  const idx = ORDER_FLOW.indexOf(order.status);
  const prev = idx > 0 ? ORDER_FLOW[idx - 1] : null;
  const next = idx >= 0 && idx < ORDER_FLOW.length - 1 ? ORDER_FLOW[idx + 1] : null;
  const isTerminal = order.status === 'RECHAZADO' || order.status === 'CANCELADO' || order.status === 'ENTREGADO';
  // ENVIADO is terminal from the orders side — final ENTREGADO comes from the shipments module.
  const isShippedTerminal = order.status === 'ENVIADO';
  // Cancel allowed when status != PENDIENTE && status != RECHAZADO (and not already terminal)
  const canCancel = order.status !== 'PENDIENTE' && order.status !== 'RECHAZADO' && order.status !== 'CANCELADO' && order.status !== 'ENTREGADO';
  // Reject only from PENDIENTE
  const canReject = order.status === 'PENDIENTE';

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 680 }} onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-tertiary)' }} className="mono">{order.id}</div>
            <h2>Orden de {order.customerName}</h2>
          </div>
          <button className="btn btn-ghost btn-icon" onClick={onClose}>{Icons.X}</button>
        </div>
        <div className="modal-body">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className={`badge ${ORDER_STATUS[order.status]?.cls}`}><span className="dot"/>{ORDER_STATUS[order.status]?.label}</span>
            <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>Creada {fmtDate(order.createdAt)}</div>
          </div>

          {/* Prev / Current / Next flow */}
          {!isTerminal && (
            <div className="status-flow">
              <div style={{ fontSize: 11, fontWeight: 500, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 8 }}>Cambiar estado</div>
              <div className="status-flow-row">
                {prev ? (
                  <button className="status-pill prev" disabled={updating} onClick={() => changeStatus(prev)} title={`Volver a ${ORDER_STATUS[prev].label}`}>
                    <span className="status-pill-arrow">←</span>
                    <span className="status-pill-label">
                      <span className="status-pill-hint">Anterior</span>
                      <span>{ORDER_STATUS[prev].label}</span>
                    </span>
                  </button>
                ) : <div className="status-pill empty"/>}

                <div className="status-pill current">
                  <span className={`badge ${ORDER_STATUS[order.status]?.cls}`}><span className="dot"/>{ORDER_STATUS[order.status]?.label}</span>
                  <span className="status-pill-hint" style={{ marginTop: 4 }}>Actual</span>
                </div>

                {next ? (
                  <button className="status-pill next" disabled={updating} onClick={() => changeStatus(next)} title={`Avanzar a ${ORDER_STATUS[next].label}`}>
                    <span className="status-pill-label">
                      <span className="status-pill-hint">Siguiente</span>
                      <span>{ORDER_STATUS[next].label}</span>
                    </span>
                    <span className="status-pill-arrow">→</span>
                  </button>
                ) : isShippedTerminal ? (
                  <div className="status-pill next-shipping" title="El estado final se actualiza desde Envíos">
                    <span className="status-pill-label">
                      <span className="status-pill-hint">Continúa en</span>
                      <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>{Icons.Truck} Envíos</span>
                    </span>
                    <span className="status-pill-arrow">→</span>
                  </div>
                ) : <div className="status-pill empty"/>}
              </div>
              {isShippedTerminal && (
                <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginTop: 10, padding: '8px 10px', background: 'var(--info-subtle)', color: 'var(--info-text)', borderRadius: 'var(--r-sm)', display: 'flex', gap: 8, alignItems: 'flex-start' }}>
                  <span style={{ flexShrink: 0, marginTop: 1 }}>{Icons.Info}</span>
                  <span>Esta orden ya está en ruta. El estado <b>Entregado</b> se asignará automáticamente cuando el envío sea marcado como entregado en el módulo de Envíos.</span>
                </div>
              )}
            </div>
          )}

          <dl className="kv">
            <dt>Cliente</dt><dd>{order.customerName}</dd>
            <dt>Email</dt><dd>{order.customerEmail}</dd>
            <dt>Dirección</dt><dd>{order.shippingAddress}</dd>
          </dl>
          <div>
            <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--text-secondary)', marginBottom: 8 }}>Productos ({order.items?.length || 0})</div>
            <div className="table-wrap">
              <table className="data">
                <thead><tr><th>Producto</th><th className="num">Cant.</th><th className="num">Precio</th><th className="num">Total</th></tr></thead>
                <tbody>
                  {order.items?.map(it => (
                    <tr key={it.id} style={{ cursor: 'default' }}>
                      <td>{it.productName}</td>
                      <td className="num mono">{it.quantity}</td>
                      <td className="num mono">{fmtCLP(it.price)}</td>
                      <td className="num mono" style={{ fontWeight: 500 }}>{fmtCLP(it.price * it.quantity)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '12px 4px', fontSize: 14, fontWeight: 600 }}>
              Total: <span className="mono" style={{ marginLeft: 8 }}>{fmtCLP(order.total)}</span>
            </div>
          </div>
        </div>
        {(canCancel || canReject) && (
          <div className="modal-footer">
            {canReject && (
              <button className="btn btn-danger" disabled={updating} onClick={() => changeStatus('RECHAZADO')}>
                Rechazar orden
              </button>
            )}
            {canCancel && (
              <button className="btn btn-danger" disabled={updating} onClick={() => { if (confirm('¿Cancelar esta orden? Esta acción no se puede deshacer.')) changeStatus('CANCELADO'); }}>
                Cancelar orden
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function NewOrderModal({ onClose, onCreated }) {
  const [data, setData] = useState({ customerName: '', customerEmail: '', shippingAddress: '' });
  const [items, setItems] = useState([{ productId: '', warehouseId: '', productName: '', quantity: 1, price: 0 }]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [saving, setSaving] = useState(false);
  const set = (k, v) => setData(d => ({ ...d, [k]: v }));

  useEffect(() => {
    Promise.all([SmartlogixAPI.listProducts(), SmartlogixAPI.listWarehouses()]).then(([p, w]) => { setProducts(p); setWarehouses(w); });
  }, []);

  const updateItem = (i, k, v) => {
    setItems(its => its.map((it, idx) => idx === i ? { ...it, [k]: v } : it));
  };
  const total = items.reduce((s, i) => s + (Number(i.price) || 0) * (Number(i.quantity) || 0), 0);

  const submit = async (e) => {
    e.preventDefault(); setSaving(true);
    try {
      await SmartlogixAPI.createOrder({ ...data, items: items.filter(i => i.productId) });
      onCreated();
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" style={{ maxWidth: 720 }} onClick={e => e.stopPropagation()} onSubmit={submit}>
        <div className="modal-header"><h2>Nueva orden</h2><button type="button" className="btn btn-ghost btn-icon" onClick={onClose}>{Icons.X}</button></div>
        <div className="modal-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="field"><label>Nombre cliente</label><input className="input" required value={data.customerName} onChange={e => set('customerName', e.target.value)}/></div>
            <div className="field"><label>Email cliente</label><input className="input" type="email" required value={data.customerEmail} onChange={e => set('customerEmail', e.target.value)}/></div>
          </div>
          <div className="field"><label>Dirección de envío</label><input className="input" required value={data.shippingAddress} onChange={e => set('shippingAddress', e.target.value)}/></div>
          <div>
            <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--text-secondary)', marginBottom: 8 }}>Items</div>
            {items.map((it, i) => (
              <div key={i} style={{ display: 'grid', gridTemplateColumns: '2fr 1.2fr 60px 90px 28px', gap: 6, marginBottom: 6, alignItems: 'center' }}>
                <select className="select" value={it.productId} onChange={e => {
                  const p = products.find(p => p.id === e.target.value);
                  updateItem(i, 'productId', e.target.value);
                  if (p) { updateItem(i, 'productName', p.name); updateItem(i, 'price', p.price); }
                }}>
                  <option value="">Selecciona producto…</option>
                  {products.map(p => <option key={p.id} value={p.id}>{p.sku} — {p.name}</option>)}
                </select>
                <select className="select" value={it.warehouseId} onChange={e => updateItem(i, 'warehouseId', e.target.value)}>
                  <option value="">Bodega…</option>
                  {warehouses.map(w => <option key={w.id} value={w.id}>{w.name}</option>)}
                </select>
                <input className="input" type="number" min="1" value={it.quantity} onChange={e => updateItem(i, 'quantity', Number(e.target.value))}/>
                <input className="input mono" type="number" value={it.price} onChange={e => updateItem(i, 'price', Number(e.target.value))}/>
                <button type="button" className="btn btn-ghost btn-icon btn-danger" onClick={() => setItems(its => its.filter((_, idx) => idx !== i))}>{Icons.X}</button>
              </div>
            ))}
            <button type="button" className="btn btn-sm" onClick={() => setItems(its => [...its, { productId: '', warehouseId: '', productName: '', quantity: 1, price: 0 }])}>{Icons.Plus}<span>Añadir item</span></button>
          </div>
          <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '8px 0', fontSize: 14, fontWeight: 600, borderTop: '1px solid var(--border)', marginTop: 8 }}>
            Total: <span className="mono" style={{ marginLeft: 8 }}>{fmtCLP(total)}</span>
          </div>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-accent" disabled={saving}>{saving ? 'Creando…' : 'Crear orden'}</button>
        </div>
      </form>
    </div>
  );
}

// ======= SHIPMENTS / TRACKING =======
function Shipments() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('all');
  const [selected, setSelected] = useState(null);
  const [tracking, setTracking] = useState('');

  const reload = () => SmartlogixAPI.listShipments().then(d => { setItems(d); setLoading(false); });
  useEffect(() => { reload(); }, []);

  const filtered = items.filter(s => statusFilter === 'all' || s.deliveryStatus === statusFilter);

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Logística</div>
          <h1>Envíos</h1>
          <p>Seguimiento de entregas en tiempo real.</p>
        </div>
        <div className="page-actions">
          <div className="search" style={{ minWidth: 260 }}>{Icons.Search}<input placeholder="Buscar tracking SLX-…" value={tracking} onChange={e => setTracking(e.target.value)}/></div>
        </div>
      </div>
      <div className="page-tabs">
        {[['all', 'Todos'], ...Object.entries(DELIVERY_STATUS).map(([k, v]) => [k, v.label])].map(([k, l]) => (
          <button key={k} className={`page-tab ${statusFilter === k ? 'active' : ''}`} onClick={() => setStatusFilter(k)}>
            {l} <span style={{ color: 'var(--text-tertiary)', marginLeft: 4 }} className="mono">{k === 'all' ? items.length : items.filter(s => s.deliveryStatus === k).length}</span>
          </button>
        ))}
      </div>
      <div className="page-body">
        <div className="table-wrap">
          <table className="data">
            <thead><tr><th>Tracking</th><th>Cliente</th><th>Dirección</th><th>Estado</th><th>ETA</th><th>Entregado</th></tr></thead>
            <tbody>
              {loading && <tr><td colSpan="6" style={{ padding: 32, textAlign: 'center', color: 'var(--text-tertiary)' }}>Cargando…</td></tr>}
              {filtered.filter(s => !tracking || s.trackingNumber.toLowerCase().includes(tracking.toLowerCase())).map(s => (
                <tr key={s.id} onClick={() => setSelected(s)} className={selected?.id === s.id ? 'selected' : ''}>
                  <td><span className="mono" style={{ fontSize: 12, fontWeight: 500 }}>{s.trackingNumber}</span></td>
                  <td>{s.customerName}</td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: 12, maxWidth: 240, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{s.shippingAddress}</td>
                  <td><span className={`badge ${DELIVERY_STATUS[s.deliveryStatus]?.cls}`}><span className="dot"/>{DELIVERY_STATUS[s.deliveryStatus]?.label}</span></td>
                  <td style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{fmtDate(s.estimatedDelivery)}</td>
                  <td style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{fmtDate(s.actualDelivery)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {selected && <ShipmentDrawer shipment={selected} onClose={() => setSelected(null)} onUpdate={reload}/>}
    </>
  );
}

function ShipmentDrawer({ shipment, onClose, onUpdate }) {
  const toast = useToast();
  const [updating, setUpdating] = useState(false);
  const idx = SHIPMENT_FLOW.indexOf(shipment.deliveryStatus);
  const prev = idx > 0 ? SHIPMENT_FLOW[idx - 1] : null;
  const next = idx >= 0 && idx < SHIPMENT_FLOW.length - 1 ? SHIPMENT_FLOW[idx + 1] : null;
  const failed = shipment.deliveryStatus === 'FAILED' || shipment.deliveryStatus === 'CANCELLED';
  const isDelivered = shipment.deliveryStatus === 'DELIVERED';
  const canFail = !failed && !isDelivered;

  const changeStatus = async (newStatus) => {
    setUpdating(true);
    try {
      await SmartlogixAPI.updateShipmentStatus(shipment.id, newStatus);
      // When shipment is delivered, propagate ENTREGADO to the linked order.
      if (newStatus === 'DELIVERED' && shipment.orderId) {
        try { await SmartlogixAPI.updateOrderStatus(shipment.orderId, 'ENTREGADO'); } catch (e) { /* non-blocking */ }
      }
      toast(`Envío actualizado a ${DELIVERY_STATUS[newStatus].label}`, 'success');
      onUpdate(); onClose();
    } finally { setUpdating(false); }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 680 }} onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div style={{ fontSize: 11, color: 'var(--text-tertiary)' }} className="mono">{shipment.trackingNumber}</div>
            <h2>Envío a {shipment.customerName}</h2>
          </div>
          <button className="btn btn-ghost btn-icon" onClick={onClose}>{Icons.X}</button>
        </div>
        <div className="modal-body">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className={`badge ${DELIVERY_STATUS[shipment.deliveryStatus]?.cls}`}><span className="dot"/>{DELIVERY_STATUS[shipment.deliveryStatus]?.label}</span>
            <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>ETA {fmtDate(shipment.estimatedDelivery)}</div>
          </div>

          {!failed && !isDelivered && (
            <div className="status-flow">
              <div style={{ fontSize: 11, fontWeight: 500, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 8 }}>Cambiar estado</div>
              <div className="status-flow-row">
                {prev ? (
                  <button className="status-pill prev" disabled={updating} onClick={() => changeStatus(prev)}>
                    <span className="status-pill-arrow">←</span>
                    <span className="status-pill-label">
                      <span className="status-pill-hint">Anterior</span>
                      <span>{DELIVERY_STATUS[prev].label}</span>
                    </span>
                  </button>
                ) : <div className="status-pill empty"/>}
                <div className="status-pill current">
                  <span className={`badge ${DELIVERY_STATUS[shipment.deliveryStatus]?.cls}`}><span className="dot"/>{DELIVERY_STATUS[shipment.deliveryStatus]?.label}</span>
                  <span className="status-pill-hint" style={{ marginTop: 4 }}>Actual</span>
                </div>
                {next ? (
                  <button className="status-pill next" disabled={updating} onClick={() => changeStatus(next)}>
                    <span className="status-pill-label">
                      <span className="status-pill-hint">Siguiente</span>
                      <span>{DELIVERY_STATUS[next].label}</span>
                    </span>
                    <span className="status-pill-arrow">→</span>
                  </button>
                ) : <div className="status-pill empty"/>}
              </div>
              {next === 'DELIVERED' && (
                <div style={{ fontSize: 12, marginTop: 10, padding: '8px 10px', background: 'var(--info-subtle)', color: 'var(--info-text)', borderRadius: 'var(--r-sm)', display: 'flex', gap: 8, alignItems: 'flex-start' }}>
                  <span style={{ flexShrink: 0, marginTop: 1 }}>{Icons.Info}</span>
                  <span>Al marcar como <b>Entregado</b> se actualizará automáticamente la orden <span className="mono">{shipment.orderId}</span> al estado <b>Entregado</b>.</span>
                </div>
              )}
            </div>
          )}

          <dl className="kv">
            <dt>Orden</dt><dd className="mono" style={{ fontSize: 12 }}>{shipment.orderId}</dd>
            <dt>Ruta</dt><dd className="mono" style={{ fontSize: 12 }}>{shipment.routeId || '— sin asignar'}</dd>
            <dt>Cliente</dt><dd>{shipment.customerName}<div style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{shipment.customerEmail}</div></dd>
            <dt>Dirección</dt><dd>{shipment.shippingAddress}</dd>
            <dt>Coordenadas</dt><dd className="mono" style={{ fontSize: 12 }}>{shipment.latitude}, {shipment.longitude}</dd>
            <dt>ETA</dt><dd>{fmtDate(shipment.estimatedDelivery)}</dd>
            <dt>Entregado</dt><dd>{fmtDate(shipment.actualDelivery)}</dd>
          </dl>
          <div style={{ height: 140, background: 'var(--bg-sunken)', border: '1px solid var(--border)', borderRadius: 8, display: 'grid', placeItems: 'center', color: 'var(--text-tertiary)', fontSize: 12, position: 'relative', overflow: 'hidden' }}>
            <div style={{ position: 'absolute', inset: 0, backgroundImage: 'repeating-linear-gradient(45deg, transparent, transparent 8px, rgba(0,0,0,0.02) 8px, rgba(0,0,0,0.02) 9px)' }}/>
            <span className="mono">[ mapa de tracking — integración pendiente ]</span>
          </div>
        </div>
        {canFail && (
          <div className="modal-footer">
            <button className="btn btn-danger" disabled={updating} onClick={() => { if (confirm('¿Marcar este envío como fallido?')) changeStatus('FAILED'); }}>Marcar fallido</button>
            <button className="btn btn-danger" disabled={updating} onClick={() => { if (confirm('¿Cancelar este envío?')) changeStatus('CANCELLED'); }}>Cancelar envío</button>
          </div>
        )}
      </div>
    </div>
  );
}

// ======= ROUTES =======
function Routes() {
  const [items, setItems] = useState([]);
  const [shipments, setShipments] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showNew, setShowNew] = useState(false);
  const [selected, setSelected] = useState(null);
  const toast = useToast();

  const reload = () => {
    setLoading(true);
    Promise.all([
      SmartlogixAPI.listRoutes(),
      SmartlogixAPI.listShipments(),
      SmartlogixAPI.listWarehouses(),
    ]).then(([r, s, w]) => { setItems(r); setShipments(s); setWarehouses(w); setLoading(false); });
  };
  useEffect(reload, []);

  const shipmentsByRoute = useMemo(() => {
    const map = {};
    shipments.forEach(s => { if (s.routeId) (map[s.routeId] = map[s.routeId] || []).push(s); });
    return map;
  }, [shipments]);

  return (
    <>
      <div className="page-header">
        <div className="page-title-group">
          <div className="crumb">Logística</div>
          <h1>Rutas</h1>
          <p>Planifica y optimiza recorridos de entrega.</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-accent" onClick={() => setShowNew(true)}>{Icons.Plus}<span>Nueva ruta</span></button>
        </div>
      </div>
      <div className="page-body">
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: 12 }}>
          {loading && <div style={{ color: 'var(--text-tertiary)' }}>Cargando…</div>}
          {!loading && items.length === 0 && (
            <div className="empty" style={{ gridColumn: '1 / -1' }}>
              <div style={{ fontSize: 14, fontWeight: 500, marginBottom: 4 }}>Sin rutas planificadas</div>
              <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>Crea una ruta para asignar envíos pendientes.</div>
            </div>
          )}
          {items.map(r => {
            const assigned = shipmentsByRoute[r.id] || [];
            return (
              <div key={r.id} className="card route-card" style={{ padding: 16, cursor: 'pointer' }} onClick={() => setSelected({ route: r, shipments: assigned })}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 12 }}>
                  <div>
                    <div className="mono" style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{r.id}</div>
                    <div style={{ fontWeight: 600, fontSize: 14, marginTop: 2 }}>{fmtDateOnly(r.routeDate)}</div>
                  </div>
                  <span className={`badge ${ROUTE_STATUS[r.status]?.cls}`}><span className="dot"/>{ROUTE_STATUS[r.status]?.label}</span>
                </div>
                <div style={{ fontSize: 12, color: 'var(--text-secondary)', marginBottom: 4 }}>Origen</div>
                <div style={{ fontSize: 13, marginBottom: 12, lineHeight: 1.4 }}>{r.originAddress}</div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, fontSize: 12, paddingTop: 12, borderTop: '1px solid var(--border-subtle)' }}>
                  <div>
                    <div style={{ color: 'var(--text-tertiary)', fontSize: 11 }}>Carrier</div>
                    <div className="mono" style={{ fontSize: 12 }}>{r.carrierId}</div>
                  </div>
                  <div>
                    <div style={{ color: 'var(--text-tertiary)', fontSize: 11 }}>Envíos</div>
                    <div style={{ fontSize: 12, fontWeight: 600 }}>{assigned.length}</div>
                  </div>
                  <div style={{ marginLeft: 'auto' }}>
                    <button className="btn btn-sm" onClick={e => { e.stopPropagation(); setSelected({ route: r, shipments: assigned }); }}>Ver detalle</button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
      {showNew && <NewRouteModal shipments={shipments.filter(s => !s.routeId && s.deliveryStatus !== 'DELIVERED' && s.deliveryStatus !== 'CANCELLED')} warehouses={warehouses} onClose={() => setShowNew(false)} onCreated={() => { reload(); toast('Ruta creada con envíos asignados', 'success'); setShowNew(false); }}/>}
      {selected && <RouteDetailModal route={selected.route} shipments={selected.shipments} onClose={() => setSelected(null)}/>}
    </>
  );
}

function NewRouteModal({ shipments, warehouses, onClose, onCreated }) {
  const [data, setData] = useState({
    originAddress: warehouses[0]?.locationAddress ? `${warehouses[0].name} — ${warehouses[0].locationAddress}` : '',
    routeDate: new Date().toISOString().slice(0, 10),
    carrierId: 'crr_local',
  });
  const [selectedShipments, setSelectedShipments] = useState([]);
  const [saving, setSaving] = useState(false);
  const set = (k, v) => setData(d => ({ ...d, [k]: v }));

  const toggle = (id) => setSelectedShipments(s => s.includes(id) ? s.filter(x => x !== id) : [...s, id]);
  const selectAll = () => setSelectedShipments(shipments.map(s => s.id));
  const clearAll = () => setSelectedShipments([]);

  const submit = async (e) => {
    e.preventDefault();
    if (selectedShipments.length === 0) {
      if (!confirm('No has seleccionado envíos. ¿Crear una ruta vacía igualmente?')) return;
    }
    setSaving(true);
    try {
      const route = await SmartlogixAPI.createRoute({ ...data, optimizedPathJson: '[]' });
      // Assign each selected shipment to the new route
      await Promise.all(selectedShipments.map(id => SmartlogixAPI.assignShipmentToRoute(id, route.id)));
      onCreated();
    } finally { setSaving(false); }
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" style={{ maxWidth: 760 }} onClick={e => e.stopPropagation()} onSubmit={submit}>
        <div className="modal-header"><h2>Nueva ruta</h2><button type="button" className="btn btn-ghost btn-icon" onClick={onClose}>{Icons.X}</button></div>
        <div className="modal-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div className="field">
              <label>Bodega de origen</label>
              <select className="select" value={data.originAddress} onChange={e => set('originAddress', e.target.value)} required>
                <option value="">Selecciona…</option>
                {warehouses.map(w => <option key={w.id} value={`${w.name} — ${w.locationAddress}`}>{w.name}</option>)}
              </select>
            </div>
            <div className="field">
              <label>Fecha de ruta</label>
              <input className="input" type="date" required value={data.routeDate} onChange={e => set('routeDate', e.target.value)}/>
            </div>
          </div>
          <div className="field">
            <label>Carrier</label>
            <select className="select" value={data.carrierId} onChange={e => set('carrierId', e.target.value)}>
              <option value="crr_local">Flota propia</option>
              <option value="crr_dhl">DHL Express</option>
              <option value="crr_chilexp">Chilexpress</option>
              <option value="crr_starken">Starken</option>
            </select>
          </div>

          <div style={{ marginTop: 8 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
              <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--text-secondary)' }}>
                Asignar envíos pendientes
                <span className="mono" style={{ marginLeft: 8, color: 'var(--text-tertiary)' }}>
                  {selectedShipments.length} de {shipments.length} seleccionados
                </span>
              </div>
              <div style={{ display: 'flex', gap: 6 }}>
                <button type="button" className="btn btn-sm btn-ghost" onClick={selectAll} disabled={shipments.length === 0}>Todos</button>
                <button type="button" className="btn btn-sm btn-ghost" onClick={clearAll} disabled={selectedShipments.length === 0}>Limpiar</button>
              </div>
            </div>

            {shipments.length === 0 ? (
              <div className="empty" style={{ padding: '20px 16px' }}>
                <div style={{ fontSize: 13, fontWeight: 500, marginBottom: 4 }}>No hay envíos sin asignar</div>
                <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }}>Todos los envíos pendientes ya están en una ruta.</div>
              </div>
            ) : (
              <div className="shipment-picker">
                {shipments.map(s => {
                  const checked = selectedShipments.includes(s.id);
                  return (
                    <label key={s.id} className={`shipment-row ${checked ? 'selected' : ''}`}>
                      <input type="checkbox" checked={checked} onChange={() => toggle(s.id)}/>
                      <div className="shipment-row-main">
                        <div className="shipment-row-top">
                          <span className="mono" style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{s.trackingNumber}</span>
                          <span className={`badge ${DELIVERY_STATUS[s.deliveryStatus]?.cls}`} style={{ fontSize: 10, padding: '2px 6px' }}>
                            <span className="dot"/>{DELIVERY_STATUS[s.deliveryStatus]?.label}
                          </span>
                        </div>
                        <div style={{ fontSize: 13, fontWeight: 500 }}>{s.customerName}</div>
                        <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{s.shippingAddress}</div>
                      </div>
                    </label>
                  );
                })}
              </div>
            )}
          </div>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-accent" disabled={saving}>
            {saving ? 'Creando…' : `Crear ruta${selectedShipments.length ? ` con ${selectedShipments.length} envío${selectedShipments.length === 1 ? '' : 's'}` : ''}`}
          </button>
        </div>
      </form>
    </div>
  );
}

function RouteDetailModal({ route, shipments, onClose }) {
  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" style={{ maxWidth: 640 }} onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <div className="mono" style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{route.id}</div>
            <h2>Ruta del {fmtDateOnly(route.routeDate)}</h2>
          </div>
          <button className="btn btn-ghost btn-icon" onClick={onClose}>{Icons.X}</button>
        </div>
        <div className="modal-body">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span className={`badge ${ROUTE_STATUS[route.status]?.cls}`}><span className="dot"/>{ROUTE_STATUS[route.status]?.label}</span>
            <div style={{ fontSize: 12, color: 'var(--text-tertiary)' }} className="mono">{route.carrierId}</div>
          </div>
          <dl className="kv">
            <dt>Origen</dt><dd>{route.originAddress}</dd>
            <dt>Fecha</dt><dd>{fmtDateOnly(route.routeDate)}</dd>
            <dt>Envíos</dt><dd>{shipments.length}</dd>
          </dl>
          <div>
            <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--text-secondary)', marginBottom: 8 }}>Paradas en esta ruta</div>
            {shipments.length === 0 ? (
              <div className="empty" style={{ padding: 16 }}>
                <div style={{ fontSize: 13, color: 'var(--text-tertiary)' }}>No hay envíos asignados a esta ruta.</div>
              </div>
            ) : (
              <ol className="route-stops">
                {shipments.map((s, i) => (
                  <li key={s.id}>
                    <div className="route-stop-num mono">{i + 1}</div>
                    <div className="route-stop-body">
                      <div className="route-stop-top">
                        <span className="mono" style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>{s.trackingNumber}</span>
                        <span className={`badge ${DELIVERY_STATUS[s.deliveryStatus]?.cls}`} style={{ fontSize: 10, padding: '2px 6px' }}>
                          <span className="dot"/>{DELIVERY_STATUS[s.deliveryStatus]?.label}
                        </span>
                      </div>
                      <div style={{ fontSize: 13, fontWeight: 500 }}>{s.customerName}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{s.shippingAddress}</div>
                    </div>
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

window.Orders = Orders;
window.Shipments = Shipments;
window.Routes = Routes;
