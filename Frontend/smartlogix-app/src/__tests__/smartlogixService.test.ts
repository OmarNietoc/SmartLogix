import { beforeEach, describe, expect, it, vi } from 'vitest';
import { smartlogixService } from '../services/smartlogixService';
import { request } from '../services/api';

vi.mock('../services/api', () => ({
  request: vi.fn(),
}));

describe('smartlogixService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(request).mockResolvedValue({});
  });

  it('calls order endpoints with expected REST paths and payloads', async () => {
    const payload = {
      customerName: 'Ana Perez',
      customerEmail: 'ana@empresa.cl',
      street: 'Av. Providencia 1234',
      comunaId: 13123,
      items: [{ productId: 'p1', warehouseId: 'w1', productName: 'Notebook', quantity: 2, price: 1000 }],
    };

    await smartlogixService.createOrder(payload);
    await smartlogixService.updateOrderStatus('order-1', 'CONFIRMED');

    expect(request).toHaveBeenNthCalledWith(1, '/smartlogix/order/orders', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    expect(request).toHaveBeenNthCalledWith(2, '/smartlogix/order/orders/order-1/status', {
      method: 'PUT',
      body: JSON.stringify({ status: 'CONFIRMED' }),
    });
  });

  it('calls inventory, shipping and route endpoints', async () => {
    await smartlogixService.getRegiones();
    await smartlogixService.getComunas(13);
    await smartlogixService.getOrders();
    await smartlogixService.getProducts();
    await smartlogixService.updateProduct('product-1', { sku: 'SKU-1', name: 'Producto', price: 1000 });
    await smartlogixService.getWarehouses();
    await smartlogixService.createWarehouse({ name: 'Central', locationAddress: 'Av. 1', type: 'DISTRIBUTION_CENTER' });
    await smartlogixService.updateWarehouse('warehouse-1', { name: 'Central', locationAddress: 'Av. 1', type: 'DISTRIBUTION_CENTER' });
    await smartlogixService.getInventory();
    await smartlogixService.createInventory({ productId: 'p1', warehouseId: 'w1', stockAvailable: 10 });
    await smartlogixService.increaseInventory('stock-1', { quantity: 5, reason: 'Ingreso' });
    await smartlogixService.reserveInventory({ orderId: 'o1', productId: 'p1', warehouseId: 'w1', quantity: 1, companyId: 'c1' });
    await smartlogixService.getShipments();
    await smartlogixService.getRoutes();
    await smartlogixService.createRoute({
      companyId: 'company-1',
      carrierId: 'LOCAL',
      originAddress: 'Bodega Central',
      shipmentIds: ['shipment-1'],
      optimizeRoute: true,
    });

    expect(request).toHaveBeenNthCalledWith(1, '/smartlogix/order/regiones');
    expect(request).toHaveBeenNthCalledWith(2, '/smartlogix/order/comunas?regionId=13');
    expect(request).toHaveBeenNthCalledWith(3, '/smartlogix/order/orders');
    expect(request).toHaveBeenNthCalledWith(4, '/smartlogix/inventory/products');
    expect(request).toHaveBeenNthCalledWith(5, '/smartlogix/inventory/products/product-1', {
      method: 'PUT',
      body: JSON.stringify({ sku: 'SKU-1', name: 'Producto', price: 1000 }),
    });
    expect(request).toHaveBeenNthCalledWith(6, '/smartlogix/inventory/warehouses');
    expect(request).toHaveBeenNthCalledWith(7, '/smartlogix/inventory/warehouses', {
      method: 'POST',
      body: JSON.stringify({ name: 'Central', locationAddress: 'Av. 1', type: 'DISTRIBUTION_CENTER' }),
    });
    expect(request).toHaveBeenNthCalledWith(8, '/smartlogix/inventory/warehouses/warehouse-1', {
      method: 'PUT',
      body: JSON.stringify({ name: 'Central', locationAddress: 'Av. 1', type: 'DISTRIBUTION_CENTER' }),
    });
    expect(request).toHaveBeenNthCalledWith(9, '/smartlogix/inventory/stocks');
    expect(request).toHaveBeenNthCalledWith(10, '/smartlogix/inventory/stocks', {
      method: 'POST',
      body: JSON.stringify({ productId: 'p1', warehouseId: 'w1', stockAvailable: 10 }),
    });
    expect(request).toHaveBeenNthCalledWith(11, '/smartlogix/inventory/stocks/stock-1/increase', {
      method: 'PATCH',
      body: JSON.stringify({ quantity: 5, reason: 'Ingreso' }),
    });
    expect(request).toHaveBeenNthCalledWith(12, '/smartlogix/inventory/reservations', {
      method: 'POST',
      body: JSON.stringify({ orderId: 'o1', productId: 'p1', warehouseId: 'w1', quantity: 1, companyId: 'c1' }),
    });
    expect(request).toHaveBeenNthCalledWith(13, '/smartlogix/shipping/shipments');
    expect(request).toHaveBeenNthCalledWith(14, '/smartlogix/shipping/routes');
    expect(request).toHaveBeenNthCalledWith(15, '/smartlogix/shipping/routes', {
      method: 'POST',
      body: JSON.stringify({
        companyId: 'company-1',
        carrierId: 'LOCAL',
        originAddress: 'Bodega Central',
        shipmentIds: ['shipment-1'],
        optimizeRoute: true,
      }),
    });
  });
});
