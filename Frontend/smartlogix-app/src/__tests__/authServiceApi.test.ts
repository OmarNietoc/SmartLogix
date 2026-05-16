import { beforeEach, describe, expect, it, vi } from 'vitest';
import { authService } from '../services/authService';
import { request } from '../services/api';

vi.mock('../services/api', () => ({
  request: vi.fn(),
}));

describe('authService API integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('normalizes login email and maps backend response to session', async () => {
    vi.mocked(request).mockResolvedValue({ token: 'jwt', email: 'admin@smartlogix.cl', companyId: 'company-1' });

    const session = await authService.login('ADMIN@SMARTLOGIX.CL ', 'demo1234');

    expect(request).toHaveBeenCalledWith('/smartlogix/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: 'admin@smartlogix.cl', password: 'demo1234' }),
    });
    expect(session.user.name).toBe('admin');
    expect(session.user.companyId).toBe('company-1');
  });

  it('normalizes register payload and maps user/company data', async () => {
    vi.mocked(request).mockResolvedValue({ token: 'jwt', email: 'ana@empresa.cl', companyId: 'company-1' });

    const session = await authService.register({
      companyName: ' Logistica Demo ',
      taxId: '76.123.456-0',
      firstName: ' Ana ',
      lastName: ' Perez ',
      email: 'ANA@EMPRESA.CL ',
      password: 'demo1234',
    });

    expect(request).toHaveBeenCalledWith('/smartlogix/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        companyName: ' Logistica Demo ',
        taxId: '761234560',
        firstName: ' Ana ',
        lastName: ' Perez ',
        email: 'ana@empresa.cl',
        password: 'demo1234',
        contactEmail: 'ana@empresa.cl',
        phone: '123456789',
      }),
    });
    expect(session.user.name).toBe('Ana Perez');
    expect(session.user.companyName).toBe('Logistica Demo');
  });
});
