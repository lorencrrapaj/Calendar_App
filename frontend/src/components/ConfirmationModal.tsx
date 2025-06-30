// src/components/ConfirmationModal.tsx
import React, { useState, useEffect } from 'react';
import { ExclamationTriangleIcon } from '@heroicons/react/24/outline';

interface ConfirmationModalProps {
  isOpen: boolean;
  title: string;
  message: string;
  onConfirm: (scope?: 'instance' | 'series') => void;
  onCancel: () => void;
  confirmText?: string;
  cancelText?: string;
  isDestructive?: boolean;
  isLoading?: boolean;
  isRecurringEvent?: boolean;
}

export default function ConfirmationModal({
  isOpen,
  title,
  message,
  onConfirm,
  onCancel,
  confirmText = 'Delete',
  cancelText = 'Cancel',
  isDestructive = false,
  isLoading = false,
  isRecurringEvent = false,
}: ConfirmationModalProps) {
  const [deleteScope, setDeleteScope] = useState<'instance' | 'series'>(
    'instance'
  );
  useEffect(() => {
    if (isOpen) {
      setDeleteScope('instance');
    }
  }, [isOpen]);
  if (!isOpen) return null;

  const handleConfirm = () => {
    if (isRecurringEvent) {
      onConfirm(deleteScope);
    } else {
      onConfirm();
    }
  };

  return (
    <div className="modal-backdrop">
      <div className="modal-card confirm confirm-small">
        <header className="modal-header">
          <h2 className="modal-title">{title}</h2>
          <button
            onClick={onCancel}
            disabled={isLoading}
            className="modal-close"
            aria-label="Close"
          >
            ✕
          </button>
        </header>

        <div className="confirmation-content">
          {isDestructive && (
            <ExclamationTriangleIcon className="confirmation-icon" />
          )}
          <p className="confirmation-message">{message}</p>

          {isRecurringEvent && (
            <div className="scope-selection">
              <p className="scope-label">
                This is a recurring event. What would you like to delete?
              </p>
              <div className="radio-group">
                <label className="radio-option">
                  <input
                    type="radio"
                    name="deleteScope"
                    value="instance"
                    checked={deleteScope === 'instance'}
                    onChange={e =>
                      setDeleteScope(e.target.value as 'instance' | 'series')
                    }
                    disabled={isLoading}
                  />
                  <span>Delete this occurrence only</span>
                </label>
                <label className="radio-option">
                  <input
                    type="radio"
                    name="deleteScope"
                    value="series"
                    checked={deleteScope === 'series'}
                    onChange={e =>
                      setDeleteScope(e.target.value as 'instance' | 'series')
                    }
                    disabled={isLoading}
                  />
                  <span>Delete entire series</span>
                </label>
              </div>
            </div>
          )}
        </div>

        <footer className="modal-actions">
          <button
            type="button"
            onClick={onCancel}
            className="btn-cancel"
            disabled={isLoading}
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            className={`btn-delete ${isLoading ? 'loading' : ''}`}
            disabled={isLoading}
          >
            {isLoading ? 'Processing…' : confirmText}
          </button>
        </footer>
      </div>
    </div>
  );
}
