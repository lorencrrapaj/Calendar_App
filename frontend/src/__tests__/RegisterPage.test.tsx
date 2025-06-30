import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import RegisterPage from '../pages/RegisterPage';
import api from '../services/api';

// Mock the API module
jest.mock('../services/api');
const mockedApi = api as jest.Mocked<typeof api>;

// Mock useNavigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const renderRegisterPage = () => {
  return render(
    <BrowserRouter>
      <RegisterPage />
    </BrowserRouter>
  );
};

describe('RegisterPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockNavigate.mockClear();
  });

  it('renders registration form with all elements', () => {
    renderRegisterPage();

    expect(
      screen.getByRole('heading', { name: /register/i })
    ).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/^password$/i)).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText(/confirm password/i)
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: /create account/i })
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /login/i })).toBeInTheDocument();
  });

  it('allows user to type in all input fields', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');

    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('Password123');
    expect(confirmInput).toHaveValue('Password123');
  });

  it('toggles password visibility for password field', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    await user.type(passwordInput, 'Password123');

    // Initially password should be hidden
    expect(passwordInput).toHaveAttribute('type', 'password');

    // Find and click the eye icon for password field
    const passwordWrapper = passwordInput.closest('.input-wrapper');
    const eyeIcon = passwordWrapper?.querySelector('svg.fa-icon');

    if (eyeIcon) {
      await user.click(eyeIcon);
      expect(passwordInput).toHaveAttribute('type', 'text');

      // Click again to hide password
      const eyeSlashIcon = passwordWrapper?.querySelector('svg.fa-icon');
      if (eyeSlashIcon) {
        await user.click(eyeSlashIcon);
      }
    }
    expect(passwordInput).toHaveAttribute('type', 'password');
  });

  it('toggles password visibility for confirm password field', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    await user.type(confirmInput, 'Password123');

    // Initially password should be hidden
    expect(confirmInput).toHaveAttribute('type', 'password');

    // Find and click the eye icon for confirm password field
    const confirmWrapper = confirmInput.closest('.input-wrapper');
    const eyeIcon = confirmWrapper?.querySelector('svg.fa-icon');

    if (eyeIcon) {
      await user.click(eyeIcon);
      expect(confirmInput).toHaveAttribute('type', 'text');

      // Click again to hide password
      const eyeSlashIcon = confirmWrapper?.querySelector('svg.fa-icon');
      if (eyeSlashIcon) {
        await user.click(eyeSlashIcon);
      }
    }
    expect(confirmInput).toHaveAttribute('type', 'password');
  });

  it('shows error for invalid email format', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'invalid@email');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/please enter a valid email address/i)
      ).toBeInTheDocument();
    });

    expect(mockedApi.post).not.toHaveBeenCalled();
  });

  it('shows error for weak password', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'weak');
    await user.type(confirmInput, 'weak');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(
          /password must be ≥8 characters, with uppercase, lowercase & a digit/i
        )
      ).toBeInTheDocument();
    });

    expect(mockedApi.post).not.toHaveBeenCalled();
  });

  it('shows error when passwords do not match', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password456');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/passwords do not match/i)).toBeInTheDocument();
    });

    expect(mockedApi.post).not.toHaveBeenCalled();
  });

  it('successfully registers user and shows success message', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockResolvedValueOnce({ data: {} });

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockedApi.post).toHaveBeenCalledWith('/auth/register', {
        email: 'test@example.com',
        password: 'Password123',
      });
    });

    await waitFor(() => {
      expect(
        screen.getByText(/account created successfully/i)
      ).toBeInTheDocument();
    });

    // Should navigate to login after 2 seconds
    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith('/login');
      },
      { timeout: 3000 }
    );
  });

  it('shows error when email is already taken', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockRejectedValueOnce({
      response: { status: 409 },
    });

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'existing@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/that email is already taken/i)
      ).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('shows generic error for other registration failures', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockRejectedValueOnce({
      response: { status: 500 },
    });

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/registration failed. please try again/i)
      ).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('disables submit button when success message is shown', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockResolvedValueOnce({ data: {} });

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/account created successfully/i)
      ).toBeInTheDocument();
    });

    expect(submitButton).toBeDisabled();
  });

  it('has correct navigation link to login page', () => {
    renderRegisterPage();

    const loginLink = screen.getByRole('link', { name: /login/i });
    expect(loginLink).toHaveAttribute('href', '/login');
  });

  it('clears error message when form is resubmitted', async () => {
    const user = userEvent.setup();

    // First submission fails
    mockedApi.post.mockRejectedValueOnce({
      response: { status: 409 },
    });

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'existing@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/that email is already taken/i)
      ).toBeInTheDocument();
    });

    // Second submission succeeds
    mockedApi.post.mockResolvedValueOnce({ data: {} });

    await user.clear(emailInput);
    await user.type(emailInput, 'new@example.com');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.queryByText(/that email is already taken/i)
      ).not.toBeInTheDocument();
    });
  });

  it('validates password requirements correctly', async () => {
    const user = userEvent.setup();
    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    // Test various invalid passwords
    const invalidPasswords = [
      'short', // Too short
      'nouppercase123', // No uppercase
      'NOLOWERCASE123', // No lowercase
      'NoDigitsHere', // No digits
      'Valid1', // Too short but has all requirements
    ];

    for (const invalidPassword of invalidPasswords) {
      await user.clear(emailInput);
      await user.clear(passwordInput);
      await user.clear(confirmInput);

      await user.type(emailInput, 'test@example.com');
      await user.type(passwordInput, invalidPassword);
      await user.type(confirmInput, invalidPassword);
      await user.click(submitButton);

      await waitFor(() => {
        expect(
          screen.getByText(
            /password must be ≥8 characters, with uppercase, lowercase & a digit/i
          )
        ).toBeInTheDocument();
      });

      expect(mockedApi.post).not.toHaveBeenCalled();
    }
  });

  it('handles form submission via Enter key', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockResolvedValueOnce({ data: {} });

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.keyboard('{Enter}');

    await waitFor(() => {
      expect(mockedApi.post).toHaveBeenCalledWith('/auth/register', {
        email: 'test@example.com',
        password: 'Password123',
      });
    });
  });

  it('maintains form state during API call', async () => {
    const user = userEvent.setup();

    // Mock a delayed API response
    mockedApi.post.mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 100))
    );

    renderRegisterPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/^password$/i);
    const confirmInput = screen.getByPlaceholderText(/confirm password/i);
    const submitButton = screen.getByRole('button', {
      name: /create account/i,
    });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'Password123');
    await user.type(confirmInput, 'Password123');
    await user.click(submitButton);

    // Form values should be maintained during API call
    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('Password123');
    expect(confirmInput).toHaveValue('Password123');

    await waitFor(() => {
      expect(
        screen.getByText(/account created successfully/i)
      ).toBeInTheDocument();
    });
  });
});
