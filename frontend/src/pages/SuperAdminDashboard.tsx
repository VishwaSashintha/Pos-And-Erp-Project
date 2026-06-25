import React, { useEffect, useState } from 'react';
import { apiFetch } from '../api';
import { Check, X, ShieldAlert, Copy, CheckCircle, Info, AlertTriangle } from 'lucide-react';
import { Modal } from '../components/Modal';

interface PendingEmployee {
  id: string;
  name: string;
  email: string;
  role: string;
  department?: string;
  status: string;
  tenantId: string;
}

export const SuperAdminDashboard: React.FC = () => {
  const [pendingList, setPendingList] = useState<PendingEmployee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Activation Link Modal State
  const [activationLink, setActivationLink] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [copied, setCopied] = useState(false);

  const fetchPending = async () => {
    try {
      setLoading(true);
      const data = await apiFetch('/api/employees/pending');
      setPendingList(data || []);
    } catch (e: any) {
      setError(e.message || 'Failed to load pending employee approvals');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPending();
  }, []);

  const handleApprove = async (id: string) => {
    setError(null);
    try {
      const response = await apiFetch(`/api/employees/${id}/approve`, {
        method: 'POST',
      });
      
      if (response && response.activationToken) {
        const link = `${window.location.origin}/onboard?token=${response.activationToken}`;
        setActivationLink(link);
        setIsModalOpen(true);
      } else {
        alert('Employee approved successfully!');
      }
      fetchPending();
    } catch (err: any) {
      setError(err.message || 'Failed to approve employee');
    }
  };

  const handleReject = async (id: string) => {
    if (!window.confirm('Are you sure you want to reject this employee approval request?')) return;
    setError(null);
    try {
      await apiFetch(`/api/employees/${id}/reject`, {
        method: 'POST',
      });
      alert('Employee request rejected.');
      fetchPending();
    } catch (err: any) {
      setError(err.message || 'Failed to reject employee');
    }
  };

  const copyToClipboard = () => {
    if (!activationLink) return;
    navigator.clipboard.writeText(activationLink);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
          <ShieldAlert size={28} className="text-indigo-500" />
          Super Admin Console
        </h1>
        <p className="text-slate-400 text-sm mt-1">Platform management workspace. Review and approve tenant employee onboardings.</p>
      </div>

      {error && (
        <div className="bg-rose-500/10 border border-rose-500/20 text-rose-400 px-4 py-3 rounded-xl text-sm flex items-center gap-2">
          <AlertTriangle size={18} />
          <span>{error}</span>
        </div>
      )}

      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <h2 className="text-lg font-bold text-white mb-4">Pending Employee Approvals</h2>

        {loading ? (
          <div className="flex items-center justify-center min-h-[20vh]">
            <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
          </div>
        ) : pendingList.length === 0 ? (
          <div className="text-center py-10 text-slate-500">
            <Info className="mx-auto text-slate-700 mb-3" size={32} />
            <p className="font-semibold text-slate-400">All caught up!</p>
            <p className="text-sm">There are no pending employee activation requests.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/20 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                  <th className="p-4">Name</th>
                  <th className="p-4">Email</th>
                  <th className="p-4">Requested Role</th>
                  <th className="p-4">Department</th>
                  <th className="p-4">Tenant ID</th>
                  <th className="p-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-850 text-sm">
                {pendingList.map((emp) => (
                  <tr key={emp.id} className="text-slate-350 hover:bg-slate-850/20 transition-colors">
                    <td className="p-4 font-bold text-white">{emp.name}</td>
                    <td className="p-4">{emp.email}</td>
                    <td className="p-4">
                      <span className="bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 text-xs font-semibold px-2 py-0.5 rounded-full">
                        {emp.role}
                      </span>
                    </td>
                    <td className="p-4">{emp.department || 'N/A'}</td>
                    <td className="p-4 font-mono text-xs text-slate-500">{emp.tenantId}</td>
                    <td className="p-4 text-right">
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => handleApprove(emp.id)}
                          className="flex items-center gap-1 bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-1.5 px-3 rounded-lg text-xs cursor-pointer transition-colors shadow shadow-emerald-650/20"
                        >
                          <Check size={14} />
                          Approve
                        </button>
                        <button
                          onClick={() => handleReject(emp.id)}
                          className="flex items-center gap-1 bg-rose-600 hover:bg-rose-500 text-white font-bold py-1.5 px-3 rounded-lg text-xs cursor-pointer transition-colors"
                        >
                          <X size={14} />
                          Reject
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Employee Activation Link Generated"
      >
        <div className="space-y-4">
          <div className="bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 p-4 rounded-xl flex items-start gap-3">
            <CheckCircle className="shrink-0 text-emerald-400 mt-0.5" size={20} />
            <div>
              <h4 className="font-bold text-sm">Request Approved</h4>
              <p className="text-xs text-emerald-500/80 mt-0.5">
                The employee profile is approved. Use the secure onboarding link below to complete setup.
              </p>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-400 uppercase tracking-wider">
              Onboarding Link
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                readOnly
                value={activationLink || ''}
                className="flex-1 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-slate-300 text-xs font-mono select-all focus:outline-none"
              />
              <button
                onClick={copyToClipboard}
                className="bg-slate-800 hover:bg-slate-750 text-slate-200 p-2 rounded-xl flex items-center justify-center cursor-pointer transition-all border border-slate-750"
                title="Copy to clipboard"
              >
                <Copy size={16} />
              </button>
            </div>
            {copied && <span className="text-[10px] text-emerald-400 block font-semibold">Copied!</span>}
          </div>

          <p className="text-xs text-slate-500">
            For production environments, this link is printed to the server logs and will be sent to the employee email address.
          </p>

          <div className="flex justify-end pt-2 border-t border-slate-850">
            <button
              onClick={() => setIsModalOpen(false)}
              className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2 px-4 rounded-xl text-xs transition-colors cursor-pointer"
            >
              Done
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
