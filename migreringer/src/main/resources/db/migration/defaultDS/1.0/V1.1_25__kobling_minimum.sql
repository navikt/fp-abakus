ALTER TABLE KOBLING
    ALTER COLUMN opplysning_periode_fom DROP NOT NULL;
ALTER TABLE KOBLING
    ALTER COLUMN opplysning_periode_tom DROP NOT NULL;

ALTER TABLE IAY_INNTEKTSMELDING
    ADD kanalreferanse varchar(100) NULL;
COMMENT ON COLUMN IAY_INNTEKTSMELDING.kanalreferanse IS 'Kildereferanse for journalposten';

ALTER TABLE IAY_INNTEKTSMELDING
    ADD kildesystem varchar(200) NULL;
COMMENT ON COLUMN IAY_INNTEKTSMELDING.kildesystem IS 'Kildesystem for inntektsmeldingen';

alter table IAY_INNTEKTSMELDING
    add mottatt_dato date;

comment on column IAY_INNTEKTSMELDING.mottatt_dato is 'Dato inntektsmelding mottatt';

INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('INNTEKTSMELDING_INNSENDINGSAARSAK',
        'Årsaken til innsending av inntektsmelding (ny eller endring)', '',
        'VL');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), '-', 'Udefinert', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSMELDING_INNSENDINGSAARSAK');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'INNTEKTSMELDING_INNSENDINGSAARSAK', '-', 'NB', 'Udefinert');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'ENDRING', 'Endring', 'Endring', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSMELDING_INNSENDINGSAARSAK');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'INNTEKTSMELDING_INNSENDINGSAARSAK', 'ENDRING', 'NB', 'Endring');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'NY', 'Ny', 'Ny', to_date('2000-01-01', 'YYYY-MM-DD'),
        'INNTEKTSMELDING_INNSENDINGSAARSAK');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'INNTEKTSMELDING_INNSENDINGSAARSAK', 'NY', 'NB', 'Ny');
