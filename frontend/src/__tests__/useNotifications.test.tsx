import { renderHook, act } from '@testing-library/react';
import { useNotifications } from '../hooks/useNotifications';

// Mock the Notification API
const mockRequestPermission = jest.fn();
const mockNotification = jest.fn() as unknown as jest.MockedClass<
  typeof Notification
> & {
  requestPermission: jest.MockedFunction<typeof Notification.requestPermission>;
  permission: NotificationPermission;
};
mockNotification.requestPermission = mockRequestPermission;
mockNotification.permission = 'default' as NotificationPermission;

// Mock localStorage
const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
};

// Mock document.visibilityState
Object.defineProperty(document, 'visibilityState', {
  writable: true,
  value: 'visible',
});

// Mock window.focus
Object.defineProperty(window, 'focus', {
  writable: true,
  value: jest.fn(),
});

// Mock window.location
Object.defineProperty(window, 'location', {
  writable: true,
  value: {
    pathname: '/calendar',
    href: '',
  },
});

describe('useNotifications', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.useFakeTimers();

    // Reset document visibility state
    Object.defineProperty(document, 'visibilityState', {
      writable: true,
      value: 'visible',
    });

    // Mock Notification API
    Object.defineProperty(window, 'Notification', {
      writable: true,
      configurable: true,
      value: mockNotification,
    });

    mockNotification.permission = 'default';

    // Mock localStorage
    Object.defineProperty(window, 'localStorage', {
      writable: true,
      value: mockLocalStorage,
    });

    mockLocalStorage.getItem.mockReturnValue(null);
  });

  afterEach(() => {
    jest.useRealTimers();
    jest.restoreAllMocks();
  });

  describe('initialization', () => {
    it('should initialize with correct default state when Notification is supported', () => {
      const { result } = renderHook(() => useNotifications());

      expect(result.current.notificationState).toEqual({
        permission: 'default',
        isSupported: true,
      });
    });

    it('should initialize with unsupported state when Notification is not available', () => {
      // Remove Notification from window
      Object.defineProperty(window, 'Notification', {
        writable: true,
        configurable: true,
        value: undefined,
      });

      const { result } = renderHook(() => useNotifications());

      expect(result.current.notificationState).toEqual({
        permission: 'denied',
        isSupported: false,
      });
    });
  });

  describe('requestPermission', () => {
    it('should request permission when permission is default', async () => {
      mockRequestPermission.mockResolvedValue('granted');

      const { result } = renderHook(() => useNotifications());

      await act(async () => {
        const permission = await result.current.requestPermission();
        expect(permission).toBe('granted');
      });

      expect(mockRequestPermission).toHaveBeenCalledTimes(1);
      expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
        'notificationPermission',
        'granted'
      );
    });

    it('should not request permission when already denied', async () => {
      mockNotification.permission = 'denied';

      const { result } = renderHook(() => useNotifications());

      await act(async () => {
        const permission = await result.current.requestPermission();
        expect(permission).toBe('denied');
      });

      expect(mockRequestPermission).not.toHaveBeenCalled();
    });

    it('should not request permission when already granted', async () => {
      mockNotification.permission = 'granted';

      const { result } = renderHook(() => useNotifications());

      await act(async () => {
        const permission = await result.current.requestPermission();
        expect(permission).toBe('granted');
      });

      expect(mockRequestPermission).not.toHaveBeenCalled();
    });

    it('should return denied when Notification is not supported', async () => {
      // Remove Notification from window
      Object.defineProperty(window, 'Notification', {
        writable: true,
        configurable: true,
        value: undefined,
      });

      const { result } = renderHook(() => useNotifications());

      await act(async () => {
        const permission = await result.current.requestPermission();
        expect(permission).toBe('denied');
      });
    });

    it('should handle requestPermission errors', async () => {
      mockRequestPermission.mockRejectedValue(
        new Error('Permission request failed')
      );
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const { result } = renderHook(() => useNotifications());

      await act(async () => {
        const permission = await result.current.requestPermission();
        expect(permission).toBe('denied');
      });

      expect(consoleSpy).toHaveBeenCalledWith(
        'Failed to request notification permission:',
        expect.any(Error)
      );
      consoleSpy.mockRestore();
    });
  });

  describe('scheduleReminders', () => {
    const mockEvents = [
      {
        id: 1,
        title: 'Test Event 1',
        description: 'Test Description 1',
        startDateTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(), // 15 minutes from now
        endDateTime: new Date(Date.now() + 16 * 60 * 1000).toISOString(),
      },
      {
        id: 2,
        title: 'Test Event 2',
        description: 'Test Description 2',
        startDateTime: new Date(Date.now() + 30 * 60 * 1000).toISOString(), // 30 minutes from now
        endDateTime: new Date(Date.now() + 31 * 60 * 1000).toISOString(),
      },
    ];

    it('should schedule reminders for future events when permission is granted', () => {
      mockNotification.permission = 'granted';

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      // Fast-forward time to trigger the first reminder (5 minutes from now)
      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      expect(mockNotification).toHaveBeenCalledWith(
        'Upcoming Event: Test Event 1',
        {
          body: 'Starting in 10 minutes: Test Description 1',
          tag: '1',
          icon: '/favicon.ico',
        }
      );
    });

    it('should not schedule reminders when permission is denied', () => {
      mockNotification.permission = 'denied';

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      expect(mockNotification).not.toHaveBeenCalled();
    });

    it('should not schedule reminders when Notification is not supported', () => {
      // Remove Notification from window
      Object.defineProperty(window, 'Notification', {
        writable: true,
        configurable: true,
        value: undefined,
      });

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      expect(mockNotification).not.toHaveBeenCalled();
    });

    it('should not schedule reminders for past events', () => {
      mockNotification.permission = 'granted';

      const pastEvents = [
        {
          id: 3,
          title: 'Past Event',
          description: 'Past Description',
          startDateTime: new Date(Date.now() - 60 * 1000).toISOString(), // 1 minute ago
          endDateTime: new Date(Date.now()).toISOString(),
        },
      ];

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.scheduleReminders(pastEvents);
      });

      act(() => {
        jest.advanceTimersByTime(60 * 1000);
      });

      expect(mockNotification).not.toHaveBeenCalled();
    });

    it('should clear existing timers when scheduling new reminders', () => {
      mockNotification.permission = 'granted';

      const { result } = renderHook(() => useNotifications());

      // Schedule first set of reminders
      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      // Schedule new set of reminders (should clear previous ones)
      const newEvents = [
        {
          id: 4,
          title: 'New Event',
          description: 'New Description',
          startDateTime: new Date(Date.now() + 20 * 60 * 1000).toISOString(),
          endDateTime: new Date(Date.now() + 21 * 60 * 1000).toISOString(),
        },
      ];

      act(() => {
        result.current.scheduleReminders(newEvents);
      });

      // Fast-forward to when original events would have triggered
      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      // Original events should not trigger
      expect(mockNotification).not.toHaveBeenCalled();
    });
  });

  describe('page visibility handling', () => {
    it('should queue notifications when document is hidden', () => {
      mockNotification.permission = 'granted';

      // Set document to hidden BEFORE rendering the hook
      Object.defineProperty(document, 'visibilityState', {
        writable: true,
        value: 'hidden',
      });

      const { result } = renderHook(() => useNotifications());

      const mockEvents = [
        {
          id: 1,
          title: 'Test Event',
          description: 'Test Description',
          startDateTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
          endDateTime: new Date(Date.now() + 16 * 60 * 1000).toISOString(),
        },
      ];

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      // Fast-forward to trigger reminder
      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      // Notification should not be dispatched immediately when hidden
      expect(mockNotification).not.toHaveBeenCalled();
    });

    it('should dispatch queued notifications when document becomes visible', () => {
      mockNotification.permission = 'granted';

      // Start with hidden document BEFORE rendering the hook
      Object.defineProperty(document, 'visibilityState', {
        writable: true,
        value: 'hidden',
      });

      const { result } = renderHook(() => useNotifications());

      const mockEvents = [
        {
          id: 1,
          title: 'Test Event',
          description: 'Test Description',
          startDateTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
          endDateTime: new Date(Date.now() + 16 * 60 * 1000).toISOString(),
        },
      ];

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      // Fast-forward to trigger reminder (should be queued)
      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      expect(mockNotification).not.toHaveBeenCalled();

      // Make document visible
      Object.defineProperty(document, 'visibilityState', {
        writable: true,
        value: 'visible',
      });

      // Trigger visibility change event
      act(() => {
        document.dispatchEvent(new Event('visibilitychange'));
      });

      // Now notification should be dispatched
      expect(mockNotification).toHaveBeenCalledWith(
        'Upcoming Event: Test Event',
        {
          body: 'Starting in 10 minutes: Test Description',
          tag: '1',
          icon: '/favicon.ico',
        }
      );
    });
  });

  describe('notification click handling', () => {
    it('should focus window and navigate to calendar on notification click', () => {
      mockNotification.permission = 'granted';
      const mockClose = jest.fn();
      const mockNotificationInstance = {
        onclick: jest.fn() as () => void,
        close: mockClose,
      } as Partial<Notification> as Notification;

      mockNotification.mockReturnValue(mockNotificationInstance);
      window.location.pathname = '/other-page';

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.dispatchNotification('Test Title', 'Test Body', 1);
      });

      expect(mockNotification).toHaveBeenCalled();

      // Simulate notification click
      act(() => {
        if (mockNotificationInstance.onclick) {
          mockNotificationInstance.onclick({} as Event);
        }
      });

      expect(window.focus).toHaveBeenCalled();
      expect(window.location.href).toBe('/calendar');
      expect(mockClose).toHaveBeenCalled();
    });

    it('should not navigate if already on calendar page', () => {
      mockNotification.permission = 'granted';
      const mockClose = jest.fn();
      const mockNotificationInstance = {
        onclick: jest.fn() as () => void,
        close: mockClose,
      } as Partial<Notification> as Notification;

      mockNotification.mockReturnValue(mockNotificationInstance);
      window.location.pathname = '/calendar';
      const originalHref = window.location.href;

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.dispatchNotification('Test Title', 'Test Body', 1);
      });

      // Simulate notification click
      act(() => {
        if (mockNotificationInstance.onclick) {
          mockNotificationInstance.onclick({} as Event);
        }
      });

      expect(window.focus).toHaveBeenCalled();
      expect(window.location.href).toBe(originalHref); // Should not change
      expect(mockClose).toHaveBeenCalled();
    });
  });

  describe('error handling', () => {
    it('should handle notification creation errors gracefully', () => {
      mockNotification.permission = 'granted';
      mockNotification.mockImplementation(() => {
        throw new Error('Notification creation failed');
      });

      const consoleSpy = jest.spyOn(console, 'error').mockImplementation();

      const { result } = renderHook(() => useNotifications());

      act(() => {
        result.current.dispatchNotification('Test Title', 'Test Body', 1);
      });

      expect(consoleSpy).toHaveBeenCalledWith(
        'Failed to create notification:',
        expect.any(Error)
      );
      consoleSpy.mockRestore();
    });
  });

  describe('cleanup', () => {
    it('should clear all timers when clearAllTimers is called', () => {
      mockNotification.permission = 'granted';

      const { result } = renderHook(() => useNotifications());

      const mockEvents = [
        {
          id: 1,
          title: 'Test Event',
          description: 'Test Description',
          startDateTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
          endDateTime: new Date(Date.now() + 16 * 60 * 1000).toISOString(),
        },
      ];

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      act(() => {
        result.current.clearAllTimers();
      });

      // Fast-forward to when reminder would have triggered
      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      // Notification should not be dispatched after clearing timers
      expect(mockNotification).not.toHaveBeenCalled();
    });

    it('should clear timers on unmount', () => {
      mockNotification.permission = 'granted';

      const { result, unmount } = renderHook(() => useNotifications());

      const mockEvents = [
        {
          id: 1,
          title: 'Test Event',
          description: 'Test Description',
          startDateTime: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
          endDateTime: new Date(Date.now() + 16 * 60 * 1000).toISOString(),
        },
      ];

      act(() => {
        result.current.scheduleReminders(mockEvents);
      });

      unmount();

      // Fast-forward to when reminder would have triggered
      act(() => {
        jest.advanceTimersByTime(5 * 60 * 1000);
      });

      // Notification should not be dispatched after unmount
      expect(mockNotification).not.toHaveBeenCalled();
    });
  });
});
