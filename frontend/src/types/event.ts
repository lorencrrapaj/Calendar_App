export interface Event {
  id: number;
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  user: {
    id: number;
    email: string;
  };
  // Recurrence fields
  recurrenceRule?: string;
  recurrenceEndDate?: string;
  recurrenceCount?: number;
  parentEventId?: number;
  originalStartDateTime?: string;
}

export interface CreateEventRequest {
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  // Recurrence fields
  recurrenceRule?: string;
  recurrenceEndDate?: string;
  recurrenceCount?: number;
  // Tag IDs to associate with this event
  tagIds?: number[];
}

export interface EventFormData {
  title: string;
  description: string;
  startDateTime: string;
  endDateTime: string;
  // Recurrence fields
  repeatType: 'none' | 'daily' | 'weekly' | 'monthly';
  repeatEndType: 'never' | 'date' | 'count';
  repeatEndDate: string;
  repeatCount: number;
}
