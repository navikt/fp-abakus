ALTER TABLE IAY_INNTEKTSPOST
    ADD COLUMN IF NOT EXISTS lonnsinntekt_beskrivelse  VARCHAR(100) default '-';

COMMENT ON COLUMN IAY_INNTEKTSPOST.lonnsinntekt_beskrivelse IS 'Dersom posten er av type lønnsinntekt gir denne en mer detaljert beskrivelse av hva inntekten gjelder.';
