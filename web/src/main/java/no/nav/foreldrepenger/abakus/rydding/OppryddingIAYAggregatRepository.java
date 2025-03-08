package no.nav.foreldrepenger.abakus.rydding;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class OppryddingIAYAggregatRepository {
    private static final Logger LOG = LoggerFactory.getLogger(OppryddingIAYAggregatRepository.class);
    protected static final String PARAM_IAY_ID = "iayId";
    protected static final String PARAM_RELATERT_YTELSE_ID_LIST = "relatertYtelseIdList";

    private EntityManager entityManager;

    OppryddingIAYAggregatRepository() {
        // CDI proxy
    }

    @Inject
    public OppryddingIAYAggregatRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
    }

    public List<Long> hentIayAggregaterUtenReferanse(Integer maxResults) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_inntekt_arbeid_ytelser iay where "
                    + "not exists (select 1 from gr_arbeid_inntekt gr where iay.id = gr.register_id or iay.id = gr.saksbehandlet_id)")
            .setMaxResults(maxResults)
            .getResultList();
        if (result.isEmpty()) {
            LOG.info("Fant ingen IAY-aggregater uten grunnlag referanse");
            return emptyList();
        }
        LOG.info("Fant {} IAY-aggregater uten grunnlag referanse", result.size());
        return result.stream().map(Number::longValue).toList();
    }

    public void slettIayAggregat(Long id) {
        var iay = entityManager.find(InntektArbeidYtelseAggregat.class, id);
        if (iay != null) {
            slettIayAktørIntekt(id);
            slettIayAktørArbeid(id);
            slettIayAktørYtelse(id);
            entityManager.remove(iay);
            entityManager.flush(); // Sørger for at endringer er lagret før vi går videre
        }
    }

    private void slettIayAktørYtelse(Long iayIdForSletting) {
        var aktørYtelseIdList = hentAktørYtelseFor(iayIdForSletting);
        if (!aktørYtelseIdList.isEmpty()) {
            var relatertYtelseIdList = hentRelaterteYtelserFor(aktørYtelseIdList);
            if (!relatertYtelseIdList.isEmpty()) {
                var ytelseGrunnlagIdList = hentYtelseGrunnlagFor(relatertYtelseIdList);
                if (!ytelseGrunnlagIdList.isEmpty()) {
                    fjernYtelseStørrelseFor(ytelseGrunnlagIdList);
                    fjernYtelseGrunnlagFor(relatertYtelseIdList);
                }
                var ytelseAnvistIdList = hentYtelseAnvistFor(relatertYtelseIdList);
                if (!ytelseAnvistIdList.isEmpty()) {
                    fjernYtelseAnvistAndelFor(ytelseAnvistIdList);
                    fjernYtelseAnvistFor(relatertYtelseIdList);
                }
                fjernRelatertYtelseFor(aktørYtelseIdList);
            }
            fjernAktørYtelseFor(iayIdForSletting);
        }
    }

    private List<Long> hentAktørYtelseFor(Long iayIdForSletting) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_aktoer_ytelse where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private List<Long> hentRelaterteYtelserFor(List<Long> aktørYtelseIdList) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_relatert_ytelse where aktoer_ytelse_id in (:aktoerYtelseIdList)")
            .setParameter("aktoerYtelseIdList", aktørYtelseIdList)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private List<Long> hentYtelseGrunnlagFor(List<Long> relatertYtelseIdList) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_ytelse_grunnlag where ytelse_id in (:relatertYtelseIdList)")
            .setParameter(PARAM_RELATERT_YTELSE_ID_LIST, relatertYtelseIdList)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private List<Long> hentYtelseAnvistFor(List<Long> relatertYtelseIdList) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_ytelse_anvist where ytelse_id in (:relatertYtelseIdList)")
            .setParameter(PARAM_RELATERT_YTELSE_ID_LIST, relatertYtelseIdList)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private void fjernYtelseStørrelseFor(List<Long> ytelseGrunnlagIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_ytelse_stoerrelse where ytelse_grunnlag_id in (:ytelseGrunnlagIdList)")
            .setParameter("ytelseGrunnlagIdList", ytelseGrunnlagIdList)
            .executeUpdate();
        LOG.info("Fjernet {} ytelse størrelser for ytelse grunnlag: {}", antallFjernet, ytelseGrunnlagIdList);
    }

    private void fjernYtelseGrunnlagFor(List<Long> relatertYtelseIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_ytelse_grunnlag where ytelse_id in (:relatertYtelseIdList)")
            .setParameter(PARAM_RELATERT_YTELSE_ID_LIST, relatertYtelseIdList)
            .executeUpdate();
        LOG.info("Fjernet {} ytelse grunnlag for relatert ytelse: {}", antallFjernet, relatertYtelseIdList);
    }

    private void fjernYtelseAnvistAndelFor(List<Long> ytelseAnvistIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_ytelse_anvist_andel where ytelse_anvist_id in (:ytelseAnvistIdList)")
            .setParameter("ytelseAnvistIdList", ytelseAnvistIdList)
            .executeUpdate();
        LOG.info("Fjernet {} ytelse anvist andeler for ytelse anvist: {}", antallFjernet, ytelseAnvistIdList);
    }

    private void fjernYtelseAnvistFor(List<Long> relatertYtelseIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_ytelse_anvist where ytelse_id in (:relatertYtelseIdList)")
            .setParameter(PARAM_RELATERT_YTELSE_ID_LIST, relatertYtelseIdList)
            .executeUpdate();
        LOG.info("Fjernet {} ytelse anvist for relatert ytelse: {}", antallFjernet, relatertYtelseIdList);
    }

    private void fjernRelatertYtelseFor(List<Long> aktørYtelseIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_relatert_ytelse where aktoer_ytelse_id in (:aktoerYtelseIdList)")
            .setParameter("aktoerYtelseIdList", aktørYtelseIdList)
            .executeUpdate();
        LOG.info("Fjernet {} relatert ytelse for aktør ytelse: {}", antallFjernet, aktørYtelseIdList);
    }

    private void fjernAktørYtelseFor(Long iayIdForSletting) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_aktoer_ytelse where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .executeUpdate();
        LOG.info("Fjernet {} aktør ytelse for iay-aggregat: {}", antallFjernet, iayIdForSletting);
    }

    private void slettIayAktørArbeid(Long iayIdForSletting) {
        var aktørArbeidIdList = hentAktørArbeidFor(iayIdForSletting);
        if (!aktørArbeidIdList.isEmpty()) {
            var yrkesaktivitetIdList = hentYrkesaktiviteterFor(aktørArbeidIdList);
            if (!yrkesaktivitetIdList.isEmpty()) {
                fjernPermisjonerFor(yrkesaktivitetIdList);
                fjernAktivitetsAvtalerFor(yrkesaktivitetIdList);
                fjernYrkesaktiviteterFor(aktørArbeidIdList);
            }
            fjernAktørArbeidFor(iayIdForSletting);
        }
    }

    private List<Long> hentAktørArbeidFor(Long iayIdForSletting) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_aktoer_arbeid where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private List<Long> hentYrkesaktiviteterFor(List<Long> aktørArbeidIdList) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_yrkesaktivitet where aktoer_arbeid_id in (:aktoerArbeidIdList)")
            .setParameter("aktoerArbeidIdList", aktørArbeidIdList)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private void fjernPermisjonerFor(List<Long> yrkesaktivitetIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_permisjon where yrkesaktivitet_id in (:yrkesaktivitetIdList)")
            .setParameter("yrkesaktivitetIdList", yrkesaktivitetIdList)
            .executeUpdate();
        LOG.info("Fjernet {} permisjoner for yrkesaktiviteter: {}", antallFjernet, yrkesaktivitetIdList);
    }

    private void fjernAktivitetsAvtalerFor(List<Long> yrkesaktivitetIdList) {
        var antallFjernet = entityManager.createNativeQuery(
                "delete from iay_aktivitets_avtale where yrkesaktivitet_id in (:yrkesaktivitetIdList)")
            .setParameter("yrkesaktivitetIdList", yrkesaktivitetIdList)
            .executeUpdate();
        LOG.info("Fjernet {} aktivitets avtaler for yrkesaktiviteter: {}", antallFjernet, yrkesaktivitetIdList);
    }

    private void fjernYrkesaktiviteterFor(List<Long> aktørArbeidIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_yrkesaktivitet where aktoer_arbeid_id in (:aktoerArbeidIdList)")
            .setParameter("aktoerArbeidIdList", aktørArbeidIdList)
            .executeUpdate();
        LOG.info("Fjernet {} yrkesaktiviteter for aktør arbeid: {}", antallFjernet, aktørArbeidIdList);
    }

    private void fjernAktørArbeidFor(Long iayIdForSletting) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_aktoer_arbeid where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .executeUpdate();
        LOG.info("Fjernet {} aktør arbeid for iay-aggregat: {}", antallFjernet, iayIdForSletting);
    }

    private void slettIayAktørIntekt(Long iayIdForSletting) {
        var aktørInntektIdList = hentAktørInntekterFor(iayIdForSletting);
        if (!aktørInntektIdList.isEmpty()) {
            var inntektIdList = hentInntekterFor(aktørInntektIdList);
            if (!inntektIdList.isEmpty()) {
                fjernInntektsposterFor(inntektIdList);
                fjernInntekterFor(aktørInntektIdList);
            }
            fjernAktørInntektFor(iayIdForSletting);
        }
    }

    private List<Long> hentAktørInntekterFor(Long iayIdForSletting) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_aktoer_inntekt where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private List<Long> hentInntekterFor(List<Long> aktørInntektIdList) {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_inntekt where aktoer_inntekt_id in (:aktoerInntektIdList)")
            .setParameter("aktoerInntektIdList", aktørInntektIdList)
            .getResultList();
        return result.stream().map(Number::longValue).toList();
    }

    private void fjernInntektsposterFor(List<Long> inntektIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_inntektspost where inntekt_id in (:inntektIdList)")
            .setParameter("inntektIdList", inntektIdList)
            .executeUpdate();
        LOG.info("Fjernet {} inntektsposter for inntekter: {}", antallFjernet, inntektIdList);
    }

    private void fjernInntekterFor(List<Long> aktørInntektIdList) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_inntekt where aktoer_inntekt_id in (:aktoerInntektIdList)")
            .setParameter("aktoerInntektIdList", aktørInntektIdList)
            .executeUpdate();
        LOG.info("Fjernet {} inntekter for aktør inntekter: {}", antallFjernet, aktørInntektIdList);

    }

    private void fjernAktørInntektFor(Long iayIdForSletting) {
        var antallFjernet = entityManager.createNativeQuery("delete from iay_aktoer_inntekt where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .executeUpdate();
        LOG.info("Fjernet {} aktør inntekter for iay-aggregat: {}", antallFjernet, iayIdForSletting);
    }
}
