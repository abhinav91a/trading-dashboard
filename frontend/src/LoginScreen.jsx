import { useState } from 'react'
import { login, register } from './session'

export default function LoginScreen({ onSignedIn }) {
  const [mode, setMode] = useState('signin')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const creating = mode === 'create'

  async function submit(e) {
    e.preventDefault()
    const name = username.trim()
    if (name.length < 3) {
      setError('Username must be at least 3 characters')
      return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters')
      return
    }

    setBusy(true)
    setError('')
    try {
      if (creating) await register(name, password)
      onSignedIn(await login(name, password))
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  return (
    <section className="login">
      <h2>{creating ? 'Create account' : 'Sign in'}</h2>
      <form onSubmit={submit}>
        <input
          placeholder="Username"
          autoComplete="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
        />
        <input
          type="password"
          placeholder="Password"
          autoComplete={creating ? 'new-password' : 'current-password'}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={busy}>
          {creating ? 'Create account' : 'Sign in'}
        </button>
      </form>
      <p>
        <button
          type="button"
          className="link"
          onClick={() => {
            setMode(creating ? 'signin' : 'create')
            setError('')
          }}
        >
          {creating ? 'Already have an account? Sign in' : 'New here? Create an account'}
        </button>
      </p>
    </section>
  )
}
