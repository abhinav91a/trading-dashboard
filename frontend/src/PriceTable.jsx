import { useSymbols } from './useSymbols'

export default function PriceTable({ prices }) {
  const symbols = useSymbols()
  const rows = symbols.map((s) => prices[s] ?? { symbol: s, price: null, previous: null, timestamp: null })

  return (
    <section className="full">
      <h2>Live prices</h2>
      <table>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Price</th>
            <th>Change</th>
            <th>Updated</th>
          </tr>
        </thead>
        <tbody>
          {rows.map((p) => {
            const change = p.price == null || p.previous == null ? 0 : p.price - p.previous
            const cls = change > 0 ? 'up' : change < 0 ? 'down' : ''
            return (
              <tr key={p.symbol}>
                <td>{p.symbol}</td>
                <td>{p.price == null ? '—' : Number(p.price).toFixed(2)}</td>
                <td className={cls}>{change ? (change > 0 ? '+' : '') + change.toFixed(2) : '—'}</td>
                <td>{p.timestamp ? new Date(p.timestamp).toLocaleTimeString() : '—'}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </section>
  )
}
