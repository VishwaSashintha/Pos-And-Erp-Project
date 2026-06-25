import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { apiFetch } from '../api';
import { ShieldAlert, CreditCard, CheckCircle2, Zap, ArrowUpRight, Clock } from 'lucide-react';

export const Billing: React.FC = () => {
  const { user } = useAuth();
  const [subscription, setSubscription] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const fetchSubscription = async () => {
    try {
      // Typically we'd fetch tenant info which includes subscription, or a dedicated endpoint.
      // Since we don't have a specific endpoint for fetching just the subscription plan directly to the user
      // without going through tenant info, let's mock it for the UI based on user token or fetch from /api/tenant/info
      const data = await apiFetch('/api/auth/me'); // Just to see if we can get tenant details
      // Assuming user object has tenant info, or we can just fetch some dummy data if API doesn't expose it yet.
      
      // Let's pretend we have a dedicated /api/billing/info endpoint or similar, 
      // but for now we'll simulate the state to show the UI
      setSubscription({
        planType: 'PRO',
        status: 'ACTIVE',
        nextBillingDate: '2026-07-25',
        amount: 99.00
      });
    } catch (e) {
      console.error('Failed to fetch billing info', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSubscription();
  }, []);

  const handleUpgrade = async (plan: string) => {
    try {
      await apiFetch(`/api/billing/subscribe?planType=${plan}`, { method: 'POST' });
      alert(`Successfully initiated upgrade to ${plan}. Awaiting payment confirmation.`);
    } catch (e: any) {
      alert(`Failed to upgrade: ${e.message}`);
    }
  };

  if (loading) {
    return <div className="flex justify-center py-12"><div className="w-8 h-8 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin"></div></div>;
  }

  return (
    <div className="space-y-8 max-w-5xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold text-white tracking-tight">Billing & Subscriptions</h2>
        <p className="text-slate-400 text-sm mt-1">Manage your plan, payment methods, and billing history</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-slate-900 border border-indigo-500/30 rounded-2xl p-6 relative overflow-hidden">
            <div className="absolute top-0 right-0 w-64 h-64 bg-indigo-500/10 rounded-full blur-3xl -mr-20 -mt-20"></div>
            
            <div className="flex justify-between items-start relative z-10">
              <div>
                <div className="flex items-center gap-3 mb-2">
                  <h3 className="text-xl font-bold text-white">{subscription?.planType} Plan</h3>
                  <span className="bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 px-2.5 py-0.5 rounded-full text-xs font-medium flex items-center gap-1">
                    <CheckCircle2 size={14}/> {subscription?.status}
                  </span>
                </div>
                <p className="text-slate-400 text-sm">Your subscription is active and renews automatically.</p>
              </div>
              <div className="text-right">
                <div className="text-3xl font-bold text-white">${subscription?.amount.toFixed(2)}</div>
                <div className="text-slate-500 text-sm">per month</div>
              </div>
            </div>

            <div className="mt-8 pt-6 border-t border-slate-800 flex justify-between items-center relative z-10">
              <div className="flex items-center gap-3 text-slate-300">
                <Clock size={18} className="text-indigo-400" />
                <span className="text-sm">Next billing date: <strong className="text-white">{subscription?.nextBillingDate}</strong></span>
              </div>
              <button className="text-indigo-400 hover:text-indigo-300 text-sm font-medium transition-colors">
                Cancel Subscription
              </button>
            </div>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
             <h3 className="text-lg font-bold text-white mb-4">Available Plans</h3>
             <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                
                <div className="border border-slate-800 rounded-xl p-5 hover:border-slate-700 transition-colors">
                   <div className="flex justify-between items-center mb-2">
                      <div className="font-bold text-white">BASIC</div>
                      <div className="text-lg font-bold text-slate-300">$29<span className="text-xs text-slate-500 font-normal">/mo</span></div>
                   </div>
                   <ul className="text-sm text-slate-400 space-y-2 mb-4">
                      <li className="flex items-center gap-2"><CheckCircle2 size={14} className="text-emerald-500"/> 5 Users Limit</li>
                      <li className="flex items-center gap-2"><CheckCircle2 size={14} className="text-emerald-500"/> Core POS & Inventory</li>
                      <li className="flex items-center gap-2 opacity-50"><XCircle size={14}/> No HR Module</li>
                   </ul>
                   <button onClick={() => handleUpgrade('BASIC')} className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg text-sm font-medium transition-colors">
                      Downgrade
                   </button>
                </div>

                <div className="border border-indigo-500/50 bg-indigo-500/5 rounded-xl p-5 relative">
                   <div className="absolute -top-3 -right-3 bg-indigo-500 text-white text-[10px] font-bold px-2 py-1 rounded-full uppercase tracking-wider flex items-center gap-1 shadow-lg">
                      <Zap size={12}/> Current
                   </div>
                   <div className="flex justify-between items-center mb-2">
                      <div className="font-bold text-indigo-400">PRO</div>
                      <div className="text-lg font-bold text-white">$99<span className="text-xs text-slate-500 font-normal">/mo</span></div>
                   </div>
                   <ul className="text-sm text-slate-400 space-y-2 mb-4">
                      <li className="flex items-center gap-2"><CheckCircle2 size={14} className="text-emerald-500"/> 50 Users Limit</li>
                      <li className="flex items-center gap-2"><CheckCircle2 size={14} className="text-emerald-500"/> All Modules (HR, Assets)</li>
                      <li className="flex items-center gap-2"><CheckCircle2 size={14} className="text-emerald-500"/> Priority Support</li>
                   </ul>
                   <button disabled className="w-full py-2 bg-indigo-600/50 text-white/50 cursor-not-allowed rounded-lg text-sm font-medium">
                      Current Plan
                   </button>
                </div>

             </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
            <div className="flex items-center gap-3 mb-4">
              <div className="p-2 bg-slate-800 rounded-lg text-indigo-400">
                <CreditCard size={20} />
              </div>
              <h3 className="font-bold text-white">Payment Method</h3>
            </div>
            <div className="border border-slate-800 rounded-lg p-4 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-10 h-6 bg-slate-800 rounded flex items-center justify-center text-[10px] font-bold text-white">VISA</div>
                <div>
                  <div className="text-sm font-medium text-slate-200">•••• •••• •••• 4242</div>
                  <div className="text-xs text-slate-500">Expires 12/28</div>
                </div>
              </div>
            </div>
            <button className="w-full mt-4 py-2 text-sm text-indigo-400 hover:text-indigo-300 font-medium transition-colors">
              Update Payment Method
            </button>
          </div>

          <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
            <h3 className="font-bold text-white mb-4">Billing History</h3>
            <div className="space-y-4">
               {[1,2,3].map((i) => (
                 <div key={i} className="flex justify-between items-center pb-4 border-b border-slate-800 last:border-0 last:pb-0">
                   <div>
                     <div className="text-sm font-medium text-slate-200">Invoice #INV-2026-{i}</div>
                     <div className="text-xs text-slate-500">Jun 25, 2026</div>
                   </div>
                   <div className="flex items-center gap-3">
                     <span className="text-sm font-medium text-slate-200">$99.00</span>
                     <button className="text-slate-500 hover:text-slate-300 transition-colors"><ArrowUpRight size={16}/></button>
                   </div>
                 </div>
               ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

const XCircle = ({ size, className }: { size: number; className?: string }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className}>
    <circle cx="12" cy="12" r="10"></circle>
    <line x1="15" y1="9" x2="9" y2="15"></line>
    <line x1="9" y1="9" x2="15" y2="15"></line>
  </svg>
);
