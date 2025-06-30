import {
  createEvent,
  getEvents,
  updateEvent,
  deleteEvent,
} from '../services/events';
import api from '../services/api';

// Mock the api module
jest.mock('../services/api');
const mockApi = api as jest.Mocked<typeof api>;

describe('events service', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createEvent', () => {
    it('should create an event and return the response data', async () => {
      const mockEventData = {
        title: 'Test Event',
        description: 'Test Description',
        startDateTime: '2024-01-15T10:00',
        endDateTime: '2024-01-15T12:00',
      };

      const mockResponse = {
        data: {
          id: 1,
          title: 'Test Event',
          description: 'Test Description',
          startDateTime: '2024-01-15T10:00:00',
          endDateTime: '2024-01-15T12:00:00',
          user: {
            id: 1,
            email: 'test@example.com',
          },
        },
      };

      mockApi.post.mockResolvedValueOnce(mockResponse);

      const result = await createEvent(mockEventData);

      expect(mockApi.post).toHaveBeenCalledWith('/events', mockEventData);
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('getEvents', () => {
    it('should fetch events and return the response data', async () => {
      const mockEvents = [
        {
          id: 1,
          title: 'Event 1',
          description: 'Description 1',
          startDateTime: '2024-01-15T10:00:00',
          endDateTime: '2024-01-15T12:00:00',
          user: {
            id: 1,
            email: 'test@example.com',
          },
        },
        {
          id: 2,
          title: 'Event 2',
          description: 'Description 2',
          startDateTime: '2024-01-16T14:00:00',
          endDateTime: '2024-01-16T16:00:00',
          user: {
            id: 1,
            email: 'test@example.com',
          },
        },
      ];

      const mockResponse = {
        data: mockEvents,
      };

      mockApi.get.mockResolvedValueOnce(mockResponse);

      const result = await getEvents();

      expect(mockApi.get).toHaveBeenCalledWith('/events');
      expect(result).toEqual(mockEvents);
    });
  });

  describe('updateEvent', () => {
    it('should update an event and return the response data', async () => {
      const eventId = 1;
      const mockEventData = {
        title: 'Updated Event',
        description: 'Updated Description',
        startDateTime: '2024-01-15T14:00',
        endDateTime: '2024-01-15T16:00',
      };

      const mockResponse = {
        data: {
          id: eventId,
          title: 'Updated Event',
          description: 'Updated Description',
          startDateTime: '2024-01-15T14:00:00',
          endDateTime: '2024-01-15T16:00:00',
          user: {
            id: 1,
            email: 'test@example.com',
          },
        },
      };

      mockApi.put.mockResolvedValueOnce(mockResponse);

      const result = await updateEvent(eventId, mockEventData);

      expect(mockApi.put).toHaveBeenCalledWith(
        `/events/${eventId}`,
        mockEventData,
        { params: { scope: 'instance' } }
      );
      expect(result).toEqual(mockResponse.data);
    });
  });

  describe('deleteEvent', () => {
    it('should delete an event', async () => {
      const eventId = 1;

      mockApi.delete.mockResolvedValueOnce({});

      await deleteEvent(eventId);

      expect(mockApi.delete).toHaveBeenCalledWith(`/events/${eventId}`, {
        params: { scope: 'instance' },
      });
    });
  });
});
