import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Auth } from '../pages/Auth';
import { authService } from '../services/authService';
import { useAuthStore } from '../store/useAuthStore';

vi.mock('../services/authService', async () => {
  const actual = await vi.importActual<typeof import('../services/authService')>('../services/authService');
  return {
    ...actual,
    authService: {
      login: vi.fn(),
      register: vi.fn(),
    },
  };
});

describe('Auth page', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.getState().clearSession();
  });

  it('logs in and stores the returned session', async () => {
    vi.mocked(authService.login).mockResolvedValue({
      token: 'jwt',
      user: { email: 'admin@smartlogix.cl', companyId: 'company-1', role: 'ADMIN', name: 'Admin' },
    });

    render(<Auth />);
    fireEvent.click(screen.getByRole('button', { name: /Continuar/i }));

    await waitFor(() => expect(authService.login).toHaveBeenCalledWith('admin@smartlogix.cl', 'demo1234'));
    expect(useAuthStore.getState().token).toBe('jwt');
  });

  it('shows validation errors from the service', async () => {
    vi.mocked(authService.login).mockRejectedValue(new Error('Ingresa un correo electronico valido.'));

    render(<Auth />);
    fireEvent.change(screen.getByDisplayValue('admin@smartlogix.cl'), { target: { value: 'bad-email' } });
    fireEvent.click(screen.getByRole('button', { name: /Continuar/i }));

    expect(await screen.findByText('Ingresa un correo electronico valido.')).toBeInTheDocument();
  });
});
