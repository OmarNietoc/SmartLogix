import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach } from 'vitest';
import { RegionComunaSelector } from '../components/RegionComunaSelector';
import { orderService } from '../services/orderService';

vi.mock('../services/orderService', () => ({
  orderService: {
    getRegiones: vi.fn(),
    getComunas: vi.fn(),
  }
}));

describe('RegionComunaSelector', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('debería cargar regiones al montar y mantener deshabilitada comuna', async () => {
    (orderService.getRegiones as any).mockResolvedValue([{ id: 1, nombre: 'Metropolitana' }]);
    
    render(<RegionComunaSelector onComunaChange={vi.fn()} />);
    
    expect(screen.getByLabelText(/Región/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Comuna/i)).toBeDisabled();

    await waitFor(() => {
      expect(screen.getByText('Metropolitana')).toBeInTheDocument();
    });
  });

  it('debería limpiar comuna y cargar comunas al seleccionar una región', async () => {
    (orderService.getRegiones as any).mockResolvedValue([{ id: 1, nombre: 'Metropolitana' }]);
    (orderService.getComunas as any).mockResolvedValue([{ id: 101, nombre: 'Santiago' }]);
    const handleComunaChange = vi.fn();

    render(<RegionComunaSelector onComunaChange={handleComunaChange} />);
    
    await waitFor(() => expect(screen.getByText('Metropolitana')).toBeInTheDocument());

    fireEvent.change(screen.getByLabelText(/Región/i), { target: { value: '1' } });

    expect(handleComunaChange).toHaveBeenCalledWith(null); // Limpia
    
    await waitFor(() => {
      expect(screen.getByLabelText(/Comuna/i)).not.toBeDisabled();
      expect(screen.getByText('Santiago')).toBeInTheDocument();
    });
  });
});
