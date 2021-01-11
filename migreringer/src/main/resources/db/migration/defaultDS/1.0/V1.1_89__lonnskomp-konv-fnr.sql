alter table LONNSKOMP_VEDTAK ALTER COLUMN AKTOER_ID DROP NOT NULL ;
alter table LONNSKOMP_VEDTAK add FNR VARCHAR(50) ;
create index IDX_LONNSKOMP_VEDTAK_3 on LONNSKOMP_VEDTAK (FNR);

insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feilhandtering_algoritme, beskrivelse)
values ('lonnskompEvent.lagre', 'Lagre lonnskompensasjon', 1, 'DEFAULT',
        'Lagrer vedtak mottatt fra kafka');
