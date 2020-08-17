update prosess_task set status='KLAR', 
  feilede_forsoek=0, 
  neste_kjoering_etter= current_timestamp at time zone 'UTC' + interval '3 minutes'
 where task_type='forvaltning.oppdaterKanalreferanse' AND status NOT IN ('KJOERT', 'FERDIG');
