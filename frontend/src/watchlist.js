const BASE = '/api/watchlists'
// Until login exists (Step 8), every request is made as this user.
const USER_ID = 'alice'
const headers = { 'Content-Type': 'application/json', 'X-User-Id': USER_ID }

async function check(res, action) {
  if (res.status === 409) throw new Error(`${action}: already exists`)
  if (!res.ok) throw new Error(`${action} failed: ${res.status}`)
  return res.status === 204 ? null : res.json()
}

export function listWatchlists() {
  return fetch(BASE, { headers }).then((r) => check(r, 'Load lists'))
}

export function createWatchlist(name) {
  return fetch(BASE, { method: 'POST', headers, body: JSON.stringify({ name }) })
    .then((r) => check(r, 'Create list'))
}

export function getWatchlist(id) {
  return fetch(`${BASE}/${id}`, { headers }).then((r) => check(r, 'Load list'))
}

export function addSymbol(id, symbol) {
  return fetch(`${BASE}/${id}/items`, { method: 'POST', headers, body: JSON.stringify({ symbol }) })
    .then((r) => check(r, 'Add symbol'))
}

export function removeSymbol(id, symbol) {
  return fetch(`${BASE}/${id}/items/${symbol}`, { method: 'DELETE', headers })
    .then((r) => check(r, 'Remove symbol'))
}
