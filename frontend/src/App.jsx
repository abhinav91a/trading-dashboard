import { useFeed } from './useFeed'
import PriceTable from './PriceTable'
import AlertFeed from './AlertFeed'
import './App.css'

export default function App() {
  const { prices, alerts, connected } = useFeed()

  return (
    <main>
      <header>
        <h1>Trading dashboard</h1>
        <span className={connected ? 'status on' : 'status off'}>
          {connected ? 'Live' : 'Reconnecting'}
        </span>
      </header>
      <div className="grid">
        <PriceTable prices={prices} />
        <AlertFeed alerts={alerts} />
      </div>
    </main>
  )
}
