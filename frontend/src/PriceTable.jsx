export default function PriceTable({ prices }) {
  const rows = Object.values(prices).sort((a, b) => a.symbol.localeCompare(b.symbol))

  return (
    <section>
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
          {rows.length === 0 && (
            <tr>
              <td colSpan={4}>Waiting for prices</td>
            </tr>
          )}
          {rows.map((p) => {
            const change = p.previous == null ? 0 : p.price - p.previous
            const cls = change > 0 ? 'up' : change < 0 ? 'down' : ''
            return (
              <tr key={p.symbol}>
                <td>{p.symbol}</td>
                <td>{Number(p.price).toFixed(2)}</td>
                <td className={cls}>
                  {change ? (change > 0 ? '+' : '') + change.toFixed(2) : '—'}
                </td>
                <td>{new Date(p.timestamp).toLocaleTimeString()}</td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </section>
  )
}
