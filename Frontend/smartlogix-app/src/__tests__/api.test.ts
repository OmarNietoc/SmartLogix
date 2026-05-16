import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, request } from '../services/api';

describe('api request helper', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  it('adds JSON content type and bearer token from persisted session', async () => {
    localStorage.setItem('smartlogix-auth', JSON.stringify({ state: { token: 'jwt-token' } }));
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      text: () => Promise.resolve(JSON.stringify({ data: { ok: true } })),
    });
    vi.stubGlobal('fetch', fetchMock);

    const result = await request<{ ok: boolean }>('/smartlogix/order/orders');

    expect(result).toEqual({ ok: true });
    const [, init] = fetchMock.mock.calls[0];
    expect(init.headers.get('Content-Type')).toBe('application/json');
    expect(init.headers.get('Authorization')).toBe('Bearer jwt-token');
  });

  it('maps backend status codes to user-facing ApiError messages', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 404,
      text: () => Promise.resolve(JSON.stringify({ message: 'raw backend message' })),
    }));

    await expect(request('/missing')).rejects.toMatchObject({
      name: 'ApiError',
      status: 404,
      message: 'Recurso no encontrado.',
    } satisfies Partial<ApiError>);
  });

  it('wraps network failures in a stable error message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('ECONNREFUSED')));

    await expect(request('/smartlogix/order/orders')).rejects.toThrow('Error de red al contactar con el servidor');
  });
});
