import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Building, Lock, User, Mail, ShieldCheck, ChevronDown, Check, Layers } from 'lucide-react';

const INDUSTRIES = [
  { value: 'RETAIL', label: 'Retail Shop' },
  { value: 'RESTAURANT', label: 'Restaurant / Café' },
  { value: 'PHARMACY', label: 'Pharmacy' },
  { value: 'GARAGE', label: 'Garage / Workshop' },
  { value: 'GENERAL', label: 'General Business' },
];

const AVAILABLE_MODULES = [
  { value: 'POS', label: 'Point of Sale', description: 'Sales terminal, invoicing, payments', icon: '💳' },
  { value: 'INVENTORY', label: 'Inventory', description: 'Products, stock levels, movements', icon: '📦' },
  { value: 'ACCOUNTING', label: 'Accounting', description: 'Ledgers, journals, financial reports', icon: '📊', mandatory: true },
  { value: 'CRM', label: 'CRM', description: 'Customer management, leads tracking', icon: '👥' },
  { value: 'HRM', label: 'HR Management', description: 'Employees, payroll, attendance', icon: '🏢' },
];

const INDUSTRY_PRESETS: Record<string, string[]> = {
  RETAIL: ['POS', 'INVENTORY', 'ACCOUNTING'],
  RESTAURANT: ['POS', 'INVENTORY', 'ACCOUNTING'],
  PHARMACY: ['POS', 'INVENTORY', 'ACCOUNTING'],
  GARAGE: ['POS', 'INVENTORY', 'ACCOUNTING'],
  GENERAL: ['POS', 'INVENTORY', 'ACCOUNTING'],
};

