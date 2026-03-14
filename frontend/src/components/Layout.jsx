import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useEffect, useState } from 'react'
import { getMarketStatus, getNotifications } from '../services/api'
import {
  LayoutDashboard, TrendingUp, Briefcase, Star,
  Trophy, Bell, Shield, LogOut, Zap
} from 'lucide-react'
import './Layout.css'

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/trade', label: 'Trade', icon: TrendingUp },
  { path: '/portfolio', label: 'Portfolio', icon: Briefcase },
  { path: '/watchlist', label: 'Watchlist', icon: Star },
  { path: '/leaderboard', label: 'Leaderboard', icon: Trophy },
  { path: '/notifications', label: 'Notifications', icon: Bell },
]

export default function Layout() {
  const { user, logoutUser } = useAuth()
  const navigate = useNavigate()
  const [marketStatus, setMarketStatus] = useState('CLOSED')
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    getMarketStatus().then(r => setMarketStatus(r.data)).catch(() => {})
    getNotifications().then(r => {
      setUnreadCount(r.data.filter(n => !n.read).length)
    }).catch(() => {})
    const interval = setInterval(() => {
      getMarketStatus().then(r => setMarketStatus(r.data)).catch(() => {})
      getNotifications().then(r => setUnreadCount(r.data.filter(n => !n.read).length)).catch(() => {})
    }, 10000)
    return () => clearInterval(interval)
  }, [])

  const handleLogout = () => {
    logoutUser()
    navigate('/login')
  }

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <Zap size={20} className="logo-icon" />
          <span className="logo-text">TradePro</span>
        </div>

        <div className="market-status-pill">
          <span className={`status-dot ${marketStatus === 'OPEN' ? 'open' : 'closed'}`} />
          <span className="status-label">Market {marketStatus}</span>
        </div>

        <nav className="sidebar-nav">
          {navItems.map(({ path, label, icon: Icon }) => (
            <NavLink
              key={path}
              to={path}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Icon size={18} />
              <span>{label}</span>
              {label === 'Notifications' && unreadCount > 0 && (
                <span className="badge">{unreadCount}</span>
              )}
            </NavLink>
          ))}
          {user?.role === 'ADMIN' && (
            <NavLink to="/admin" className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}>
              <Shield size={18} />
              <span>Admin</span>
            </NavLink>
          )}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <div className="user-avatar">{user?.username?.[0]?.toUpperCase()}</div>
            <div>
              <div className="user-name">{user?.username}</div>
              <div className="user-balance mono">₹{Number(user?.balance || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</div>
            </div>
          </div>
          <button className="logout-btn" onClick={handleLogout} title="Logout">
            <LogOut size={16} />
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
