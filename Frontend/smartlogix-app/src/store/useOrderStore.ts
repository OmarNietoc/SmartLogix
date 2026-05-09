import { create } from 'zustand';

interface OrderState {
  orders: any[];
  isLoading: boolean;
  error: string | null;
  setOrders: (orders: any[]) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
}

export const useOrderStore = create<OrderState>((set) => ({
  orders: [],
  isLoading: false,
  error: null,
  setOrders: (orders) => set({ orders }),
  setLoading: (isLoading) => set({ isLoading }),
  setError: (error) => set({ error }),
}));
