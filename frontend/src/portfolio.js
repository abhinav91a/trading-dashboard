const BASE = '/api/portfolios'
const KEY = 'portfolioId'

export async function getPortfolioId() {
  const saved = localStorage.getItem(KEY)
  if (saved) return saved

  const res = await fetch(BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name: 'My portfolio' }),
  })
  if (!res.ok) throw new Error(`Create portfolio failed: ${res.status}`)
  const { id } = await res.json()
  localStorage.setItem(KEY, id)
  return id
}

export async function fetchPortfolio(id) {
  const res = await fetch(`${BASE}/${id}`)
  if (!res.ok) throw new Error(`Load portfolio failed: ${res.status}`)
  return res.json()
}

export async function recordTrade(id, trade) {
  const res = await fetch(`${BASE}/${id}/trades`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(trade),
  })
  if (res.status === 400) throw new Error('Trade rejected: check the quantity and your holdings')
  if (!res.ok) throw new Error(`Trade failed: ${res.status}`)
  return res.json()
}
