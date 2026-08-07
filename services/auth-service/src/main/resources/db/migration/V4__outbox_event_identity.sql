ALTER TABLE outbox_events
    ADD COLUMN IF NOT EXISTS event_id VARCHAR(64);

UPDATE outbox_events
SET event_id = 'legacy-' || id
WHERE event_id IS NULL;

ALTER TABLE outbox_events
    ALTER COLUMN event_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_outbox_events_event_id
    ON outbox_events(event_id);
