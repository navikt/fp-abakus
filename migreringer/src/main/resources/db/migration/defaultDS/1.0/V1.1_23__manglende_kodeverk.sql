INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'LAGT_TIL_AV_SAKSBEHANDLER', 'Arbeidsforhold lagt til av saksbehandler', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'ARBEIDSFORHOLD_HANDLING_TYPE', 'LAGT_TIL_AV_SAKSBEHANDLER', 'NB', 'Arbeidsforhold lagt til av saksbehandler');

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'KUNSTIG', 'Kunstig arbeidsforhold lagt til av saksbehandler', to_date('2000-01-01', 'YYYY-MM-DD'), 'ORGANISASJONSTYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'ORGANISASJONSTYPE', 'KUNSTIG', 'NB', 'Kunstig arbeidsforhold lagt til av saksbehandler');

INSERT INTO KODELISTE (id, kode, beskrivelse, gyldig_fom, kodeverk)
VALUES (seq_kodeliste.nextval, 'INNTEKT_IKKE_MED_I_BG', 'Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget', to_date('2000-01-01', 'YYYY-MM-DD'), 'ARBEIDSFORHOLD_HANDLING_TYPE');

INSERT INTO KODELISTE_NAVN_I18N (ID, KL_KODEVERK, KL_KODE, SPRAK, NAVN)
VALUES (SEQ_KODELISTE_NAVN_I18N.NEXTVAL, 'ARBEIDSFORHOLD_HANDLING_TYPE', 'INNTEKT_IKKE_MED_I_BG', 'NB', 'Inntekten til arbeidsforholdet skal ikke være med i beregningsgrunnlaget');
