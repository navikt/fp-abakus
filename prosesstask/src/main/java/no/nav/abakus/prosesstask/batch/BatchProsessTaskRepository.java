package no.nav.abakus.prosesstask.batch;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskStatus;

/**
 * Implementasjon av repository som er tilgjengelig for å lagre og opprette nye tasks.
 */
@ApplicationScoped
public class BatchProsessTaskRepository {

    private EntityManager entityManager;
    private ProsessTaskTjeneste taskTjeneste;

    BatchProsessTaskRepository() {
        // for CDI proxying
    }

    @Inject
    public BatchProsessTaskRepository(EntityManager entityManager,
                                      ProsessTaskTjeneste taskTjeneste) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
        this.taskTjeneste = taskTjeneste;
    }

    public List<TaskStatus> finnStatusForGruppe(String gruppe) {
        final Query query = entityManager
            .createNativeQuery("SELECT pt.status, count(*) FROM PROSESS_TASK pt WHERE pt.TASK_GRUPPE = :gruppe GROUP BY pt.status")
            .setParameter("gruppe", gruppe);

        List<no.nav.vedtak.felles.prosesstask.api.TaskStatus> statuser = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Object[]> result = query.getResultList();
        for (Object[] objects : result) {
            statuser.add(new TaskStatus(ProsessTaskStatus.valueOf((String) objects[0]), (BigDecimal) objects[1])); // NOSONAR
        }
        return statuser;
    }

    public int tømNestePartisjon() {
        return taskTjeneste.tømNestePartisjon();
    }

    public String lagre(ProsessTaskData taskData) {
        return taskTjeneste.lagre(taskData);
    }

    public String lagre(ProsessTaskGruppe gruppe) {
        return taskTjeneste.lagre(gruppe);
    }
}
