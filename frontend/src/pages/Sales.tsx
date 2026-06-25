import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Search, ShoppingCart, Trash2, Plus, Minus, Check, Users, DollarSign, Receipt } from 'lucide-react';
import { Modal } from '../components/Modal';

interface Customer {
  id: string;
  name: string;
  phone: string;
}

interface Product {
  id: string;
  name: string;
  sku: string;
  sellingPrice: number;
  quantity: number;
}

interface CartItem {
  product: Product;
  quantity: number;
  discountPercentage: number;
}

export const Sales: React.FC = () => {
  const { user } = useAuth();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [cart, setCart] = useState<CartItem[]>([]);
  
  
  const [prodSearch, setProdSearch] = useState('');
  const [custSearch, setCustSearch] = useState('');
  
  
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  
  
  const [subTotal, setSubTotal] = useState(0);
  const [totalDiscount, setTotalDiscount] = useState(0);
  const [tax, setTax] = useState(0);
  const [grandTotal, setGrandTotal] = useState(0);

  
  const [isPayModalOpen, setIsPayModalOpen] = useState(false);
  const [amountPaid, setAmountPaid] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD' | 'MOBILE'>('CASH');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) return;
    const fetchSalesData = async () => {
      try {
        const [custData, prodData] = await Promise.all([
          apiFetch(`/api/customers/${user.tenantId}`).catch(() => []),
          apiFetch(`/api/products/${user.tenantId}`).catch(() => [])
        ]);
        setCustomers(custData || []);
        setProducts(prodData || []);
      } catch (e) {
        console.error('Failed to load customers and products', e);
      }
    };
    fetchSalesData();
  }, [user]);

  
  useEffect(() => {
    let sub = 0;
    let disc = 0;
    cart.forEach((item) => {
      const lineCost = item.product.sellingPrice * item.quantity;
      sub += lineCost;
      disc += lineCost * (item.discountPercentage / 100);
    });

    const calculatedTax = (sub - disc) * 0.1; 
    const finalTotal = sub - disc + calculatedTax;

    setSubTotal(sub);
    setTotalDiscount(disc);
    setTax(calculatedTax);
    setGrandTotal(finalTotal);
    setAmountPaid(Number(finalTotal.toFixed(2)));
  }, [cart]);

  
  const addToCart = (product: Product) => {
    if (product.quantity <= 0) {
      alert('Product is out of stock!');
      return;
    }

    const existingIdx = cart.findIndex((item) => item.product.id === product.id);
    if (existingIdx > -1) {
      const item = cart[existingIdx];
      if (item.quantity >= product.quantity) {
        alert(`Cannot add more. Only ${product.quantity} items in stock.`);
        return;
      }
      const newCart = [...cart];
      newCart[existingIdx].quantity += 1;
      setCart(newCart);
    } else {
      setCart([...cart, { product, quantity: 1, discountPercentage: 0 }]);
    }
  };

  
  const updateQuantity = (productId: string, val: number) => {
    const existingIdx = cart.findIndex((item) => item.product.id === productId);
    if (existingIdx > -1) {
      const item = cart[existingIdx];
      const newQty = item.quantity + val;
      if (newQty <= 0) {
        setCart(cart.filter((i) => i.product.id !== productId));
      } else {
        if (newQty > item.product.quantity) {
          alert(`Cannot add more. Only ${item.product.quantity} items in stock.`);
          return;
        }
        const newCart = [...cart];
        newCart[existingIdx].quantity = newQty;
        setCart(newCart);
      }
    }
  };

  
  const removeFromCart = (productId: string) => {
    setCart(cart.filter((item) => item.product.id !== productId));
  };

  const handleOpenCheckout = () => {
    if (cart.length === 0) {
      alert('Cart is empty.');
      return;
    }
    setIsPayModalOpen(true);
  };

  const handleCheckoutSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;
    setLoading(true);

    
    const invoiceItems = cart.map((item) => ({
      productName: item.product.name,
      quantity: item.quantity,
      unitPrice: item.product.sellingPrice
    }));

    
    const invNum = `INV-${Date.now().toString().slice(-6)}`;

    const invoicePayload = {
      invoiceNumber: invNum,
      customer: selectedCustomer ? { id: selectedCustomer.id } : null,
      subTotal: Number(subTotal.toFixed(2)),
      discount: Number(totalDiscount.toFixed(2)),
      tax: Number(tax.toFixed(2)),
      total: Number(grandTotal.toFixed(2)),
      status: 'DRAFT',
      items: invoiceItems
    };

    try {
      
      const savedInvoice = await apiFetch(`/api/v1/invoices/${user.tenantId}`, {
        method: 'POST',
        body: JSON.stringify(invoicePayload),
      });

      
      await apiFetch(`/api/v1/invoices/${user.tenantId}/${savedInvoice.id}/confirm`, {
        method: 'PUT',
      });

      
      if (amountPaid > 0) {
        await apiFetch(`/api/v1/invoices/${user.tenantId}/${savedInvoice.id}/pay?amount=${amountPaid}`, {
          method: 'PUT',
        });
      }

      alert('Sale invoice finalized successfully! Loading print preview...');

      
      const token = localStorage.getItem('token');
      const pdfResponse = await fetch(`/api/v1/invoices/${user.tenantId}/${savedInvoice.id}/pdf`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      if (pdfResponse.ok) {
        const blob = await pdfResponse.blob();
        const fileURL = URL.createObjectURL(blob);
        window.open(fileURL);
      }

      setCart([]);
      setSelectedCustomer(null);
      setIsPayModalOpen(false);
      
      
      const updatedProducts = await apiFetch(`/api/products/${user.tenantId}`).catch(() => []);
      setProducts(updatedProducts || []);
    } catch (err) {
      console.error(err);
      alert('Error finalizing sale. Verify inventory stock balances.');
    } finally {
      setLoading(false);
    }
  };

  
  const filteredProducts = products.filter(
    (p) =>
      p.name.toLowerCase().includes(prodSearch.toLowerCase()) ||
      p.sku.toLowerCase().includes(prodSearch.toLowerCase())
  );

  const filteredCustomers = customers.filter(
    (c) =>
      c.name.toLowerCase().includes(custSearch.toLowerCase()) ||
      c.phone.includes(custSearch)
  );

  return (
    <div className="grid grid-cols-1 xl:grid-cols-3 gap-8 h-[calc(100vh-8rem)] animate-fade-in">
      <div className="xl:col-span-2 space-y-6 flex flex-col h-full overflow-hidden">
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg flex flex-col gap-4">
          <div className="flex items-center gap-2 text-white">
            <Users className="text-indigo-400" size={18} />
            <h3 className="font-bold text-sm uppercase tracking-wider">Customer Assignment</h3>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 items-center">
            <div className="relative flex-1 w-full">
              <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 pointer-events-none">
                <Search size={16} />
              </span>
              <input
                type="text"
                placeholder="Search registered customers..."
                value={custSearch}
                onChange={(e) => setCustSearch(e.target.value)}
                className="w-full pl-9 pr-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 text-xs"
              />
              {custSearch && (
                <div className="absolute left-0 right-0 top-full mt-1 bg-slate-950 border border-slate-800 rounded-xl shadow-2xl max-h-40 overflow-y-auto z-40 p-1 divide-y divide-slate-850">
                  {filteredCustomers.length === 0 ? (
                    <div className="p-2 text-slate-500 text-xs">No matches found</div>
                  ) : (
                    filteredCustomers.map((c) => (
                      <button
                        key={c.id}
                        onClick={() => {
                          setSelectedCustomer(c);
                          setCustSearch('');
                        }}
                        className="w-full text-left p-2 hover:bg-slate-800 text-slate-200 text-xs flex justify-between items-center cursor-pointer rounded-lg"
                      >
                        <span className="font-semibold">{c.name}</span>
                        <span className="text-slate-500 font-mono text-[10px]">{c.phone}</span>
                      </button>
                    ))
                  )}
                </div>
              )}
            </div>
            <div className="w-full sm:w-auto shrink-0 flex items-center gap-3 bg-slate-950 border border-slate-800 px-4 py-2 rounded-xl text-slate-300">
              <span className="text-xs text-slate-500">Billing:</span>
              <span className="font-bold text-white text-xs">
                {selectedCustomer ? selectedCustomer.name : 'Walk-In Customer'}
              </span>
              {selectedCustomer && (
                <button
                  onClick={() => setSelectedCustomer(null)}
                  className="text-[10px] text-rose-400 hover:text-rose-300 font-bold bg-rose-500/10 px-2 py-0.5 rounded-full cursor-pointer"
                >
                  Clear
                </button>
              )}
            </div>
          </div>
        </div>
        <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl shadow-lg flex-1 flex flex-col overflow-hidden">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3 mb-4">
            <h3 className="font-bold text-sm uppercase tracking-wider text-white">Product Catalog</h3>
            <div className="relative w-full max-w-xs">
              <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 pointer-events-none">
                <Search size={14} />
              </span>
              <input
                type="text"
                placeholder="Find catalog items..."
                value={prodSearch}
                onChange={(e) => setProdSearch(e.target.value)}
                className="w-full pl-8 pr-4 py-1.5 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 text-xs"
              />
            </div>
          </div>
          <div className="flex-1 overflow-y-auto grid grid-cols-1 md:grid-cols-2 gap-4 pr-1">
            {filteredProducts.map((prod) => (
              <div
                key={prod.id}
                onClick={() => addToCart(prod)}
                className="bg-slate-950 border border-slate-800 p-4 rounded-xl hover:border-indigo-500/50 hover:bg-slate-950/80 transition-all cursor-pointer flex flex-col justify-between hover:scale-[1.01] shadow"
              >
                <div>
                  <h4 className="font-bold text-white text-sm">{prod.name}</h4>
                  <div className="text-[10px] text-slate-500 font-mono mt-0.5">{prod.sku}</div>
                </div>
                <div className="flex justify-between items-end mt-4">
                  <div>
                    <span className="text-[10px] text-slate-500">Selling:</span>
                    <div className="text-emerald-400 font-extrabold text-sm">${prod.sellingPrice.toFixed(2)}</div>
                  </div>
                  <div className="text-right">
                    <span className="text-[10px] text-slate-500">In Stock:</span>
                    <div className={`text-xs font-semibold ${prod.quantity <= 3 ? 'text-rose-400' : 'text-slate-300'}`}>
                      {prod.quantity} units
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="bg-slate-900 border border-slate-800 rounded-2xl shadow-lg flex flex-col h-full overflow-hidden p-5">
        <div className="flex items-center gap-2 text-white border-b border-slate-800 pb-3 mb-4">
          <ShoppingCart className="text-indigo-400" size={18} />
          <h3 className="font-bold text-sm uppercase tracking-wider">Shopping Cart</h3>
        </div>
        <div className="flex-1 overflow-y-auto space-y-3 pr-1">
          {cart.length === 0 ? (
            <div className="flex flex-col items-center justify-center h-full text-slate-500 text-xs">
              <ShoppingCart size={32} className="text-slate-700 mb-2" />
              <span>Cart is empty</span>
              <span>Select catalog items on the left</span>
            </div>
          ) : (
            cart.map((item) => (
              <div key={item.product.id} className="bg-slate-950 border border-slate-850 p-3 rounded-xl flex items-center justify-between gap-3 shadow-xs">
                <div className="flex-1 min-w-0">
                  <h5 className="font-bold text-slate-200 text-xs truncate leading-snug">{item.product.name}</h5>
                  <span className="text-[10px] text-slate-400 font-semibold">${item.product.sellingPrice.toFixed(2)}</span>
                </div>
                <div className="flex items-center gap-1.5 bg-slate-900 p-1 rounded-lg border border-slate-850">
                  <button
                    onClick={() => updateQuantity(item.product.id, -1)}
                    className="p-1 text-slate-400 hover:text-white rounded hover:bg-slate-800 cursor-pointer"
                  >
                    <Minus size={10} />
                  </button>
                  <span className="text-xs font-bold text-white w-4 text-center">{item.quantity}</span>
                  <button
                    onClick={() => updateQuantity(item.product.id, 1)}
                    className="p-1 text-slate-400 hover:text-white rounded hover:bg-slate-800 cursor-pointer"
                  >
                    <Plus size={10} />
                  </button>
                </div>
                <button
                  onClick={() => removeFromCart(item.product.id)}
                  className="text-slate-500 hover:text-rose-400 p-1 rounded hover:bg-slate-800 cursor-pointer transition-colors"
                >
                  <Trash2 size={14} />
                </button>
              </div>
            ))
          )}
        </div>
        <div className="border-t border-slate-800 pt-4 mt-4 space-y-2.5">
          <div className="flex justify-between text-xs text-slate-400">
            <span>Subtotal:</span>
            <span>${subTotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-xs text-slate-400">
            <span>Discounts:</span>
            <span className="text-rose-400">-${totalDiscount.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-xs text-slate-400">
            <span>Tax (10% GST):</span>
            <span>${tax.toFixed(2)}</span>
          </div>
          <div className="flex justify-between items-center text-sm font-extrabold text-white border-t border-slate-800 pt-3">
            <span>Grand Total:</span>
            <span className="text-xl text-emerald-400 font-extrabold">${grandTotal.toFixed(2)}</span>
          </div>

          <button
            onClick={handleOpenCheckout}
            disabled={cart.length === 0}
            className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-3 px-4 rounded-xl shadow-lg shadow-indigo-500/20 cursor-pointer transition-all mt-4 text-xs flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Receipt size={14} />
            <span>Process Checkout Invoice</span>
          </button>
        </div>
        <Modal isOpen={isPayModalOpen} onClose={() => setIsPayModalOpen(false)} title="Checkout Terminal">
          <form onSubmit={handleCheckoutSubmit} className="space-y-5">
            <div className="bg-slate-950 p-5 rounded-2xl border border-slate-850 text-center">
              <span className="text-[10px] text-slate-500 font-bold uppercase tracking-wider block">Grand Invoice Total</span>
              <span className="text-3xl font-extrabold text-emerald-400 tracking-tight block mt-1">${grandTotal.toFixed(2)}</span>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">
                Payment Mode
              </label>
              <div className="grid grid-cols-3 gap-3">
                {(['CASH', 'CARD', 'MOBILE'] as const).map((method) => (
                  <button
                    key={method}
                    type="button"
                    onClick={() => setPaymentMethod(method)}
                    className={`py-2 px-3 border text-xs font-bold rounded-xl transition-all cursor-pointer flex flex-col items-center gap-1.5 ${
                      paymentMethod === method
                        ? 'border-indigo-600 bg-indigo-500/10 text-white'
                        : 'border-slate-800 bg-slate-950 text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    <span>{method}</span>
                    {paymentMethod === method && <Check size={12} className="text-indigo-400" />}
                  </button>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Amount Paid ($)
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                  <DollarSign size={16} />
                </span>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={amountPaid}
                  onChange={(e) => setAmountPaid(Number(e.target.value))}
                  className="w-full pl-9 pr-4 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm font-semibold"
                />
              </div>
            </div>
            <div className="flex justify-end gap-3 border-t border-slate-850 pt-4 mt-6">
              <button
                type="button"
                onClick={() => setIsPayModalOpen(false)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 font-semibold rounded-xl text-sm transition-colors cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={loading}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer disabled:opacity-50"
              >
                {loading ? 'Fulfilling Order...' : 'Confirm Payment'}
              </button>
            </div>
          </form>
        </Modal>
      </div>
    </div>
  );
};
