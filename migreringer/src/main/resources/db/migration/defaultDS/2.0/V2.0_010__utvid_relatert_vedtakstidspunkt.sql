ALTER TABLE IAY_RELATERT_YTELSE
    ADD COLUMN VEDTATT_TIDSPUNKT TIMESTAMP(3) NULL;
COMMENT ON COLUMN IAY_RELATERT_YTELSE.VEDTATT_TIDSPUNKT IS 'Tidspunktet vedtaket ble fattet';