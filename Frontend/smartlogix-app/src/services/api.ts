const getBaseUrl = () => import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  data: unknown;

  constructor(status: number, message: string, data?: unknown) {
    super(message);
    this.status = status;
    this.data = data;
    this.name = 'ApiError';
  }
}

const getPersistedToken = () => {
  try {
    const rawSession = localStorage.getItem('smartlogix-auth');
    return rawSession ? JSON.parse(rawSession)?.state?.token as string | null : null;
  } catch {
    return null;
  }
};

export const request = async <T>(path: string, options: RequestInit = {}): Promise<T> => {
  const url = `${getBaseUrl()}${path}`;
  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const token = getPersistedToken();
  if (token && !token.startsWith('mock-')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  try {
    const response = await fetch(url, { ...options, headers });
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
      let errorMessage = data?.message || 'Error desconocido';

      if (response.status === 409) {
        errorMessage = 'El recurso ya existe o hay un conflicto de datos.';
      } else if (response.status === 404) {
        errorMessage = 'Recurso no encontrado.';
      } else if (response.status === 401) {
        errorMessage = 'No autorizado. Por favor inicia sesión.';
      } else if (response.status >= 500) {
        errorMessage = 'Error interno del servidor. Intenta más tarde.';
      }

      throw new ApiError(response.status, errorMessage, data);
    }

    return data?.data !== undefined ? data.data : data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new Error('Error de red al contactar con el servidor', { cause: error });
  }
};
