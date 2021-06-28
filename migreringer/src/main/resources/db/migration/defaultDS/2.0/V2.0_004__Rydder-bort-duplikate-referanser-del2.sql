DELETE
FROM iay_aktivitets_avtale
WHERE yrkesaktivitet_id IN (
    SELECT ya1.id
    FROM iay_yrkesaktivitet ya1 INNER JOIN iay_aktoer_arbeid aa1 ON (
            ya1.aktoer_arbeid_id = aa1.id
        ) INNER JOIN tmp_feilrettingskandidater tmp1 ON (
            aa1.inntekt_arbeid_ytelser_id = tmp1.register_id
        )
);

DELETE
FROM iay_permisjon
WHERE yrkesaktivitet_id IN (
    SELECT ya1.id
    FROM iay_yrkesaktivitet ya1 INNER JOIN iay_aktoer_arbeid aa1 ON (
            ya1.aktoer_arbeid_id = aa1.id
        ) INNER JOIN tmp_feilrettingskandidater tmp1 ON (
            aa1.inntekt_arbeid_ytelser_id = tmp1.register_id
        )
);

DELETE
FROM iay_yrkesaktivitet
WHERE id IN (
    SELECT ya1.id
    FROM iay_yrkesaktivitet ya1 INNER JOIN iay_aktoer_arbeid aa1 ON (
            ya1.aktoer_arbeid_id = aa1.id
        ) INNER JOIN tmp_feilrettingskandidater tmp1 ON (
            aa1.inntekt_arbeid_ytelser_id = tmp1.register_id
        )
);

DELETE
FROM iay_arbeidsforhold_refer
WHERE id in (
    SELECT id
    FROM iay_arbeidsforhold_refer r INNER JOIN tmp_feilrettingskandidater tmp1 ON (
                r.informasjon_id = tmp1.informasjon_id
            AND r.arbeidsgiver_orgnr = tmp1.arbeidsgiver_orgnr
            AND (
                                tmp1.antall_im1 = 0 AND r.intern_referanse= tmp1.arbeidsforhold_intern_id1
                        OR
                                tmp1.antall_im2 = 0 AND r.intern_referanse = tmp1.arbeidsforhold_intern_id2
                    )
        )
);

UPDATE prosess_task pt
SET status = 'KLAR', feilede_forsoek=0
FROM (
         SELECT max(id) as id, tmp1.kobling_id
         FROM prosess_task p INNER JOIN tmp_feilrettingskandidater tmp1 ON (
                                                                                   tmp1.kobling_id = CAST(SUBSTRING(p.task_parametere, 'behandlingId=([^[:space:]]*)') AS BIGINT)
                                                                               ) AND p.task_type = 'registerdata.innhent'
         GROUP BY tmp1.kobling_id
     ) AS subquery
WHERE pt.id=subquery.id;
