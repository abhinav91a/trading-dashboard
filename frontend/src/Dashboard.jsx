import { useEffect } from 'react'
import { useFeed } from './useFeed'
import { endSession } from './session'
import PriceTable from './PriceTable'
import ChartPanel from './ChartPanel'
import AlertFeed from './AlertFeed'
import AlertRulesPanel from './AlertRulesPanel'
import WatchlistPanel from './WatchlistPanel'
import PortfolioPanel from './PortfolioPanel'

export default function Dashboard({ session }) {
  const { prices, alerts, connected } = useFeed(session.token)

  // The gateway checks the token only when the socket opens, so sign out at expiry
  useEffect(() => {
    const timer = setTimeout(endSession, session.expiresAt - Date.now())
    return () => clearTimeout(timer)
  }, [session.expiresAt])

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <span className="dot" />
          <h1>Trading dashboard</h1>
        </div>
        <div className="header-right">
          <span className={connected ? 'status on' : 'status off'}>
            {connected ? 'Live' : 'Reconnecting'}
          </span>
          <span className="user">Signed in as {session.username}</span>
          <button className="remove" onClick={() => endSession()}>Sign out</button>
        </div>
      </header>

      <div className="workspace">
        <div className="cell chart-cell"><ChartPanel /></div>
        <div className="cell price-cell"><PriceTable prices={prices} /></div>
        <div className="cell portfolio-cell"><PortfolioPanel prices={prices} /></div>
        <div className="bottom">
          <div className="cell"><WatchlistPanel prices={prices} /></div>
          <div className="cell"><AlertRulesPanel prices={prices} /></div>
          <div className="cell"><AlertFeed alerts={alerts} /></div>
        </div>
      </div>
    </div>
  )
}
