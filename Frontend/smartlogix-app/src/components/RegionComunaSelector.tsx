import React, { useEffect, useState } from 'react';
import { orderService, type Region, type Comuna } from '../services/orderService';

interface RegionComunaSelectorProps {
  onComunaChange: (comunaId: number | null) => void;
}

export const RegionComunaSelector: React.FC<RegionComunaSelectorProps> = ({ onComunaChange }) => {
  const [regiones, setRegiones] = useState<Region[]>([]);
  const [comunas, setComunas] = useState<Comuna[]>([]);
  const [selectedRegionId, setSelectedRegionId] = useState<number | ''>('');
  const [selectedComunaId, setSelectedComunaId] = useState<number | ''>('');
  const [isLoadingRegiones, setIsLoadingRegiones] = useState(false);
  const [isLoadingComunas, setIsLoadingComunas] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let mounted = true;
    setIsLoadingRegiones(true);
    orderService.getRegiones()
      .then((data) => {
        if (mounted) setRegiones(data);
      })
      .catch((err) => {
        if (mounted) setError(err.message || 'Error al cargar regiones');
      })
      .finally(() => {
        if (mounted) setIsLoadingRegiones(false);
      });
    return () => { mounted = false; };
  }, []);

  const handleRegionChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const rId = e.target.value ? Number(e.target.value) : '';
    setSelectedRegionId(rId);
    // Limpiar comuna seleccionada
    setSelectedComunaId('');
    onComunaChange(null);
    setComunas([]);

    if (rId) {
      setIsLoadingComunas(true);
      orderService.getComunas(rId)
        .then(setComunas)
        .catch(() => setError('Error al cargar comunas'))
        .finally(() => setIsLoadingComunas(false));
    }
  };

  const handleComunaChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const cId = e.target.value ? Number(e.target.value) : '';
    setSelectedComunaId(cId);
    onComunaChange(cId === '' ? null : cId);
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
      <div className="field">
        <label htmlFor="region-select">Región</label>
        <select
          id="region-select"
          className="select"
          value={selectedRegionId}
          onChange={handleRegionChange}
          disabled={isLoadingRegiones || isLoadingComunas}
        >
          <option value="">{isLoadingRegiones ? 'Cargando regiones...' : 'Seleccione región'}</option>
          {regiones.map((r) => (
            <option key={r.id} value={r.id}>{r.nombre}</option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="comuna-select">Comuna</label>
        <select
          id="comuna-select"
          className="select"
          value={selectedComunaId}
          onChange={handleComunaChange}
          disabled={!selectedRegionId || isLoadingComunas}
        >
          <option value="">{isLoadingComunas ? 'Cargando comunas...' : 'Seleccione comuna'}</option>
          {comunas.map((c) => (
            <option key={c.id} value={c.id}>{c.nombre}</option>
          ))}
        </select>
      </div>
      
      {error && <p className="field-error" style={{ gridColumn: 'span 2' }}>{error}</p>}
    </div>
  );
};
