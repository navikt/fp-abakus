package no.nav.foreldrepenger.abakus.lonnskomp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.dbstoette.JpaExtension;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonAnvist;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class LagreLønnskompensasjonTaskTest {

    private static final String SAK = "3028155d-c556-4a8a-a38d-a526b1129bf2";
    private static final PersonIdent FNR = new PersonIdent("19575903667");
    private static final AktørId AKTØR_ID = new AktørId("1957590366736");

    @RegisterExtension
    public static final JpaExtension repositoryRule = new JpaExtension();

    private LønnskompensasjonRepository repository = new LønnskompensasjonRepository(repositoryRule.getEntityManager());

    private AktørTjeneste aktørTjeneste = mock(AktørTjeneste.class);

    @Test
    public void skal_oppdatere_lagret_vedtak() {
        when(aktørTjeneste.hentAktørForIdent(eq(FNR), any())).thenReturn(Optional.of(AKTØR_ID));

        LønnskompensasjonVedtak vedtak = new LønnskompensasjonVedtak();
        vedtak.setFnr(FNR.getIdent());
        vedtak.setSakId(SAK);
        vedtak.setBeløp(new Beløp(new BigDecimal(18000L)));
        vedtak.setOrgNummer(new OrgNummer("999999999"));
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2020,4,20), LocalDate.of(2020,5,15)));
        vedtak.leggTilAnvistPeriode(LønnskompensasjonAnvist.LønnskompensasjonAnvistBuilder.ny()
            .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.of(2020,4,20), LocalDate.of(2020,4,20)))
            .medBeløp(new BigDecimal(1000)).build());
        repository.lagre(vedtak);

        assertThat(vedtak.getAktørId()).isNull();

        ProsessTaskData data = new ProsessTaskData(LagreLønnskompensasjonTask.TASKTYPE);
        data.setProperty(LagreLønnskompensasjonTask.SAK, SAK);

        LagreLønnskompensasjonTask task = new LagreLønnskompensasjonTask(repository, aktørTjeneste);
        task.doTask(data);

        repositoryRule.getEntityManager().clear();

        var oppdatertVedtak = repository.hentSak(SAK, FNR.getIdent());
        assertThat(oppdatertVedtak.get().getAktørId()).isEqualTo(AKTØR_ID);
    }
}
