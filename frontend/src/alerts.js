const BASE = '/api/alerts'
// Until login exists (Step 8), every request is made as this user.
const headers = { 'Content-Type': 'application/json', 'X-User-Id': 'alice' }

export async function listRules() {
  const res = await fetch(BASE, { headers })
  if (!res.ok) throw new Error(`Load rules failed: ${res.status}`)
  return res.json()
}

export async function createRule(rule) {
  const res = await fetch(BASE, { method: 'POST', headers, body: JSON.stringify(rule) })
  if (res.status === 400) throw new Error('Rule rejected: check the threshold and cooldown')
  if (!res.ok) throw new Error(`Create rule failed: ${res.status}`)
  return res.json()
}

export async function deleteRule(id) {
  const res = await fetch(`${BASE}/${id}`, { method: 'DELETE', headers })
  if (!res.ok) throw new Error(`Delete rule failed: ${res.status}`)
}
