import { useEffect, useState } from 'react'

// Loads candles for one symbol and range. Old responses are ignored if the user switches again first.
export function useBars(symbol, timeframe, days) {
  const [candles, setCandles] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    setError('')
    setCandles([])

    fetch(`/api/market/bars/${symbol}?timeframe=${timeframe}&days=${days}`)
      .then((r) => {
        if (!r.ok) throw new Error(`Chart data unavailable (${r.status})`)
        return r.json()
      })
      .then((body) => {
        if (cancelled) return
        setCandles(
          (body.bars ?? []).map((b) => ({
            time: Math.floor(new Date(b.t).getTime() / 1000),
            open: b.o,
            high: b.h,
            low: b.l,
            close: b.c,
          }))
        )
      })
      .catch((e) => {
        if (!cancelled) setError(e.message)
      })

    return () => {
      cancelled = true
    }
  }, [symbol, timeframe, days])

  return { candles, error }
}
