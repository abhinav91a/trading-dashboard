import { useEffect, useState } from 'react'

// Fetched once per page load and shared by every component that needs the list
const FALLBACK = ['AAPL']
let cached = null

export function useSymbols() {
  const [symbols, setSymbols] = useState(cached ?? FALLBACK)

  useEffect(() => {
    if (cached) return
    fetch('/api/market/symbols')
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error(r.status))))
      .then((list) => {
        cached = list
        setSymbols(list)
      })
      .catch(() => {})
  }, [])

  return symbols
}
