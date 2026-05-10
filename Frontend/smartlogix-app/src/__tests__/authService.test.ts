import { describe, expect, it } from 'vitest';
import { isValidRut, normalizeRut, validateRegister } from '../services/authService';

const baseRegisterData = {
  companyName: 'Logistica Andina',
  taxId: '76.123.456-0',
  firstName: 'Ana',
  lastName: 'Perez',
  email: 'ana@empresa.cl',
  password: 'demo1234',
};

describe('authService RUT validation', () => {
  it('normalizes formatted company RUTs before sending', () => {
    expect(normalizeRut('76.123.456-0')).toBe('761234560');
  });

  it('accepts valid Chilean RUT check digits', () => {
    expect(isValidRut('76.123.456-0')).toBe(true);
  });

  it('rejects invalid Chilean RUT check digits before calling the backend', () => {
    expect(validateRegister({ ...baseRegisterData, taxId: '76.123.456-8' }))
      .toBe('Ingresa un RUT de empresa valido.');
  });
});
