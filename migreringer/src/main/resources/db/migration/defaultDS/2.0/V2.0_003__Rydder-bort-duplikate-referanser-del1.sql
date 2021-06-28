CREATE TABLE tmp_feilrettingskandidater AS (
    SELECT k.saksnummer,
           g.id AS grunnlag_id,
           g.register_id AS register_id,
           ar1.informasjon_id AS informasjon_id,
           g.kobling_id AS kobling_id,
           ar1.arbeidsgiver_orgnr AS arbeidsgiver_orgnr,
           ar1.intern_referanse AS arbeidsforhold_intern_id1, (
               SELECT COUNT(*)
               FROM iay_inntektsmelding im1
               WHERE im1.arbeidsforhold_intern_id = ar1.intern_referanse
                 AND im1.arbeidsgiver_orgnr = ar1.arbeidsgiver_orgnr
                 AND im1.inntektsmeldinger_id = g.inntektsmeldinger_id
           ) AS antall_im1,
           ar2.intern_referanse AS arbeidsforhold_intern_id2, (
               SELECT COUNT(*)
               FROM iay_inntektsmelding im2
               WHERE im2.arbeidsforhold_intern_id = ar2.intern_referanse
                 AND im2.arbeidsgiver_orgnr = ar2.arbeidsgiver_orgnr
                 AND im2.inntektsmeldinger_id = g.inntektsmeldinger_id
           ) AS antall_im2
    FROM IAY_ARBEIDSFORHOLD_REFER ar1 INNER JOIN gr_arbeid_inntekt g ON (
            g.informasjon_id = ar1.informasjon_id
        ) INNER JOIN IAY_ARBEIDSFORHOLD_REFER ar2 ON (
                ar2.informasjon_id = ar1.informasjon_id
            AND ar2.arbeidsgiver_orgnr = ar1.arbeidsgiver_orgnr
            AND ar2.ekstern_referanse = ar1.ekstern_referanse
            AND ar2.id > ar1.id
        ) INNER JOIN KOBLING k ON (
            k.id = g.kobling_id
        )
    WHERE k.ytelse_type IN ('PSB', 'OMP')
);
CREATE TABLE bck_iay_aktivitets_avtale AS (
    SELECT *
    FROM iay_aktivitets_avtale
    WHERE yrkesaktivitet_id IN (
        SELECT ya1.id
        FROM iay_yrkesaktivitet ya1 INNER JOIN iay_aktoer_arbeid aa1 ON (
                ya1.aktoer_arbeid_id = aa1.id
            ) INNER JOIN tmp_feilrettingskandidater tmp1 ON (
                aa1.inntekt_arbeid_ytelser_id = tmp1.register_id
            )
    )
);

CREATE TABLE bck_iay_permisjon AS (
    SELECT *
    FROM iay_permisjon
    WHERE yrkesaktivitet_id IN (
        SELECT ya1.id
        FROM iay_yrkesaktivitet ya1 INNER JOIN iay_aktoer_arbeid aa1 ON (
                ya1.aktoer_arbeid_id = aa1.id
            ) INNER JOIN tmp_feilrettingskandidater tmp1 ON (
                aa1.inntekt_arbeid_ytelser_id = tmp1.register_id
            )
    )
);

CREATE TABLE bck_iay_yrkesaktivitet AS (
    SELECT *
    FROM iay_yrkesaktivitet
    WHERE id IN (
        SELECT ya1.id
        FROM iay_yrkesaktivitet ya1 INNER JOIN iay_aktoer_arbeid aa1 ON (
                ya1.aktoer_arbeid_id = aa1.id
            ) INNER JOIN tmp_feilrettingskandidater tmp1 ON (
                aa1.inntekt_arbeid_ytelser_id = tmp1.register_id
            )
    )
);

CREATE TABLE bck_iay_arbeidsforhold_refer AS (
    SELECT *
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
    )
);
