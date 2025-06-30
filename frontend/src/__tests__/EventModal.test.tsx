import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import EventModal from '../components/EventModal';
import * as eventsService from '../services/events';

// Mock the events service
jest.mock('../services/events');
const mockCreateEvent = eventsService.createEvent as jest.MockedFunction<
  typeof eventsService.createEvent
>;

// Mock data for tags
const mockAvailableTags = [
  { id: 1, name: 'Work' },
  { id: 2, name: 'Personal' },
  { id: 3, name: 'Meeting' },
];

describe('EventModal', () => {
  const mockOnClose = jest.fn();
  const mockOnEventCreated = jest.fn();

  const defaultProps = {
    isOpen: true,
    onClose: mockOnClose,
    onEventCreated: mockOnEventCreated,
    availableTags: mockAvailableTags,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    it('renders title input field', () => {
      render(<EventModal {...defaultProps} />);

      expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/title/i)).toHaveAttribute('type', 'text');
    });

    it('renders description textarea field', () => {
      render(<EventModal {...defaultProps} />);

      expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/description/i).tagName).toBe('TEXTAREA');
    });

    it('renders start date/time input field', () => {
      render(<EventModal {...defaultProps} />);

      expect(screen.getByLabelText(/start date\/time/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/start date\/time/i)).toHaveAttribute(
        'type',
        'datetime-local'
      );
    });

    it('renders end date/time input field', () => {
      render(<EventModal {...defaultProps} />);

      expect(screen.getByLabelText(/end date\/time/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/end date\/time/i)).toHaveAttribute(
        'type',
        'datetime-local'
      );
    });

    it('renders cancel and create buttons', () => {
      render(<EventModal {...defaultProps} />);

      expect(
        screen.getByRole('button', { name: /cancel/i })
      ).toBeInTheDocument();
      expect(
        screen.getByRole('button', { name: /create event/i })
      ).toBeInTheDocument();
    });

    it('renders modal title', () => {
      render(<EventModal {...defaultProps} />);

      expect(
        screen.getByRole('heading', { name: 'Create Event' })
      ).toBeInTheDocument();
    });

    it('renders close button (X)', () => {
      render(<EventModal {...defaultProps} />);

      expect(screen.getByText('✕')).toBeInTheDocument();
    });

    it('does not render when isOpen is false', () => {
      render(<EventModal {...defaultProps} isOpen={false} />);

      expect(screen.queryByText('Create Event')).not.toBeInTheDocument();
    });
  });

  describe('Validation Errors', () => {
    it('shows error for empty title field', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(screen.getByText('Title is required')).toBeInTheDocument();
    });

    it('shows error for empty description field', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(screen.getByText('Description is required')).toBeInTheDocument();
    });

    it('shows error for empty start date/time field', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(
        screen.getByText('Start date and time is required')
      ).toBeInTheDocument();
    });

    it('shows error for empty end date/time field', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(
        screen.getByText('End date and time is required')
      ).toBeInTheDocument();
    });

    it('shows error when end date is before start date', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T12:00');
      await user.type(endInput, '2024-01-15T10:00'); // End before start
      await user.click(submitButton);

      expect(screen.getByText('End must be after start')).toBeInTheDocument();
    });

    it('shows error when end date equals start date', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T12:00');
      await user.type(endInput, '2024-01-15T12:00'); // End equals start
      await user.click(submitButton);

      expect(screen.getByText('End must be after start')).toBeInTheDocument();
    });

    it('clears validation errors when user starts typing', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      // Trigger validation error
      await user.click(submitButton);
      expect(screen.getByText('Title is required')).toBeInTheDocument();

      // Start typing to clear error
      await user.type(titleInput, 'T');
      expect(screen.queryByText('Title is required')).not.toBeInTheDocument();
    });
  });

  describe('Successful Submit', () => {
    it('calls createEvent with correct data and triggers onEventCreated', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
      });

      // Wait for success banner to appear
      await waitFor(() => {
        expect(screen.getByText('Event created!')).toBeInTheDocument();
      });

      // Wait for modal to close and onEventCreated to be called
      await waitFor(
        () => {
          expect(mockOnEventCreated).toHaveBeenCalled();
        },
        { timeout: 2000 }
      );
    });

    it('shows success banner after successful creation', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Event created!')).toBeInTheDocument();
      });
    });

    it('disables form during submission', async () => {
      const user = userEvent.setup();
      // Mock a delayed response
      mockCreateEvent.mockImplementation(
        () => new Promise(resolve => setTimeout(resolve, 100))
      );

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      const cancelButton = screen.getByRole('button', { name: /cancel/i });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      // Check that form elements are disabled during submission
      expect(titleInput).toBeDisabled();
      expect(descriptionInput).toBeDisabled();
      expect(startInput).toBeDisabled();
      expect(endInput).toBeDisabled();
      expect(submitButton).toBeDisabled();
      expect(cancelButton).toBeDisabled();
      expect(screen.getByText('Creating…')).toBeInTheDocument();
    });
  });

  describe('API Error Handling', () => {
    it('shows error banner when createEvent throws an error', async () => {
      const user = userEvent.setup();
      const errorMessage =
        'End date and time must be after start date and time';
      mockCreateEvent.mockRejectedValueOnce({
        response: { data: errorMessage },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(errorMessage)).toBeInTheDocument();
      });

      // Verify error banner is displayed
      const errorBanner = screen
        .getByText(errorMessage)
        .closest('.modal-error-banner');
      expect(errorBanner).toBeInTheDocument();
    });

    it('shows generic error message when API error has no response data', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockRejectedValueOnce(new Error('Network error'));

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Error creating event')).toBeInTheDocument();
      });
    });

    it('re-enables form after API error', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockRejectedValueOnce(new Error('API Error'));

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Error creating event')).toBeInTheDocument();
      });

      // Form should be re-enabled after error
      expect(titleInput).not.toBeDisabled();
      expect(descriptionInput).not.toBeDisabled();
      expect(startInput).not.toBeDisabled();
      expect(endInput).not.toBeDisabled();
      expect(submitButton).not.toBeDisabled();
      expect(
        screen.getByRole('button', { name: 'Create Event' })
      ).toBeInTheDocument(); // Button text back to normal
    });
  });

  describe('Modal Interaction', () => {
    it('calls onClose when cancel button is clicked', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('calls onClose when close (X) button is clicked', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const closeButton = screen.getByText('✕');
      await user.click(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('resets form data after successful submission', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.click(submitButton);

      // Wait for the form to be reset (after the timeout in the component)
      await waitFor(
        () => {
          expect(mockOnEventCreated).toHaveBeenCalled();
        },
        { timeout: 2000 }
      );
    });
  });

  describe('Tag Functionality', () => {
    it('renders tag dropdown component', () => {
      render(<EventModal {...defaultProps} />);

      expect(screen.getByText('Select tags…')).toBeInTheDocument();
      expect(screen.getByText('Tags')).toBeInTheDocument();
    });

    it('shows placeholder text when no tags selected', () => {
      render(<EventModal {...defaultProps} />);

      const dropdownButton = screen.getByText('Select tags…');
      expect(dropdownButton).toHaveTextContent('Select tags…');
    });

    it('allows tag selection through dropdown', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      // Click dropdown button to open menu
      const dropdownButton = screen.getByText('Select tags…');
      await user.click(dropdownButton);

      // Select first two tags
      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      const personalCheckbox = screen.getByRole('checkbox', {
        name: /personal/i,
      });

      await user.click(workCheckbox);
      await user.click(personalCheckbox);

      // Check that button text shows selected tags
      expect(dropdownButton).toHaveTextContent('Work, Personal');
    });

    it('includes selected tags when creating event', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      });

      render(<EventModal {...defaultProps} />);

      // Fill form
      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');

      // Select tags through dropdown
      const dropdownButton = screen.getByText('Select tags…');
      await user.click(dropdownButton);

      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      const personalCheckbox = screen.getByRole('checkbox', {
        name: /personal/i,
      });

      await user.click(workCheckbox);
      await user.click(personalCheckbox);

      // Submit
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        tagIds: [1, 2],
      });
    });

    it('does not include tagIds when no tags selected', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      });

      render(<EventModal {...defaultProps} />);

      // Fill form without selecting tags
      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');

      // Submit
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
      });
    });

    it('preserves selected tags when editing event', () => {
      const editEvent = {
        id: 1,
        title: 'Edit Event',
        description: 'Edit Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        tags: [
          { id: 1, name: 'Work' },
          { id: 3, name: 'Meeting' },
        ],
      };

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      // The dropdown button should show the selected tag names
      const dropdownButton = screen.getByText('Work, Meeting');
      expect(dropdownButton).toHaveTextContent('Work, Meeting');
    });

    it('clears selected tags when modal is closed and reopened', () => {
      const { rerender } = render(
        <EventModal {...defaultProps} isOpen={false} />
      );

      // Reopen modal
      rerender(<EventModal {...defaultProps} isOpen={true} />);

      const dropdownButton = screen.getByText('Select tags…');
      expect(dropdownButton).toHaveTextContent('Select tags…');
    });
  });

  describe('Recurrence Functionality', () => {
    it('shows repeat end options when repeat type is not none', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const repeatSelect = screen.getByLabelText(/repeat/i);
      await user.selectOptions(repeatSelect, 'daily');

      expect(screen.getByLabelText(/end repeat/i)).toBeInTheDocument();
    });

    it('hides repeat end options when repeat type is none', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const repeatSelect = screen.getByLabelText(/repeat/i);
      await user.selectOptions(repeatSelect, 'none');

      expect(screen.queryByLabelText(/end repeat/i)).not.toBeInTheDocument();
    });

    it('shows end date field when repeat end type is date', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const repeatSelect = screen.getByLabelText(/repeat/i);
      await user.selectOptions(repeatSelect, 'weekly');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'date');

      expect(screen.getByLabelText(/end date \*/i)).toBeInTheDocument();
    });

    it('shows repeat count field when repeat end type is count', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const repeatSelect = screen.getByLabelText(/repeat/i);
      await user.selectOptions(repeatSelect, 'monthly');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'count');

      expect(screen.getByLabelText(/number of times/i)).toBeInTheDocument();
    });

    it('validates repeat end date is required when repeat end type is date', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'daily');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'date');

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(
        screen.getByText('End date is required when repeat end type is date')
      ).toBeInTheDocument();
    });

    it('validates repeat end date is after start date', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'weekly');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'date');

      const repeatEndDateInput = screen.getByLabelText(/end date \*/i);
      await user.type(repeatEndDateInput, '2024-01-14'); // Before start date

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(
        screen.getByText('Repeat end date must be after start date')
      ).toBeInTheDocument();
    });

    it('validates repeat count is positive when repeat end type is count', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'monthly');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'count');

      const repeatCountInput = screen.getByLabelText(/number of times/i);
      fireEvent.change(repeatCountInput, { target: { value: '-1' } });

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      await waitFor(() => {
        expect(
          screen.getByText('Repeat count must be a positive number')
        ).toBeInTheDocument();
      });
    });

    it('creates event with daily recurrence rule', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'daily');

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=DAILY',
        recurrenceEndDate: undefined,
        recurrenceCount: undefined,
      });
    });

    it('creates event with weekly recurrence rule', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'weekly');

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=WEEKLY',
        recurrenceEndDate: undefined,
        recurrenceCount: undefined,
      });
    });

    it('creates event with monthly recurrence rule', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'monthly');

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=MONTHLY',
        recurrenceEndDate: undefined,
        recurrenceCount: undefined,
      });
    });

    it('creates event with recurrence end date', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'daily');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'date');

      const repeatEndDateInput = screen.getByLabelText(/end date \*/i);
      await user.type(repeatEndDateInput, '2024-01-30');

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=DAILY',
        recurrenceEndDate: '2024-01-30T23:59:59',
        recurrenceCount: undefined,
      });
    });

    it('creates event with recurrence count', async () => {
      const user = userEvent.setup();
      mockCreateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00:00',
        endDateTime: '2024-01-15T12:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} />);

      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const repeatSelect = screen.getByLabelText(/repeat/i);

      await user.type(titleInput, 'Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2024-01-15T10:00');
      await user.type(endInput, '2024-01-15T12:00');
      await user.selectOptions(repeatSelect, 'weekly');

      const repeatEndSelect = screen.getByLabelText(/end repeat/i);
      await user.selectOptions(repeatEndSelect, 'count');

      const repeatCountInput = screen.getByLabelText(/number of times/i);
      await user.clear(repeatCountInput);
      await user.type(repeatCountInput, '5');

      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });
      await user.click(submitButton);

      expect(mockCreateEvent).toHaveBeenCalledWith({
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=WEEKLY',
        recurrenceEndDate: undefined,
        recurrenceCount: 5,
      });
    });
  });

  describe('Edit Mode', () => {
    const mockUpdateEvent = eventsService.updateEvent as jest.MockedFunction<
      typeof eventsService.updateEvent
    >;

    beforeEach(() => {
      jest.clearAllMocks();
    });

    it('renders edit mode title and button text', () => {
      const editEvent = {
        id: 1,
        title: 'Edit Event',
        description: 'Edit Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
      };

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      expect(
        screen.getByRole('heading', { name: 'Edit Event' })
      ).toBeInTheDocument();
      expect(
        screen.getByRole('button', { name: /update event/i })
      ).toBeInTheDocument();
    });

    it('populates form with edit event data', () => {
      const editEvent = {
        id: 1,
        title: 'Edit Event',
        description: 'Edit Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
      };

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      expect(screen.getByDisplayValue('Edit Event')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Edit Description')).toBeInTheDocument();
      expect(screen.getByDisplayValue('2024-01-15T10:00')).toBeInTheDocument();
      expect(screen.getByDisplayValue('2024-01-15T12:00')).toBeInTheDocument();
    });

    it('populates form with recurrence data when editing recurring event', async () => {
      const editEvent = {
        id: 1,
        title: 'Recurring Event',
        description: 'Recurring Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=DAILY',
        recurrenceEndDate: '2024-01-30T23:59:59',
      };

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      await waitFor(() => {
        const repeatTypeSelect = screen.getByLabelText('Repeat');
        expect(repeatTypeSelect).toHaveValue('daily');
      });

      await waitFor(() => {
        const repeatEndTypeSelect = screen.getByLabelText('End Repeat');
        expect(repeatEndTypeSelect).toHaveValue('date');
      });

      await waitFor(() => {
        const repeatEndDateInput = screen.getByLabelText('End Date *');
        expect(repeatEndDateInput).toHaveValue('2024-01-30');
      });
    });

    it('populates form with recurrence count when editing recurring event with count', async () => {
      const editEvent = {
        id: 1,
        title: 'Recurring Event',
        description: 'Recurring Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        recurrenceRule: 'FREQ=WEEKLY',
        recurrenceCount: 5,
      };

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      await waitFor(() => {
        const repeatTypeSelect = screen.getByLabelText('Repeat');
        expect(repeatTypeSelect).toHaveValue('weekly');
      });

      await waitFor(() => {
        const repeatEndTypeSelect = screen.getByLabelText('End Repeat');
        expect(repeatEndTypeSelect).toHaveValue('count');
      });

      await waitFor(() => {
        const repeatCountInput = screen.getByLabelText('Number of Times *');
        expect(repeatCountInput).toHaveValue('5');
      });
    });

    it('calls updateEvent with correct data and scope', async () => {
      const user = userEvent.setup();
      const editEvent = {
        id: 1,
        title: 'Edit Event',
        description: 'Edit Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
        editScope: 'instance' as const,
      };

      mockUpdateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Updated Event',
        description: 'Updated Description',
        startDateTime: '2024-01-15T11:00:00',
        endDateTime: '2024-01-15T13:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      const titleInput = screen.getByDisplayValue('Edit Event');
      await user.clear(titleInput);
      await user.type(titleInput, 'Updated Event');

      const submitButton = screen.getByRole('button', {
        name: /update event/i,
      });
      await user.click(submitButton);

      expect(mockUpdateEvent).toHaveBeenCalledWith(
        1,
        {
          title: 'Updated Event',
          description: 'Edit Description',
          startDateTime: '2024-01-15T10:00',
          endDateTime: '2024-01-15T12:00',
          recurrenceRule: undefined,
          recurrenceEndDate: undefined,
          recurrenceCount: undefined,
        },
        'instance'
      );
    });

    it('shows success banner after successful update', async () => {
      const user = userEvent.setup();
      const editEvent = {
        id: 1,
        title: 'Edit Event',
        description: 'Edit Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
      };

      mockUpdateEvent.mockResolvedValueOnce({
        id: 1,
        title: 'Updated Event',
        description: 'Updated Description',
        startDateTime: '2024-01-15T11:00:00',
        endDateTime: '2024-01-15T13:00:00',
        user: { id: 1, email: 'test@example.com' },
      });

      render(<EventModal {...defaultProps} editEvent={editEvent} />);

      const submitButton = screen.getByRole('button', {
        name: /update event/i,
      });
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText('Event updated!')).toBeInTheDocument();
      });
    });
  });

  describe('Tag Selection', () => {
    it('toggles tag dropdown when clicked', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const dropdownButton = screen.getByText('Select tags…');
      await user.click(dropdownButton);

      expect(
        screen.getByRole('checkbox', { name: /work/i })
      ).toBeInTheDocument();
      expect(
        screen.getByRole('checkbox', { name: /personal/i })
      ).toBeInTheDocument();
      expect(
        screen.getByRole('checkbox', { name: /meeting/i })
      ).toBeInTheDocument();
    });

    it('updates selected tags display when tags are selected', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const dropdownButton = screen.getByText('Select tags…');
      await user.click(dropdownButton);

      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      await user.click(workCheckbox);

      // Check that the dropdown button now shows the selected tag
      const dropdownButtonAfter =
        screen.getByTestId('tag-dropdown') || screen.getByText(/work/i);
      expect(dropdownButtonAfter).toHaveTextContent('Work');
    });

    it('removes tag when unchecked', async () => {
      const user = userEvent.setup();
      render(<EventModal {...defaultProps} />);

      const dropdownButton = screen.getByText('Select tags…');
      await user.click(dropdownButton);

      const workCheckbox = screen.getByRole('checkbox', { name: /work/i });
      await user.click(workCheckbox); // Select
      await user.click(workCheckbox); // Unselect

      expect(screen.getByText('Select tags…')).toBeInTheDocument();
    });
  });
});
