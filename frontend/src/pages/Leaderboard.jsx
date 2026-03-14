import { useEffect, useState } from 'react'
import { getLeaderboard } from '../services/api'
import { useAuth } from '../context/AuthContext'
import { Trophy, Medal } from 'lucide-react'

export default function Leaderboard() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const { user } = useAuth()

  useEffect(() => {
    getLeaderboard()
      .then(r => {
        const entries = Object.entries(r.data).map(([username, value], index) => ({ rank: index + 1, username, value }))
        setData(entries)
      })
      .finally(() => setLoading(false))
  }, [])

  const rankIcon = (rank) => {
    if (rank === 1) return <Trophy size={16} style={{ color: '#ffd700' }} />
    if (rank === 2) return <Medal size={16} style={{ color: '#c0c0c0' }} />
    if (rank === 3) return <Medal size={16} style={{ color: '#cd7f32' }} />
    return <span className="mono" style={{ color: 'var(--text-muted)', fontSize: 13 }}>#{rank}</span>
  }

  if (loading) return <div className="page"><div className="spinner" /></div>

  return (
    <div className="page fade-in">
      <h1 className="page-title">Leaderboard</h1>
      <p className="muted" style={{ marginBottom: 24, fontSize: 13 }}>Top 10 traders ranked by total portfolio value + balance</p>

      <div className="card">
        <div className="card-header">
          <span className="card-title">Top Traders</span>
          <span className="tag tag-yellow"><Trophy size={11} /> Rankings</span>
        </div>
        <div className="table-wrap">
          <table>
            <thead>
              <tr><th>Rank</th><th>Trader</th><th>Total Value</th></tr>
            </thead>
            <tbody>
              {data.map(entry => (
                <tr key={entry.username} style={{
                  background: entry.username === user?.username ? 'var(--accent-dim)' : undefined
                }}>
                  <td style={{ width: 60 }}>{rankIcon(entry.rank)}</td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                      <div style={{
                        width: 30, height: 30, borderRadius: '50%',
                        background: entry.rank === 1 ? 'linear-gradient(135deg, #ffd700, #ff8c00)' :
                          entry.rank === 2 ? 'linear-gradient(135deg, #c0c0c0, #808080)' :
                            entry.rank === 3 ? 'linear-gradient(135deg, #cd7f32, #8b4513)' :
                              'linear-gradient(135deg, var(--accent), var(--blue))',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        color: '#000', fontWeight: 800, fontSize: 12
                      }}>
                        {entry.username[0].toUpperCase()}
                      </div>
                      <span style={{ fontWeight: 600 }}>
                        {entry.username}
                        {entry.username === user?.username && (
                          <span className="tag tag-green" style={{ marginLeft: 8, fontSize: 9 }}>You</span>
                        )}
                      </span>
                    </div>
                  </td>
                  <td>
                    <span className="mono" style={{ fontWeight: 700, color: entry.rank <= 3 ? 'var(--yellow)' : 'var(--text-primary)', fontSize: 15 }}>
                      ₹{Number(entry.value).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </span>
                  </td>
                </tr>
              ))}
              {data.length === 0 && (
                <tr><td colSpan={3} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No data yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
