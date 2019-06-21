INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier)
VALUES ('UTSETTELSE_AARSAK_TYPE', 'Kodeverk over årsaker til avvik(Utsettelse, opphold eller overføring) i perioder.',
        '', 'VL');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), '-', 'Ikke satt eller valgt kode', 'Ikke satt eller valgt kode',
        to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'LOVBESTEMT_FERIE', 'Lovbestemt ferie', 'Lovbestemt ferie',
        to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'SYKDOM', 'Avhengig av hjelp grunnet sykdom', 'Avhengig av hjelp grunnet sykdom',
        to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'ARBEID', 'Arbeid', 'Arbeid',
        to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'INSTITUSJONSOPPHOLD_SØKER', 'Søker er innlagt i helseinstitusjon',
        'Søker er innlagt i helseinstitusjon',
        to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), 'INSTITUSJONSOPPHOLD_BARNET', 'Barn er innlagt i helseinstitusjon',
        'Barn er innlagt i helseinstitusjon',
        to_date('2000-01-01', 'YYYY-MM-DD'), 'UTSETTELSE_AARSAK_TYPE');

INSERT INTO KODEVERK (KODE, NAVN, BESKRIVELSE)
VALUES ('NATURAL_YTELSE_TYPE', 'Natural ytelse typer',
        'Forskjellige former for natural ytelser fra inntektsmeldingen.');
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'ELEKTRISK_KOMMUNIKASJON', 'elektroniskKommunikasjon',
        'Elektrisk kommunikasjon', 'Elektrisk kommunikasjon', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'AKSJER_UNDERKURS', 'aksjerGrunnfondsbevisTilUnderkurs',
        'Aksjer grunnfondsbevis til underkurs', 'Aksjer grunnfondsbevis til underkurs',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'LOSJI', 'losji', 'Losji', 'Losji',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'KOST_DOEGN', 'kostDoegn', 'Kostpenger døgnsats',
        'Kostpenger døgnsats', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'BESOEKSREISER_HJEM', 'besoeksreiserHjemmetAnnet',
        'Besøksreiser hjemmet annet', 'Besøksreiser hjemmet annet', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'KOSTBESPARELSE_HJEM', 'kostbesparelseIHjemmet',
        'Kostbesparelser i hjemmet', 'Kostbesparelser i hjemmet', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'RENTEFORDEL_LAAN', 'rentefordelLaan', 'Rentefordel lån',
        'Rentefordel lån', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'BIL', 'bil', 'Bil', 'Bil',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'KOST_DAGER', 'kostDager', 'Kostpenger dager',
        'Kostpenger dager',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'BOLIG', 'bolig', 'Bolig', 'Bolig',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'FORSIKRINGER', 'skattepliktigDelForsikringer',
        'Skattepliktig del forsikringer', 'Skattepliktig del forsikringer', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'FRI_TRANSPORT', 'friTransport', 'Fri transport',
        'Fri transport',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'OPSJONER', 'opsjoner', 'Opsjoner', 'Opsjoner',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'TILSKUDD_BARNEHAGE', 'tilskuddBarnehageplass',
        'Tilskudd barnehageplass', 'Tilskudd barnehageplass', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'ANNET', 'annet', 'Annet', 'Annet',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'BEDRIFTSBARNEHAGE', 'bedriftsbarnehageplass',
        'Bedriftsbarnehageplass', 'Bedriftsbarnehageplass', to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'YRKESBIL_KILOMETER', 'yrkebilTjenestligbehovKilometer',
        'Yrkesbil tjenesteligbehov kilometer', 'Yrkesbil tjenesteligbehov kilometer',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'YRKESBIL_LISTEPRIS', 'yrkebilTjenestligbehovListepris',
        'Yrkesbil tjenesteligbehov listepris', 'Yrkesbil tjenesteligbehov listepris',
        to_date('2006-07-01', 'YYYY-MM-DD'));
INSERT INTO KODELISTE (id, kodeverk, kode, offisiell_kode, navn, beskrivelse, gyldig_fom)
VALUES (nextval('seq_kodeliste'), 'NATURAL_YTELSE_TYPE', 'UTENLANDSK_PENSJONSORDNING',
        'innbetalingTilUtenlandskPensjonsordning', 'Innbetaling utenlandsk pensjonsordning',
        'Innbetaling utenlandsk pensjonsordning', to_date('2006-07-01', 'YYYY-MM-DD'));
