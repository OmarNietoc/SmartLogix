import { request } from './api';

export interface OrderItemPayload {
  productId: string;
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

export const orderService = {
  getRegiones: () => {
    return request<Region[]>('/smartlogix/order/regiones', { method: 'GET' });
  },

  getComunas: (regionId: number) => {
    return request<Comuna[]>(`/smartlogix/order/comunas?regionId=${regionId}`, { method: 'GET' });
  },

  createOrder: (payload: CreateOrderPayload) => {
    return request<any>('/smartlogix/order/orders', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  getAllOrders: () => {
    return request<any[]>('/smartlogix/order/orders', { method: 'GET' });
  },

  getOrderById: (id: string) => {
    return request<any>(`/smartlogix/order/orders/${id}`, { method: 'GET' });
  },

  updateOrderStatus: (id: string, status: string) => {
    return request<any>(`/smartlogix/order/orders/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    });
  }
};
