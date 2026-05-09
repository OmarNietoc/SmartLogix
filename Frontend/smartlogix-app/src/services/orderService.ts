import { smartlogixService, type Comuna, type CreateOrderPayload, type Region } from './smartlogixService';

export type { Comuna, CreateOrderPayload, Region };

export const orderService = {
  getRegiones: smartlogixService.getRegiones,
  getComunas: smartlogixService.getComunas,
  createOrder: smartlogixService.createOrder,
  getAllOrders: smartlogixService.getOrders,
  updateOrderStatus: smartlogixService.updateOrderStatus,
};
