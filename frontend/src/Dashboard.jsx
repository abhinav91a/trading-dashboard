import { useEffect } from 'react'
import { useFeed } from './useFeed'
import { endSession } from './session'
import PriceTable from './PriceTable'
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
    <main>
      <header>
        <h1>Trading dashboard</h1>
        <div className="header-right">
          <span className={connected ? 'status on' : 'status off'}>
            {connected ? 'Live' : 'Reconnecting'}
          </span>
          <span className="user">Signed in as {session.username}</span>
          <button className="remove" onClick={() => endSession()}>Sign out</button>
        </div>
      </header>
      <div className="grid">
        <PriceTable prices={prices} />
        <AlertFeed alerts={alerts} />
        <AlertRulesPanel prices={prices} />
        <WatchlistPanel prices={prices} />
        <PortfolioPanel prices={prices} />
      </div>
    </main>
  )
}
