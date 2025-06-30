import React, { useState, useEffect } from 'react';
import { createEvent, updateEvent } from '../services/events';
import { EventFormData, CreateEventRequest } from '../types/event';
import { EventDTO, TagDTO } from '../pages/CalendarPage';
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
} from '@heroicons/react/24/outline';

interface EventModalProps {
  isOpen: boolean;
  onClose: () => void;
  onEventCreated: () => void;
  editEvent?: EventDTO | null;
  availableTags: TagDTO[];
}

export default function EventModal({
  isOpen,
  onClose,
  onEventCreated,
  editEvent,
  availableTags,
}: EventModalProps) {
  const [formData, setFormData] = useState<EventFormData>({
    title: '',
    description: '',
    startDateTime: '',
    endDateTime: '',
    repeatType: 'none',
    repeatEndType: 'never',
    repeatEndDate: '',
    repeatCount: 10,
  });
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  const [tagMenuOpen, setTagMenuOpen] = useState(false);
  const [errors, setErrors] = useState<
    Partial<Record<keyof EventFormData, string>>
  >({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string>('');
  const [success, setSuccess] = useState(false);

  const isEditMode = !!editEvent;

  // Helper function to parse RRULE into form fields
  const parseRecurrenceRule = (
    rrule: string | undefined,
    recurrenceEndDate?: string,
    recurrenceCount?: number
  ): Partial<EventFormData> => {
    if (!rrule) {
      return {
        repeatType: 'none',
        repeatEndType: 'never',
        repeatEndDate: '',
        repeatCount: 10,
      };
    }

    let repeatType: 'none' | 'daily' | 'weekly' | 'monthly' = 'none';
    if (rrule.includes('FREQ=DAILY')) repeatType = 'daily';
    else if (rrule.includes('FREQ=WEEKLY')) repeatType = 'weekly';
    else if (rrule.includes('FREQ=MONTHLY')) repeatType = 'monthly';

    // Determine repeat end type based on available data
    let repeatEndType: 'never' | 'date' | 'count' = 'never';
    let repeatEndDate = '';
    let repeatCount = 10;

    if (recurrenceEndDate) {
      repeatEndType = 'date';
      repeatEndDate = recurrenceEndDate.split('T')[0]; // Convert to date format
    } else if (recurrenceCount && recurrenceCount > 0) {
      repeatEndType = 'count';
      repeatCount = recurrenceCount;
    }

    return {
      repeatType,
      repeatEndType,
      repeatEndDate,
      repeatCount,
    };
  };

  // Helper function to convert form fields to RRULE format
  const buildRecurrenceRule = (): string | undefined => {
    if (formData.repeatType === 'none') return undefined;

    let rrule = '';
    switch (formData.repeatType) {
      case 'daily':
        rrule = 'FREQ=DAILY';
        break;
      case 'weekly':
        rrule = 'FREQ=WEEKLY';
        break;
      case 'monthly':
        rrule = 'FREQ=MONTHLY';
        break;
      default:
        return undefined;
    }

    return rrule;
  };

  // Populate form when editing
  useEffect(() => {
    if (editEvent) {
      const recurrenceFields = parseRecurrenceRule(
        editEvent.recurrenceRule,
        editEvent.recurrenceEndDate,
        editEvent.recurrenceCount
      );
      const newFormData = {
        title: editEvent.title,
        description: editEvent.description,
        startDateTime: editEvent.startDateTime,
        endDateTime: editEvent.endDateTime,
        repeatType: recurrenceFields.repeatType || 'none',
        repeatEndType: recurrenceFields.repeatEndType || 'never',
        repeatEndDate: recurrenceFields.repeatEndDate || '',
        repeatCount: recurrenceFields.repeatCount || 10,
      };
      setFormData(newFormData);
      // Initialize selected tags
      setSelectedTagIds(
        editEvent.tags ? editEvent.tags.map(tag => tag.id) : []
      );
    } else {
      setFormData({
        title: '',
        description: '',
        startDateTime: '',
        endDateTime: '',
        repeatType: 'none',
        repeatEndType: 'never',
        repeatEndDate: '',
        repeatCount: 10,
      });
      // Clear selected tags
      setSelectedTagIds([]);
    }
    setErrors({});
    setSubmitError('');
    setSuccess(false);
  }, [editEvent]);

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof EventFormData, string>> = {};
    if (!formData.title.trim()) newErrors.title = 'Title is required';
    if (!formData.description.trim())
      newErrors.description = 'Description is required';
    if (!formData.startDateTime)
      newErrors.startDateTime = 'Start date and time is required';
    if (!formData.endDateTime)
      newErrors.endDateTime = 'End date and time is required';

    if (formData.startDateTime && formData.endDateTime) {
      const start = new Date(formData.startDateTime);
      const end = new Date(formData.endDateTime);
      if (end <= start) newErrors.endDateTime = 'End must be after start';
    }

    // Validate recurrence fields
    if (formData.repeatType !== 'none') {
      if (formData.repeatEndType === 'date' && !formData.repeatEndDate) {
        newErrors.repeatEndDate =
          'End date is required when repeat end type is date';
      }
      if (
        formData.repeatEndType === 'date' &&
        formData.repeatEndDate &&
        formData.startDateTime
      ) {
        const start = new Date(formData.startDateTime);
        const endDate = new Date(formData.repeatEndDate);
        if (endDate <= start) {
          newErrors.repeatEndDate = 'Repeat end date must be after start date';
        }
      }
      if (
        formData.repeatEndType === 'count' &&
        (!formData.repeatCount || formData.repeatCount <= 0)
      ) {
        newErrors.repeatCount = 'Repeat count must be a positive number';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    setIsSubmitting(true);
    setSubmitError('');
    try {
      // Convert form data to API format
      const apiData: CreateEventRequest = {
        title: formData.title,
        description: formData.description,
        startDateTime: formData.startDateTime,
        endDateTime: formData.endDateTime,
        recurrenceRule: buildRecurrenceRule(),
        recurrenceEndDate:
          formData.repeatEndType === 'date' && formData.repeatEndDate
            ? `${formData.repeatEndDate}T23:59:59`
            : undefined,
        recurrenceCount:
          formData.repeatEndType === 'count' ? formData.repeatCount : undefined,
        tagIds: selectedTagIds.length > 0 ? selectedTagIds : undefined,
      };

      if (isEditMode && editEvent) {
        const scope = editEvent.editScope || 'instance';
        await updateEvent(editEvent.id, apiData, scope);
      } else {
        await createEvent(apiData);
      }
      setSuccess(true);
      // keep the banner visible briefly, then reset and close
      setTimeout(() => {
        setSuccess(false);
        setFormData({
          title: '',
          description: '',
          startDateTime: '',
          endDateTime: '',
          repeatType: 'none',
          repeatEndType: 'never',
          repeatEndDate: '',
          repeatCount: 10,
        });
        setErrors({});
        onClose();
        onEventCreated();
      }, 1500);
    } catch (err: unknown) {
      console.error(err);
      const errorMessage =
        err &&
        typeof err === 'object' &&
        'response' in err &&
        err.response &&
        typeof err.response === 'object' &&
        'data' in err.response
          ? String(err.response.data)
          : 'Error ' + (isEditMode ? 'updating' : 'creating') + ' event';
      setSubmitError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInputChange = (
    field: keyof EventFormData,
    value: string | number
  ) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) setErrors(prev => ({ ...prev, [field]: undefined }));
  };

  if (!isOpen) return null;

  return (
    <div className="modal-backdrop">
      <div className="modal-card">
        {/* success / error banners */}
        {success && (
          <div className="modal-success-banner">
            <CheckCircleIcon className="banner-icon" />
            <span>Event {isEditMode ? 'updated' : 'created'}!</span>
          </div>
        )}
        {submitError && (
          <div className="modal-error-banner">
            <ExclamationTriangleIcon className="banner-icon" />
            <span>{submitError}</span>
          </div>
        )}

        <div className="modal-header">
          <h2 className="modal-title">
            {isEditMode ? 'Edit Event' : 'Create Event'}
          </h2>
          <button
            onClick={onClose}
            disabled={isSubmitting}
            className="modal-close"
          >
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          {/* Title */}
          <div className="modal-field">
            <label htmlFor="title" className="modal-label">
              Title *
            </label>
            <input
              id="title"
              type="text"
              value={formData.title}
              onChange={e => handleInputChange('title', e.target.value)}
              disabled={isSubmitting}
              className={`modal-input ${errors.title ? 'border-red' : ''}`}
            />
            {errors.title && <p className="field-error">{errors.title}</p>}
          </div>

          {/* Description */}
          <div className="modal-field">
            <label htmlFor="description" className="modal-label">
              Description *
            </label>
            <textarea
              id="description"
              rows={3}
              value={formData.description}
              onChange={e => handleInputChange('description', e.target.value)}
              disabled={isSubmitting}
              className={`modal-textarea ${errors.description ? 'border-red' : ''}`}
            />
            {errors.description && (
              <p className="field-error">{errors.description}</p>
            )}
          </div>

          {/* Start */}
          <div className="modal-field">
            <label htmlFor="startDateTime" className="modal-label">
              Start Date/Time *
            </label>
            <input
              id="startDateTime"
              type="datetime-local"
              value={formData.startDateTime}
              onChange={e => handleInputChange('startDateTime', e.target.value)}
              disabled={isSubmitting}
              className={`modal-input ${errors.startDateTime ? 'border-red' : ''}`}
            />
            {errors.startDateTime && (
              <p className="field-error">{errors.startDateTime}</p>
            )}
          </div>

          {/* End */}
          <div className="modal-field">
            <label htmlFor="endDateTime" className="modal-label">
              End Date/Time *
            </label>
            <input
              id="endDateTime"
              type="datetime-local"
              value={formData.endDateTime}
              onChange={e => handleInputChange('endDateTime', e.target.value)}
              disabled={isSubmitting}
              className={`modal-input ${errors.endDateTime ? 'border-red' : ''}`}
            />
            {errors.endDateTime && (
              <p className="field-error">{errors.endDateTime}</p>
            )}
          </div>

          {/* Repeat */}
          <div className="modal-field">
            <label htmlFor="repeatType" className="modal-label">
              Repeat
            </label>
            <select
              id="repeatType"
              value={formData.repeatType}
              onChange={e => handleInputChange('repeatType', e.target.value)}
              disabled={isSubmitting}
              className="modal-input"
            >
              <option value="none">None</option>
              <option value="daily">Daily</option>
              <option value="weekly">Weekly</option>
              <option value="monthly">Monthly</option>
            </select>
          </div>

          {/* Repeat End Options - only show if repeat is not 'none' */}
          {formData.repeatType !== 'none' && (
            <>
              <div className="modal-field">
                <label htmlFor="repeatEndType" className="modal-label">
                  End Repeat
                </label>
                <select
                  id="repeatEndType"
                  value={formData.repeatEndType}
                  onChange={e =>
                    handleInputChange('repeatEndType', e.target.value)
                  }
                  disabled={isSubmitting}
                  className="modal-input"
                >
                  <option value="never">Never</option>
                  <option value="date">On Date</option>
                  <option value="count">After X Times</option>
                </select>
              </div>

              {/* Repeat End Date - only show if repeatEndType is 'date' */}
              {formData.repeatEndType === 'date' && (
                <div className="modal-field">
                  <label htmlFor="repeatEndDate" className="modal-label">
                    End Date *
                  </label>
                  <input
                    id="repeatEndDate"
                    type="date"
                    value={formData.repeatEndDate}
                    onChange={e =>
                      handleInputChange('repeatEndDate', e.target.value)
                    }
                    disabled={isSubmitting}
                    className={`modal-input ${errors.repeatEndDate ? 'border-red' : ''}`}
                  />
                  {errors.repeatEndDate && (
                    <p className="field-error">{errors.repeatEndDate}</p>
                  )}
                </div>
              )}

              {/* Repeat Count - only show if repeatEndType is 'count' */}
              {formData.repeatEndType === 'count' && (
                <div className="modal-field">
                  <label htmlFor="repeatCount" className="modal-label">
                    Number of Times *
                  </label>
                  <input
                    id="repeatCount"
                    type="text"
                    value={formData.repeatCount}
                    onChange={e => {
                      const inputValue = e.target.value;
                      const numValue = Number(inputValue);
                      const value = isNaN(numValue) ? 0 : numValue;
                      handleInputChange('repeatCount', value);
                    }}
                    disabled={isSubmitting}
                    className={`modal-input ${errors.repeatCount ? 'border-red' : ''}`}
                  />
                  {errors.repeatCount && (
                    <p className="field-error">{errors.repeatCount}</p>
                  )}
                </div>
              )}
            </>
          )}

          {/* Tags */}
          <div className="modal-field tag-dropdown-container">
            <label htmlFor="tag-dropdown" className="modal-label">
              Tags
            </label>
            <button
              id="tag-dropdown"
              data-testid="tag-dropdown"
              type="button"
              className="tag-dropdown-button"
              onClick={() => setTagMenuOpen(o => !o)}
            >
              {selectedTagIds.length === 0
                ? 'Select tags…'
                : availableTags
                    .filter(t => selectedTagIds.includes(t.id))
                    .map(t => t.name)
                    .join(', ')}
              <span className="tag-dropdown-arrow">▾</span>
            </button>
            {tagMenuOpen && (
              <ul className="tag-dropdown-menu">
                {availableTags.map(tag => (
                  <li key={tag.id}>
                    <label className="tag-dropdown-item">
                      <input
                        type="checkbox"
                        checked={selectedTagIds.includes(tag.id)}
                        onChange={() => {
                          const next = selectedTagIds.includes(tag.id)
                            ? selectedTagIds.filter(id => id !== tag.id)
                            : [...selectedTagIds, tag.id];
                          setSelectedTagIds(next);
                        }}
                      />
                      <span>{tag.name}</span>
                    </label>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {/* Actions */}
          <div className="modal-actions">
            <button
              type="button"
              onClick={onClose}
              className="btn-cancel"
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? isEditMode
                  ? 'Updating…'
                  : 'Creating…'
                : isEditMode
                  ? 'Update Event'
                  : 'Create Event'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
