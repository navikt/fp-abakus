package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.PgiFolketrygdenResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.SigrunPgiFolketrygdenMapper;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.pgifolketrygden.SigrunPgiFolketrygdenResponse;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class PgiFolketrygdenResponseTest {

    // Offisielt https://skatteetaten.github.io/api-dokumentasjon/api/pgi_folketrygden?tab=Eksempler
    // Erstattet FASTLAND / "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": 85000 med null for testformål
    private static final String SKATT_EKSEMPEL = """
        {
            "norskPersonidentifikator": "02116049964",
            "inntektsaar": 2019,
            "pensjonsgivendeInntekt": [
                {
                    "skatteordning": "FASTLAND",
                    "datoForFastsetting": "2020-09-27",
                    "pensjonsgivendeInntektAvLoennsinntekt": 698219,
                    "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": null,
                    "pensjonsgivendeInntektAvNaeringsinntekt": 150000,
                    "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": null
                },
                {
                    "skatteordning": "SVALBARD",
                    "datoForFastsetting": "2020-09-27",
                    "pensjonsgivendeInntektAvLoennsinntekt": 492160,
                    "pensjonsgivendeInntektAvLoennsinntektBarePensjonsdel": null,
                    "pensjonsgivendeInntektAvNaeringsinntekt": 2530000,
                    "pensjonsgivendeInntektAvNaeringsinntektFraFiskeFangstEllerFamiliebarnehage": null
                }
            ]
        }
        """;

    @Test
    void skal_mappe_og_beregne_slå_sammen_lønn_fra_beregent_med_lønn_fra_summertskattegrunnlag() {
        var respons = DefaultJsonMapper.fromJson(SKATT_EKSEMPEL, PgiFolketrygdenResponse.class);

        var sigrunMap = new SigrunPgiFolketrygdenResponse(Map.of(Year.of(respons.inntektsaar()), Optional.of(respons)));

        var intern = SigrunPgiFolketrygdenMapper.mapFraSigrunTilIntern(sigrunMap);

        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE)))
            .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(2680000)));
        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.LØNN)))
            .hasValueSatisfying(v -> assertThat(v).isEqualByComparingTo(BigDecimal.valueOf(1190379)));
        assertThat(intern.values().stream().findFirst().map(m -> m.get(InntektspostType.NÆRING_FISKE_FANGST_FAMBARNEHAGE))).isEmpty();
    }


}
