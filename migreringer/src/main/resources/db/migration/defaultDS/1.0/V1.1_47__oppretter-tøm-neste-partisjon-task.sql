insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feilhandtering_algoritme, beskrivelse, cron_expression)
values ('partition.cleanBucket', 'Partisjon clean', 1, 'DEFAULT',
        'Tømmer partisjonen for neste måned', '0 0 7 1 * *');
