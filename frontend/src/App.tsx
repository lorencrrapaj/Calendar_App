// src/App.tsx
import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CalendarPage from './pages/CalendarPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import ResetSuccessPage from './pages/ResetSuccessPage';
import AccountSettingsPage from './pages/AccountSettingsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* redirect root → /login */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        {/* public */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route path="/reset-success" element={<ResetSuccessPage />} />

        {/* newly added: user settings / change‐password */}
        <Route path="/account-settings" element={<AccountSettingsPage />} />
        {/* protected */}
        <Route path="/calendar" element={<CalendarPage />} />

        {/* catch‐all back to login */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
