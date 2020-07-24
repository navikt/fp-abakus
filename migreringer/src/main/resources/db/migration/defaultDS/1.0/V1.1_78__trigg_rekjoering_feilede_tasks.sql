update PROSESS_TASK set neste_kjoering_etter = current_timestamp where task_type = 'retry.feilendeTasks' and status = 'KLAR';
