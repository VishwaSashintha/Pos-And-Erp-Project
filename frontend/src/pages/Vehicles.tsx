import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Plus, Search, Edit3, Trash2, Car, Calendar, User, Settings } from 'lucide-react';
import { Modal } from '../components/Modal';

interface Customer {
  id: string;
  name: string;
}

interface Vehicle {
  id: string;
  vehicleNumber: string;
  brand: string;
  model: string;
  color: string;
  fuelType: string;
  yearOfManufacture?: number;
  chassisNumber?: string;
  engineNumber?: string;
  lastServiceDate?: string;
  nextServiceDue?: string;
  mileage?: number;
  customer: Customer;
}

export const Vehicles: React.FC = () => {
  const { user } = useAuth();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  
  const [editingId, setEditingId] = useState<string | null>(null);

  const [vehicleNumber, setVehicleNumber] = useState('');
  const [brand, setBrand] = useState('');
  const [model, setModel] = useState('');
  const [color, setColor] = useState('');
  const [fuelType, setFuelType] = useState('');
  const [mileage, setMileage] = useState<number | ''>('');
  const [customerId, setCustomerId] = useState('');

  const fetchData = async () => {
    if (!user) return;
    try {
      const [vehData, custData] = await Promise.all([
        apiFetch(`/api/vehicles/${user.tenantId}`),
        apiFetch(`/api/customers/${user.tenantId}`)
      ]);
      setVehicles(vehData || []);
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
    setBrand('');
    setModel('');
    setColor('');
    setFuelType('');
    setMileage('');
    setCustomerId('');
  };

  const handleOpenCreateModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (v: Vehicle) => {
    setEditingId(v.id);
    setVehicleNumber(v.vehicleNumber);
    setBrand(v.brand || '');
    setModel(v.model || '');
    setColor(v.color || '');
    setFuelType(v.fuelType || '');
    setMileage(v.mileage || '');
    setCustomerId(v.customer?.id || '');
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!vehicleNumber || !customerId) return;

    const body = {
      vehicleNumber,
      brand,
      model,
      color,
      fuelType,
      mileage: mileage === '' ? null : Number(mileage),
      customer: { id: customerId }
    };

    try {
      if (editingId) {
        await apiFetch(`/api/vehicles/${user?.tenantId}/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(body),
        });
      } else {
        await apiFetch('/api/vehicles', {
          method: 'POST',
          body: JSON.stringify({ ...body, tenant: { id: user?.tenantId } }),
        });
      }
      setIsModalOpen(false);
      resetForm();
      fetchData();
    } catch (err) {
      alert('Error saving vehicle data');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this vehicle?')) return;
    try {
      await apiFetch(`/api/vehicles/${user?.tenantId}/${id}`, {
        method: 'DELETE',
      });
      fetchData();
    } catch (e) {
      alert('Failed to delete vehicle');
    }
  };

  const filteredVehicles = vehicles.filter(
    (v) =>
      v.vehicleNumber.toLowerCase().includes(search.toLowerCase()) ||
      (v.brand && v.brand.toLowerCase().includes(search.toLowerCase())) ||
      (v.customer?.name && v.customer.name.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Vehicles Database</h1>
          <p className="text-slate-400 text-sm mt-1">Manage customer vehicles and track service history.</p>
        </div>
        <button
          onClick={handleOpenCreateModal}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all cursor-pointer text-sm"
        >
          <Plus size={18} />
          <span>Add Vehicle</span>
        </button>
      </div>
      <div className="relative max-w-md">
        <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 pointer-events-none">
          <Search size={18} />
        </span>
        <input
          type="text"
          placeholder="Search by vehicle number, brand, or owner..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
        />
      </div>

      {loading ? (
        <div className="flex items-center justify-center min-h-[30vh]">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : filteredVehicles.length === 0 ? (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
          <Car size={40} className="mx-auto text-slate-600 mb-4" />
          <p className="font-semibold text-lg text-slate-400 mb-1">No vehicles found</p>
          <p className="text-sm">Try modifying your search query or register a new vehicle.</p>
        </div>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                  <th className="p-4 pl-6">Registration No</th>
                  <th className="p-4">Vehicle Details</th>
                  <th className="p-4">Owner</th>
                  <th className="p-4">Service Info</th>
                  <th className="p-4 text-right pr-6">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-850 text-sm">
                {filteredVehicles.map((v) => (
                  <tr key={v.id} className="text-slate-300 hover:bg-slate-850/30 transition-colors">
                    <td className="p-4 pl-6">
                      <div className="font-bold text-white text-base tracking-wider bg-slate-800 inline-block px-3 py-1 rounded-md border border-slate-700">
                        {v.vehicleNumber}
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="font-semibold text-slate-200">{v.brand || 'Unknown'} {v.model || ''}</div>
                      <div className="flex items-center gap-1.5 text-slate-500 text-xs mt-1">
                        <span>{v.color || 'No color'}</span>
                        <span className="text-slate-700">•</span>
                        <span>{v.fuelType || 'No fuel type'}</span>
                      </div>
                    </td>
                    <td className="p-4">
                      <div className="flex items-center gap-2 text-slate-300">
                        <div className="w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center text-indigo-400 shrink-0">
                          <User size={12} />
                        </div>
                        <span className="truncate">{v.customer?.name || 'Unknown'}</span>
                      </div>
                    </td>
                    <td className="p-4 text-xs text-slate-400">
                      <div className="flex items-center gap-1.5 mb-1">
                        <Settings size={12} className="text-slate-500"/>
                        <span>Mileage: {v.mileage ? `${v.mileage.toLocaleString()} km` : 'N/A'}</span>
                      </div>
                      <div className="flex items-center gap-1.5">
                        <Calendar size={12} className="text-slate-500"/>
                        <span>Due: {v.nextServiceDue || 'Not Scheduled'}</span>
                      </div>
                    </td>
                    <td className="p-4 text-right pr-6">
                      <div className="flex justify-end gap-2">
                        <button
                          onClick={() => handleOpenEditModal(v)}
                          className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-white rounded-lg transition-colors cursor-pointer"
                        >
                          <Edit3 size={16} />
                        </button>
                        <button
                          onClick={() => handleDelete(v.id)}
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
      )}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingId ? 'Edit Vehicle Details' : 'Register New Vehicle'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Registration Number <span className="text-indigo-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. WP CAA-1234"
              value={vehicleNumber}
              onChange={(e) => setVehicleNumber(e.target.value)}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm font-mono"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Brand
              </label>
              <input
                type="text"
                placeholder="e.g. Toyota"
                value={brand}
                onChange={(e) => setBrand(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Model
              </label>
              <input
                type="text"
                placeholder="e.g. Corolla"
                value={model}
                onChange={(e) => setModel(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Color
              </label>
              <input
                type="text"
                placeholder="e.g. Pearl White"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Fuel Type
              </label>
              <select
                value={fuelType}
                onChange={(e) => setFuelType(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
              >
                <option value="">Select...</option>
                <option value="PETROL">Petrol</option>
                <option value="DIESEL">Diesel</option>
                <option value="HYBRID">Hybrid</option>
                <option value="EV">Electric</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Owner / Customer <span className="text-indigo-500">*</span>
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
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Current Mileage (km)
              </label>
              <input
                type="number"
                placeholder="e.g. 45000"
                value={mileage}
                onChange={(e) => setMileage(e.target.value === '' ? '' : Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
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
              {editingId ? 'Save Changes' : 'Register Vehicle'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
