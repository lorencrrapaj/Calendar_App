import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import DayEventsModal from '../components/DayEventsModal';
import { EventDTO } from '../pages/CalendarPage';

describe('DayEventsModal', () => {
  const mockDate = new Date('2025-07-15T10:00:00Z');
  const mockEvents: EventDTO[] = [
    {
      id: 1,
      title: 'Morning Meeting',
      description: 'Daily standup meeting',
      startDateTime: '2025-07-15T09:00:00',
      endDateTime: '2025-07-15T10:00:00',
    },
    {
      id: 2,
      title: 'Lunch Break',
      description: 'Team lunch',
      startDateTime: '2025-07-15T12:00:00',
      endDateTime: '2025-07-15T13:00:00',
    },
  ];

  const defaultProps = {
    date: mockDate,
    events: mockEvents,
    onClose: jest.fn(),
    onEditEvent: jest.fn(),
    onDeleteEvent: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the modal with date and events', () => {
    render(<DayEventsModal {...defaultProps} />);

    expect(screen.getByText('Title: Morning Meeting')).toBeInTheDocument();
    expect(screen.getByText('Daily standup meeting')).toBeInTheDocument();
    expect(screen.getByText('Title: Lunch Break')).toBeInTheDocument();
    expect(screen.getByText('Team lunch')).toBeInTheDocument();
  });

  it('displays "No events on this day" when events array is empty', () => {
    render(<DayEventsModal {...defaultProps} events={[]} />);

    expect(screen.getByText('No events on this day.')).toBeInTheDocument();
  });

  it('calls onClose when close button is clicked', () => {
    render(<DayEventsModal {...defaultProps} />);

    const closeButton = screen.getByText('✕');
    fireEvent.click(closeButton);

    expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
  });

  it('calls onEditEvent when edit button is clicked', () => {
    render(<DayEventsModal {...defaultProps} />);

    const editButtons = screen.getAllByTitle('Edit event');
    fireEvent.click(editButtons[0]);

    expect(defaultProps.onEditEvent).toHaveBeenCalledWith(mockEvents[0]);
  });

  it('opens confirmation modal when delete button is clicked', () => {
    render(<DayEventsModal {...defaultProps} />);

    const deleteButtons = screen.getAllByTitle('Delete event');
    fireEvent.click(deleteButtons[0]);

    expect(screen.getByText('Are you sure?')).toBeInTheDocument();
    expect(
      screen.getByText(
        "Are you sure you want to delete 'Morning Meeting'? This cannot be undone."
      )
    ).toBeInTheDocument();
  });

  it('calls onDeleteEvent when delete is confirmed', () => {
    render(<DayEventsModal {...defaultProps} />);

    // Click delete button to open confirmation modal
    const deleteButtons = screen.getAllByTitle('Delete event');
    fireEvent.click(deleteButtons[0]);

    // Confirm deletion
    const confirmButton = screen.getByText('Delete');
    fireEvent.click(confirmButton);

    expect(defaultProps.onDeleteEvent).toHaveBeenCalledWith(
      mockEvents[0],
      'instance'
    );
  });

  it('closes confirmation modal when delete is cancelled', () => {
    render(<DayEventsModal {...defaultProps} />);

    // Click delete button to open confirmation modal
    const deleteButtons = screen.getAllByTitle('Delete event');
    fireEvent.click(deleteButtons[0]);

    expect(screen.getByText('Are you sure?')).toBeInTheDocument();

    // Cancel deletion
    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    // Confirmation modal should be closed
    expect(screen.queryByText('Are you sure?')).not.toBeInTheDocument();
    expect(defaultProps.onDeleteEvent).not.toHaveBeenCalled();
  });

  it('displays event times correctly', () => {
    render(<DayEventsModal {...defaultProps} />);

    // Check that time formatting is displayed (exact format may vary by locale)
    expect(screen.getByText(/09:00.*AM.*–.*10:00.*AM/)).toBeInTheDocument();
    expect(screen.getByText(/12:00.*PM.*–.*01:00.*PM/)).toBeInTheDocument();
  });

  it('displays formatted date in header', () => {
    render(<DayEventsModal {...defaultProps} />);

    // Check that the date is formatted and displayed in the header
    const dateHeader = screen.getByRole('dialog').querySelector('h2');
    expect(dateHeader).toBeInTheDocument();
    expect(dateHeader?.textContent).toMatch(/July.*15/); // Should contain month and day
  });

  it('handles multiple events correctly', () => {
    const manyEvents: EventDTO[] = [
      ...mockEvents,
      {
        id: 3,
        title: 'Afternoon Meeting',
        description: 'Project review',
        startDateTime: '2025-07-15T15:00:00',
        endDateTime: '2025-07-15T16:00:00',
      },
    ];

    render(<DayEventsModal {...defaultProps} events={manyEvents} />);

    expect(screen.getByText('Title: Morning Meeting')).toBeInTheDocument();
    expect(screen.getByText('Title: Lunch Break')).toBeInTheDocument();
    expect(screen.getByText('Title: Afternoon Meeting')).toBeInTheDocument();

    // Should have 3 edit buttons and 3 delete buttons
    expect(screen.getAllByTitle('Edit event')).toHaveLength(3);
    expect(screen.getAllByTitle('Delete event')).toHaveLength(3);
  });
});
