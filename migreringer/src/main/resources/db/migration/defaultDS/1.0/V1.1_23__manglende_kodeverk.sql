INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES
  ('ARBEIDSFORHOLD_HANDLING_TYPE',
   'Kodeverk over gyldige typer av handlinger saksbehandler kan utføre av overstyringer på arbeidsforhold', '',
   'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), '-', 'Udefinert', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES
  (nextval('seq_kodeliste'), 'BRUK', 'Bruk', 'Bruk', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'BRUK_UTEN_INNTEKTSMELDING', 'Bruk, men ikke benytt inntektsmelding',
        'Bruk men ikke benytt inntektsmelding', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'IKKE_BRUK', 'Ikke bruk', 'Ikke bruk', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'SLÅTT_SAMMEN_MED_ANNET', 'Arbeidsforholdet er slått sammen med et annet', 'Arbeidsforholdet er slått sammen med et annet', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');
        
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'NYTT_ARBEIDSFORHOLD', 'Arbeidsforholdet er ansett som nytt', 'Arbeidsforholdet er ansett som nytt', to_date('2000-01-01', 'YYYY-MM-DD'),
        'ARBEIDSFORHOLD_HANDLING_TYPE');


INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'BRUK_MED_OVERSTYRT_PERIODE', 'Bruk arbeidsforholdet med overstyrt periode', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN_I18N'), 'ARBEIDSFORHOLD_HANDLING_TYPE',    'BRUK_MED_OVERSTYRT_PERIODE',   'NB',   'Bruk arbeidsforholdet med overstyrt periode');

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'LAGT_TIL_AV_SAKSBEHANDLER', 'Arbeidsforhold lagt til av saksbehandler', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (nextval('SEQ_KODELISTE_NAVN_I18N'), 'ARBEIDSFORHOLD_HANDLING_TYPE', 'LAGT_TIL_AV_SAKSBEHANDLER', 'NB', 'Arbeidsforhold lagt til av saksbehandler');

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'KUNSTIG', 'Kunstig arbeidsforhold lagt til av saksbehandler', to_date('2000-01-01', 'YYYY-MM-DD'), 'ORGANISASJONSTYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (nextval('SEQ_KODELISTE_NAVN_I18N'), 'ORGANISASJONSTYPE', 'KUNSTIG', 'NB', 'Kunstig arbeidsforhold lagt til av saksbehandler');

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'INNTEKT_IKKE_MED_I_BG', 'Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (nextval('SEQ_KODELISTE_NAVN_I18N'), 'ARBEIDSFORHOLD_HANDLING_TYPE', 'INNTEKT_IKKE_MED_I_BG', 'NB', 'Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget');
