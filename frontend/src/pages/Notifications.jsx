import { useEffect, useState } from 'react'
import { getNotifications, markAllRead } from '../services/api'
import { useToast } from '../context/ToastContext'
import { Bell, CheckCheck, TrendingUp, TrendingDown } from 'lucide-react'

export default function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)
  const toast = useToast()

  const load = () => {
    getNotifications()
      .then(r => setNotifications(r.data))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const handleMarkAll = async () => {
    try {
      await markAllRead()
      toast.success('All notifications marked as read')
      load()
    } catch { toast.error('Failed') }
  }

  const unread = notifications.filter(n => !n.read).length

  if (loading) return <div className="page"><div className="spinner" /></div>

  return (
    <div className="page fade-in">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 24 }}>
        <div>
          <h1 className="page-title" style={{ marginBottom: 4 }}>Notifications</h1>
          {unread > 0 && <span className="tag tag-red">{unread} unread</span>}
        </div>
        {unread > 0 && (
          <button className="btn-secondary" onClick={handleMarkAll} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <CheckCheck size={15} /> Mark All Read
          </button>
        )}
      </div>

      <div className="card">
        {notifications.length === 0 ? (
          <div className="empty-state" style={{ padding: 60 }}>
            <Bell size={40} className="muted" />
            <p className="muted" style={{ marginTop: 12 }}>No notifications yet</p>
            <p className="muted" style={{ fontSize: 12 }}>Trade activity will appear here</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {notifications.map((n, i) => {
              const isBuy = n.message?.toLowerCase().includes('bought')
              return (
                <div key={n.id} style={{
                  display: 'flex', alignItems: 'flex-start', gap: 14, padding: '14px 0',
                  borderBottom: i < notifications.length - 1 ? '1px solid var(--border)' : 'none',
                  opacity: n.read ? 0.6 : 1,
                  transition: 'opacity 0.2s'
                }}>
                  <div style={{
                    width: 36, height: 36, borderRadius: '50%', flexShrink: 0,
                    background: isBuy ? 'var(--green-dim)' : 'var(--red-dim)',
                    border: `1px solid ${isBuy ? 'var(--green)' : 'var(--red)'}`,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    color: isBuy ? 'var(--green)' : 'var(--red)'
                  }}>
                    {isBuy ? <TrendingUp size={16} /> : <TrendingDown size={16} />}
                  </div>
                  <div style={{ flex: 1 }}>
                    <div style={{ fontSize: 14, fontWeight: n.read ? 400 : 600 }}>{n.message}</div>
                    <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 3 }}>
                      {n.createdAt ? new Date(n.createdAt).toLocaleString('en-IN') : ''}
                    </div>
                  </div>
                  {!n.read && (
                    <div style={{ width: 8, height: 8, borderRadius: '50%', background: 'var(--accent)', flexShrink: 0, marginTop: 4 }} />
                  )}
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
