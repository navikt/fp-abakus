package no.nav.foreldrepenger.abakus.rydding.arbeidsforhold;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

@ApplicationScoped
public class OppryddingIayInformasjonRepository {
    private static final Logger LOG = LoggerFactory.getLogger(OppryddingIayInformasjonRepository.class);

    private EntityManager entityManager;

    OppryddingIayInformasjonRepository() {
        // CDI proxy
    }

    @Inject
    public OppryddingIayInformasjonRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    List<Long> hentIayInformasjonUtenReferanse(Integer maxResults) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery("select distinct id from iay_informasjon info where "
                + "not exists (select 1 from gr_arbeid_inntekt gr where info.id = gr.informasjon_id)").setMaxResults(maxResults).getResultList();
        if (result.isEmpty()) {
            LOG.debug("Fant ingen IAY-Informasjon uten grunnlag referanse");
            return emptyList();
        }
        LOG.debug("Fant {} IAY-Informasjon uten grunnlag referanse", result.size());
        return result.stream().map(Number::longValue).toList();
    }

    void slettIayInformasjon(Long id) {
        var arbeidsforholdInformasjon = entityManager.find(ArbeidsforholdInformasjon.class, id);
        if (arbeidsforholdInformasjon != null) {
            slettArbeidsforholdInformasjon(id);
            fjernInformasjonFor(id);
        }
    }

    private void slettArbeidsforholdInformasjon(Long informasjonId) {
        var overstyrteArbeidsforhold = hentOverstyrteArbeidsforholdFor(informasjonId);
        if (!overstyrteArbeidsforhold.isEmpty()) {
            fjernOverstyrteArbeidsforholdPerioderFor(overstyrteArbeidsforhold);
            fjernOverstyrteArbeidsforholFor(informasjonId);
        }
        fjernArbeidsforholReferanserFor(informasjonId);
    }

    private List<Long> hentOverstyrteArbeidsforholdFor(Long informasjonId) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_arbeidsforhold where informasjon_id = :infoId").setParameter("infoId", informasjonId).getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private void fjernOverstyrteArbeidsforholdPerioderFor(List<Long> overstyrteArbeidsforholdIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                        "delete from iay_overstyrte_perioder where arbeidsforhold_id in (:overstyrteArbeidsforholdIdList)")
                .setParameter("overstyrteArbeidsforholdIdList", overstyrteArbeidsforholdIdList)
                .executeUpdate();
        LOG.debug("Fjernet {} overstyrte arbeidsforhold perioder for arbeidsforhold: {}", antallFjernet, overstyrteArbeidsforholdIdList);
    }

    private void fjernOverstyrteArbeidsforholFor(Long informasjonId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_arbeidsforhold where informasjon_id = :informasjonId")
                .setParameter("informasjonId", informasjonId)
                .executeUpdate();
        LOG.debug("Fjernet {} overstyrte arbeidsforhold for informasjon: {}", antallFjernet, informasjonId);
    }

    private void fjernArbeidsforholReferanserFor(Long informasjonId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_arbeidsforhold_refer where informasjon_id = :informasjonId")
                .setParameter("informasjonId", informasjonId)
                .executeUpdate();
        LOG.debug("Fjernet {} arbeidsforhold referanser for informasjon: {}", antallFjernet, informasjonId);
    }

    private void fjernInformasjonFor(Long informasjonId) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_informasjon where id = :informasjonId")
                .setParameter("informasjonId", informasjonId)
                .executeUpdate();
        LOG.debug("Fjernet {} arbeidsforhold informasjon med id: {}", antallFjernet, informasjonId);
    }
}
