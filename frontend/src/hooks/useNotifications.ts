import { useState, useEffect, useRef, useCallback } from 'react';

export interface EventDTO {
  id: number;
  title: string;
  description: string;
  startDateTime: string; // ISO
  endDateTime: string; // ISO
}

// Default reminder offset: 10 minutes before event
const REMINDER_OFFSET_MS = 10 * 60 * 1000; // 10 minutes in milliseconds

interface NotificationState {
  permission: NotificationPermission;
  isSupported: boolean;
}

interface QueuedNotification {
  eventId: number;
  title: string;
  body: string;
}

export function useNotifications() {
  const [notificationState, setNotificationState] = useState<NotificationState>(
    {
      permission: 'default',
      isSupported: false,
    }
  );

  const timersRef = useRef<Map<number, NodeJS.Timeout>>(new Map());
  const queuedNotificationsRef = useRef<QueuedNotification[]>([]);
  const isDocumentVisibleRef = useRef(true);

  // Initialize notification support and permission state
  useEffect(() => {
    const isSupported =
      'Notification' in window && window.Notification !== undefined;
    const permission = isSupported ? Notification.permission : 'denied';

    setNotificationState({
      permission,
      isSupported,
    });

    // Load stored permission from localStorage if available
    const storedPermission = localStorage.getItem('notificationPermission');
    if (storedPermission && storedPermission !== permission) {
      // Update stored permission if it differs from current
      localStorage.setItem('notificationPermission', permission);
    }
  }, []);

  // Dispatch a notification
  const dispatchNotification = useCallback(
    (title: string, body: string, eventId: number) => {
      if (
        !notificationState.isSupported ||
        notificationState.permission !== 'granted'
      ) {
        return;
      }

      try {
        const notification = new Notification(title, {
          body,
          tag: eventId.toString(),
          icon: '/favicon.ico', // Optional: add an icon
        });

        notification.onclick = () => {
          window.focus();
          // Navigate to calendar if not already there
          if (window.location.pathname !== '/calendar') {
            window.location.href = '/calendar';
          }
          notification.close();
        };

        // Auto-close notification after 10 seconds
        setTimeout(() => {
          notification.close();
        }, 10000);
      } catch (error) {
        console.error('Failed to create notification:', error);
      }
    },
    [notificationState.isSupported, notificationState.permission]
  );

  // Handle page visibility changes
  useEffect(() => {
    const handleVisibilityChange = () => {
      const isVisible = document.visibilityState === 'visible';
      isDocumentVisibleRef.current = isVisible;

      // If page becomes visible and we have queued notifications, dispatch them
      if (isVisible && queuedNotificationsRef.current.length > 0) {
        queuedNotificationsRef.current.forEach(notification => {
          dispatchNotification(
            notification.title,
            notification.body,
            notification.eventId
          );
        });
        queuedNotificationsRef.current = [];
      }
    };

    // Initialize visibility state
    isDocumentVisibleRef.current = document.visibilityState === 'visible';

    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () =>
      document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [dispatchNotification]);

  // Request notification permission
  const requestPermission =
    useCallback(async (): Promise<NotificationPermission> => {
      if (!notificationState.isSupported) {
        return 'denied';
      }

      // Don't re-prompt if already denied
      if (notificationState.permission === 'denied') {
        return 'denied';
      }

      // Don't re-prompt if already granted
      if (notificationState.permission === 'granted') {
        return 'granted';
      }

      try {
        const permission = await Notification.requestPermission();
        localStorage.setItem('notificationPermission', permission);

        setNotificationState(prev => ({
          ...prev,
          permission,
        }));

        return permission;
      } catch (error) {
        console.error('Failed to request notification permission:', error);
        return 'denied';
      }
    }, [notificationState.isSupported, notificationState.permission]);

  // Schedule reminders for events
  const scheduleReminders = useCallback(
    (events: EventDTO[]) => {
      // Clear existing timers
      timersRef.current.forEach(timer => clearTimeout(timer));
      timersRef.current.clear();

      // Skip if notifications not supported or not granted
      if (
        !notificationState.isSupported ||
        notificationState.permission !== 'granted'
      ) {
        return;
      }

      const now = new Date().getTime();

      events.forEach(event => {
        const eventStart = new Date(event.startDateTime).getTime();
        const delay = eventStart - now - REMINDER_OFFSET_MS;

        // Only schedule if the reminder time is in the future
        if (delay > 0) {
          const timer = setTimeout(() => {
            const title = `Upcoming Event: ${event.title}`;
            const body = `Starting in 10 minutes${event.description ? `: ${event.description}` : ''}`;

            // If document is hidden, queue the notification
            if (!isDocumentVisibleRef.current) {
              queuedNotificationsRef.current.push({
                eventId: event.id,
                title,
                body,
              });
            } else {
              dispatchNotification(title, body, event.id);
            }

            // Remove timer from map after it fires
            timersRef.current.delete(event.id);
          }, delay);

          timersRef.current.set(event.id, timer);
        }
      });
    },
    [
      notificationState.isSupported,
      notificationState.permission,
      dispatchNotification,
    ]
  );

  // Clear all timers (for cleanup)
  const clearAllTimers = useCallback(() => {
    timersRef.current.forEach(timer => clearTimeout(timer));
    timersRef.current.clear();
    queuedNotificationsRef.current = [];
  }, []);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      clearAllTimers();
    };
  }, [clearAllTimers]);

  return {
    notificationState,
    requestPermission,
    scheduleReminders,
    clearAllTimers,
    dispatchNotification,
  };
}
