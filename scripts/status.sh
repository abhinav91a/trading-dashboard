#!/usr/bin/env bash
# Read-only status check for the trading dashboard.

group_describe() {
  docker exec td-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
    --bootstrap-server localhost:9092 --describe --group "$1" 2>/dev/null
}

price_end_offsets() {
  group_describe alert-service | awk 'NR>1 && $2=="price-ticks" {s+=$5} END {print s+0}'
}

echo "== Docker"
if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running. Start Docker Desktop first."
  exit 1
fi
for c in td-kafka td-postgres td-redis; do
  state=$(docker inspect -f '{{.State.Status}}' "$c" 2>/dev/null)
  printf "%-12s %s\n" "$c" "${state:-missing}"
done

echo
echo "== Spring services"
for entry in market-data:8081 portfolio:8082 watchlist:8083 alert:8084 gateway:8080; do
  name=${entry%%:*}
  port=${entry##*:}
  code=$(curl -s -o /dev/null -m 3 -w '%{http_code}' "http://localhost:$port/actuator/health")
  if [ "$code" = "200" ]; then status="UP"; else status="DOWN ($code)"; fi
  printf "%-12s port %s  %s\n" "$name" "$port" "$status"
done

echo
echo "== Redis"
docker exec td-redis redis-cli ping

echo
echo "== Postgres (row counts are approximate)"
for db in portfolio_db watchlist_db alert_db; do
  info=$(docker exec td-postgres psql -U trader -d "$db" -tAc \
    "select string_agg(relname || '=' || n_live_tup, ', ' order by relname) from pg_stat_user_tables;" 2>/dev/null)
  echo "$db: ${info:-unreachable}"
done

echo
echo "== Kafka"
echo "topics: $(docker exec td-kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list 2>/dev/null | tr '\n' ' ')"
for g in portfolio-service alert-service api-gateway-feed; do
  lag=$(group_describe "$g" | awk 'NR>1 && $6 ~ /^[0-9]+$/ {s+=$6; n++} END {print (n ? s : "no data")}')
  echo "consumer group $g: total lag $lag"
done

echo
echo "== Price flow (5 second sample)"
before=$(price_end_offsets)
sleep 5
after=$(price_end_offsets)
if [ "$after" -gt "$before" ]; then
  echo "prices flowing: $((after - before)) new ticks in 5 seconds"
else
  echo "no new prices in 5 seconds, so check that market-data is running"
fi
