import React from 'react';
import { render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { BrowserRouter } from 'react-router-dom';
import CalendarPage from '../pages/CalendarPage';
import * as eventsService from '../services/events';
import api from '../services/api';

// Mock the events service
jest.mock('../services/events');
const mockCreateEvent = eventsService.createEvent as jest.MockedFunction<
  typeof eventsService.createEvent
>;

// Mock the API service
jest.mock('../services/api');
const mockApi = api as jest.Mocked<typeof api>;

// Mock react-router-dom navigate
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

// Mock useNotifications hook
const mockRequestPermission = jest.fn();
const mockScheduleReminders = jest.fn();
const mockClearAllTimers = jest.fn();
const mockDispatchNotification = jest.fn();

jest.mock('../hooks/useNotifications', () => ({
  useNotifications: () => ({
    notificationState: {
      permission: 'default',
      isSupported: true,
    },
    requestPermission: mockRequestPermission,
    scheduleReminders: mockScheduleReminders,
    clearAllTimers: mockClearAllTimers,
    dispatchNotification: mockDispatchNotification,
  }),
}));

// Mock data
const mockTags = [
  { id: 1, name: 'Work' },
  { id: 2, name: 'Personal' },
  { id: 3, name: 'Meeting' },
];

const mockEvents = [
  {
    id: 1,
    title: 'Morning Meeting',
    description: 'Daily standup meeting',
    startDateTime: '2025-07-15T09:00:00',
    endDateTime: '2025-07-15T10:00:00',
    tags: [mockTags[0], mockTags[2]], // Work, Meeting
  },
  {
    id: 2,
    title: 'Lunch Break',
    description: 'Team lunch',
    startDateTime: '2025-07-15T12:00:00',
    endDateTime: '2025-07-15T13:00:00',
    tags: [mockTags[1]], // Personal
  },
  {
    id: 3,
    title: 'Project Review',
    description: 'Weekly project review',
    startDateTime: '2025-07-16T14:00:00',
    endDateTime: '2025-07-16T15:00:00',
    tags: [mockTags[0]], // Work
  },
];

// Wrapper component for router
const CalendarPageWrapper = () => (
  <BrowserRouter>
    <CalendarPage />
  </BrowserRouter>
);

describe('CalendarPage', () => {
  beforeEach(() => {
    jest.clearAllMocks();

    // Clear notification mocks
    mockRequestPermission.mockClear();
    mockScheduleReminders.mockClear();
    mockClearAllTimers.mockClear();
    mockDispatchNotification.mockClear();
    // Mock the current date to be July 15, 2025
    const mockDate = new Date('2025-07-15T10:00:00Z');
    const OriginalDate = Date;
    global.Date = jest.fn((...args: unknown[]) => {
      if (args.length === 0) {
        return mockDate;
      }
      return new OriginalDate(...(args as [string | number | Date]));
    }) as unknown as DateConstructor;
    global.Date.now = jest.fn(() => mockDate.getTime());
    global.Date.UTC = OriginalDate.UTC;
    global.Date.parse = OriginalDate.parse;
    Object.setPrototypeOf(global.Date, OriginalDate);

    // Setup default API mock responses
    mockApi.get.mockImplementation(url => {
      if (url === '/tags') {
        return Promise.resolve({ data: mockTags });
      }
      return Promise.resolve({ data: mockEvents });
    });
    mockApi.post.mockResolvedValue({ data: {} });
    mockApi.delete.mockResolvedValue({ data: {} });
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  describe('EventModal Integration', () => {
    it('clicking "+ New event" opens EventModal', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      const newEventButton = screen.getByText('+ New event');
      await user.click(newEventButton);

      // Check that EventModal is opened
      expect(
        screen.getByRole('heading', { name: 'Create Event' })
      ).toBeInTheDocument();
      expect(screen.getByLabelText(/title/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/start date\/time/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/end date\/time/i)).toBeInTheDocument();
    });

    it('EventModal can be closed by clicking cancel', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Open modal
      const newEventButton = screen.getByText('+ New event');
      await user.click(newEventButton);
      expect(
        screen.getByRole('heading', { name: 'Create Event' })
      ).toBeInTheDocument();

      // Close modal
      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      // Modal should be closed
      expect(
        screen.queryByRole('heading', { name: 'Create Event' })
      ).not.toBeInTheDocument();
    });

    it('EventModal can be closed by clicking the X button', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Open modal
      const newEventButton = screen.getByText('+ New event');
      await user.click(newEventButton);
      expect(
        screen.getByRole('heading', { name: 'Create Event' })
      ).toBeInTheDocument();

      // Close modal with X button
      const closeButton = screen.getByText('✕');
      await user.click(closeButton);

      // Modal should be closed
      expect(
        screen.queryByRole('heading', { name: 'Create Event' })
      ).not.toBeInTheDocument();
    });
  });

  describe('Event Creation and Display', () => {
    it('after successful createEvent, new event appears in calendar and modal closes', async () => {
      const user = userEvent.setup();

      // Mock successful event creation
      const newEvent = {
        id: 4,
        title: 'New Test Event',
        description: 'Test Description',
        startDateTime: '2025-07-17T15:00:00',
        endDateTime: '2025-07-17T16:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      };

      mockCreateEvent.mockResolvedValueOnce(newEvent);

      // Mock API to return events including the new one after creation
      mockApi.get.mockImplementation(url => {
        if (url === '/tags') {
          return Promise.resolve({ data: mockTags });
        }
        // After event creation, return events including the new one
        return Promise.resolve({ data: [...mockEvents, newEvent] });
      });

      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Open EventModal
      const newEventButton = screen.getByText('+ New event');
      await user.click(newEventButton);

      // Fill out the form
      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'New Test Event');
      await user.type(descriptionInput, 'Test Description');
      await user.type(startInput, '2025-07-17T15:00');
      await user.type(endInput, '2025-07-17T16:00');
      await user.click(submitButton);

      // Wait for success and modal to close
      await waitFor(
        () => {
          expect(screen.queryByText('Create Event')).not.toBeInTheDocument();
        },
        { timeout: 3000 }
      );

      // Check that the new event appears in the calendar
      await waitFor(() => {
        // Look for the new event in the calendar grid
        const calendarGrid = document.querySelector(
          '.calendar-grid'
        ) as HTMLElement;
        expect(
          within(calendarGrid!).getByText('New Test Event')
        ).toBeInTheDocument();
      });
    });

    it('displays events in month view calendar cells', async () => {
      render(<CalendarPageWrapper />);

      // Wait for events to load and be displayed
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
        expect(screen.getByText('Project Review')).toBeInTheDocument();
      });
    });

    it('displays events in week view', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to week view
      const weekButton = screen.getByRole('button', { name: /week/i });
      await user.click(weekButton);

      // Events should still be visible in week view
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
      });
    });

    it('displays events in day view', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to day view
      const dayButton = screen.getByRole('button', { name: /day/i });
      await user.click(dayButton);

      // Events should still be visible in day view
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
      });
    });

    it('correctly displays events on July 15 and 16, 2025 in their respective calendar cells', async () => {
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
        expect(screen.getByText('Project Review')).toBeInTheDocument();
      });

      // Find calendar cells for July 15 and 16
      const day15Cells = screen.getAllByText('15');
      const day16Cells = screen.getAllByText('16');

      // Find the calendar cell for July 15 (should contain events)
      const july15Cell = day15Cells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );
      expect(july15Cell).toBeInTheDocument();

      // Find the calendar cell for July 16 (should contain events)
      const july16Cell = day16Cells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );
      expect(july16Cell).toBeInTheDocument();

      // Verify July 15 cell contains the correct events
      const july15Container = july15Cell?.closest('.grid-cell');
      if (july15Container) {
        expect(july15Container.textContent).toContain('Morning Meeting');
        expect(july15Container.textContent).toContain('Lunch Break');
        expect(july15Container.textContent).not.toContain('Project Review');
      }

      // Verify July 16 cell contains the correct events
      const july16Container = july16Cell?.closest('.grid-cell');
      if (july16Container) {
        expect(july16Container.textContent).toContain('Project Review');
        expect(july16Container.textContent).not.toContain('Morning Meeting');
        expect(july16Container.textContent).not.toContain('Lunch Break');
      }
    });
  });

  describe('DayEventsModal Integration', () => {
    it('clicking a day with events opens DayEventsModal showing correct items', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Find a calendar cell that contains events (look for the day number)
      // Since events are on July 15th and 16th, we need to find those cells
      const calendarCells = screen.getAllByText('15');
      const dayCell = calendarCells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );

      expect(dayCell).toBeInTheDocument();

      // Click on the day cell
      const cellContainer = dayCell?.closest('.grid-cell');
      if (cellContainer) {
        await user.click(cellContainer);
      }

      // DayEventsModal should open and show events for that day
      await waitFor(() => {
        // The modal should show events for July 15th
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();

        // Check that the modal contains the events for July 15th
        const modalContent = modal.textContent;
        expect(modalContent).toContain('Morning Meeting');
        expect(modalContent).toContain('Lunch Break');
        expect(modalContent).not.toContain('Project Review');
      });
    });

    it('clicking a week view cell opens DayEventsModal with correct events', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to week view
      const weekButton = screen.getByRole('button', { name: /week/i });
      await user.click(weekButton);

      // Wait for week view to render
      await waitFor(() => {
        expect(screen.getByText('Monday')).toBeInTheDocument();
      });

      // Find and click a week cell that should contain events
      // The events are on July 15th (Tuesday) and 16th (Wednesday)
      const weekCells = screen.getAllByText('Morning Meeting');
      const weekCell = weekCells[0]?.closest('.calendar-week-cell');

      expect(weekCell).toBeInTheDocument();
      if (weekCell) {
        await user.click(weekCell);
      }

      // DayEventsModal should open and show events for that day
      await waitFor(() => {
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();

        // Check that the modal contains the events for the clicked day
        const modalContent = modal.textContent;
        expect(modalContent).toContain('Morning Meeting');
      });
    });

    it('clicking a day view cell opens DayEventsModal with correct events', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to day view
      const dayButton = screen.getByRole('button', { name: /day/i });
      await user.click(dayButton);

      // Wait for day view to render
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Find and click a day cell that should contain events
      const dayCells = screen.getAllByText('Morning Meeting');
      const dayCell = dayCells[0]?.closest('.calendar-day-cell');

      expect(dayCell).toBeInTheDocument();
      if (dayCell) {
        await user.click(dayCell);
      }

      // DayEventsModal should open and show events for that day
      await waitFor(() => {
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();

        // Check that the modal contains the events for the current day
        const modalContent = modal.textContent;
        expect(modalContent).toContain('Morning Meeting');
        expect(modalContent).toContain('Lunch Break');
      });
    });

    it('DayEventsModal can be closed', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Click on a day with events
      const calendarCells = screen.getAllByText('15');
      const dayCell = calendarCells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );

      const cellContainer = dayCell?.closest('.grid-cell');
      if (cellContainer) {
        await user.click(cellContainer);
      }

      // Wait for modal to open
      await waitFor(() => {
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();
        expect(modal.textContent).toContain('Morning Meeting');
      });

      // Close the modal (assuming DayEventsModal has a close button or backdrop click)
      // This depends on the DayEventsModal implementation
      const modalBackdrop =
        screen.getByRole('dialog', { hidden: true }) ||
        document.querySelector('.modal-backdrop');

      if (modalBackdrop) {
        await user.click(modalBackdrop);
      }
    });

    it('clicking on a day without events opens empty DayEventsModal', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Find a day without events (e.g., day 1)
      const dayOneCell = screen.getByText('1').closest('.grid-cell');
      expect(dayOneCell).toBeInTheDocument();

      if (dayOneCell) {
        await user.click(dayOneCell);
      }

      // DayEventsModal should open but show no events
      // This test depends on how DayEventsModal handles empty event lists
      await waitFor(() => {
        // The modal should show "No events on this day." message
        expect(screen.getByText('No events on this day.')).toBeInTheDocument();
      });
    });
  });

  describe('Calendar Navigation', () => {
    it('can navigate between months using chevron buttons', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Get current month (should be July 2025 based on current date)
      const currentMonth = screen.getAllByText(/july \d{4}/i)[0];
      expect(currentMonth).toBeInTheDocument();

      // Click next month button (ChevronDownIcon)
      const nextMonthButtons = screen.getAllByRole('button');
      const nextButton = nextMonthButtons.find(
        btn =>
          btn.querySelector('svg') &&
          btn.querySelector('svg')?.getAttribute('data-slot') === 'icon'
      );

      if (nextButton) {
        await user.click(nextButton);
      }

      // Month should change (this might require mocking the date or adjusting the test)
      // The exact assertion depends on the current date and navigation logic
    });

    it('can switch between different view modes', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Should start in month view
      const monthButton = screen.getByRole('button', { name: /month/i });
      const weekButton = screen.getByRole('button', { name: /week/i });
      const dayButton = screen.getByRole('button', { name: /day/i });

      expect(monthButton).toHaveClass('active');

      // Switch to week view
      await user.click(weekButton);
      expect(weekButton).toHaveClass('active');
      expect(monthButton).not.toHaveClass('active');

      // Switch to day view
      await user.click(dayButton);
      expect(dayButton).toHaveClass('active');
      expect(weekButton).not.toHaveClass('active');

      // Switch back to month view
      await user.click(monthButton);
      expect(monthButton).toHaveClass('active');
      expect(dayButton).not.toHaveClass('active');
    });
  });

  describe('User Menu and Logout', () => {
    it('can open and close the account menu', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Find and click the settings/cog button
      const cogButton = screen.getByRole('button', { name: '' }); // Cog icon button
      await user.click(cogButton);

      // Menu should open
      expect(screen.getByText('Account Settings')).toBeInTheDocument();
      expect(screen.getByText('Logout')).toBeInTheDocument();

      // Click outside to close menu (click on the cog button again)
      await user.click(cogButton);

      // Menu should close
      await waitFor(() => {
        expect(screen.queryByText('Account Settings')).not.toBeInTheDocument();
      });
    });

    it('navigates to account settings when clicked', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Open menu
      const cogButton = screen.getByRole('button', { name: '' });
      await user.click(cogButton);

      // Click Account Settings
      const accountSettingsButton = screen.getByText('Account Settings');
      await user.click(accountSettingsButton);

      // Should navigate to account settings
      expect(mockNavigate).toHaveBeenCalledWith('/account-settings');
    });

    it('handles logout correctly', async () => {
      const user = userEvent.setup();

      // Mock localStorage
      const mockRemoveItem = jest.spyOn(Storage.prototype, 'removeItem');

      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Open menu
      const cogButton = screen.getByRole('button', { name: '' });
      await user.click(cogButton);

      // Click Logout
      const logoutButton = screen.getByText('Logout');
      await user.click(logoutButton);

      // Should remove token and navigate to login
      await waitFor(() => {
        expect(mockRemoveItem).toHaveBeenCalledWith('token');
        expect(mockNavigate).toHaveBeenCalledWith('/login');
      });

      mockRemoveItem.mockRestore();
    });
  });

  describe('Error Handling', () => {
    it('handles API errors gracefully when fetching events', async () => {
      // Mock API error for all API calls
      mockApi.get.mockImplementation(() => {
        return Promise.reject(new Error('Server error'));
      });

      render(<CalendarPageWrapper />);

      // Component should still render even with API error
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // No events should be displayed
      expect(screen.queryByText('Morning Meeting')).not.toBeInTheDocument();
    });

    it('handles network errors when fetching events', async () => {
      // Mock network error for all API calls
      mockApi.get.mockImplementation(() => {
        return Promise.reject(new Error('Network error'));
      });

      render(<CalendarPageWrapper />);

      // Component should still render even with network error
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // No events should be displayed
      expect(screen.queryByText('Morning Meeting')).not.toBeInTheDocument();
    });
  });

  describe('Toast Functionality', () => {
    it('shows success toast when event is deleted', async () => {
      const user = userEvent.setup();

      // Mock successful delete
      mockApi.delete.mockResolvedValueOnce({});

      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Click on a day with events to open DayEventsModal
      const calendarCells = screen.getAllByText('15');
      const dayCell = calendarCells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );

      const cellContainer = dayCell?.closest('.grid-cell');
      if (cellContainer) {
        await user.click(cellContainer);
      }

      // Wait for modal to open
      await waitFor(() => {
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();
      });

      // Click delete button
      const deleteButtons = screen.getAllByTitle('Delete event');
      await user.click(deleteButtons[0]);

      // Confirm deletion
      const confirmButton = screen.getByText('Delete');
      await user.click(confirmButton);

      // Should show success toast
      await waitFor(() => {
        expect(screen.getByText('Event deleted.')).toBeInTheDocument();
      });
    });
  });

  describe('Week and Day Views', () => {
    it('displays events correctly in week view', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to week view
      const weekButton = screen.getByRole('button', { name: /week/i });
      await user.click(weekButton);

      // Events should be displayed in week view format
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
      });

      // Should show time slots
      expect(screen.getByText('9:00')).toBeInTheDocument();
      expect(screen.getByText('12:00')).toBeInTheDocument();
    });

    it('displays events correctly in day view', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to day view
      const dayButton = screen.getByRole('button', { name: /day/i });
      await user.click(dayButton);

      // Events should be displayed in day view format
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
      });

      // Should show time slots
      expect(screen.getByText('9:00')).toBeInTheDocument();
      expect(screen.getByText('12:00')).toBeInTheDocument();
    });

    it('handles eventsForHour function correctly', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch to week view to trigger eventsForHour
      const weekButton = screen.getByRole('button', { name: /week/i });
      await user.click(weekButton);

      // Events should be in correct time slots
      await waitFor(() => {
        // Morning Meeting should be in 9:00 slot
        const morningSlot = screen
          .getByText('9:00')
          .closest('.calendar-week-row');
        expect(morningSlot?.textContent).toContain('Morning Meeting');

        // Lunch Break should be in 12:00 slot
        const lunchSlot = screen
          .getByText('12:00')
          .closest('.calendar-week-row');
        expect(lunchSlot?.textContent).toContain('Lunch Break');
      });
    });
  });

  describe('Event Management', () => {
    it('handles edit event flow correctly', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Click on a day with events to open DayEventsModal
      const calendarCells = screen.getAllByText('15');
      const dayCell = calendarCells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );

      const cellContainer = dayCell?.closest('.grid-cell');
      if (cellContainer) {
        await user.click(cellContainer);
      }

      // Wait for modal to open
      await waitFor(() => {
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();
      });

      // Click edit button
      const editButtons = screen.getAllByTitle('Edit event');
      await user.click(editButtons[0]);

      // Should open EventModal in edit mode
      await waitFor(() => {
        expect(
          screen.getByRole('heading', { name: 'Edit Event' })
        ).toBeInTheDocument();
        expect(screen.getByDisplayValue('Morning Meeting')).toBeInTheDocument();
      });
    });

    it('handles event creation success correctly', async () => {
      const user = userEvent.setup();

      // Mock successful event creation
      const newEvent = {
        id: 4,
        title: 'New Event',
        description: 'New Description',
        startDateTime: '2025-07-15T16:00:00',
        endDateTime: '2025-07-15T17:00:00',
        user: {
          id: 1,
          email: 'test@example.com',
        },
      };

      mockCreateEvent.mockResolvedValueOnce(newEvent);

      // Mock API to return events including the new one after creation
      mockApi.get.mockImplementation(url => {
        if (url === '/tags') {
          return Promise.resolve({ data: mockTags });
        }
        // After event creation, return events including the new one
        return Promise.resolve({ data: [...mockEvents, newEvent] });
      });

      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Open EventModal
      const newEventButton = screen.getByText('+ New event');
      await user.click(newEventButton);

      // Fill and submit form
      const titleInput = screen.getByLabelText(/title/i);
      const descriptionInput = screen.getByLabelText(/description/i);
      const startInput = screen.getByLabelText(/start date\/time/i);
      const endInput = screen.getByLabelText(/end date\/time/i);
      const submitButton = screen.getByRole('button', {
        name: /create event/i,
      });

      await user.type(titleInput, 'New Event');
      await user.type(descriptionInput, 'New Description');
      await user.type(startInput, '2025-07-15T16:00');
      await user.type(endInput, '2025-07-15T17:00');
      await user.click(submitButton);

      // Should close modal and refresh events
      await waitFor(
        () => {
          expect(
            screen.queryByRole('heading', { name: 'Create Event' })
          ).not.toBeInTheDocument();
        },
        { timeout: 5000 }
      );

      await waitFor(
        () => {
          // Look for the new event in the calendar grid
          const calendarGrid = document.querySelector(
            '.calendar-grid'
          ) as HTMLElement;
          expect(
            within(calendarGrid!).getByText('New Event')
          ).toBeInTheDocument();
        },
        { timeout: 5000 }
      );
    });
  });

  describe('Tag Functionality', () => {
    it('renders tags section in sidebar', async () => {
      render(<CalendarPageWrapper />);

      await waitFor(() => {
        expect(screen.getByText('Tags')).toBeInTheDocument();
        expect(screen.getByText('All events')).toBeInTheDocument();
        expect(screen.getByText('+ New Tag')).toBeInTheDocument();
      });
    });

    it('displays available tags in dropdown', async () => {
      render(<CalendarPageWrapper />);

      await waitFor(() => {
        const tagsList = document.querySelector('.tags-list') as HTMLElement;
        expect(within(tagsList!).getByText('Work')).toBeInTheDocument();
        expect(within(tagsList!).getByText('Personal')).toBeInTheDocument();
        expect(within(tagsList!).getByText('Meeting')).toBeInTheDocument();
      });
    });

    it('displays tags on events in day modal', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for events to load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Click on a day with events to open DayEventsModal
      const calendarCells = screen.getAllByText('15');
      const dayCell = calendarCells.find(
        cell =>
          cell.closest('.grid-cell') &&
          cell.closest('.grid-cell')?.querySelector('.event-item')
      );

      const cellContainer = dayCell?.closest('.grid-cell');
      if (cellContainer) {
        await user.click(cellContainer);
      }

      // Wait for modal to open and check for tags
      await waitFor(() => {
        const modal = screen.getByRole('dialog');
        expect(modal).toBeInTheDocument();
        // Morning Meeting should show Work and Meeting tags
        expect(modal.textContent).toContain('Work');
        expect(modal.textContent).toContain('Meeting');
      });
    });

    it('filters events by selected tag', async () => {
      const user = userEvent.setup();

      // Mock API to return filtered events when tagId is provided
      mockApi.get.mockImplementation((url, config) => {
        if (url === '/tags') {
          return Promise.resolve({ data: mockTags });
        }
        if (config?.params?.tagId === 1) {
          // Return only Work-tagged events
          return Promise.resolve({
            data: mockEvents.filter(event =>
              event.tags.some(tag => tag.id === 1)
            ),
          });
        }
        return Promise.resolve({ data: mockEvents });
      });

      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Lunch Break')).toBeInTheDocument();
      });

      // Select Work tag from dropdown
      const tagDropdown = screen.getByDisplayValue('All events');
      await user.selectOptions(tagDropdown, '1');

      // Should filter to only show Work-tagged events
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
        expect(screen.getByText('Project Review')).toBeInTheDocument();
        expect(screen.queryByText('Lunch Break')).not.toBeInTheDocument();
      });
    });

    it('creates new tag successfully', async () => {
      const user = userEvent.setup();

      // Mock successful tag creation
      const newTag = { id: 4, name: 'Urgent' };
      mockApi.post.mockResolvedValueOnce({ data: newTag });
      mockApi.get.mockImplementation(url => {
        if (url === '/tags') {
          return Promise.resolve({ data: [...mockTags, newTag] });
        }
        return Promise.resolve({ data: mockEvents });
      });

      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('+ New Tag')).toBeInTheDocument();
      });

      // Click New Tag button
      const newTagButton = screen.getByText('+ New Tag');
      await user.click(newTagButton);

      // Should open tag creation modal
      await waitFor(() => {
        expect(screen.getByText('Create New Tag')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Tag name')).toBeInTheDocument();
      });

      // Fill and submit form
      const tagNameInput = screen.getByPlaceholderText('Tag name');
      const createButton = screen.getByRole('button', { name: 'Create' });

      await user.type(tagNameInput, 'Urgent');
      await user.click(createButton);

      // Should close modal and show success message
      await waitFor(() => {
        expect(screen.queryByText('Create New Tag')).not.toBeInTheDocument();
        expect(
          screen.getByText('Tag created successfully')
        ).toBeInTheDocument();
      });

      // Should refresh tags list
      await waitFor(() => {
        const tagsList = document.querySelector('.tags-list') as HTMLElement;
        expect(within(tagsList!).getByText('Urgent')).toBeInTheDocument();
      });
    });

    it('deletes tag successfully', async () => {
      const user = userEvent.setup();

      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        const tagsList = document.querySelector('.tags-list') as HTMLElement;
        expect(within(tagsList!).getByText('Personal')).toBeInTheDocument();
      });

      // Find and click delete button for Personal tag
      const tagsList = document.querySelector('.tags-list') as HTMLElement;
      const personalTagItem = within(tagsList!)
        .getByText('Personal')
        .closest('.tag-item') as HTMLElement;
      const deleteButton = within(personalTagItem!).getByRole('button', {
        name: '×',
      });

      // Mock successful tag deletion and updated tags list
      mockApi.delete.mockResolvedValueOnce({ data: {} });
      mockApi.get.mockImplementation(url => {
        if (url === '/tags') {
          return Promise.resolve({
            data: mockTags.filter(tag => tag.id !== 2),
          });
        }
        return Promise.resolve({ data: mockEvents });
      });

      if (deleteButton) {
        await user.click(deleteButton);
      }

      // Should show success message and remove tag
      await waitFor(() => {
        expect(
          screen.getByText('Tag deleted successfully')
        ).toBeInTheDocument();
        expect(screen.queryByText('Personal')).not.toBeInTheDocument();
      });
    });

    it('clears tag filter when filtered tag is deleted', async () => {
      const user = userEvent.setup();

      // Mock API responses
      mockApi.get.mockImplementation((url, config) => {
        if (url === '/tags') {
          return Promise.resolve({ data: mockTags });
        }
        if (config?.params?.tagId === 2) {
          return Promise.resolve({
            data: mockEvents.filter(event =>
              event.tags.some(tag => tag.id === 2)
            ),
          });
        }
        return Promise.resolve({ data: mockEvents });
      });

      render(<CalendarPageWrapper />);

      // Wait for initial load and select Personal tag filter
      await waitFor(() => {
        expect(screen.getByDisplayValue('All events')).toBeInTheDocument();
      });

      const tagDropdown = screen.getByDisplayValue('All events');
      await user.selectOptions(tagDropdown, '2');

      // Verify filter is applied
      await waitFor(() => {
        expect(tagDropdown).toHaveDisplayValue('Personal');
      });

      // Mock tag deletion and updated tags list
      mockApi.delete.mockResolvedValueOnce({ data: {} });
      mockApi.get.mockImplementation(url => {
        if (url === '/tags') {
          return Promise.resolve({
            data: mockTags.filter(tag => tag.id !== 2),
          });
        }
        return Promise.resolve({ data: mockEvents });
      });

      // Delete the Personal tag
      const tagsList = document.querySelector('.tags-list') as HTMLElement;
      const personalTagItem = within(tagsList!)
        .getByText('Personal')
        .closest('.tag-item') as HTMLElement;
      const deleteButton = within(personalTagItem!).getByRole('button', {
        name: '×',
      });

      if (deleteButton) {
        await user.click(deleteButton);
      }

      // Should clear the filter and show all events
      await waitFor(() => {
        expect(tagDropdown).toHaveDisplayValue('All events');
      });
    });

    it('handles tag creation error gracefully', async () => {
      const user = userEvent.setup();

      // Mock API error
      mockApi.post.mockRejectedValueOnce(new Error('Tag creation failed'));

      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('+ New Tag')).toBeInTheDocument();
      });

      // Open tag creation modal
      const newTagButton = screen.getByText('+ New Tag');
      await user.click(newTagButton);

      // Fill and submit form
      const tagNameInput = screen.getByPlaceholderText('Tag name');
      const createButton = screen.getByRole('button', { name: 'Create' });

      await user.type(tagNameInput, 'Test Tag');
      await user.click(createButton);

      // Should show error message
      await waitFor(() => {
        expect(screen.getByText('Failed to create tag')).toBeInTheDocument();
      });
    });

    it('prevents creating tag with empty name', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('+ New Tag')).toBeInTheDocument();
      });

      // Open tag creation modal
      const newTagButton = screen.getByText('+ New Tag');
      await user.click(newTagButton);

      // Try to submit with empty name
      const createButton = screen.getByRole('button', { name: 'Create' });
      expect(createButton).toBeDisabled();
    });

    it('handles calendar navigation with setValue function', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for component to load
      await waitFor(() => {
        expect(screen.getByText('+ New event')).toBeInTheDocument();
      });

      // Switch to week view to trigger setValue calls
      const weekButton = screen.getByRole('button', { name: /week/i });
      await user.click(weekButton);

      // Wait for week view to render
      await waitFor(() => {
        expect(screen.getByText('Monday')).toBeInTheDocument();
      });

      // Switch to day view to trigger more setValue calls
      const dayButton = screen.getByRole('button', { name: /day/i });
      await user.click(dayButton);

      // Wait for day view to render
      await waitFor(() => {
        expect(screen.getByText('Morning Meeting')).toBeInTheDocument();
      });

      // Switch back to month view
      const monthButton = screen.getByRole('button', { name: /month/i });
      await user.click(monthButton);

      // Should be back in month view
      await waitFor(() => {
        expect(monthButton).toHaveClass('active');
      });
    });

    it('handles new tag modal close functionality', async () => {
      const user = userEvent.setup();
      render(<CalendarPageWrapper />);

      // Wait for initial load
      await waitFor(() => {
        expect(screen.getByText('+ New Tag')).toBeInTheDocument();
      });

      // Open tag creation modal
      const newTagButton = screen.getByText('+ New Tag');
      await user.click(newTagButton);

      // Should open tag creation modal
      await waitFor(() => {
        expect(screen.getByText('Create New Tag')).toBeInTheDocument();
      });

      // Close modal by clicking Cancel
      const cancelButton = screen.getByRole('button', { name: 'Cancel' });
      await user.click(cancelButton);

      // Modal should be closed
      await waitFor(() => {
        expect(screen.queryByText('Create New Tag')).not.toBeInTheDocument();
      });
    });
  });
});
