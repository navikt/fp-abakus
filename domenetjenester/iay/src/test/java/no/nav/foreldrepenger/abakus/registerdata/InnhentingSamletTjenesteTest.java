package no.nav.foreldrepenger.abakus.registerdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonAnvist;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

class InnhentingSamletTjenesteTest {


    private LønnskompensasjonRepository repository = mock(LønnskompensasjonRepository.class);

    private InnhentingSamletTjeneste samletTjeneste;

    @BeforeEach
    public void before() {
        samletTjeneste = new InnhentingSamletTjeneste(null, null, null, repository, null, null);
    }

    @Test
    void skal_telle_riktig_dager_og_fordele() {
        // Arrange
        LocalDate fom = LocalDate.of(2020, 5, 15);
        LocalDate tom = LocalDate.of(2020, 6, 10);
        LønnskompensasjonVedtak lk = new LønnskompensasjonVedtak();
        lk.setAktørId(AktørId.dummy());
        lk.setOrgNummer(new OrgNummer("999999999"));
        lk.setSakId(UUID.randomUUID().toString());
        lk.setBeløp(new Beløp(new BigDecimal(30000L)));
        lk.setPeriode(IntervallEntitet.fraOgMedTilOgMed(fom, tom));
        lk.leggTilAnvistPeriode(LønnskompensasjonAnvist.LønnskompensasjonAnvistBuilder.ny()
            .medBeløp(new BigDecimal(16000))
            .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(YearMonth.from(fom).atDay(1), YearMonth.from(fom).atEndOfMonth()))
            .build());
        lk.leggTilAnvistPeriode(LønnskompensasjonAnvist.LønnskompensasjonAnvistBuilder.ny()
            .medBeløp(new BigDecimal(14000))
            .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(YearMonth.from(tom).atDay(1), YearMonth.from(tom).atEndOfMonth()))
            .build());

        when(repository.hentLønnskompensasjonForIPeriode(any(), any(), any())).thenReturn(Set.of(lk));
        // Act
        var mi = samletTjeneste.getLønnskompensasjon(lk.getAktørId(),
            IntervallEntitet.fraOgMedTilOgMed(LocalDate.now().minusMonths(17), LocalDate.now()));
        assertThat(mi.size()).isEqualTo(2);
        assertThat(mi.stream().filter(i -> i.getMåned().equals(YearMonth.from(tom))).findAny().orElse(null).getBeløp()).isEqualTo(
            new BigDecimal(14000));
    }

}
