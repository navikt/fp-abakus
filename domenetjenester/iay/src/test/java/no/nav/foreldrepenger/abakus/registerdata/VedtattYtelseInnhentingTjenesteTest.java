package no.nav.foreldrepenger.abakus.registerdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.domene.iay.YtelseAnvist;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseAndelBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvistBuilder;

public class VedtattYtelseInnhentingTjenesteTest {

    private VedtakYtelseRepository vedtakYtelseRepository = mock(VedtakYtelseRepository.class);

    private VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste;

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository = mock(InntektArbeidYtelseRepository.class);

    @BeforeEach
    public void before() {
        vedtattYtelseInnhentingTjeneste = new VedtattYtelseInnhentingTjeneste(vedtakYtelseRepository, inntektArbeidYtelseRepository);
    }

    @Test
    public void skal_mappe_vedtatt_ytelse() throws Exception {
        // Arrange
        VedtakYtelse vy = VedtakYtelseBuilder.oppdatere(Optional.empty())
            .medPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(2)))
            .medAktør(AktørId.dummy())
            .medStatus(YtelseStatus.AVSLUTTET)
            .medSaksnummer(new Saksnummer("123"))
            .medKilde(Fagsystem.FPSAK)
            .medYtelseType(YtelseType.ENGANGSTØNAD)
            .leggTil(getYtelseAnvist())
            .build();
        Kobling k = new Kobling();
        k.setOpplysningsperiode(IntervallEntitet.fraOgMed(LocalDate.now().minusMonths(17)));
        when(vedtakYtelseRepository.hentYtelserForIPeriode(any(), any(), any())).thenReturn(List.of(vy));
        when(inntektArbeidYtelseRepository.hentInntektArbeidYtelseGrunnlagForBehandling(any())).thenReturn(Optional.empty());
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(
            Optional.empty());
        // Act
        vedtattYtelseInnhentingTjeneste.innhentFraYtelsesRegister(AktørId.dummy(), k, builder);
        assertThat(builder.build().getAlleYtelser()).isNotEmpty();
        assertThat(builder.build().getAlleYtelser().stream().map(Ytelse::getYtelseAnvist).flatMap(Collection::stream).count()).isEqualTo(1);
        assertThat(builder.build()
            .getAlleYtelser()
            .stream()
            .map(Ytelse::getYtelseAnvist)
            .flatMap(Collection::stream)
            .map(YtelseAnvist::getYtelseAnvistAndeler)
            .count()).isEqualTo(1);

    }

    private YtelseAnvistBuilder getYtelseAnvist() {
        return YtelseAnvistBuilder.ny()
            .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(2)))
            .leggTilFordeling(VedtakYtelseAndelBuilder.ny()
                .medArbeidsforholdId("aiowjd332423")
                .medArbeidsgiver(Arbeidsgiver.virksomhet("910909088"))
                .medDagsats(BigDecimal.TEN)
                .medInntektskategori(Inntektskategori.ARBEIDSTAKER)
                .medRefusjonsgrad(BigDecimal.ZERO)
                .medUtbetalingsgrad(BigDecimal.TEN));
    }


}
