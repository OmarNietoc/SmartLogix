import React, { useEffect } from 'react';
import { useOrderStore } from '../store/useOrderStore';
import { orderService } from '../services/orderService';
import { PlusCircle } from 'lucide-react';

export const Dashboard: React.FC<{ onCreateOrder: () => void }> = ({ onCreateOrder }) => {
  const { orders, setOrders, isLoading, setLoading, setError, error } = useOrderStore();

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    orderService.getAllOrders()
      .then(data => { if (mounted) setOrders(data); })
      .catch(err => { if (mounted) setError(err.message); })
      .finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, [setLoading, setOrders, setError]);

  return (
    <>
      <header className="page-header">
        <div className="page-title-group">
          <div className="crumb">Operación / Vista general</div>
          <h1>Dashboard de Pedidos</h1>
        </div>
        <div className="page-actions">
          <button onClick={onCreateOrder} className="btn btn-primary">
            <PlusCircle className="ico" /> Nuevo Pedido
          </button>
        </div>
      </header>

      <div className="page-body">
        {error && <div className="conn-banner" style={{marginBottom: 16}}>{error}</div>}

        <div className="table-wrap">
          <table className="data">
            <thead>
              <tr>
                <th>ID Pedido</th>
                <th>Cliente</th>
                <th>Destino</th>
                <th className="num">Total</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={5} className="empty">Cargando pedidos...</td></tr>
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={5}>
                    <div className="empty">
                      <h4>No hay pedidos</h4>
                      <p>Aún no hay pedidos registrados en el sistema.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                orders.map((o) => (
                  <tr key={o.id}>
                    <td className="mono">{o.id.substring(0,8)}</td>
                    <td>{o.customerName}</td>
                    <td>{o.street}, {o.comunaNombre || `Comuna ${o.comunaId}`}</td>
                    <td className="num">${o.total}</td>
                    <td>
                      <span className={`badge ${
                        o.status === 'PENDIENTE' ? 'amber' :
                        o.status === 'ENTREGADO' ? 'green' : 'blue'
                      }`}>
                        <span className="dot"></span> {o.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
};
