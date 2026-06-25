import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { Plus, Search, Edit3, Trash2, Tag, AlertTriangle, Layers } from 'lucide-react';
import { Modal } from '../components/Modal';

interface Category {
  id: string;
  name: string;
  description?: string;
}

interface Product {
  id: string;
  name: string;
  sku: string;
  barcode?: string;
  sellingPrice: number;
  costPrice: number;
  quantity: number;
  reorderLevel: number;
  category?: Category;
}

export const Products: React.FC = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'products' | 'categories'>('products');
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  
  const [isProdModalOpen, setIsProdModalOpen] = useState(false);
  const [isCatModalOpen, setIsCatModalOpen] = useState(false);

  
  const [editingProdId, setEditingProdId] = useState<string | null>(null);

  
  const [prodName, setProdName] = useState('');
  const [prodSku, setProdSku] = useState('');
  const [prodBarcode, setProdBarcode] = useState('');
  const [prodSellingPrice, setProdSellingPrice] = useState(0);
  const [prodCostPrice, setProdCostPrice] = useState(0);
  const [prodQuantity, setProdQuantity] = useState(0);
  const [prodReorderLevel, setProdReorderLevel] = useState(5);
  const [prodCategoryId, setProdCategoryId] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [productFileUrl, setProductFileUrl] = useState<string | null>(null);

  
  const [catName, setCatName] = useState('');
  const [catDesc, setCatDesc] = useState('');

  const fetchData = async () => {
    if (!user) return;
    setLoading(true);
    try {
      const [productsData, categoriesData] = await Promise.all([
        apiFetch(`/api/products/${user.tenantId}`).catch(() => []),
        apiFetch(`/api/categories/${user.tenantId}`).catch(() => [])
      ]);
      setProducts(productsData || []);
      setCategories(categoriesData || []);
    } catch (e) {
      console.error('Failed to load data', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  
  const resetProdForm = () => {
    setEditingProdId(null);
    setProdName('');
    setProdSku('');
    setProdBarcode('');
    setProdSellingPrice(0);
    setProdCostPrice(0);
    setProdQuantity(0);
    setProdReorderLevel(5);
    setProdCategoryId('');
  };

  const handleOpenProdCreate = () => {
    resetProdForm();
    setSelectedFile(null);
    setProductFileUrl(null);
    if (categories.length > 0) {
      setProdCategoryId(categories[0].id);
    }
    setIsProdModalOpen(true);
  };

  const handleOpenProdEdit = async (prod: Product) => {
    setEditingProdId(prod.id);
    setProdName(prod.name);
    setProdSku(prod.sku);
    setProdBarcode(prod.barcode || '');
    setProdSellingPrice(prod.sellingPrice);
    setProdCostPrice(prod.costPrice);
    setProdQuantity(prod.quantity);
    setProdReorderLevel(prod.reorderLevel);
    setProdCategoryId(prod.category?.id || '');
    setSelectedFile(null);
    setProductFileUrl(null);
    setIsProdModalOpen(true);

    try {
      const files = await apiFetch(`/api/files/${user?.tenantId}?module=PRODUCT&referenceId=${prod.id}`);
      if (files && files.length > 0) {
        setProductFileUrl(files[0].fileUrl);
      }
    } catch (e) {
      console.error('Failed to load product image', e);
    }
  };

  const handleProdSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!prodName || !prodSku) return;

    const body = {
      name: prodName,
      sku: prodSku,
      barcode: prodBarcode || null,
      sellingPrice: Number(prodSellingPrice),
      costPrice: Number(prodCostPrice),
      quantity: Number(prodQuantity),
      reorderLevel: Number(prodReorderLevel),
      category: prodCategoryId ? { id: prodCategoryId } : null
    };

    try {
      let savedProduct;
      if (editingProdId) {
        savedProduct = await apiFetch(`/api/products/${user?.tenantId}/${editingProdId}`, {
          method: 'PUT',
          body: JSON.stringify(body),
        });
      } else {
        savedProduct = await apiFetch('/api/products', {
          method: 'POST',
          body: JSON.stringify(body),
        });
      }

      if (selectedFile && savedProduct && savedProduct.id) {
        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('module', 'PRODUCT');
        formData.append('referenceId', savedProduct.id);

        await fetch('/api/files/upload', {
          method: 'POST',
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token'),
            'tenantId': user?.tenantId || '',
          },
          body: formData,
        });
      }

      setIsProdModalOpen(false);
      resetProdForm();
      fetchData();
    } catch (err) {
      alert('Error saving product');
    }
  };

  const handleProdDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return;
    try {
      await apiFetch(`/api/products/${user?.tenantId}/${id}`, {
        method: 'DELETE',
      });
      fetchData();
    } catch (e) {
      alert('Failed to delete product');
    }
  };

  
  const handleCatSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!catName) return;

    try {
      await apiFetch('/api/categories', {
        method: 'POST',
        body: JSON.stringify({ name: catName, description: catDesc }),
      });
      setIsCatModalOpen(false);
      setCatName('');
      setCatDesc('');
      fetchData();
    } catch (err) {
      alert('Error creating category');
    }
  };

  const handleCatDelete = async (id: string) => {
    if (!window.confirm('Are you sure you want to delete this category? Products associated with it might be unassigned.')) return;
    try {
      await apiFetch(`/api/categories/${user?.tenantId}/${id}`, {
        method: 'DELETE',
      });
      fetchData();
    } catch (e) {
      alert('Failed to delete category');
    }
  };

  const filteredProducts = products.filter(
    (p) =>
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      p.sku.toLowerCase().includes(search.toLowerCase()) ||
      (p.barcode && p.barcode.includes(search))
  );

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight">Inventory Catalog</h1>
          <p className="text-slate-400 text-sm mt-1">Manage tenant warehouse items, categories, and alerts.</p>
        </div>

        <div className="flex gap-3">
          <button
            onClick={() => setIsCatModalOpen(true)}
            className="flex items-center gap-2 bg-slate-800 hover:bg-slate-750 text-slate-200 font-semibold py-2.5 px-4 rounded-xl border border-slate-700 transition-all cursor-pointer text-sm"
          >
            <Layers size={18} />
            <span>New Category</span>
          </button>
          <button
            onClick={handleOpenProdCreate}
            className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-2.5 px-4 rounded-xl shadow-lg shadow-indigo-500/20 transition-all cursor-pointer text-sm"
          >
            <Plus size={18} />
            <span>Add Product</span>
          </button>
        </div>
      </div>
      <div className="flex border-b border-slate-800">
        <button
          onClick={() => setActiveTab('products')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'products'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Products
        </button>
        <button
          onClick={() => setActiveTab('categories')}
          className={`py-3 px-6 font-semibold border-b-2 text-sm transition-all cursor-pointer ${
            activeTab === 'categories'
              ? 'border-indigo-500 text-white'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Categories
        </button>
      </div>

      {activeTab === 'products' ? (
        
        <>
          <div className="relative max-w-md">
            <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500 pointer-events-none">
              <Search size={18} />
            </span>
            <input
              type="text"
              placeholder="Search products by name, SKU, or barcode..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 bg-slate-900 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
            />
          </div>

          {loading ? (
            <div className="flex items-center justify-center min-h-[30vh]">
              <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : filteredProducts.length === 0 ? (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
              <Plus size={40} className="mx-auto text-slate-600 mb-4" />
              <p className="font-semibold text-lg text-slate-400 mb-1">No products registered</p>
              <p className="text-sm">Create catalog products to enable POS transaction invoices.</p>
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                      <th className="p-4 pl-6">Product details</th>
                      <th className="p-4">SKU / Barcode</th>
                      <th className="p-4">Category</th>
                      <th className="p-4 text-right">Pricing</th>
                      <th className="p-4 text-right">In Stock</th>
                      <th className="p-4 text-right pr-6">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-850 text-sm">
                    {filteredProducts.map((p) => {
                      const isLowStock = p.quantity <= p.reorderLevel;
                      return (
                        <tr key={p.id} className="text-slate-350 hover:bg-slate-850/30 transition-colors">
                          <td className="p-4 pl-6">
                            <div className="font-bold text-white text-base">{p.name}</div>
                          </td>
                          <td className="p-4">
                            <div className="font-mono text-xs text-slate-300">{p.sku}</div>
                            {p.barcode && <div className="text-[10px] text-slate-500 mt-0.5">BC: {p.barcode}</div>}
                          </td>
                          <td className="p-4">
                            <span className="inline-flex items-center gap-1 text-slate-350 bg-slate-800 px-2 py-0.5 rounded-full text-xs border border-slate-700">
                              <Tag size={10} className="text-indigo-400" />
                              <span>{p.category?.name || 'Unassigned'}</span>
                            </span>
                          </td>
                          <td className="p-4 text-right">
                            <div className="text-white font-semibold">${p.sellingPrice.toFixed(2)}</div>
                            <div className="text-xs text-slate-500">Cost: ${p.costPrice.toFixed(2)}</div>
                          </td>
                          <td className="p-4 text-right">
                            <div className="flex items-center justify-end gap-1.5 font-bold">
                              <span className={isLowStock ? 'text-rose-400' : 'text-slate-200'}>{p.quantity}</span>
                              {isLowStock && (
                                <span title="Low Stock Alert">
                                  <AlertTriangle size={14} className="text-amber-400" />
                                </span>
                              )}
                            </div>
                            <div className="text-[10px] text-slate-500">Reorder limit: {p.reorderLevel}</div>
                          </td>
                          <td className="p-4 text-right pr-6">
                            <div className="flex justify-end gap-2">
                              <button
                                onClick={() => handleOpenProdEdit(p)}
                                className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-white rounded-lg transition-colors cursor-pointer"
                              >
                                <Edit3 size={16} />
                              </button>
                              <button
                                onClick={() => handleProdDelete(p.id)}
                                className="p-1.5 hover:bg-slate-800 text-slate-400 hover:text-rose-400 rounded-lg transition-colors cursor-pointer"
                              >
                                <Trash2 size={16} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      ) : (
        
        <>
          {loading ? (
            <div className="flex items-center justify-center min-h-[30vh]">
              <div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
            </div>
          ) : categories.length === 0 ? (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 shadow-xl">
              <Layers size={40} className="mx-auto text-slate-600 mb-4" />
              <p className="font-semibold text-lg text-slate-400 mb-1">No categories configured</p>
              <p className="text-sm">Categories help group products for reports and searches.</p>
            </div>
          ) : (
            <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl max-w-2xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-slate-800 bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                      <th className="p-4 pl-6">Category Name</th>
                      <th className="p-4">Description</th>
                      <th className="p-4 text-right pr-6">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-850 text-sm">
                    {categories.map((c) => (
                      <tr key={c.id} className="text-slate-350 hover:bg-slate-850/30 transition-colors">
                        <td className="p-4 pl-6 font-bold text-white text-base">{c.name}</td>
                        <td className="p-4 text-slate-400 text-xs">{c.description || 'No description provided'}</td>
                        <td className="p-4 text-right pr-6">
                          <button
                            onClick={() => handleCatDelete(c.id)}
                            className="p-1.5 hover:bg-slate-850 text-slate-400 hover:text-rose-400 rounded-lg transition-colors cursor-pointer"
                          >
                            <Trash2 size={16} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
      <Modal
        isOpen={isProdModalOpen}
        onClose={() => setIsProdModalOpen(false)}
        title={editingProdId ? 'Modify Product Specifications' : 'Add New Inventory Item'}
      >
        <form onSubmit={handleProdSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Product Name <span className="text-indigo-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g. 5W-30 Synthetic Motor Oil"
                value={prodName}
                onChange={(e) => setProdName(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Category
              </label>
              <select
                value={prodCategoryId}
                onChange={(e) => setProdCategoryId(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
              >
                <option value="">No Category</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                SKU Number <span className="text-indigo-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g. OIL-5W30-4L"
                value={prodSku}
                onChange={(e) => setProdSku(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Barcode
              </label>
              <input
                type="text"
                placeholder="e.g. 07963249"
                value={prodBarcode}
                onChange={(e) => setProdBarcode(e.target.value)}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-600 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Cost Price ($)
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={prodCostPrice}
                onChange={(e) => setProdCostPrice(Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Selling Price ($)
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={prodSellingPrice}
                onChange={(e) => setProdSellingPrice(Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Initial Stock Level
              </label>
              <input
                type="number"
                required
                disabled={!!editingProdId} 
                value={prodQuantity}
                onChange={(e) => setProdQuantity(Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm disabled:opacity-50"
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Reorder Alert Level
              </label>
              <input
                type="number"
                required
                value={prodReorderLevel}
                onChange={(e) => setProdReorderLevel(Number(e.target.value))}
                className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 focus:outline-none focus:border-indigo-500 text-sm"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Product Image / Attachment
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
            {productFileUrl && (
              <div className="mt-2 text-xs text-indigo-400 flex items-center gap-2">
                <a href={productFileUrl} target="_blank" rel="noreferrer" className="underline font-medium">View Current Image</a>
              </div>
            )}
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-850 pt-4 mt-6">
            <button
              type="button"
              onClick={() => setIsProdModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 font-semibold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer"
            >
              {editingProdId ? 'Save Product Specs' : 'Register Product'}
            </button>
          </div>
        </form>
      </Modal>
      <Modal
        isOpen={isCatModalOpen}
        onClose={() => setIsCatModalOpen(false)}
        title="Configure Product Category"
      >
        <form onSubmit={handleCatSubmit} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Category Name <span className="text-indigo-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Engine Oils, Brake Pads"
              value={catName}
              onChange={(e) => setCatName(e.target.value)}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-605 focus:outline-none focus:border-indigo-500 text-sm"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
              Description / Notes
            </label>
            <textarea
              placeholder="e.g. Internal lubricants and oils"
              value={catDesc}
              onChange={(e) => setCatDesc(e.target.value)}
              rows={3}
              className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-605 focus:outline-none focus:border-indigo-500 text-sm resize-none"
            />
          </div>

          <div className="flex justify-end gap-3 border-t border-slate-850 pt-4 mt-6">
            <button
              type="button"
              onClick={() => setIsCatModalOpen(false)}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-750 text-slate-300 font-semibold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-xl text-sm transition-colors cursor-pointer"
            >
              Add Category
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
