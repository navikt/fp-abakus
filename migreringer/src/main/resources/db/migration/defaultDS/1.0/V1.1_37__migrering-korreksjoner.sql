UPDATE gr_arbeid_inntekt
SET aktiv = 'N'
where aktiv = 'J';

CREATE UNIQUE INDEX UIDX_gr_arbeid_inntekt_01
    ON gr_arbeid_inntekt (
                          (CASE
                               WHEN AKTIV = 'J'
                                   THEN kobling_id
                               ELSE NULL END),
                          (CASE
                               WHEN AKTIV = 'J'
                                   THEN AKTIV
                               ELSE NULL END)
        );
