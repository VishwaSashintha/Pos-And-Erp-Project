import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Users, FileText, Clock, DollarSign, Plus, Search, Calendar, CheckCircle2, XCircle } from 'lucide-react';
import { Modal } from '../components/Modal';

export const HRHub: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'leaves' | 'attendance' | 'payroll'>('leaves');
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchData = async (endpoint: string) => {
    setLoading(true);
    try {
      const result = await apiFetch(endpoint);
      setData(result || []);
    } catch (e) {
      console.error('Failed to fetch HR data', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'leaves') fetchData('/api/hr/leaves');
    else if (activeTab === 'attendance') fetchData('/api/hr/attendance');
    else if (activeTab === 'payroll') fetchData('/api/hr/payroll');
  }, [activeTab]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">HR & Payroll Hub</h2>
          <p className="text-slate-400 text-sm mt-1">Manage employee attendance, leaves, and payroll</p>
        </div>
      </div>

      <div className="flex space-x-1 border-b border-slate-800">
        <button
          onClick={() => setActiveTab('leaves')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'leaves' ? 'border-indigo-500 text-indigo-400' : 'border-transparent text-slate-400 hover:text-slate-300 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center gap-2"><Calendar size={16}/> Leave Requests</div>
        </button>
        <button
          onClick={() => setActiveTab('attendance')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'attendance' ? 'border-indigo-500 text-indigo-400' : 'border-transparent text-slate-400 hover:text-slate-300 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center gap-2"><Clock size={16}/> Attendance Logs</div>
        </button>
        <button
          onClick={() => setActiveTab('payroll')}
          className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
            activeTab === 'payroll' ? 'border-indigo-500 text-indigo-400' : 'border-transparent text-slate-400 hover:text-slate-300 hover:border-slate-700'
          }`}
        >
          <div className="flex items-center gap-2"><DollarSign size={16}/> Payroll</div>
        </button>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
        {loading ? (
           <div className="py-12 flex justify-center"><div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div></div>
        ) : (
           <div className="overflow-x-auto">
             <table className="w-full text-left border-collapse">
                <thead>
                  {activeTab === 'leaves' && (
                    <tr className="border-b border-slate-800 text-slate-400 text-sm uppercase tracking-wider">
                      <th className="pb-3 font-medium pl-4">Employee</th>
                      <th className="pb-3 font-medium">Type</th>
                      <th className="pb-3 font-medium">From</th>
                      <th className="pb-3 font-medium">To</th>
                      <th className="pb-3 font-medium">Status</th>
                    </tr>
                  )}
                  {activeTab === 'attendance' && (
                    <tr className="border-b border-slate-800 text-slate-400 text-sm uppercase tracking-wider">
                      <th className="pb-3 font-medium pl-4">Employee</th>
                      <th className="pb-3 font-medium">Date</th>
                      <th className="pb-3 font-medium">Check In</th>
                      <th className="pb-3 font-medium">Check Out</th>
                      <th className="pb-3 font-medium">Late</th>
                    </tr>
                  )}
                  {activeTab === 'payroll' && (
                    <tr className="border-b border-slate-800 text-slate-400 text-sm uppercase tracking-wider">
                      <th className="pb-3 font-medium pl-4">Employee</th>
                      <th className="pb-3 font-medium">Period</th>
                      <th className="pb-3 font-medium">Basic ($)</th>
                      <th className="pb-3 font-medium">Net ($)</th>
                      <th className="pb-3 font-medium">Status</th>
                    </tr>
                  )}
                </thead>
                <tbody className="text-sm">
                  {data.map((item, i) => (
                    <tr key={i} className="border-b border-slate-800/50 hover:bg-slate-800/20 transition-colors">
                      <td className="py-4 pl-4 text-slate-200 font-medium">{item.employee?.name || 'Unknown'}</td>
                      
                      {activeTab === 'leaves' && (
                        <>
                          <td className="py-4 text-slate-300">{item.leaveType}</td>
                          <td className="py-4 text-slate-300">{item.startDate}</td>
                          <td className="py-4 text-slate-300">{item.endDate}</td>
                          <td className="py-4">
                            <span className={`px-2 py-0.5 rounded text-xs border ${
                              item.status === 'APPROVED' ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' :
                              item.status === 'REJECTED' ? 'bg-rose-500/10 text-rose-400 border-rose-500/20' :
                              'bg-amber-500/10 text-amber-400 border-amber-500/20'
                            }`}>{item.status}</span>
                          </td>
                        </>
                      )}

                      {activeTab === 'attendance' && (
                        <>
                          <td className="py-4 text-slate-300">{item.date}</td>
                          <td className="py-4 text-slate-300">{item.checkIn ? new Date(item.checkIn).toLocaleTimeString() : '-'}</td>
                          <td className="py-4 text-slate-300">{item.checkOut ? new Date(item.checkOut).toLocaleTimeString() : '-'}</td>
                          <td className="py-4">
                            {item.isLate ? <span className="text-rose-400 font-medium">Yes</span> : <span className="text-emerald-400">No</span>}
                          </td>
                        </>
                      )}

                      {activeTab === 'payroll' && (
                        <>
                          <td className="py-4 text-slate-300">{item.month}/{item.year}</td>
                          <td className="py-4 text-slate-300">${item.basicSalary?.toFixed(2)}</td>
                          <td className="py-4 text-emerald-400 font-medium">${item.netSalary?.toFixed(2)}</td>
                          <td className="py-4">
                             <span className={`px-2 py-0.5 rounded text-xs border ${
                              item.status === 'PAID' ? 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20' : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                            }`}>{item.status}</span>
                          </td>
                        </>
                      )}
                    </tr>
                  ))}
                  {data.length === 0 && (
                     <tr>
                       <td colSpan={5} className="py-8 text-center text-slate-500">
                         No records found for the selected view.
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
