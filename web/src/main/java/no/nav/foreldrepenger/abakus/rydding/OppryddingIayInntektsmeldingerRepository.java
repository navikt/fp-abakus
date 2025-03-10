package no.nav.foreldrepenger.abakus.rydding;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@ApplicationScoped
public class OppryddingIayInntektsmeldingerRepository {
    private static final Logger LOG = LoggerFactory.getLogger(OppryddingIayInntektsmeldingerRepository.class);

    private EntityManager entityManager;

    OppryddingIayInntektsmeldingerRepository() {
        // CDI proxy
    }

    @Inject
    public OppryddingIayInntektsmeldingerRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public List<Long> hentIayInntektsmeldingerUtenReferanse(Integer maxResults) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery("select distinct id from iay_inntektsmeldinger im where "
                + "not exists (select 1 from gr_arbeid_inntekt gr where im.id = gr.inntektsmeldinger_id)").setMaxResults(maxResults).getResultList();
        if (result.isEmpty()) {
            LOG.debug("Fant ingen IAY-Inntektsmeldinger uten grunnlag referanse");
            return emptyList();
        }
        LOG.debug("Fant {} IAY-Inntektsmeldinger uten grunnlag referanse", result.size());
        return result.stream().map(Number::longValue).toList();
    }

    public void slettIayInntektsmeldinger(Long id) {
        var inntektsmeldingAggregat = entityManager.find(InntektsmeldingAggregat.class, id);
        if (inntektsmeldingAggregat != null) {
            slettInntektsmeldingFor(id);
            fjernInntektsmeldingerFor(id);
        }
    }

    private void slettInntektsmeldingFor(Long inntektsmeldingerId) {
        var inntektsmeldinger = hentInntektsmeldingerFor(inntektsmeldingerId);
        if (!inntektsmeldinger.isEmpty()) {
            fjernRefusjonFor(inntektsmeldinger);
            fjernNaturalYtelseFor(inntektsmeldinger);
            fjernGraderingFor(inntektsmeldinger);
            fjernFraværFor(inntektsmeldinger);
            fjernUtsettelsePeriodeFor(inntektsmeldinger);
            fjernInntektsmeldingFor(inntektsmeldingerId);
        }
    }

    private List<Long> hentInntektsmeldingerFor(Long inntektsmeldingerId) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
            "select distinct id from iay_inntektsmelding where inntektsmeldinger_id = :imId")
            .setParameter("imId", inntektsmeldingerId).getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private void fjernRefusjonFor(List<Long> inntektsmeldingIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                "delete from iay_refusjon where inntektsmelding_id in (:inntektsmeldingIdList)")
            .setParameter("inntektsmeldingIdList", inntektsmeldingIdList)
            .executeUpdate();
        LOG.debug("Fjernet {} refusjon for inntektsmeldinger: {}", antallFjernet, inntektsmeldingIdList);
    }

    private void fjernNaturalYtelseFor(List<Long> inntektsmeldingIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                "delete from iay_natural_ytelse where inntektsmelding_id in (:inntektsmeldingIdList)")
            .setParameter("inntektsmeldingIdList", inntektsmeldingIdList)
            .executeUpdate();
        LOG.debug("Fjernet {} natural ytelse for inntektsmeldinger: {}", antallFjernet, inntektsmeldingIdList);
    }

    private void fjernGraderingFor(List<Long> inntektsmeldingIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                "delete from iay_gradering where inntektsmelding_id in (:inntektsmeldingIdList)")
            .setParameter("inntektsmeldingIdList", inntektsmeldingIdList)
            .executeUpdate();
        LOG.debug("Fjernet {} gradering for inntektsmeldinger: {}", antallFjernet, inntektsmeldingIdList);
    }

    private void fjernFraværFor(List<Long> inntektsmeldingIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                "delete from iay_fravaer where inntektsmelding_id in (:inntektsmeldingIdList)")
            .setParameter("inntektsmeldingIdList", inntektsmeldingIdList)
            .executeUpdate();
        LOG.debug("Fjernet {} fravær for inntektsmeldinger: {}", antallFjernet, inntektsmeldingIdList);
    }

    private void fjernUtsettelsePeriodeFor(List<Long> inntektsmeldingIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                "delete from iay_utsettelse_periode where inntektsmelding_id in (:inntektsmeldingIdList)")
            .setParameter("inntektsmeldingIdList", inntektsmeldingIdList)
            .executeUpdate();
        LOG.debug("Fjernet {} utsettelse periode for inntektsmeldinger: {}", antallFjernet, inntektsmeldingIdList);
    }

    private void fjernInntektsmeldingFor(Long inntektsmeldingerId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_inntektsmelding where iay_inntektsmeldinger = :inntektsmeldingerId")
            .setParameter("inntektsmeldingerId", inntektsmeldingerId)
            .executeUpdate();
        LOG.debug("Fjernet {} inntektsmelding for inntektsmeldinger: {}", antallFjernet, inntektsmeldingerId);
    }

    private void fjernInntektsmeldingerFor(Long inntektsmeldingerId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_inntektsmeldinger where id = :inntektsmeldingerId")
            .setParameter("inntektsmeldingerId", inntektsmeldingerId)
            .executeUpdate();
        LOG.debug("Fjernet {} inntektsmeldinger med id: {}", antallFjernet, inntektsmeldingerId);
    }
}
