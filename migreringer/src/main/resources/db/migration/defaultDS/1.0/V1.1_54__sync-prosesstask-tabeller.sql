alter table prosess_task
    add column SISTE_KJOERING_PLUKK_TS timestamp(6);

COMMENT ON COLUMN PROSESS_TASK.SISTE_KJOERING_PLUKK_TS IS 'siste gang tasken ble forsøkt plukket (fra db til in-memory, før kjøring)';
