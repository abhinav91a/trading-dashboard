import { useSymbols } from './useSymbols'
import { useEffect, useState } from 'react'
import { getPortfolioId, fetchPortfolio, recordTrade } from './portfolio'


const fmt = (n) => (n == null ? '—' : Number(n).toFixed(2))
const pnlClass = (n) => (n > 0 ? 'up' : n < 0 ? 'down' : '')

export default function PortfolioPanel({ prices }) {
  const symbols = useSymbols()
  const [portfolioId, setPortfolioId] = useState(null)
  const [view, setView] = useState(null)
  const [form, setForm] = useState({ symbol: 'AAPL', side: 'BUY', quantity: '', price: '' })
  const [message, setMessage] = useState('')

  useEffect(() => {
    getPortfolioId().then(setPortfolioId).catch((e) => setMessage(e.message))
  }, [])

  useEffect(() => {
    if (!portfolioId) return
    const load = () => fetchPortfolio(portfolioId).then(setView).catch((e) => setMessage(e.message))
    load()
    const timer = setInterval(load, 2000)
    return () => clearInterval(timer)
  }, [portfolioId])

  async function submit(e) {
    e.preventDefault()
    const price = form.price || prices[form.symbol]?.price
    if (!price) {
      setMessage(`No live price yet for ${form.symbol}`)
      return
    }
    try {
      await recordTrade(portfolioId, {
        symbol: form.symbol,
        side: form.side,
        quantity: Number(form.quantity),
        price: Number(price),
      })
      setMessage(`${form.side} ${form.quantity} ${form.symbol} at ${Number(price).toFixed(2)} recorded`)
      setForm((f) => ({ ...f, quantity: '', price: '' }))
      fetchPortfolio(portfolioId).then(setView)
    } catch (err) {
      setMessage(err.message)
    }
  }

  const positions = view?.positions ?? []
  const totalValue = positions.reduce((s, p) => s + Number(p.marketValue ?? 0), 0)
  const totalPnl = positions.reduce((s, p) => s + Number(p.unrealizedPnl ?? 0), 0)

  return (
    <section className="full">
      <h2>Portfolio</h2>
      <form className="trade-form" onSubmit={submit}>
        <select value={form.symbol} onChange={(e) => setForm({ ...form, symbol: e.target.value })}>
          {symbols.map((s) => <option key={s}>{s}</option>)}
        </select>
        <select value={form.side} onChange={(e) => setForm({ ...form, side: e.target.value })}>
          <option>BUY</option>
          <option>SELL</option>
        </select>
        <input
          type="number" step="any" min="0" required placeholder="Quantity"
          value={form.quantity}
          onChange={(e) => setForm({ ...form, quantity: e.target.value })}
        />
        <input
          type="number" step="0.01" min="0"
          placeholder={`Price (${prices[form.symbol]?.price ?? 'live'})`}
          value={form.price}
          onChange={(e) => setForm({ ...form, price: e.target.value })}
        />
        <button type="submit" disabled={!portfolioId}>Record trade</button>
      </form>
      {message && <p className="msg">{message}</p>}
      <table>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Quantity</th>
            <th>Avg cost</th>
            <th>Last</th>
            <th>Value</th>
            <th>P&amp;L</th>
          </tr>
        </thead>
        <tbody>
          {positions.length === 0 && (
            <tr>
              <td colSpan={6}>No positions yet</td>
            </tr>
          )}
          {positions.map((p) => (
            <tr key={p.symbol}>
              <td>{p.symbol}</td>
              <td>{Number(p.quantity)}</td>
              <td>{fmt(p.averageCost)}</td>
              <td>{fmt(p.lastPrice)}</td>
              <td>{fmt(p.marketValue)}</td>
              <td className={pnlClass(Number(p.unrealizedPnl))}>{fmt(p.unrealizedPnl)}</td>
            </tr>
          ))}
        </tbody>
        {positions.length > 0 && (
          <tfoot>
            <tr>
              <td colSpan={4}>Total</td>
              <td>{fmt(totalValue)}</td>
              <td className={pnlClass(totalPnl)}>{fmt(totalPnl)}</td>
            </tr>
          </tfoot>
        )}
      </table>
    </section>
  )
}
