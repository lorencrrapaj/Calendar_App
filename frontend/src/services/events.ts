import api from './api';
import { Event, CreateEventRequest } from '../types/event';

export async function createEvent(
  eventData: CreateEventRequest
): Promise<Event> {
  const response = await api.post('/events', eventData);
  return response.data;
}

export async function getEvents(): Promise<Event[]> {
  const response = await api.get('/events');
  return response.data;
}

export async function getEventsInRange(
  start: string,
  end: string
): Promise<Event[]> {
  const response = await api.get('/events', {
    params: { start, end },
  });
  return response.data;
}

export async function updateEvent(
  id: number,
  eventData: CreateEventRequest,
  scope: 'instance' | 'series' = 'instance'
): Promise<Event> {
  const response = await api.put(`/events/${id}`, eventData, {
    params: { scope },
  });
  return response.data;
}

export async function deleteEvent(
  id: number,
  scope: 'instance' | 'series' = 'instance'
): Promise<void> {
  await api.delete(`/events/${id}`, {
    params: { scope },
  });
}
