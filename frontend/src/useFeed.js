import { useEffect, useState } from 'react'
import { authFetch } from './session'

const MAX_ALERTS = 50

// Last trade and previous close for every configured symbol. This fills the table before the
// first live tick and outside market hours. Market data is public, so no token is sent.
async function loadSnapshots() {
  const symbols = await fetch('/api/market/symbols').then((r) => r.json())
  return fetch(`/api/market/snapshots?symbols=${symbols.join(',')}`).then((r) => r.json())
}

export function useFeed(token) {
  const [prices, setPrices] = useState({})
  const [alerts, setAlerts] = useState([])
  const [connected, setConnected] = useState(false)

  // A live tick that arrived first keeps its price. The snapshot only supplies the previous close.
  useEffect(() => {
    let cancelled = false
    loadSnapshots()
      .then((rows) => {
        if (cancelled) return
        setPrices((prev) => {
          const next = { ...prev }
          for (const row of rows) {
            const current = next[row.symbol]
            next[row.symbol] = current
              ? { ...current, previous: current.previous ?? row.previousClose }
              : { symbol: row.symbol, price: row.price, previous: row.previousClose, timestamp: row.timestamp }
          }
          return next
        })
      })
      .catch(() => {})
    return () => {
      cancelled = true
    }
  }, [])

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
          // "previous" is the previous close. It carries over from the snapshot, so Change
          // shows the move since the last session rather than since the last tick.
          setPrices((prev) => ({
            ...prev,
            [data.symbol]: { ...data, previous: prev[data.symbol]?.previous ?? null },
          }))
        } else if (topic === 'alert-triggered') {
          setAlerts((prev) => [data, ...prev].slice(0, MAX_ALERTS))
        }
      }

      socket.onclose = () => {
        setConnected(false)
        if (closed) return
        failures += 1
        if (failures >= 3) authFetch('/api/watchlists').catch(() => {})
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
