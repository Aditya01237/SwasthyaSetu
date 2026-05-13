# ELK Observability

SwasthyaSetu includes an optional ELK overlay for centralized local logs:

- Elasticsearch stores logs.
- Logstash receives Docker container logs through the GELF logging driver.
- Kibana lets you search logs from the browser.

## Start ELK With The App

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build -d
sh scripts/ci/health-check-observability.sh
sh scripts/ci/smoke-elk-log.sh
```

For split service databases plus ELK:

```bash
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml -f docker-compose.observability.yml up --build -d
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml --profile service-dbs run --rm service-db-sync
sh scripts/ci/health-check-observability.sh
sh scripts/ci/smoke-elk-log.sh
```

To start only ELK and run the smoke evidence check:

```bash
sh scripts/ci/check-elk-observability.sh
```

## Useful URLs

- Kibana: `http://localhost:5601`
- Elasticsearch health: `http://localhost:9200/_cluster/health`
- Logstash API: `http://localhost:9600/_node/pipelines`

## Find Logs In Kibana

1. Open Kibana.
2. Go to Discover.
3. Create a data view with `swasthya-setu-logs-*`.
4. Search by fields like `service_name`, `docker_container`, `message`, or `short_message`.

## Final Demo Evidence

Capture these for the evaluator:

1. Terminal output from `sh scripts/ci/check-elk-observability.sh` showing `ELK smoke log found in Elasticsearch`.
2. Kibana data view named `swasthya-setu-logs-*`.
3. Kibana Discover filtered by `SwasthyaSetu ELK smoke log` or by an application service name such as `auth-service`.
4. Elasticsearch index list showing `swasthya-setu-logs-YYYY.MM.dd`.

Useful Elasticsearch checks:

```bash
curl -fsS "http://localhost:9200/_cat/indices/swasthya-setu-logs-*?v"
curl -fsS "http://localhost:9200/swasthya-setu-logs-*/_search?q=service_name:elk-smoke&size=1"
```

## Troubleshooting

If `check-elk-observability.sh` waits until Elasticsearch times out and even `docker ps` hangs, restart Docker Desktop and rerun:

```bash
sh scripts/ci/check-elk-observability.sh
```

If Docker is responsive but Elasticsearch is unhealthy, inspect:

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml logs --tail=120 elasticsearch
```

## Stop ELK

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml down
```

Use `-v` only when you intentionally want to delete Elasticsearch data.
