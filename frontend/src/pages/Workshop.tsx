import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Plus, Search, Edit3, Trash2, Wrench, User, FileText, CheckCircle, Clock, Car } from 'lucide-react';
import { Modal } from '../components/Modal';

interface Customer {
  id: string;
  name: string;
}

interface JobCard {
  id: string;
  jobNumber: string;
  vehicleNumber: string;
  status: string;
  laborCost: number;
  partsCost: number;
  totalCost: number;
  invoiceGenerated: boolean;
  customer: Customer;
}

export const Workshop: React.FC = () => {
  const { user } = useAuth();
  const [jobCards, setJobCards] = useState<JobCard[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  const [editingId, setEditingId] = useState<string | null>(null);

  const [vehicleNumber, setVehicleNumber] = useState('');
  const [status, setStatus] = useState('CREATED');
  const [laborCost, setLaborCost] = useState<number | ''>(0);
  const [partsCost, setPartsCost] = useState<number | ''>(0);
  const [customerId, setCustomerId] = useState('');

  const fetchData = async () => {
    if (!user) return;
    try {
      const [jobsData, custData] = await Promise.all([
        apiFetch(`/api/jobcards/${user.tenantId}`),
        apiFetch(`/api/customers/${user.tenantId}`)
      ]);
      setJobCards(jobsData || []);
      setCustomers(custData || []);
    } catch (e) {
      console.error('Failed to fetch data', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const resetForm = () => {
    setEditingId(null);
    setVehicleNumber('');
    setStatus('CREATED');
    setLaborCost(0);
    setPartsCost(0);
    setCustomerId('');
  };

  const handleOpenCreateModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (job: JobCard) => {
    setEditingId(job.id);
    setVehicleNumber(job.vehicleNumber || '');
    setStatus(job.status || 'CREATED');
    setLaborCost(job.laborCost || 0);
    setPartsCost(job.partsCost || 0);
    setCustomerId(job.customer?.id || '');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!vehicleNumber || !customerId) return;

    const totalCost = (Number(laborCost) || 0) + (Number(partsCost) || 0);

    const body = {
      vehicleNumber,
      status,
      laborCost: Number(laborCost),
      partsCost: Number(partsCost),
      totalCost,
      customer: { id: customerId }
    };

    try {
      if (editingId) {
        await apiFetch(`/api/jobcards/${user?.tenantId}/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(body),
        });
      } else {
        await apiFetch('/api/jobcards', {
          method: 'POST',
          body: JSON.stringify({ ...body, tenant: { id: user?.tenantId } }),
        });
      }
      setIsModalOpen(false);
      resetForm();
      fetchData();
    } catch (err) {
      alert('Error saving job card data');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this job card?')) return;
    try {
      await apiFetch(`/api/jobcards/${user?.tenantId}/${id}`, {
        method: 'DELETE',
      });
      fetchData();
    } catch (e) {
      alert('Failed to delete job card');
    }
  };

  const handleGenerateInvoice = async (jobCardId: string) => {
    if (!window.confirm('Generate an invoice for this Job Card? This will lock costs and reduce inventory stock.')) return;
    try {
      await apiFetch(`/api/jobcards/${jobCardId}/generate-invoice`, {
        method: 'POST',
        headers: {
          'tenantId': user?.tenantId || ''
        }
      });
      alert('Invoice generated successfully!');
      fetchData();
    } catch (e) {
      alert('Failed to generate invoice');
    }
  };

  const filteredJobs = jobCards.filter(
    (j) =>
      (j.jobNumber && j.jobNumber.toLowerCase().includes(search.toLowerCase())) ||
      (j.vehicleNumber && j.vehicleNumber.toLowerCase().includes(search.toLowerCase())) ||
      (j.customer?.name && j.customer.name.toLowerCase().includes(search.toLowerCase()))
  );

  const getStatusColor = (s: string) => {
    switch (s) {
      case 'CREATED': return 'bg-slate-500/10 text-slate-400 border-slate-500/20';
      case 'IN_PROGRESS': return 'bg-amber-500/10 text-amber-400 border-amber-500/20';
      case 'COMPLETED': return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20';
      case 'INVOICED': return 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20';
      default: return 'bg-slate-500/10 text-slate-400 border-slate-500/20';
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Workshop & Job Cards</h1>
          <p className="text-slate-400 text-sm mt-1">Manage repair jobs, assign mechanics, and track repair costs.</p>
        </div>
        <button
          onClick={handleOpenCreateModal}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all cursor-pointer text-sm"
        >
          <Plus size={18} />
          <span>New Job Card</span>
        </button>
      </div>
      <div className="relative max-w-md">
        <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 pointer-events-none">
          <Search size={18} />
        </span>
        <input
          type="text"
          placeholder="Search by Job No, Vehicle No, or Customer..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
        />
      </div>

      {loading ? (
        <div className="flex items-center justify-center min-h-[30vh]">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : filteredJobs.length === 0 ? (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
          <Wrench size={40} className="mx-auto text-slate-600 mb-4" />
          <p className="font-semibold text-lg text-slate-400 mb-1">No Job Cards found</p>
          <p className="text-sm">Create a new job card to begin repairs.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredJobs.map((job) => (
            <div key={job.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl hover:border-slate-700 transition-colors group">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="font-bold text-white text-lg tracking-wide">{job.jobNumber}</h3>
                  <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-semibold border ${getStatusColor(job.status)} mt-2`}>
                    {job.status === 'COMPLETED' ? <CheckCircle size={12}/> : <Clock size={12}/>}
                    {job.status.replace('_', ' ')}
                  </span>
                </div>
                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button onClick={() => handleOpenEditModal(job)} className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition-colors">
                    <Edit3 size={16} />
                  </button>
                  <button onClick={() => handleDelete(job.id)} className="p-1.5 text-slate-400 hover:text-rose-400 rounded-lg hover:bg-slate-800 transition-colors">
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
              
              <div className="space-y-3 mb-5">
                <div className="flex items-center gap-3 text-sm text-slate-300">
                  <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-indigo-400 shrink-0">
                    <Car size={14} />
                  </div>
                  <div>
                    <div className="text-xs text-slate-500 font-medium">Vehicle</div>
                    <div className="font-semibold">{job.vehicleNumber}</div>
                  </div>
                </div>
                <div className="flex items-center gap-3 text-sm text-slate-300">
                  <div className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-indigo-400 shrink-0">
                    <User size={14} />
                  </div>
                  <div>
                    <div className="text-xs text-slate-500 font-medium">Customer</div>
                    <div className="font-semibold truncate w-48">{job.customer?.name || 'Unknown'}</div>
                  </div>
                </div>
              </div>

              <div className="bg-slate-950/50 rounded-xl p-3 border border-slate-800/50 mb-4">
                <div className="flex justify-between text-xs text-slate-400 mb-1">
                  <span>Labor Cost</span>
                  <span>${job.laborCost?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between text-xs text-slate-400 mb-2">
                  <span>Parts Cost</span>
                  <span>${job.partsCost?.toFixed(2) || '0.00'}</span>
                </div>
                <div className="flex justify-between text-sm font-bold text-white border-t border-slate-800 pt-2">
                  <span>Total Est. Cost</span>
                  <span className="text-emerald-400">${job.totalCost?.toFixed(2) || '0.00'}</span>
                </div>
              </div>

              {!job.invoiceGenerated && job.status === 'COMPLETED' ? (
                 <button 
                  onClick={() => handleGenerateInvoice(job.id)}
                  className="w-full py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold rounded-xl transition-colors flex justify-center items-center gap-2 text-sm"
                 >
                   <FileText size={16} /> Generate Invoice
                 </button>
              ) : job.invoiceGenerated ? (
                 <div className="w-full py-2.5 bg-slate-800 text-slate-400 font-semibold rounded-xl flex justify-center items-center gap-2 text-sm border border-slate-700 cursor-not-allowed">
                   <CheckCircle size={16} className="text-emerald-500" /> Invoiced
                 </div>
              ) : (
                 <div className="w-full py-2.5 bg-slate-950 text-slate-500 font-semibold rounded-xl flex justify-center items-center gap-2 text-sm border border-slate-800 border-dashed">
                   Work in Progress
                 </div>
              )}

            </div>
          ))}
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingId ? 'Edit Job Card' : 'Create Job Card'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Vehicle Reg Number <span className="text-indigo-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g. ABC-1234"
                value={vehicleNumber}
                onChange={(e) => setVehicleNumber(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm font-mono"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Status
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
              >
                <option value="CREATED">Created</option>
                <option value="ASSIGNED">Assigned</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="ON_HOLD">On Hold</option>
                <option value="COMPLETED">Completed</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Customer <span className="text-indigo-500">*</span>
            </label>
            <select
              required
              value={customerId}
              onChange={(e) => setCustomerId(e.target.value)}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
            >
              <option value="">Select Customer...</option>
              {customers.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Est. Labor Cost
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">$</span>
                <input
                  type="number"
                  step="0.01"
                  value={laborCost}
                  onChange={(e) => setLaborCost(e.target.value === '' ? '' : Number(e.target.value))}
                  className="w-full pl-8 pr-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
                />
              </div>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Est. Parts Cost
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">$</span>
                <input
                  type="number"
                  step="0.01"
                  value={partsCost}
                  onChange={(e) => setPartsCost(e.target.value === '' ? '' : Number(e.target.value))}
                  className="w-full pl-8 pr-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
                />
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-850 pt-4 mt-6">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 font-semibold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer"
            >
              {editingId ? 'Save Changes' : 'Create Job Card'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
