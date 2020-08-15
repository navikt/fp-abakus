insert into PROSESS_TASK_TYPE (kode, navn, feil_maks_forsoek, feilhandtering_algoritme, beskrivelse, cron_expression)
values ('forvaltning.oppdaterKanalreferanse', 'Forvaltning hent manglende kanalreferanser', 1, 'DEFAULT',
        'Forvaltning hent manglende kanalreferanser for inntektsmeldinger', '0/5 * * * * *');
