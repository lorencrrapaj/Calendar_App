// src/pages/CalendarPage.tsx
import React, { FC, useState, useRef, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import DayEventsModal from '../components/DayEventsModal';
import EventModal from '../components/EventModal';
import Toast from '../components/Toast';
import {
  CalendarIcon,
  ChevronUpIcon,
  ChevronDownIcon,
  CogIcon,
} from '@heroicons/react/24/outline';
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import api from '../services/api';
import { deleteEvent } from '../services/events';
import { useNotifications } from '../hooks/useNotifications';

type ViewMode = 'day' | 'week' | 'month';
const viewModes: ViewMode[] = ['day', 'week', 'month'];

export interface TagDTO {
  id: number;
  name: string;
}

export interface EventDTO {
  id: number;
  title: string;
  description: string;
  startDateTime: string; // ISO
  endDateTime: string; // ISO
  userEmail?: string;
  recurrenceRule?: string;
  recurrenceEndDate?: string;
  recurrenceCount?: number;
  parentEventId?: number;
  originalStartDateTime?: string;
  excludedDates?: string;
  tags?: TagDTO[]; // Tags associated with this event
  editScope?: 'instance' | 'series'; // Frontend-only property for edit scope
}

const CalendarPage: FC = () => {
  const navigate = useNavigate();

  // --- NOTIFICATIONS ---
  const {
    notificationState,
    requestPermission,
    scheduleReminders,
    clearAllTimers,
  } = useNotifications();

  // --- STATE ---
  const [value, setValue] = useState<Date>(new Date());
  const [view, setView] = useState<ViewMode>('month');
  const [menuOpen, setMenuOpen] = useState(false);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [showDayModal, setShowDayModal] = useState(false);
  const [editEvent, setEditEvent] = useState<EventDTO | null>(null);
  const [toast, setToast] = useState<{
    message: string;
    type: 'success' | 'error';
    isVisible: boolean;
  }>({
    message: '',
    type: 'success',
    isVisible: false,
  });

  // --- TAGS STATE ---
  const [availableTags, setAvailableTags] = useState<TagDTO[]>([]);
  const [selectedTagFilter, setSelectedTagFilter] = useState<number | null>(
    null
  );
  const [showNewTagModal, setShowNewTagModal] = useState(false);
  const [newTagName, setNewTagName] = useState('');

  // for clicking outside sidebar menu
  const menuRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  // Request notification permission on mount
  useEffect(() => {
    if (
      notificationState.isSupported &&
      notificationState.permission === 'default'
    ) {
      requestPermission();
    }
  }, [
    notificationState.isSupported,
    notificationState.permission,
    requestPermission,
  ]);

  // Cleanup timers on unmount
  useEffect(() => {
    return () => {
      clearAllTimers();
    };
  }, [clearAllTimers]);

  // --- FETCH TAGS ---
  const fetchTags = useCallback(async () => {
    try {
      const res = await api.get<TagDTO[]>('/tags');
      setAvailableTags(res.data);
    } catch (err) {
      console.error('Failed to fetch tags:', err);
      showToast('Failed to load tags', 'error');
    }
  }, []);

  // Fetch tags on mount
  useEffect(() => {
    fetchTags();
  }, [fetchTags]);

  // --- FETCH EVENTS ---
  const fetchEvents = useCallback(async () => {
    let start: Date, end: Date;

    if (view === 'month') {
      const year = value.getFullYear(),
        month = value.getMonth();
      const firstOfMonth = new Date(year, month, 1);
      const firstDow = (firstOfMonth.getDay() + 6) % 7; // Monday=0
      start = new Date(year, month, 1 - firstDow);
      end = new Date(start);
      end.setDate(start.getDate() + 42 - 1);
    } else if (view === 'week') {
      const dow = (value.getDay() + 6) % 7;
      start = new Date(value);
      start.setDate(value.getDate() - dow);
      start.setHours(0, 0, 0, 0);
      end = new Date(start);
      end.setDate(start.getDate() + 6);
      end.setHours(23, 59, 59, 999);
    } else {
      start = new Date(value);
      start.setHours(0, 0, 0, 0);
      end = new Date(value);
      end.setHours(23, 59, 59, 999);
    }

    try {
      const params: Record<string, string | number> = {
        start: start.toISOString(),
        end: end.toISOString(),
      };

      // Add tag filter if selected
      if (selectedTagFilter) {
        params.tagId = selectedTagFilter;
      }

      const res = await api.get<EventDTO[]>('/events', { params });
      const fetchedEvents = Array.isArray(res.data) ? res.data : [];
      setEvents(fetchedEvents);

      // Schedule reminders for the fetched events
      scheduleReminders(fetchedEvents);
    } catch (err) {
      console.error('Failed to fetch events', err);
      setEvents([]);
    }
  }, [view, value, scheduleReminders, selectedTagFilter]);

  // refetch on mount + whenever view/value changes
  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  // called after you successfully create an event
  const handleEventCreated = () => {
    setIsEventModalOpen(false);
    setEditEvent(null);
    fetchEvents();
  };

  // handle edit event
  const handleEditEvent = (event: EventDTO, scope?: 'instance' | 'series') => {
    setEditEvent({ ...event, editScope: scope });
    setIsEventModalOpen(true);
    setShowDayModal(false);
  };

  // toast helpers
  const showToast = (message: string, type: 'success' | 'error') => {
    setToast({ message, type, isVisible: true });
  };

  const hideToast = () => {
    setToast(prev => ({ ...prev, isVisible: false }));
  };

  // handle delete event (called from ConfirmationModal)
  const handleDeleteEvent = async (
    event: EventDTO,
    scope?: 'instance' | 'series'
  ) => {
    try {
      let eventIdToDelete = event.id;

      // For series deletion, use the master event ID
      if (scope === 'series' && event.parentEventId) {
        eventIdToDelete = event.parentEventId;
      }

      await deleteEvent(eventIdToDelete, scope || 'instance');
      setShowDayModal(false);
      fetchEvents();
      showToast('Event deleted.', 'success');
    } catch (error) {
      console.error('Failed to delete event:', error);
      showToast('Failed to delete event. Please try again.', 'error');
    }
  };

  // logout
  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // Ignore logout errors - we'll clear token anyway
    }
    localStorage.removeItem('token');
    navigate('/login');
  };

  // --- TAG MANAGEMENT ---
  const handleCreateTag = async () => {
    if (!newTagName.trim()) return;

    try {
      await api.post('/tags', { name: newTagName.trim() });
      setNewTagName('');
      setShowNewTagModal(false);
      fetchTags(); // Refresh tags list
      showToast('Tag created successfully', 'success');
    } catch (err) {
      console.error('Failed to create tag:', err);
      showToast('Failed to create tag', 'error');
    }
  };

  const handleDeleteTag = async (tagId: number) => {
    try {
      await api.delete(`/tags/${tagId}`);
      // If the deleted tag was selected for filtering, clear the filter
      if (selectedTagFilter === tagId) {
        setSelectedTagFilter(null);
      }
      fetchTags(); // Refresh tags list
      showToast('Tag deleted successfully', 'success');
    } catch (err) {
      console.error('Failed to delete tag:', err);
      showToast('Failed to delete tag', 'error');
    }
  };

  // --- HELPERS & LABELS ---
  const monthLabel = value.toLocaleString('default', {
    month: 'long',
    year: 'numeric',
  });
  const daysInMonth = new Date(
    value.getFullYear(),
    value.getMonth() + 1,
    0
  ).getDate();
  const firstDay = new Date(value.getFullYear(), value.getMonth(), 1).getDay();
  const offset = (firstDay + 6) % 7;
  const totalCells = 35;

  const eventsForDate = (d: Date) => {
    const filtered = (events || []).filter(e => {
      const eventDate = new Date(e.startDateTime);
      const match =
        eventDate.getFullYear() === d.getFullYear() &&
        eventDate.getMonth() === d.getMonth() &&
        eventDate.getDate() === d.getDate();
      return match;
    });
    return filtered;
  };

  const eventsForHour = (d: Date, hr: number) =>
    (events || []).filter(e => {
      const dt = new Date(e.startDateTime);
      return (
        dt.getFullYear() === d.getFullYear() &&
        dt.getMonth() === d.getMonth() &&
        dt.getDate() === d.getDate() &&
        dt.getHours() === hr
      );
    });

  // Helper function to open DayEventsModal
  const openDayModal = (date: Date) => {
    setSelectedDate(date);
    setShowDayModal(true);
  };

  // --- RENDER ---
  return (
    <div className="calendar-container">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="hamburger">☰</div>
          <h2>Calendar</h2>
        </div>
        <button className="btn-new" onClick={() => setIsEventModalOpen(true)}>
          + New event
        </button>
        <div className="mini-calendar-section">
          <div className="mini-calendar-header month-toggle">
            <ChevronUpIcon
              className="icon-small"
              onClick={() =>
                setValue(
                  new Date(
                    value.getFullYear(),
                    value.getMonth() - 1,
                    value.getDate()
                  )
                )
              }
            />
            <span>{monthLabel}</span>
            <ChevronDownIcon
              className="icon-small"
              onClick={() =>
                setValue(
                  new Date(
                    value.getFullYear(),
                    value.getMonth() + 1,
                    value.getDate()
                  )
                )
              }
            />
          </div>
          <Calendar
            onChange={d => d instanceof Date && setValue(d)}
            value={value}
            showNavigation={false}
          />
        </div>

        {/* Tags Section */}
        <div className="tags-section">
          <h3 className="tags-header">Tags</h3>
          <div className="tags-dropdown-container">
            <select
              value={selectedTagFilter || ''}
              onChange={e =>
                setSelectedTagFilter(
                  e.target.value ? Number(e.target.value) : null
                )
              }
              className="tags-dropdown"
            >
              <option value="">All events</option>
              {availableTags.map(tag => (
                <option key={tag.id} value={tag.id}>
                  {tag.name}
                </option>
              ))}
            </select>
          </div>

          {/* Tag List with Delete Buttons */}
          <div className="tags-list">
            {availableTags.map(tag => (
              <div key={tag.id} className="tag-item">
                <span className="tag-name">{tag.name}</span>
                <button
                  onClick={() => handleDeleteTag(tag.id)}
                  className="tag-delete-btn"
                  title="Delete tag"
                >
                  ×
                </button>
              </div>
            ))}
          </div>

          <button
            className="btn-new-tag"
            onClick={() => setShowNewTagModal(true)}
          >
            + New Tag
          </button>
        </div>

        <div className="sidebar-footer" ref={menuRef}>
          <button className="icon-btn" onClick={() => setMenuOpen(o => !o)}>
            <CogIcon />
          </button>
          {menuOpen && (
            <ul className="account-menu">
              <li>
                <button onClick={() => navigate('/account-settings')}>
                  Account Settings
                </button>
              </li>
              <li>
                <button onClick={handleLogout}>Logout</button>
              </li>
            </ul>
          )}
        </div>
      </aside>

      {/* Main View */}
      <main className="main-view">
        {/* toolbar */}
        <div className="toolbar">
          <div className="month-nav">
            <ChevronUpIcon
              className="icon-small"
              onClick={() =>
                setValue(
                  new Date(
                    value.getFullYear(),
                    value.getMonth() - 1,
                    value.getDate()
                  )
                )
              }
            />
            <span>{monthLabel}</span>
            <ChevronDownIcon
              className="icon-small"
              onClick={() =>
                setValue(
                  new Date(
                    value.getFullYear(),
                    value.getMonth() + 1,
                    value.getDate()
                  )
                )
              }
            />
          </div>
          <div className="view-buttons">
            {viewModes.map(m => (
              <button
                key={m}
                className={`btn-view ${view === m ? 'active' : ''}`}
                onClick={() => {
                  if (m === 'day') setValue(new Date());
                  setView(m);
                }}
              >
                <CalendarIcon className="btn-icon" />
                {m[0].toUpperCase() + m.slice(1)}
              </button>
            ))}
          </div>
        </div>

        {/* Month View */}
        {view === 'month' && (
          <div className="calendar-card">
            <div className="calendar-grid">
              {[
                'Monday',
                'Tuesday',
                'Wednesday',
                'Thursday',
                'Friday',
                'Saturday',
                'Sunday',
              ].map(d => (
                <div key={d} className="grid-header">
                  {d}
                </div>
              ))}
              {Array.from({ length: totalCells }).map((_, i) => {
                const dayNum = i - offset + 1;
                const inMonth = dayNum > 0 && dayNum <= daysInMonth;
                const cellDate = new Date(
                  value.getFullYear(),
                  value.getMonth(),
                  dayNum
                );
                const today = new Date();
                const isToday =
                  inMonth &&
                  dayNum === today.getDate() &&
                  value.getMonth() === today.getMonth() &&
                  value.getFullYear() === today.getFullYear();
                const evts = inMonth ? eventsForDate(cellDate) : [];

                return (
                  <div
                    key={i}
                    className={`grid-cell ${isToday ? 'today' : ''}`}
                    onClick={() => {
                      if (inMonth) {
                        openDayModal(cellDate);
                      }
                    }}
                  >
                    {inMonth && <span className="cell-daynum">{dayNum}</span>}
                    {evts.slice(0, 3).map(e => (
                      <div key={e.id} className="event-item">
                        {' '}
                        {e.title}
                      </div>
                    ))}
                    {evts.length > 3 && (
                      <div className="more-dots">
                        {[0, 1, 2].map((_, i) => (
                          <span key={i} />
                        ))}{' '}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Week View */}
        {view === 'week' && (
          <div className="calendar-week">
            <div className="calendar-week-header">
              <div className="calendar-week-timeslot"></div>
              {[
                'Monday',
                'Tuesday',
                'Wednesday',
                'Thursday',
                'Friday',
                'Saturday',
                'Sunday',
              ].map((d, idx) => {
                const todayDow = (new Date().getDay() + 6) % 7;
                return (
                  <div
                    key={d}
                    className={`calendar-week-day ${idx === todayDow ? 'today' : ''}`}
                  >
                    {d}
                  </div>
                );
              })}
            </div>
            <div className="calendar-week-body">
              {Array.from({ length: 24 }).map((_, hr) => {
                const nowHr = new Date().getHours();
                // compute the Monday of this week
                const dow = (value.getDay() + 6) % 7;
                const monday = new Date(value);
                monday.setDate(value.getDate() - dow);
                return (
                  <div
                    key={hr}
                    className={`calendar-week-row ${hr === nowHr ? 'today' : ''}`}
                  >
                    <div className="calendar-week-timeslot">{hr}:00</div>
                    {Array.from({ length: 7 }).map((__, col) => {
                      const cellDay = new Date(monday);
                      cellDay.setDate(monday.getDate() + col);
                      return (
                        <div
                          key={col}
                          className="calendar-week-cell"
                          onClick={() => openDayModal(cellDay)}
                        >
                          {eventsForHour(cellDay, hr).map(e => (
                            <div key={e.id} className="event-item-week">
                              {e.title}
                            </div>
                          ))}
                        </div>
                      );
                    })}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Day View */}
        {view === 'day' && (
          <div className="calendar-day">
            <div className="calendar-day-header">
              {value.toLocaleDateString('default', {
                weekday: 'long',
                month: 'long',
                day: 'numeric',
              })}
            </div>
            <div className="calendar-day-body">
              {Array.from({ length: 24 }).map((_, hr) => {
                const nowHr = new Date().getHours();
                return (
                  <div
                    key={hr}
                    className={`calendar-day-row ${hr === nowHr ? 'today' : ''}`}
                  >
                    <div className="calendar-day-timeslot">{hr}:00</div>
                    <div
                      className="calendar-day-cell"
                      onClick={() => openDayModal(value)}
                    >
                      {eventsForHour(value, hr).map(e => (
                        <div key={e.id} className="event-item-day">
                          {e.title}
                        </div>
                      ))}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </main>

      {/* Create Event */}
      <EventModal
        isOpen={isEventModalOpen}
        onClose={() => {
          setIsEventModalOpen(false);
          setEditEvent(null);
        }}
        onEventCreated={handleEventCreated}
        editEvent={editEvent}
        availableTags={availableTags}
      />

      {/* Day’s detail modal */}
      {showDayModal && selectedDate && (
        <DayEventsModal
          date={selectedDate}
          events={eventsForDate(selectedDate)}
          onClose={() => setShowDayModal(false)}
          onEditEvent={handleEditEvent}
          onDeleteEvent={handleDeleteEvent}
        />
      )}

      {/* Toast notifications */}
      <Toast
        message={toast.message}
        type={toast.type}
        isVisible={toast.isVisible}
        onClose={hideToast}
      />

      {/* New Tag Modal */}
      {showNewTagModal && (
        <div className="modal-backdrop">
          <div className="login-card max-w-sm mx-auto relative p-6">
            <button
              onClick={() => {
                setShowNewTagModal(false);
                setNewTagName('');
              }}
              className="absolute top-4 right-4 text-gray-300 hover:text-white"
            >
              ✕
            </button>
            <h3 className="text-xl font-semibold text-white text-center mb-4">
              Create New Tag
            </h3>
            <div className="space-y-4">
              <input
                type="text"
                value={newTagName}
                onChange={e => setNewTagName(e.target.value)}
                placeholder="Tag name"
                className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                onKeyPress={e => {
                  if (e.key === 'Enter') {
                    handleCreateTag();
                  }
                }}
              />
              <div className="flex space-x-3">
                <button
                  onClick={() => {
                    setShowNewTagModal(false);
                    setNewTagName('');
                  }}
                  className="flex-1 bg-gray-600 hover:bg-gray-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={handleCreateTag}
                  disabled={!newTagName.trim()}
                  className="flex-1 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white font-medium py-2 px-4 rounded-lg transition-colors"
                >
                  Create
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CalendarPage;
