// src/pages/ForgotPasswordPage.tsx
import { FC, useState } from 'react';
import {
  EnvelopeIcon,
  ExclamationCircleIcon,
} from '@heroicons/react/24/outline';
import { Link } from 'react-router-dom';

const ForgotPasswordPage: FC = () => {
  const [email, setEmail] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: send reset link to `email`
    alert(`Reset link sent to ${email}`);
  };

  return (
    <div className="login-container">
      <div className="login-card" style={{ maxWidth: 400 }}>
        {/* Top icon */}
        <ExclamationCircleIcon
          className="block mx-auto mb-4"
          width={56}
          height={56}
          style={{ color: '#4BB543' }} // or any accent color
        />

        <h1 style={{ textAlign: 'center', marginBottom: '0.5rem' }}>
          Forgot Password
        </h1>
        <p
          style={{
            textAlign: 'center',
            color: '#fff',
            marginBottom: '1.5rem',
            fontSize: '0.95rem',
          }}
        >
          Enter your email and we’ll send you a link to reset your password.
        </p>

        <form onSubmit={handleSubmit}>
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

          <button type="submit" style={{ marginTop: '1rem' }}>
            Send reset link
          </button>
        </form>

        <div className="footer" style={{ marginTop: '1rem' }}>
          Remembered?{' '}
          <Link
            to="/login"
            style={{ textDecoration: 'underline', color: '#fff' }}
          >
            Login
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;
