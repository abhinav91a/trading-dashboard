import { useEffect, useState } from 'react'
import { getSession } from './session'
import LoginScreen from './LoginScreen'
import Dashboard from './Dashboard'
import './App.css'

export default function App() {
  const [session, setSession] = useState(getSession)

  // endSession() fires this event on sign-out, on expiry, and on any 401 from an API call
  useEffect(() => {
    const clear = () => setSession(null)
    window.addEventListener('session-ended', clear)
    return () => window.removeEventListener('session-ended', clear)
  }, [])

  if (!session) return <LoginScreen onSignedIn={setSession} />
  return <Dashboard session={session} />
}
