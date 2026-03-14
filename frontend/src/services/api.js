import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// Auth
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)

// User
export const getMe = () => api.get('/users/me')

// Stocks
export const getStocks = () => api.get('/stocks')
export const getStock = (symbol) => api.get(`/stocks/${symbol}`)

// Orders
export const placeOrder = (data) => api.post('/orders/place', data)
export const getMyOrders = () => api.get('/orders/my')
export const getBuyOrders = (symbol) => api.get(`/orders/book/${symbol}/buy`)
export const getSellOrders = (symbol) => api.get(`/orders/book/${symbol}/sell`)

// Portfolio
export const getPortfolio = () => api.get('/portfolio')

// Trades
export const getMyTrades = () => api.get('/trades/my')

// Watchlist
export const getWatchlist = () => api.get('/watchlist')
export const addToWatchlist = (symbol) => api.post(`/watchlist/add/${symbol}`)
export const removeFromWatchlist = (symbol) => api.delete(`/watchlist/remove/${symbol}`)

// Analytics
export const getPnL = () => api.get('/analytics/pnl')
export const getLeaderboard = () => api.get('/leaderboard')

// Market
export const getMarketStatus = () => api.get('/market/status')

// Price history
export const getPriceHistory = (symbol) => api.get(`/prices/${symbol}`)

// Notifications
export const getNotifications = () => api.get('/notifications')
export const markAllRead = () => api.post('/notifications/read-all')

// Admin
export const adminCreateStock = (data) => api.post('/admin/stocks/create', data)
export const adminDeleteStock = (id) => api.delete(`/admin/stocks/delete/${id}`)
export const adminEnableTrading = (id) => api.post(`/admin/stocks/${id}/enable`)
export const adminDisableTrading = (id) => api.post(`/admin/stocks/${id}/disable`)

export default api
