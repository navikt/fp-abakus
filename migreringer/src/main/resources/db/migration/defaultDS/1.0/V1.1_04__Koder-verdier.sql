Insert into KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE,
                      KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, ENDRET_AV, ENDRET_TID,
                      SAMMENSATT)
values ('KONFIG_VERDI_TYPE', 'VL', null, null, null, 'N', 'N', 'KonfigVerdiType',
        'Angir type den konfigurerbare verdien er av slik at dette kan brukes til validering og fremstilling.', 'VL',
        to_timestamp('23.08.2018', 'DD.MM.RRRR'), null, null, 'N');
Insert into KODEVERK (KODE, KODEVERK_EIER, KODEVERK_EIER_REF, KODEVERK_EIER_VER, KODEVERK_EIER_NAVN, KODEVERK_SYNK_NYE,
                      KODEVERK_SYNK_EKSISTERENDE, NAVN, BESKRIVELSE, OPPRETTET_AV, OPPRETTET_TID, ENDRET_AV, ENDRET_TID,
                      SAMMENSATT)
values ('KONFIG_VERDI_GRUPPE', 'VL', null, null, null, 'N', 'N', 'KonfigVerdiGruppe',
        'Angir en gruppe konfigurerbare verdier tilhører. Det åpner for å kunne ha lister og Maps av konfigurerbare verdier',
        'VL', to_timestamp('23.08.2018', 'DD.MM.RRRR'), null, null, 'N');

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_GRUPPE', 'INGEN', null,
        'Ingen gruppe definert (default).  Brukes istdf. NULL siden dette inngår i en Primary Key. Koder som ikke er del av en gruppe må alltid være unike.',
        to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));

Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'BOOLEAN', null, 'Støtter J(a) / N(ei) flagg',
        to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'PERIOD', null,
        'ISO 8601 Periode verdier.  Eks. P10M (10 måneder), P1D (1 dag) ', to_date('01.01.2000', 'DD.MM.RRRR'),
        to_date('31.12.9999', 'DD.MM.RRRR'));
Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'DURATION', null,
        'ISO 8601 Duration (tid) verdier.  Eks. PT1H (1 time), PT1M (1 minutt) ', to_date('01.01.2000', 'DD.MM.RRRR'),
        to_date('31.12.9999', 'DD.MM.RRRR'));
Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'INTEGER', null, 'Heltallsverdier (positiv/negativ)',
        to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'STRING', null, null, to_date('01.01.2000', 'DD.MM.RRRR'),
        to_date('31.12.9999', 'DD.MM.RRRR'));
Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'URI', null, 'URI for å angi id til en ressurs',
        to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
Insert into KODELISTE (ID, KODEVERK, KODE, OFFISIELL_KODE, BESKRIVELSE, GYLDIG_FOM, GYLDIG_TOM)
values (nextval('seq_kodeliste'), 'KONFIG_VERDI_TYPE', 'DATE', null, 'Dato',
        to_date('01.01.2000', 'DD.MM.RRRR'), to_date('31.12.9999', 'DD.MM.RRRR'));
