update PROSESS_TASK
set
  neste_kjoering_etter = current_timestamp
where
  kode = 'retry.feilendeTasks' and STATUS='KLAR';
