package no.nav.foreldrepenger.abakus.lonnskomp;

import javax.inject.Inject;

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ProsessTask(LagreLønnskompensasjonTask.TASKTYPE)
public class LagreLønnskompensasjonTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "lonnskompEvent.lagre";
    public static final String SAK = "sakId";

    private LønnskompensasjonRepository repository;
    private AktørTjeneste aktørTjeneste;


    public LagreLønnskompensasjonTask() {
    }

    @Inject
    public LagreLønnskompensasjonTask(LønnskompensasjonRepository repository, AktørTjeneste aktørTjeneste) {
        this.repository = repository;
        this.aktørTjeneste = aktørTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        String sak = data.getPropertyValue(SAK);

        repository.hentSak(sak).stream()
            .filter(v -> v.getAktørId() == null)
            .forEach(v -> aktørTjeneste.hentAktørForIdent(new PersonIdent(v.getFnr()), YtelseType.FORELDREPENGER)
                .ifPresent(aktørId -> repository.oppdaterFødselsnummer(v.getFnr(), aktørId)));
    }
}
