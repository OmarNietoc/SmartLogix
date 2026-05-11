import { request } from './api';

export interface OrderItemPayload {
  productId: string;
  warehouseId: string;
  productName: string;
  quantity: number;
  price: number;
}

export interface CreateOrderPayload {
  customerName: string;
  customerEmail: string;
  street: string;
  comunaId: number;
  items: OrderItemPayload[];
}

export interface Region {
  id: number;
  nombre: string;
}

export interface Comuna {
  id: number;
  nombre: string;
  regionId: number;
}

export interface Order {
  id: string;
  customerName: string;
  customerEmail: string;
  street: string;
  comunaId: number;
  comunaNombre?: string;
  regionNombre?: string;
  status: string;
  total: number;
  createdAt?: string;
  updatedAt?: string;
  items?: OrderItemPayload[];
}

export interface Product {
  id: string;
  companyId?: string;
  sku: string;
  name: string;
  price: number;
  status: string;
}

export interface ProductPayload {
  sku: string;
  name: string;
  price: number;
}



export interface Warehouse {
  id: string;
  companyId?: string;
  name: string;
  locationAddress: string;
  type: string;
  status: string;
}

export interface WarehousePayload {
  name: string;
  locationAddress: string;
  type: string;
}

export interface Inventory {
  id: string;
  productId: string;
  warehouseId: string;
  sku: string;
  productName: string;
  warehouseName: string;
  stockAvailable: number;
  stockReserved: number;
  lastUpdated?: string;
}

export interface Shipment {
  id: string;
  orderId: string;
  routeId?: string;
  customerName: string;
  customerEmail: string;
  shippingAddress: string;
  latitude?: number;
  longitude?: number;
  trackingNumber: string;
  deliveryStatus: string;
  estimatedDelivery?: string;
  actualDelivery?: string;
}

export interface Route {
  id: string;
  companyId?: string;
  carrierId?: string;
  routeDate?: string;
  originAddress: string;
  status: string;
  shipments?: Shipment[];
}

export interface CreateRoutePayload {
  companyId: string;
  carrierId: string;
  originAddress: string;
  shipmentIds: string[];
  optimizeRoute: boolean;
}

export interface InventoryCreationPayload {
  productId: string;
  warehouseId: string;
  stockAvailable: number;
}

export interface StockAdjustmentPayload {
  quantity: number;
  reason: string;
}

export interface StockReservationPayload {
  orderId: string;
  productId: string;
  warehouseId: string;
  quantity: number;
  companyId: string;
}

export const smartlogixService = {
  getRegiones: () => request<Region[]>('/smartlogix/order/regiones'),

  getComunas: (regionId: number) => request<Comuna[]>(`/smartlogix/order/comunas?regionId=${regionId}`),
  getOrders: () => request<Order[]>('/smartlogix/order/orders'),
  createOrder: (payload: CreateOrderPayload) => request<Order>('/smartlogix/order/orders', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  updateOrderStatus: (id: string, status: string) => request<Order>(`/smartlogix/order/orders/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status }),
  }),
  getProducts: () => request<Product[]>('/smartlogix/inventory/products'),
  createProduct: (payload: ProductPayload) => request<Product>('/smartlogix/inventory/products', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  updateProduct: (id: string, payload: ProductPayload) => request<Product>(`/smartlogix/inventory/products/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  }),
  getWarehouses: () => request<Warehouse[]>('/smartlogix/inventory/warehouses'),
  createWarehouse: (payload: WarehousePayload) => request<Warehouse>('/smartlogix/inventory/warehouses', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  updateWarehouse: (id: string, payload: WarehousePayload) => request<Warehouse>(`/smartlogix/inventory/warehouses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  }),
  getInventory: () => request<Inventory[]>('/smartlogix/inventory/stocks'),
  createInventory: (payload: InventoryCreationPayload) => request<Inventory>('/smartlogix/inventory/stocks', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  increaseInventory: (id: string, payload: StockAdjustmentPayload) => request<Inventory>(`/smartlogix/inventory/stocks/${id}/increase`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  }),
  reserveInventory: (payload: StockReservationPayload) => request('/smartlogix/inventory/reservations', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),


  getShipments: () => request<Shipment[]>('/smartlogix/shipping/shipments'),
  getRoutes: () => request<Route[]>('/smartlogix/shipping/routes'),
  createRoute: (payload: CreateRoutePayload) => request<Route>('/smartlogix/shipping/routes', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
};
