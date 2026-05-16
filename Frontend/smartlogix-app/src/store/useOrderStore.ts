import { create } from 'zustand';
import type { Order } from '../services/smartlogixService';

interface OrderState {
  orders: Order[];
  isLoading: boolean;
  error: string | null;
  setOrders: (orders: Order[]) => void;
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
