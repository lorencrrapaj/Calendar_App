// src/pages/ResetPasswordPage.tsx
import { FC, useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { EyeIcon, EyeSlashIcon } from '@heroicons/react/24/outline';

function useQuery() {
  return new URLSearchParams(useLocation().search);
}

const ResetPasswordPage: FC = () => {
  const query = useQuery();
  const token = query.get('token') || '';
  const navigate = useNavigate();

  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!token) {
      // no token → back to login
      navigate('/login');
    }
  }, [token, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirm) {
      setError('Passwords do not match');
      return;
    }
    // call backend
    const res = await fetch('/api/auth/reset-password', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, password }),
    });
    if (res.ok) {
      navigate('/reset-success');
    } else {
      const json = await res.json();
      setError(json.message || 'Failed to reset');
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1>Reset Password</h1>
        {error && <p style={{ color: '#f55', textAlign: 'center' }}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <div className="input-wrapper">
            <input
              type={showPassword ? 'text' : 'password'}
              placeholder="New Password"
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

          <div className="input-wrapper">
            <input
              type={showConfirm ? 'text' : 'password'}
              placeholder="Confirm New Password"
              value={confirm}
              onChange={e => setConfirm(e.target.value)}
              required
            />
            {showConfirm ? (
              <EyeSlashIcon
                className="fa-icon"
                onClick={() => setShowConfirm(false)}
              />
            ) : (
              <EyeIcon
                className="fa-icon"
                onClick={() => setShowConfirm(true)}
              />
            )}
          </div>

          <button type="submit">Set new password</button>
        </form>
      </div>
    </div>
  );
};

export default ResetPasswordPage;
