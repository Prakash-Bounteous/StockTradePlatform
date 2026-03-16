import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { withdraw as withdrawApi } from '../services/api'
import { Wallet, ArrowRight, ShieldCheck, ArrowDownLeft, ArrowUpRight } from 'lucide-react'
import './Deposit.css'

const QUICK_AMOUNTS   = [5000, 10000, 25000, 50000, 100000, 500000]
const PAYMENT_METHODS = [
  { id: 'upi',     label: 'UPI',         icon: '⚡', desc: 'GPay, PhonePe, Paytm' },
  { id: 'netbank', label: 'Net Banking', icon: '🏦', desc: 'All major banks' },
  { id: 'card',    label: 'Debit Card',  icon: '💳', desc: 'Visa, Mastercard, Rupay' },
]
const WITHDRAW_METHODS = [
  { id: 'bank',  label: 'Bank Transfer', icon: '🏦', desc: '1-2 business days' },
  { id: 'upi',   label: 'UPI',           icon: '⚡', desc: 'Instant transfer' },
]

export default function Deposit() {
  const { user, refreshUser } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()

  const [tab, setTab] = useState('deposit')   // 'deposit' | 'withdraw'

  // Deposit state
  const [dAmount, setDAmount]   = useState('')
  const [dMethod, setDMethod]   = useState('upi')
  const [dStep,   setDStep]     = useState(1)
  const [dLoading, setDLoading] = useState(false)

  // Withdraw state
  const [wAmount, setWAmount]   = useState('')
  const [wMethod, setWMethod]   = useState('bank')
  const [wStep,   setWStep]     = useState(1)
  const [wLoading, setWLoading] = useState(false)

  const parsedDeposit  = parseFloat(dAmount)  || 0
  const parsedWithdraw = parseFloat(wAmount) || 0
  const balance        = Number(user?.balance || 0)

  // ── Deposit handlers ──────────────────────────────────────────
  const handleDepositNext = () => {
    if (parsedDeposit < 100)       return toast.error('Minimum deposit is ₹100')
    if (parsedDeposit > 1000000)   return toast.error('Maximum deposit is ₹10,00,000')
    setDStep(2)
  }

  const handleDepositConfirm = async () => {
    setDLoading(true)
    try {
      const token = localStorage.getItem('token')
      await axios.post(
        'http://localhost:8080/users/deposit',
        { amount: parsedDeposit },
        { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } }
      )
      await refreshUser()
      setDStep(3)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Deposit failed')
      setDStep(1)
    } finally {
      setDLoading(false)
    }
  }

  // ── Withdraw handlers ─────────────────────────────────────────
  const handleWithdrawNext = () => {
    if (parsedWithdraw < 100)           return toast.error('Minimum withdrawal is ₹100')
    if (parsedWithdraw > balance)       return toast.error(`Insufficient balance. Available ₹${balance.toLocaleString('en-IN')}`)
    setWStep(2)
  }

  const handleWithdrawConfirm = async () => {
    setWLoading(true)
    try {
      await withdrawApi({ amount: parsedWithdraw })
      await refreshUser()
      setWStep(3)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Withdrawal failed')
      setWStep(1)
    } finally {
      setWLoading(false)
    }
  }

  const resetDeposit  = () => { setDStep(1); setDAmount('') }
  const resetWithdraw = () => { setWStep(1); setWAmount('') }

  return (
    <div className="page fade-in">
      <div className="deposit-container">

        {/* ── Left info panel ── */}
        <div className="deposit-info">
          <div className="deposit-brand">
            <Wallet size={28} style={{ color: 'var(--accent)' }} />
            <h2>My Funds</h2>
          </div>
          <p className="muted" style={{ fontSize: 13, marginBottom: 24 }}>
            Manage your virtual trading funds. Deposit money to trade or withdraw your available balance anytime.
          </p>

          <div className="balance-box">
            <div className="muted" style={{ fontSize: 12, marginBottom: 4 }}>Available Balance</div>
            <div className="mono" style={{ fontSize: 26, fontWeight: 700, color: 'var(--accent)' }}>
              ₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
            </div>
          </div>

          <div className="deposit-limits">
            <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
              Deposit Limits
            </div>
            <div className="limit-row"><span className="muted" style={{ fontSize: 12 }}>Minimum</span><span className="mono" style={{ fontSize: 12 }}>₹100</span></div>
            <div className="limit-row"><span className="muted" style={{ fontSize: 12 }}>Per transaction</span><span className="mono" style={{ fontSize: 12 }}>₹10,00,000</span></div>
            <div className="limit-row"><span className="muted" style={{ fontSize: 12 }}>Max balance</span><span className="mono" style={{ fontSize: 12 }}>₹1,00,00,000</span></div>
          </div>

          <div className="deposit-limits" style={{ marginTop: 12 }}>
            <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--text-secondary)', marginBottom: 6, textTransform: 'uppercase', letterSpacing: '0.06em' }}>
              Withdrawal Limits
            </div>
            <div className="limit-row"><span className="muted" style={{ fontSize: 12 }}>Minimum</span><span className="mono" style={{ fontSize: 12 }}>₹100</span></div>
            <div className="limit-row"><span className="muted" style={{ fontSize: 12 }}>Maximum</span><span className="mono" style={{ fontSize: 12 }}>Full balance</span></div>
          </div>

          <div className="secure-badge" style={{ marginTop: 16 }}>
            <ShieldCheck size={14} style={{ color: 'var(--green)' }} />
            <span style={{ fontSize: 12, color: 'var(--green)' }}>Simulated — no real money involved</span>
          </div>
        </div>

        {/* ── Right form panel ── */}
        <div className="deposit-form card">

          {/* Tab switcher */}
          <div className="funds-tabs">
            <button
              className={`funds-tab ${tab === 'deposit' ? 'active' : ''}`}
              onClick={() => { setTab('deposit'); resetDeposit() }}
            >
              <ArrowDownLeft size={15} /> Deposit
            </button>
            <button
              className={`funds-tab ${tab === 'withdraw' ? 'active withdraw-tab' : ''}`}
              onClick={() => { setTab('withdraw'); resetWithdraw() }}
            >
              <ArrowUpRight size={15} /> Withdraw
            </button>
          </div>

          {/* ════════════ DEPOSIT FLOW ════════════ */}
          {tab === 'deposit' && (
            <>
              {dStep === 1 && (
                <div className="fade-in">
                  <div className="step-header">
                    <span className="step-badge">Step 1 of 2</span>
                    <h3>Enter Deposit Amount</h3>
                  </div>

                  <div className="quick-amounts">
                    {QUICK_AMOUNTS.map(val => (
                      <button key={val}
                        className={`quick-btn ${dAmount === val.toString() ? 'active' : ''}`}
                        onClick={() => setDAmount(val.toString())}>
                        ₹{val.toLocaleString('en-IN')}
                      </button>
                    ))}
                  </div>

                  <div className="form-group" style={{ marginBottom: 16 }}>
                    <label>Or enter custom amount</label>
                    <div style={{ position: 'relative' }}>
                      <span style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)', fontSize: 14, fontFamily: 'var(--font-mono)' }}>₹</span>
                      <input type="number" min="100" max="1000000" placeholder="Enter amount"
                        value={dAmount} onChange={e => setDAmount(e.target.value)} style={{ paddingLeft: 28 }} />
                    </div>
                  </div>

                  <div className="form-group" style={{ marginBottom: 20 }}>
                    <label>Payment Method</label>
                    <div className="payment-methods">
                      {PAYMENT_METHODS.map(pm => (
                        <div key={pm.id} className={`payment-card ${dMethod === pm.id ? 'active' : ''}`} onClick={() => setDMethod(pm.id)}>
                          <span className="payment-icon">{pm.icon}</span>
                          <div>
                            <div style={{ fontSize: 13, fontWeight: 700 }}>{pm.label}</div>
                            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{pm.desc}</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {parsedDeposit >= 100 && (
                    <div className="amount-preview fade-in">
                      <span className="muted" style={{ fontSize: 12 }}>You will add</span>
                      <span className="mono" style={{ fontSize: 18, fontWeight: 700, color: 'var(--accent)' }}>
                        ₹{parsedDeposit.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  )}

                  <button className="btn-primary" style={{ width: '100%', padding: 13, marginTop: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}
                    onClick={handleDepositNext} disabled={parsedDeposit < 100}>
                    Continue <ArrowRight size={16} />
                  </button>
                </div>
              )}

              {dStep === 2 && (
                <div className="fade-in">
                  <div className="step-header">
                    <span className="step-badge">Step 2 of 2</span>
                    <h3>Confirm Deposit</h3>
                  </div>
                  <div className="confirm-summary">
                    <div className="confirm-row">
                      <span className="muted">Amount</span>
                      <span className="mono" style={{ fontWeight: 700, fontSize: 18, color: 'var(--accent)' }}>
                        ₹{parsedDeposit.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                    <div className="confirm-row">
                      <span className="muted">Method</span>
                      <span style={{ fontWeight: 600 }}>{PAYMENT_METHODS.find(p => p.id === dMethod)?.icon} {PAYMENT_METHODS.find(p => p.id === dMethod)?.label}</span>
                    </div>
                    <div className="confirm-row">
                      <span className="muted">Current Balance</span>
                      <span className="mono">₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                    </div>
                    <div className="confirm-row" style={{ borderTop: '1px solid var(--border)', paddingTop: 12, marginTop: 4 }}>
                      <span style={{ fontWeight: 700 }}>New Balance</span>
                      <span className="mono" style={{ fontWeight: 700, color: 'var(--green)', fontSize: 16 }}>
                        ₹{(balance + parsedDeposit).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 10, marginTop: 20 }}>
                    <button className="btn-secondary" style={{ flex: 1 }} onClick={() => setDStep(1)} disabled={dLoading}>Back</button>
                    <button className="btn-primary" style={{ flex: 2, padding: 13 }} onClick={handleDepositConfirm} disabled={dLoading}>
                      {dLoading ? 'Processing...' : '✓ Confirm Deposit'}
                    </button>
                  </div>
                </div>
              )}

              {dStep === 3 && (
                <div className="fade-in success-state">
                  <div className="success-icon">✅</div>
                  <h3>Deposit Successful!</h3>
                  <p className="muted" style={{ fontSize: 13 }}>
                    ₹{parsedDeposit.toLocaleString('en-IN', { minimumFractionDigits: 2 })} added to your account.
                  </p>
                  <div className="confirm-summary" style={{ marginTop: 20, width: '100%' }}>
                    <div className="confirm-row">
                      <span className="muted">New Balance</span>
                      <span className="mono" style={{ fontWeight: 700, color: 'var(--green)', fontSize: 16 }}>
                        ₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 10, marginTop: 20, width: '100%' }}>
                    <button className="btn-secondary" style={{ flex: 1 }} onClick={resetDeposit}>Deposit More</button>
                    <button className="btn-primary" style={{ flex: 2 }} onClick={() => navigate('/trade')}>
                      Start Trading <ArrowRight size={14} />
                    </button>
                  </div>
                </div>
              )}
            </>
          )}

          {/* ════════════ WITHDRAW FLOW ════════════ */}
          {tab === 'withdraw' && (
            <>
              {wStep === 1 && (
                <div className="fade-in">
                  <div className="step-header">
                    <span className="step-badge withdraw-badge">Step 1 of 2</span>
                    <h3>Enter Withdrawal Amount</h3>
                  </div>

                  {/* Quick % buttons */}
                  <div className="quick-amounts" style={{ marginBottom: 16 }}>
                    {[25, 50, 75, 100].map(pct => {
                      const val = Math.floor((balance * pct) / 100)
                      return (
                        <button key={pct}
                          className={`quick-btn ${wAmount === val.toString() ? 'active withdraw-active' : ''}`}
                          onClick={() => setWAmount(val.toString())}
                          disabled={balance === 0}>
                          {pct}% {pct === 100 ? '(All)' : ''}
                        </button>
                      )
                    })}
                  </div>

                  <div className="form-group" style={{ marginBottom: 16 }}>
                    <label>Or enter custom amount</label>
                    <div style={{ position: 'relative' }}>
                      <span style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)', fontSize: 14, fontFamily: 'var(--font-mono)' }}>₹</span>
                      <input type="number" min="100" max={balance} placeholder="Enter amount"
                        value={wAmount} onChange={e => setWAmount(e.target.value)} style={{ paddingLeft: 28 }} />
                    </div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 4 }}>
                      Available: <span className="mono" style={{ color: 'var(--accent)' }}>₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                    </div>
                  </div>

                  <div className="form-group" style={{ marginBottom: 20 }}>
                    <label>Withdraw To</label>
                    <div className="payment-methods">
                      {WITHDRAW_METHODS.map(wm => (
                        <div key={wm.id} className={`payment-card ${wMethod === wm.id ? 'active withdraw-card' : ''}`} onClick={() => setWMethod(wm.id)}>
                          <span className="payment-icon">{wm.icon}</span>
                          <div>
                            <div style={{ fontSize: 13, fontWeight: 700 }}>{wm.label}</div>
                            <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{wm.desc}</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>

                  {parsedWithdraw >= 100 && parsedWithdraw <= balance && (
                    <div className="amount-preview fade-in" style={{ background: 'var(--red-dim)', borderColor: 'var(--red)44' }}>
                      <span className="muted" style={{ fontSize: 12 }}>You will withdraw</span>
                      <span className="mono" style={{ fontSize: 18, fontWeight: 700, color: 'var(--red)' }}>
                        ₹{parsedWithdraw.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  )}

                  {balance === 0 && (
                    <div style={{ background: 'var(--yellow-dim)', border: '1px solid var(--yellow)', borderRadius: 'var(--radius)', padding: '10px 14px', fontSize: 13, color: 'var(--yellow)', marginBottom: 12 }}>
                      ⚠️ No balance available to withdraw
                    </div>
                  )}

                  <button
                    className="btn-sell"
                    style={{ width: '100%', padding: 13, marginTop: 8, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}
                    onClick={handleWithdrawNext}
                    disabled={parsedWithdraw < 100 || parsedWithdraw > balance || balance === 0}>
                    Continue <ArrowRight size={16} />
                  </button>
                </div>
              )}

              {wStep === 2 && (
                <div className="fade-in">
                  <div className="step-header">
                    <span className="step-badge withdraw-badge">Step 2 of 2</span>
                    <h3>Confirm Withdrawal</h3>
                  </div>
                  <div className="confirm-summary">
                    <div className="confirm-row">
                      <span className="muted">Amount</span>
                      <span className="mono" style={{ fontWeight: 700, fontSize: 18, color: 'var(--red)' }}>
                        ₹{parsedWithdraw.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                    <div className="confirm-row">
                      <span className="muted">Withdraw To</span>
                      <span style={{ fontWeight: 600 }}>{WITHDRAW_METHODS.find(w => w.id === wMethod)?.icon} {WITHDRAW_METHODS.find(w => w.id === wMethod)?.label}</span>
                    </div>
                    <div className="confirm-row">
                      <span className="muted">Current Balance</span>
                      <span className="mono">₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span>
                    </div>
                    <div className="confirm-row" style={{ borderTop: '1px solid var(--border)', paddingTop: 12, marginTop: 4 }}>
                      <span style={{ fontWeight: 700 }}>Remaining Balance</span>
                      <span className="mono" style={{ fontWeight: 700, color: (balance - parsedWithdraw) === 0 ? 'var(--red)' : 'var(--text-primary)', fontSize: 16 }}>
                        ₹{(balance - parsedWithdraw).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  </div>

                  {(balance - parsedWithdraw) === 0 && (
                    <div style={{ background: 'var(--yellow-dim)', border: '1px solid var(--yellow)', borderRadius: 'var(--radius)', padding: '8px 12px', fontSize: 12, color: 'var(--yellow)', marginTop: 12 }}>
                      ⚠️ This will empty your trading account
                    </div>
                  )}

                  <div style={{ display: 'flex', gap: 10, marginTop: 20 }}>
                    <button className="btn-secondary" style={{ flex: 1 }} onClick={() => setWStep(1)} disabled={wLoading}>Back</button>
                    <button className="btn-sell" style={{ flex: 2, padding: 13 }} onClick={handleWithdrawConfirm} disabled={wLoading}>
                      {wLoading ? 'Processing...' : '✓ Confirm Withdrawal'}
                    </button>
                  </div>
                </div>
              )}

              {wStep === 3 && (
                <div className="fade-in success-state">
                  <div className="success-icon">🏧</div>
                  <h3>Withdrawal Successful!</h3>
                  <p className="muted" style={{ fontSize: 13 }}>
                    ₹{parsedWithdraw.toLocaleString('en-IN', { minimumFractionDigits: 2 })} withdrawn from your account.
                  </p>
                  <div className="confirm-summary" style={{ marginTop: 20, width: '100%' }}>
                    <div className="confirm-row">
                      <span className="muted">Remaining Balance</span>
                      <span className="mono" style={{ fontWeight: 700, fontSize: 16 }}>
                        ₹{balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 10, marginTop: 20, width: '100%' }}>
                    <button className="btn-secondary" style={{ flex: 1 }} onClick={resetWithdraw}>Withdraw More</button>
                    <button className="btn-primary" style={{ flex: 2 }} onClick={() => navigate('/dashboard')}>
                      Go to Dashboard <ArrowRight size={14} />
                    </button>
                  </div>
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}
