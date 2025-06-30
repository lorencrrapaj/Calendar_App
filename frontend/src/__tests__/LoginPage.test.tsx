import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import LoginPage from '../pages/LoginPage';
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

// Mock localStorage
const mockLocalStorage = {
  setItem: jest.fn(),
  getItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

// Store original localStorage
const originalLocalStorage = window.localStorage;

// Mock localStorage before tests
Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
  writable: true,
  configurable: true,
});

const renderLoginPage = () => {
  return render(
    <BrowserRouter>
      <LoginPage />
    </BrowserRouter>
  );
};

describe('LoginPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLocalStorage.setItem.mockClear();
    mockLocalStorage.getItem.mockClear();
    mockLocalStorage.removeItem.mockClear();
    mockNavigate.mockClear();
  });

  afterAll(() => {
    // Restore original localStorage
    Object.defineProperty(window, 'localStorage', {
      value: originalLocalStorage,
      writable: true,
    });
  });

  it('renders login form with all elements', () => {
    renderLoginPage();

    expect(screen.getByRole('heading', { name: /login/i })).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
    expect(screen.getByText(/remember me/i)).toBeInTheDocument();
    expect(
      screen.getByRole('link', { name: /forgot password/i })
    ).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /register/i })).toBeInTheDocument();
  });

  it('allows user to type in email and password fields', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');
  });

  it('toggles password visibility when eye icon is clicked', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    const passwordInput = screen.getByPlaceholderText(/password/i);
    await user.type(passwordInput, 'password123');

    // Initially password should be hidden
    expect(passwordInput).toHaveAttribute('type', 'password');

    // Find the password input wrapper and then the eye icon within it
    const passwordWrapper = passwordInput.closest('.input-wrapper');
    const eyeIcon = passwordWrapper?.querySelector('svg.fa-icon');

    if (eyeIcon) {
      await user.click(eyeIcon);
      expect(passwordInput).toHaveAttribute('type', 'text');

      // Click again to hide password
      const eyeSlashIcon = passwordWrapper?.querySelector('svg.fa-icon');
      if (eyeSlashIcon) {
        await user.click(eyeSlashIcon);
        expect(passwordInput).toHaveAttribute('type', 'password');
      }
    }
  });

  it('submits form with valid credentials and navigates to calendar', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockResolvedValueOnce({
      data: { token: 'fake-jwt-token' },
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');

    await act(async () => {
      await user.click(submitButton);
    });

    await waitFor(() => {
      expect(mockedApi.post).toHaveBeenCalledWith('/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      });
    });

    await waitFor(() => {
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'token',
        'fake-jwt-token'
      );
    });

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/calendar');
    });
  });

  it('displays error message when login fails', async () => {
    const user = userEvent.setup();
    const errorMessage = 'Invalid credentials';
    mockedApi.post.mockRejectedValueOnce({
      response: { data: errorMessage },
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });

    expect(mockLocalStorage.setItem).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('displays generic error message when API error has no response data', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockRejectedValueOnce({
      response: null,
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Login failed.')).toBeInTheDocument();
    });
  });

  it('clears error message when form is resubmitted', async () => {
    const user = userEvent.setup();

    // First submission fails
    mockedApi.post.mockRejectedValueOnce({
      response: { data: 'Invalid credentials' },
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'wrongpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });

    // Second submission succeeds
    mockedApi.post.mockResolvedValueOnce({
      data: { token: 'fake-jwt-token' },
    });

    await user.clear(passwordInput);
    await user.type(passwordInput, 'correctpassword');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.queryByText('Invalid credentials')).not.toBeInTheDocument();
    });
  });

  it('prevents form submission with empty fields due to HTML5 validation', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    const submitButton = screen.getByRole('button', { name: /login/i });
    await user.click(submitButton);

    // API should not be called with empty fields due to HTML5 required attribute
    expect(mockedApi.post).not.toHaveBeenCalled();
  });

  it('has correct navigation links', () => {
    renderLoginPage();

    const forgotPasswordLink = screen.getByRole('link', {
      name: /forgot password/i,
    });
    const registerLink = screen.getByRole('link', { name: /register/i });

    expect(forgotPasswordLink).toHaveAttribute('href', '/forgot-password');
    expect(registerLink).toHaveAttribute('href', '/register');
  });

  it('has remember me checkbox', () => {
    renderLoginPage();

    const rememberMeCheckbox = screen.getByRole('checkbox');
    expect(rememberMeCheckbox).toBeInTheDocument();
    expect(rememberMeCheckbox).not.toBeChecked();
  });

  it('allows checking and unchecking remember me checkbox', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    const rememberMeCheckbox = screen.getByRole('checkbox');

    await user.click(rememberMeCheckbox);
    expect(rememberMeCheckbox).toBeChecked();

    await user.click(rememberMeCheckbox);
    expect(rememberMeCheckbox).not.toBeChecked();
  });

  it('handles form submission via Enter key', async () => {
    const user = userEvent.setup();
    mockedApi.post.mockResolvedValueOnce({
      data: { token: 'fake-jwt-token' },
    });

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.keyboard('{Enter}');

    await waitFor(() => {
      expect(mockedApi.post).toHaveBeenCalledWith('/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      });
    });
  });

  it('maintains form state during API call', async () => {
    const user = userEvent.setup();

    // Mock a delayed API response
    mockedApi.post.mockImplementation(
      () =>
        new Promise(resolve =>
          setTimeout(() => resolve({ data: { token: 'fake-jwt-token' } }), 100)
        )
    );

    renderLoginPage();

    const emailInput = screen.getByPlaceholderText(/email/i);
    const passwordInput = screen.getByPlaceholderText(/password/i);
    const submitButton = screen.getByRole('button', { name: /login/i });

    await user.type(emailInput, 'test@example.com');
    await user.type(passwordInput, 'password123');
    await user.click(submitButton);

    // Form values should be maintained during API call
    expect(emailInput).toHaveValue('test@example.com');
    expect(passwordInput).toHaveValue('password123');

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/calendar');
    });
  });
});
