ALTER TABLE kobling
    ADD COLUMN SAKSNUMMER VARCHAR(19);
ALTER TABLE KOBLING
    ALTER COLUMN SAKSNUMMER SET NOT NULL;
COMMENT ON COLUMN KOBLING.SAKSNUMMER IS 'Saksnummer til saken koblingen gjelder for';
create index IDX_KOBLING_2
    on KOBLING (SAKSNUMMER);
create index IDX_KOBLING_3
    on KOBLING (ytelse_type, kl_ytelse_type);
