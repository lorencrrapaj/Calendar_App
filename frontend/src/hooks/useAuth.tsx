import { createContext, useContext, ReactNode } from 'react';
import { useState, useEffect } from 'react';

// 1) Define the shape of your auth context:
interface AuthContextType {
  user: { id: string; name: string } | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

// 2) Create the context with a default value
const AuthContext = createContext<AuthContextType>({
  user: null,
  login: async () => {},
  logout: () => {},
});

// 3) Provider component to wrap your app
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<{ id: string; name: string } | null>(null);

  // e.g. check localStorage / cookie on mount
  useEffect(() => {
    const stored = localStorage.getItem('user');
    if (stored) {
      try {
        setUser(JSON.parse(stored));
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
      } catch (_error) {
        // Invalid JSON in localStorage, ignore and continue with null user
        localStorage.removeItem('user');
      }
    }
  }, []);

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const login = async (username: string, _password: string) => {
    // TODO: call your real API
    // const resp = await api.login(username, password);
    // setUser(resp.user);
    const fakeUser = { id: '123', name: username };
    setUser(fakeUser);
    localStorage.setItem('user', JSON.stringify(fakeUser));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// 4) Typed hook for consumers
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
