import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Plus, CheckCircle, PackageCheck, FileText, Calendar, AlertCircle } from 'lucide-react';
import { Modal } from '../components/Modal';

interface Supplier {
  id: string;
  name: string;
}

interface Product {
  id: string;
  name: string;
  sku: string;
}

interface PurchaseOrderItem {
  id?: string;
  product: Product;
  quantity: number;
  cost: number;
}

interface PurchaseOrder {
  id: string;
  poNumber: string;
  supplier: Supplier;
  totalAmount: number;
  status: 'DRAFT' | 'SUBMITTED' | 'RECEIVED' | 'CANCELLED';
  createdAt: string;
  items: PurchaseOrderItem[];
}

interface GoodsReceivedNote {
  id: string;
  grnNumber: string;
  purchaseOrderId: string;
  product: Product;
  quantityReceived: number;
  receivedDate: string;
  notes?: string;
}

export const Purchases: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'orders' | 'grns'>('orders');
  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [grns, setGrns] = useState<GoodsReceivedNote[]>([]);
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  
  const [isPoModalOpen, setIsPoModalOpen] = useState(false);
  const [isGrnModalOpen, setIsGrnModalOpen] = useState(false);

  
  const [selectedOrderForReceive, setSelectedOrderForReceive] = useState<PurchaseOrder | null>(null);

  
  const [supplierId, setSupplierId] = useState('');
  const [poItems, setPoItems] = useState<PurchaseOrderItem[]>([]);
  
  
  const [selectedProdId, setSelectedProdId] = useState('');
  const [itemQuantity, setItemQuantity] = useState(1);
  const [itemCost, setItemCost] = useState(0);

  
  const [receiveProdId, setReceiveProdId] = useState('');
  const [receiveQuantity, setReceiveQuantity] = useState(1);
  const [receiveNotes, setReceiveNotes] = useState('');

  const fetchData = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const [ordersData, grnsData, suppliersData, productsData] = await Promise.all([
        apiFetch(`/api/purchases/${user.tenantId}`).catch(() => []),
        apiFetch(`/api/purchases/${user.tenantId}/grns`).catch(() => []),
        apiFetch(`/api/suppliers/${user.tenantId}`).catch(() => []),
        apiFetch(`/api/products/${user.tenantId}`).catch(() => [])
      ]);
      setOrders(ordersData || []);
      setGrns(grnsData || []);
      setSuppliers(suppliersData || []);
      setProducts(productsData || []);
    } catch (e) {
      console.error('Failed to load purchases data', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  
  const addPoItem = () => {
    if (!selectedProdId) return;
    const prod = products.find((p) => p.id === selectedProdId);
    if (!prod) return;

    
    if (poItems.some((item) => item.product.id === selectedProdId)) {
      alert('Product already added to list.');
      return;
    }

    setPoItems([...poItems, { product: prod, quantity: itemQuantity, cost: itemCost }]);
    setSelectedProdId('');
    setItemQuantity(1);
    setItemCost(0);
  };

  
  const removePoItem = (idx: number) => {
    setPoItems(poItems.filter((_, i) => i !== idx));
  };

  
  const handlePoSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!supplierId || poItems.length === 0) {
      alert('Please select a supplier and add at least one item.');
      return;
    }

    const poNum = `PO-${Date.now().toString().slice(-6)}`;
    const totalAmount = poItems.reduce((sum, item) => sum + (item.cost * item.quantity), 0);

    const payload = {
      poNumber: poNum,
      supplier: { id: supplierId },
      totalAmount,
      status: 'DRAFT',
      items: poItems.map(item => ({
        product: { id: item.product.id },
        quantity: item.quantity,
        cost: item.cost
      }))
    };

    try {
      await apiFetch('/api/purchases', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      setIsPoModalOpen(false);
      setSupplierId('');
      setPoItems([]);
      fetchData();
    } catch (err) {
      alert('Error creating Purchase Order');
    }
  };

  
  const handlePoVendorSubmit = async (orderId: string) => {
    try {
      await apiFetch(`/api/purchases/${user?.tenantId}/${orderId}/submit`, {
        method: 'PUT'
      });
      fetchData();
    } catch (err) {
      alert('Error submitting Purchase Order');
    }
  };

  
  const handleOpenReceiveWizard = (order: PurchaseOrder) => {
    setSelectedOrderForReceive(order);
    if (order.items.length > 0) {
      setReceiveProdId(order.items[0].product.id);
      setReceiveQuantity(order.items[0].quantity);
    }
    setReceiveNotes('');
    setIsGrnModalOpen(true);
  };

  
  const handleGrnSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedOrderForReceive || !receiveProdId) return;

    const payload = {
      productId: receiveProdId,
      quantity: Number(receiveQuantity),
      notes: receiveNotes
    };

    try {
      await apiFetch(`/api/purchases/${user?.tenantId}/${selectedOrderForReceive.id}/receive`, {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      setIsGrnModalOpen(false);
      setSelectedOrderForReceive(null);
      fetchData();
    } catch (err) {
      alert('Error receiving goods. Make sure product is valid and limits match.');
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Purchasing Hub</h1>
          <p className="text-slate-400 text-sm mt-1">Manage vendor supply streams and record inventory receipt notes.</p>
        </div>

        <button
          onClick={() => {
            setSupplierId(suppliers.length > 0 ? suppliers[0].id : '');
            setPoItems([]);
            setIsPoModalOpen(true);
          }}
          disabled={suppliers.length === 0}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all cursor-pointer text-sm disabled:opacity-50"
        >
          <Plus size={18} />
          <span>New Purchase Order</span>
        </button>
      </div>
      <div className="flex border-b border-slate-800">
        <button
          onClick={() => setActiveTab('orders')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'orders'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Purchase Orders (PO)
        </button>
        <button
          onClick={() => setActiveTab('grns')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'grns'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Goods Received Notes (GRN)
        </button>
      </div>

      {loading ? (
        <div className="flex items-center justify-center min-h-[30vh]">
          <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
        </div>
      ) : activeTab === 'orders' ? (
        
        orders.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
            <FileText size={40} className="mx-auto text-slate-600 mb-4" />
            <p className="font-semibold text-lg text-slate-400 mb-1">No orders logged</p>
            <p className="text-sm">Initiate procurement requests to restock product inventory.</p>
          </div>
        ) : (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    <th className="p-4 pl-6">PO Number</th>
                    <th className="p-4">Vendor</th>
                    <th className="p-4 text-right">Order Cost</th>
                    <th className="p-4 text-center">Status</th>
                    <th className="p-4 text-right pr-6">Workflow</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-sm">
                  {orders.map((po) => (
                    <tr key={po.id} className="text-slate-350 hover:bg-slate-850/30 transition-colors">
                      <td className="p-4 pl-6 font-bold text-white font-mono">{po.poNumber}</td>
                      <td className="p-4">
                        <div className="text-slate-200 font-semibold">{po.supplier?.name}</div>
                      </td>
                      <td className="p-4 text-right font-semibold text-emerald-400">${po.totalAmount.toFixed(2)}</td>
                      <td className="p-4 text-center">
                        <span
                          className={`text-[10px] font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full ${
                            po.status === 'RECEIVED'
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : po.status === 'SUBMITTED'
                              ? 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                              : 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                          }`}
                        >
                          {po.status}
                        </span>
                      </td>
                      <td className="p-4 text-right pr-6">
                        {po.status === 'DRAFT' && (
                          <button
                            onClick={() => handlePoVendorSubmit(po.id)}
                            className="bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-1 px-3 rounded-lg text-xs cursor-pointer shadow-xs transition-colors"
                          >
                            Submit Order
                          </button>
                        )}
                        {po.status === 'SUBMITTED' && (
                          <button
                            onClick={() => handleOpenReceiveWizard(po)}
                            className="bg-emerald-600 hover:bg-emerald-500 text-white font-bold py-1 px-3 rounded-lg text-xs cursor-pointer shadow-xs transition-colors flex items-center gap-1.5 ml-auto"
                          >
                            <PackageCheck size={12} />
                            <span>Receive Goods</span>
                          </button>
                        )}
                        {po.status === 'RECEIVED' && (
                          <span className="text-xs text-slate-500 flex items-center justify-end gap-1 font-medium">
                            <CheckCircle size={12} className="text-emerald-500" />
                            <span>Fulfilled</span>
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      ) : (
        
        grns.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
            <PackageCheck size={40} className="mx-auto text-slate-600 mb-4" />
            <p className="font-semibold text-lg text-slate-400 mb-1">No goods receipted</p>
            <p className="text-sm">Receive goods on submitted POs to record stock movements here.</p>
          </div>
        ) : (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl max-w-4xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                    <th className="p-4 pl-6">GRN Log</th>
                    <th className="p-4">Product</th>
                    <th className="p-4 text-right">Qty Received</th>
                    <th className="p-4">Notes</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-850 text-sm">
                  {grns.map((grn) => (
                    <tr key={grn.id} className="text-slate-350 hover:bg-slate-850/30 transition-colors">
                      <td className="p-4 pl-6">
                        <div className="font-bold text-white font-mono">{grn.grnNumber}</div>
                        <div className="flex items-center gap-1.5 text-slate-500 text-xs mt-0.5">
                          <Calendar size={12} />
                          <span>{grn.receivedDate}</span>
                        </div>
                      </td>
                      <td className="p-4">
                        <div className="text-slate-200 font-semibold">{grn.product?.name}</div>
                        <div className="text-[10px] text-slate-500 font-mono mt-0.5">SKU: {grn.product?.sku}</div>
                      </td>
                      <td className="p-4 text-right font-bold text-emerald-400">+{grn.quantityReceived}</td>
                      <td className="p-4 text-slate-400 text-xs italic">{grn.notes || 'No comments'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      )}
      <Modal isOpen={isPoModalOpen} onClose={() => setIsPoModalOpen(false)} title="Generate Purchase Order">
        <form onSubmit={handlePoSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Select Vendor Supplier <span className="text-indigo-500">*</span>
            </label>
            <select
              value={supplierId}
              onChange={(e) => setSupplierId(e.target.value)}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
            >
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>

          <div className="border border-slate-850 bg-slate-950/60 p-4 rounded-xl space-y-4">
            <h4 className="text-xs font-bold text-indigo-400 uppercase tracking-wider">Catalog Item Builder</h4>
            
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div className="sm:col-span-2">
                <label className="block text-[10px] font-semibold text-slate-500 mb-1">Product</label>
                <select
                  value={selectedProdId}
                  onChange={(e) => setSelectedProdId(e.target.value)}
                  className="w-full px-2.5 py-1.5 bg-slate-950 border border-slate-850 rounded-lg text-slate-200 text-xs focus:outline-none"
                >
                  <option value="">Choose item...</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-[10px] font-semibold text-slate-500 mb-1">Quantity</label>
                <input
                  type="number"
                  min="1"
                  value={itemQuantity}
                  onChange={(e) => setItemQuantity(Number(e.target.value))}
                  className="w-full px-2.5 py-1.5 bg-slate-950 border border-slate-855 rounded-lg text-slate-200 text-xs"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 items-end">
              <div>
                <label className="block text-[10px] font-semibold text-slate-500 mb-1">Supply Unit Cost ($)</label>
                <input
                  type="number"
                  step="0.01"
                  value={itemCost}
                  onChange={(e) => setItemCost(Number(e.target.value))}
                  className="w-full px-2.5 py-1.5 bg-slate-950 border border-slate-855 rounded-lg text-slate-200 text-xs"
                />
              </div>
              <button
                type="button"
                onClick={addPoItem}
                className="w-full py-2 bg-indigo-600/80 hover:bg-indigo-600 text-white rounded-lg text-xs font-bold cursor-pointer transition-colors"
              >
                Add Item
              </button>
            </div>
            <div className="space-y-2 max-h-32 overflow-y-auto border-t border-slate-850 pt-3">
              {poItems.length === 0 ? (
                <div className="text-center text-[10px] text-slate-500">No items added to checklist yet.</div>
              ) : (
                poItems.map((item, idx) => (
                  <div key={idx} className="flex justify-between items-center text-xs bg-slate-950 p-2 rounded-lg border border-slate-855">
                    <div>
                      <span className="font-semibold text-slate-200">{item.product.name}</span>
                      <div className="text-[10px] text-slate-500">Qty: {item.quantity} × Cost: ${item.cost.toFixed(2)}</div>
                    </div>
                    <button
                      type="button"
                      onClick={() => removePoItem(idx)}
                      className="text-rose-400 hover:text-rose-300 font-bold px-2 py-0.5 rounded cursor-pointer hover:bg-slate-850"
                    >
                      Delete
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-850 pt-4 mt-6">
            <button
              type="button"
              onClick={() => setIsPoModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 font-semibold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={poItems.length === 0}
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer disabled:opacity-50"
            >
              Create Draft PO
            </button>
          </div>
        </form>
      </Modal>
      <Modal isOpen={isGrnModalOpen} onClose={() => setIsGrnModalOpen(false)} title="Verify Received Goods (GRN)">
        <form onSubmit={handleGrnSubmit} className="space-y-4">
          <div className="bg-amber-500/10 border border-amber-500/20 text-amber-400 p-3.5 rounded-xl text-xs flex gap-2">
            <AlertCircle size={16} className="shrink-0" />
            <div>
              <span className="font-bold block">Audited Inventory Log</span>
              <span>Confirming this receipt will automatically update the product's actual warehouse quantities.</span>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Verify Product Item
            </label>
            <select
              value={receiveProdId}
              onChange={(e) => setReceiveProdId(e.target.value)}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
            >
              {selectedOrderForReceive?.items.map((item) => (
                <option key={item.product.id} value={item.product.id}>
                  {item.product.name} (Ordered: {item.quantity})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Actual Quantity Received
            </label>
            <input
              type="number"
              min="1"
              required
              value={receiveQuantity}
              onChange={(e) => setReceiveQuantity(Number(e.target.value))}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Inspection / Shipping Notes
            </label>
            <textarea
              placeholder="e.g. Received in perfect condition, verified invoice"
              value={receiveNotes}
              onChange={(e) => setReceiveNotes(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm resize-none"
            />
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-850 pt-4 mt-6">
            <button
              type="button"
              onClick={() => setIsGrnModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 font-semibold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Confirm Receipt (Log GRN)
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
