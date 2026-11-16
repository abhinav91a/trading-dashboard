import { useState } from 'react'
import { useSymbols } from './useSymbols'
import { useBars } from './useBars'
import CandlestickChart from './CandlestickChart'

const RANGES = [
  { label: '1W', timeframe: '1Hour', days: 7 },
  { label: '1M', timeframe: '1Day', days: 30 },
  { label: '6M', timeframe: '1Day', days: 182 },
  { label: '1Y', timeframe: '1Day', days: 365 },
]

export default function ChartPanel() {
  const symbols = useSymbols()
  const [symbol, setSymbol] = useState('AAPL')
  const [range, setRange] = useState(RANGES[1])
  const { candles, error } = useBars(symbol, range.timeframe, range.days)

  return (
    <section className="full">
      <div className="chart-head">
        <h2>{symbol} chart</h2>
        <select value={symbol} onChange={(e) => setSymbol(e.target.value)}>
          {symbols.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
        <div className="ranges">
          {RANGES.map((r) => (
            <button
              key={r.label}
              className={r === range ? 'range active' : 'range'}
              onClick={() => setRange(r)}
            >
              {r.label}
            </button>
          ))}
        </div>
      </div>
      {error && <p className="msg">{error}</p>}
      {!error && candles.length === 0 && <p className="msg">Loading chart data...</p>}
      <CandlestickChart candles={candles} />
    </section>
  )
}
