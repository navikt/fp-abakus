DELETE
FROM iay_aktivitets_avtale
WHERE id IN (
    SELECT a.id
    FROM bck_omp_iay_aktivitets_avtale a
);

DELETE
FROM iay_permisjon
WHERE id IN (
    SELECT a.id
    FROM bck_omp_iay_permisjon a
);

DELETE
FROM iay_yrkesaktivitet
WHERE id IN (
    SELECT a.id
    FROM bck_omp_iay_yrkesaktivitet a
);

DELETE
FROM iay_arbeidsforhold_refer
WHERE id IN (
    SELECT a.id
    FROM bck_omp_iay_arbeidsforhold_refer a
);

UPDATE prosess_task pt
SET status = 'KLAR', feilede_forsoek=0
FROM (
         SELECT max(id) as id, tmp1.kobling_id
         FROM prosess_task p INNER JOIN tmp_omp_feilrettingskandidater tmp1 ON (
                                                                                       tmp1.kobling_id = CAST(SUBSTRING(p.task_parametere, 'behandlingId=([^[:space:]]*)') AS BIGINT)
                                                                                   ) AND p.task_type = 'registerdata.innhent'
         GROUP BY tmp1.kobling_id
     ) AS subquery
WHERE pt.id=subquery.id;
