INSERT INTO KODEVERK (kode, navn, beskrivelse, kodeverk_eier, kodeverk_eier_ref, kodeverk_eier_ver)
VALUES
  ('PERMISJONSBESKRIVELSE_TYPE',
   'PermisjonsOgPermitteringsBeskrivelse', 
   'Kodeverk over gyldige typer permisjon (PermisjonsOgPermitteringsBeskrivelse)', 'Kodeverkforvaltning',
   'PermisjonsOgPermitteringsBeskrivelse', 1);
   
INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), 'PERMISJON', 'Permisjon', 'Permisjon', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'permisjon');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    'PERMISJON',   'NB',   'Permisjon');


INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), 'PERMISJON_MED_FORELDREPENGER', 'Permisjon med foreldrepenger', 'Permisjon med foreldrepenger', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'permisjonMedForeldrepenger');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    'PERMISJON_MED_FORELDREPENGER',   'NB',   'Permisjon med foreldrepenger');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), 'PERMISJON_VED_MILITÆRTJENESTE', 'Permisjon ved militærtjeneste', 'Permisjon ved militærtjeneste', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'permisjonVedMilitaertjeneste');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    'PERMISJON_VED_MILITÆRTJENESTE',   'NB',   'Permisjon ved militærtjeneste');


INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), 'PERMITTERING', 'Permittering', 'Permittering', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'permittering');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    'PERMITTERING',   'NB',   'Permittering');


INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), 'UTDANNINGSPERMISJON', 'Utdanningspermisjon', 'Utdanningspermisjon', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'utdanningspermisjon');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    'UTDANNINGSPERMISJON',   'NB',   'Utdanningspermisjon');

INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), 'VELFERDSPERMISJON', 'Velferdspermisjon', 'Velferdspermisjon', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'velferdspermisjon');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    'VELFERDSPERMISJON',   'NB',   'Velferdspermisjon');

