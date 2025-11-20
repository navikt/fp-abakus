package no.nav.foreldrepenger.abakus.rydding.opptjening;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;

@ApplicationScoped
public class OppryddingIayOppgittOpptjeningRepository {
    private static final Logger LOG = LoggerFactory.getLogger(OppryddingIayOppgittOpptjeningRepository.class);
    protected static final String PARAM_OPPGITT_OPPTJENING_ID = "oppgittOpptjeningId";

    private EntityManager entityManager;

    OppryddingIayOppgittOpptjeningRepository() {
        // CDI proxy
    }

    @Inject
    public OppryddingIayOppgittOpptjeningRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    List<Long> hentIayOppgittOpptjeningUtenReferanse(Integer maxResults) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_oppgitt_opptjening oppt where "
                    + "not exists (select 1 from gr_arbeid_inntekt gr where oppt.id = gr.oppgitt_opptjening_id or oppt.id = gr.overstyrt_oppgitt_opptjening_id)")
            .setMaxResults(maxResults)
            .getResultList();
        if (result.isEmpty()) {
            LOG.debug("Fant ingen IAY-Oppgitt Opptjening uten grunnlag referanse");
            return emptyList();
        }
        LOG.debug("Fant {} IAY-Oppgitt Opptjening uten grunnlag referanse", result.size());
        return result.stream().map(Number::longValue).toList();
    }

    void slettIayOppgittOpptjening(Long id) {
        var oppgittOpptjeningAggregat = entityManager.find(OppgittOpptjening.class, id);
        if (oppgittOpptjeningAggregat != null) {
            slettOppgittOpptjeningFor(id);
            fjernOppgittOpptjeningFor(id);
        }
    }

    private void slettOppgittOpptjeningFor(Long oppgittOpptjeningId) {
        var oppgittFrilans = hentOppgittFrilansFor(oppgittOpptjeningId);
        if (!oppgittFrilans.isEmpty()) {
            fjernOppgittFrilansoppdragFor(oppgittFrilans);
            fjernOppgittFrilansFor(oppgittOpptjeningId);
        }
        fjernAnnenAktivitetFor(oppgittOpptjeningId);
        fjernEgenNæringFor(oppgittOpptjeningId);
        fjernOppgittArbeidsforholdFor(oppgittOpptjeningId);
    }

    private List<Long> hentOppgittFrilansFor(Long oppgittOpptjeningId) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_oppgitt_frilans where oppgitt_opptjening_id = :oppgittOpptjeningId")
            .setParameter(PARAM_OPPGITT_OPPTJENING_ID, oppgittOpptjeningId)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private void fjernOppgittFrilansoppdragFor(List<Long> oppgittFrilansIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_oppgitt_frilansoppdrag where frilans_id in (:oppgittFrilansIdList)")
            .setParameter("oppgittFrilansIdList", oppgittFrilansIdList)
            .executeUpdate();
        LOG.debug("Fjernet {} oppgitt frilansoppdrag for oppgitt frilans: {}", antallFjernet, oppgittFrilansIdList);
    }

    private void fjernOppgittFrilansFor(Long oppgittOpptjeningId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_oppgitt_frilans where oppgitt_opptjening_id in (:oppgittOpptjeningId)")
            .setParameter(PARAM_OPPGITT_OPPTJENING_ID, oppgittOpptjeningId)
            .executeUpdate();
        LOG.debug("Fjernet {} oppgitt frilans for oppgitt opptjening: {}", antallFjernet, oppgittOpptjeningId);
    }

    private void fjernAnnenAktivitetFor(Long oppgittOpptjeningId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_annen_aktivitet where oppgitt_opptjening_id in (:oppgittOpptjeningId)")
            .setParameter(PARAM_OPPGITT_OPPTJENING_ID, oppgittOpptjeningId)
            .executeUpdate();
        LOG.debug("Fjernet {} annen aktivitet for oppgitt opptjening: {}", antallFjernet, oppgittOpptjeningId);
    }

    private void fjernEgenNæringFor(Long oppgittOpptjeningId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_egen_naering where oppgitt_opptjening_id in (:oppgittOpptjeningId)")
            .setParameter(PARAM_OPPGITT_OPPTJENING_ID, oppgittOpptjeningId)
            .executeUpdate();
        LOG.debug("Fjernet {} egen næring for oppgitt opptjening: {}", antallFjernet, oppgittOpptjeningId);
    }

    private void fjernOppgittArbeidsforholdFor(Long oppgittOpptjeningId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_oppgitt_arbeidsforhold where oppgitt_opptjening_id in (:oppgittOpptjeningId)")
            .setParameter(PARAM_OPPGITT_OPPTJENING_ID, oppgittOpptjeningId)
            .executeUpdate();
        LOG.debug("Fjernet {} oppgitt arbeidsforhold for oppgitt opptjening: {}", antallFjernet, oppgittOpptjeningId);
    }

    private void fjernOppgittOpptjeningFor(Long oppgittOpptjeningId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_oppgitt_opptjening where id = :oppgittOpptjeningId")
            .setParameter(PARAM_OPPGITT_OPPTJENING_ID, oppgittOpptjeningId)
            .executeUpdate();
        LOG.debug("Fjernet {} oppgitt opptjening med id: {}", antallFjernet, oppgittOpptjeningId);
    }
}
