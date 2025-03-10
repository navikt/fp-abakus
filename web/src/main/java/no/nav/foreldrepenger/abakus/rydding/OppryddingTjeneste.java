package no.nav.foreldrepenger.abakus.rydding;

import java.util.List;

import no.nav.foreldrepenger.abakus.rydding.task.FjernIayInformasjonUtenReferanseTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.task.FjernIAYGrunnlagUtenReferanseTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;


@ApplicationScoped
public class OppryddingTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(OppryddingTjeneste.class);
    private OppryddingIAYAggregatRepository iayOppryddingRepository;
    private OppryddingIayInformasjonRepository informasjonOppryddingRepository;
    private ProsessTaskTjeneste taskTjeneste;

    OppryddingTjeneste() {
        // CDI proxy
    }

    @Inject
    public OppryddingTjeneste(OppryddingIAYAggregatRepository iayOppryddingRepository,
                              OppryddingIayInformasjonRepository informasjonOppryddingRepository,
                              ProsessTaskTjeneste taskTjeneste) {
        this.iayOppryddingRepository = iayOppryddingRepository;
        this.informasjonOppryddingRepository = informasjonOppryddingRepository;
        this.taskTjeneste = taskTjeneste;
    }

    public void fjernAlleIayAggregatUtenReferanse() {
        var iayAggregatUtenReferanse = iayOppryddingRepository.hentIayAggregaterUtenReferanse(1000);
        if (iayAggregatUtenReferanse.isEmpty()) {
            LOG.info("Ingen IAY aggregat for sletting.");
            return;}
        LOG.info("Fjerner {} IAY-aggregater uten referanse.", iayAggregatUtenReferanse.size());
        opprettFjernIayAggregatTask(iayAggregatUtenReferanse);
    }

    public void fjernAlleIayInformasjontUtenReferanse() {
        var informasjonUtenReferanse = informasjonOppryddingRepository.hentIayInformasjonUtenReferanse(1500);
        if (informasjonUtenReferanse.isEmpty()) {
            LOG.info("Ingen IAY-Informasjon aggregat for sletting.");
            return;
        }
        LOG.info("Fjerner {} IAY-Informasjon aggregater uten referanse.", informasjonUtenReferanse.size());
        opprettFjernIayInformasjonTask(informasjonUtenReferanse);
    }

    private void opprettFjernIayAggregatTask(List<Long> iayIdsList) {
        LOG.info("Oppretter task for å fjerne {} IAY-aggregater uten referanse.", iayIdsList.size());
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(DefaultJsonMapper.toJson(iayIdsList));
        taskTjeneste.lagre(prosessTaskData);
    }

    private void opprettFjernIayInformasjonTask(List<Long> informasjonIdsList) {
        LOG.info("Oppretter task for å fjerne {} IAY-Informasjon uten referanse.", informasjonIdsList.size());
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class);
        prosessTaskData.setPayload(DefaultJsonMapper.toJson(informasjonIdsList));
        taskTjeneste.lagre(prosessTaskData);
    }
}
