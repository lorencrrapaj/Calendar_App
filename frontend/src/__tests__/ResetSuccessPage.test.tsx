import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import ResetSuccessPage from '../pages/ResetSuccessPage';

// Wrapper component for router
const ResetSuccessPageWrapper = () => (
  <BrowserRouter>
    <ResetSuccessPage />
  </BrowserRouter>
);

describe('ResetSuccessPage', () => {
  it('renders the success message', () => {
    render(<ResetSuccessPageWrapper />);

    expect(
      screen.getByRole('heading', { name: 'Password Updated' })
    ).toBeInTheDocument();
    expect(
      screen.getByText('Your password has been changed successfully.')
    ).toBeInTheDocument();
  });

  it('renders the login button', () => {
    render(<ResetSuccessPageWrapper />);

    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument();
  });

  it('login button links to login page', () => {
    render(<ResetSuccessPageWrapper />);

    const loginLink = screen.getByRole('link');
    expect(loginLink).toHaveAttribute('href', '/login');
  });

  it('renders the check circle icon', () => {
    const { container } = render(<ResetSuccessPageWrapper />);

    // Check for the check circle icon (SVG element)
    const svgElement = container.querySelector('svg');
    expect(svgElement).toBeInTheDocument();
  });

  it('has correct page structure', () => {
    const { container } = render(<ResetSuccessPageWrapper />);

    const loginContainer = container.querySelector('.login-container');
    expect(loginContainer).toBeInTheDocument();

    const loginCard = container.querySelector('.login-card');
    expect(loginCard).toBeInTheDocument();
  });

  it('success message has correct styling', () => {
    render(<ResetSuccessPageWrapper />);

    const successMessage = screen.getByText(
      'Your password has been changed successfully.'
    );
    expect(successMessage).toHaveStyle({ color: '#fff', textAlign: 'center' });
  });

  it('login button has correct styling', () => {
    render(<ResetSuccessPageWrapper />);

    const loginButton = screen.getByRole('button', { name: 'Login' });
    expect(loginButton).toHaveStyle({ width: '100%', marginTop: '1rem' });
  });

  it('check icon has correct styling', () => {
    const { container } = render(<ResetSuccessPageWrapper />);

    const svgElement = container.querySelector('svg');
    expect(svgElement).toHaveClass('block', 'mx-auto', 'my-4');
    expect(svgElement).toHaveStyle({
      width: '48px',
      height: '48px',
      color: '#4BB543',
    });
  });
});