export const Login: React.FC = () => {
  const { login, registerTenant } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  
  // Login state
  const [loginUsername, setLoginUsername] = useState('');
  const [loginPassword, setLoginPassword] = useState('');

  // Registration state
  const [regTenantName, setRegTenantName] = useState('');
  const [regUsername, setRegUsername] = useState('');
  const [regPassword, setRegPassword] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regIndustry, setRegIndustry] = useState('');
  const [regModules, setRegModules] = useState<string[]>(['ACCOUNTING']); // Accounting always on
  const [regStep, setRegStep] = useState(1); // 1 = details, 2 = industry + modules

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!loginUsername || !loginPassword) {
      setError('Please fill in all fields.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await login(loginUsername, loginPassword);
    } catch (err: any) {
      setError(err.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleIndustrySelect = (industry: string) => {
    setRegIndustry(industry);
    const presets = INDUSTRY_PRESETS[industry] || ['ACCOUNTING'];
    setRegModules([...new Set([...presets, 'ACCOUNTING'])]);
  };

  const toggleModule = (mod: string) => {
    if (mod === 'ACCOUNTING') return; // Can't toggle mandatory module
    setRegModules((prev) =>
      prev.includes(mod) ? prev.filter((m) => m !== mod) : [...prev, mod]
    );
  };

  const handleRegisterSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (regStep === 1) {
      if (!regTenantName || !regUsername || !regPassword || !regEmail) {
        setError('Please fill in all fields.');
        return;
      }
      setError(null);
      setRegStep(2);
      return;
    }
    // Step 2: industry + modules
    if (!regIndustry) {
      setError('Please select your business type.');
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await registerTenant(regTenantName, regUsername, regPassword, regEmail, regIndustry, regModules);
    } catch (err: any) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen w-screen bg-slate-950 flex items-center justify-center p-4 relative overflow-hidden">
      <div className="absolute top-1/4 left-1/4 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-indigo-900/10 rounded-full blur-[120px] pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 translate-x-1/2 translate-y-1/2 w-[500px] h-[500px] bg-purple-900/10 rounded-full blur-[120px] pointer-events-none"></div>

      <div className={`w-full ${isRegister && regStep === 2 ? 'max-w-lg' : 'max-w-md'} bg-slate-900/40 border border-slate-800/80 backdrop-blur-xl p-8 rounded-3xl shadow-2xl relative z-10 transition-all duration-300`}>
        <div className="flex flex-col items-center mb-8">
          <div className="bg-indigo-600 p-3.5 rounded-2xl text-white shadow-xl shadow-indigo-500/20 mb-3">
            <Building size={28} />
          </div>
          <h2 className="text-3xl font-extrabold tracking-tight text-white">BOS Platform</h2>
          <p className="text-sm text-slate-400 mt-1">Business Operating System</p>
        </div>
        <div className="flex bg-slate-950 p-1 rounded-xl mb-6 border border-slate-850">
          <button
            type="button"
            onClick={() => {
              setIsRegister(false);
              setError(null);
              setRegStep(1);
            }}
            className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all cursor-pointer ${
              !isRegister ? 'bg-indigo-600 text-white shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Sign In
          </button>
          <button
            type="button"
            onClick={() => {
              setIsRegister(true);
              setError(null);
            }}
            className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all cursor-pointer ${
              isRegister ? 'bg-indigo-600 text-white shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Register Workspace
          </button>
        </div>

        {error && (
          <div className="bg-red-500/10 border border-red-500/20 text-red-400 px-4 py-3 rounded-xl text-xs font-medium mb-4 flex items-center gap-2">
            <ShieldCheck size={16} className="shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {!isRegister ? (
          /* ─── LOGIN FORM ─── */
          <form onSubmit={handleLoginSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Username
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                  <User size={16} />
                </span>
                <input
                  type="text"
                  required
                  placeholder="Enter administrator username"
                  value={loginUsername}
                  onChange={(e) => setLoginUsername(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                Password
              </label>
              <div className="relative">
                <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                  <Lock size={16} />
                </span>
                <input
                  type="password"
                  required
                  placeholder="Enter password"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold shadow-lg shadow-indigo-500/20 transition-all cursor-pointer flex items-center justify-center gap-2 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>
        ) : (
          /* ─── REGISTER FORM ─── */
          <form onSubmit={handleRegisterSubmit} className="space-y-4">
            {regStep === 1 && (
              <>
                {/* Step indicator */}
                <div className="flex items-center gap-2 mb-2">
                  <div className="flex items-center gap-1.5">
                    <div className="w-6 h-6 rounded-full bg-indigo-600 text-white text-xs flex items-center justify-center font-bold">1</div>
                    <span className="text-xs text-slate-300 font-medium">Details</span>
                  </div>
                  <div className="flex-1 h-px bg-slate-700"></div>
                  <div className="flex items-center gap-1.5">
                    <div className="w-6 h-6 rounded-full bg-slate-700 text-slate-400 text-xs flex items-center justify-center font-bold">2</div>
                    <span className="text-xs text-slate-500 font-medium">Modules</span>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                    Organization / Tenant Name
                  </label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                      <Building size={16} />
                    </span>
                    <input
                      type="text"
                      required
                      placeholder="e.g. Acme Spares"
                      value={regTenantName}
                      onChange={(e) => setRegTenantName(e.target.value)}
                      className="w-full pl-10 pr-4 py-3 bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                    Admin Email Address
                  </label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                      <Mail size={16} />
                    </span>
                    <input
                      type="email"
                      required
                      placeholder="admin@organization.com"
                      value={regEmail}
                      onChange={(e) => setRegEmail(e.target.value)}
                      className="w-full pl-10 pr-4 py-3 bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                    Admin Username
                  </label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                      <User size={16} />
                    </span>
                    <input
                      type="text"
                      required
                      placeholder="Enter administrator username"
                      value={regUsername}
                      onChange={(e) => setRegUsername(e.target.value)}
                      className="w-full pl-10 pr-4 py-3 bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
                    Admin Password
                  </label>
                  <div className="relative">
                    <span className="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-500">
                      <Lock size={16} />
                    </span>
                    <input
                      type="password"
                      required
                      placeholder="Create password"
                      value={regPassword}
                      onChange={(e) => setRegPassword(e.target.value)}
                      className="w-full pl-10 pr-4 py-3 bg-slate-950/60 border border-slate-800 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-indigo-500 transition-colors text-sm"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  className="w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold shadow-lg shadow-indigo-500/20 transition-all cursor-pointer flex items-center justify-center gap-2 text-sm"
                >
                  Continue — Select Modules
                  <ChevronDown size={16} className="rotate-[-90deg]" />
                </button>
              </>
            )}

            {regStep === 2 && (
              <>
                {/* Step indicator */}
                <div className="flex items-center gap-2 mb-2">
                  <div className="flex items-center gap-1.5">
                    <div className="w-6 h-6 rounded-full bg-emerald-600 text-white text-xs flex items-center justify-center font-bold"><Check size={12} /></div>
                    <span className="text-xs text-emerald-400 font-medium">Details</span>
                  </div>
                  <div className="flex-1 h-px bg-indigo-600"></div>
                  <div className="flex items-center gap-1.5">
                    <div className="w-6 h-6 rounded-full bg-indigo-600 text-white text-xs flex items-center justify-center font-bold">2</div>
                    <span className="text-xs text-slate-300 font-medium">Modules</span>
                  </div>
                </div>

                {/* Industry Selector */}
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">
                    Business Type
                  </label>
                  <div className="grid grid-cols-2 gap-2">
                    {INDUSTRIES.map((ind) => (
                      <button
                        key={ind.value}
                        type="button"
                        onClick={() => handleIndustrySelect(ind.value)}
                        className={`px-3 py-2.5 rounded-xl text-sm font-medium transition-all cursor-pointer border ${
                          regIndustry === ind.value
                            ? 'bg-indigo-600/20 border-indigo-500 text-indigo-300 shadow-sm shadow-indigo-500/10'
                            : 'bg-slate-950/40 border-slate-800 text-slate-400 hover:border-slate-600 hover:text-slate-300'
                        }`}
                      >
                        {ind.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Module Marketplace */}
                <div>
                  <label className="flex items-center gap-2 text-xs font-semibold text-slate-400 mb-2 uppercase tracking-wider">
                    <Layers size={14} />
                    Select Modules
                  </label>
                  <div className="space-y-2">
                    {AVAILABLE_MODULES.map((mod) => {
                      const isSelected = regModules.includes(mod.value);
                      const isMandatory = mod.mandatory;
                      return (
                        <button
                          key={mod.value}
                          type="button"
                          onClick={() => toggleModule(mod.value)}
                          disabled={isMandatory}
                          className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all border text-left ${
                            isSelected
                              ? 'bg-indigo-600/10 border-indigo-500/40 shadow-sm'
                              : 'bg-slate-950/30 border-slate-800 hover:border-slate-600'
                          } ${isMandatory ? 'cursor-not-allowed opacity-80' : 'cursor-pointer'}`}
                        >
                          <span className="text-xl">{mod.icon}</span>
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-2">
                              <span className={`text-sm font-semibold ${isSelected ? 'text-indigo-300' : 'text-slate-300'}`}>
                                {mod.label}
                              </span>
                              {isMandatory && (
                                <span className="text-[10px] bg-amber-500/20 text-amber-400 px-1.5 py-0.5 rounded font-bold tracking-wide">
                                  REQUIRED
                                </span>
                              )}
                            </div>
                            <p className="text-xs text-slate-500 mt-0.5 truncate">{mod.description}</p>
                          </div>
                          <div className={`w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all ${
                            isSelected ? 'bg-indigo-600 border-indigo-600' : 'border-slate-600'
                          }`}>
                            {isSelected && <Check size={12} className="text-white" />}
                          </div>
                        </button>
                      );
                    })}
                  </div>
                </div>

                <div className="flex gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setRegStep(1)}
                    className="flex-1 py-3 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl font-semibold transition-all cursor-pointer text-sm"
                  >
                    Back
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-[2] py-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl font-bold shadow-lg shadow-indigo-500/20 transition-all cursor-pointer flex items-center justify-center gap-2 text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {loading ? 'Creating Workspace...' : 'Initialize BOS Workspace'}
                  </button>
                </div>
              </>
            )}
          </form>
        )}
      </div>
    </div>
  );
};
