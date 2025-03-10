package no.nav.foreldrepenger.abakus.rydding.task;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.rydding.OppryddingIayInformasjonRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@ProsessTask(value = "opprydding.iayInformasjon.uten.referanse", maxFailedRuns = 2)
public class FjernIayInformasjonUtenReferanseTask implements ProsessTaskHandler {
    public static final int MAX_PARTITION_SIZE = 250;
    private static final Logger LOG = LoggerFactory.getLogger(FjernIayInformasjonUtenReferanseTask.class);
    private final OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository;
    private final ProsessTaskTjeneste taskTjeneste;

    @Inject
    public FjernIayInformasjonUtenReferanseTask(OppryddingIayInformasjonRepository oppryddingIayInformasjonRepository, ProsessTaskTjeneste taskTjeneste) {
        this.oppryddingIayInformasjonRepository = oppryddingIayInformasjonRepository;
        this.taskTjeneste = taskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        Set<Integer> iayInformasjonUtenReferanse = DefaultJsonMapper.fromJson(prosessTaskData.getPayloadAsString(), Set.class);
        LOG.info("Fjerner {} IAY-Informasjon uten referanse.", iayInformasjonUtenReferanse.size());
        iayInformasjonUtenReferanse.forEach(infoId ->  oppryddingIayInformasjonRepository.slettIayInformasjon(infoId.longValue()));
        LOG.info("Slettet {} IAY-Informasjon uten referanse", iayInformasjonUtenReferanse.size());

        var nyeIayInformasjonTilSletting = oppryddingIayInformasjonRepository.hentIayInformasjonUtenReferanse(MAX_PARTITION_SIZE);
        if (!nyeIayInformasjonTilSletting.isEmpty()) {
            opprettFjernInformasjonAggregatTask(nyeIayInformasjonTilSletting);
        }
    }

    private void opprettFjernInformasjonAggregatTask(List<Long> infoIdsList) {
        LOG.info("Oppretter en ny task for å fjerne {} IAY-Informasjon uten referanse.", infoIdsList.size());
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIayInformasjonUtenReferanseTask.class);
        prosessTaskData.setPayload(DefaultJsonMapper.toJson(infoIdsList));
        taskTjeneste.lagre(prosessTaskData);
    }
}
