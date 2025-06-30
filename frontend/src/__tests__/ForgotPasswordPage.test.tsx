import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import ForgotPasswordPage from '../pages/ForgotPasswordPage';

// Mock window.alert
const mockAlert = jest.fn();
global.alert = mockAlert;

// Wrapper component for router
const ForgotPasswordPageWrapper = () => (
  <BrowserRouter>
    <ForgotPasswordPage />
  </BrowserRouter>
);

describe('ForgotPasswordPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the forgot password form', () => {
    render(<ForgotPasswordPageWrapper />);

    expect(
      screen.getByRole('heading', { name: 'Forgot Password' })
    ).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: 'Send reset link' })
    ).toBeInTheDocument();
  });

  it('displays the correct description text', () => {
    render(<ForgotPasswordPageWrapper />);

    expect(
      screen.getByText(content => {
        return (
          content.includes('Enter your email and we') &&
          content.includes('send you a link')
        );
      })
    ).toBeInTheDocument();
  });

  it('renders the login link', () => {
    render(<ForgotPasswordPageWrapper />);

    expect(screen.getByText('Remembered?')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Login' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Login' })).toHaveAttribute(
      'href',
      '/login'
    );
  });

  it('renders the envelope icon', () => {
    const { container } = render(<ForgotPasswordPageWrapper />);

    // Check for the envelope icon (SVG element)
    const svgElement = container.querySelector('svg');
    expect(svgElement).toBeInTheDocument();
  });

  it('renders the exclamation circle icon', () => {
    const { container } = render(<ForgotPasswordPageWrapper />);

    // Check for SVG elements (there should be 2: exclamation circle and envelope)
    const svgElements = container.querySelectorAll('svg');
    expect(svgElements).toHaveLength(2);
  });

  it('allows user to type in email field', async () => {
    const user = userEvent.setup();
    render(<ForgotPasswordPageWrapper />);

    const emailInput = screen.getByPlaceholderText('Email');
    await user.type(emailInput, 'test@example.com');

    expect(emailInput).toHaveValue('test@example.com');
  });

  it('submits form and shows alert with email', async () => {
    const user = userEvent.setup();
    render(<ForgotPasswordPageWrapper />);

    const emailInput = screen.getByPlaceholderText('Email');
    const submitButton = screen.getByRole('button', {
      name: 'Send reset link',
    });

    await user.type(emailInput, 'test@example.com');
    await user.click(submitButton);

    expect(mockAlert).toHaveBeenCalledWith(
      'Reset link sent to test@example.com'
    );
  });

  it('requires email field to be filled', async () => {
    const user = userEvent.setup();
    render(<ForgotPasswordPageWrapper />);

    const emailInput = screen.getByPlaceholderText('Email');
    const submitButton = screen.getByRole('button', {
      name: 'Send reset link',
    });

    expect(emailInput).toBeRequired();
    expect(emailInput).toHaveAttribute('type', 'email');

    // Try to submit without filling email
    await user.click(submitButton);

    // The form should not submit (alert should not be called)
    expect(mockAlert).not.toHaveBeenCalled();
  });

  it('has correct form structure', () => {
    const { container } = render(<ForgotPasswordPageWrapper />);

    const form = container.querySelector('form');
    expect(form).toBeInTheDocument();

    const inputWrapper = container.querySelector('.input-wrapper');
    expect(inputWrapper).toBeInTheDocument();
  });
});
