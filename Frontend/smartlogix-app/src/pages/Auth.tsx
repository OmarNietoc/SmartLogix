import React, { useState } from 'react';
import { BarChart3, Boxes, Route, ShieldCheck, Truck } from 'lucide-react';
import { authService } from '../services/authService';
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

        <section className="auth-copy">
          <h1>Control logístico para operar sin puntos ciegos.</h1>
          <p>Centraliza órdenes, stock, bodegas, rutas y entregas en una consola pensada para equipos operativos.</p>
          <div className="auth-metrics">
            <Metric icon={<Boxes />} label="Inventario" value="Multi-bodega" />
            <Metric icon={<Route />} label="Rutas" value="OSRM" />
            <Metric icon={<ShieldCheck />} label="Saga" value="RabbitMQ" />
          </div>
        </section>

        <div className="auth-foot">
          <Truck className="ico" />
          API Gateway en http://localhost:8080
        </div>
      </aside>

      <main className="auth-form-wrap">
        {mode === 'login' ? <LoginForm onSwitch={() => setMode('register')} /> : <RegisterForm onSwitch={() => setMode('login')} />}
      </main>
    </div>
  );
};

const Metric = ({ icon, label, value }: { icon: React.ReactElement<{ className?: string }>; label: string; value: string }) => (
  <div className="auth-metric">
    {React.cloneElement(icon, { className: 'ico' })}
    <span>{label}</span>
    <strong>{value}</strong>
  </div>
);

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
      const session = await authService.login(email, password);
      setSession(session.token, session.user);
    } catch (error) {
      setErr(error instanceof Error ? error.message : 'No se pudo iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="auth-form" onSubmit={submit} noValidate>
      <div className="form-heading">
        <BarChart3 className="heading-icon" />
        <div>
          <h1>Inicia sesión</h1>
          <p>Usuarios demo: admin, operador o conductor con contraseña demo1234.</p>
        </div>
      </div>

      <div className="segmented">
        <button type="button" className="active">Iniciar sesión</button>
        <button type="button" onClick={onSwitch}>Crear cuenta</button>
      </div>

      <div className="field">
        <label>Correo electrónico</label>
        <input className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
      </div>
      <div className="field">
        <label>Contraseña</label>
        <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={8} />
      </div>
      {err && <div className="field-error">{err}</div>}
      <button type="submit" className="btn btn-primary full" disabled={loading}>
        {loading ? 'Validando...' : 'Continuar'}
      </button>
    </form>
  );
};

const RegisterForm: React.FC<{ onSwitch: () => void }> = ({ onSwitch }) => {
  const { setSession } = useAuthStore();
  const [data, setData] = useState({ companyName: '', taxId: '', firstName: '', lastName: '', email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');

  const set = (key: keyof typeof data, value: string) => setData((current) => ({ ...current, [key]: value }));

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr('');
    setLoading(true);
    try {
      const session = await authService.register(data);
      setSession(session.token, session.user);
    } catch (error) {
      setErr(error instanceof Error ? error.message : 'No se pudo crear la cuenta');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="auth-form" onSubmit={submit} noValidate>
      <div className="form-heading">
        <ShieldCheck className="heading-icon" />
        <div>
          <h1>Crear cuenta</h1>
          <p>Registro demo mientras ms-auth no expone endpoints implementados.</p>
        </div>
      </div>

      <div className="segmented">
        <button type="button" onClick={onSwitch}>Iniciar sesión</button>
        <button type="button" className="active">Crear cuenta</button>
      </div>

      <div className="field">
        <label>Empresa</label>
        <input className="input" value={data.companyName} onChange={(e) => set('companyName', e.target.value)} required placeholder="Logística Andina S.A." />
      </div>
      <div className="field">
        <label>RUT empresa</label>
        <input className="input" value={data.taxId} onChange={(e) => set('taxId', e.target.value)} required placeholder="76.123.456-7" />
      </div>
      <div className="form-grid two">
        <div className="field">
          <label>Nombre</label>
          <input className="input" value={data.firstName} onChange={(e) => set('firstName', e.target.value)} required />
        </div>
        <div className="field">
          <label>Apellido</label>
          <input className="input" value={data.lastName} onChange={(e) => set('lastName', e.target.value)} required />
        </div>
      </div>
      <div className="field">
        <label>Correo corporativo</label>
        <input className="input" type="email" value={data.email} onChange={(e) => set('email', e.target.value)} required />
      </div>
      <div className="field">
        <label>Contraseña</label>
        <input className="input" type="password" value={data.password} onChange={(e) => set('password', e.target.value)} required minLength={8} />
      </div>
      {err && <div className="field-error">{err}</div>}
      <button type="submit" className="btn btn-primary full" disabled={loading}>
        {loading ? 'Creando...' : 'Crear cuenta'}
      </button>
    </form>
  );
};
