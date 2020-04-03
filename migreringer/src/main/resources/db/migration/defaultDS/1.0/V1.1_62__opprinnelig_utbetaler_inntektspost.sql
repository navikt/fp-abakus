ALTER TABLE iay_inntektspost
    ADD COLUMN opprinelig_utbetaler_orgnr VARCHAR(19);
comment on column iay_inntektspost.opprinelig_utbetaler_orgnr is 'Hvilken enhet som opprinnelig stod for denne utbetalingen.';
