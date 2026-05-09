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
  if (!data.firstName.trim()) return 'Ingresa el nombre del usuario.';
  if (!data.lastName.trim()) return 'Ingresa el apellido del usuario.';
  if (!emailPattern.test(data.email.trim().toLowerCase())) return 'Ingresa un correo corporativo válido.';
  if (data.password.length < 8) return 'La contraseña debe tener al menos 8 caracteres.';
  return null;
};

export const authService = {
  login: async (email: string, password: string) => {
    const validationError = validateLogin(email, password);
    if (validationError) throw new Error(validationError);

    await new Promise((resolve) => setTimeout(resolve, 350));

    const normalizedEmail = email.trim().toLowerCase();
    const user = demoUsers.find((candidate) => candidate.email === normalizedEmail);
    if (!user || user.password !== password) {
      throw new Error('Correo o contraseña incorrectos.');
    }

    return {
      token: `mock-jwt-token-${user.role.toLowerCase()}`,
      user: {
        name: user.name,
        email: user.email,
        companyName: user.companyName,
        role: user.role,
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

    await new Promise((resolve) => setTimeout(resolve, 350));

    const normalizedEmail = data.email.trim().toLowerCase();
    if (demoUsers.some((user) => user.email === normalizedEmail)) {
      throw new Error('Ya existe un usuario demo con ese correo.');
    }

    return {
      token: 'mock-jwt-token-admin',
      user: {
        name: `${data.firstName.trim()} ${data.lastName.trim()}`,
        email: normalizedEmail,
        companyName: data.companyName.trim(),
        role: 'ADMIN',
      },
    };
  },
};
