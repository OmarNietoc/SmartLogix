/* SmartLogix API client
   Wraps the API Gateway at /smartlogix/* with JWT auth + offline mock fallback.
*/
(function () {
  const DEFAULT_BASE = 'http://localhost:8080';
  const STORAGE_KEY = 'smartlogix.session';
  const BASE_KEY = 'smartlogix.apiBase';
  const MOCK_KEY = 'smartlogix.mockMode';

  const getBase = () => localStorage.getItem(BASE_KEY) || DEFAULT_BASE;
  const setBase = (b) => localStorage.setItem(BASE_KEY, b);
  const getSession = () => {
    try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null'); } catch { return null; }
  };
  const setSession = (s) => {
    if (s) localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
    else localStorage.removeItem(STORAGE_KEY);
  };
  const isMock = () => localStorage.getItem(MOCK_KEY) === '1';
  const setMock = (v) => localStorage.setItem(MOCK_KEY, v ? '1' : '0');

  async function request(path, opts = {}) {
    const session = getSession();
    const headers = {
      'Content-Type': 'application/json',
      ...(opts.headers || {}),
    };
    if (session?.token) headers.Authorization = `Bearer ${session.token}`;

    const url = getBase() + path;
    const ctrl = new AbortController();
    const timeout = setTimeout(() => ctrl.abort(), 8000);
    try {
      const res = await fetch(url, {
        method: opts.method || 'GET',
        headers,
        body: opts.body ? JSON.stringify(opts.body) : undefined,
        signal: ctrl.signal,
        mode: 'cors',
      });
      clearTimeout(timeout);
      const text = await res.text();
      const data = text ? JSON.parse(text) : null;
      if (!res.ok) {
        const err = new Error(data?.message || `HTTP ${res.status}`);
        err.status = res.status; err.data = data;
        throw err;
      }
      // Backend wraps responses in MessageResponse { statusCode, message, data }
      return data && 'data' in data ? data.data : data;
    } catch (e) {
      clearTimeout(timeout);
      if (e.name === 'AbortError') {
        const err = new Error('Tiempo de espera agotado'); err.network = true; throw err;
      }
      if (e.status) throw e;
      const err = new Error('No se pudo conectar al backend'); err.network = true; throw err;
    }
  }

  // ===== MOCK fallback (used when backend offline) =====
  const mockDB = {
    companies: [{ id: 'cmp_01', taxId: '76.123.456-7', name: 'Logística Andina S.A.', contactEmail: 'contacto@andina.cl', phone: '+56 2 2345 6789' }],
    users: [{ id: 'usr_01', authId: 'auth_01', companyId: 'cmp_01', firstName: 'María', lastName: 'González', role: 'ADMIN' }],
    products: [
      { id: 'prd_01', companyId: 'cmp_01', sku: 'SKU-A001', name: 'Caja cartón corrugado 40x30x20', price: 1290, status: 'ACTIVE' },
      { id: 'prd_02', companyId: 'cmp_01', sku: 'SKU-A002', name: 'Pallet madera estándar', price: 8500, status: 'ACTIVE' },
      { id: 'prd_03', companyId: 'cmp_01', sku: 'SKU-B011', name: 'Film stretch 50cm x 300m', price: 5490, status: 'ACTIVE' },
      { id: 'prd_04', companyId: 'cmp_01', sku: 'SKU-B012', name: 'Etiqueta térmica 100x150 (rollo)', price: 3200, status: 'ACTIVE' },
      { id: 'prd_05', companyId: 'cmp_01', sku: 'SKU-C201', name: 'Cinta embalaje transparente', price: 890, status: 'ACTIVE' },
      { id: 'prd_06', companyId: 'cmp_01', sku: 'SKU-C202', name: 'Bolsa burbuja A4', price: 240, status: 'INACTIVE' },
    ],
    warehouses: [
      { id: 'wh_01', companyId: 'cmp_01', name: 'CD Pudahuel', locationAddress: 'Av. El Mar 1234, Pudahuel', type: 'WAREHOUSE', status: 'ACTIVE' },
      { id: 'wh_02', companyId: 'cmp_01', name: 'Sucursal Providencia', locationAddress: 'Av. Providencia 2222', type: 'RETAIL_STORE', status: 'ACTIVE' },
      { id: 'wh_03', companyId: 'cmp_01', name: 'CD Antofagasta', locationAddress: 'Ruta 5 Norte km 1340', type: 'WAREHOUSE', status: 'ACTIVE' },
    ],
    inventory: [
      { id: 'inv_01', productId: 'prd_01', warehouseId: 'wh_01', sku: 'SKU-A001', productName: 'Caja cartón corrugado 40x30x20', warehouseName: 'CD Pudahuel', stockAvailable: 1240, stockReserved: 80, lastUpdated: '2026-05-04T14:22:00' },
      { id: 'inv_02', productId: 'prd_02', warehouseId: 'wh_01', sku: 'SKU-A002', productName: 'Pallet madera estándar', warehouseName: 'CD Pudahuel', stockAvailable: 320, stockReserved: 12, lastUpdated: '2026-05-05T09:10:00' },
      { id: 'inv_03', productId: 'prd_03', warehouseId: 'wh_01', sku: 'SKU-B011', productName: 'Film stretch 50cm x 300m', warehouseName: 'CD Pudahuel', stockAvailable: 88, stockReserved: 4, lastUpdated: '2026-05-05T10:01:00' },
      { id: 'inv_04', productId: 'prd_01', warehouseId: 'wh_03', sku: 'SKU-A001', productName: 'Caja cartón corrugado 40x30x20', warehouseName: 'CD Antofagasta', stockAvailable: 560, stockReserved: 0, lastUpdated: '2026-05-04T16:45:00' },
      { id: 'inv_05', productId: 'prd_04', warehouseId: 'wh_02', sku: 'SKU-B012', productName: 'Etiqueta térmica 100x150 (rollo)', warehouseName: 'Sucursal Providencia', stockAvailable: 14, stockReserved: 0, lastUpdated: '2026-05-05T08:30:00' },
      { id: 'inv_06', productId: 'prd_05', warehouseId: 'wh_01', sku: 'SKU-C201', productName: 'Cinta embalaje transparente', warehouseName: 'CD Pudahuel', stockAvailable: 4200, stockReserved: 320, lastUpdated: '2026-05-05T11:15:00' },
    ],
    orders: [
      { id: 'ord_5021', customerName: 'Juan Pérez', customerEmail: 'juan@example.cl', shippingAddress: 'Las Condes 4500, Santiago', status: 'PENDIENTE', total: 24580, createdAt: '2026-05-05T09:14:00', updatedAt: '2026-05-05T09:14:00', items: [{ id: 'oi_1', productId: 'prd_01', warehouseId: 'wh_01', productName: 'Caja cartón corrugado 40x30x20', quantity: 12, price: 1290 }, { id: 'oi_2', productId: 'prd_05', warehouseId: 'wh_01', productName: 'Cinta embalaje transparente', quantity: 10, price: 890 }] },
      { id: 'ord_5020', customerName: 'Carolina Soto', customerEmail: 'caro.soto@empresa.cl', shippingAddress: 'Manuel Montt 1188, Providencia', status: 'APROBADO', total: 17000, createdAt: '2026-05-05T08:50:00', updatedAt: '2026-05-05T09:02:00', items: [{ id: 'oi_3', productId: 'prd_02', warehouseId: 'wh_01', productName: 'Pallet madera estándar', quantity: 2, price: 8500 }] },
      { id: 'ord_5019', customerName: 'Diego Ramírez', customerEmail: 'd.ramirez@correo.cl', shippingAddress: 'San Pedro 220, Antofagasta', status: 'ENVIADO', total: 6450, createdAt: '2026-05-04T16:32:00', updatedAt: '2026-05-05T07:14:00', items: [{ id: 'oi_4', productId: 'prd_01', warehouseId: 'wh_03', productName: 'Caja cartón corrugado 40x30x20', quantity: 5, price: 1290 }] },
      { id: 'ord_5018', customerName: 'Verónica Lazo', customerEmail: 'v.lazo@cliente.cl', shippingAddress: 'Apoquindo 6275, Las Condes', status: 'ENTREGADO', total: 32990, createdAt: '2026-05-03T11:20:00', updatedAt: '2026-05-04T18:00:00', items: [{ id: 'oi_5', productId: 'prd_03', warehouseId: 'wh_01', productName: 'Film stretch 50cm x 300m', quantity: 6, price: 5490 }] },
      { id: 'ord_5017', customerName: 'Andrés Muñoz', customerEmail: 'andres.m@logistica.cl', shippingAddress: 'Ruta 68 km 22, Casablanca', status: 'RECHAZADO', total: 1780, createdAt: '2026-05-03T09:05:00', updatedAt: '2026-05-03T10:30:00', items: [{ id: 'oi_6', productId: 'prd_05', warehouseId: 'wh_01', productName: 'Cinta embalaje transparente', quantity: 2, price: 890 }] },
      { id: 'ord_5016', customerName: 'Patricia Rojas', customerEmail: 'p.rojas@cliente.cl', shippingAddress: 'Bilbao 3450, Providencia', status: 'PENDIENTE', total: 8500, createdAt: '2026-05-05T10:30:00', updatedAt: '2026-05-05T10:30:00', items: [{ id: 'oi_7', productId: 'prd_02', warehouseId: 'wh_01', productName: 'Pallet madera estándar', quantity: 1, price: 8500 }] },
    ],
    shipments: [
      { id: 'shp_01', orderId: 'ord_5019', routeId: 'rt_01', customerName: 'Diego Ramírez', customerEmail: 'd.ramirez@correo.cl', shippingAddress: 'San Pedro 220, Antofagasta', latitude: -23.6509, longitude: -70.3975, trackingNumber: 'SLX-92841-AT', deliveryStatus: 'DISPATCHED', estimatedDelivery: '2026-05-06T14:00:00', actualDelivery: null },
      { id: 'shp_02', orderId: 'ord_5020', routeId: 'rt_02', customerName: 'Carolina Soto', customerEmail: 'caro.soto@empresa.cl', shippingAddress: 'Manuel Montt 1188, Providencia', latitude: -33.4263, longitude: -70.6201, trackingNumber: 'SLX-92842-RM', deliveryStatus: 'ASSIGNED', estimatedDelivery: '2026-05-05T18:00:00', actualDelivery: null },
      { id: 'shp_03', orderId: 'ord_5018', routeId: 'rt_02', customerName: 'Verónica Lazo', customerEmail: 'v.lazo@cliente.cl', shippingAddress: 'Apoquindo 6275, Las Condes', latitude: -33.4108, longitude: -70.5681, trackingNumber: 'SLX-92840-RM', deliveryStatus: 'DELIVERED', estimatedDelivery: '2026-05-04T18:00:00', actualDelivery: '2026-05-04T17:42:00' },
      { id: 'shp_04', orderId: 'ord_5021', routeId: null, customerName: 'Juan Pérez', customerEmail: 'juan@example.cl', shippingAddress: 'Las Condes 4500, Santiago', latitude: -33.4090, longitude: -70.5750, trackingNumber: 'SLX-92843-RM', deliveryStatus: 'PENDING', estimatedDelivery: '2026-05-06T18:00:00', actualDelivery: null },
    ],
    routes: [
      { id: 'rt_01', companyId: 'cmp_01', carrierId: 'crr_dhl', routeDate: '2026-05-06', originAddress: 'CD Antofagasta — Ruta 5 Norte km 1340', optimizedPathJson: '[]', status: 'IN_PROGRESS' },
      { id: 'rt_02', companyId: 'cmp_01', carrierId: 'crr_local', routeDate: '2026-05-05', originAddress: 'CD Pudahuel — Av. El Mar 1234', optimizedPathJson: '[]', status: 'IN_PROGRESS' },
      { id: 'rt_03', companyId: 'cmp_01', carrierId: 'crr_local', routeDate: '2026-05-04', originAddress: 'CD Pudahuel — Av. El Mar 1234', optimizedPathJson: '[]', status: 'COMPLETED' },
      { id: 'rt_04', companyId: 'cmp_01', carrierId: 'crr_dhl', routeDate: '2026-05-07', originAddress: 'CD Pudahuel — Av. El Mar 1234', optimizedPathJson: '[]', status: 'PLANNED' },
    ],
  };

  const mockDelay = (data) => new Promise(r => setTimeout(() => r(data), 150 + Math.random() * 200));
  const newId = (p) => `${p}_${Math.random().toString(36).slice(2, 8)}`;

  // ===== Public API =====
  const API = {
    config: { getBase, setBase, isMock, setMock },
    session: { get: getSession, set: setSession, clear: () => setSession(null) },

    async ping() {
      try { await request('/smartlogix/inventary/products', { method: 'GET' }); return { online: true, mock: false }; }
      catch (e) {
        if (e.network) return { online: false, mock: true };
        return { online: true, mock: false };
      }
    },

    // --- Auth ---
    async login({ email, password }) {
      if (isMock()) {
        await mockDelay();
        if (!email.includes('@')) throw new Error('Credenciales inválidas');
        const session = { token: 'mock.jwt.token', user: { email, name: 'María González', role: 'ADMIN', companyId: 'cmp_01', companyName: 'Logística Andina S.A.' } };
        setSession(session); return session;
      }
      try {
        const data = await request('/smartlogix/auth/login', { method: 'POST', body: { email, password } });
        const session = { token: data.token || data.accessToken, user: data.user || { email } };
        setSession(session); return session;
      } catch (e) {
        if (e.network) {
          setMock(true);
          return API.login({ email, password });
        }
        throw e;
      }
    },

    async register({ email, password, firstName, lastName, companyName, taxId }) {
      if (isMock()) {
        await mockDelay();
        const session = { token: 'mock.jwt.token', user: { email, name: `${firstName} ${lastName}`, role: 'ADMIN', companyId: 'cmp_01', companyName } };
        setSession(session); return session;
      }
      try {
        await request('/smartlogix/auth/register', { method: 'POST', body: { email, password, firstName, lastName } });
        await request('/smartlogix/users/companies', { method: 'POST', body: { name: companyName, taxId, contactEmail: email } });
        return API.login({ email, password });
      } catch (e) {
        if (e.network) { setMock(true); return API.register({ email, password, firstName, lastName, companyName, taxId }); }
        throw e;
      }
    },

    async logout() { setSession(null); },

    // --- Products ---
    async listProducts(companyId) {
      if (isMock()) return mockDelay([...mockDB.products]);
      try { return await request(`/smartlogix/inventary/products${companyId ? `?companyId=${companyId}` : ''}`); }
      catch (e) { if (e.network) { setMock(true); return mockDelay([...mockDB.products]); } throw e; }
    },
    async createProduct(p) {
      if (isMock()) { const np = { ...p, id: newId('prd') }; mockDB.products.push(np); return mockDelay(np); }
      try { return await request('/smartlogix/inventary/products', { method: 'POST', body: p }); }
      catch (e) { if (e.network) { setMock(true); return API.createProduct(p); } throw e; }
    },
    async updateProduct(id, p) {
      if (isMock()) { const i = mockDB.products.findIndex(x => x.id === id); if (i >= 0) mockDB.products[i] = { ...mockDB.products[i], ...p }; return mockDelay(mockDB.products[i]); }
      try { return await request(`/smartlogix/inventary/products/${id}`, { method: 'PUT', body: p }); }
      catch (e) { if (e.network) { setMock(true); return API.updateProduct(id, p); } throw e; }
    },
    async deleteProduct(id) {
      if (isMock()) { mockDB.products = mockDB.products.filter(x => x.id !== id); return mockDelay({}); }
      try { return await request(`/smartlogix/inventary/products/${id}`, { method: 'DELETE' }); }
      catch (e) { if (e.network) { setMock(true); return API.deleteProduct(id); } throw e; }
    },

    // --- Warehouses ---
    async listWarehouses(companyId) {
      if (isMock()) return mockDelay([...mockDB.warehouses]);
      try { return await request(`/smartlogix/inventary/warehouses${companyId ? `?companyId=${companyId}` : ''}`); }
      catch (e) { if (e.network) { setMock(true); return mockDelay([...mockDB.warehouses]); } throw e; }
    },
    async createWarehouse(w) {
      if (isMock()) { const nw = { ...w, id: newId('wh') }; mockDB.warehouses.push(nw); return mockDelay(nw); }
      try { return await request('/smartlogix/inventary/warehouses', { method: 'POST', body: w }); }
      catch (e) { if (e.network) { setMock(true); return API.createWarehouse(w); } throw e; }
    },

    // --- Inventory ---
    async listInventory(filters = {}) {
      if (isMock()) return mockDelay([...mockDB.inventory]);
      const q = new URLSearchParams(Object.entries(filters).filter(([_, v]) => v)).toString();
      try { return await request(`/smartlogix/inventary/stocks${q ? '?' + q : ''}`); }
      catch (e) { if (e.network) { setMock(true); return mockDelay([...mockDB.inventory]); } throw e; }
    },
    async adjustStock(id, delta, reason) {
      if (isMock()) {
        const i = mockDB.inventory.findIndex(x => x.id === id);
        if (i >= 0) { mockDB.inventory[i].stockAvailable += delta; mockDB.inventory[i].lastUpdated = new Date().toISOString(); }
        return mockDelay(mockDB.inventory[i]);
      }
      const path = delta > 0 ? `${id}/increase` : `${id}/decrease`;
      try { return await request(`/smartlogix/inventary/stocks/${path}`, { method: 'PATCH', body: { quantity: Math.abs(delta), reason } }); }
      catch (e) { if (e.network) { setMock(true); return API.adjustStock(id, delta, reason); } throw e; }
    },

    // --- Orders ---
    async listOrders() {
      if (isMock()) return mockDelay([...mockDB.orders]);
      try { return await request('/smartlogix/order/orders'); }
      catch (e) { if (e.network) { setMock(true); return mockDelay([...mockDB.orders]); } throw e; }
    },
    async getOrder(id) {
      if (isMock()) return mockDelay(mockDB.orders.find(o => o.id === id));
      try { return await request(`/smartlogix/order/orders/${id}`); }
      catch (e) { if (e.network) { setMock(true); return API.getOrder(id); } throw e; }
    },
    async createOrder(o) {
      if (isMock()) {
        const total = o.items.reduce((s, i) => s + (i.price * i.quantity), 0);
        const no = { id: newId('ord'), ...o, status: 'PENDIENTE', total, createdAt: new Date().toISOString(), updatedAt: new Date().toISOString(), items: o.items.map(i => ({ ...i, id: newId('oi') })) };
        mockDB.orders.unshift(no); return mockDelay(no);
      }
      try { return await request('/smartlogix/order/orders', { method: 'POST', body: o }); }
      catch (e) { if (e.network) { setMock(true); return API.createOrder(o); } throw e; }
    },
    async updateOrderStatus(id, status) {
      if (isMock()) {
        const i = mockDB.orders.findIndex(o => o.id === id);
        if (i >= 0) { mockDB.orders[i].status = status; mockDB.orders[i].updatedAt = new Date().toISOString(); }
        return mockDelay(mockDB.orders[i]);
      }
      try { return await request(`/smartlogix/order/orders/${id}/status`, { method: 'PUT', body: { status } }); }
      catch (e) { if (e.network) { setMock(true); return API.updateOrderStatus(id, status); } throw e; }
    },

    // --- Shipping ---
    async listShipments(deliveryStatus) {
      if (isMock()) return mockDelay(deliveryStatus ? mockDB.shipments.filter(s => s.deliveryStatus === deliveryStatus) : [...mockDB.shipments]);
      try { return await request(`/smartlogix/shipping/shipments${deliveryStatus ? '?deliveryStatus=' + deliveryStatus : ''}`); }
      catch (e) { if (e.network) { setMock(true); return API.listShipments(deliveryStatus); } throw e; }
    },
    async getShipmentByTracking(tn) {
      if (isMock()) { await mockDelay(); const s = mockDB.shipments.find(x => x.trackingNumber === tn); if (!s) throw new Error('No encontrado'); return s; }
      try { return await request(`/smartlogix/shipping/shipments/tracking/${tn}`); }
      catch (e) { if (e.network) { setMock(true); return API.getShipmentByTracking(tn); } throw e; }
    },
    async updateShipmentStatus(id, status) {
      if (isMock()) {
        const i = mockDB.shipments.findIndex(s => s.id === id);
        if (i >= 0) { mockDB.shipments[i].deliveryStatus = status; if (status === 'DELIVERED') mockDB.shipments[i].actualDelivery = new Date().toISOString(); }
        return mockDelay(mockDB.shipments[i]);
      }
      try { return await request(`/smartlogix/shipping/shipments/${id}/status`, { method: 'PATCH', body: status }); }
      catch (e) { if (e.network) { setMock(true); return API.updateShipmentStatus(id, status); } throw e; }
    },
    async listRoutes(filters = {}) {
      if (isMock()) return mockDelay([...mockDB.routes]);
      const q = new URLSearchParams(Object.entries(filters).filter(([_, v]) => v)).toString();
      try { return await request(`/smartlogix/shipping/routes${q ? '?' + q : ''}`); }
      catch (e) { if (e.network) { setMock(true); return mockDelay([...mockDB.routes]); } throw e; }
    },
    async createRoute(r) {
      if (isMock()) { const nr = { ...r, id: newId('rt'), routeDate: r.routeDate || new Date().toISOString().slice(0, 10), status: 'PLANNED' }; mockDB.routes.unshift(nr); return mockDelay(nr); }
      try { return await request('/smartlogix/shipping/routes', { method: 'POST', body: r }); }
      catch (e) { if (e.network) { setMock(true); return API.createRoute(r); } throw e; }
    },
    async assignShipmentToRoute(shipmentId, routeId) {
      if (isMock()) {
        const i = mockDB.shipments.findIndex(s => s.id === shipmentId);
        if (i >= 0) { mockDB.shipments[i].routeId = routeId; mockDB.shipments[i].deliveryStatus = 'ASSIGNED'; }
        return mockDelay(mockDB.shipments[i]);
      }
      try { return await request(`/smartlogix/shipping/shipments/${shipmentId}/route`, { method: 'PATCH', body: { routeId } }); }
      catch (e) { if (e.network) { setMock(true); return API.assignShipmentToRoute(shipmentId, routeId); } throw e; }
    },
  };

  window.SmartlogixAPI = API;
})();
