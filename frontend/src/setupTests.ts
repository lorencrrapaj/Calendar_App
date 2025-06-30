import '@testing-library/jest-dom';

// Polyfill for TextEncoder/TextDecoder
import { TextEncoder, TextDecoder } from 'util';
global.TextEncoder = TextEncoder as typeof global.TextEncoder;
global.TextDecoder = TextDecoder as typeof global.TextDecoder;

// Polyfill for TransformStream (needed for MSW)
global.TransformStream = class TransformStream {
  readable: ReadableStream;
  writable: WritableStream;

  constructor() {
    this.readable = new ReadableStream();
    this.writable = new WritableStream();
  }
} as unknown as typeof TransformStream;

// Polyfill for fetch API (needed for MSW)
import 'whatwg-fetch';

// Mock BroadcastChannel for MSW
global.BroadcastChannel = class BroadcastChannel {
  name: string;
  onmessage: ((event: MessageEvent) => void) | null = null;
  onmessageerror: ((event: MessageEvent) => void) | null = null;

  constructor(name: string) {
    this.name = name;
  }

  postMessage() {
    // Mock implementation
  }

  close() {
    // Mock implementation
  }

  addEventListener() {
    // Mock implementation
  }

  removeEventListener() {
    // Mock implementation
  }

  dispatchEvent() {
    return true;
  }
} as unknown as typeof BroadcastChannel;

// Mock import.meta for Vite - more robust approach
Object.defineProperty(global, 'import', {
  value: {
    meta: {
      env: {
        VITE_API_URL: undefined,
      },
    },
  },
  writable: true,
  configurable: true,
});

// Also define it on globalThis for better compatibility
if (typeof globalThis !== 'undefined') {
  Object.defineProperty(globalThis, 'import', {
    value: {
      meta: {
        env: {
          VITE_API_URL: undefined,
        },
      },
    },
    writable: true,
    configurable: true,
  });
}

// Mock IntersectionObserver
global.IntersectionObserver = class IntersectionObserver {
  root = null;
  rootMargin = '';
  thresholds = [];

  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
  takeRecords() {
    return [];
  }
} as typeof IntersectionObserver;

// Mock ResizeObserver
global.ResizeObserver = class ResizeObserver {
  constructor() {}
  disconnect() {}
  observe() {}
  unobserve() {}
};

// Mock matchMedia
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: jest.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: jest.fn(), // deprecated
    removeListener: jest.fn(), // deprecated
    addEventListener: jest.fn(),
    removeEventListener: jest.fn(),
    dispatchEvent: jest.fn(),
  })),
});

// Mock scrollTo
Object.defineProperty(window, 'scrollTo', {
  writable: true,
  value: jest.fn(),
});

// Mock sessionStorage
const sessionStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};
Object.defineProperty(window, 'sessionStorage', {
  value: sessionStorageMock,
});
