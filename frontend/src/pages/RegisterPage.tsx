// src/pages/RegisterPage.tsx
import { FC, useState } from 'react';
import {
  EnvelopeIcon,
  EyeIcon,
  EyeSlashIcon,
  CheckCircleIcon,
} from '@heroicons/react/24/outline';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';

// simple email regex
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
// at least 8 chars, one upper, one lower, one digit
const PW_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

const RegisterPage: FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // client-side validations
    if (!EMAIL_REGEX.test(email)) {
      setError('Please enter a valid email address.');
      return;
    }
    if (!PW_REGEX.test(password)) {
      setError(
        'Password must be ≥8 characters, with uppercase, lowercase & a digit.'
      );
      return;
    }
    if (password !== confirm) {
      setError('Passwords do not match.');
      return;
    }

    try {
      await api.post('/auth/register', { email, password });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      if (
        err &&
        typeof err === 'object' &&
        'response' in err &&
        (err as { response?: { status?: number } }).response?.status === 409
      ) {
        setError('That email is already taken.');
      } else {
        setError('Registration failed. Please try again.');
      }
    }
  };

  return (
    <div className="login-container">
      <form
        className={`login-card overflow-visible ${success ? 'pt-16' : 'pt-6'}`}
        onSubmit={handleSubmit}
      >
        {success && (
          <div className="success-banner">
            <CheckCircleIcon className="banner-icon" />
            <span>Account created successfully!</span>
          </div>
        )}

        <h1 className="mb-4 text-2xl text-center font-semibold">Register</h1>

        {error && <p className="text-red-400 text-center mb-4">{error}</p>}

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

        <div className="input-wrapper">
          <input
            type={showConfirm ? 'text' : 'password'}
            placeholder="Confirm Password"
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
            <EyeIcon className="fa-icon" onClick={() => setShowConfirm(true)} />
          )}
        </div>

        <button
          type="submit"
          className="w-full py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition disabled:opacity-50"
          disabled={success}
        >
          Create Account
        </button>

        <p className="footer mt-4 text-center">
          Already have an account?{' '}
          <Link to="/login" className="underline">
            Login
          </Link>
        </p>
      </form>
    </div>
  );
};

export default RegisterPage;
