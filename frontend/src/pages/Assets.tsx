import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Package, Plus, Search, ShieldAlert, Monitor, CheckCircle2, Wrench } from 'lucide-react';
import { Modal } from '../components/Modal';

interface Asset {
  id: string;
  name: string;
  category: string;
  serialNumber: string;
  purchaseDate: string;
  purchasePrice: number;
  status: string;
}

export const Assets: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);

  const [name, setName] = useState('');
  const [category, setCategory] = useState('');
  const [serialNumber, setSerialNumber] = useState('');
  const [purchasePrice, setPurchasePrice] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const fetchAssets = async () => {
    try {
      const data = await apiFetch('/api/assets');
      setAssets(data || []);
    } catch (e) {
      console.error('Failed to fetch assets', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAssets();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      await apiFetch('/api/assets', {
        method: 'POST',
        body: JSON.stringify({ name, category, serialNumber, purchasePrice }),
      });
      setIsModalOpen(false);
      fetchAssets();
    } catch (err: any) {
      setError(err.message || 'Failed to create asset');
    }
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'AVAILABLE':
        return <span className="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded text-xs">Available</span>;
      case 'ASSIGNED':
        return <span className="bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 px-2 py-0.5 rounded text-xs">Assigned</span>;
      case 'MAINTENANCE':
        return <span className="bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2 py-0.5 rounded text-xs">Maintenance</span>;
      case 'RETIRED':
        return <span className="bg-rose-500/10 text-rose-400 border border-rose-500/20 px-2 py-0.5 rounded text-xs">Retired</span>;
      default:
        return <span>{status}</span>;
    }
  };

  const filteredAssets = assets.filter((a) =>
    a.name.toLowerCase().includes(search.toLowerCase()) ||
    a.serialNumber.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Asset Management</h2>
          <p className="text-slate-400 text-sm mt-1">Track company hardware, devices, and physical resources</p>
        </div>
        {hasPermission(['MANAGE_ASSETS']) && (
          <button
            onClick={() => setIsModalOpen(true)}
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg flex items-center gap-2 transition-colors shadow-lg shadow-indigo-500/20"
          >
            <Plus size={20} />
            Register Asset
          </button>
        )}
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl p-4">
        <div className="relative max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={20} />
          <input
            type="text"
            placeholder="Search by asset name or serial..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-950 border border-slate-800 rounded-lg pl-10 pr-4 py-2 text-white focus:outline-none focus:border-indigo-500"
          />
        </div>

        <div className="mt-6 overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-800 text-slate-400 text-sm uppercase tracking-wider">
                <th className="pb-3 font-medium pl-4">Asset Info</th>
                <th className="pb-3 font-medium">Category</th>
                <th className="pb-3 font-medium">Serial #</th>
                <th className="pb-3 font-medium">Purchase Value</th>
                <th className="pb-3 font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="text-sm">
              {filteredAssets.map((asset) => (
                <tr key={asset.id} className="border-b border-slate-800/50 hover:bg-slate-800/20 transition-colors">
                  <td className="py-4 pl-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-lg bg-slate-800 flex items-center justify-center text-slate-400">
                        <Monitor size={20} />
                      </div>
                      <div>
                        <div className="font-medium text-slate-200">{asset.name}</div>
                        <div className="text-xs text-slate-500">ID: {asset.id.slice(0, 8)}</div>
                      </div>
                    </div>
                  </td>
                  <td className="py-4 text-slate-300">{asset.category}</td>
                  <td className="py-4 text-slate-300 font-mono text-xs">{asset.serialNumber}</td>
                  <td className="py-4 text-slate-300">${asset.purchasePrice?.toFixed(2)}</td>
                  <td className="py-4">{getStatusBadge(asset.status)}</td>
                </tr>
              ))}
              {filteredAssets.length === 0 && !loading && (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-slate-500">
                    No assets found matching your criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Register New Asset">
        <form onSubmit={handleSubmit} className="space-y-4 mt-4">
          {error && (
            <div className="bg-rose-500/10 border border-rose-500/20 text-rose-400 p-3 rounded-lg text-sm flex gap-2">
              <ShieldAlert size={18} />
              {error}
            </div>
          )}
          
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1">Asset Name</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2 text-white focus:border-indigo-500"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">Category</label>
              <input
                type="text"
                required
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2 text-white focus:border-indigo-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-1">Serial Number</label>
              <input
                type="text"
                required
                value={serialNumber}
                onChange={(e) => setSerialNumber(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2 text-white focus:border-indigo-500"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-300 mb-1">Purchase Price ($)</label>
            <input
              type="number"
              step="0.01"
              required
              value={purchasePrice}
              onChange={(e) => setPurchasePrice(parseFloat(e.target.value))}
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2 text-white focus:border-indigo-500"
            />
          </div>

          <div className="pt-4 flex justify-end gap-3">
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="px-4 py-2 rounded-lg text-slate-300 hover:text-white transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-2 rounded-lg transition-colors shadow-lg shadow-indigo-500/20"
            >
              Register Asset
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
