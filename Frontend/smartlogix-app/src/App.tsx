import React, { useEffect, useMemo, useState } from 'react';
import {
  BarChart3,
  Boxes,
  ClipboardList,
  Edit2,
  Eye,
  LayoutDashboard,
  LogOut,
  Map,
  Package,
  Plus,
  RefreshCw,
  Search,
  Truck,
  Warehouse,
  X,
} from 'lucide-react';
import { Auth } from './pages/Auth';
import { CreateOrder } from './pages/CreateOrder';
import { useAuthStore } from './store/useAuthStore';
import {
  smartlogixService,
  type Inventory,
  type Order,
  type Product,
  type Route,
  type Shipment,
  type Warehouse as WarehouseRecord,
} from './services/smartlogixService';

type View = 'dashboard' | 'orders' | 'create-order' | 'products' | 'warehouses' | 'stock' | 'shipments' | 'routes';

interface WorkspaceData {
  orders: Order[];
  products: Product[];
  warehouses: WarehouseRecord[];
  stock: Inventory[];
  shipments: Shipment[];
  routes: Route[];
}

const emptyData: WorkspaceData = {
  orders: [],
  products: [],
  warehouses: [],
  stock: [],
  shipments: [],
  routes: [],
};

const navGroups = [
  {
    label: 'Operación', items: [
      { id: 'dashboard', label: 'Vista general', icon: LayoutDashboard },
      { id: 'orders', label: 'Órdenes', icon: ClipboardList },
    ]
  },
  {
    label: 'Inventario', items: [
      { id: 'products', label: 'Productos', icon: Package },
      { id: 'warehouses', label: 'Bodegas', icon: Warehouse },
      { id: 'stock', label: 'Stock', icon: Boxes },
    ]
  },
  {
    label: 'Logística', items: [
      { id: 'shipments', label: 'Enví­os', icon: Truck },
      { id: 'routes', label: 'Rutas', icon: Map },
    ]
  },
] as const;

