import { authFetch, JSON_HEADERS } from './session'

const BASE = '/api/alerts'

export async function listRules() {
  const res = await authFetch(BASE)
  if (!res.ok) throw new Error(`Load rules failed: ${res.status}`)
  return res.json()
}

export async function createRule(rule) {
  const res = await authFetch(BASE, { method: 'POST', headers: JSON_HEADERS, body: JSON.stringify(rule) })
  if (res.status === 400) throw new Error('Rule rejected: check the threshold and cooldown')
  if (!res.ok) throw new Error(`Create rule failed: ${res.status}`)
  return res.json()
}

export async function deleteRule(id) {
  const res = await authFetch(`${BASE}/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error(`Delete rule failed: ${res.status}`)
}
