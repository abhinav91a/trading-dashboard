export default function AlertFeed({ alerts }) {
  return (
    <section>
      <h2>Triggered alerts</h2>
      {alerts.length === 0 && <p>No alerts fired yet.</p>}
      <ul>
        {alerts.map((a) => (
          <li key={a.ruleId + a.triggeredAt}>
            <strong>{a.userId}</strong> {a.symbol} {a.direction.toLowerCase()} {Number(a.threshold)}
            {' '}(price {Number(a.price).toFixed(2)}) at {new Date(a.triggeredAt).toLocaleTimeString()}
          </li>
        ))}
      </ul>
    </section>
  )
}
