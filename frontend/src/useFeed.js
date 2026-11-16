import { useEffect, useState } from 'react'

const MAX_ALERTS = 50

// Opens the gateway's WebSocket and keeps the latest prices and alerts in state.
export function useFeed() {
  const [prices, setPrices] = useState({})
  const [alerts, setAlerts] = useState([])
  const [connected, setConnected] = useState(false)

  useEffect(() => {
    let socket
    let retryTimer
    let closed = false

    function connect() {
      const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
      socket = new WebSocket(`${scheme}://${window.location.host}/ws/feed`)

      socket.onopen = () => setConnected(true)

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
        if (!closed) retryTimer = setTimeout(connect, 2000)
      }
    }

    connect()
    return () => {
      closed = true
      clearTimeout(retryTimer)
      socket?.close()
    }
  }, [])

  return { prices, alerts, connected }
}
