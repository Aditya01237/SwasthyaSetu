# ELK Observability

SwasthyaSetu includes an optional ELK overlay for centralized local logs:

- Elasticsearch stores logs.
- Logstash receives Docker container logs through the GELF logging driver.
- Kibana lets you search logs from the browser.

## Start ELK With The App

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build -d
sh scripts/ci/health-check-observability.sh
```

For split service databases plus ELK:

```bash
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml -f docker-compose.observability.yml up --build -d
docker compose -f docker-compose.yml -f docker-compose.service-dbs.yml --profile service-dbs run --rm service-db-sync
sh scripts/ci/health-check-observability.sh
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

## Stop ELK

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml down
```

Use `-v` only when you intentionally want to delete Elasticsearch data.
