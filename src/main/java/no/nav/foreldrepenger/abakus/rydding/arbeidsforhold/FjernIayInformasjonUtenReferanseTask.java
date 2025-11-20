package no.nav.foreldrepenger.abakus.rydding.arbeidsforhold;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.OppryddingTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayInformasjon.uten.referanse", maxFailedRuns = 2)
public class FjernIayInformasjonUtenReferanseTask implements ProsessTaskHandler {
    static final int IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE = 1000;

    private static final Logger LOG = LoggerFactory.getLogger(FjernIayInformasjonUtenReferanseTask.class);

    private final OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository;
    private final OppryddingTjeneste oppryddingTjeneste;

    @Inject
    public FjernIayInformasjonUtenReferanseTask(OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository, OppryddingTjeneste oppryddingTjeneste) {
        this.oppryddingIayInformasjonRepository = oppryddingIayInformasjonRepository;
        this.oppryddingTjeneste = oppryddingTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData unused) {
        var iayInformasjonUtenReferanse = oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE);
        LOG.info("Fjerner {} IAY-Informasjon uten referanse.", iayInformasjonUtenReferanse.size());
        iayInformasjonUtenReferanse.forEach(oppryddingIayInformasjonRepository::slettIayInformasjon);
        LOG.info("Slettet {} IAY-Informasjon uten referanse", iayInformasjonUtenReferanse.size());

        if (iayInformasjonUtenReferanse.size() >= IAY_ARBEIDSFORHOLD_INFORMASJON_BATCH_SIZE) {
            oppryddingTjeneste.opprettFjernIayInformasjonTask();
        }
    }
}
