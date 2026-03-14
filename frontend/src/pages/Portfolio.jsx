import { useEffect, useState } from 'react'
import { getPortfolio, getPnL, getMyTrades } from '../services/api'
import { TrendingUp, TrendingDown, Briefcase } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

export default function Portfolio() {
  const [portfolio, setPortfolio] = useState([])
  const [pnl, setPnl] = useState({})
  const [trades, setTrades] = useState([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    Promise.all([getPortfolio(), getPnL(), getMyTrades()])
      .then(([p, pnlRes, t]) => {
        setPortfolio(p.data)
        setPnl(pnlRes.data)
        setTrades(t.data)
      })
      .finally(() => setLoading(false))
  }, [])

  const totalPnl = pnl['TOTAL'] || 0
  const portfolioValue = portfolio.reduce((sum, p) => sum + Number(p.stock?.price || 0) * Number(p.quantity || 0), 0)

  if (loading) return <div className="page"><div className="spinner" /></div>

  return (
    <div className="page fade-in">
      <h1 className="page-title">Portfolio</h1>

      <div className="grid-3" style={{ marginBottom: 24 }}>
        <div className="card">
          <div className="card-title" style={{ marginBottom: 8 }}>Portfolio Value</div>
          <div className="mono" style={{ fontSize: 24, fontWeight: 700, color: 'var(--blue)' }}>
            ₹{portfolioValue.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
        </div>
        <div className="card">
          <div className="card-title" style={{ marginBottom: 8 }}>Total P&L</div>
          <div className={`mono ${totalPnl >= 0 ? 'positive' : 'negative'}`} style={{ fontSize: 24, fontWeight: 700, display: 'flex', alignItems: 'center', gap: 8 }}>
            {totalPnl >= 0 ? <TrendingUp size={20} /> : <TrendingDown size={20} />}
            {totalPnl >= 0 ? '+' : ''}₹{Number(totalPnl).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
        </div>
        <div className="card">
          <div className="card-title" style={{ marginBottom: 8 }}>Holdings</div>
          <div className="mono" style={{ fontSize: 24, fontWeight: 700, color: 'var(--yellow)' }}>{portfolio.length} Stocks</div>
        </div>
      </div>

      <div className="card" style={{ marginBottom: 24 }}>
        <div className="card-header"><span className="card-title">Holdings</span></div>
        {portfolio.length === 0 ? (
          <div className="empty-state">
            <Briefcase size={36} className="muted" />
            <p className="muted">No holdings yet</p>
            <button className="btn-primary" style={{ marginTop: 12 }} onClick={() => navigate('/trade')}>Start Trading</button>
          </div>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Symbol</th>
                  <th>Company</th>
                  <th>Quantity</th>
                  <th>Avg Buy Price</th>
                  <th>Current Price</th>
                  <th>Current Value</th>
                  <th>P&L</th>
                  <th>P&L %</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.map(p => {
                  const pl = pnl[p.stock?.symbol] || 0
                  const currentValue = Number(p.stock?.price || 0) * Number(p.quantity || 0)
                  const invested = Number(p.averagePrice || 0) * Number(p.quantity || 0)
                  const plPct = invested > 0 ? ((currentValue - invested) / invested) * 100 : 0
                  return (
                    <tr key={p.id} style={{ cursor: 'pointer' }} onClick={() => navigate(`/trade/${p.stock?.symbol}`)}>
                      <td><span className="mono" style={{ fontWeight: 700, color: 'var(--accent)' }}>{p.stock?.symbol}</span></td>
                      <td style={{ color: 'var(--text-secondary)', fontSize: 12 }}>{p.stock?.companyName}</td>
                      <td><span className="mono">{p.quantity}</span></td>
                      <td><span className="mono">₹{Number(p.averagePrice).toFixed(2)}</span></td>
                      <td><span className="mono">₹{Number(p.stock?.price).toFixed(2)}</span></td>
                      <td><span className="mono">₹{currentValue.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span></td>
                      <td><span className={`mono ${pl >= 0 ? 'positive' : 'negative'}`}>{pl >= 0 ? '+' : ''}₹{Number(pl).toFixed(2)}</span></td>
                      <td><span className={`tag ${plPct >= 0 ? 'tag-green' : 'tag-red'}`}>{plPct >= 0 ? '+' : ''}{plPct.toFixed(2)}%</span></td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="card">
        <div className="card-header"><span className="card-title">Trade History</span></div>
        {trades.length === 0 ? (
          <p className="muted" style={{ padding: 20, textAlign: 'center', fontSize: 13 }}>No trades yet</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Stock</th><th>Type</th><th>Quantity</th><th>Price</th><th>Total</th><th>Time</th></tr>
              </thead>
              <tbody>
                {trades.slice(0, 20).map(t => (
                  <tr key={t.id}>
                    <td><span className="mono" style={{ fontWeight: 700 }}>{t.stock?.symbol}</span></td>
                    <td>
                      <span className={`tag ${t.buyer?.id ? 'tag-green' : 'tag-red'}`}>
                        {t.buyer ? 'BUY' : 'SELL'}
                      </span>
                    </td>
                    <td><span className="mono">{t.quantity}</span></td>
                    <td><span className="mono">₹{Number(t.price).toFixed(2)}</span></td>
                    <td><span className="mono">₹{(Number(t.price) * Number(t.quantity)).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</span></td>
                    <td style={{ fontSize: 12, color: 'var(--text-muted)' }}>
                      {t.executedAt ? new Date(t.executedAt).toLocaleString('en-IN') : '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
