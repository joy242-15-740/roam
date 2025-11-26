-- Migrate calendar_sources type from old values to REGION
-- All calendar sources are now region-based

UPDATE calendar_sources SET type = 'REGION' WHERE type IN ('PERSONAL', 'WORK', 'OPERATIONS', 'CUSTOM');
