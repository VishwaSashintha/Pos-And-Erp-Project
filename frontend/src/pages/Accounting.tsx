import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Plus, Calendar, FileText, TrendingUp, TrendingDown, DollarSign, Edit3, Trash2 } from 'lucide-react';
import { Modal } from '../components/Modal';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

interface Expense {
  id: string;
  description: string;
  category: string;
  amount: number;
  date: string;
  reference?: string;
}

interface Income {
  id: string;
  description: string;
  category: string;
  amount: number;
  date: string;
  reference?: string;
}

interface PLReport {
  startDate: string;
  endDate: string;
  totalIncomes: number;
  totalExpenses: number;
  totalSales: number;
  totalSalesPaid: number;
  totalRevenue: number;
  netProfit: number;
}

export const Accounting: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'expenses' | 'incomes' | 'reports'>('reports');
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [incomes, setIncomes] = useState<Income[]>([]);
  const [plReport, setPlReport] = useState<PLReport | null>(null);
  const [loading, setLoading] = useState(true);

  
  const [startDate, setStartDate] = useState(
    new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0]
  );
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);

  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalType, setModalType] = useState<'expense' | 'income'>('expense');
  const [editingId, setEditingId] = useState<string | null>(null);

  
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState('');
  const [amount, setAmount] = useState(0);
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [reference, setReference] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [entryFileUrl, setEntryFileUrl] = useState<string | null>(null);

  const fetchLedgers = async () => {
    if (!user) return;
    try {
      const [expData, incData] = await Promise.all([
        apiFetch(`/api/finance/expenses/${user.tenantId}`).catch(() => []),
        apiFetch(`/api/finance/incomes/${user.tenantId}`).catch(() => [])
      ]);
      setExpenses(expData || []);
      setIncomes(incData || []);
    } catch (e) {
      console.error('Failed to load ledger sheets', e);
    }
  };

  const fetchReport = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const data = await apiFetch(
        `/api/finance/reports/profit-loss/${user.tenantId}?startDate=${startDate}&endDate=${endDate}`
      );
      setPlReport(data);
    } catch (e) {
      console.error('Failed to load profit and loss report', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLedgers();
    fetchReport();
  }, [user]);

  const handleFetchReport = (e: React.FormEvent) => {
    e.preventDefault();
    fetchReport();
  };

  const resetForm = () => {
    setEditingId(null);
    setDescription('');
    setCategory('');
    setAmount(0);
    setDate(new Date().toISOString().split('T')[0]);
    setReference('');
    setSelectedFile(null);
    setEntryFileUrl(null);
  };

  const handleOpenCreateModal = (type: 'expense' | 'income') => {
    resetForm();
    setModalType(type);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = async (item: any, type: 'expense' | 'income') => {
    setEditingId(item.id);
    setModalType(type);
    setDescription(item.description);
    setCategory(item.category);
    setAmount(item.amount);
    setDate(item.date);
    setReference(item.reference || '');
    setSelectedFile(null);
    setEntryFileUrl(null);
    setIsModalOpen(true);

    try {
      const files = await apiFetch(`/api/files/${user?.tenantId}?module=FINANCE&referenceId=${item.id}`);
      if (files && files.length > 0) {
        setEntryFileUrl(files[0].fileUrl);
      }
    } catch (e) {
      console.error('Failed to load attachment', e);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!description || !category || amount <= 0) return;

    const body = { description, category, amount: Number(amount), date, reference };
    const path = modalType === 'expense' ? 'expenses' : 'incomes';

    try {
      let savedEntry;
      if (editingId) {
        savedEntry = await apiFetch(`/api/finance/${path}/${user?.tenantId}/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(body),
        });
      } else {
        savedEntry = await apiFetch(`/api/finance/${path}`, {
          method: 'POST',
          body: JSON.stringify(body),
        });
      }

      if (selectedFile && savedEntry && savedEntry.id) {
        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('module', 'FINANCE');
        formData.append('referenceId', savedEntry.id);

        await fetch('/api/files/upload', {
          method: 'POST',
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
            'tenantId': user?.tenantId || '',
          },
          body: formData,
        });
      }

      setIsModalOpen(false);
      resetForm();
      fetchLedgers();
      fetchReport();
    } catch (err) {
      alert('Error saving ledger entry');
    }
  };

  const handleDelete = async (id: string, type: 'expense' | 'income') => {
    if (!window.confirm(`Are you sure you want to delete this ${type}?`)) return;
    const path = type === 'expense' ? 'expenses' : 'incomes';
    try {
      await apiFetch(`/api/finance/${path}/${user?.tenantId}/${id}`, {
        method: 'DELETE',
      });
      fetchLedgers();
      fetchReport();
    } catch (e) {
      alert('Failed to delete ledger entry');
    }
  };

  const formatCurrency = (val: number = 0) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(val);
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Financial Ledger</h1>
          <p className="text-slate-400 text-sm mt-1">Audit cash flows, track expenses, and view organizational profit sheets.</p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={() => handleOpenCreateModal('expense')}
            className="flex items-center gap-2 bg-slate-800 hover:bg-slate-750 text-rose-400 font-semibold py-2.5 px-4 rounded-xl border border-slate-700 transition-all cursor-pointer text-sm"
          >
            <Plus size={18} />
            <span>Record Expense</span>
          </button>
          <button
            onClick={() => handleOpenCreateModal('income')}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all cursor-pointer text-sm"
          >
            <Plus size={18} />
            <span>Record Income</span>
          </button>
        </div>
      </div>
      <div className="flex border-b border-slate-800">
        <button
          onClick={() => setActiveTab('reports')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'reports'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Profit & Loss Statement
        </button>
        <button
          onClick={() => setActiveTab('expenses')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'expenses'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Expenses Book
        </button>
        <button
          onClick={() => setActiveTab('incomes')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'incomes'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Other Incomes Book
        </button>
      </div>

      {activeTab === 'reports' && (
        
        <div className="space-y-6">
          <form onSubmit={handleFetchReport} className="bg-slate-900 border border-slate-800 p-5 rounded-2xl flex flex-wrap gap-4 items-end shadow-md">
            <div>
              <label className="block text-[10px] font-semibold text-slate-500 mb-1.5 uppercase tracking-wider">Start Date</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-xs"
              />
            </div>
            <div>
              <label className="block text-[10px] font-semibold text-slate-500 mb-1.5 uppercase tracking-wider">End Date</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-xs"
              />
            </div>
            <button
              type="submit"
              className="py-2.5 px-5 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl transition-colors cursor-pointer text-xs flex items-center gap-2"
            >
              <Calendar size={14} />
              <span>Generate Statement</span>
            </button>
          </form>

          {loading ? (
            <div className="flex items-center justify-center min-h-[20vh]">
              <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-xl flex items-center gap-4 bg-gradient-to-br from-indigo-500/10 to-indigo-600/5">
                  <div className="p-3 bg-indigo-600/20 text-indigo-400 rounded-xl">
                    <TrendingUp size={22} />
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Sales Revenue</span>
                    <h3 className="text-2xl font-extrabold text-white tracking-tight mt-0.5">{formatCurrency(plReport?.totalSales)}</h3>
                  </div>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-xl flex items-center gap-4 bg-gradient-to-br from-emerald-500/10 to-emerald-600/5">
                  <div className="p-3 bg-emerald-600/20 text-emerald-400 rounded-xl">
                    <DollarSign size={22} />
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Other Incomes</span>
                    <h3 className="text-2xl font-extrabold text-white tracking-tight mt-0.5">{formatCurrency(plReport?.totalIncomes)}</h3>
                  </div>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-xl flex items-center gap-4 bg-gradient-to-br from-rose-500/10 to-rose-600/5">
                  <div className="p-3 bg-rose-600/20 text-rose-400 rounded-xl">
                    <TrendingDown size={22} />
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Total Expenses</span>
                    <h3 className="text-2xl font-extrabold text-white tracking-tight mt-0.5">{formatCurrency(plReport?.totalExpenses)}</h3>
                  </div>
                </div>

                <div className="bg-slate-900 border border-slate-800 p-6 rounded-2xl shadow-xl flex items-center gap-4 bg-gradient-to-br from-teal-500/15 to-teal-600/10">
                  <div className="p-3 bg-teal-600/20 text-teal-400 rounded-xl">
                    <FileText size={22} />
                  </div>
                  <div>
                    <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider">Net Profit</span>
                    <h3 className={`text-2xl font-extrabold tracking-tight mt-0.5 ${(plReport?.netProfit || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                      {formatCurrency(plReport?.netProfit)}
                    </h3>
                  </div>
                </div>
              </div>
              <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl max-w-2xl">
                <h3 className="text-lg font-bold text-white mb-4 border-b border-slate-800 pb-3 uppercase tracking-wider text-xs text-slate-400">Statement breakdown</h3>
                
                <div className="space-y-4">
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-slate-350">Invoice Sales Revenue (A)</span>
                    <span className="font-semibold text-white">{formatCurrency(plReport?.totalSales)}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm">
                    <span className="text-slate-350">Manual Registered Incomes (B)</span>
                    <span className="font-semibold text-white">{formatCurrency(plReport?.totalIncomes)}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm border-b border-slate-800 pb-3">
                    <span className="font-bold text-slate-200">Gross Revenue (A + B)</span>
                    <span className="font-bold text-white">{formatCurrency(plReport?.totalRevenue)}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm border-b border-slate-800 pb-3">
                    <span className="text-slate-350">Operating / Material Expenses (C)</span>
                    <span className="font-semibold text-rose-400">-{formatCurrency(plReport?.totalExpenses)}</span>
                  </div>
                  <div className="flex justify-between items-center text-base pt-1">
                    <span className="font-extrabold text-white">Net Income / Profit</span>
                    <span className={`text-lg font-extrabold ${(plReport?.netProfit || 0) >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                      {formatCurrency(plReport?.netProfit)}
                    </span>
                  </div>
                </div>
              </div>
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
                  <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">Revenue vs Expenses</h3>
                  <ResponsiveContainer width="100%" height={220}>
                    <BarChart
                      data={[
                        { name: 'Sales', value: plReport?.totalSales || 0, fill: '#6366f1' },
                        { name: 'Incomes', value: plReport?.totalIncomes || 0, fill: '#10b981' },
                        { name: 'Expenses', value: plReport?.totalExpenses || 0, fill: '#f43f5e' },
                      ]}
                      margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                      <XAxis dataKey="name" stroke="#a1a1aa" tick={{ fontSize: 11 }} />
                      <YAxis stroke="#a1a1aa" tick={{ fontSize: 11 }} />
                      <Tooltip
                        contentStyle={{ backgroundColor: '#0f172a', border: '1px solid #334155', borderRadius: '12px', fontSize: '12px' }}
                        labelStyle={{ color: '#e2e8f0' }}
                      />
                      <Bar dataKey="value" radius={[8, 8, 0, 0]}>
                        {[
                          { name: 'Sales', fill: '#6366f1' },
                          { name: 'Incomes', fill: '#10b981' },
                          { name: 'Expenses', fill: '#f43f5e' },
                        ].map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={entry.fill} />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </div>
                <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
                  <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-4">Profit Composition</h3>
                  <ResponsiveContainer width="100%" height={220}>
                    <PieChart>
                      <Pie
                        data={[
                          { name: 'Sales Revenue', value: plReport?.totalSales || 0 },
                          { name: 'Other Incomes', value: plReport?.totalIncomes || 0 },
                          { name: 'Expenses', value: plReport?.totalExpenses || 0 },
                        ]}
                        cx="50%"
                        cy="50%"
                        innerRadius={50}
                        outerRadius={80}
                        paddingAngle={5}
                        dataKey="value"
                      >
                        <Cell fill="#6366f1" />
                        <Cell fill="#10b981" />
                        <Cell fill="#f43f5e" />
                      </Pie>
                      <Tooltip
                        contentStyle={{ backgroundColor: '#0f172a', border: '1px solid #334155', borderRadius: '12px', fontSize: '12px' }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="flex justify-center gap-4 mt-2">
                    <div className="flex items-center gap-1.5 text-xs text-slate-400"><span className="w-2.5 h-2.5 rounded-full bg-indigo-500 inline-block"></span>Sales</div>
                    <div className="flex items-center gap-1.5 text-xs text-slate-400"><span className="w-2.5 h-2.5 rounded-full bg-emerald-500 inline-block"></span>Incomes</div>
                    <div className="flex items-center gap-1.5 text-xs text-slate-400"><span className="w-2.5 h-2.5 rounded-full bg-rose-500 inline-block"></span>Expenses</div>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
      )}

      {activeTab === 'expenses' && (
        
        expenses.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
            <TrendingDown size={40} className="mx-auto text-slate-600 mb-4" />
            <p className="font-semibold text-lg text-slate-400 mb-1">No expenses recorded</p>
            <p className="text-sm">Log manual company or warehouse expenses to audit overheads.</p>
          </div>
        ) : (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl max-w-4xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    <th className="p-4 pl-6">Description</th>
                    <th className="p-4">Category</th>
                    <th className="p-4">Date</th>
                    <th className="p-4 text-right">Amount</th>
                    <th className="p-4 text-right pr-6">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-sm">
                  {expenses.map((exp) => (
                    <tr key={exp.id} className="text-slate-350 hover:bg-slate-850/30 transition-colors">
                      <td className="p-4 pl-6">
                        <div className="font-bold text-white text-base">{exp.description}</div>
                        {exp.reference && <div className="text-[10px] text-slate-500 mt-0.5">Ref: {exp.reference}</div>}
                      </td>
                      <td className="p-4">
                        <span className="bg-rose-500/10 text-rose-400 border border-rose-500/20 text-xs font-semibold px-2 py-0.5 rounded-full">
                          {exp.category}
                        </span>
                      </td>
                      <td className="p-4 text-slate-400">{exp.date}</td>
                      <td className="p-4 text-right font-extrabold text-rose-400">-${exp.amount.toFixed(2)}</td>
                      <td className="p-4 text-right pr-6">
                        <div className="flex justify-end gap-2">
                          <button
                            onClick={() => handleOpenEditModal(exp, 'expense')}
                            className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-white rounded-lg transition-colors cursor-pointer"
                          >
                            <Edit3 size={16} />
                          </button>
                          <button
                            onClick={() => handleDelete(exp.id, 'expense')}
                            className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-rose-400 rounded-lg transition-colors cursor-pointer"
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      )}

      {activeTab === 'incomes' && (
        
        incomes.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
            <TrendingUp size={40} className="mx-auto text-slate-600 mb-4" />
            <p className="font-semibold text-lg text-slate-400 mb-1">No other incomes logged</p>
            <p className="text-sm">Log manual incomes (e.g. rent, bank interest) to compute net cashflows.</p>
          </div>
        ) : (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl max-w-4xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    <th className="p-4 pl-6">Description</th>
                    <th className="p-4">Category</th>
                    <th className="p-4">Date</th>
                    <th className="p-4 text-right">Amount</th>
                    <th className="p-4 text-right pr-6">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-sm">
                  {incomes.map((inc) => (
                    <tr key={inc.id} className="text-slate-350 hover:bg-slate-850/30 transition-colors">
                      <td className="p-4 pl-6">
                        <div className="font-bold text-white text-base">{inc.description}</div>
                        {inc.reference && <div className="text-[10px] text-slate-500 mt-0.5">Ref: {inc.reference}</div>}
                      </td>
                      <td className="p-4">
                        <span className="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs font-semibold px-2 py-0.5 rounded-full">
                          {inc.category}
                        </span>
                      </td>
                      <td className="p-4 text-slate-400">{inc.date}</td>
                      <td className="p-4 text-right font-extrabold text-emerald-400">+${inc.amount.toFixed(2)}</td>
                      <td className="p-4 text-right pr-6">
                        <div className="flex justify-end gap-2">
                          <button
                            onClick={() => handleOpenEditModal(inc, 'income')}
                            className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-white rounded-lg transition-colors cursor-pointer"
                          >
                            <Edit3 size={16} />
                          </button>
                          <button
                            onClick={() => handleDelete(inc.id, 'income')}
                            className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-rose-400 rounded-lg transition-colors cursor-pointer"
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      )}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingId ? `Edit ${modalType} Entry` : `Record ${modalType} Entry`}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Description <span className="text-indigo-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Office rent for June, Bank credit"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-650 focus:outline-none focus:border-indigo-500 text-sm"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Category <span className="text-indigo-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g. Utilities, Salaries, Sales"
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-650 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Date
              </label>
              <input
                type="date"
                required
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm font-semibold"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Amount ($) <span className="text-indigo-500">*</span>
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={amount}
                onChange={(e) => setAmount(Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm font-semibold"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Reference ID
              </label>
              <input
                type="text"
                placeholder="Receipt / invoice number"
                value={reference}
                onChange={(e) => setReference(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-650 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Entry Attachment / Receipt
            </label>
            <input
              type="file"
              onChange={(e) => {
                if (e.target.files && e.target.files.length > 0) {
                  setSelectedFile(e.target.files[0]);
                }
              }}
              className="w-full text-sm text-slate-400 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-indigo-600/10 file:text-indigo-400 hover:file:bg-indigo-600/20 file:cursor-pointer"
            />
            {entryFileUrl && (
              <div className="mt-2 text-xs text-indigo-400">
                <a href={entryFileUrl} target="_blank" rel="noreferrer" className="underline font-medium">View Receipt Attachment</a>
              </div>
            )}
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
              {editingId ? 'Save Changes' : 'Record Entry'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
