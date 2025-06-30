import { FC, useState } from 'react';
import {
  EnvelopeIcon,
  EyeIcon,
  EyeSlashIcon,
} from '@heroicons/react/24/outline';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

const LoginPage: FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      // POST /api/auth/login expects { email, password }
      const res = await api.post('/auth/login', { email, password });
      const token = res.data.token;
      localStorage.setItem('token', token);
      navigate('/calendar');
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'response' in err) {
        const errorResponse = err as { response?: { data?: string } };
        setError(errorResponse.response?.data || 'Login failed.');
      } else {
        setError('Login failed.');
      }
    }
  };

  return (
    <div className="login-container">
      <form className="login-card" onSubmit={handleSubmit}>
        <h1>Login</h1>

        {error && <p className="text-red-400 mb-4">{error}</p>}

        {/* Email */}
        <div className="input-wrapper">
          <input
            type="email"
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
          />
          <EnvelopeIcon className="fa-icon" />
        </div>

        {/* Password */}
        <div className="input-wrapper">
          <input
            type={showPassword ? 'text' : 'password'}
            placeholder="Password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          {showPassword ? (
            <EyeSlashIcon
              className="fa-icon"
              onClick={() => setShowPassword(false)}
            />
          ) : (
            <EyeIcon
              className="fa-icon"
              onClick={() => setShowPassword(true)}
            />
          )}
        </div>

        <div className="form-row">
          <label>
            <input type="checkbox" /> Remember Me
          </label>
          <Link to="/forgot-password">Forgot Password?</Link>
        </div>

        <button type="submit">Login</button>

        <div className="footer">
          Don’t have an account? <Link to="/register">Register</Link>
        </div>
      </form>
    </div>
  );
};

export default LoginPage;
