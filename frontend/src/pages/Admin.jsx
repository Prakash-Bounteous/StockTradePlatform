import { useEffect, useState } from 'react'
import { getStocks, adminCreateStock, adminDeleteStock, adminEnableTrading, adminDisableTrading } from '../services/api'
import { useToast } from '../context/ToastContext'
import { Shield, Plus, Trash2, Play, Pause } from 'lucide-react'

const emptyForm = { symbol: '', companyName: '', price: '', totalShares: '' }

export default function Admin() {
  const [stocks, setStocks] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const toast = useToast()

  const load = () => {
    getStocks().then(r => setStocks(r.data)).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    if (!form.symbol || !form.companyName || !form.price || !form.totalShares) {
      return toast.error('All fields are required')
    }
    setSubmitting(true)
    try {
      await adminCreateStock({
        symbol: form.symbol.toUpperCase(),
        companyName: form.companyName,
        price: Number(form.price),
        totalShares: Number(form.totalShares)
      })
      toast.success(`${form.symbol.toUpperCase()} created successfully`)
      setForm(emptyForm)
      setShowForm(false)
      load()
    } catch (err) {
      toast.error(err.response?.data || 'Failed to create stock')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id, symbol) => {
    if (!window.confirm(`Delete ${symbol}? This cannot be undone.`)) return
    try {
      await adminDeleteStock(id)
      toast.success(`${symbol} deleted`)
      load()
    } catch { toast.error('Delete failed') }
  }

  const handleToggle = async (stock) => {
    try {
      if (stock.tradable) {
        await adminDisableTrading(stock.id)
        toast.success(`Trading halted for ${stock.symbol}`)
      } else {
        await adminEnableTrading(stock.id)
        toast.success(`Trading enabled for ${stock.symbol}`)
      }
      load()
    } catch { toast.error('Toggle failed') }
  }

  if (loading) return <div className="page"><div className="spinner" /></div>

  return (
    <div className="page fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
            <Shield size={22} style={{ color: 'var(--accent)' }} />
            <h1 className="page-title" style={{ margin: 0 }}>Admin Panel</h1>
          </div>
          <p className="muted" style={{ fontSize: 13 }}>Manage stocks and trading controls</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm(p => !p)} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <Plus size={15} /> {showForm ? 'Cancel' : 'Add Stock'}
        </button>
      </div>

      {/* Create Stock Form */}
      {showForm && (
        <div className="card fade-in" style={{ marginBottom: 24, border: '1px solid var(--accent)44' }}>
          <div className="card-header"><span className="card-title">New Stock</span></div>
          <form onSubmit={handleCreate}>
            <div className="grid-2" style={{ marginBottom: 16 }}>
              <div className="form-group">
                <label>Symbol</label>
                <input placeholder="e.g. RELIANCE" value={form.symbol}
                  onChange={e => setForm(p => ({ ...p, symbol: e.target.value.toUpperCase() }))} />
              </div>
              <div className="form-group">
                <label>Company Name</label>
                <input placeholder="e.g. Reliance Industries Ltd" value={form.companyName}
                  onChange={e => setForm(p => ({ ...p, companyName: e.target.value }))} />
              </div>
              <div className="form-group">
                <label>Initial Price (₹)</label>
                <input type="number" min="0.01" step="0.01" placeholder="e.g. 2500.00" value={form.price}
                  onChange={e => setForm(p => ({ ...p, price: e.target.value }))} />
              </div>
              <div className="form-group">
                <label>Total Shares</label>
                <input type="number" min="1" placeholder="e.g. 1000000" value={form.totalShares}
                  onChange={e => setForm(p => ({ ...p, totalShares: e.target.value }))} />
              </div>
            </div>
            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? 'Creating...' : 'Create Stock'}
            </button>
          </form>
        </div>
      )}

      {/* Stock Management Table */}
      <div className="card">
        <div className="card-header">
          <span className="card-title">All Stocks ({stocks.length})</span>
          <span className="tag tag-green">{stocks.filter(s => s.tradable).length} Active</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>ID</th><th>Symbol</th><th>Company</th><th>Price</th><th>Total Shares</th><th>Status</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {stocks.map(s => (
                <tr key={s.id}>
                  <td className="mono muted" style={{ fontSize: 12 }}>{s.id}</td>
                  <td><span className="mono" style={{ fontWeight: 700, color: 'var(--accent)' }}>{s.symbol}</span></td>
                  <td style={{ fontSize: 13, color: 'var(--text-secondary)' }}>{s.companyName}</td>
                  <td><span className="mono">₹{Number(s.price).toFixed(2)}</span></td>
                  <td><span className="mono">{Number(s.totalShares).toLocaleString()}</span></td>
                  <td><span className={`tag ${s.tradable ? 'tag-green' : 'tag-red'}`}>{s.tradable ? 'Active' : 'Halted'}</span></td>
                  <td>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button
                        onClick={() => handleToggle(s)}
                        style={{
                          background: s.tradable ? 'var(--yellow-dim)' : 'var(--green-dim)',
                          color: s.tradable ? 'var(--yellow)' : 'var(--green)',
                          border: `1px solid ${s.tradable ? 'var(--yellow)' : 'var(--green)'}`,
                          padding: '6px 12px', fontSize: 12, borderRadius: 6,
                          display: 'flex', alignItems: 'center', gap: 4
                        }}
                      >
                        {s.tradable ? <><Pause size={12} /> Halt</> : <><Play size={12} /> Enable</>}
                      </button>
                      <button className="btn-danger" onClick={() => handleDelete(s.id, s.symbol)}
                        style={{ display: 'flex', alignItems: 'center', gap: 4 }}>
                        <Trash2 size={12} /> Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {stocks.length === 0 && (
                <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No stocks. Add one above.</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Stats */}
      <div className="grid-3" style={{ marginTop: 24 }}>
        <div className="card">
          <div className="card-title" style={{ marginBottom: 8 }}>Total Stocks</div>
          <div className="mono" style={{ fontSize: 28, fontWeight: 700, color: 'var(--accent)' }}>{stocks.length}</div>
        </div>
        <div className="card">
          <div className="card-title" style={{ marginBottom: 8 }}>Active Stocks</div>
          <div className="mono" style={{ fontSize: 28, fontWeight: 700, color: 'var(--green)' }}>{stocks.filter(s => s.tradable).length}</div>
        </div>
        <div className="card">
          <div className="card-title" style={{ marginBottom: 8 }}>Halted Stocks</div>
          <div className="mono" style={{ fontSize: 28, fontWeight: 700, color: 'var(--red)' }}>{stocks.filter(s => !s.tradable).length}</div>
        </div>
      </div>
    </div>
  )
}
