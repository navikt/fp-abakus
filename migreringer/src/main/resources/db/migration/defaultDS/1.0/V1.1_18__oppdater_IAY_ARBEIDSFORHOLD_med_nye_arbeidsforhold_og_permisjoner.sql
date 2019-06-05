alter table IAY_ARBEIDSFORHOLD add column arbeidsgiver_navn varchar(100);
alter table IAY_ARBEIDSFORHOLD add column stillingsprosent NUMERIC(5, 2);
alter table IAY_ARBEIDSFORHOLD add column permisjon_bruk  VARCHAR(1);
alter table IAY_ARBEIDSFORHOLD add column permisjon_fom DATE;
alter table IAY_ARBEIDSFORHOLD add column permisjon_tom DATE;

alter table IAY_ARBEIDSFORHOLD
    add constraint CHK_PERMISJON_BRUK_1
        check (PERMISJON_BRUK IN ('J', 'N'));

COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.arbeidsgiver_navn is 'Kjært navn for arbeidsgiver angitt av saksbehandler';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.stillingsprosent is 'Stillingsprosent for arbeidsgiver overstyr av saksbehandler';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.permisjon_bruk is 'Angi om permisjonperiode skal brukes (dersom saksbehandler har tatt stilling til det). Ellers bruk permisjon fra IAY_PERMISJON';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.permisjon_fom is 'Startdato for permisjon som skal brukes opplyst av saksbehandler (dersom saksbehandler har tatt stilling til det). Ellers bruk permisjon fra IAY_PERMISJON';
COMMENT ON COLUMN IAY_ARBEIDSFORHOLD.permisjon_tom is 'Siste dato for permisjon som skal brukes opplyst av saksbehandler (dersom saksbehandler har tatt stilling til det). Ellers bruk permisjon fra IAY_PERMISJON';