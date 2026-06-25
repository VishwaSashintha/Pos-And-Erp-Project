import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Database, ShieldAlert } from 'lucide-react';

interface NavbarProps {
  title: string;
}

export const Navbar: React.FC<NavbarProps> = ({ title }) => {
  const { user } = useAuth();

  return (
    <header className="h-16 border-b border-slate-800 bg-slate-900/80 backdrop-blur-md text-slate-100 flex items-center justify-between px-8 sticky top-0 z-20 w-[calc(100%-16rem)] ml-64">
      <div>
        <h2 className="text-xl font-bold text-white tracking-tight">{title}</h2>
      </div>
      <div className="flex items-center gap-6">
        <div className="flex items-center gap-2 bg-slate-800/80 px-3 py-1.5 rounded-xl border border-slate-700">
          <Database className="text-indigo-400" size={16} />
          <div className="flex flex-col text-left">
            <span className="text-[10px] text-slate-400 font-semibold uppercase tracking-wider leading-none">
              Tenant ID
            </span>
            <span className="text-xs text-indigo-200 font-mono font-medium truncate max-w-[150px] leading-tight">
              {user?.tenantId}
            </span>
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs text-amber-400 bg-amber-500/10 px-3 py-1.5 rounded-xl border border-amber-500/20 font-medium">
          <ShieldAlert size={14} />
          <span>PostgreSQL Scoped</span>
        </div>
      </div>
    </header>
  );
};
