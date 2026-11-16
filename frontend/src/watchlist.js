import { authFetch, JSON_HEADERS } from './session'

const BASE = '/api/watchlists'

async function check(res, action) {
  if (res.status === 409) throw new Error(`${action}: already exists`)
  if (!res.ok) throw new Error(`${action} failed: ${res.status}`)
  return res.status === 204 ? null : res.json()
}

export const listWatchlists = () =>
  authFetch(BASE).then((r) => check(r, 'Load lists'))

export const createWatchlist = (name) =>
  authFetch(BASE, { method: 'POST', headers: JSON_HEADERS, body: JSON.stringify({ name }) })
    .then((r) => check(r, 'Create list'))

export const getWatchlist = (id) =>
  authFetch(`${BASE}/${id}`).then((r) => check(r, 'Load list'))

export const addSymbol = (id, symbol) =>
  authFetch(`${BASE}/${id}/items`, { method: 'POST', headers: JSON_HEADERS, body: JSON.stringify({ symbol }) })
    .then((r) => check(r, 'Add symbol'))

export const removeSymbol = (id, symbol) =>
  authFetch(`${BASE}/${id}/items/${symbol}`, { method: 'DELETE' })
    .then((r) => check(r, 'Remove symbol'))
