import { authFetch, getSession, JSON_HEADERS } from './session'

const BASE = '/api/portfolios'

// React runs effects twice in development, so two calls can arrive at once.
// Sharing one in-flight request per user stops them from creating two portfolios.
const inFlight = new Map()

export function getPortfolioId() {
  const session = getSession()
  if (!session) return Promise.reject(new Error('Not signed in'))

  if (!inFlight.has(session.userId)) {
    const request = findOrCreatePortfolio().finally(() => inFlight.delete(session.userId))
    inFlight.set(session.userId, request)
  }
  return inFlight.get(session.userId)
}

async function findOrCreatePortfolio() {
  const listRes = await authFetch(BASE)
  if (!listRes.ok) throw new Error(`Load portfolios failed: ${listRes.status}`)
  const existing = await listRes.json()
  if (existing.length > 0) return existing[0].id

  const createRes = await authFetch(BASE, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ name: 'My portfolio' }),
  })
  if (!createRes.ok) throw new Error(`Create portfolio failed: ${createRes.status}`)
  return (await createRes.json()).id
}

export async function fetchPortfolio(id) {
  const res = await authFetch(`${BASE}/${id}`)
  if (!res.ok) throw new Error(`Load portfolio failed: ${res.status}`)
  return res.json()
}

export async function recordTrade(id, trade) {
  const res = await authFetch(`${BASE}/${id}/trades`, {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify(trade),
  })
  if (res.status === 400) throw new Error('Trade rejected: check the quantity and your holdings')
  if (!res.ok) throw new Error(`Trade failed: ${res.status}`)
  return res.json()
}
