import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import Toast from '../components/Toast';

// Mock timers for testing auto-dismiss functionality
jest.useFakeTimers();

describe('Toast', () => {
  const defaultProps = {
    message: 'Test message',
    type: 'success' as const,
    isVisible: true,
    onClose: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    jest.clearAllTimers();
  });

  afterEach(() => {
    jest.runOnlyPendingTimers();
    jest.useRealTimers();
    jest.useFakeTimers();
  });

  it('renders when isVisible is true', () => {
    render(<Toast {...defaultProps} />);

    expect(screen.getByText('Test message')).toBeInTheDocument();
  });

  it('does not render when isVisible is false', () => {
    render(<Toast {...defaultProps} isVisible={false} />);

    expect(screen.queryByText('Test message')).not.toBeInTheDocument();
  });

  it('renders success toast with correct styling', () => {
    render(<Toast {...defaultProps} type="success" />);

    const toast = screen.getByText('Test message').closest('.toast');
    expect(toast).toHaveClass('toast-success');
  });

  it('renders error toast with correct styling', () => {
    render(<Toast {...defaultProps} type="error" />);

    const toast = screen.getByText('Test message').closest('.toast');
    expect(toast).toHaveClass('toast-error');
  });

  it('shows success icon for success type', () => {
    render(<Toast {...defaultProps} type="success" />);

    // CheckCircleIcon should be present
    const icon = document.querySelector('.toast-icon');
    expect(icon).toBeInTheDocument();
  });

  it('shows error icon for error type', () => {
    render(<Toast {...defaultProps} type="error" />);

    // ExclamationTriangleIcon should be present
    const icon = document.querySelector('.toast-icon');
    expect(icon).toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', () => {
    render(<Toast {...defaultProps} />);

    fireEvent.click(screen.getByText('✕'));
    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('auto-dismisses after default duration (3000ms)', () => {
    render(<Toast {...defaultProps} />);

    expect(defaultProps.onClose).not.toHaveBeenCalled();

    // Fast-forward time by 3000ms
    jest.advanceTimersByTime(3000);

    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('auto-dismisses after custom duration', () => {
    render(<Toast {...defaultProps} duration={5000} />);

    expect(defaultProps.onClose).not.toHaveBeenCalled();

    // Fast-forward time by 5000ms
    jest.advanceTimersByTime(5000);

    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('does not auto-dismiss when isVisible is false', () => {
    render(<Toast {...defaultProps} isVisible={false} />);

    jest.advanceTimersByTime(3000);

    expect(defaultProps.onClose).not.toHaveBeenCalled();
  });

  it('clears timer when component unmounts', () => {
    const { unmount } = render(<Toast {...defaultProps} />);

    unmount();
    jest.advanceTimersByTime(3000);

    expect(defaultProps.onClose).not.toHaveBeenCalled();
  });

  it('resets timer when isVisible changes from false to true', () => {
    const { rerender } = render(<Toast {...defaultProps} isVisible={false} />);

    // Change to visible
    rerender(<Toast {...defaultProps} isVisible={true} />);

    jest.advanceTimersByTime(3000);

    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('displays the correct message text', () => {
    const customMessage = 'Custom toast message';
    render(<Toast {...defaultProps} message={customMessage} />);

    expect(screen.getByText(customMessage)).toBeInTheDocument();
  });

  it('has correct CSS classes for styling', () => {
    render(<Toast {...defaultProps} type="success" />);

    const toast = screen.getByText('Test message').closest('.toast');
    expect(toast).toHaveClass('toast', 'toast-success');

    const content = toast?.querySelector('.toast-content');
    expect(content).toBeInTheDocument();

    const message = toast?.querySelector('.toast-message');
    expect(message).toBeInTheDocument();

    const closeButton = toast?.querySelector('.toast-close');
    expect(closeButton).toBeInTheDocument();
  });
});
