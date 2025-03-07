package no.nav.foreldrepenger.abakus.rydding;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;

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

    public List<Long> hentAlleIayAggregatUtenReferanse() {
        @SuppressWarnings("unchecked") List<Number> result = entityManager.createNativeQuery(
                "select distinct id from iay_inntekt_arbeid_ytelser iay where not exists (select 1 from gr_arbeid_inntekt gr where iay.id = gr.register_id or iay.id = gr.saksbehandlet_id)")
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
        }
    }

    private void slettIayAktørYtelse(Long iayIdForSletting) {
        var aktørYtelseIdList = hentAktørYtelseFor(iayIdForSletting);
        var relatertYtelseIdList = hentRelaterteYtelserFor(aktørYtelseIdList);

        var ytelseGrunnlagIdList = hentYtelseGrunnlagFor(relatertYtelseIdList);
        var ytelseAnvistIdList = hentYtelseAnvistFor(relatertYtelseIdList);

        var fjernetYtelseStørrelser = fjernYtelseStørrelseFor(ytelseGrunnlagIdList);
        LOG.info("Fjernet {} ytelse størrelser for ytelse grunnlag: {}", fjernetYtelseStørrelser, ytelseGrunnlagIdList);

        var fjernetYtelseGrunnlag = fjernYtelseGrunnlagFor(relatertYtelseIdList);
        LOG.info("Fjernet {} ytelse grunnlag for relatert ytelse: {}", fjernetYtelseGrunnlag, relatertYtelseIdList);

        var fjernetYtelseAnvistAndeler = fjernYtelseAnvistAndelFor(ytelseAnvistIdList);
        LOG.info("Fjernet {} ytelse anvist andeler for ytelse anvist: {}", fjernetYtelseAnvistAndeler, ytelseAnvistIdList);

        var fjernetYtelseAnvist = fjernYtelseAnvistFor(relatertYtelseIdList);
        LOG.info("Fjernet {} ytelse anvist for relatert ytelse: {}", fjernetYtelseAnvist, relatertYtelseIdList);

        var fjernetRelatertYtelsere = fjernRelatertYtelseFor(aktørYtelseIdList);
        LOG.info("Fjernet {} relatert ytelse for aktør ytelse: {}", fjernetRelatertYtelsere, aktørYtelseIdList);

        var fjernAktørYtelse = fjernAktørYtelseFor(iayIdForSletting);
        LOG.info("Fjernet {} aktør ytelse for iay-aggregat: {}", fjernAktørYtelse, iayIdForSletting);
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

    private int fjernYtelseStørrelseFor(List<Long> ytelseGrunnlagIdList) {
        return entityManager.createNativeQuery("delete from iay_ytelse_stoerrelse where ytelse_grunnlag_id in (:ytelseGrunnlagIdList)")
            .setParameter("ytelseGrunnlagIdList", ytelseGrunnlagIdList)
            .executeUpdate();
    }

    private int fjernYtelseGrunnlagFor(List<Long> relatertYtelseIdList) {
        return entityManager.createNativeQuery("delete from iay_ytelse_grunnlag where ytelse_id in (:relatertYtelseIdList)")
            .setParameter(PARAM_RELATERT_YTELSE_ID_LIST, relatertYtelseIdList)
            .executeUpdate();
    }

    private int fjernYtelseAnvistAndelFor(List<Long> ytelseAnvistIdList) {
        return entityManager.createNativeQuery("delete from iay_ytelse_anvist_andel where ytelse_anvist_id in (:ytelseAnvistIdList)")
            .setParameter("ytelseAnvistIdList", ytelseAnvistIdList)
            .executeUpdate();
    }

    private int fjernYtelseAnvistFor(List<Long> relatertYtelseIdList) {
        return entityManager.createNativeQuery("delete from iay_ytelse_anvist where ytelse_id in (:relatertYtelseIdList)")
            .setParameter(PARAM_RELATERT_YTELSE_ID_LIST, relatertYtelseIdList)
            .executeUpdate();
    }

    private int fjernRelatertYtelseFor(List<Long> aktørYtelseIdList) {
        return entityManager.createNativeQuery("delete from iay_relatert_ytelse where aktoer_ytelse_id in (:aktoerYtelseIdList)")
            .setParameter("aktoerYtelseIdList", aktørYtelseIdList)
            .executeUpdate();
    }

    private int fjernAktørYtelseFor(Long iayIdForSletting) {
        return entityManager.createNativeQuery("delete from iay_aktoer_ytelse where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .executeUpdate();
    }

    private void slettIayAktørArbeid(Long iayIdForSletting) {
        var aktørArbeidIdList = hentAktørArbeidFor(iayIdForSletting);
        var yrkesaktivitetIdList = hentYrkesaktiviteterFor(aktørArbeidIdList);

        var fjernetPermisjoner = fjernPermisjonerFor(yrkesaktivitetIdList);
        LOG.info("Fjernet {} permisjoner for yrkesaktiviteter: {}", fjernetPermisjoner, yrkesaktivitetIdList);

        var fjernetAktivitetsAvtaler = fjernAktivitetsAvtalerFor(yrkesaktivitetIdList);
        LOG.info("Fjernet {} aktivitets avtaler for yrkesaktiviteter: {}", fjernetAktivitetsAvtaler, yrkesaktivitetIdList);

        var fjernetYrkesaktiviteter = fjernYrkesaktiviteterFor(aktørArbeidIdList);
        LOG.info("Fjernet {} yrkesaktiviteter for aktør arbeid: {}", fjernetYrkesaktiviteter, aktørArbeidIdList);

        var fjernAktørArbeid = fjernAktørArbeidFor(iayIdForSletting);
        LOG.info("Fjernet {} aktør arbeid for iay-aggregat: {}", fjernAktørArbeid, iayIdForSletting);
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

    private int fjernPermisjonerFor(List<Long> yrkesaktivitetIdList) {
        return entityManager.createNativeQuery("delete from iay_permisjon where yrkesaktivitet_id in (:yrkesaktivitetIdList)")
            .setParameter("yrkesaktivitetIdList", yrkesaktivitetIdList)
            .executeUpdate();
    }

    private int fjernAktivitetsAvtalerFor(List<Long> yrkesaktivitetIdList) {
        return entityManager.createNativeQuery("delete from iay_aktivitets_avtale where yrkesaktivitet_id in (:yrkesaktivitetIdList)")
            .setParameter("yrkesaktivitetIdList", yrkesaktivitetIdList)
            .executeUpdate();
    }

    private int fjernYrkesaktiviteterFor(List<Long> aktørArbeidIdList) {
        return entityManager.createNativeQuery("delete from iay_yrkesaktivitet where aktoer_arbeid_id in (:aktoerArbeidIdList)")
            .setParameter("aktoerArbeidIdList", aktørArbeidIdList)
            .executeUpdate();
    }

    private int fjernAktørArbeidFor(Long iayIdForSletting) {
        return entityManager.createNativeQuery("delete from iay_aktoer_arbeid where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .executeUpdate();
    }

    private void slettIayAktørIntekt(Long iayIdForSletting) {
        var aktørInntektIdList = hentAktørInntekterFor(iayIdForSletting);
        var inntektIdList = hentInntekterFor(aktørInntektIdList);

        var fjernetInntektsposter = fjernInntektsposterFor(inntektIdList);
        LOG.info("Fjernet {} inntektsposter for inntekter: {}", fjernetInntektsposter, inntektIdList);

        var fjernInntekter = fjernInntekterFor(aktørInntektIdList);
        LOG.info("Fjernet {} inntekter for aktør inntekter: {}", fjernInntekter, aktørInntektIdList);

        var fjernAktørInntekter = fjernAktørInntektFor(iayIdForSletting);
        LOG.info("Fjernet {} aktør inntekter for iay-aggregat: {}", fjernAktørInntekter, iayIdForSletting);
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

    private int fjernInntektsposterFor(List<Long> inntektIdList) {
        return entityManager.createNativeQuery("delete from iay_inntektspost where inntekt_id in (:inntektIdList)")
            .setParameter("inntektIdList", inntektIdList)
            .executeUpdate();
    }

    private int fjernInntekterFor(List<Long> aktørInntektIdList) {
        return entityManager.createNativeQuery("delete from iay_inntekt where aktoer_inntekt_id in (:aktoerInntektIdList)")
            .setParameter("aktoerInntektIdList", aktørInntektIdList)
            .executeUpdate();
    }

    private int fjernAktørInntektFor(Long iayIdForSletting) {
        return entityManager.createNativeQuery("delete from iay_aktoer_inntekt where inntekt_arbeid_ytelser_id = :iayId")
            .setParameter(PARAM_IAY_ID, iayIdForSletting)
            .executeUpdate();
    }
}
