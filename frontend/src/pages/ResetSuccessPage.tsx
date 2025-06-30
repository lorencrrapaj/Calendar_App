// src/pages/ResetSuccessPage.tsx
import { FC } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircleIcon } from '@heroicons/react/24/solid';

const ResetSuccessPage: FC = () => (
  <div className="login-container">
    <div className="login-card">
      <h1>Password Updated</h1>

      {/* Green tick */}
      <CheckCircleIcon
        className="block mx-auto my-4"
        style={{ width: 48, height: 48, color: '#4BB543' }}
      />

      <p style={{ color: '#fff', textAlign: 'center' }}>
        Your password has been changed successfully.
      </p>

      <Link to="/login">
        <button style={{ width: '100%', marginTop: '1rem' }}>Login</button>
      </Link>
    </div>
  </div>
);

export default ResetSuccessPage;
