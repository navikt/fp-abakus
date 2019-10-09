INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'BASERT_PÅ_INNTEKTSMELDING', 'Arbeidsforholdet som ikke ligger i AA-reg er basert på inntektsmelding', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'ARBEIDSFORHOLD_HANDLING_TYPE', 'BASERT_PÅ_INNTEKTSMELDING', 'NB', 'Arbeidsforholdet som ikke ligger i AA-reg er basert på inntektsmelding');