export default function App() {
  const [view, setView] = useState<View>('dashboard');
  const [data, setData] = useState<WorkspaceData>(emptyData);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { token, user, clearSession } = useAuthStore();

  const loadData = async () => {
    setLoading(true);
    setError(null);
    const results = await Promise.allSettled([
      smartlogixService.getOrders(),
      smartlogixService.getProducts(),
      smartlogixService.getWarehouses(),
      smartlogixService.getInventory(),
      smartlogixService.getShipments(),
      smartlogixService.getRoutes(),
    ]);

    setData({
      orders: valueOrEmpty(results[0]),
      products: valueOrEmpty(results[1]),
      warehouses: valueOrEmpty(results[2]),
      stock: valueOrEmpty(results[3]),
      shipments: valueOrEmpty(results[4]),
      routes: valueOrEmpty(results[5]),
    });

    const failed = results.filter((result) => result.status === 'rejected').length;
    if (failed) setError(`${failed} módulos no respondieron. Revisa que el backend esté levantado en el gateway.`);
    setLoading(false);
  };

  useEffect(() => {
    if (token) void loadData();
  }, [token]);

  if (!token) return <Auth />;

  const renderView = () => {
    if (view === 'create-order') {
      return <CreateOrder onBack={() => setView('orders')} onCreated={loadData} />;
    }

    return (
      <>
        <PageHeader view={view} loading={loading} onRefresh={loadData} onCreateOrder={() => setView('create-order')} />
        <main className="page-body">
          {error && <div className="banner"><RefreshCw className="ico" />{error}</div>}
          {view === 'dashboard' && <Dashboard data={data} onCreateOrder={() => setView('create-order')} />}
          {view === 'orders' && <OrdersView orders={data.orders} onCreateOrder={() => setView('create-order')} />}
          {view === 'products' && <ProductManagerView products={data.products} warehouses={data.warehouses} stock={data.stock} onSaved={loadData} />}
          {view === 'warehouses' && <WarehouseManagerView warehouses={data.warehouses} />}
          {view === 'stock' && <StockView stock={data.stock} onSaved={loadData} />}
          {view === 'shipments' && <ShipmentsView shipments={data.shipments} />}
          {view === 'routes' && <RoutesView routes={data.routes} />}
        </main>
      </>
    );
  };

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">SL</div>
          <div>
            <strong>SmartLogix</strong>
            <span>Control tower</span>
          </div>
        </div>

        <nav>
          {navGroups.map((group) => (
            <React.Fragment key={group.label}>
              <div className="nav-label">{group.label}</div>
              {group.items.map((item) => {
                const Icon = item.icon;
                const active = view === item.id || (view === 'create-order' && item.id === 'orders');
                return (
                  <button key={item.id} className={`nav-item ${active ? 'active' : ''}`} onClick={() => setView(item.id)}>
                    <Icon className="ico" />
                    <span>{item.label}</span>
                  </button>
                );
              })}
            </React.Fragment>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="avatar">{initials(user?.name || user?.email || 'AD')}</div>
          <div className="user-block">
            <strong>{user?.name || 'Admin'}</strong>
            <span>{user?.companyName || 'SmartLogix'}</span>
          </div>
          <button className="btn btn-icon" onClick={clearSession} title="Cerrar sesión">
            <LogOut className="ico" />
          </button>
        </div>
      </aside>

      <section className="content-shell">{renderView()}</section>
    </div>
  );
}

const valueOrEmpty = <T,>(result: PromiseSettledResult<T[]>): T[] => result.status === 'fulfilled' ? result.value : [];

const PageHeader = ({ view, loading, onRefresh, onCreateOrder }: {
  view: View;
  loading: boolean;
  onRefresh: () => void;
  onCreateOrder: () => void;
}) => {
  const meta: Record<View, { title: string; eyebrow: string; description: string }> = {
    dashboard: { title: 'Vista general', eyebrow: 'Operación', description: 'Salud completa de órdenes, inventario y última milla.' },
    orders: { title: 'Órdenes', eyebrow: 'Operación', description: 'Seguimiento del flujo Saga desde compra hasta entrega.' },
    'create-order': { title: 'Crear orden', eyebrow: 'Operación', description: 'Nuevo pedido.' },
    products: { title: 'Productos', eyebrow: 'Inventario', description: 'Catálogo SKU conectado a ms-inventory.' },
    warehouses: { title: 'Bodegas', eyebrow: 'Inventario', description: 'Centros de distribución, tránsito y almacenamiento.' },
    stock: { title: 'Stock', eyebrow: 'Inventario', description: 'Disponibilidad y reservas por producto y bodega.' },
    shipments: { title: 'Envíos', eyebrow: 'Logística', description: 'Tracking y estado de despachos individuales.' },
    routes: { title: 'Rutas', eyebrow: 'Logística', description: 'Planificación de rutas y asignación de envíos.' },
  };

  return (
    <header className="page-header">
      <div>
        <span className="eyebrow">{meta[view].eyebrow}</span>
        <h1>{meta[view].title}</h1>
        <p>{meta[view].description}</p>
      </div>
      <div className="page-actions">
        <button className="btn" onClick={onRefresh} disabled={loading}>
          <RefreshCw className={`ico ${loading ? 'spin' : ''}`} /> Actualizar
        </button>
        {(view === 'dashboard' || view === 'orders') && (
          <button className="btn btn-primary" onClick={onCreateOrder}>
            <Plus className="ico" /> Nueva orden
          </button>
        )}
      </div>
    </header>
  );
};

const Dashboard = ({ data, onCreateOrder }: { data: WorkspaceData; onCreateOrder: () => void }) => {
  const pendingOrders = data.orders.filter((order) => ['PENDING', 'PENDIENTE', 'CREATED'].includes(order.status)).length;
  const lowStock = data.stock.filter((item) => Number(item.stockAvailable) <= 5).length;
  const inTransit = data.shipments.filter((shipment) => shipment.deliveryStatus === 'IN_TRANSIT').length;
  const activeRoutes = data.routes.filter((route) => ['PENDING', 'IN_PROGRESS'].includes(route.status)).length;

  return (
    <div className="dashboard-grid">
      <div className="kpi-grid">
        <Kpi label="Órdenes totales" value={data.orders.length} detail={`${pendingOrders} pendientes`} />
        <Kpi label="Productos" value={data.products.length} detail={`${lowStock} con bajo stock`} tone={lowStock ? 'warn' : 'ok'} />
        <Kpi label="En tránsito" value={inTransit} detail={`${data.shipments.length} envíos`} />
        <Kpi label="Rutas activas" value={activeRoutes} detail={`${data.routes.length} planificadas`} />
      </div>

      <section className="panel wide">
        <PanelTitle icon={<BarChart3 className="ico" />} title="Pulso operativo" action={<button className="btn btn-sm" onClick={onCreateOrder}><Plus className="ico" /> Orden</button>} />
        <div className="flow-strip">
          <FlowStep label="Orden" value={data.orders.length} />
          <FlowStep label="Reserva" value={data.stock.reduce((sum, item) => sum + Number(item.stockReserved || 0), 0)} />
          <FlowStep label="Enví­o" value={data.shipments.length} />
          <FlowStep label="Ruta" value={data.routes.length} />
        </div>
      </section>

      <section className="panel">
        <PanelTitle icon={<ClipboardList className="ico" />} title="Órdenes recientes" />
        <MiniList rows={data.orders.slice(0, 5).map((order) => ({
          title: order.customerName,
          subtitle: `${order.comunaNombre || 'Destino'} Â· ${formatMoney(order.total)}`,
          badge: order.status,
        }))} empty="Sin Órdenes registradas" />
      </section>

      <section className="panel">
        <PanelTitle icon={<Boxes className="ico" />} title="Alertas de stock" />
        <MiniList rows={data.stock.filter((item) => Number(item.stockAvailable) <= 5).slice(0, 5).map((item) => ({
          title: item.productName || item.sku,
          subtitle: item.warehouseName,
          badge: `${item.stockAvailable} disp.`,
        }))} empty="No hay alertas de stock" />
      </section>
    </div>
  );
};

const OrdersView = ({ orders, onCreateOrder }: { orders: Order[]; onCreateOrder: () => void }) => (
  <DataPanel
    title="Listado de Órdenes"
    searchPlaceholder="Buscar cliente, comuna o estado"
    rows={orders}
    emptyAction={<button className="btn btn-primary" onClick={onCreateOrder}><Plus className="ico" /> Crear primera orden</button>}
    columns={[
      { header: 'Cliente', render: (order) => <StrongCell title={order.customerName} subtitle={order.customerEmail} /> },
      { header: 'Destino', render: (order) => `${order.street}, ${order.comunaNombre || order.comunaId}` },
      { header: 'Estado', render: (order) => <StatusBadge value={order.status} /> },
      { header: 'Total', align: 'right', render: (order) => formatMoney(order.total) },
      { header: 'Creación', render: (order) => formatDate(order.createdAt) },
    ]}
  />
);


const ProductManagerView = ({ products, warehouses, stock, onSaved }: { products: Product[]; warehouses: WarehouseRecord[]; stock: Inventory[]; onSaved: () => Promise<void> }) => {
  const [editing, setEditing] = useState<Product | null>(null);
  const [creating, setCreating] = useState(false);

  return (
    <>
      <DataPanel
        title="Catalogo de productos"
        searchPlaceholder="Buscar producto o SKU"
        rows={products}
        headerAction={<button className="btn btn-primary" onClick={() => setCreating(true)}><Plus className="ico" /> Producto</button>}
        columns={[
          { header: 'Producto', render: (product) => <StrongCell title={product.name} subtitle={product.sku} /> },
          { header: 'Empresa', render: (product) => shortId(product.companyId) },
          { header: 'Estado', render: (product) => <StatusBadge value={product.status} /> },
          { header: 'Precio', align: 'right', render: (product) => formatMoney(product.price) },
          { header: '', align: 'right', render: (product) => <button className="btn btn-icon" onClick={() => setEditing(product)} aria-label="Editar producto"><Edit2 className="ico" /></button> },
        ]}
      />
      {(creating || editing) && (
        <ProductModal
          product={editing}
          onClose={() => { setCreating(false); setEditing(null); }}
          onSaved={onSaved}
          products={products}
          warehouses={warehouses}
          stock={stock}
        />
      )}
    </>
  );
};

const WarehouseManagerView = ({ warehouses }: { warehouses: WarehouseRecord[] }) => {
  const [selected, setSelected] = useState<WarehouseRecord | null>(null);

  return (
    <>
      <DataPanel
        title="Bodegas"
        searchPlaceholder="Buscar bodega, direccion o tipo"
        rows={warehouses}
        onRowClick={(warehouse) => setSelected(warehouse)}
        columns={[
          { header: 'Bodega', render: (warehouse) => <StrongCell title={warehouse.name} subtitle={warehouse.locationAddress} /> },
          { header: 'Tipo', render: (warehouse) => <StatusBadge value={warehouse.type} /> },
          { header: 'Estado', render: (warehouse) => <StatusBadge value={warehouse.status} /> },
          { header: 'Empresa', render: (warehouse) => shortId(warehouse.companyId) },
          { header: '', align: 'right', render: (warehouse) => <button className="btn btn-icon" onClick={(event) => { event.stopPropagation(); setSelected(warehouse); }} aria-label="Ver bodega"><Eye className="ico" /></button> },
        ]}
      />
      {selected && (
        <WarehouseDetailModal
          warehouse={selected}
          onClose={() => setSelected(null)}
        />
      )}
    </>
  );
};

const StockView = ({ stock, onSaved }: { stock: Inventory[]; onSaved: () => Promise<void> }) => {
  const [editing, setEditing] = useState<Inventory | null>(null);

  return (
    <>
      <DataPanel
        title="Stock por bodega"
        searchPlaceholder="Buscar producto, SKU o bodega"
        rows={stock}
        onRowClick={(item) => setEditing(item)}
        columns={[
          { header: 'Producto', render: (item) => <StrongCell title={item.productName || item.sku} subtitle={item.sku} /> },
          { header: 'Bodega', render: (item) => item.warehouseName },
          { header: 'Disponible', align: 'right', render: (item) => item.stockAvailable },
          { header: 'Reservado', align: 'right', render: (item) => item.stockReserved },
          { header: 'Actualizado', render: (item) => formatDate(item.lastUpdated) },
          { header: '', align: 'right', render: (item) => <button className="btn btn-icon" onClick={(event) => { event.stopPropagation(); setEditing(item); }} aria-label="Editar stock"><Edit2 className="ico" /></button> },
        ]}
      />
      {editing && (
        <StockModal
          inventory={editing}
          onClose={() => setEditing(null)}
          onSaved={onSaved}
        />
      )}
    </>
  );
};

const ShipmentsView = ({ shipments }: { shipments: Shipment[] }) => {
  const [selectedShipment, setSelectedShipment] = useState<Shipment | null>(null);

  return (
    <>
      <DataPanel
        title="Envíos"
        searchPlaceholder="Buscar tracking, cliente o dirección"
        rows={shipments}
        onRowClick={(shipment) => setSelectedShipment(shipment)}
        columns={[
          { header: 'Tracking', render: (shipment) => <StrongCell title={shipment.trackingNumber || shortId(shipment.id)} subtitle={shipment.customerName} /> },
          { header: 'Dirección', render: (shipment) => shipment.shippingAddress },
          { header: 'Estado', render: (shipment) => <StatusBadge value={shipment.deliveryStatus} /> },
          { header: 'Entrega estimada', render: (shipment) => formatDate(shipment.estimatedDelivery) },
          { header: 'Ruta', render: (shipment) => shortId(shipment.routeId) },
        ]}
      />
      {selectedShipment && <ShipmentModal shipment={selectedShipment} onClose={() => setSelectedShipment(null)} />}
    </>
  );
};

const RoutesView = ({ routes }: { routes: Route[] }) => (
  <DataPanel
    title="Rutas"
    searchPlaceholder="Buscar ruta, origen o estado"
    rows={routes}
    columns={[
      { header: 'Ruta', render: (route) => <StrongCell title={shortId(route.id)} subtitle={route.originAddress} /> },
      { header: 'Estado', render: (route) => <StatusBadge value={route.status} /> },
      { header: 'Fecha', render: (route) => formatDate(route.routeDate) },
      { header: 'Envíos', align: 'right', render: (route) => route.shipments?.length || 0 },
      { header: 'Carrier', render: (route) => shortId(route.carrierId) },
    ]}
  />
);

interface Column<T> {
  header: string;
  align?: 'right';
  render: (row: T) => React.ReactNode;
}

const DataPanel = <T extends object>({ title, rows, columns, searchPlaceholder, emptyAction, onRowClick, headerAction }: {
  title: string;
  rows: T[];
  columns: Column<T>[];
  searchPlaceholder: string;
  emptyAction?: React.ReactNode;
  onRowClick?: (row: T) => void;
  headerAction?: React.ReactNode;
}) => {
  const [query, setQuery] = useState('');
  const filteredRows = useMemo(() => {
    const normalized = query.trim().toLowerCase();
    if (!normalized) return rows;
    return rows.filter((row) => JSON.stringify(row).toLowerCase().includes(normalized));
  }, [query, rows]);

  return (
    <section className="panel table-panel">
      <PanelTitle
        icon={<Search className="ico" />}
        title={title}
        action={<div className="panel-actions">{headerAction}<div className="search-box"><Search className="ico" /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder={searchPlaceholder} /></div></div>}
      />
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>{columns.map((column) => <th className={column.align === 'right' ? 'right' : ''} key={column.header}>{column.header}</th>)}</tr>
          </thead>
          <tbody>
            {filteredRows.map((row, index) => (
              <tr key={index} className={onRowClick ? 'clickable-row' : ''} onClick={() => onRowClick?.(row)}>
                {columns.map((column) => <td className={column.align === 'right' ? 'right' : ''} key={column.header}>{column.render(row)}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
        {!filteredRows.length && <EmptyState text={query ? 'Sin resultados para la búsqueda.' : 'No hay datos disponibles para este módulo.'} action={emptyAction} />}
      </div>
    </section>
  );
};

const ProductModal = ({
  product,
  onClose,
  onSaved,
  products,
  warehouses,
  stock,
}: {
  product: Product | null;
  onClose: () => void;
  onSaved: () => Promise<void>;
  products: Product[];
  warehouses: WarehouseRecord[];
  stock: Inventory[];
}) => {
  const activeWarehouses = useMemo(
    () => warehouses.filter((warehouse) => (warehouse.status || '').toUpperCase() !== 'INACTIVE'),
    [warehouses],
  );

  const [form, setForm] = useState({
    sku: product?.sku || '',
    name: product?.name || '',
    price: product?.price ? String(product.price) : '',
    warehouseId: '',
    initialStock: '0',
  });

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    const price = Number(form.price);
    if (!form.sku.trim()) return setError('Ingresa el SKU.');
    if (!form.name.trim()) return setError('Ingresa el nombre del producto.');
    if (!Number.isFinite(price) || price < 0) return setError('Ingresa un precio valido.');

    if (!form.warehouseId) return setError('Selecciona una bodega destino.');
    const initialStock = Number(form.initialStock);
    if (!Number.isInteger(initialStock) || initialStock <= 0) return setError('Ingresa un stock inicial mayor a cero.');

    setSaving(true);
    setError('');
    try {
      const payload = { sku: form.sku.trim(), name: form.name.trim(), price };
      let createdOrUpdated: Product | null = null;

      if (product) {
        createdOrUpdated = await smartlogixService.updateProduct(product.id, payload);
      } else {
        const existingProduct = products.find((item) => item.sku.trim().toLowerCase() === payload.sku.toLowerCase());
        createdOrUpdated = existingProduct
          ? await smartlogixService.updateProduct(existingProduct.id, payload)
          : await smartlogixService.createProduct(payload);
      }

      // Crear o aumentar inventario para la bodega seleccionada
      if (!createdOrUpdated) throw new Error('No se pudo obtener el producto guardado.');

      // Si ya existe stock para (product, warehouse) lo incrementamos.
      // Si no existe, creamos.
      const existing = stock.find(
        (s) => s.productId === createdOrUpdated.id && s.warehouseId === form.warehouseId,
      );
      if (existing) {
        await smartlogixService.increaseInventory(existing.id, { quantity: initialStock, reason: 'Ingreso de stock' });
      } else {
        await smartlogixService.createInventory({
          productId: createdOrUpdated.id,
          warehouseId: form.warehouseId,
          stockAvailable: initialStock,
        });
      }


      await onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo guardar el producto');
    } finally {
      setSaving(false);
    }
  };


  return (
    <EditorModal title={product ? 'Editar producto' : 'Nuevo producto'} onClose={onClose}>
      <form className="editor-form" onSubmit={submit}>
        <div className="form-grid two">
          <div className="field">
            <label>SKU</label>
            <input className="input" value={form.sku} onChange={(event) => setForm((current) => ({ ...current, sku: event.target.value }))} required />
          </div>
          <div className="field">
            <label>Precio</label>
            <input className="input" type="number" min="0" step="0.01" value={form.price} onChange={(event) => setForm((current) => ({ ...current, price: event.target.value }))} required />
          </div>
          <div className="field wide">
            <label>Nombre</label>
            <input className="input" value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required />
          </div>
          <div className="field">
            <label>Bodega destino</label>
            <select
              className="select"
              value={form.warehouseId}
              onChange={(event) => setForm((current) => ({ ...current, warehouseId: event.target.value }))}
              required
              disabled={!activeWarehouses.length}
            >
              <option value="">{activeWarehouses.length ? 'Selecciona bodega' : 'No hay bodegas disponibles'}</option>
              {activeWarehouses.map((warehouse) => (
                <option key={warehouse.id} value={warehouse.id}>
                  {warehouse.name} - {warehouse.locationAddress}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label>Stock inicial</label>
            <input
              className="input"
              type="number"
              min="1"
              step="1"
              value={form.initialStock}
              onChange={(event) => setForm((current) => ({ ...current, initialStock: event.target.value }))}
              required
            />
          </div>
        </div>
        {error && <p className="field-error">{error}</p>}
        <div className="editor-actions">
          <button type="button" className="btn" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</button>
        </div>
      </form>
    </EditorModal>
  );
};

const WarehouseDetailModal = ({ warehouse, onClose }: { warehouse: WarehouseRecord; onClose: () => void }) => (
  <EditorModal title="Detalle de bodega" onClose={onClose}>
    <div className="editor-form">
      <div className="form-grid two">
        <div className="field">
          <label>Nombre</label>
          <input className="input" value={warehouse.name} disabled />
        </div>
        <div className="field">
          <label>Tipo</label>
          <input className="input" value={warehouse.type} disabled />
        </div>
        <div className="field wide">
          <label>Direccion</label>
          <input className="input" value={warehouse.locationAddress} disabled />
        </div>
        <div className="field">
          <label>Estado</label>
          <input className="input" value={warehouse.status} disabled />
        </div>
        <div className="field">
          <label>Empresa</label>
          <input className="input" value={warehouse.companyId || 'N/A'} disabled />
        </div>
      </div>
      <div className="editor-actions">
        <button type="button" className="btn btn-primary" onClick={onClose}>Cerrar</button>
      </div>
    </div>
  </EditorModal>
);

const StockModal = ({ inventory, onClose, onSaved }: { inventory: Inventory; onClose: () => void; onSaved: () => Promise<void> }) => {
  const { user } = useAuthStore();
  const [action, setAction] = useState<'increase' | 'reserve'>('increase');
  const [quantity, setQuantity] = useState('1');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    const parsedQuantity = Number(quantity);
    if (!Number.isInteger(parsedQuantity) || parsedQuantity <= 0) return setError('Ingresa una cantidad mayor a cero.');
    if (action === 'reserve' && parsedQuantity > Number(inventory.stockAvailable)) return setError('No puedes reservar mas stock del disponible.');

    setSaving(true);
    setError('');
    try {
      if (action === 'increase') {
        await smartlogixService.increaseInventory(inventory.id, {
          quantity: parsedQuantity,
          reason: 'Aumento manual de stock',
        });
      } else {
        if (!user?.companyId) throw new Error('No se pudo identificar la empresa para reservar stock.');
        await smartlogixService.reserveInventory({
          orderId: `manual-${Date.now()}`,
          productId: inventory.productId,
          warehouseId: inventory.warehouseId,
          quantity: parsedQuantity,
          companyId: user.companyId,
        });
      }

      await onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo actualizar el stock');
    } finally {
      setSaving(false);
    }
  };

  return (
    <EditorModal title="Editar stock" onClose={onClose}>
      <form className="editor-form" onSubmit={submit}>
        <div className="form-grid two">
          <div className="field">
            <label>Producto</label>
            <input className="input" value={inventory.productName || inventory.sku} disabled />
          </div>
          <div className="field">
            <label>SKU</label>
            <input className="input" value={inventory.sku} disabled />
          </div>
          <div className="field wide">
            <label>Bodega</label>
            <input className="input" value={inventory.warehouseName} disabled />
          </div>
          <div className="field">
            <label>Stock disponible</label>
            <input className="input" value={inventory.stockAvailable} disabled />
          </div>
          <div className="field">
            <label>Stock reservado</label>
            <input className="input" value={inventory.stockReserved} disabled />
          </div>
          <div className="field">
            <label>Accion</label>
            <select className="select" value={action} onChange={(event) => setAction(event.target.value as 'increase' | 'reserve')}>
              <option value="increase">Aumentar stock</option>
              <option value="reserve">Reservar stock</option>
            </select>
          </div>
          <div className="field">
            <label>Cantidad</label>
            <input
              className="input"
              type="number"
              min="1"
              max={action === 'reserve' ? inventory.stockAvailable : undefined}
              step="1"
              value={quantity}
              onChange={(event) => setQuantity(event.target.value)}
              required
            />
          </div>
        </div>
        {error && <p className="field-error">{error}</p>}
        <div className="editor-actions">
          <button type="button" className="btn" onClick={onClose}>Cancelar</button>
          <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Guardar'}</button>
        </div>
      </form>
    </EditorModal>
  );
};

const EditorModal = ({ title, children, onClose }: { title: string; children: React.ReactNode; onClose: () => void }) => (
  <div className="modal-backdrop" onClick={onClose}>
    <article className="editor-modal" onClick={(event) => event.stopPropagation()}>
      <header className="modal-head">
        <h2>{title}</h2>
        <button className="btn btn-icon" onClick={onClose} aria-label="Cerrar">
          <X className="ico" />
        </button>
      </header>
      {children}
    </article>
  </div>
);

const ShipmentModal = ({ shipment, onClose }: { shipment: Shipment; onClose: () => void }) => {
  const destination = shipment.shippingAddress || 'Dirección no disponible';
  const mapUrl = shipment.latitude && shipment.longitude
    ? `https://maps.google.com/maps?q=${shipment.latitude},${shipment.longitude}&z=15&output=embed`
    : `https://maps.google.com/maps?q=${encodeURIComponent(destination)}&z=15&output=embed`;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <article className="shipment-modal" onClick={(event) => event.stopPropagation()}>
        <header className="modal-head">
          <div>
            <span className="eyebrow">Detalle de enví­o</span>
            <h2>{shipment.trackingNumber || shortId(shipment.id)}</h2>
          </div>
          <button className="btn btn-icon" onClick={onClose} aria-label="Cerrar detalle">
            <X className="ico" />
          </button>
        </header>

        <div className="shipment-detail-grid">
          <section className="shipment-facts">
            <StatusBadge value={shipment.deliveryStatus} />
            <dl className="detail-list">
              <dt>Cliente</dt>
              <dd>{shipment.customerName || 'Sin cliente'}</dd>
              <dt>Email</dt>
              <dd>{shipment.customerEmail || 'Sin email'}</dd>
              <dt>Dirección validada</dt>
              <dd>{destination}</dd>
              <dt>Orden</dt>
              <dd>{shortId(shipment.orderId)}</dd>
              <dt>Ruta</dt>
              <dd>{shortId(shipment.routeId)}</dd>
              <dt>Entrega estimada</dt>
              <dd>{formatDate(shipment.estimatedDelivery)}</dd>
              <dt>Entrega real</dt>
              <dd>{formatDate(shipment.actualDelivery)}</dd>
              <dt>Coordenadas</dt>
              <dd>{shipment.latitude && shipment.longitude ? `${shipment.latitude}, ${shipment.longitude}` : 'Mapa por dirección'}</dd>
            </dl>
          </section>

          <section className="map-panel">
            <iframe
              title={`Mapa de entrega ${shipment.trackingNumber || shipment.id}`}
              src={mapUrl}
              loading="lazy"
              referrerPolicy="no-referrer-when-downgrade"
            />
          </section>
        </div>
      </article>
    </div>
  );
};

const PanelTitle = ({ icon, title, action }: { icon: React.ReactNode; title: string; action?: React.ReactNode }) => (
  <div className="panel-title">
    <div>{icon}<h2>{title}</h2></div>
    {action}
  </div>
);

const Kpi = ({ label, value, detail, tone }: { label: string; value: number; detail: string; tone?: 'ok' | 'warn' }) => (
  <article className={`kpi ${tone || ''}`}>
    <span>{label}</span>
    <strong>{value}</strong>
    <small>{detail}</small>
  </article>
);

const FlowStep = ({ label, value }: { label: string; value: number }) => (
  <div className="flow-step">
    <strong>{value}</strong>
    <span>{label}</span>
  </div>
);

const MiniList = ({ rows, empty }: { rows: Array<{ title: string; subtitle: string; badge: string }>; empty: string }) => {
  if (!rows.length) return <EmptyState text={empty} />;
  return (
    <div className="mini-list">
      {rows.map((row, index) => (
        <div className="mini-row" key={`${row.title}-${index}`}>
          <StrongCell title={row.title} subtitle={row.subtitle} />
          <StatusBadge value={row.badge} />
        </div>
      ))}
    </div>
  );
};

const StrongCell = ({ title, subtitle }: { title: React.ReactNode; subtitle?: React.ReactNode }) => (
  <div className="strong-cell">
    <strong>{title}</strong>
    {subtitle && <span>{subtitle}</span>}
  </div>
);

const StatusBadge = ({ value }: { value?: string }) => {
  const status = value || 'N/A';
  const normalized = status.toUpperCase();
  const tone = normalized.includes('DELIVERED') || normalized.includes('ACTIVE') || normalized.includes('COMPLETED') || normalized.includes('CONFIRMED')
    ? 'ok'
    : normalized.includes('PENDING') || normalized.includes('TRANSIT') || normalized.includes('PROGRESS')
      ? 'warn'
      : normalized.includes('FAILED') || normalized.includes('CANCEL')
        ? 'danger'
        : 'neutral';
  return <span className={`status ${tone}`}>{status.replaceAll('_', ' ')}</span>;
};

const EmptyState = ({ text, action }: { text: string; action?: React.ReactNode }) => (
  <div className="empty-state">
    <Boxes className="ico" />
    <p>{text}</p>
    {action}
  </div>
);

const formatMoney = (value?: number) => new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 0,
}).format(Number(value || 0));

const formatDate = (value?: string) => value ? new Intl.DateTimeFormat('es-CL', { dateStyle: 'medium' }).format(new Date(value)) : 'Sin fecha';
const shortId = (value?: string) => value ? value.slice(0, 8) : 'N/A';
const initials = (value: string) => value.split(/\s|@/).filter(Boolean).slice(0, 2).map((part) => part[0]?.toUpperCase()).join('');

