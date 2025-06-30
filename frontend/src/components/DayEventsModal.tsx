// src/components/DayEventsModal.tsx
import React, { useState } from 'react';
import { EventDTO, TagDTO } from '../pages/CalendarPage';
import ConfirmationModal from './ConfirmationModal';

interface DayEventsModalProps {
  date: Date;
  events: EventDTO[];
  onClose: () => void;
  onEditEvent: (event: EventDTO, scope?: 'instance' | 'series') => void;
  onDeleteEvent: (event: EventDTO, scope?: 'instance' | 'series') => void;
}

export default function DayEventsModal({
  date,
  events,
  onClose,
  onEditEvent,
  onDeleteEvent,
}: DayEventsModalProps) {
  const [eventToDelete, setEventToDelete] = useState<EventDTO | null>(null);
  const [showEditOptions, setShowEditOptions] = useState<EventDTO | null>(null);

  const isRecurringEvent = (event: EventDTO): boolean => {
    return !!(event.recurrenceRule && event.recurrenceRule.trim() !== '');
  };

  const handleEditClick = (event: EventDTO) => {
    if (isRecurringEvent(event)) {
      setShowEditOptions(event);
    } else {
      onEditEvent(event);
    }
  };

  const handleEditSingle = () => {
    if (showEditOptions) {
      onEditEvent(showEditOptions, 'instance');
      setShowEditOptions(null);
    }
  };

  const handleEditSeries = () => {
    if (showEditOptions) {
      onEditEvent(showEditOptions, 'series');
      setShowEditOptions(null);
    }
  };

  const handleDeleteClick = (event: EventDTO) => {
    setEventToDelete(event);
  };

  const handleConfirmDelete = (scope?: 'instance' | 'series') => {
    if (eventToDelete) {
      onDeleteEvent(eventToDelete, scope || 'instance');
      setEventToDelete(null);
    }
  };

  const handleCancelDelete = () => {
    setEventToDelete(null);
  };

  return (
    <div className="modal-backdrop">
      <div
        className="login-card max-w-md mx-auto relative p-6 overflow-auto"
        role="dialog"
      >
        {/* close button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-300 hover:text-white"
        >
          ✕
        </button>

        {/* header */}
        <h2 className="text-2xl font-semibold text-white text-center mb-4">
          {date.toLocaleDateString('default', {
            weekday: 'long',
            month: 'long',
            day: 'numeric',
          })}
        </h2>

        {/* no-events fallback */}
        {events.length === 0 && (
          <p className="text-gray-300 text-center">No events on this day.</p>
        )}

        {/* list */}
        <ul className="space-y-4">
          {events.map(e => (
            <li
              key={e.id}
              className="bg-white/20 backdrop-filter backdrop-blur-sm rounded-lg p-4"
            >
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <h3 className="text-lg font-medium text-white">
                    Title: {e.title}
                  </h3>
                  <p className="text-gray-200 text-sm mb-2">{e.description}</p>

                  {/* Tags */}
                  {e.tags && e.tags.length > 0 && (
                    <div className="flex flex-wrap gap-1 mb-2">
                      {e.tags.map((tag: TagDTO) => (
                        <span
                          key={tag.id}
                          className="inline-block bg-blue-500/20 text-blue-200 text-xs px-2 py-1 "
                        >
                          Tag: {tag.name}
                        </span>
                      ))}
                    </div>
                  )}

                  <p className="text-gray-400 text-xs">
                    {new Date(e.startDateTime).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}{' '}
                    –{' '}
                    {new Date(e.endDateTime).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>
                <div className="flex space-x-2 ml-4">
                  <button
                    onClick={() => handleEditClick(e)}
                    className="edit-button"
                    title="Edit event"
                  >
                    ✎
                  </button>
                  <button
                    onClick={() => handleDeleteClick(e)}
                    className="delete-button"
                    title="Delete event"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>
      </div>

      {/* Edit Options Modal for Recurring Events */}
      {showEditOptions && (
        <div className="modal-backdrop">
          <div className="login-card max-w-sm mx-auto relative p-6">
            <button
              onClick={() => setShowEditOptions(null)}
              className="absolute top-4 right-4 text-gray-300 hover:text-white"
            >
              ✕
            </button>
            <h3 className="text-xl font-semibold text-white text-center mb-4">
              Edit Recurring Event
            </h3>
            <p className="text-gray-300 text-center mb-6">
              This is a recurring event. What would you like to edit?
            </p>
            <div className="space-y-3">
              <button
                onClick={handleEditSingle}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
              >
                Edit this occurrence only
              </button>
              <button
                onClick={handleEditSeries}
                className="w-full bg-purple-600 hover:bg-purple-700 text-white font-medium py-2 px-4 rounded-lg transition-colors"
              >
                Edit entire series
              </button>
            </div>
          </div>
        </div>
      )}

      <ConfirmationModal
        isOpen={!!eventToDelete}
        title="Are you sure?"
        message={`Are you sure you want to delete '${eventToDelete?.title}'? This cannot be undone.`}
        onConfirm={handleConfirmDelete}
        onCancel={handleCancelDelete}
        confirmText="Delete"
        cancelText="Cancel"
        isDestructive={true}
        isRecurringEvent={
          eventToDelete ? isRecurringEvent(eventToDelete) : false
        }
      />
    </div>
  );
}
