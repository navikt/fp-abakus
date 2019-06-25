ALTER TABLE iay_oppgitt_arbeidsforhold
    ADD COLUMN VERSJON bigint default 0 not null;
ALTER TABLE iay_oppgitt_opptjening
    ADD COLUMN VERSJON bigint default 0 not null;
ALTER TABLE iay_egen_naering
    ADD COLUMN VERSJON bigint default 0 not null;
