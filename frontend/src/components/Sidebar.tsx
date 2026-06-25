import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  Users,
  Car,
  Wrench,
  Package,
  Truck,
  ShoppingCart,
  FileText,
  TrendingUp,
  LogOut,
  Building,
  ShieldAlert
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar: React.FC = () => {
  const { logout, user, hasPermission, isSuperAdmin } = useAuth();

  const menuItems = [
    { name: 'Dashboard', path: '/', icon: LayoutDashboard },
    { name: 'Customers', path: '/customers', icon: Users, requiredPermissions: ['RECORD_TRANSACTIONS', 'MANAGE_USERS'], requiredModule: 'CRM' },
    { name: 'Vehicles', path: '/vehicles', icon: Car, requiredPermissions: ['RECORD_TRANSACTIONS', 'MANAGE_USERS'], requiredModule: 'POS' },
    { name: 'Workshop Jobs', path: '/workshop', icon: Wrench, requiredPermissions: ['RECORD_TRANSACTIONS'], requiredModule: 'POS' },
    { name: 'Inventory & Products', path: '/products', icon: Package, requiredPermissions: ['MANAGE_PRODUCTS'], requiredModule: 'INVENTORY' },
    { name: 'Suppliers', path: '/suppliers', icon: Truck, requiredPermissions: ['MANAGE_SUPPLIERS'], requiredModule: 'INVENTORY' },
    { name: 'Sales / POS', path: '/sales', icon: ShoppingCart, requiredPermissions: ['RECORD_TRANSACTIONS'], requiredModule: 'POS' },
    { name: 'POS Shifts', path: '/shifts', icon: FileText, requiredPermissions: ['MANAGE_POS'], requiredModule: 'POS' },
    { name: 'Purchases', path: '/purchases', icon: FileText, requiredPermissions: ['MANAGE_PURCHASES'], requiredModule: 'INVENTORY' },
    { name: 'Accounting', path: '/accounting', icon: TrendingUp, requiredPermissions: ['VIEW_FINANCES', 'RECORD_TRANSACTIONS'], requiredModule: 'ACCOUNTING' },
    { name: 'HR Hub', path: '/hr', icon: Users, requiredPermissions: ['MANAGE_HR'], requiredModule: 'HR' },
    { name: 'Assets', path: '/assets', icon: Package, requiredPermissions: ['VIEW_ASSETS'], requiredModule: 'ASSETS' },
    { name: 'Billing & Subscriptions', path: '/billing', icon: ShieldAlert, requiredPermissions: ['MANAGE_BILLING'] },
  ];

  const visibleMenuItems = menuItems.filter((item) => {
    if (isSuperAdmin()) return false;

    // Check module activation
    if (item.requiredModule) {
      const enabled = user?.enabledModules || [];
      // If legacy tenant with no modules defined, allow access for compatibility
      if (enabled.length > 0 && !enabled.includes(item.requiredModule)) {
        return false;
      }
    }

    // Check permissions
    if (item.requiredPermissions && !hasPermission(item.requiredPermissions)) {
      return false;
    }

    return true;
  });

  return (
    <aside className="w-64 bg-slate-900 border-r border-slate-800 text-slate-300 flex flex-col h-screen fixed left-0 top-0 z-30">
      <div className="p-6 border-b border-slate-800 flex items-center gap-3">
        <div className="bg-indigo-600 p-2 rounded-xl text-white shadow-lg shadow-indigo-500/30">
          <Building size={20} />
        </div>
        <div>
          <h1 className="font-bold text-lg text-white leading-none">BOS Platform</h1>
          <span className="text-xs text-indigo-400 font-medium tracking-wide uppercase">Universal OS</span>
        </div>
      </div>
      <div className="px-6 py-4 border-b border-slate-850 bg-slate-950/40">
        <div className="flex flex-col gap-1">
          <div className="text-xs text-slate-500 font-semibold tracking-wider uppercase">Active Session</div>
          <div className="text-sm font-semibold text-slate-200 truncate">{user?.username}</div>
          <div className="inline-flex items-center gap-1.5 mt-1">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
            <span className="text-xs text-indigo-300 font-medium capitalize bg-indigo-500/10 px-2 py-0.5 rounded-full">
              {user?.role?.toLowerCase() || 'guest'}
            </span>
          </div>
        </div>
      </div>
      <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
        {/* Render Platform Super Admin Panel */}
        {isSuperAdmin() && (
          <NavLink
            to="/super-admin"
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 font-medium ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-500/20'
                  : 'hover:bg-slate-800 hover:text-slate-100 text-slate-400'
              }`
            }
          >
            <ShieldAlert size={20} />
            <span>Super Admin Panel</span>
          </NavLink>
        )}

        {/* Render dynamic tenant-level menus */}
        {!isSuperAdmin() && visibleMenuItems.map((item) => (
          <NavLink
            key={item.name}
            to={item.path}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 font-medium ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-500/20'
                  : 'hover:bg-slate-800 hover:text-slate-100 text-slate-400'
              }`
            }
          >
            <item.icon size={20} />
            <span>{item.name}</span>
          </NavLink>
        ))}

        {/* Render Employees page link for OWNER */}
        {!isSuperAdmin() && (user?.role === 'OWNER') && (
          <NavLink
            to="/employees"
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 font-medium ${
                isActive
                  ? 'bg-indigo-600 text-white shadow-md shadow-indigo-500/20'
                  : 'hover:bg-slate-800 hover:text-slate-100 text-slate-400'
              }`
            }
          >
            <Users size={20} />
            <span>Employees</span>
          </NavLink>
        )}
      </nav>
      <div className="p-4 border-t border-slate-800">
        <button
          onClick={logout}
          className="flex items-center gap-3 w-full px-4 py-3 rounded-xl hover:bg-red-500/10 hover:text-red-400 text-slate-400 transition-all duration-200 font-medium cursor-pointer"
        >
          <LogOut size={20} />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
