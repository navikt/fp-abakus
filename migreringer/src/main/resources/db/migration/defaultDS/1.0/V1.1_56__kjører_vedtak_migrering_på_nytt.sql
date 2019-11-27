DELETE
FROM ve_ytelse_anvist
WHERE id IS NOT NULL;
DELETE
FROM vedtak_ytelse
WHERE id is not null;

CREATE UNIQUE INDEX UIDX_vedtak_ytelse_01
    ON vedtak_ytelse (
                      (CASE
                           WHEN AKTIV = 'J'
                               THEN (kilde || '-' || ytelse_type || '-' || saksnummer || '-' || aktoer_id)
                           ELSE NULL END),
                      (CASE
                           WHEN AKTIV = 'J'
                               THEN AKTIV
                           ELSE NULL END)
        );

UPDATE prosess_task_type set feil_maks_forsoek = 3 WHERE kode = 'vedtakEvent.lagre';

UPDATE prosess_task
SET status               = 'KLAR',
    feilede_forsoek      = 0,
    neste_kjoering_etter = current_timestamp
WHERE task_type = 'vedtakEvent.lagre';
