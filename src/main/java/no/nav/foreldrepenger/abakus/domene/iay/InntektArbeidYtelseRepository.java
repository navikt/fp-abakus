package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.jpa.HibernateHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.diff.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.repository.KoblingRepository;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;

@ApplicationScoped
public class InntektArbeidYtelseRepository {

    private static final Logger LOG = LoggerFactory.getLogger(InntektArbeidYtelseRepository.class);

    private EntityManager entityManager;
    private KoblingRepository koblingRepository;

    InntektArbeidYtelseRepository() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager");
        this.entityManager = entityManager;
        this.koblingRepository = new KoblingRepository(entityManager);
    }

    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = getAktivtInntektArbeidGrunnlag(koblingReferanse);
        return grunnlag.orElseThrow(
            () -> new TekniskException("FP-731232", String.format("Finner ikke InntektArbeidYtelse grunnlag for kobling %s", koblingReferanse)));

    }

    public Optional<InntektArbeidYtelseAggregat> hentIAYAggregatFor(KoblingReferanse koblingReferanse, UUID eksternReferanse) {
        TypedQuery<InntektArbeidYtelseAggregat> query = entityManager.createQuery(
            "SELECT iay FROM InntektArbeidYtelser iay WHERE iay.eksternReferanse = :eksternReferanse", InntektArbeidYtelseAggregat.class);
        query.setParameter("eksternReferanse", eksternReferanse);

        var res = HibernateVerktøy.hentUniktResultat(query);
        if (res.isPresent()) {
            validerKoblingMatcherIayAggregat(koblingReferanse, eksternReferanse);
        }
        return res;
    }

    private void validerKoblingMatcherIayAggregat(KoblingReferanse koblingReferanse, UUID eksternReferanse) {
        var matchKoblingIayAggregat = entityManager.createNativeQuery("""
            SELECT 1 FROM kobling k
                WHERE k.kobling_referanse = :ref
                AND (
                    exists(SELECT 1 FROM gr_arbeid_inntekt gr INNER JOIN iay_inntekt_arbeid_ytelser iaya ON gr.register_id = iaya.id WHERE iaya.ekstern_referanse = :eksternReferanse)
                    OR
                    exists(SELECT 1 FROM gr_arbeid_inntekt gr INNER JOIN iay_inntekt_arbeid_ytelser iaya ON gr.saksbehandlet_id = iaya.id WHERE iaya.ekstern_referanse = :eksternReferanse)
                    )
                """);

        matchKoblingIayAggregat.setParameter("ref", koblingReferanse.getReferanse());
        matchKoblingIayAggregat.setParameter("eksternReferanse", eksternReferanse);
        if (matchKoblingIayAggregat.getResultStream().findFirst().isEmpty()) {
            throw new IllegalStateException(
                "KoblingReferanse [" + koblingReferanse + "] er ikke aktiv eller er ikke knyttet til IAY aggregat [" + eksternReferanse + "]");
        }
    }

    public Set<Inntektsmelding> hentAlleInntektsmeldingerFor(AktørId aktørId,
                                                             Saksnummer saksnummer,
                                                             no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType) {

        // sjekker ikke om grunnlag er aktivt eller ikke, tar alt
        final TypedQuery<Inntektsmelding> query = entityManager.createQuery("""
            SELECT DISTINCT(im)
             FROM InntektArbeidGrunnlag gr
             JOIN Kobling k ON (k.id = gr.koblingId)
             JOIN Inntektsmeldinger ims ON (ims.id = gr.inntektsmeldinger.id)
             JOIN Inntektsmelding im ON (im.inntektsmeldinger.id = ims.id)
             WHERE k.saksnummer = :ref
             AND k.ytelseType = :ytelse
             AND k.aktørId = :aktørId
            """, Inntektsmelding.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        return Set.copyOf(query.getResultList());
    }

    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdInfoInntektsmeldingerMapFor(AktørId aktørId,
                                                                                                         Saksnummer saksnummer,
                                                                                                         no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType) {
        final TypedQuery<Object[]> query = entityManager.createQuery("""
            SELECT im, arbInf
             FROM InntektArbeidGrunnlag gr
             JOIN Kobling k ON (k.id = gr.koblingId)
             JOIN Inntektsmeldinger ims ON (ims.id = gr.inntektsmeldinger.id)
             JOIN Inntektsmelding im ON (im.inntektsmeldinger.id = ims.id)
             JOIN ArbeidsforholdInformasjon arbInf ON (arbInf.id = gr.arbeidsforholdInformasjon.id)
             WHERE k.saksnummer = :saksnummer
             AND k.ytelseType = :ytelse
             AND k.aktørId = :aktørId
            """, Object[].class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("ytelse", ytelseType);

        return queryTilMap(query.getResultList());
    }

    private Map<Inntektsmelding, ArbeidsforholdInformasjon> queryTilMap(List<Object[]> list) {
        Map<Inntektsmelding, ArbeidsforholdInformasjon> inntektsmeldingArbinfoMap = new HashMap<>();
        list.forEach(res -> {
            Inntektsmelding im = (Inntektsmelding) res[0];
            ArbeidsforholdInformasjon arbInf = (ArbeidsforholdInformasjon) res[1];
            if (!inntektsmeldingArbinfoMap.containsKey(im)) {
                inntektsmeldingArbinfoMap.put(im, arbInf);
            }
        });
        return inntektsmeldingArbinfoMap.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey(Inntektsmelding.COMP_REKKEFØLGE))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<InntektArbeidYtelseGrunnlag> hentAlleInntektArbeidYtelseGrunnlagFor(AktørId aktørId,
                                                                                    Saksnummer saksnummer,
                                                                                    no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType,
                                                                                    boolean kunAktive) {
        String sql;
        if (kunAktive) {
            sql = """
                SELECT gr
                 FROM InntektArbeidGrunnlag gr
                 JOIN Kobling k ON k.id = gr.koblingId
                 WHERE k.saksnummer = :saksnummer AND k.ytelseType = :ytelse and k.aktørId = :aktørId
                 AND gr.aktiv = true
                 ORDER BY gr.koblingId, gr.opprettetTidspunkt
                 """;
        } else {
            sql = """
                SELECT gr
                 FROM InntektArbeidGrunnlag gr
                 JOIN Kobling k ON k.id = gr.koblingId
                 WHERE k.saksnummer = :saksnummer AND k.ytelseType = :ytelse and k.aktørId = :aktørId
                 ORDER BY gr.koblingId, gr.opprettetTidspunkt
                 """;
        }

        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery(sql, InntektArbeidYtelseGrunnlag.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("ytelse", ytelseType);

        return new ArrayList<>(query.getResultList());
    }

    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(KoblingReferanse koblingReferanse) {
        return getAktivtInntektArbeidGrunnlag(koblingReferanse);
    }

    public DiffResult diffResultat(InntektArbeidYtelseGrunnlag grunnlag1, InntektArbeidYtelseGrunnlag grunnlag2, boolean onlyCheckTrackedFields) {
        return new RegisterdataDiffsjekker(onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    /**
     * @param koblingReferanse
     * @param versjonType      (REGISTER, SAKSBEHANDLET)
     * @return InntektArbeidYtelseAggregatBuilder
     * <p>
     * NB! bør benytte via InntektArbeidYtelseTjeneste og ikke direkte
     */
    public InntektArbeidYtelseAggregatBuilder opprettBuilderFor(KoblingReferanse koblingReferanse,
                                                                UUID angittAggregatReferanse,
                                                                LocalDateTime angittOpprettetTidspunkt,
                                                                VersjonType versjonType) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return opprettBuilderFor(versjonType, angittAggregatReferanse, angittOpprettetTidspunkt, grunnlag);
    }

    /**
     * Metoden brukes av en forvaltningstjeneste for å opprette en builder for et nytt aggregat.
     * @param oppgittOpptjeningEksternReferanse
     * @return
     */
    public Optional<OppgittOpptjening> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse) {
        TypedQuery<OppgittOpptjening> query = entityManager.createQuery(
            "SELECT oo FROM OppgittOpptjening oo WHERE oo.eksternReferanse = :eksternReferanse", OppgittOpptjening.class);
        query.setParameter("eksternReferanse", oppgittOpptjeningEksternReferanse);
        var res = HibernateVerktøy.hentUniktResultat(query);

        if (res.isEmpty()) {
            return Optional.empty();
        } else {
            // sjekk om opptjening finnes i noen aktivt grunnlag
            // sjekker om kobling er aktiv her siden denne metoden brukes av forvaltningstjeneste direkte
            var query2 = entityManager.createNativeQuery("select 1 from kobling k inner join GR_ARBEID_INNTEKT gr on gr.kobling_id=k.id"
                + " inner join oppgitt_opptjening opp on opp.id=gr.oppgitt_opptjening_id"
                + " where k.aktiv = true and gr.aktiv = true and opp.ekstern_referanse = :ref");
            query2.setParameter("ref", oppgittOpptjeningEksternReferanse);
            boolean harAktivKoblingOgGrunnlag = query.getResultStream().findAny().isPresent();
            if (harAktivKoblingOgGrunnlag) {
                return res;
            } else {
                throw new IllegalStateException(
                    "Etterspurte OppgittOpptjening som ikke er koblet til noe aktiv kobling/grunnlag: " + oppgittOpptjeningEksternReferanse);
            }
        }
    }

    private InntektArbeidYtelseAggregatBuilder opprettBuilderFor(VersjonType versjonType,
                                                                 UUID angittReferanse,
                                                                 LocalDateTime opprettetTidspunkt,
                                                                 Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(grunnlag);
        Objects.requireNonNull(grunnlagBuilder, "grunnlagBuilder");
        Optional<InntektArbeidYtelseGrunnlag> aggregat = Optional.ofNullable(grunnlagBuilder.getKladd());
        Objects.requireNonNull(aggregat, "aggregat");
        if (aggregat.isPresent()) {
            final InntektArbeidYtelseGrunnlag aggregat1 = aggregat.get();
            return InntektArbeidYtelseAggregatBuilder.builderFor(hentRiktigVersjon(versjonType, aggregat1), angittReferanse, opprettetTidspunkt,
                versjonType);
        }
        throw new TekniskException("FP-512369", "Aggregat kan ikke være null ved opprettelse av builder");
    }

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder oppgittOpptjening) {
        if (oppgittOpptjening == null) {
            return null;
        }
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayGrunnlag);
        grunnlag.medOppgittOpptjening(oppgittOpptjening);

        InntektArbeidYtelseGrunnlag build = grunnlag.build();
        lagreOgFlush(koblingReferanse, build);
        return build.getGrunnlagReferanse();
    }

    public GrunnlagReferanse lagreOgNullstillOverstyring(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder oppgittOpptjening) {
        if (oppgittOpptjening == null) {
            return null;
        }
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayGrunnlag);
        grunnlag.medOppgittOpptjening(oppgittOpptjening)
            .fjernOverstyrtOppgittOpptjening();

        InntektArbeidYtelseGrunnlag build = grunnlag.build();
        lagreOgFlush(koblingReferanse, build);
        return build.getGrunnlagReferanse();
    }

    public GrunnlagReferanse lagreOverstyring(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder overstyrOppgittOpptjening) {
        if (overstyrOppgittOpptjening == null) {
            return null;
        }
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayGrunnlag);
        grunnlag.medOverstyrtOppgittOpptjening(overstyrOppgittOpptjening);

        InntektArbeidYtelseGrunnlag build = grunnlag.build();
        lagreOgFlush(koblingReferanse, build);
        return build.getGrunnlagReferanse();
    }

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse,
                                   ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                   List<Inntektsmelding> inntektsmeldingerList) {
        Objects.requireNonNull(inntektsmeldingerList, "inntektsmelding");
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingReferanse);

        var utdaterteInntektsmeldingerJournalposter = oppdaterBuilderMedNyeInntektsmeldinger(informasjonBuilder, inntektsmeldingerList, builder);

        InntektArbeidYtelseGrunnlag grunnlag = builder.build();
        var utdaterteInntektsmeldinger = inntektsmeldingerList.stream()
            .filter(it -> utdaterteInntektsmeldingerJournalposter.contains(it.getJournalpostId()) && harIkkeAltHåndtertJournalpost(grunnlag,
                it.getJournalpostId()))
            .collect(Collectors.toList());
        var utdatertBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(grunnlag);
        lagreDeaktivertGrunnlagMedUtdaterteInntektsmeldinger(koblingReferanse, informasjonBuilder, utdaterteInntektsmeldinger, utdatertBuilder);
        lagreOgFlush(koblingReferanse, grunnlag);

        return grunnlag.getGrunnlagReferanse();
    }

    private boolean harIkkeAltHåndtertJournalpost(InntektArbeidYtelseGrunnlag build, JournalpostId journalpostId) {
        var imaggregat = build.getInntektsmeldinger();
        return imaggregat.map(inntektsmeldingAggregat -> inntektsmeldingAggregat.getInntektsmeldinger()
            .stream()
            .noneMatch(it -> it.getJournalpostId().equals(journalpostId))).orElse(true);
    }

    private void lagreDeaktivertGrunnlagMedUtdaterteInntektsmeldinger(KoblingReferanse koblingReferanse,
                                                                      ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                                                      List<Inntektsmelding> utdaterteInntektsmeldinger,
                                                                      InntektArbeidYtelseGrunnlagBuilder utdatertBuilder) {
        Set<JournalpostId> utdaterteInntektsmeldingerJournalposter = new HashSet<>();
        utdatertBuilder.medDeaktivert();

        if (utdaterteInntektsmeldinger.isEmpty()) {
            return; // quick exit, ingenting nytt å gjøre her
        }

        final InntektsmeldingAggregat inntektsmeldinger = utdatertBuilder.getInntektsmeldinger();
        for (Inntektsmelding inntektsmelding : utdaterteInntektsmeldinger) {
            utdaterteInntektsmeldingerJournalposter.addAll(inntektsmeldinger.leggTilEllerErstattMedUtdatertForHistorikk(inntektsmelding));
        }
        utdatertBuilder.setInntektsmeldinger(inntektsmeldinger);
        utdatertBuilder.medInformasjon(informasjonBuilder.build());

        InntektArbeidYtelseGrunnlag build = utdatertBuilder.build();
        lagreOgFlush(koblingReferanse, build);

        if (!utdaterteInntektsmeldingerJournalposter.isEmpty()) {
            var collect = utdaterteInntektsmeldinger.stream()
                .filter(it -> utdaterteInntektsmeldingerJournalposter.contains(it.getJournalpostId()) && harIkkeAltHåndtertJournalpost(build,
                    it.getJournalpostId()))
                .collect(Collectors.toList());
            var builder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(build);
            lagreDeaktivertGrunnlagMedUtdaterteInntektsmeldinger(koblingReferanse, informasjonBuilder, collect, builder);
        }
    }

    /**
     * Legger til nye inntektsmeldinger på angitt IAYG builder.
     *
     * @return utdaterteInntektsmeldinger
     */
    public Set<JournalpostId> oppdaterBuilderMedNyeInntektsmeldinger(ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                                                     List<Inntektsmelding> nyeInntektsmeldinger,
                                                                     InntektArbeidYtelseGrunnlagBuilder targetBuilder) {

        Set<JournalpostId> utdaterteInntektsmeldinger = new HashSet<>();

        if (nyeInntektsmeldinger.isEmpty()) {
            return utdaterteInntektsmeldinger; // quick exit, ingenting nytt å gjøre her
        }

        final InntektsmeldingAggregat inntektsmeldinger = targetBuilder.getInntektsmeldinger();
        for (Inntektsmelding inntektsmelding : nyeInntektsmeldinger) {
            // Kommet inn inntektsmelding på arbeidsforhold som vi har gått videre med uten inntektsmelding?
            if (informasjonBuilder.kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(inntektsmelding)) {
                informasjonBuilder.fjernOverstyringVedrørende(inntektsmelding.getArbeidsgiver(), inntektsmelding.getArbeidsforholdRef());
            }
            // Gjelder tilfeller der det først har kommet inn inntektsmelding uten id, også kommer det inn en inntektsmelding med spesifik id
            // nullstiller da valg gjort i 5080 slik at saksbehandler må ta stilling til aksjonspunktet på nytt.
            informasjonBuilder.utledeArbeidsgiverSomMåTilbakestilles(inntektsmelding).ifPresent(informasjonBuilder::fjernOverstyringerSomGjelder);
            utdaterteInntektsmeldinger.addAll(inntektsmeldinger.leggTilEllerErstatt(inntektsmelding));
        }
        targetBuilder.setInntektsmeldinger(inntektsmeldinger);
        targetBuilder.medInformasjon(informasjonBuilder.build());

        return utdaterteInntektsmeldinger;
    }

    private InntektArbeidYtelseGrunnlagBuilder opprettGrunnlagBuilderFor(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidAggregat = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidAggregat);
    }

    private void lagreOgFlush(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlag nyttGrunnlag) {
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");

        if (nyttGrunnlag == null) {
            return;
        }

        validerKoblingErAktiv(koblingReferanse);
        sjekkKonsistens(nyttGrunnlag);

        Optional<InntektArbeidYtelseGrunnlag> tidligereAggregat = getAktivtInntektArbeidGrunnlag(koblingReferanse);

        if (tidligereAggregat.isPresent()) {
            InntektArbeidYtelseGrunnlag aggregat = tidligereAggregat.get();
            if (diffResultat(aggregat, nyttGrunnlag, false).isEmpty()) {
                return;
            }

            if (nyttGrunnlag.isAktiv()) {
                aggregat.setAktivt(false);
                entityManager.persist(aggregat);
                entityManager.flush();
            }
            lagreGrunnlag(nyttGrunnlag, koblingReferanse);
        } else {
            lagreGrunnlag(nyttGrunnlag, koblingReferanse);
        }
        entityManager.flush();
    }

    private void validerKoblingErAktiv(KoblingReferanse koblingReferanse) {
        koblingRepository.hentForKoblingReferanse(koblingReferanse).ifPresent(InntektArbeidYtelseRepository::validerIkkeAvsluttet);
    }

    private static void validerIkkeAvsluttet(Kobling kobling) {
        if (!kobling.erAktiv()) {
            throw new TekniskException("FT-49000", String.format(
                "Ikke tillatt å gjøre endringer på en avsluttet kobling. Gjelder kobling med referanse %s",
                kobling.getKoblingReferanse()));
        }
    }

    /**
     * Kaster exception hvis grunnlaget er i en ugyldig tilstand
     *
     * @param grunnlag grunnlaget som skal sjekkes
     */
    private void sjekkKonsistens(InntektArbeidYtelseGrunnlag grunnlag) {
        final var arbeidsforholdInformasjon = grunnlag.getArbeidsforholdInformasjon()
            .orElseGet(() -> ArbeidsforholdInformasjonBuilder.builder(Optional.empty()).build());

        var grRef = grunnlag.getGrunnlagReferanse();

        grunnlag.getRegisterVersjon()
            .ifPresent(
                aggregat -> aggregat.getAktørArbeid().stream().map(AktørArbeid::hentAlleYrkesaktiviteter).flatMap(Collection::stream).forEach(it -> {
                    if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                        arbeidsforholdInformasjon.finnEkstern(grRef, it.getArbeidsgiver(),
                            it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                        // (kaster exception hvis ikke)
                    }
                }));

        grunnlag.getInntektsmeldinger().ifPresent(aggregat -> aggregat.getInntektsmeldinger().forEach(it -> {
            if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                arbeidsforholdInformasjon.finnEkstern(grRef, it.getArbeidsgiver(),
                    it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                // (kaster exception hvis ikke)
            }
        }));

        grunnlag.getSaksbehandletVersjon()
            .ifPresent(
                aggregat -> aggregat.getAktørArbeid().stream().map(AktørArbeid::hentAlleYrkesaktiviteter).flatMap(Collection::stream).forEach(it -> {
                    if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                        arbeidsforholdInformasjon.finnEkstern(grRef, it.getArbeidsgiver(),
                            it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                        // (kaster exception hvis ikke)
                    }
                }));
    }

    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlagBuilder builder) {
        lagreOgFlush(koblingReferanse, builder.build());
    }

    private void lagreGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, KoblingReferanse koblingReferanse) {
        InntektArbeidYtelseGrunnlag entitet = nyttGrunnlag;
        Long koblingId = hentKoblingIdFor(koblingReferanse);
        entitet.setKobling(koblingId);

        if (entitet.getGrunnlagReferanse() == null) {
            // lag ny referanse
            entitet.setGrunnlagReferanse(new GrunnlagReferanse(UUID.randomUUID()));
        }

        nyttGrunnlag.getOppgittOpptjening().ifPresent(this::lagreOppgittOpptjening);
        nyttGrunnlag.getOverstyrtOppgittOpptjening().ifPresent(this::lagreOppgittOpptjening);

        var registerVersjon = entitet.getRegisterVersjon();
        registerVersjon.ifPresent(this::lagreInntektArbeid);

        var saksbehandletFørVersjon = nyttGrunnlag.getSaksbehandletVersjon();
        saksbehandletFørVersjon.ifPresent(this::lagreInntektArbeid);

        nyttGrunnlag.getInntektsmeldinger().ifPresent(this::lagreInntektsMeldinger);

        entitet.getArbeidsforholdInformasjon().ifPresent(this::lagreInformasjon);
        entityManager.persist(nyttGrunnlag);
    }

    private void lagreInformasjon(ArbeidsforholdInformasjon data) {

        // va
        entityManager.persist(data);
        for (var referanseEntitet : data.getArbeidsforholdReferanser()) {
            entityManager.persist(referanseEntitet);
        }
        for (var overstyringEntitet : data.getOverstyringer()) {
            entityManager.persist(overstyringEntitet);
        }
    }

    private void lagreOppgittOpptjening(OppgittOpptjening entitet) {
        entityManager.persist(entitet);

        for (OppgittAnnenAktivitet aktivitet : entitet.getAnnenAktivitet()) {
            entityManager.persist(aktivitet);
        }

        for (OppgittEgenNæring næring : entitet.getEgenNæring()) {
            entityManager.persist(næring);
        }

        entitet.getFrilans().ifPresent(frilans -> {
            entityManager.persist(frilans);
            for (OppgittFrilansoppdrag frilansoppdrag : frilans.getFrilansoppdrag()) {
                entityManager.persist(frilansoppdrag);
            }
        });

        List<OppgittArbeidsforhold> oppgittArbeidsforhold = entitet.getOppgittArbeidsforhold();
        for (OppgittArbeidsforhold arbeidsforhold : oppgittArbeidsforhold) {
            entityManager.persist(arbeidsforhold);
        }
    }

    private void lagreInntektsMeldinger(InntektsmeldingAggregat inntektsmeldingAggregat) {
        entityManager.persist(inntektsmeldingAggregat);
        var inntektsmeldinger = inntektsmeldingAggregat.getInntektsmeldinger();
        for (Inntektsmelding entitet : inntektsmeldinger) {
            entityManager.persist(entitet);
            for (var gradering : entitet.getGraderinger()) {
                entityManager.persist(gradering);
            }

            for (var naturalYtelse : entitet.getNaturalYtelser()) {
                entityManager.persist(naturalYtelse);
            }

            for (var utsettelsePeriode : entitet.getUtsettelsePerioder()) {
                entityManager.persist(utsettelsePeriode);
            }

            for (var refusjon : entitet.getEndringerRefusjon()) {
                entityManager.persist(refusjon);
            }

            for (var fravær : entitet.getOppgittFravær()) {
                entityManager.persist(fravær);
            }
        }
    }

    private void lagreInntektArbeid(InntektArbeidYtelseAggregat entitet) {
        entityManager.persist(entitet);

        for (AktørArbeid aktørArbeid : entitet.getAktørArbeid()) {
            entityManager.persist(aktørArbeid);
            lagreAktørArbeid(aktørArbeid);
        }

        for (AktørInntekt aktørInntekt : entitet.getAktørInntekt()) {
            entityManager.persist(aktørInntekt);
            lagreInntekt(aktørInntekt);
        }

        for (AktørYtelse aktørYtelse : entitet.getAktørYtelse()) {
            entityManager.persist(aktørYtelse);
            lagreAktørYtelse(aktørYtelse);
        }
    }

    private void lagreInntekt(AktørInntekt aktørInntekt) {
        for (Inntekt inntekt : aktørInntekt.getInntekt()) {
            entityManager.persist(inntekt);
            for (Inntektspost inntektspost : inntekt.getAlleInntektsposter()) {
                entityManager.persist(inntektspost);
            }
        }
    }

    private void lagreAktørArbeid(AktørArbeid aktørArbeid) {
        for (Yrkesaktivitet yrkesaktivitet : aktørArbeid.hentAlleYrkesaktiviteter()) {
            entityManager.persist(yrkesaktivitet);
            for (AktivitetsAvtale aktivitetsAvtale : yrkesaktivitet.getAlleAktivitetsAvtaler()) {
                entityManager.persist(aktivitetsAvtale);
            }
            for (Permisjon permisjon : yrkesaktivitet.getPermisjon()) {
                entityManager.persist(permisjon);
            }
        }
    }

    private void lagreAktørYtelse(AktørYtelse aktørYtelse) {
        for (Ytelse ytelse : aktørYtelse.getAlleYtelser()) {
            entityManager.persist(ytelse);
            for (YtelseAnvist ytelseAnvist : ytelse.getYtelseAnvist()) {
                entityManager.persist(ytelseAnvist);
                if (ytelseAnvist.getYtelseAnvistAndeler() != null) {
                    ytelseAnvist.getYtelseAnvistAndeler().forEach(entityManager::persist);
                }
            }
            ytelse.getYtelseGrunnlag().ifPresent(yg -> {
                entityManager.persist(yg);
                for (YtelseStørrelse størrelse : yg.getYtelseStørrelse()) {
                    entityManager.persist(størrelse);
                }
            });
        }
    }

    private Optional<InntektArbeidYtelseAggregat> hentRiktigVersjon(VersjonType versjonType, InntektArbeidYtelseGrunnlag aggregat) {
        if (versjonType == VersjonType.REGISTER) {
            return aggregat.getRegisterVersjon();
        } else if (versjonType == VersjonType.SAKSBEHANDLET) {
            return aggregat.getSaksbehandletVersjon();
        }
        throw new IllegalStateException("Kunne ikke finne riktig versjon av InntektArbeidYtelseGrunnlag");
    }

    private Optional<InntektArbeidYtelseGrunnlag> getAktivtInntektArbeidGrunnlag(KoblingReferanse koblingReferanse) {

        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery("""
            SELECT gr FROM InntektArbeidGrunnlag gr JOIN Kobling k ON k.id = gr.koblingId
            WHERE k.koblingReferanse = :ref
            AND gr.aktiv = true""", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", koblingReferanse);
        List<InntektArbeidYtelseGrunnlag> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        } else if (resultList.size() == 1) {
            return Optional.of(resultList.getFirst());
        }
        throw new IllegalStateException("Finner flere aktive grunnlag på koblingReferanse=" + koblingReferanse);
    }

    public void slettAlleInaktiveGrunnlagFor(KoblingReferanse koblingReferanse) {
        final var query = entityManager.createQuery("""
            DELETE FROM InntektArbeidGrunnlag gr
            WHERE gr.aktiv = false
            AND gr.koblingId = (SELECT k.id FROM Kobling k where k.koblingReferanse = :ref)
            """);
        query.setParameter("ref", koblingReferanse);
        var countDelete = query.executeUpdate();
        LOG.info("Slettet {} inaktive grunnlag for kobling {}", countDelete, koblingReferanse);
    }

    private Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        if (grunnlag.isPresent()) {
            final Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = Optional.of(grunnlag.get());
            return inntektArbeidYtelseGrunnlag.flatMap(InntektArbeidYtelseGrunnlag::getArbeidsforholdInformasjon);
        }
        return Optional.empty();
    }

    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(KoblingReferanse koblingReferanse) {
        final Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return hentArbeidsforholdInformasjon(grunnlag);
    }

    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseForReferanse(GrunnlagReferanse grunnlagReferanse) {
        if (grunnlagReferanse == null) {
            return Optional.empty();
        }
        return getVersjonAvInntektArbeidYtelseForReferanseId(grunnlagReferanse);
    }

    private Optional<InntektArbeidYtelseGrunnlag> getVersjonAvInntektArbeidYtelseForReferanseId(GrunnlagReferanse grunnlagReferanse) {
        Objects.requireNonNull(grunnlagReferanse, "aggregatId");
        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery(
            "FROM InntektArbeidGrunnlag gr " + " WHERE gr.grunnlagReferanse = :ref ", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", grunnlagReferanse);
        query.setHint(HibernateHints.HINT_CACHE_MODE, "IGNORE");
        return Optional.ofNullable(query.getResultList().getFirst());
    }

    public KoblingReferanse hentKoblingReferanseFor(GrunnlagReferanse grunnlagReferanse) {
        final TypedQuery<KoblingReferanse> query = entityManager.createQuery(
            "SELECT k.koblingReferanse FROM Kobling k JOIN InntektArbeidGrunnlag gr ON gr.koblingId = k.id WHERE gr.grunnlagReferanse=:ref",
            KoblingReferanse.class);
        query.setParameter("ref", grunnlagReferanse);
        return query.getSingleResult();
    }

    private Long hentKoblingIdFor(KoblingReferanse koblingReferanse) {
        return koblingRepository.hentForKoblingReferanse(koblingReferanse).map(Kobling::getId).orElse(null);
    }

    public boolean erGrunnlagAktivt(UUID eksternReferanse) {
        Objects.requireNonNull(eksternReferanse, "eksternReferanse");
        final TypedQuery<Boolean> query = entityManager.createQuery("SELECT gr.aktiv FROM InntektArbeidGrunnlag gr WHERE gr.grunnlagReferanse = :ref", Boolean.class);
        query.setParameter("ref", new GrunnlagReferanse(eksternReferanse));
        query.setHint(HibernateHints.HINT_CACHE_MODE, "IGNORE");
        Optional<Boolean> grunnlagOpt = query.getResultList().stream().findFirst();
        return grunnlagOpt.orElse(false);
    }
}
