DELETE
FROM IAY_FRAVAER
WHERE inntektsmelding_id IN (SELECT id
                             FROM IAY_INNTEKTSMELDING
                             WHERE journalpost_id IN ('508400184', '508400200', '509120985', '508689144', '508687613')
                               AND start_dato_permisjon IS NOT NULL);

DELETE
FROM IAY_UTSETTELSE_PERIODE
WHERE inntektsmelding_id IN (SELECT id
                             FROM IAY_INNTEKTSMELDING
                             WHERE journalpost_id IN ('508400184', '508400200', '509120985', '508689144', '508687613')
                               AND start_dato_permisjon IS NOT NULL);

DELETE
FROM IAY_NATURAL_YTELSE
WHERE inntektsmelding_id IN (SELECT id
                             FROM IAY_INNTEKTSMELDING
                             WHERE journalpost_id IN ('508400184', '508400200', '509120985', '508689144', '508687613')
                               AND start_dato_permisjon IS NOT NULL);

DELETE
FROM IAY_GRADERING
WHERE inntektsmelding_id IN (SELECT id
                             FROM IAY_INNTEKTSMELDING
                             WHERE journalpost_id IN ('508400184', '508400200', '509120985', '508689144', '508687613')
                               AND start_dato_permisjon IS NOT NULL);

DELETE
FROM IAY_INNTEKTSMELDING
WHERE journalpost_id IN ('508400184', '508400200', '509120985', '508689144', '508687613')
  AND start_dato_permisjon IS NOT NULL;
