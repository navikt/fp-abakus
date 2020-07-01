COMMENT ON COLUMN IAY_RELATERT_YTELSE.SAKSNUMMER IS 'Obsolete - bruk kolonne saksreferanse';
update iay_relatert_ytelse set saksreferanse = saksnummer where saksnummer is not null and saksreferanse is not null;
