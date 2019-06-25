INSERT INTO KODELISTE (id, kode, navn, beskrivelse, gyldig_fom, kodeverk, offisiell_kode)
VALUES (nextval('seq_kodeliste'), '-', 'Udefinert', 'Udefinert', to_date('2000-01-01', 'YYYY-MM-DD'),
        'PERMISJONSBESKRIVELSE_TYPE', 'permisjon');
INSERT INTO KODELISTE_NAVN_I18N (id, kl_kodeverk, kl_kode, sprak, navn)
VALUES (nextval('SEQ_KODELISTE_NAVN'), 'PERMISJONSBESKRIVELSE_TYPE',    '-',   'NB',   'Udefinert');
