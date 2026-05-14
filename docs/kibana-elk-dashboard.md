# Kibana — View SwasthyaSetu application logs (ELK)

Prerequisites: Elasticsearch, Logstash, and Kibana are running (for example `docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d elasticsearch logstash kibana`, or Jenkins **`RUN_ELK_VERIFICATION`**).

Default Kibana URL: `http://localhost:5601` (or the host port from your `.env` / `KIBANA_PORT`).

## 1. Create a data view (index pattern)

1. Open Kibana → **Management** (gear) → **Data views** (or **Stack Management** → **Data views**, depending on Kibana 8.x layout).
2. **Create data view**.
3. **Name**: `swasthya-setu-logs`
4. **Index pattern**: `swasthya-setu-logs-*`  
   Logstash writes to this pattern (see `docker/logstash/pipeline/logstash.conf`).
5. **Timestamp field**: `@timestamp`
6. Save.

## 2. Explore logs

1. Go to **Discover**.
2. Select the **swasthya-setu-logs** data view.
3. Filter or search (example): `service_name`, `docker_container`, `message`, or free-text search for errors.

## 3. Optional dashboard

1. **Dashboard** → **Create dashboard**.
2. **Add** → **Lens** (or **Aggregation based**).
3. Build visualizations from the same data view (e.g. count of documents over time, breakdown by `service_name` if present).

## 4. CI smoke

`scripts/ci/smoke-elk-log.sh` sends a UDP GELF test message and asserts it appears in `swasthya-setu-logs-*`. It is run as part of `scripts/ci/check-elk-observability.sh`.
