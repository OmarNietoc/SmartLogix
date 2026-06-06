export interface DemoUser {
  email: string;
  password: string;
  name: string;
  companyName: string;
  role: 'ADMIN' | 'OPERATOR' | 'DRIVER' | 'VIEWER';
}

export const demoUsers: DemoUser[] = [
  {
    email: 'admin@smartlogix.cl',
    password: 'demo1234',
    name: 'Admin SmartLogix',
    companyName: 'SmartLogix Demo',
    role: 'ADMIN',
  },
  {
    email: 'operador@smartlogix.cl',
    password: 'demo1234',
    name: 'Operador SmartLogix',
    companyName: 'SmartLogix Demo',
    role: 'OPERATOR',
  },
  {
    email: 'conductor@smartlogix.cl',
    password: 'demo1234',
    name: 'Conductor SmartLogix',
    companyName: 'SmartLogix Demo',
    role: 'DRIVER',
  },
];

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const normalizeRut = (rut: string) => rut.replace(/[.-]/g, '').trim().toUpperCase();
//
export const isValidRut = (rut: string) => {
  const normalized = normalizeRut(rut);
  if (!/^\d{7,8}[0-9K]$/.test(normalized)) return false;

  const body = normalized.slice(0, -1);
  const verifier = normalized.slice(-1);
  let multiplier = 2;
  let sum = 0;

  for (let i = body.length - 1; i >= 0; i -= 1) {
    sum += Number(body[i]) * multiplier;
    multiplier = multiplier === 7 ? 2 : multiplier + 1;
  }

  const remainder = 11 - (sum % 11);
  const expected = remainder === 11 ? '0' : remainder === 10 ? 'K' : String(remainder);
  return verifier === expected;
};

export const validateLogin = (email: string, password: string) => {
  const normalizedEmail = email.trim().toLowerCase();

  if (!normalizedEmail) return 'Ingresa tu correo electrónico.';
  if (!emailPattern.test(normalizedEmail)) return 'Ingresa un correo electrónico válido.';
  if (!password) return 'Ingresa tu contraseña.';
  if (password.length < 8) return 'La contraseña debe tener al menos 8 caracteres.';

  return null;
};

export const validateRegister = (data: {
  companyName: string;
  taxId: string;
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}) => {
  if (!data.companyName.trim()) return 'Ingresa el nombre de la empresa.';
  if (!data.taxId.trim()) return 'Ingresa el RUT de la empresa.';
  if (!isValidRut(data.taxId)) return 'Ingresa un RUT de empresa valido.';
  if (!data.firstName.trim()) return 'Ingresa el nombre del usuario.';
  if (!data.lastName.trim()) return 'Ingresa el apellido del usuario.';
  if (!emailPattern.test(data.email.trim().toLowerCase())) return 'Ingresa un correo corporativo válido.';
  if (data.password.length < 8) return 'La contraseña debe tener al menos 8 caracteres.';
  return null;
};

import { request } from './api';

export const authService = {
  login: async (email: string, password: string) => {
    const validationError = validateLogin(email, password);
    if (validationError) throw new Error(validationError);

    const response = await request<{ token: string, email: string, companyId: string }>('/smartlogix/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: email.trim().toLowerCase(), password }),
    });

    return {
      token: response.token,
      user: {
        email: response.email,
        companyId: response.companyId,
        role: 'ADMIN', // Rol por defecto temporal
        name: response.email.split('@')[0],
      },
    };
  },

  register: async (data: {
    companyName: string;
    taxId: string;
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }) => {
    const validationError = validateRegister(data);
    if (validationError) throw new Error(validationError);

    const response = await request<{ token: string, email: string, companyId: string }>('/smartlogix/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        ...data,
        taxId: normalizeRut(data.taxId),
        email: data.email.trim().toLowerCase(),
        contactEmail: data.email.trim().toLowerCase(),
        phone: '123456789'
      }),
    });

    return {
      token: response.token,
      user: {
        email: response.email,
        companyId: response.companyId,
        role: 'ADMIN',
        name: `${data.firstName.trim()} ${data.lastName.trim()}`,
        companyName: data.companyName.trim(),
      },
    };
  },
};
