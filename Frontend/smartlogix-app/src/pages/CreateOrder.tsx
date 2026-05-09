import React, { useState } from 'react';
import { RegionComunaSelector } from '../components/RegionComunaSelector';
import { orderService, type CreateOrderPayload } from '../services/orderService';
import { Check, X } from 'lucide-react';

export const CreateOrder: React.FC<{ onBack: () => void }> = ({ onBack }) => {
  const [customerName, setCustomerName] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');
  const [street, setStreet] = useState('');
  const [comunaId, setComunaId] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState<{ type: 'error' | 'success'; text: string } | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!comunaId) {
      setMessage({ type: 'error', text: 'Debe seleccionar una comuna.' });
      return;
    }

    setIsSubmitting(true);
    setMessage(null);

    const payload: CreateOrderPayload = {
      customerName,
      customerEmail,
      street,
      comunaId,
      items: [{ productId: 'prd_01', quantity: 1, price: 1290 }]
    };

    try {
      await orderService.createOrder(payload);
      setMessage({ type: 'success', text: 'Pedido creado exitosamente.' });
      setTimeout(() => onBack(), 1500);
    } catch (err: any) {
      setMessage({ type: 'error', text: err.message || 'Error al crear el pedido' });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <header className="page-header">
        <div className="page-title-group">
          <div className="crumb">Operación / Órdenes</div>
          <h1>Crear Nuevo Pedido</h1>
        </div>
        <div className="page-actions">
          <button type="button" onClick={onBack} className="btn">
            Volver
          </button>
        </div>
      </header>

      <div className="page-body">
        {message && (
          <div className={`conn-banner ${message.type === 'success' ? 'ok' : ''}`} style={{marginBottom: 16}}>
            {message.type === 'success' ? <Check className="ico" /> : <X className="ico" />}
            {message.text}
          </div>
        )}

        <div className="card" style={{ maxWidth: 600, margin: '0 auto' }}>
          <div className="card-header">
            <h3>Datos del Pedido</h3>
          </div>
          <div className="card-body">
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="field">
                <label>Nombre del Cliente</label>
                <input required type="text" className="input" value={customerName} onChange={(e) => setCustomerName(e.target.value)} />
              </div>
              
              <div className="field">
                <label>Email del Cliente</label>
                <input required type="email" className="input" value={customerEmail} onChange={(e) => setCustomerEmail(e.target.value)} />
              </div>

              <div className="field">
                <label>Dirección (Calle y número)</label>
                <input required type="text" className="input" value={street} onChange={(e) => setStreet(e.target.value)} />
              </div>

              <RegionComunaSelector onComunaChange={setComunaId} />

              <div style={{ marginTop: 16, display: 'flex', justifyContent: 'flex-end' }}>
                <button type="submit" disabled={isSubmitting || !comunaId} className="btn btn-primary">
                  {isSubmitting ? 'Creando...' : 'Crear Pedido'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </>
  );
};
