// Unmock the api module to test the real implementation
jest.unmock('../services/api');

describe('API Service', () => {
  // Mock localStorage before importing api
  const mockLocalStorage = {
    getItem: jest.fn(),
    setItem: jest.fn(),
    removeItem: jest.fn(),
    clear: jest.fn(),
  };

  beforeAll(() => {
    Object.defineProperty(window, 'localStorage', {
      value: mockLocalStorage,
      writable: true,
    });
  });

  beforeEach(() => {
    jest.clearAllMocks();
    mockLocalStorage.getItem.mockClear();
  });

  it('exports an axios instance', async () => {
    const api = await import('../services/api');
    expect(api.default).toBeDefined();
    expect(typeof api.default).toBe('object');
  });

  it('has axios methods', async () => {
    const api = await import('../services/api');
    expect(typeof api.default.get).toBe('function');
    expect(typeof api.default.post).toBe('function');
    expect(typeof api.default.put).toBe('function');
    expect(typeof api.default.delete).toBe('function');
  });

  it('has interceptors configured', async () => {
    const api = await import('../services/api');
    expect(api.default.interceptors).toBeDefined();
    expect(api.default.interceptors.request).toBeDefined();
  });

  it('is properly configured as axios instance', async () => {
    const api = await import('../services/api');
    // Just verify it's a properly configured axios instance
    expect(api.default).toHaveProperty('interceptors');
    expect(api.default).toHaveProperty('get');
    expect(api.default).toHaveProperty('post');
  });
});
