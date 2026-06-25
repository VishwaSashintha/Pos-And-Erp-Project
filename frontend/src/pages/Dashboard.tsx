import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import {
  TrendingUp,
  Users,
  AlertTriangle,
  ShoppingCart,
  ArrowRight,
  TrendingDown,
  DollarSign
} from 'lucide-react';
import { Link } from 'react-router-dom';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface Invoice {
  id: string;
  invoiceNumber: string;
  customer?: { name: string };
  total: number;
  paidAmount: number;
  status: string;
  createdAt: string;
}

interface Product {
  id: string;
  name: string;
  sku: string;
  quantity: number;
  reorderLevel: number;
}

interface PLReport {
  totalIncomes: number;
  totalExpenses: number;
  totalSales: number;
  totalSalesPaid: number;
  totalRevenue: number;
  netProfit: number;
}

export const Dashboard: React.FC = () => {
  const { user } = useAuth();
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [customersCount, setCustomersCount] = useState(0);
  const [lowStockProducts, setLowStockProducts] = useState<Product[]>([]);
  const [plReport, setPlReport] = useState<PLReport | null>(null);
  const [salesChartData, setSalesChartData] = useState<{ month: string; total: number }[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    const fetchDashboardData = async () => {
      try {
        const [invoicesData, customersData, , lowStockData, plReportData] = await Promise.all([
          apiFetch(`/api/v1/invoices/${user.tenantId}`).catch(() => []),
          apiFetch(`/api/customers/${user.tenantId}`).catch(() => []),
          apiFetch(`/api/products/${user.tenantId}`).catch(() => []),
          apiFetch(`/api/products/${user.tenantId}/low-stock`).catch(() => []),
          apiFetch(`/api/finance/reports/profit-loss/${user.tenantId}`).catch(() => null)
        ]);

        setInvoices(invoicesData.slice(0, 5)); 
        setCustomersCount(customersData.length || 0);
        setLowStockProducts(lowStockData || []);
        setPlReport(plReportData);
        
        const chartData = (invoicesData as any[]).reduce((acc: { month: string; total: number }[], inv: any) => {
          const date = new Date(inv.createdAt);
          const month = date.toLocaleString('default', { month: 'short', year: 'numeric' });
          const existing = acc.find((d: { month: string; total: number }) => d.month === month);
          if (existing) {
            existing.total += inv.total;
          } else {
            acc.push({ month, total: inv.total });
          }
          return acc;
        }, [] as { month: string; total: number }[]);
        setSalesChartData(chartData);
      } catch (e) {
        console.error('Failed to load dashboard data', e);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [user]);

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  
  const formatCurrency = (value: number = 0) => {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
  };

  const statCards = [
    {
      title: 'Net Profit',
      value: formatCurrency(plReport?.netProfit),
      icon: DollarSign,
      color: 'from-emerald-500/20 to-teal-500/20 text-emerald-400 border-emerald-500/30',
      description: 'Total revenue minus expenses'
    },
    {
      title: 'Sales Revenue',
      value: formatCurrency(plReport?.totalSales),
      icon: TrendingUp,
      color: 'from-indigo-500/20 to-purple-500/20 text-indigo-400 border-indigo-500/30',
      description: 'Invoice totals'
    },
    {
      title: 'Total Expenses',
      value: formatCurrency(plReport?.totalExpenses),
      icon: TrendingDown,
      color: 'from-rose-500/20 to-orange-500/20 text-rose-400 border-rose-500/30',
      description: 'Manual and catalog expenses'
    },
    {
      title: 'Customers',
      value: customersCount.toString(),
      icon: Users,
      color: 'from-sky-500/20 to-blue-500/20 text-sky-400 border-sky-500/30',
      description: 'Registered client entities'
    }
  ];

  return (
    <div className="space-y-8 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold text-white tracking-tight">System Dashboard</h1>
        <p className="text-slate-400 text-sm mt-1">Real-time modular monolith intelligence overview.</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((card, idx) => (
          <div
            key={idx}
            className={`bg-slate-900 border rounded-2xl p-6 flex flex-col justify-between shadow-xl relative overflow-hidden bg-gradient-to-br ${card.color.split(' ')[0]} ${card.color.split(' ')[1]} ${card.color.split(' ')[3]}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-slate-400 text-xs font-semibold uppercase tracking-wider">{card.title}</p>
                <h3 className="text-3xl font-extrabold text-white mt-2 tracking-tight">{card.value}</h3>
              </div>
              <div className="bg-slate-950/40 p-2.5 rounded-xl text-slate-100">
                <card.icon size={20} className={card.color.split(' ')[2]} />
              </div>
            </div>
            <p className="text-slate-500 text-xs mt-4 font-medium">{card.description}</p>
          </div>
        ))}
      </div>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
        <h3 className="font-bold text-lg text-white mb-4">Sales Over Time</h3>
        <ResponsiveContainer width="100%" height={220}>
          <AreaChart data={salesChartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#6366F1" stopOpacity={0.8} />
                <stop offset="95%" stopColor="#6366F1" stopOpacity={0} />
              </linearGradient>
            </defs>
            <XAxis dataKey="month" stroke="#a1a1aa" tick={{ fontSize: 11 }} />
            <YAxis stroke="#a1a1aa" tick={{ fontSize: 11 }} />
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <Tooltip
              contentStyle={{ backgroundColor: '#0f172a', border: '1px solid #334155', borderRadius: '12px', fontSize: '12px' }}
              labelStyle={{ color: '#e2e8f0' }}
              itemStyle={{ color: '#818cf8' }}
            />
            <Area type="monotone" dataKey="total" stroke="#6366F1" fillOpacity={1} fill="url(#colorSales)" />
          </AreaChart>
        </ResponsiveContainer>
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
          <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-4">
            <div className="flex items-center gap-2">
              <AlertTriangle className="text-amber-400" size={20} />
              <h3 className="font-bold text-lg text-white">Low Stock Alerts</h3>
            </div>
            <span className="bg-amber-500/10 text-amber-400 text-xs px-2.5 py-1 rounded-full font-semibold border border-amber-500/20">
              {lowStockProducts.length} Items
            </span>
          </div>

          {lowStockProducts.length === 0 ? (
            <div className="text-center py-10 text-slate-500 text-sm">
              No low stock alerts! All products are well stocked.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    <th className="pb-3">Product</th>
                    <th className="pb-3">SKU</th>
                    <th className="pb-3 text-right">In Stock</th>
                    <th className="pb-3 text-right">Limit</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-sm">
                  {lowStockProducts.slice(0, 5).map((prod) => (
                    <tr key={prod.id} className="text-slate-350 hover:bg-slate-850/40 transition-colors">
                      <td className="py-3 font-medium text-slate-200">{prod.name}</td>
                      <td className="py-3 font-mono text-xs">{prod.sku}</td>
                      <td className="py-3 text-right text-rose-400 font-bold">{prod.quantity}</td>
                      <td className="py-3 text-right text-slate-500">{prod.reorderLevel}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {lowStockProducts.length > 5 && (
                <div className="mt-4 flex justify-end">
                  <Link
                    to="/products"
                    className="text-xs text-indigo-400 hover:text-indigo-350 font-semibold flex items-center gap-1 hover:gap-1.5 transition-all"
                  >
                    <span>View all low stock items</span>
                    <ArrowRight size={14} />
                  </Link>
                </div>
              )}
            </div>
          )}
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
          <div className="flex items-center justify-between border-b border-slate-800 pb-4 mb-4">
            <div className="flex items-center gap-2">
              <ShoppingCart className="text-indigo-400" size={20} />
              <h3 className="font-bold text-lg text-white">Recent Sales</h3>
            </div>
            <Link
              to="/sales"
              className="text-xs text-indigo-400 hover:text-indigo-350 font-semibold flex items-center gap-1 hover:gap-1.5 transition-all"
            >
              <span>POS Terminals</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          {invoices.length === 0 ? (
            <div className="text-center py-10 text-slate-500 text-sm">
              No sales completed yet. Expose POS invoices to generate statistics.
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    <th className="pb-3">Invoice</th>
                    <th className="pb-3">Customer</th>
                    <th className="pb-3 text-right">Total</th>
                    <th className="pb-3 text-center">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-sm">
                  {invoices.map((inv) => (
                    <tr key={inv.id} className="text-slate-350 hover:bg-slate-850/40 transition-colors">
                      <td className="py-3 font-semibold text-slate-200">{inv.invoiceNumber}</td>
                      <td className="py-3 text-slate-300 truncate max-w-[120px]">{inv.customer?.name || 'Walk-In'}</td>
                      <td className="py-3 text-right font-bold text-white">{formatCurrency(inv.total)}</td>
                      <td className="py-3 text-center">
                        <span
                          className={`text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-full ${
                            inv.status === 'PAID'
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                          }`}
                        >
                          {inv.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
