-- ORGANISASJONSTYPE
insert into kodeverk (kode, navn, beskrivelse)
values ('ORGANISASJONSTYPE', 'Organisasjonstype', 'Beskriver typen til organisasjonen');
INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'JURIDISK_ENHET', 'Juridisk enhet', 'ORGANISASJONSTYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'ORGANISASJONSTYPE', 'JURIDISK_ENHET', 'NB', 'Juridisk enhet');

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'VIRKSOMHET', 'Virksomhet', 'ORGANISASJONSTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'ORGANISASJONSTYPE', 'VIRKSOMHET', 'NB', 'Virksomhet');

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), '-', 'Udefinert', 'ORGANISASJONSTYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'ORGANISASJONSTYPE', '-', 'NB', 'Udefinert');

-- ARBEID_TYPE
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('ARBEID_TYPE', 'N', 'N', 'Arbeid type', 'Kodeverk for arbeid typer');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'VANLIG', 'VANLIG', null,
        TO_DATE('2017-12-07 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:25.651', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', '-', null, 'Ikke definert',
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:25.654', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'ORDINÆRT_ARBEIDSFORHOLD', 'ordinaertArbeidsforhold', null,
        TO_DATE('2014-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:27.757', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'FORENKLET_OPPGJØRSORDNING', 'forenkletOppgjoersordning', null,
        TO_DATE('2014-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:27.760', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'FRILANSER_OPPDRAGSTAKER', 'frilanserOppdragstakerHonorarPersonerMm',
        null, TO_DATE('2014-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:27.763', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'MARITIMT_ARBEIDSFORHOLD', 'maritimtArbeidsforhold', null,
        TO_DATE('2014-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:27.767', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'PENSJON_OG_ANDRE_TYPER_YTELSER_UTEN_ANSETTELSESFORHOLD',
        'pensjonOgAndreTyperYtelserUtenAnsettelsesforhold', null,
        TO_DATE('2014-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:27.770', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'NÆRING', null, null,
        TO_DATE('2014-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:27.773', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'FRILANSER', null, null,
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:16:08.556', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, '{ "gui": "true" }');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'VENTELØNN_VARTPENGER', null, 'Ventelønn eller vartpenger',
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:16:23.746', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, '{ "gui": "true" }');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'ETTERLØNN_SLUTTPAKKE', null, 'Etterlønn eller sluttpakke',
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:16:23.762', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, '{ "gui": "true" }');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'LØNN_UNDER_UTDANNING', null, null,
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:34.510', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, '{ "gui": "true" }');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'MILITÆR_ELLER_SIVILTJENESTE', null, null,
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:34.441', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, '{ "gui": "true" }');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM, OPPRETTET_AV,
                       OPPRETTET_TID, ENDRET_AV, ENDRET_TID, EKSTRA_DATA)
VALUES (nextval('seq_kodeliste'), 'ARBEID_TYPE', 'UTENLANDSK_ARBEIDSFORHOLD', null, null,
        TO_DATE('2000-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        TO_DATE('9999-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'VL',
        TO_TIMESTAMP('2019-02-18 13:15:55.024', 'YYYY-MM-DD HH24:MI:SS.FF3'), null, null, '{ "gui": "true" }');

-- INNTEKTSPOST_TYPE
INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('INNTEKTSPOST_TYPE', 'N', 'N', 'Arbeid type', 'Kodeverk for inntektspost type');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
(nextval('seq_kodeliste'), 'INNTEKTSPOST_TYPE', 'YTELSE', 'YTELSE', 'Ytelse', NULL,
 to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES
(nextval('seq_kodeliste'), 'INNTEKTSPOST_TYPE', 'LØNN', 'LØNN', 'Lønn', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, EKSTRA_DATA, OFFISIELL_KODE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTSPOST_TYPE', 'SELVSTENDIG_NÆRINGSDRIVENDE', 'Selvstendig næringsdrivende',
        to_date('2000-01-01', 'YYYY-MM-DD'), '{typer: [svalbardpersoninntektNaering,  personinntektNaering]', '-');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, EKSTRA_DATA, OFFISIELL_KODE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTSPOST_TYPE', 'NÆRING_FISKE_FANGST_FAMBARNEHAGE',
        'Jordbruk/Skogbruk/Fiske/FamilieBarnehage', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{typer: [personinntektFiskeFangstFamilebarnehage]', 'personinntektFiskeFangstFamilebarnehage');

INSERT INTO KODELISTE (id, kodeverk, kode, navn, gyldig_fom, EKSTRA_DATA, OFFISIELL_KODE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTSPOST_TYPE', '-', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'),
        '{typer: [personinntektFiskeFangstFamilebarnehage]', null);


--
Insert into KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, NAVN, BESKRIVELSE,
                      OPPRETTET_AV, OPPRETTET_TID, ENDRET_AV, ENDRET_TID, KODEVERK_SYNK_EKSISTERENDE, KODEVERK_SYNK_NYE,
                      SAMMENSATT)
values ('INNTEKTS_KILDE', 'VL', null, null, null, 'Arbeid type', 'Kodeverk for inntekts kilder', 'VL',
        to_timestamp('09.11.2018 08.37.50,854000000', 'DD.MM.YYYY HH24.MI.SS'), null, null, 'N', 'N', 'N');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTS_KILDE', '-', null, 'Ikke definert');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTS_KILDE', 'INNTEKT_OPPTJENING', 'INNTEKT', null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTS_KILDE', 'SIGRUN', 'SIGRUN', null);
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTS_KILDE', 'INNTEKT_BEREGNING', null,
        'Inntektskomponenten beregningsgrunnlag.');
INSERT INTO KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE)
VALUES (nextval('seq_kodeliste'), 'INNTEKTS_KILDE', 'INNTEKT_SAMMENLIGNING', null,
        'Inntektskomponenten sammenligningsgrunnlag.');

-- PENSJON_TRYGD_BESKRIVELSE
INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye,
                      kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('PENSJON_TRYGD_BESKRIVELSE', 'Kodeverkforvaltning', 'PensjonEllerTrygdeBeskrivelse',
        'http://nav.no/kodeverk/Kode/PensjonEllerTrygdeBeskrivelse', 6, 'J', 'J', 'Pensjon Eller Trygde Beskrivelse',
        'Beskrivelse av pensjon eller trygde beskrivelse', 'J');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', '-', NULL, 'Undefined', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'ALDERSPENSJON', 'alderspensjon', 'Alderspensjon', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'ANNET', 'annet', 'Annet', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'AFP', 'avtalefestetPensjon', 'Avtalefestet pensjon',
        NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'BARNEPENSJON', 'barnepensjon', 'Barnepensjon', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'BARNEPENSJON_ANDRE',
        'barnepensjonFraAndreEnnFolketrygden', 'Barnepensjon fra andre enn folketrygden', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'BIL', 'bil', 'Bil', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'BOLIG', 'bolig', 'Bolig', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'EKTEFELLE', 'ektefelletillegg', 'Ektefelletillegg',
        NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'ELEKTRONISK_KOMMUNIKASJON', 'elektroniskKommunikasjon',
        'Elektronisk kommunikasjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'INNSKUDDS_ENGANGS', 'engangsutbetalingInnskuddspensjon',
        'Engangsutbetaling innskuddspensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'ETTERLATTE_PENSJON', 'etterlattepensjon',
        'Etterlatte pensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'ETTERLØNN', 'etterloenn', 'Etterlønn', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'ETTERLØNN_OG_ETTERPENSJON', 'etterloennOgEtterpensjon',
        'Etterlønn og etterpensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'FØDERÅD', 'foederaad', 'Føderåd', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'INTRODUKSJONSSTØNAD', 'introduksjonsstoenad',
        'Introduksjonsstønad', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_BARNEPENSJON', 'ipaEllerIpsBarnepensjon',
        'Ipa eller ips barnepensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_ENGANGSUTBETALING',
        'ipaEllerIpsEngangsutbetaling', 'Ipa eller ips engangsutbetaling', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_PERIODISKE', 'ipaEllerIpsPeriodiskeYtelser',
        'Ipa eller ips periodiske ytelser', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'IPA_IPS_UFØRE', 'ipaEllerIpsUfoerepensjon',
        'Ipa eller ips uførepensjon', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'KRIGSPENSJON', 'krigspensjon', 'Krigspensjon', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'KVALIFISERINGSSTØNAD', 'kvalifiseringstoenad',
        'Kvalifiseringsstønad', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'NY_AFP', 'nyAvtalefestetPensjonPrivatSektor',
        'Ny avtalefestet pensjon privat sektor', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'NYE_LIVRENTER',
        'nyeLivrenterIArbeidsforholdOgLivrenterFortsettelsesforsikringer',
        'Nye livrenter i arbeidsforhold og livrenter fortsettelsesforsikringer', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'OVERGANGSSTØNAD_ENSLIG',
        'overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe31Mars2014EllerTidligere',
        'Overgangsstønad til enslig mor eller far', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'OVERGANGSSTØNAD_EKTEFELLE',
        'overgangsstoenadTilGjenlevendeEktefelle', 'Overgangsstønad til gjenlevende ektefelle', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'PENSJON_DØDSMÅNED', 'pensjonIDoedsmaaneden',
        'Pensjon i dødsmåned', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'LIVRENTER', 'pensjonOgLivrenterIArbeidsforhold',
        'Pensjon og livrenter i arbeidsforhold', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'RENTEFORDEL_LÅN', 'rentefordelLaan', 'Rentefordel lån',
        NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'SUPPLERENDE_STØNAD',
        'supplerendeStoenadTilPersonKortBotidNorge', 'Supplerende stønad til person med kort botid i Norge', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'UFØREPENSJON', 'ufoerepensjon', 'Uførepensjon', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'UFØREPENSJON_ANDRE',
        'ufoerepensjonFraAndreEnnFolketrygden', 'Uførepensjon fra andre enn folketrygden', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'UFØREPENSJON_ANDRE_ETTEROPPGJØR',
        'ufoereytelseEtteroppgjoerFraAndreEnnFolketrygden', 'Uførepensjon etteroppgjør fra andre enn folketrygden',
        NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'PENSJON_TRYGD_BESKRIVELSE', 'UNDERHOLDNINGSBIDRAG',
        'underholdsbidragTilTidligereEktefelle', 'Underholdningsbidrag tidligere ektefelle', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));

-- YTELSE_FRA_OFFENTLIGE
INSERT INTO KODEVERK (kode, kodeverk_eier, kodeverk_eier_navn, kodeverk_eier_ref, kodeverk_eier_ver, kodeverk_synk_nye,
                      kodeverk_synk_eksisterende, navn, beskrivelse, sammensatt)
VALUES ('YTELSE_FRA_OFFENTLIGE', 'Kodeverkforvaltning', 'YtelseFraOffentligeBeskrivelse',
        'http://nav.no/kodeverk/Kode/YtelseFraOffentligeBeskrivelse', 4, 'J', 'J', 'Ytelser fra offentlige',
        'Ytelse fra offentlige', 'J');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', '-', null, 'UNDEFINED', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'AAP', 'arbeidsavklaringspenger', 'Arbeidsavklaringspenger',
        NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'DAGPENGER_FISKER', 'dagpengerTilFiskerSomBareHarHyre',
        'Dagpenger til fisker som bare har hyre', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'DAGPENGER_ARBEIDSLØS', 'dagpengerVedArbeidsloeshet',
        'Dagpenger ved arbeidsløshet', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'FORELDREPENGER', 'foreldrepenger', 'Foreldrepenger', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'OVERGANGSSTØNAD_ENSLIG',
        'overgangsstoenadTilEnsligMorEllerFarSomBegynteAaLoepe1April2014EllerSenere',
        'Overgangsstønad til enslig mor eller far', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'SVANGERSKAPSPENGER', 'svangerskapspenger',
        'Svangerskapspenger', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'SYKEPENGER', 'sykepenger', 'Sykepenger', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'SYKEPENGER_FISKER', 'sykepengerTilFiskerSomBareHarHyre',
        'Sykepenger fisker', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'UFØRETRYGD', 'ufoeretrygd', 'Uføretrygd', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'UFØRETRYGD_ETTEROPPGJØR', 'ufoereytelseEtteroppgjoer',
        'Uføretrygd etteroppgjør', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'UNDERHOLDNINGSBIDRAG_BARN', 'underholdsbidragTilBarn',
        'Underholdningsbidrag til barn', NULL, to_date('2017-12-07', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'YTELSE_FRA_OFFENTLIGE', 'VENTELØNN', 'venteloenn', 'Ventelønn', NULL,
        to_date('2017-12-07', 'YYYY-MM-DD'));

-- NÆRINGSINNTEKT_TYPE
insert into kodeverk (kode, navn, beskrivelse)
values ('NÆRINGSINNTEKT_TYPE', 'Næringsinntekt Type', 'Beskriver næringsinntekt');
INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'ANNET', 'annet', 'Annet', 'NÆRINGSINNTEKT_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'ANNET', 'NB', 'Annet');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'DAGPENGER_TIL_FISKER', 'dagpengerTilFisker', 'Dagpenger til fisker',
        'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'DAGPENGER_TIL_FISKER', 'NB', 'Dagpenger til fisker');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'DAGPENGER_VED_ARBEIDSLØSHET', 'dagpengerVedArbeidsloeshet',
        'Dagpenger ved arbeidsløshet', 'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'DAGPENGER_VED_ARBEIDSLØSHET', 'NB',
        'Dagpenger ved arbeidsløshet');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'LOTT_KUN_TRYGDEAVGIFT', 'lottKunTrygdeavgift', 'Lott kun trygdeavgift',
        'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'LOTT_KUN_TRYGDEAVGIFT', 'NB', 'Lott kun trygdeavgift');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SYKEPENGER', 'sykepenger', 'Sykepenger', 'NÆRINGSINNTEKT_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'SYKEPENGER', 'NB', 'Sykepenger (næringsinntekt)');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SYKEPENGER_TIL_DAGMAMMA', 'sykepengerTilDagmamma', 'Sykepenger til dagmamma',
        'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'SYKEPENGER_TIL_DAGMAMMA', 'NB', 'Sykepenger til dagmamma');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SYKEPENGER_TIL_FISKER', 'sykepengerTilFisker', 'Sykepenger til fisker',
        'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'SYKEPENGER_TIL_FISKER', 'NB', 'Sykepenger til fisker');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE', 'sykepengerTilJordOgSkogbrukere',
        'Sykepenger til jord og skogbrukere', 'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'SYKEPENGER_TIL_JORD_OG_SKOGBRUKERE', 'NB',
        'Sykepenger til jord og skogbrukere');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'VEDERLAG', 'vederlag', 'Vederlag', 'NÆRINGSINNTEKT_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'VEDERLAG', 'NB', 'Vederlag');

INSERT INTO KODELISTE (id, kode, OFFISIELL_KODE, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'VEDERLAG_DAGMAMMA_I_EGETHJEM', 'vederlagDagmammaIEgetHjem',
        'Vederlag dagmamma i egethjem', 'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', 'VEDERLAG_DAGMAMMA_I_EGETHJEM', 'NB',
        'Vederlag dagmamma i egethjem');

INSERT INTO KODELISTE (id, kode, beskrivelse, kodeverk, gyldig_fom)
VALUES (nextval('seq_kodeliste'), '-', 'Udefinert', 'NÆRINGSINNTEKT_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('seq_kodeliste_navn'), 'NÆRINGSINNTEKT_TYPE', '-', 'NB', 'Udefinert');


-- Skatte regel
INSERT INTO kodeverk (kode, navn, beskrivelse, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN,
                      KODEVERK_SYNK_EKSISTERENDE)
values
('SKATTE_OG_AVGIFTSREGEL', 'SkatteOgAvgiftsregel', 'Skatte -og avgiftsregel', 'Kodeverkforvaltning',
 'http://nav.no/kodeverk/Kodeverk/SkatteOgAvgiftsregel', 4,
 'SkatteOgAvgiftsregel', 'N');

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'JAN_MAYEN_OG_BILANDENE', 'janMayenOgBilandene',
        'Inntekt på Jan Mayen og i norske biland i Antarktis', to_date('2013-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'KILDESKATT_PÅ_PENSJONER', 'kildeskattPaaPensjoner',
        'Kildeskatt på pensjoner', to_date('2013-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'NETTOLØNN', 'nettoloenn', 'Nettolønn',
        to_date('2013-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'NETTOLØNN_FOR_SJØFOLK', 'nettoloennForSjoefolk',
        'Nettolønn for sjøfolk', to_date('2015-09-15', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'SÆRSKILT_FRADRAG_FOR_SJØFOLK', 'saerskiltFradragForSjoefolk',
        'Særskilt fradrag for sjøfolk', to_date('2013-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'SKATTEFRI_ORGANISASJON', 'skattefriOrganisasjon',
        'Skattefri Organisasjon', to_date('2016-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', 'SVALBARD', 'svalbard',
        'Svalbardinntekt', to_date('2013-01-01', 'YYYY-MM-DD'));

INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'SKATTE_OG_AVGIFTSREGEL', '-', 'Ikke definert',
        'Ikke definert', to_date('2013-01-01', 'YYYY-MM-DD'));
