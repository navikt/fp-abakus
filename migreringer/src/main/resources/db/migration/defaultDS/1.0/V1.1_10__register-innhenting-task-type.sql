INSERT INTO PROSESS_TASK_FEILHAND (KODE,NAVN,BESKRIVELSE,OPPRETTET_AV,INPUT_VARIABEL1,INPUT_VARIABEL2) values ('ÅPNINGSTID','Åpningstidsbasert feilhåndtering','Åpningstidsbasert feilhåndtering. INPUT_VARIABEL1 = åpningstid og INPUT_VARIABEL2 = stengetid','VL',6,21);

insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feilhandtering_algoritme, beskrivelse)
values ('registerdata.innhent', 'Innhenting av registerdata', 3, 'ÅPNINGSTID', 'Endring av behandlende enhet utenom ordinær logikk.');
