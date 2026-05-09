const getBaseUrl = () => import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  data: any;

  constructor(status: number, message: string, data?: any) {
    super(message);
    this.status = status;
    this.data = data;
    this.name = 'ApiError';
  }
}

export const request = async <T>(path: string, options: RequestInit = {}): Promise<T> => {
  const url = `${getBaseUrl()}${path}`;
  const headers = new Headers(options.headers || {});
  
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  // En el futuro, aquí se inyectará el token JWT en Authorization
  // const token = useAuthStore.getState().token;
  // if (token) headers.set('Authorization', `Bearer ${token}`);

  try {
    const response = await fetch(url, { ...options, headers });
    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
      let errorMessage = data?.message || 'Error desconocido';
      
      // Manejo centralizado de errores según rúbrica EV02
      if (response.status === 409) {
        errorMessage = 'El recurso ya existe o hay un conflicto de datos.';
      } else if (response.status === 404) {
        errorMessage = 'Recurso no encontrado.';
      } else if (response.status === 401) {
        errorMessage = 'No autorizado. Por favor inicie sesión.';
      } else if (response.status >= 500) {
        errorMessage = 'Error interno del servidor. Intente más tarde.';
      }

      throw new ApiError(response.status, errorMessage, data);
    }

    // Backend retorna { statusCode, message, data: { ... } }
    return data?.data !== undefined ? data.data : data;
  } catch (error) {
    if (error instanceof ApiError) throw error;
    throw new Error('Error de red al contactar con el servidor');
  }
};
