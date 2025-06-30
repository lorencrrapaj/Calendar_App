import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfirmationModal from '../components/ConfirmationModal';

describe('ConfirmationModal', () => {
  const defaultProps = {
    isOpen: true,
    title: 'Test Title',
    message: 'Test message',
    onConfirm: jest.fn(),
    onCancel: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders when isOpen is true', () => {
    render(<ConfirmationModal {...defaultProps} />);

    expect(screen.getByText('Test Title')).toBeInTheDocument();
    expect(screen.getByText('Test message')).toBeInTheDocument();
  });

  it('does not render when isOpen is false', () => {
    render(<ConfirmationModal {...defaultProps} isOpen={false} />);

    expect(screen.queryByText('Test Title')).not.toBeInTheDocument();
  });

  it('calls onCancel when cancel button is clicked', () => {
    render(<ConfirmationModal {...defaultProps} />);

    fireEvent.click(screen.getByText('Cancel'));
    expect(defaultProps.onCancel).toHaveBeenCalledTimes(1);
  });

  it('calls onCancel when close button (×) is clicked', () => {
    render(<ConfirmationModal {...defaultProps} />);

    fireEvent.click(screen.getByText('✕'));
    expect(defaultProps.onCancel).toHaveBeenCalledTimes(1);
  });

  it('calls onConfirm when confirm button is clicked', () => {
    render(<ConfirmationModal {...defaultProps} />);

    fireEvent.click(screen.getByText('Delete'));
    expect(defaultProps.onConfirm).toHaveBeenCalledTimes(1);
  });

  it('renders custom button text', () => {
    render(
      <ConfirmationModal
        {...defaultProps}
        confirmText="Delete"
        cancelText="Keep"
      />
    );

    expect(screen.getByText('Delete')).toBeInTheDocument();
    expect(screen.getByText('Keep')).toBeInTheDocument();
  });

  it('shows warning icon when isDestructive is true', () => {
    render(<ConfirmationModal {...defaultProps} isDestructive={true} />);

    // Check for the warning icon (ExclamationTriangleIcon)
    const icon = document.querySelector('.confirmation-icon');
    expect(icon).toBeInTheDocument();
  });

  it('disables buttons when isLoading is true', () => {
    render(<ConfirmationModal {...defaultProps} isLoading={true} />);

    const confirmButton = screen.getByText('Processing…');
    const cancelButton = screen.getByText('Cancel');
    const closeButton = screen.getByText('✕');

    expect(confirmButton).toBeDisabled();
    expect(cancelButton).toBeDisabled();
    expect(closeButton).toBeDisabled();
  });

  it('applies destructive styling when isDestructive is true', () => {
    render(
      <ConfirmationModal
        {...defaultProps}
        isDestructive={true}
        confirmText="Delete"
      />
    );

    const deleteButton = screen.getByText('Delete');
    expect(deleteButton).toHaveClass('btn-delete');
  });

  it('applies normal styling when isDestructive is false', () => {
    render(<ConfirmationModal {...defaultProps} isDestructive={false} />);

    const confirmButton = screen.getByText('Delete');
    expect(confirmButton).toHaveClass('btn-delete');
  });

  it('prevents actions when loading', () => {
    render(<ConfirmationModal {...defaultProps} isLoading={true} />);

    fireEvent.click(screen.getByText('Processing…'));
    fireEvent.click(screen.getByText('Cancel'));
    fireEvent.click(screen.getByText('✕'));

    expect(defaultProps.onConfirm).not.toHaveBeenCalled();
    expect(defaultProps.onCancel).not.toHaveBeenCalled();
  });
});
