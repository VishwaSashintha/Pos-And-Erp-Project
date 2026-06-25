import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Play, Square, DollarSign, Clock, Calendar, CheckCircle2 } from 'lucide-react';

interface PosShift {
  id: string;
  terminalName: string;
  openedBy: { name: string };
  closedBy?: { name: string };
  startTime: string;
  endTime?: string;
  openingCash: number;
  closingCash?: number;
  expectedCash?: number;
  status: string;
}

export const PosShifts: React.FC = () => {
  const { user } = useAuth();
  const [shifts, setShifts] = useState<PosShift[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchShifts = async () => {
    setLoading(true);
    try {
      const data = await apiFetch('/api/pos/shifts');
      setShifts(data || []);
    } catch (e) {
      console.error('Failed to fetch POS shifts', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchShifts();
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">POS Shifts & Cash Drawer</h2>
          <p className="text-slate-400 text-sm mt-1">Monitor terminal sessions, opening cash, and daily closings</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-lg relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
             <Play size={64} className="text-emerald-500" />
          </div>
          <h3 className="text-slate-400 text-sm font-medium mb-1 relative z-10">Active Shifts</h3>
          <p className="text-3xl font-bold text-white relative z-10">{shifts.filter(s => s.status === 'OPEN').length}</p>
        </div>
        
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-lg relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
             <DollarSign size={64} className="text-indigo-500" />
          </div>
          <h3 className="text-slate-400 text-sm font-medium mb-1 relative z-10">Total Cash in Drawers</h3>
          <p className="text-3xl font-bold text-emerald-400 relative z-10">
            ${shifts.filter(s => s.status === 'OPEN').reduce((sum, s) => sum + (s.openingCash || 0), 0).toFixed(2)}
          </p>
        </div>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
        {loading ? (
           <div className="py-12 flex justify-center"><div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div></div>
        ) : (
           <div className="overflow-x-auto">
             <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 text-sm uppercase tracking-wider">
                    <th className="pb-3 font-medium pl-4">Terminal</th>
                    <th className="pb-3 font-medium">Status</th>
                    <th className="pb-3 font-medium">Opened By / Time</th>
                    <th className="pb-3 font-medium">Closed By / Time</th>
                    <th className="pb-3 font-medium">Cash Flow</th>
                  </tr>
                </thead>
                <tbody className="text-sm">
                  {shifts.map((shift) => (
                    <tr key={shift.id} className="border-b border-slate-800/50 hover:bg-slate-800/20 transition-colors">
                      <td className="py-4 pl-4 text-slate-200 font-medium">
                        <div className="flex items-center gap-2">
                           <Monitor size={16} className="text-slate-500"/>
                           {shift.terminalName}
                        </div>
                      </td>
                      <td className="py-4">
                        <span className={`px-2 py-0.5 rounded text-xs border font-medium ${
                          shift.status === 'OPEN' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' : 'bg-slate-800 text-slate-400 border-slate-700'
                        }`}>
                          {shift.status}
                        </span>
                      </td>
                      <td className="py-4 text-slate-300">
                        <div className="font-medium text-slate-200">{shift.openedBy?.name || 'Unknown'}</div>
                        <div className="text-xs text-slate-500 flex items-center gap-1 mt-0.5"><Clock size={12}/> {new Date(shift.startTime).toLocaleString()}</div>
                      </td>
                      <td className="py-4 text-slate-300">
                        {shift.status === 'CLOSED' ? (
                          <>
                            <div className="font-medium text-slate-200">{shift.closedBy?.name || 'Unknown'}</div>
                            <div className="text-xs text-slate-500 flex items-center gap-1 mt-0.5"><CheckCircle2 size={12}/> {new Date(shift.endTime!).toLocaleString()}</div>
                          </>
                        ) : (
                          <span className="text-slate-600">-</span>
                        )}
                      </td>
                      <td className="py-4">
                        <div className="text-slate-300 text-xs">Opening: <span className="font-medium text-slate-200">${shift.openingCash?.toFixed(2)}</span></div>
                        {shift.status === 'CLOSED' && (
                          <>
                             <div className="text-slate-300 text-xs mt-1">Expected: <span className="font-medium text-slate-200">${shift.expectedCash?.toFixed(2)}</span></div>
                             <div className="text-slate-300 text-xs mt-1">Actual: <span className="font-medium text-indigo-400">${shift.closingCash?.toFixed(2)}</span></div>
                          </>
                        )}
                      </td>
                    </tr>
                  ))}
                  {shifts.length === 0 && (
                     <tr>
                       <td colSpan={5} className="py-8 text-center text-slate-500">
                         No POS shifts found.
                       </td>
                     </tr>
                  )}
                </tbody>
             </table>
           </div>
        )}
      </div>
    </div>
  );
};

// Monitor Icon component inline
const Monitor = ({ size, className }: { size: number; className?: string }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}>
    <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
    <line x1="8" y1="21" x2="16" y2="21"></line>
    <line x1="12" y1="17" x2="12" y2="21"></line>
  </svg>
);
