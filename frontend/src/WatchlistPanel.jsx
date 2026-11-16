import { useEffect, useState } from 'react'
import {
  listWatchlists,
  createWatchlist,
  getWatchlist,
  addSymbol,
  removeSymbol,
} from './watchlist'

const fmt = (n) => (n == null ? '—' : Number(n).toFixed(2))

export default function WatchlistPanel({ prices }) {
  const [lists, setLists] = useState([])
  const [selectedId, setSelectedId] = useState(null)
  const [view, setView] = useState(null)
  const [newListName, setNewListName] = useState('')
  const [newSymbol, setNewSymbol] = useState('')
  const [message, setMessage] = useState('')

  // Load the user's lists and select the first one. Lists are created only from the form,
  // so React's double-run of effects in development can't create duplicates.
  useEffect(() => {
    listWatchlists()
      .then((all) => {
        setLists(all)
        if (all.length > 0) setSelectedId(all[0].id)
      })
      .catch((e) => setMessage(e.message))
  }, [])

  // Load the selected list's symbols whenever the selection changes.
  useEffect(() => {
    if (!selectedId) return
    getWatchlist(selectedId).then(setView).catch((e) => setMessage(e.message))
  }, [selectedId])

  // Runs a write, shows the result, and reloads the list. Returns true on success.
  async function run(action, successText) {
    try {
      await action()
      setMessage(successText)
      setView(await getWatchlist(selectedId))
      return true
    } catch (e) {
      setMessage(e.message)
      return false
    }
  }

  async function onCreateList(e) {
    e.preventDefault()
    const name = newListName.trim()
    if (!name) return
    try {
      const created = await createWatchlist(name)
      setLists((prev) => [...prev, created].sort((a, b) => a.name.localeCompare(b.name)))
      setSelectedId(created.id)
      setNewListName('')
      setMessage(`Created list ${name}`)
    } catch (err) {
      setMessage(err.message)
    }
  }

  async function onAddSymbol(e) {
    e.preventDefault()
    const symbol = newSymbol.trim().toUpperCase()
    if (!symbol) return
    if (await run(() => addSymbol(selectedId, symbol), `Added ${symbol}`)) {
      setNewSymbol('')
    }
  }

  const symbols = view?.symbols ?? []

  return (
    <section>
      <h2>Watchlist</h2>
      <form className="trade-form" onSubmit={onAddSymbol}>
        <select value={selectedId ?? ''} onChange={(e) => setSelectedId(e.target.value)}>
          {lists.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
        </select>
        <input
          placeholder="Symbol"
          value={newSymbol}
          onChange={(e) => setNewSymbol(e.target.value)}
        />
        <button type="submit" disabled={!selectedId}>Add symbol</button>
      </form>
      <form className="trade-form" onSubmit={onCreateList}>
        <input
          placeholder="New list name"
          value={newListName}
          onChange={(e) => setNewListName(e.target.value)}
        />
        <button type="submit">Create list</button>
      </form>
      {message && <p className="msg">{message}</p>}
      <table>
        <thead>
          <tr>
            <th>Symbol</th>
            <th>Price</th>
            <th>Change</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {!selectedId && (
            <tr>
              <td colSpan={4}>Create a list to get started</td>
            </tr>
          )}
          {selectedId && symbols.length === 0 && (
            <tr>
              <td colSpan={4}>No symbols in this list</td>
            </tr>
          )}
          {symbols.map((s) => {
            const p = prices[s]
            const change = p?.previous == null ? 0 : p.price - p.previous
            const cls = change > 0 ? 'up' : change < 0 ? 'down' : ''
            return (
              <tr key={s}>
                <td>{s}</td>
                <td>{fmt(p?.price)}</td>
                <td className={cls}>{change ? (change > 0 ? '+' : '') + change.toFixed(2) : '—'}</td>
                <td>
                  <button
                    className="remove"
                    onClick={() => run(() => removeSymbol(selectedId, s), `Removed ${s}`)}
                  >
                    Remove
                  </button>
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </section>
  )
}
