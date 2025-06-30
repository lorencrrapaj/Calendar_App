import React, { useState } from 'react';
import { render, screen, act, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthProvider, useAuth } from '../hooks/useAuth';

// Mock localStorage
const mockLocalStorage = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

// Store original localStorage
const originalLocalStorage = window.localStorage;

// Mock localStorage before tests
Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
  writable: true,
  configurable: true,
});

// Test component that uses the useAuth hook
const TestComponent = () => {
  const { user, login, logout } = useAuth();

  return (
    <div>
      <div data-testid="user-info">
        {user ? `User: ${user.name} (ID: ${user.id})` : 'No user'}
      </div>
      <button
        data-testid="login-button"
        onClick={() => login('testuser', 'password')}
      >
        Login
      </button>
      <button data-testid="logout-button" onClick={logout}>
        Logout
      </button>
    </div>
  );
};

describe('useAuth', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLocalStorage.getItem.mockClear();
    mockLocalStorage.setItem.mockClear();
    mockLocalStorage.removeItem.mockClear();
  });

  afterAll(() => {
    // Restore original localStorage
    Object.defineProperty(window, 'localStorage', {
      value: originalLocalStorage,
      writable: true,
    });
  });

  it('provides default context values when used outside provider', async () => {
    const user = userEvent.setup();

    render(<TestComponent />);

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');

    // Test that default login function doesn't throw
    await act(async () => {
      await user.click(screen.getByTestId('login-button'));
    });

    // Should still show no user since default login does nothing
    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');

    // Test that default logout function doesn't throw
    await act(async () => {
      await user.click(screen.getByTestId('logout-button'));
    });

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
  });

  it('initializes with no user when localStorage is empty', async () => {
    mockLocalStorage.getItem.mockReturnValue(null);

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    });
    expect(mockLocalStorage.getItem).toHaveBeenCalledWith('user');
  });

  it('initializes with user from localStorage', async () => {
    const storedUser = { id: '456', name: 'stored-user' };
    mockLocalStorage.getItem.mockReturnValue(JSON.stringify(storedUser));

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent(
        'User: stored-user (ID: 456)'
      );
    });
    expect(mockLocalStorage.getItem).toHaveBeenCalledWith('user');
  });

  it('handles invalid JSON in localStorage gracefully', async () => {
    mockLocalStorage.getItem.mockReturnValue('invalid-json');

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    });
    expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('user');
  });

  it('logs in user successfully', async () => {
    const user = userEvent.setup();
    mockLocalStorage.getItem.mockReturnValue(null);

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    });

    await act(async () => {
      await user.click(screen.getByTestId('login-button'));
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent(
        'User: testuser (ID: 123)'
      );
    });

    expect(mockLocalStorage.setItem).toHaveBeenCalledWith(
      'user',
      JSON.stringify({ id: '123', name: 'testuser' })
    );
  });

  it('logs out user successfully', async () => {
    const user = userEvent.setup();
    const storedUser = { id: '456', name: 'stored-user' };
    mockLocalStorage.getItem.mockReturnValue(JSON.stringify(storedUser));

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent(
        'User: stored-user (ID: 456)'
      );
    });

    await act(async () => {
      await user.click(screen.getByTestId('logout-button'));
    });

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('user');
  });

  it('login function is async', async () => {
    mockLocalStorage.getItem.mockReturnValue(null);

    const TestAsyncComponent = () => {
      const { login } = useAuth();
      const [isLoading, setIsLoading] = useState(false);

      const handleLogin = async () => {
        setIsLoading(true);
        await login('asyncuser', 'password');
        setIsLoading(false);
      };

      return (
        <div>
          <div data-testid="loading-state">
            {isLoading ? 'Loading' : 'Not Loading'}
          </div>
          <button data-testid="async-login-button" onClick={handleLogin}>
            Async Login
          </button>
        </div>
      );
    };

    const user = userEvent.setup();
    await act(async () => {
      render(
        <AuthProvider>
          <TestAsyncComponent />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('loading-state')).toHaveTextContent(
      'Not Loading'
    );

    await act(async () => {
      await user.click(screen.getByTestId('async-login-button'));
    });

    expect(screen.getByTestId('loading-state')).toHaveTextContent(
      'Not Loading'
    );
  });

  it('maintains user state across multiple components', async () => {
    const storedUser = { id: '789', name: 'shared-user' };
    mockLocalStorage.getItem.mockReturnValue(JSON.stringify(storedUser));

    const SecondTestComponent = () => {
      const { user } = useAuth();
      return (
        <div data-testid="second-user-info">{user?.name || 'No user'}</div>
      );
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
          <SecondTestComponent />
        </AuthProvider>
      );
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent(
        'User: shared-user (ID: 789)'
      );
    });
    expect(screen.getByTestId('second-user-info')).toHaveTextContent(
      'shared-user'
    );
  });

  it('updates all components when user state changes', async () => {
    const user = userEvent.setup();
    mockLocalStorage.getItem.mockReturnValue(null);

    const SecondTestComponent = () => {
      const { user } = useAuth();
      return (
        <div data-testid="second-user-info">{user?.name || 'No user'}</div>
      );
    };

    await act(async () => {
      render(
        <AuthProvider>
          <TestComponent />
          <SecondTestComponent />
        </AuthProvider>
      );
    });

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
    expect(screen.getByTestId('second-user-info')).toHaveTextContent('No user');

    await act(async () => {
      await user.click(screen.getByTestId('login-button'));
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-info')).toHaveTextContent(
        'User: testuser (ID: 123)'
      );
      expect(screen.getByTestId('second-user-info')).toHaveTextContent(
        'testuser'
      );
    });
  });

  it('provides correct context type structure', () => {
    const ContextTestComponent = () => {
      const authContext = useAuth();

      return (
        <div>
          <div data-testid="has-user">{typeof authContext.user}</div>
          <div data-testid="has-login">{typeof authContext.login}</div>
          <div data-testid="has-logout">{typeof authContext.logout}</div>
        </div>
      );
    };

    render(
      <AuthProvider>
        <ContextTestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('has-user')).toHaveTextContent('object');
    expect(screen.getByTestId('has-login')).toHaveTextContent('function');
    expect(screen.getByTestId('has-logout')).toHaveTextContent('function');
  });

  it('handles empty string in localStorage', () => {
    mockLocalStorage.getItem.mockReturnValue('');

    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-info')).toHaveTextContent('No user');
  });

  it('login creates user with correct structure', async () => {
    const user = userEvent.setup();
    mockLocalStorage.getItem.mockReturnValue(null);

    const DetailedTestComponent = () => {
      const { user, login } = useAuth();

      return (
        <div>
          <div data-testid="user-id">{user?.id || 'No ID'}</div>
          <div data-testid="user-name">{user?.name || 'No Name'}</div>
          <button
            data-testid="detailed-login-button"
            onClick={() => login('detaileduser', 'password')}
          >
            Login
          </button>
        </div>
      );
    };

    render(
      <AuthProvider>
        <DetailedTestComponent />
      </AuthProvider>
    );

    await act(async () => {
      await user.click(screen.getByTestId('detailed-login-button'));
    });

    await waitFor(() => {
      expect(screen.getByTestId('user-id')).toHaveTextContent('123');
      expect(screen.getByTestId('user-name')).toHaveTextContent('detaileduser');
    });
  });
});
