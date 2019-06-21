INSERT INTO KODEVERK (kode, kodeverk_synk_nye, kodeverk_synk_eksisterende, navn, beskrivelse)
VALUES ('VIRKSOMHET_TYPE', 'N', 'N', 'Virksomhet type', 'Kodeverk for virksomhetstyper');
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk)
VALUES (nextval('seq_kodeliste'), '-', 'Ikke definert', 'Ikke definert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'VIRKSOMHET_TYPE');
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (nextval('seq_kodeliste'), 'FISKE', 'Fiske', 'Fiske', 'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (nextval('seq_kodeliste'), 'FRILANSER', 'Frilanser', 'Frilanser', 'VIRKSOMHET_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (nextval('seq_kodeliste'), 'REINDRIFT', 'Reindrift', 'Reindrift', 'VIRKSOMHET_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (nextval('seq_kodeliste'), 'JORDBRUK_SKOGBRUK', 'Jordbruk/skogbruk', 'Jordbruk/skogbruk', 'VIRKSOMHET_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (nextval('seq_kodeliste'), 'ANNEN', 'Annen næringsvirksomhet', 'Annen næringsvirksomhet', 'VIRKSOMHET_TYPE',
        to_date('2000-01-01', 'YYYY-MM-DD'));
insert into KODELISTE (id, kode, navn, beskrivelse, kodeverk, gyldig_fom)
values (nextval('seq_kodeliste'), 'DAGMAMMA', 'Dagmamma/familiebarnehage', 'Dagmamma/familiebarnehage',
        'VIRKSOMHET_TYPE', to_date('2000-01-01', 'YYYY-MM-DD'));
