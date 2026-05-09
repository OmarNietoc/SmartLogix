import React, { useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';

export const Auth: React.FC = () => {
  const [mode, setMode] = useState<'login' | 'register'>('login');

  return (
    <div className="auth-shell">
      <aside className="auth-side">
        <div className="brand-large">
          <div className="brand-mark">SL</div>
          <span>SmartLogix</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16, position: 'relative', zIndex: 1 }}>
          <h2>Tu operación logística, bajo control.</h2>
          <p>Gestiona inventario multi-bodega, órdenes, rutas y entregas en tiempo real desde una sola consola.</p>
          <div style={{ display: 'flex', gap: 24, marginTop: 16, color: 'var(--text-secondary)', fontSize: 13 }}>
            <div><div style={{ fontSize: 22, fontWeight: 600, color: 'var(--text)', letterSpacing: '-0.02em' }}>4.2k</div>órdenes / mes</div>
            <div><div style={{ fontSize: 22, fontWeight: 600, color: 'var(--text)', letterSpacing: '-0.02em' }}>98.7%</div>entregas a tiempo</div>
            <div><div style={{ fontSize: 22, fontWeight: 600, color: 'var(--text)', letterSpacing: '-0.02em' }}>12</div>integraciones</div>
          </div>
        </div>
        <div style={{ fontSize: 12, color: 'var(--text-tertiary)', position: 'relative', zIndex: 1 }}>© 2026 SmartLogix · Versión 2.4</div>
      </aside>
      <div className="auth-form-wrap">
        {mode === 'login' ? <LoginForm onSwitch={() => setMode('register')} /> : <RegisterForm onSwitch={() => setMode('login')} />}
      </div>
    </div>
  );
};

const LoginForm: React.FC<{ onSwitch: () => void }> = ({ onSwitch }) => {
  const { setSession } = useAuthStore();
  const [email, setEmail] = useState('admin@smartlogix.cl');
  const [password, setPassword] = useState('demo1234');
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  const submit = async (e: React.FormEvent) => {
    e.preventDefault(); 
    setErr(''); 
    setLoading(true);
    try {
      // Simulación de login - fase 7 usará JWT real
      await new Promise(r => setTimeout(r, 1000));
      if (!email.includes('@')) throw new Error('Credenciales inválidas');
      setSession('mock-jwt-token', { name: 'Admin', email, companyName: 'SmartLogix' });
    } catch (error: any) { 
      setErr(error.message); 
    } finally { 
      setLoading(false); 
    }
  };

  return (
    <form className="auth-form" onSubmit={submit}>
      <div>
        <h1>Inicia sesión</h1>
        <p className="sub">Bienvenido de vuelta a tu panel de operaciones.</p>
      </div>
      <div className="auth-toggle">
        <button type="button" className="active">Iniciar sesión</button>
        <button type="button" onClick={onSwitch}>Crear cuenta</button>
      </div>
      <div className="field">
        <label>Correo electrónico</label>
        <input className="input" type="email" value={email} onChange={e => setEmail(e.target.value)} required autoFocus />
      </div>
      <div className="field">
        <label>Contraseña</label>
        <input className="input" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
      </div>
      {err && <div className="field-error">{err}</div>}
      <button type="submit" className="btn btn-accent" disabled={loading} style={{ justifyContent: 'center', padding: '8px 12px' }}>
        {loading ? 'Ingresando…' : 'Continuar'}
      </button>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 12 }}>
        <a href="#">¿Olvidaste tu contraseña?</a>
        <span style={{ color: 'var(--text-tertiary)' }}>SSO próximamente</span>
      </div>
    </form>
  );
};

const RegisterForm: React.FC<{ onSwitch: () => void }> = ({ onSwitch }) => {
  const { setSession } = useAuthStore();
  const [data, setData] = useState({ companyName: '', taxId: '', firstName: '', lastName: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  
  const set = (k: string, v: string) => setData(d => ({ ...d, [k]: v }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault(); 
    setErr(''); 
    setLoading(true);
    try { 
      await new Promise(r => setTimeout(r, 1000));
      setSession('mock-jwt-token', { name: `${data.firstName} ${data.lastName}`, email: data.email, companyName: data.companyName });
    } catch (error: any) { 
      setErr(error.message); 
    } finally { 
      setLoading(false); 
    }
  };

  return (
    <form className="auth-form" onSubmit={submit}>
      <div>
        <h1>Crear cuenta</h1>
        <p className="sub">Empieza a gestionar tu logística en minutos.</p>
      </div>
      <div className="auth-toggle">
        <button type="button" onClick={onSwitch}>Iniciar sesión</button>
        <button type="button" className="active">Crear cuenta</button>
      </div>
      <div className="field">
        <label>Nombre de la empresa</label>
        <input className="input" value={data.companyName} onChange={e => set('companyName', e.target.value)} required placeholder="Logística Andina S.A." />
      </div>
      <div className="field">
        <label>RUT empresa</label>
        <input className="input" value={data.taxId} onChange={e => set('taxId', e.target.value)} required placeholder="76.123.456-7" />
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
        <div className="field">
          <label>Nombre</label>
          <input className="input" value={data.firstName} onChange={e => set('firstName', e.target.value)} required />
        </div>
        <div className="field">
          <label>Apellido</label>
          <input className="input" value={data.lastName} onChange={e => set('lastName', e.target.value)} required />
        </div>
      </div>
      <div className="field">
        <label>Correo corporativo</label>
        <input className="input" type="email" value={data.email} onChange={e => set('email', e.target.value)} required />
      </div>
      <div className="field">
        <label>Contraseña</label>
        <input className="input" type="password" value={data.password} onChange={e => set('password', e.target.value)} required minLength={6} />
        <span className="hint">Mínimo 6 caracteres.</span>
      </div>
      {err && <div className="field-error">{err}</div>}
      <button type="submit" className="btn btn-accent" disabled={loading} style={{ justifyContent: 'center', padding: '8px 12px' }}>
        {loading ? 'Creando…' : 'Crear cuenta y continuar'}
      </button>
    </form>
  );
};
