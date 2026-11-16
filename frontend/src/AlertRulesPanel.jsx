import { useEffect, useState } from 'react'
import { listRules, createRule, deleteRule } from './alerts'

const SYMBOLS = ['AAPL', 'MSFT', 'GOOGL', 'TSLA', 'AMZN']
const EMPTY = { symbol: 'AAPL', direction: 'ABOVE', threshold: '', cooldownSeconds: '' }

export default function AlertRulesPanel({ prices }) {
  const [rules, setRules] = useState([])
  const [form, setForm] = useState(EMPTY)
  const [message, setMessage] = useState('')

  useEffect(() => {
    listRules().then(setRules).catch((e) => setMessage(e.message))
  }, [])

  async function onCreate(e) {
    e.preventDefault()
    const body = {
      symbol: form.symbol,
      direction: form.direction,
      threshold: Number(form.threshold),
      ...(form.cooldownSeconds !== '' && { cooldownSeconds: Number(form.cooldownSeconds) }),
    }
    try {
      const created = await createRule(body)
      setRules((prev) => [...prev, created])
      setMessage(`Alert set: ${created.symbol} ${created.direction.toLowerCase()} ${Number(created.threshold).toFixed(2)}`)
      setForm((f) => ({ ...f, threshold: '', cooldownSeconds: '' }))
    } catch (err) {
      setMessage(err.message)
    }
  }

  async function onDelete(id) {
    try {
      await deleteRule(id)
      setRules((prev) => prev.filter((r) => r.id !== id))
      setMessage('Alert deleted')
    } catch (err) {
      setMessage(err.message)
    }
  }

  return (
    <section>
      <h2>Alert rules</h2>
      <form className="trade-form" onSubmit={onCreate}>
        <select value={form.symbol} onChange={(e) => setForm({ ...form, symbol: e.target.value })}>
          {SYMBOLS.map((s) => <option key={s}>{s}</option>)}
        </select>
        <select value={form.direction} onChange={(e) => setForm({ ...form, direction: e.target.value })}>
          <option>ABOVE</option>
          <option>BELOW</option>
        </select>
        <input
          type="number" step="0.01" min="0" required
          placeholder={`Threshold (${prices[form.symbol]?.price ?? 'live'})`}
          value={form.threshold}
          onChange={(e) => setForm({ ...form, threshold: e.target.value })}
        />
        <input
          type="number" step="1" min="0"
          placeholder="Cooldown s (300)"
          value={form.cooldownSeconds}
          onChange={(e) => setForm({ ...form, cooldownSeconds: e.target.value })}
        />
        <button type="submit">Set alert</button>
      </form>
      {message && <p className="msg">{message}</p>}
      <table>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Rule</th>
            <th>Cooldown</th>
            <th>Last fired</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {rules.length === 0 && (
            <tr>
              <td colSpan={5}>No alert rules yet</td>
            </tr>
          )}
          {rules.map((r) => (
            <tr key={r.id}>
              <td>{r.symbol}</td>
              <td>{r.direction.toLowerCase()} {Number(r.threshold).toFixed(2)}</td>
              <td>{r.cooldownSeconds}s</td>
              <td>{r.lastTriggeredAt ? new Date(r.lastTriggeredAt).toLocaleTimeString() : 'never'}</td>
              <td>
                <button className="remove" onClick={() => onDelete(r.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}
