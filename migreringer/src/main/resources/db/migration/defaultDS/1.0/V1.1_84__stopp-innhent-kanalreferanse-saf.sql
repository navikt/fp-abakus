update prosess_task_type set cron_expression=null where kode='forvaltning.oppdaterKanalreferanse';
update prosess_task set neste_kjoering_etter = current_timestamp at time zone 'UTC' + interval '3 minutes' where task_type='forvaltning.oppdaterKanalreferanse' and status NOT IN ('KJOERT','FERDIG');
