import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getStocks, getPortfolio, getPnL, getMarketStatus } from '../services/api'
import { TrendingUp, TrendingDown, DollarSign, Briefcase, Activity, ArrowRight, Wallet } from 'lucide-react'
import './Dashboard.css'

function StatCard({ label, value, sub, color, icon: Icon }) {
  return (
    <div className="stat-card card fade-in">
      <div className="stat-top">
        <span className="card-title">{label}</span>
        <div className="stat-icon" style={{ background: `${color}22`, color }}>
          <Icon size={16} />
        </div>
      </div>
      <div className="stat-value mono" style={{ color }}>{value}</div>
      {sub && <div className="stat-sub">{sub}</div>}
    </div>
  )
}

export default function Dashboard() {
  const { user, refreshUser } = useAuth()
  const navigate = useNavigate()
  const [stocks, setStocks] = useState([])
  const [portfolio, setPortfolio] = useState([])
  const [pnl, setPnl] = useState({})
  const [marketStatus, setMarketStatus] = useState('CLOSED')
  const [loading, setLoading] = useState(true)

  const loadData = () => {
    Promise.all([getStocks(), getPortfolio(), getPnL(), getMarketStatus()])
      .then(([s, p, pnlRes, ms]) => {
        setStocks(s.data)
        setPortfolio(p.data)
        setPnl(pnlRes.data)
        setMarketStatus(ms.data)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    loadData()
    refreshUser() // Always refresh balance on dashboard load

    const interval = setInterval(() => {
      loadData()
      refreshUser()
    }, 8000)
    return () => clearInterval(interval)
  }, [])

  const totalPnl = pnl['TOTAL'] || 0
  const portfolioValue = portfolio.reduce((sum, p) =>
    sum + (Number(p.stock?.price) || 0) * (Number(p.quantity) || 0), 0)

  if (loading) return <div className="page"><div className="spinner" /></div>

  return (
    <div className="page fade-in">
      <div className="dashboard-header">
        <div>
          <h1 className="page-title">Good {getGreeting()}, {user?.username} 👋</h1>
          <p className="muted" style={{ fontSize: 13 }}>Here's your trading overview</p>
        </div>
        <div className={`market-badge ${marketStatus === 'OPEN' ? 'open' : 'closed'}`}>
          <span className={`status-dot ${marketStatus === 'OPEN' ? 'open' : 'closed'}`} />
          Market {marketStatus}
        </div>
      </div>

      {/* Zero balance prompt */}
      {Number(user?.balance || 0) === 0 && (
        <div style={{
          background: 'var(--yellow-dim)', border: '1px solid var(--yellow)',
          borderRadius: 'var(--radius-lg)', padding: '16px 20px',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          marginBottom: 20, flexWrap: 'wrap', gap: 12
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <Wallet size={20} style={{ color: 'var(--yellow)' }} />
            <div>
              <div style={{ fontWeight: 700, fontSize: 14 }}>Your account has no funds yet</div>
              <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>Add money to start trading stocks</div>
            </div>
          </div>
          <button className="btn-primary" onClick={() => navigate('/deposit')}
            style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <Wallet size={14} /> Deposit Now
          </button>
        </div>
      )}

      <div className="grid-4" style={{ marginBottom: 24 }}>
        <StatCard label="Balance"
          value={`₹${Number(user?.balance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          icon={DollarSign} color="var(--accent)" />
        <StatCard label="Portfolio Value"
          value={`₹${portfolioValue.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          icon={Briefcase} color="var(--blue)" />
        <StatCard label="Total P&L"
          value={`${totalPnl >= 0 ? '+' : ''}₹${Number(totalPnl).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          icon={totalPnl >= 0 ? TrendingUp : TrendingDown}
          color={totalPnl >= 0 ? 'var(--green)' : 'var(--red)'} />
        <StatCard label="Holdings" value={portfolio.length}
          sub={`${portfolio.length} stock${portfolio.length !== 1 ? 's' : ''}`}
          icon={Activity} color="var(--yellow)" />
      </div>

      <div className="grid-2" style={{ marginBottom: 24 }}>
        {/* Live Market */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">Live Market</span>
            <button className="btn-secondary" style={{ padding: '6px 12px', fontSize: 12, display: 'flex', alignItems: 'center', gap: 4 }}
              onClick={() => navigate('/trade')}>
              Trade <ArrowRight size={12} />
            </button>
          </div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Symbol</th><th>Company</th><th>Price</th><th>Status</th></tr></thead>
              <tbody>
                {stocks.filter(s => s.symbol !== 'system' && s.companyName).slice(0, 8).map(stock => (
                  <tr key={stock.id} className="clickable-row" onClick={() => navigate(`/trade/${stock.symbol}`)}>
                    <td><span className="mono" style={{ fontWeight: 700, color: 'var(--accent)' }}>{stock.symbol}</span></td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: 12 }}>{stock.companyName}</td>
                    <td><span className="mono">₹{Number(stock.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span></td>
                    <td><span className={`tag ${stock.tradable ? 'tag-green' : 'tag-red'}`}>{stock.tradable ? 'Active' : 'Halted'}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Holdings */}
        <div className="card">
          <div className="card-header">
            <span className="card-title">My Holdings</span>
            <button className="btn-secondary" style={{ padding: '6px 12px', fontSize: 12, display: 'flex', alignItems: 'center', gap: 4 }}
              onClick={() => navigate('/portfolio')}>
              View All <ArrowRight size={12} />
            </button>
          </div>
          {portfolio.length === 0 ? (
            <div className="empty-state">
              <Briefcase size={32} className="muted" />
              <p className="muted">No holdings yet</p>
              <button className="btn-primary" style={{ marginTop: 12 }} onClick={() => navigate('/trade')}>
                Start Trading
              </button>
            </div>
          ) : (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Symbol</th><th>Qty</th><th>Avg Price</th><th>P&L</th></tr></thead>
                <tbody>
                  {portfolio.slice(0, 6).map(p => {
                    const pl = pnl[p.stock?.symbol] || 0
                    return (
                      <tr key={p.id}>
                        <td><span className="mono" style={{ fontWeight: 700 }}>{p.stock?.symbol}</span></td>
                        <td><span className="mono">{p.quantity}</span></td>
                        <td><span className="mono">₹{Number(p.averagePrice).toFixed(2)}</span></td>
                        <td><span className={`mono ${pl >= 0 ? 'positive' : 'negative'}`}>
                          {pl >= 0 ? '+' : ''}₹{Number(pl).toFixed(2)}
                        </span></td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

function getGreeting() {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
}
