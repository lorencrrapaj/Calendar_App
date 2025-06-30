import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { BrowserRouter } from 'react-router-dom';
import AccountSettingsPage from '../pages/AccountSettingsPage';
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

const renderAccountSettingsPage = () => {
  return render(
    <BrowserRouter>
      <AccountSettingsPage />
    </BrowserRouter>
  );
};

describe('AccountSettingsPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockNavigate.mockClear();
  });

  it('renders account settings form with all elements', async () => {
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(
        screen.getByRole('heading', { name: /account settings/i })
      ).toBeInTheDocument();
    });

    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText(/current password/i)
    ).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/^new password$/i)).toBeInTheDocument();
    expect(
      screen.getByPlaceholderText(/confirm new password/i)
    ).toBeInTheDocument();
    expect(
      screen.getByRole('button', { name: /update password/i })
    ).toBeInTheDocument();
    expect(screen.getByText(/change password/i)).toBeInTheDocument();
  });

  it('fetches and displays user email on component mount', async () => {
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'user@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(mockedApi.get).toHaveBeenCalledWith('/auth/me');
    });

    await waitFor(() => {
      expect(screen.getByDisplayValue('user@example.com')).toBeInTheDocument();
    });
  });

  it('redirects to login when fetching user info fails with 401', async () => {
    mockedApi.get.mockRejectedValueOnce({
      response: { status: 401 },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });

  it('has read-only email field', async () => {
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      const emailInput = screen.getByPlaceholderText(/email/i);
      expect(emailInput).toHaveAttribute('readonly');
    });
  });

  it('has back button that navigates to calendar', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const backButton = screen.getByRole('button', { name: '' }); // Back button with arrow icon
    await user.click(backButton);

    expect(mockNavigate).toHaveBeenCalledWith('/calendar');
  });

  it('allows user to type in password fields', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');

    expect(currentPasswordInput).toHaveValue('OldPassword123');
    expect(newPasswordInput).toHaveValue('NewPassword456');
    expect(confirmPasswordInput).toHaveValue('NewPassword456');
  });

  it('toggles password visibility for current password field', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    await user.type(currentPasswordInput, 'password123');

    // Initially password should be hidden
    expect(currentPasswordInput).toHaveAttribute('type', 'password');

    // Find and click the eye icon for current password field
    const currentPasswordWrapper =
      currentPasswordInput.closest('.input-wrapper');
    const currentPasswordEyeIcon =
      currentPasswordWrapper?.querySelector('svg.fa-icon');

    if (currentPasswordEyeIcon) {
      await user.click(currentPasswordEyeIcon);
      expect(currentPasswordInput).toHaveAttribute('type', 'text');

      // Click again to hide password
      const eyeSlashIcon = currentPasswordWrapper?.querySelector('svg.fa-icon');
      if (eyeSlashIcon) {
        await user.click(eyeSlashIcon);
      }
    }
    expect(currentPasswordInput).toHaveAttribute('type', 'password');
  });

  it('toggles password visibility for new password field', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    await user.type(newPasswordInput, 'password123');

    // Initially password should be hidden
    expect(newPasswordInput).toHaveAttribute('type', 'password');

    // Find and click the eye icon for new password field
    const newPasswordWrapper = newPasswordInput.closest('.input-wrapper');
    const newPasswordEyeIcon = newPasswordWrapper?.querySelector('svg.fa-icon');

    if (newPasswordEyeIcon) {
      await user.click(newPasswordEyeIcon);
      expect(newPasswordInput).toHaveAttribute('type', 'text');

      // Click again to hide password
      const eyeSlashIcon = newPasswordWrapper?.querySelector('svg.fa-icon');
      if (eyeSlashIcon) {
        await user.click(eyeSlashIcon);
      }
    }
    expect(newPasswordInput).toHaveAttribute('type', 'password');
  });

  it('toggles password visibility for confirm password field', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    await user.type(confirmPasswordInput, 'password123');

    // Initially password should be hidden
    expect(confirmPasswordInput).toHaveAttribute('type', 'password');

    // Find and click the eye icon for confirm password field
    const confirmPasswordWrapper =
      confirmPasswordInput.closest('.input-wrapper');
    const confirmPasswordEyeIcon =
      confirmPasswordWrapper?.querySelector('svg.fa-icon');

    if (confirmPasswordEyeIcon) {
      await user.click(confirmPasswordEyeIcon);
      expect(confirmPasswordInput).toHaveAttribute('type', 'text');

      // Click again to hide password
      const eyeSlashIcon = confirmPasswordWrapper?.querySelector('svg.fa-icon');
      if (eyeSlashIcon) {
        await user.click(eyeSlashIcon);
      }
    }
    expect(confirmPasswordInput).toHaveAttribute('type', 'password');
  });

  it('shows error when new passwords do not match', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'DifferentPassword789');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/new passwords do not match/i)
      ).toBeInTheDocument();
    });

    expect(mockedApi.put).not.toHaveBeenCalled();
  });

  it('shows error for weak new password', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'weak');
    await user.type(confirmPasswordInput, 'weak');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(
          /new password must be ≥8 characters, with uppercase, lowercase & a digit/i
        )
      ).toBeInTheDocument();
    });

    expect(mockedApi.put).not.toHaveBeenCalled();
  });

  it('successfully updates password and shows success message', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });
    mockedApi.put.mockResolvedValueOnce({ data: {} });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.click(submitButton);

    await waitFor(() => {
      expect(mockedApi.put).toHaveBeenCalledWith('/auth/password', {
        oldPassword: 'OldPassword123',
        newPassword: 'NewPassword456',
      });
    });

    await waitFor(() => {
      expect(screen.getByText(/password updated/i)).toBeInTheDocument();
    });

    // Should navigate to login after 2 seconds
    await waitFor(
      () => {
        expect(mockNavigate).toHaveBeenCalledWith('/login');
      },
      { timeout: 3000 }
    );
  });

  it('shows error when current password is incorrect', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });
    mockedApi.put.mockRejectedValueOnce({
      response: { status: 401 },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'WrongPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/current password is incorrect/i)
      ).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalledWith('/login');
  });

  it('shows generic error for other API failures', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });
    mockedApi.put.mockRejectedValueOnce({
      response: { status: 500 },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/failed to update password. try again/i)
      ).toBeInTheDocument();
    });

    expect(mockNavigate).not.toHaveBeenCalledWith('/login');
  });

  it('disables submit button when success message is shown', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });
    mockedApi.put.mockResolvedValueOnce({ data: {} });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText(/password updated/i)).toBeInTheDocument();
    });

    expect(submitButton).toBeDisabled();
  });

  it('clears error message when form is resubmitted', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    // First submission with mismatched passwords
    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'DifferentPassword789');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.getByText(/new passwords do not match/i)
      ).toBeInTheDocument();
    });

    // Second submission with matching passwords
    mockedApi.put.mockResolvedValueOnce({ data: {} });

    await user.clear(confirmPasswordInput);
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.click(submitButton);

    await waitFor(() => {
      expect(
        screen.queryByText(/new passwords do not match/i)
      ).not.toBeInTheDocument();
    });
  });

  it('handles form submission via Enter key', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });
    mockedApi.put.mockResolvedValueOnce({ data: {} });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.keyboard('{Enter}');

    await waitFor(() => {
      expect(mockedApi.put).toHaveBeenCalledWith('/auth/password', {
        oldPassword: 'OldPassword123',
        newPassword: 'NewPassword456',
      });
    });
  });

  it('validates password requirements correctly', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
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
      await user.clear(currentPasswordInput);
      await user.clear(newPasswordInput);
      await user.clear(confirmPasswordInput);

      await user.type(currentPasswordInput, 'OldPassword123');
      await user.type(newPasswordInput, invalidPassword);
      await user.type(confirmPasswordInput, invalidPassword);
      await user.click(submitButton);

      await waitFor(() => {
        expect(
          screen.getByText(
            /new password must be ≥8 characters, with uppercase, lowercase & a digit/i
          )
        ).toBeInTheDocument();
      });

      expect(mockedApi.put).not.toHaveBeenCalled();
    }
  });

  it('maintains form state during API call', async () => {
    const user = userEvent.setup();
    mockedApi.get.mockResolvedValueOnce({
      data: { email: 'test@example.com' },
    });

    // Mock a delayed API response
    mockedApi.put.mockImplementation(
      () => new Promise(resolve => setTimeout(() => resolve({ data: {} }), 100))
    );

    renderAccountSettingsPage();

    await waitFor(() => {
      expect(screen.getByDisplayValue('test@example.com')).toBeInTheDocument();
    });

    const currentPasswordInput =
      screen.getByPlaceholderText(/current password/i);
    const newPasswordInput = screen.getByPlaceholderText(/^new password$/i);
    const confirmPasswordInput =
      screen.getByPlaceholderText(/confirm new password/i);
    const submitButton = screen.getByRole('button', {
      name: /update password/i,
    });

    await user.type(currentPasswordInput, 'OldPassword123');
    await user.type(newPasswordInput, 'NewPassword456');
    await user.type(confirmPasswordInput, 'NewPassword456');
    await user.click(submitButton);

    // Form values should be maintained during API call
    expect(currentPasswordInput).toHaveValue('OldPassword123');
    expect(newPasswordInput).toHaveValue('NewPassword456');
    expect(confirmPasswordInput).toHaveValue('NewPassword456');

    await waitFor(() => {
      expect(screen.getByText(/password updated/i)).toBeInTheDocument();
    });
  });
});
