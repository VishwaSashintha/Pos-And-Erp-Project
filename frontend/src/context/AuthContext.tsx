import React, { createContext, useState, useEffect, useContext } from 'react';

export interface User {
  username: string;
  role: string;
  tenantId: string | null;
  enabledModules: string[];
}

export const ROLE_PERMISSIONS: Record<string, string[]> = {
  SUPER_ADMIN: ["MANAGE_USERS", "VIEW_FINANCES", "RECORD_TRANSACTIONS", "MANAGE_PRODUCTS", "MANAGE_SUPPLIERS", "MANAGE_PURCHASES", "VIEW_REPORTS", "PLATFORM_ADMIN", "MANAGE_EMPLOYEES"],
  OWNER: ["MANAGE_USERS", "VIEW_FINANCES", "RECORD_TRANSACTIONS", "MANAGE_PRODUCTS", "MANAGE_SUPPLIERS", "MANAGE_PURCHASES", "VIEW_REPORTS", "MANAGE_EMPLOYEES"],
  ADMIN: ["MANAGE_USERS", "VIEW_FINANCES", "RECORD_TRANSACTIONS", "MANAGE_PRODUCTS", "MANAGE_SUPPLIERS", "MANAGE_PURCHASES", "VIEW_REPORTS", "MANAGE_EMPLOYEES"],
  MANAGER: ["VIEW_FINANCES", "RECORD_TRANSACTIONS", "MANAGE_PRODUCTS", "MANAGE_SUPPLIERS", "MANAGE_PURCHASES", "VIEW_REPORTS"],
  CASHIER: ["RECORD_TRANSACTIONS"],
  STORE_KEEPER: ["MANAGE_PRODUCTS", "MANAGE_SUPPLIERS", "MANAGE_PURCHASES"],
  ACCOUNTANT: ["VIEW_FINANCES", "RECORD_TRANSACTIONS", "VIEW_REPORTS"],
  HR_OFFICER: ["MANAGE_EMPLOYEES", "VIEW_REPORTS"]
};

interface AuthContextType {
  token: string | null;
  user: User | null;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  registerTenant: (
    tenantName: string,
    username: string,
    password: string,
    email: string,
    industry?: string,
    selectedModules?: string[]
  ) => Promise<void>;
  logout: () => void;
  hasPermission: (perms: string[]) => boolean;
  isSuperAdmin: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);


function decodeJwt(token: string): any {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      window
        .atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    if (token) {
      const decoded = decodeJwt(token);
      if (decoded) {
        
        const isExpired = decoded.exp * 1000 < Date.now();
        if (isExpired) {
          logout();
        } else {
          setUser({
            username: decoded.sub,
            role: decoded.role,
            tenantId: decoded.tenantId || null,
            enabledModules: decoded.enabledModules || [],
          });
        }
      } else {
        logout();
      }
    } else {
      setUser(null);
    }
    setIsLoading(false);
  }, [token]);

  const login = async (username: string, password: string) => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Login failed. Please check credentials.');
      }

      const apiResponse = await response.json();
      // ApiResponse wraps data inside { success, message, data: { token, ... } }
      const payload = apiResponse.data || apiResponse;

      localStorage.setItem('token', payload.token);
      setToken(payload.token);

      const decoded = decodeJwt(payload.token);
      setUser({
        username: decoded?.sub || payload.username,
        role: decoded?.role || payload.role,
        tenantId: decoded?.tenantId || payload.tenantId || null,
        enabledModules: decoded?.enabledModules || [],
      });
    } catch (error) {
      logout();
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const registerTenant = async (
    tenantName: string,
    username: string,
    password: string,
    email: string,
    industry?: string,
    selectedModules?: string[]
  ) => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/auth/register-tenant', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ tenantName, username, password, email, industry, selectedModules }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Tenant registration failed.');
      }

      // After registration, log in automatically
      await login(username, password);
    } catch (error) {
      setIsLoading(false);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  const hasPermission = (perms: string[]): boolean => {
    if (!user) return false;
    const roleUpper = user.role.toUpperCase();
    // Super Admins have all permissions
    if (roleUpper === 'SUPER_ADMIN') return true;
    const userPerms = ROLE_PERMISSIONS[roleUpper] || [];
    return perms.some((p) => userPerms.includes(p));
  };

  const isSuperAdmin = (): boolean => {
    return user?.role?.toUpperCase() === 'SUPER_ADMIN';
  };

  return (
    <AuthContext.Provider value={{ token, user, isLoading, login, registerTenant, logout, hasPermission, isSuperAdmin }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
