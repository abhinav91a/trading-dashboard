import { useEffect, useState } from 'react'
import { authFetch } from './session'

const MAX_ALERTS = 50

export function useFeed(token) {
  const [prices, setPrices] = useState({})
  const [alerts, setAlerts] = useState([])
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    let socket
    let retryTimer
    let closed = false
    let failures = 0

    function connect() {
      if (closed) return
      const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
      const url = `${scheme}://${window.location.host}/ws/feed?token=${encodeURIComponent(token)}`
      socket = new WebSocket(url)

      socket.onopen = () => {
        failures = 0
        setConnected(true)
      }

      socket.onmessage = (event) => {
        const { topic, data } = JSON.parse(event.data)
        if (topic === 'price-ticks') {
          setPrices((prev) => ({
            ...prev,
            [data.symbol]: { ...data, previous: prev[data.symbol]?.price ?? null },
          }))
        } else if (topic === 'alert-triggered') {
          setAlerts((prev) => [data, ...prev].slice(0, MAX_ALERTS))
        }
      }

      socket.onclose = () => {
        setConnected(false)
        if (closed) return
        failures += 1
        if (failures >= 3) {
          // Browsers hide the handshake status, so a REST call tells us whether the token is bad.
          // A 401 ends the session. Other errors are ignored and the socket keeps retrying.
          authFetch('/api/watchlists').catch(() => {})
        }
        retryTimer = setTimeout(connect, Math.min(2000 * failures, 15000))
      }
    }

    connect()
    return () => {
      closed = true
      clearTimeout(retryTimer)
      socket?.close()
    }
  }, [token])

  return { prices, alerts, connected }
}
