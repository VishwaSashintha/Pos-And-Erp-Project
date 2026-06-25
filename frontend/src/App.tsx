import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Sidebar } from './components/Sidebar';
import { Navbar } from './components/Navbar';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Customers } from './pages/Customers';
import { Vehicles } from './pages/Vehicles';
import { Workshop } from './pages/Workshop';
import { Products } from './pages/Products';
import { Suppliers } from './pages/Suppliers';
import { Sales } from './pages/Sales';
import { Purchases } from './pages/Purchases';
import { Accounting } from './pages/Accounting';
import { Employees } from './pages/Employees';
import { SuperAdminDashboard } from './pages/SuperAdminDashboard';
import { Onboard } from './pages/Onboard';
import './App.css';


const ProtectedLayout: React.FC<{ children: React.ReactNode; requiredPermissions?: string[] }> = ({ children, requiredPermissions }) => {
  const { token, isLoading, hasPermission } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-950">
        <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredPermissions && !hasPermission(requiredPermissions)) {
    return <Navigate to="/" replace />;
  }

  
  const getNavbarTitle = (pathname: string) => {
    switch (pathname) {
      case '/':
        return 'System Dashboard';
      case '/customers':
        return 'Customer Database';
      case '/products':
        return 'Inventory & Products';
      case '/suppliers':
        return 'Suppliers Registry';
      case '/sales':
        return 'Sales Terminal (POS)';
      case '/purchases':
        return 'Purchasing Hub';
      case '/accounting':
        return 'Financial Ledgers';
      case '/employees':
        return 'Employee Directory';
      case '/super-admin':
        return 'Super Admin Console';
      default:
        return 'BOS Platform';
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Navbar title={getNavbarTitle(location.pathname)} />
        <main className="p-8 overflow-y-auto flex-1 w-[calc(100%-16rem)] ml-64 bg-slate-950 text-slate-100">
          {children}
        </main>
      </div>
    </div>
  );
};


const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-950">
        <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (token) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

const AppContent: React.FC = () => {
  return (
    <Routes>
      <Route
        path="/login"
        element={
          <PublicRoute>
            <Login />
          </PublicRoute>
        }
      />
      
      <Route
        path="/"
        element={
          <ProtectedLayout>
            <Dashboard />
          </ProtectedLayout>
        }
      />

      <Route
        path="/customers"
        element={
          <ProtectedLayout requiredPermissions={['RECORD_TRANSACTIONS', 'MANAGE_USERS']}>
            <Customers />
          </ProtectedLayout>
        }
      />

      <Route
        path="/vehicles"
        element={
          <ProtectedLayout requiredPermissions={['RECORD_TRANSACTIONS', 'MANAGE_USERS']}>
            <Vehicles />
          </ProtectedLayout>
        }
      />

      <Route
        path="/workshop"
        element={
          <ProtectedLayout requiredPermissions={['RECORD_TRANSACTIONS']}>
            <Workshop />
          </ProtectedLayout>
        }
      />

      <Route
        path="/products"
        element={
          <ProtectedLayout requiredPermissions={['MANAGE_PRODUCTS']}>
            <Products />
          </ProtectedLayout>
        }
      />

      <Route
        path="/suppliers"
        element={
          <ProtectedLayout requiredPermissions={['MANAGE_SUPPLIERS']}>
            <Suppliers />
          </ProtectedLayout>
        }
      />

      <Route
        path="/sales"
        element={
          <ProtectedLayout requiredPermissions={['RECORD_TRANSACTIONS']}>
            <Sales />
          </ProtectedLayout>
        }
      />

      <Route
        path="/purchases"
        element={
          <ProtectedLayout requiredPermissions={['MANAGE_PURCHASES']}>
            <Purchases />
          </ProtectedLayout>
        }
      />

      <Route
        path="/accounting"
        element={
          <ProtectedLayout requiredPermissions={['VIEW_FINANCES', 'RECORD_TRANSACTIONS']}>
            <Accounting />
          </ProtectedLayout>
        }
      />

      <Route
        path="/employees"
        element={
          <ProtectedLayout requiredPermissions={['MANAGE_EMPLOYEES']}>
            <Employees />
          </ProtectedLayout>
        }
      />

      <Route
        path="/super-admin"
        element={
          <ProtectedLayout requiredPermissions={['PLATFORM_ADMIN']}>
            <SuperAdminDashboard />
          </ProtectedLayout>
        }
      />

      <Route
        path="/onboard"
        element={
          <PublicRoute>
            <Onboard />
          </PublicRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
