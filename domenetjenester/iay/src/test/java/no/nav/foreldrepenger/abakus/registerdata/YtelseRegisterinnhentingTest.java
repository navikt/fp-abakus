package no.nav.foreldrepenger.abakus.registerdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Ytelse;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvistBuilder;

public class YtelseRegisterinnhentingTest {

    private InnhentingSamletTjeneste samletTjeneste = mock(InnhentingSamletTjeneste.class);

    private VedtakYtelseRepository vedtakYtelseRepository = mock(VedtakYtelseRepository.class);

    private YtelseRegisterInnhenting ytelseRegisterInnhenting;

    @BeforeEach
    public void before() {
        ytelseRegisterInnhenting = new YtelseRegisterInnhenting(samletTjeneste, vedtakYtelseRepository);
    }

    @Test
    public void skal_gjenskape_feil() throws Exception {
        // Arrange
        VedtakYtelse vy = VedtakYtelseBuilder.oppdatere(Optional.empty())
            .medPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(2)))
            .medAktør(AktørId.dummy())
            .medStatus(YtelseStatus.AVSLUTTET)
            .medSaksnummer(new Saksnummer("123"))
            .medKilde(Fagsystem.FPSAK)
            .medYtelseType(YtelseType.ENGANGSTØNAD)
            .medBehandlingsTema(TemaUnderkategori.ENGANGSSTONAD_FODSEL)
            .leggTil(YtelseAnvistBuilder.ny().medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(4), LocalDate.now().minusMonths(2))))
            .build();
        Kobling k = new Kobling();
        k.setOpplysningsperiode(IntervallEntitet.fraOgMed(LocalDate.now().minusMonths(17)));
        when(samletTjeneste.hentYtelserTjenester(any(), any())).thenReturn(Collections.emptyList());
        when(samletTjeneste.innhentInfotrygdGrunnlag(any(), any(), any())).thenReturn(Collections.emptyList());
        when(vedtakYtelseRepository.hentYtelserForIPeriode(any(), any(), any())).thenReturn(List.of(vy));
        InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder builder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());
        // Act
        ytelseRegisterInnhenting.innhentFraYtelsesRegister(AktørId.dummy(), k, builder);
        assertThat(builder.build().getAlleYtelser()).isNotEmpty();
        assertThat(builder.build().getAlleYtelser().stream().map(Ytelse::getYtelseAnvist).flatMap(Collection::stream).count()).isEqualTo(1);
    }

}
