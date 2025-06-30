// src/pages/AccountSettingsPage.tsx
import { FC, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ArrowLeftIcon,
  EnvelopeIcon,
  EyeIcon,
  EyeSlashIcon,
  CheckCircleIcon,
} from '@heroicons/react/24/outline';
import api from '../services/api';

const PW_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

const AccountSettingsPage: FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [currentPwd, setCurrentPwd] = useState('');
  const [newPwd, setNewPwd] = useState('');
  const [confirmPwd, setConfirmPwd] = useState('');
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    api
      .get('/auth/me')
      .then(res => setEmail(res.data.email))
      .catch(err => {
        if (err.response?.status === 401) navigate('/login');
      });
  }, [navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (newPwd !== confirmPwd) {
      setError('New passwords do not match.');
      return;
    }
    if (!PW_REGEX.test(newPwd)) {
      setError(
        'New password must be ≥8 characters, with uppercase, lowercase & a digit.'
      );
      return;
    }

    try {
      await api.put('/auth/password', {
        oldPassword: currentPwd,
        newPassword: newPwd,
      });
      setSuccess(true);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      if (
        err &&
        typeof err === 'object' &&
        'response' in err &&
        (err as { response?: { status?: number } }).response?.status === 401
      ) {
        setError('Current password is incorrect.');
      } else {
        setError('Failed to update password. Try again.');
      }
    }
  };

  return (
    <div className="login-container">
      <form
        onSubmit={handleSubmit}
        className={`login-card relative overflow-visible ${
          success ? 'pt-16' : 'pt-6'
        }`}
      >
        <button
          type="button"
          onClick={() => navigate('/calendar')}
          className="back-btn"
        >
          <ArrowLeftIcon className="w-6 h-6 text-white" />
        </button>

        {success && (
          <div className="success-banner">
            <CheckCircleIcon className="banner-icon" />
            <span>Password updated!</span>
          </div>
        )}

        <h1 className="mb-4 text-2xl text-center font-semibold text-white">
          Account Settings
        </h1>

        {error && <p className="text-red-400 text-center mb-4">{error}</p>}

        <div className="input-wrapper">
          <input
            type="email"
            placeholder="Email"
            value={email}
            readOnly
            required
          />
          <EnvelopeIcon className="fa-icon" />
        </div>

        <h2 className="change-password-heading text-lg font-medium">
          Change Password
        </h2>

        <div className="input-wrapper">
          <input
            type={showCurrent ? 'text' : 'password'}
            placeholder="Current Password"
            value={currentPwd}
            onChange={e => setCurrentPwd(e.target.value)}
            required
          />
          {showCurrent ? (
            <EyeSlashIcon
              className="fa-icon"
              onClick={() => setShowCurrent(false)}
            />
          ) : (
            <EyeIcon className="fa-icon" onClick={() => setShowCurrent(true)} />
          )}
        </div>

        <div className="input-wrapper">
          <input
            type={showNew ? 'text' : 'password'}
            placeholder="New Password"
            value={newPwd}
            onChange={e => setNewPwd(e.target.value)}
            required
          />
          {showNew ? (
            <EyeSlashIcon
              className="fa-icon"
              onClick={() => setShowNew(false)}
            />
          ) : (
            <EyeIcon className="fa-icon" onClick={() => setShowNew(true)} />
          )}
        </div>

        <div className="input-wrapper">
          <input
            type={showConfirm ? 'text' : 'password'}
            placeholder="Confirm New Password"
            value={confirmPwd}
            onChange={e => setConfirmPwd(e.target.value)}
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
          Update Password
        </button>
      </form>
    </div>
  );
};

export default AccountSettingsPage;
