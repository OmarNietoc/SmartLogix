import { beforeEach, describe, expect, it } from 'vitest';
import { useAuthStore, type AuthUser } from '../store/useAuthStore';

const mockUser: AuthUser = {
  email: 'admin@smartlogix.cl',
  companyId: 'company-1',
  role: 'ADMIN',
  name: 'Juan Pérez',
  companyName: 'Mi Empresa',
};

describe('useAuthStore', () => {
  beforeEach(() => {
    useAuthStore.getState().clearSession();
  });

  it('initial state has null token and user', () => {
    const { token, user } = useAuthStore.getState();
    expect(token).toBeNull();
    expect(user).toBeNull();
  });

  it('setSession stores token and user', () => {
    useAuthStore.getState().setSession('jwt-token-123', mockUser);

    const { token, user } = useAuthStore.getState();
    expect(token).toBe('jwt-token-123');
    expect(user).toEqual(mockUser);
  });

  it('clearSession resets token and user to null', () => {
    useAuthStore.getState().setSession('jwt-token-123', mockUser);
    useAuthStore.getState().clearSession();

    const { token, user } = useAuthStore.getState();
    expect(token).toBeNull();
    expect(user).toBeNull();
  });

  it('setSession overwrites previous session', () => {
    const firstUser: AuthUser = { email: 'first@empresa.cl', companyId: 'c-1', role: 'USER' };
    const secondUser: AuthUser = { email: 'second@empresa.cl', companyId: 'c-2', role: 'ADMIN' };

    useAuthStore.getState().setSession('token-1', firstUser);
    useAuthStore.getState().setSession('token-2', secondUser);

    const { token, user } = useAuthStore.getState();
    expect(token).toBe('token-2');
    expect(user?.email).toBe('second@empresa.cl');
  });

  it('setSession preserves all user fields', () => {
    useAuthStore.getState().setSession('jwt-token-123', mockUser);

    const { user } = useAuthStore.getState();
    expect(user?.email).toBe('admin@smartlogix.cl');
    expect(user?.companyId).toBe('company-1');
    expect(user?.role).toBe('ADMIN');
    expect(user?.name).toBe('Juan Pérez');
    expect(user?.companyName).toBe('Mi Empresa');
  });
});
