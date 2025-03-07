package no.nav.foreldrepenger.abakus.rydding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
    protected static final int MAX_PARTITION_SIZE = 250;
    private OppryddingIAYAggregatRepository iayOppryddingRepository;
    private ProsessTaskTjeneste taskTjeneste;

    OppryddingTjeneste() {
        // CDI proxy
    }

    @Inject
    public OppryddingTjeneste(OppryddingIAYAggregatRepository iayOppryddingRepository, ProsessTaskTjeneste taskTjeneste) {
        this.iayOppryddingRepository = iayOppryddingRepository;
        this.taskTjeneste = taskTjeneste;
    }

    public void fjernAlleIayAggregatUtenReferanse() {
        var iayAggregatUtenReferanse = iayOppryddingRepository.hentAlleIayAggregatUtenReferanse();

        LOG.info("Fjerner {} IAY-aggregater uten referanse.", iayAggregatUtenReferanse.size());
        partisjonerOverFlereTask(iayAggregatUtenReferanse, this::opprettFjernIayAggregatTask);
    }

    private void partisjonerOverFlereTask(Set<Long> iayAggregatUtenReferanse, Consumer<List<Long>> opprettTaskConsumer) {
        List<Long> partisjon = new ArrayList<>(MAX_PARTITION_SIZE);
        for (var iayAggregat : iayAggregatUtenReferanse) {
            partisjon.add(iayAggregat);
            if (partisjon.size() == MAX_PARTITION_SIZE) {
                opprettTaskConsumer.accept(partisjon);
                partisjon.clear();
            }
        }
        // Oppretter en task for resterende aggregater
        if (!partisjon.isEmpty()) {
            opprettFjernIayAggregatTask(partisjon);
        }
    }

    private void opprettFjernIayAggregatTask(List<Long> iayIdsList) {
        LOG.info("Oppretter task for å fjerne {} IAY-aggregater uten referanse.", iayIdsList.size());
        var prosessTaskData = ProsessTaskData.forProsessTask(FjernIAYGrunnlagUtenReferanseTask.class);
        prosessTaskData.setPayload(DefaultJsonMapper.toJson(iayIdsList));
        taskTjeneste.lagre(prosessTaskData);
    }
}
