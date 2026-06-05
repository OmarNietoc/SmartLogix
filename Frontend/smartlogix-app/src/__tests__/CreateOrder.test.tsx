import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CreateOrder } from '../pages/CreateOrder';
import type { Order } from '../services/smartlogixService';

vi.mock('../services/smartlogixService', () => ({
  smartlogixService: {
    getProducts: vi.fn().mockResolvedValue([]),
    getWarehouses: vi.fn().mockResolvedValue([]),
    getInventory: vi.fn().mockResolvedValue([{
      id: 'inv-1',
      productId: 'prod-1',
      warehouseId: 'wh-1',
      productName: 'Producto Test',
      warehouseName: 'Bodega Central',
      sku: 'SKU-001',
      stockAvailable: 10,
    }]),
    createOrder: vi.fn(),
    getShipments: vi.fn().mockResolvedValue([]),
    createRoute: vi.fn().mockResolvedValue({}),
  },
}));

vi.mock('../components/RegionComunaSelector', () => ({
  RegionComunaSelector: ({ onComunaChange }: { onComunaChange: (id: number) => void }) => (
    <button data-testid="select-comuna" onClick={() => onComunaChange(101)}>
      Seleccionar comuna
    </button>
  ),
}));

import { smartlogixService } from '../services/smartlogixService';

describe('CreateOrder', () => {
  const onBack = vi.fn();
  const onCreated = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders form with customer and address fields', async () => {
    render(<CreateOrder onBack={onBack} />);

    await waitFor(() => {
      expect(screen.getByText('Crear nueva orden')).toBeInTheDocument();
    });

    expect(screen.getByPlaceholderText('Juan Pérez')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('juan@empresa.cl')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Av. Providencia 1234')).toBeInTheDocument();
  });

  it('renders back button and calls onBack when clicked', async () => {
    render(<CreateOrder onBack={onBack} />);

    await waitFor(() => {
      expect(screen.getByText('Volver')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByText('Volver'));
    expect(onBack).toHaveBeenCalledOnce();
  });

  it('shows loading catalog banner while fetching products', () => {
    render(<CreateOrder onBack={onBack} />);
    expect(screen.getByText(/Cargando productos/i)).toBeInTheDocument();
  });

  it('submit button is disabled when form is empty', async () => {
    render(<CreateOrder onBack={onBack} />);

    await waitFor(() => {
      expect(screen.queryByText(/Cargando catálogo/i)).not.toBeInTheDocument();
    });

    const submitBtn = screen.getByText('Crear orden');
    expect(submitBtn).toBeDisabled();
  });

  it('shows error when street is too short on submit', async () => {
    render(<CreateOrder onBack={onBack} />);

    await waitFor(() => {
      expect(screen.queryByText(/Cargando catálogo/i)).not.toBeInTheDocument();
    });

    fireEvent.change(screen.getByPlaceholderText('Juan Pérez'), { target: { value: 'Juan' } });
    fireEvent.change(screen.getByPlaceholderText('juan@empresa.cl'), { target: { value: 'juan@test.cl' } });
    fireEvent.change(screen.getByPlaceholderText('Av. Providencia 1234'), { target: { value: 'Av' } });
    fireEvent.click(screen.getByTestId('select-comuna'));

    const form = document.querySelector('form')!;
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText(/dirección debe incluir calle y numeración/i)).toBeInTheDocument();
    });
  });

  async function fillValidForm() {
    await waitFor(() => {
      expect(screen.queryByText(/Cargando catálogo/i)).not.toBeInTheDocument();
    });

    fireEvent.change(screen.getByPlaceholderText('Juan Pérez'), { target: { value: 'Juan Pérez' } });
    fireEvent.change(screen.getByPlaceholderText('juan@empresa.cl'), { target: { value: 'juan@test.cl' } });
    fireEvent.change(screen.getByPlaceholderText('Av. Providencia 1234'), { target: { value: 'Av. Principal 1234' } });
    fireEvent.click(screen.getByTestId('select-comuna'));

    // select stock item so canSubmit becomes true
    const stockSelect = await screen.findByRole('combobox');
    fireEvent.change(stockSelect, { target: { value: 'inv-1' } });
  }

  it('calls smartlogixService.createOrder on valid submit', async () => {
    const mockOrder = { id: 'order-new' };
    vi.mocked(smartlogixService.createOrder).mockResolvedValueOnce(mockOrder as Partial<Order> as Order);
    vi.mocked(smartlogixService.getShipments).mockResolvedValue([]);

    render(<CreateOrder onBack={onBack} onCreated={onCreated} />);
    await fillValidForm();

    const form = document.querySelector('form')!;
    fireEvent.submit(form);

    await waitFor(() => {
      expect(smartlogixService.createOrder).toHaveBeenCalledOnce();
    });
  });

  it('shows error banner when createOrder fails', async () => {
    vi.mocked(smartlogixService.createOrder).mockRejectedValueOnce(new Error('Error de red'));

    render(<CreateOrder onBack={onBack} />);
    await fillValidForm();

    const form = document.querySelector('form')!;
    fireEvent.submit(form);

    await waitFor(() => {
      expect(screen.getByText('Error de red')).toBeInTheDocument();
    });
  });
});
