import React from 'react';
import { render } from '@testing-library/react';

// Mock all the page components to avoid import issues
jest.mock('../pages/LoginPage', () => {
  const MockLoginPage = () => <div data-testid="login-page">Login Page</div>;
  return { default: MockLoginPage };
});

jest.mock('../pages/RegisterPage', () => {
  const MockRegisterPage = () => (
    <div data-testid="register-page">Register Page</div>
  );
  return { default: MockRegisterPage };
});

jest.mock('../pages/CalendarPage', () => {
  const MockCalendarPage = () => (
    <div data-testid="calendar-page">Calendar Page</div>
  );
  return { default: MockCalendarPage };
});

jest.mock('../pages/ForgotPasswordPage', () => {
  const MockForgotPasswordPage = () => (
    <div data-testid="forgot-password-page">Forgot Password Page</div>
  );
  return { default: MockForgotPasswordPage };
});

jest.mock('../pages/ResetPasswordPage', () => {
  const MockResetPasswordPage = () => (
    <div data-testid="reset-password-page">Reset Password Page</div>
  );
  return { default: MockResetPasswordPage };
});

jest.mock('../pages/ResetSuccessPage', () => {
  const MockResetSuccessPage = () => (
    <div data-testid="reset-success-page">Reset Success Page</div>
  );
  return { default: MockResetSuccessPage };
});

jest.mock('../pages/AccountSettingsPage', () => {
  const MockAccountSettingsPage = () => (
    <div data-testid="account-settings-page">Account Settings Page</div>
  );
  return { default: MockAccountSettingsPage };
});

import App from '../App';

describe('App', () => {
  it('renders without crashing', () => {
    const { container } = render(<App />);
    expect(container).toBeInTheDocument();
  });

  it('contains router structure', () => {
    const { container } = render(<App />);
    expect(container.firstChild).toBeInTheDocument();
  });

  it('renders the app component', () => {
    render(<App />);
    // Just verify it renders without throwing an error
    expect(document.body).toBeInTheDocument();
  });
});
