import { useEffect, useState } from 'react'
import { getWatchlist, addToWatchlist, removeFromWatchlist, getStocks } from '../services/api'
import { useToast } from '../context/ToastContext'
import { useNavigate } from 'react-router-dom'
import { Star, StarOff, Plus, TrendingUp, TrendingDown } from 'lucide-react'

export default function Watchlist() {
  const [watchlist, setWatchlist] = useState([])
  const [stocks, setStocks] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const toast = useToast()
  const navigate = useNavigate()

  const load = () => {
    Promise.all([getWatchlist(), getStocks()])
      .then(([w, s]) => { setWatchlist(w.data); setStocks(s.data) })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const watchedSymbols = new Set(watchlist.map(w => w.stock?.symbol))

  const handleAdd = async (symbol) => {
    try {
      await addToWatchlist(symbol)
      toast.success(`${symbol} added to watchlist`)
      load()
    } catch (err) {
      toast.error(err.response?.data || 'Already in watchlist')
    }
  }

  const handleRemove = async (symbol) => {
    try {
      await removeFromWatchlist(symbol)
      toast.success(`${symbol} removed from watchlist`)
      load()
    } catch { toast.error('Failed to remove') }
  }

  const filteredStocks = stocks.filter(s =>
    s.symbol.toLowerCase().includes(search.toLowerCase()) ||
    s.companyName?.toLowerCase().includes(search.toLowerCase())
  )

  if (loading) return <div className="page"><div className="spinner" /></div>

  return (
    <div className="page fade-in">
      <h1 className="page-title">Watchlist</h1>

      {watchlist.length > 0 && (
        <div className="card" style={{ marginBottom: 24 }}>
          <div className="card-header"><span className="card-title">Watching ({watchlist.length})</span></div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Symbol</th><th>Company</th><th>Price</th><th>Status</th><th>Action</th></tr></thead>
              <tbody>
                {watchlist.map(w => (
                  <tr key={w.id}>
                    <td>
                      <span className="mono" style={{ fontWeight: 700, color: 'var(--accent)', cursor: 'pointer' }}
                        onClick={() => navigate(`/trade/${w.stock?.symbol}`)}>
                        {w.stock?.symbol}
                      </span>
                    </td>
                    <td style={{ color: 'var(--text-secondary)', fontSize: 12 }}>{w.stock?.companyName}</td>
                    <td><span className="mono">₹{Number(w.stock?.price).toFixed(2)}</span></td>
                    <td><span className={`tag ${w.stock?.tradable ? 'tag-green' : 'tag-red'}`}>{w.stock?.tradable ? 'Active' : 'Halted'}</span></td>
                    <td>
                      <button className="btn-danger" onClick={() => handleRemove(w.stock?.symbol)} style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <StarOff size={13} /> Remove
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <div className="card">
        <div className="card-header">
          <span className="card-title">Add to Watchlist</span>
          <input type="text" placeholder="Search..." value={search} onChange={e => setSearch(e.target.value)} style={{ width: 200 }} />
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Symbol</th><th>Company</th><th>Price</th><th>Action</th></tr></thead>
            <tbody>
              {filteredStocks.map(s => (
                <tr key={s.id}>
                  <td><span className="mono" style={{ fontWeight: 700 }}>{s.symbol}</span></td>
                  <td style={{ color: 'var(--text-secondary)', fontSize: 12 }}>{s.companyName}</td>
                  <td><span className="mono">₹{Number(s.price).toFixed(2)}</span></td>
                  <td>
                    {watchedSymbols.has(s.symbol)
                      ? <span className="tag tag-green"><Star size={11} /> Watching</span>
                      : <button className="btn-secondary" style={{ padding: '6px 12px', fontSize: 12, display: 'flex', alignItems: 'center', gap: 4 }}
                          onClick={() => handleAdd(s.symbol)}>
                          <Plus size={13} /> Watch
                        </button>
                    }
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
