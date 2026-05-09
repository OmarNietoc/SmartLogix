import React, { useEffect, useMemo, useState } from 'react';
import { ArrowLeft, Check, Plus, RefreshCw, Trash2, X } from 'lucide-react';
import { RegionComunaSelector } from '../components/RegionComunaSelector';
import {
  smartlogixService,
  type CreateOrderPayload,
  type Inventory,
  type OrderItemPayload,
  type Product,
  type Warehouse,
} from '../services/smartlogixService';

const blankItem = (): OrderItemPayload => ({
  productId: '',
  warehouseId: '',
  productName: '',
  quantity: 1,
  price: 0,
});

export const CreateOrder: React.FC<{ onBack: () => void; onCreated?: () => void }> = ({ onBack, onCreated }) => {
  const [customerName, setCustomerName] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');
  const [street, setStreet] = useState('');
  const [comunaId, setComunaId] = useState<number | null>(null);
  const [items, setItems] = useState<OrderItemPayload[]>([blankItem()]);
  const [products, setProducts] = useState<Product[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [stock, setStock] = useState<Inventory[]>([]);
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState<{ type: 'error' | 'success'; text: string } | null>(null);

  useEffect(() => {
    let mounted = true;
    setCatalogLoading(true);

    Promise.allSettled([
      smartlogixService.getProducts(),
      smartlogixService.getWarehouses(),
      smartlogixService.getInventory(),
    ])
      .then(([productsResult, warehousesResult, stockResult]) => {
        if (!mounted) return;
        if (productsResult.status === 'fulfilled') setProducts(productsResult.value);
        if (warehousesResult.status === 'fulfilled') setWarehouses(warehousesResult.value);
        if (stockResult.status === 'fulfilled') setStock(stockResult.value);

        const failed = [productsResult, warehousesResult, stockResult].some((result) => result.status === 'rejected');
        if (failed) setMessage({ type: 'error', text: 'No se pudo cargar el catálogo completo. Revisa productos, bodegas y stock.' });
      })
      .finally(() => {
        if (mounted) setCatalogLoading(false);
      });

    return () => { mounted = false; };
  }, []);

  const productById = useMemo(() => new Map(products.map((product) => [product.id, product])), [products]);
  const warehouseById = useMemo(() => new Map(warehouses.map((warehouse) => [warehouse.id, warehouse])), [warehouses]);
  const hasStockOptions = stock.length > 0;
  const total = useMemo(() => items.reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.price || 0), 0), [items]);
  const canSubmit = Boolean(comunaId) && items.every((item) => item.productId && item.warehouseId && item.productName && item.quantity >= 1);

  const patchItem = (index: number, patch: Partial<OrderItemPayload>) => {
    setItems((current) => current.map((item, itemIndex) => itemIndex === index ? { ...item, ...patch } : item));
  };

  const selectStockItem = (index: number, inventoryId: string) => {
    const selectedStock = stock.find((entry) => entry.id === inventoryId);
    if (!selectedStock) {
      patchItem(index, blankItem());
      return;
    }

    const product = productById.get(selectedStock.productId);
    patchItem(index, {
      productId: selectedStock.productId,
      warehouseId: selectedStock.warehouseId,
      productName: selectedStock.productName || product?.name || selectedStock.sku,
      price: Number(product?.price || 0),
    });
  };

  const selectProduct = (index: number, productId: string) => {
    const product = productById.get(productId);
    patchItem(index, {
      productId,
      productName: product?.name || '',
      price: Number(product?.price || 0),
    });
  };

  const removeItem = (index: number) => {
    setItems((current) => current.length === 1 ? current : current.filter((_, itemIndex) => itemIndex !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const destinationError = validateDestination(street, comunaId);
    if (destinationError) {
      setMessage({ type: 'error', text: destinationError });
      return;
    }

    if (!comunaId) {
      setMessage({ type: 'error', text: 'Debes seleccionar una comuna.' });
      return;
    }

    if (!canSubmit) {
      setMessage({ type: 'error', text: 'Selecciona producto, bodega y cantidad para cada línea.' });
      return;
    }

    setIsSubmitting(true);
    setMessage(null);

    const payload: CreateOrderPayload = { customerName, customerEmail, street, comunaId, items };

    try {
      const createdOrder = await smartlogixService.createOrder(payload);
      setMessage({ type: 'success', text: 'Orden creada. Generando envío y ruta automática...' });
      await createAutomaticRoute(createdOrder.id);
      setMessage({ type: 'success', text: 'Orden creada. El envío quedó asociado a una ruta automática.' });
      onCreated?.();
      setTimeout(() => onBack(), 900);
    } catch (err) {
      setMessage({ type: 'error', text: err instanceof Error ? err.message : 'Error al crear la orden' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <header className="page-header">
        <div>
          <span className="eyebrow">Operación / Órdenes</span>
          <h1>Crear nueva orden</h1>
          <p>Registra cliente, dirección oficial y productos desde el catálogo del backend.</p>
        </div>
        <button type="button" onClick={onBack} className="btn">
          <ArrowLeft className="ico" /> Volver
        </button>
      </header>

      <div className="page-body narrow">
        {message && (
          <div className={`banner ${message.type === 'success' ? 'ok' : 'danger'}`}>
            {message.type === 'success' ? <Check className="ico" /> : <X className="ico" />}
            {message.text}
          </div>
        )}

        <form className="form-panel" onSubmit={handleSubmit}>
          <section>
            <h2>Cliente y destino</h2>
            <div className="form-grid two">
              <div className="field">
                <label>Nombre del cliente</label>
                <input required className="input" value={customerName} onChange={(e) => setCustomerName(e.target.value)} placeholder="Juan Pérez" />
              </div>
              <div className="field">
                <label>Email del cliente</label>
                <input required type="email" className="input" value={customerEmail} onChange={(e) => setCustomerEmail(e.target.value)} placeholder="juan@empresa.cl" />
              </div>
            </div>
            <div className="field">
              <label>Dirección</label>
              <input required className="input" value={street} onChange={(e) => setStreet(e.target.value)} placeholder="Av. Providencia 1234" />
              <span className="hint">Debe incluir calle y numeración. Región y comuna se validan contra el catálogo oficial.</span>
            </div>
            <RegionComunaSelector onComunaChange={setComunaId} />
          </section>

          <section>
            <div className="section-row">
              <div>
                <h2>Productos</h2>
                <span className="section-hint">
                  {catalogLoading ? 'Cargando catálogo...' : hasStockOptions ? 'Selecciona desde stock disponible por bodega.' : 'Selecciona producto y bodega del catálogo.'}
                </span>
              </div>
              <button type="button" className="btn btn-sm" onClick={() => setItems((current) => [...current, blankItem()])}>
                <Plus className="ico" /> Agregar
              </button>
            </div>

            {catalogLoading && (
              <div className="banner">
                <RefreshCw className="ico spin" />
                Cargando productos, bodegas y stock...
              </div>
            )}

            {!catalogLoading && products.length === 0 && stock.length === 0 && (
              <div className="banner danger">
                <X className="ico" />
                No hay productos disponibles. Carga productos en inventario antes de crear órdenes.
              </div>
            )}

            <div className="line-items">
              {items.map((item, index) => (
                <div className={`line-item ${hasStockOptions ? 'from-stock' : ''}`} key={index}>
                  {hasStockOptions ? (
                    <div className="field product-picker">
                      <label>Producto y bodega</label>
                      <select
                        required
                        className="select"
                        value={stock.find((entry) => entry.productId === item.productId && entry.warehouseId === item.warehouseId)?.id || ''}
                        onChange={(e) => selectStockItem(index, e.target.value)}
                      >
                        <option value="">Selecciona producto</option>
                        {stock.map((entry) => (
                          <option key={entry.id} value={entry.id}>
                            {entry.productName || entry.sku} · {entry.warehouseName} · {entry.stockAvailable} disp.
                          </option>
                        ))}
                      </select>
                    </div>
                  ) : (
                    <>
                      <div className="field product-picker">
                        <label>Producto</label>
                        <select required className="select" value={item.productId} onChange={(e) => selectProduct(index, e.target.value)}>
                          <option value="">Selecciona producto</option>
                          {products.map((product) => (
                            <option key={product.id} value={product.id}>
                              {product.name} · {product.sku}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="field">
                        <label>Bodega</label>
                        <select required className="select" value={item.warehouseId} onChange={(e) => patchItem(index, { warehouseId: e.target.value })}>
                          <option value="">Selecciona bodega</option>
                          {warehouses.map((warehouse) => (
                            <option key={warehouse.id} value={warehouse.id}>{warehouse.name}</option>
                          ))}
                        </select>
                      </div>
                    </>
                  )}

                  <div className="field">
                    <label>Cantidad</label>
                    <input required min={1} type="number" className="input" value={item.quantity} onChange={(e) => patchItem(index, { quantity: Number(e.target.value) })} />
                  </div>
                  <div className="field">
                    <label>Precio</label>
                    <input required min={0} type="number" className="input" value={item.price} onChange={(e) => patchItem(index, { price: Number(e.target.value) })} />
                  </div>
                  <button type="button" className="btn btn-icon" onClick={() => removeItem(index)} aria-label="Eliminar producto">
                    <Trash2 className="ico" />
                  </button>
                  {item.productId && (
                    <div className="line-item-meta">
                      {item.productName} · {warehouseById.get(item.warehouseId)?.name || item.warehouseId || 'Bodega'}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </section>

          <footer className="form-footer">
            <div>
              <span>Total estimado</span>
              <strong>{formatMoney(total)}</strong>
            </div>
            <button type="submit" disabled={isSubmitting || !canSubmit} className="btn btn-primary">
              {isSubmitting ? 'Creando...' : 'Crear orden'}
            </button>
          </footer>
        </form>
      </div>
    </>
  );
};

const formatMoney = (value: number) => new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 0,
}).format(value);

const validateDestination = (street: string, comunaId: number | null) => {
  const normalizedStreet = street.trim();
  if (!normalizedStreet) return 'Ingresa una dirección de entrega.';
  if (normalizedStreet.length < 6) return 'La dirección debe incluir calle y numeración.';
  if (!/\d/.test(normalizedStreet)) return 'La dirección debe incluir numeración para ubicar la entrega.';
  if (!comunaId) return 'Selecciona una región y comuna válida.';
  return null;
};

const createAutomaticRoute = async (orderId: string) => {
  const shipment = await waitForShipment(orderId);
  if (!shipment || shipment.routeId || shipment.deliveryStatus !== 'PENDING') return;

  await smartlogixService.createRoute({
    companyId: 'smartlogix-demo',
    carrierId: 'LOCAL',
    originAddress: 'Bodega Central Santiago, Av. Las Condes 1234, Las Condes, Región Metropolitana, Chile',
    shipmentIds: [shipment.id],
    optimizeRoute: true,
  });
};

const waitForShipment = async (orderId: string) => {
  for (let attempt = 0; attempt < 8; attempt += 1) {
    const shipments = await smartlogixService.getShipments();
    const shipment = shipments.find((candidate) => candidate.orderId === orderId);
    if (shipment) return shipment;
    await new Promise((resolve) => setTimeout(resolve, 900));
  }
  return null;
};
