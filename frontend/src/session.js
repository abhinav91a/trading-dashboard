const TOKEN_KEY = 'session.token'

export const JSON_HEADERS = { 'Content-Type': 'application/json' }

// Reads the token's payload. This only decodes it so the page knows the user and expiry.
// The servers still verify the signature.
function decodeClaims(token) {
  const part = token.split('.')[1]
  const base64 = part.replace(/-/g, '+').replace(/_/g, '/')
  const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
  const bytes = Uint8Array.from(atob(padded), (c) => c.charCodeAt(0))
  return JSON.parse(new TextDecoder().decode(bytes))
}

export function getSession() {
  const token = localStorage.getItem(TOKEN_KEY)
  if (!token) return null
  try {
    const claims = decodeClaims(token)
    if (!claims.exp || claims.exp * 1000 <= Date.now()) throw new Error('expired')
    return {
      token,
      userId: claims.sub,
      username: claims.username,
      expiresAt: claims.exp * 1000,
    }
  } catch {
    localStorage.removeItem(TOKEN_KEY)
    return null
  }
}

// Clears the session and tells the app to show the login screen
export function endSession() {
  localStorage.removeItem(TOKEN_KEY)
  window.dispatchEvent(new Event('session-ended'))
}

// fetch that adds the token. A 401 means the token is expired or invalid, so end the session.
export async function authFetch(url, options = {}) {
  const session = getSession()
  if (!session) {
    endSession()
    throw new Error('Not signed in')
  }
  const res = await fetch(url, {
    ...options,
    headers: { ...options.headers, Authorization: `Bearer ${session.token}` },
  })
  if (res.status === 401) {
    endSession()
    throw new Error('Session expired')
  }
  return res
}

export async function register(username, password) {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ username, password }),
  })
  if (res.status === 409) throw new Error('That username is taken')
  if (res.status === 400) throw new Error('Username must be 3 to 50 characters and password 8 to 72')
  if (!res.ok) throw new Error(`Registration failed (${res.status})`)
}

export async function login(username, password) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: JSON_HEADERS,
    body: JSON.stringify({ username, password }),
  })
  if (res.status === 401) throw new Error('Wrong username or password')
  if (!res.ok) throw new Error(`Sign-in failed (${res.status})`)
  const { token } = await res.json()
  localStorage.setItem(TOKEN_KEY, token)
  return getSession()
}
