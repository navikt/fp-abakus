package no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.Arrays;
import java.util.List;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.PgiFolketrygdenResponse;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.klient.SigrunRestClient;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SigrunTjenesteTest {

    private static final String FNR = "12345678910";
    private static final PersonIdent PERSONIDENT = new PersonIdent(FNR);

    private static final Year IFJOR = Year.now().minusYears(1);

    private static final SigrunRestClient CONSUMER = Mockito.mock(SigrunRestClient.class);

    private static final SigrunTjeneste TJENESTE = new SigrunTjeneste(CONSUMER);


    @Test
    void skal_hente_og_mappe_om_data_fra_sigrun() {
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR))
            .thenReturn(lagResponsFor(IFJOR));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(1)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(1)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(2)))
            .thenReturn(lagResponsUtenInntektFor(IFJOR.minusYears(2)));

        var inntekter = TJENESTE.hentPensjonsgivende(PERSONIDENT, null);
        assertThat(inntekter.keySet()).hasSize(3);
        assertThat(inntekter.get(intervallFor(IFJOR)).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(2))).get(InntektspostType.LØNN)).isZero();
    }

    @Test
    void skal_hente_data_for_forifjor_når_skatteoppgjoer_mangler_for_ifjor() {

        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR))
            .thenReturn(null);
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(1)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(1)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(2)))
            .thenReturn(lagResponsMedNæringFor(IFJOR.minusYears(2)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(3)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(3)));


        var inntekter = TJENESTE.hentPensjonsgivende(PERSONIDENT, null);
        assertThat(inntekter.keySet()).hasSize(3);
        assertThat(inntekter.get(intervallFor(IFJOR))).isNull();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(2))).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(2))).get(InntektspostType.SELVSTENDIG_NÆRINGSDRIVENDE).compareTo(new BigDecimal(500L))).isZero();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(3))).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
    }

    @Test
    void skal_hente_og_mappe_om_data_fra_sigrun_opplysiningsperiode() {
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR))
            .thenReturn(lagResponsFor(IFJOR));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(1)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(1)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(2)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(2)));
        var opplysningsperiode = IntervallEntitet.fraOgMedTilOgMed(intervallFor(IFJOR.minusYears(2)).getFomDato(), intervallFor(IFJOR).getTomDato());

        var inntekter = TJENESTE.hentPensjonsgivende(PERSONIDENT, opplysningsperiode);
        assertThat(inntekter.keySet()).hasSize(3);
        assertThat(inntekter.get(intervallFor(IFJOR)).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(2))).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
    }

    @Test
    void skal_hente_data_for_forifjor_når_skatteoppgjoer_mangler_for_ifjor_opplysiningsperiode() {
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR))
            .thenReturn(null);
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(1)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(1)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(2)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(2)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(3)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(3)));
        var opplysningsperiode = IntervallEntitet.fraOgMedTilOgMed(intervallFor(IFJOR.minusYears(2)).getFomDato(), intervallFor(IFJOR).getTomDato());

        var inntekter = TJENESTE.hentPensjonsgivende(PERSONIDENT, opplysningsperiode);
        assertThat(inntekter.keySet()).hasSize(3);
        assertThat(inntekter.get(intervallFor(IFJOR))).isNull();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(3))).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
    }

    @Test
    void skal_hente_data_for_inntil_tre_år_når_skatteoppgjoer_mangler_for_ifjor_opplysiningsperiode() {
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR))
            .thenReturn(null);
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(1)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(1)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(2)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(2)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(3)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(3)));
        Mockito.when(CONSUMER.hentPensjonsgivendeInntektForFolketrygden(FNR, IFJOR.minusYears(4)))
            .thenReturn(lagResponsFor(IFJOR.minusYears(4)));
        var opplysningsperiode = IntervallEntitet.fraOgMedTilOgMed(intervallFor(IFJOR.minusYears(4)).getFomDato(), intervallFor(IFJOR).getTomDato());

        var inntekter = TJENESTE.hentPensjonsgivende(PERSONIDENT, opplysningsperiode);
        assertThat(inntekter.keySet()).hasSize(4);
        assertThat(inntekter.get(intervallFor(IFJOR))).isNull();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(3))).get(InntektspostType.LØNN).compareTo(new BigDecimal(1000L))).isZero();
        assertThat(inntekter.get(intervallFor(IFJOR.minusYears(5)))).isNull();
    }

    private PgiFolketrygdenResponse lagResponsFor(Year år) {
        var inntekt = new PgiFolketrygdenResponse.Pgi(PgiFolketrygdenResponse.Skatteordning.FASTLAND,
            LocalDate.of(år.plusYears(1).getValue(), 6,1), 1000L ,
            null, null, null);
        return new PgiFolketrygdenResponse(PERSONIDENT.getIdent(), år.getValue(), List.of(inntekt));
    }

    private PgiFolketrygdenResponse lagResponsMedNæringFor(Year år) {
        var inntektF = new PgiFolketrygdenResponse.Pgi(PgiFolketrygdenResponse.Skatteordning.FASTLAND,
            LocalDate.of(år.plusYears(1).getValue(), 6,1), 500L ,
            null, null, null);
        var inntektS = new PgiFolketrygdenResponse.Pgi(PgiFolketrygdenResponse.Skatteordning.SVALBARD,
            LocalDate.of(år.plusYears(1).getValue(), 6,1), null ,
            null, 500L, null);
        var inntektK = new PgiFolketrygdenResponse.Pgi(PgiFolketrygdenResponse.Skatteordning.KILDESKATT_PAA_LOENN,
            LocalDate.of(år.plusYears(1).getValue(), 6,1), 500L ,
            null, null, null);
        return new PgiFolketrygdenResponse(PERSONIDENT.getIdent(), år.getValue(), List.of(inntektF, inntektS, inntektK));
    }

    private PgiFolketrygdenResponse lagResponsUtenInntektFor(Year år) {
        var inntekt = new PgiFolketrygdenResponse.Pgi(PgiFolketrygdenResponse.Skatteordning.FASTLAND,
            LocalDate.of(år.plusYears(1).getValue(), 6,1), 0L ,
            null, null, null);
        return new PgiFolketrygdenResponse(PERSONIDENT.getIdent(), år.getValue(), List.of(inntekt));
    }

    private IntervallEntitet intervallFor(Year år) {
        return IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().with(år).withDayOfYear(1), LocalDate.now().with(år).withDayOfYear(år.length()));
    }
}
