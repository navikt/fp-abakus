package no.nav.foreldrepenger.abakus.domene.iay;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.diff.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningAggregat;
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

    private static final Logger log = LoggerFactory.getLogger(InntektArbeidYtelseRepository.class);

    private EntityManager entityManager;
    private KoblingRepository koblingRepository;

    InntektArbeidYtelseRepository() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseRepository(EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
        this.koblingRepository = new KoblingRepository(entityManager);
    }

    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = getAktivtInntektArbeidGrunnlag(koblingReferanse);
        return grunnlag.orElseThrow(() -> new TekniskException("FP-731232", String.format("Finner ikke InntektArbeidYtelse grunnlag for kobling %s", koblingReferanse)));

    }

    public Optional<InntektArbeidYtelseAggregat> hentIAYAggregatFor(KoblingReferanse koblingReferanse, UUID eksternReferanse) {
        TypedQuery<InntektArbeidYtelseAggregat> query = entityManager.createQuery("SELECT iay " +
            " FROM InntektArbeidYtelser iay " +
            " WHERE iay.eksternReferanse = :eksternReferanse", InntektArbeidYtelseAggregat.class);
        query.setParameter("eksternReferanse", eksternReferanse);

        var res = HibernateVerktøy.hentUniktResultat(query);
        if (res.isPresent()) {
            validerKoblingMatcherIayAggregat(koblingReferanse, eksternReferanse);
        }
        return res;
    }

    private void validerKoblingMatcherIayAggregat(KoblingReferanse koblingReferanse, UUID eksternReferanse) {
        var matchKoblingIayAggregat = entityManager.createNativeQuery("select 1 from kobling k "
            + " where k.aktiv=true and k.kobling_referanse=:ref and "
            + " ("
            + "  exists(select 1 from GR_ARBEID_INNTEKT gr inner join IAY_INNTEKT_ARBEID_YTELSER iaya on gr.register_id=iaya.id where iaya.ekstern_referanse=:eksternReferanse )"
            + "   OR "
            + "  exists (select 1 from GR_ARBEID_INNTEKT gr inner join IAY_INNTEKT_ARBEID_YTELSER iaya on gr.saksbehandlet_id=iaya.id where iaya.ekstern_referanse=:eksternReferanse )"
            + " )")
            .setParameter("ref", koblingReferanse.getReferanse())
            .setParameter("eksternReferanse", eksternReferanse);
        if (matchKoblingIayAggregat.getResultStream().findFirst().isEmpty()) {
            throw new IllegalStateException("KoblingReferanse [" + koblingReferanse + "] er ikke aktiv eller er ikke knyttet til IAY aggregat [" + eksternReferanse + "]");
        }
    }

    public TidssoneConfig hentKonfigurasjon() {
        Query showTimezone = entityManager.createNativeQuery("show timezone");
        Query currentTimestamp = entityManager.createNativeQuery("SELECT current_timestamp");
        Object timezone = showTimezone.getSingleResult();
        LocalDateTime tidsstempel = ((Timestamp) currentTimestamp.getSingleResult()).toLocalDateTime();
        return new TidssoneConfig((String) timezone, tidsstempel);
    }

    public Set<Inntektsmelding> hentAlleInntektsmeldingerFor(AktørId aktørId,
                                                             Saksnummer saksnummer,
                                                             no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType) {

        // sjekker kun kobling.aktiv; ikke om grunnlag er aktivt eller ikke, tar alt
        final TypedQuery<Inntektsmelding> query = entityManager.createQuery("""
            SELECT DISTINCT(im)
             FROM InntektArbeidGrunnlag gr
             JOIN Kobling k ON k.id = gr.koblingId
             JOIN Inntektsmeldinger ims ON ims.id = gr.inntektsmeldinger.id
             JOIN Inntektsmelding im ON im.inntektsmeldinger.id = ims.id
             WHERE k.saksnummer = :ref AND k.ytelseType = :ytelse and k.aktørId = :aktørId and k.aktiv=true
            """, Inntektsmelding.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        var inntektsmeldingSet = Set.copyOf(query.getResultList());
        return inntektsmeldingSet;
    }

    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdInfoInntektsmeldingerMapFor(AktørId aktørId,
                                                                                                         Saksnummer saksnummer,
                                                                                                         KoblingReferanse ref,
                                                                                                         no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType) {
        final TypedQuery<Object[]> query = entityManager.createQuery("""
            SELECT im, arbInf
             FROM InntektArbeidGrunnlag gr
             JOIN Kobling k ON k.id = gr.koblingId
             JOIN Inntektsmeldinger ims ON ims.id = gr.inntektsmeldinger.id
             JOIN Inntektsmelding im ON im.inntektsmeldinger.id = ims.id
             JOIN ArbeidsforholdInformasjon arbInf on arbInf.id = gr.arbeidsforholdInformasjon.id
             WHERE k.saksnummer = :ref AND k.koblingReferanse = :eksternRef AND k.ytelseType = :ytelse and k.aktørId = :aktørId and k.aktiv=true and gr.aktiv=:aktiv
             """, Object[].class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        query.setParameter("eksternRef", ref);
        query.setParameter("aktiv", true);

        return queryTilMap(query.getResultList());
    }

    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdInfoInntektsmeldingerMapFor(AktørId aktørId,
                                                                                                         Saksnummer saksnummer,
                                                                                                         no.nav.abakus.iaygrunnlag.kodeverk.YtelseType ytelseType) {

        final TypedQuery<Object[]> query = entityManager.createQuery("""
            SELECT im, arbInf
             FROM InntektArbeidGrunnlag gr
             JOIN Kobling k ON k.id = gr.koblingId
             JOIN Inntektsmeldinger ims ON ims.id = gr.inntektsmeldinger.id
             JOIN Inntektsmelding im ON im.inntektsmeldinger.id = ims.id
             JOIN ArbeidsforholdInformasjon arbInf on arbInf.id = gr.arbeidsforholdInformasjon.id
             WHERE k.saksnummer = :saksnummer AND k.ytelseType = :ytelse and k.aktørId = :aktørId and k.aktiv=true
            """, Object[].class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("saksnummer", saksnummer);
        query.setParameter("ytelse", ytelseType);

        return queryTilMap(query.getResultList());
    }

    private Map<Inntektsmelding, ArbeidsforholdInformasjon> queryTilMap(List<Object[]> list) {
        Map<Inntektsmelding, ArbeidsforholdInformasjon> inntektsmeldingArbinfoMap = new HashMap<>();
        list
            .forEach(res -> {
                Inntektsmelding im = (Inntektsmelding) res[0];
                ArbeidsforholdInformasjon arbInf = (ArbeidsforholdInformasjon) res[1];
                if (!inntektsmeldingArbinfoMap.containsKey(im)) {
                    inntektsmeldingArbinfoMap.put(im, arbInf);
                }
            });
        return inntektsmeldingArbinfoMap.entrySet().stream()
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
                 AND (gr.aktiv = true AND k.aktiv=true)
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

        var grunnlag = query.getResultList().stream().map(g -> g).collect(Collectors.toList());
        return grunnlag;
    }

    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = getAktivtInntektArbeidGrunnlag(koblingReferanse);
        return grunnlag.map(InntektArbeidYtelseGrunnlag.class::cast);
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
    public InntektArbeidYtelseAggregatBuilder opprettBuilderFor(KoblingReferanse koblingReferanse, UUID angittAggregatReferanse,
                                                                LocalDateTime angittOpprettetTidspunkt, VersjonType versjonType) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return opprettBuilderFor(versjonType, angittAggregatReferanse, angittOpprettetTidspunkt, grunnlag);
    }

    public Optional<OppgittOpptjening> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse) {
        TypedQuery<OppgittOpptjening> query = entityManager.createQuery("SELECT oo " +
            " FROM OppgittOpptjening oo " +
            " WHERE oo.eksternReferanse = :eksternReferanse", OppgittOpptjening.class);
        query.setParameter("eksternReferanse", oppgittOpptjeningEksternReferanse);
        var res = HibernateVerktøy.hentUniktResultat(query);

        if (res.isEmpty()) {
            return Optional.empty();
        } else {
            // sjekk om opptjening finnes i noen aktivt grunnlag
            var query2 = entityManager.createNativeQuery("select 1 from kobling k"
                + " inner join GR_ARBEID_INNTEKT gr on gr.kobling_id=k.id"
                + " inner join oppgitt_opptjening opp on opp.id=gr.oppgitt_opptjening_id"
                + " where k.aktiv=true and gr.aktiv=:aktiv and opp.ekstern_referanse=:ref");
            query2.setParameter("aktiv", true);
            query2.setParameter("ref", oppgittOpptjeningEksternReferanse);
            boolean harAktivKoblingOgGrunnlag = query.getResultStream().findAny().isPresent();
            if (harAktivKoblingOgGrunnlag) {
                return res;
            } else {
                throw new IllegalStateException("Etterspurte OppgittOpptjening som ikke er koblet til noe aktiv kobling/grunnlag: " + oppgittOpptjeningEksternReferanse);
            }
        }
    }

    private InntektArbeidYtelseAggregatBuilder opprettBuilderFor(VersjonType versjonType, UUID angittReferanse, LocalDateTime opprettetTidspunkt,
                                                                 Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(grunnlag);
        Objects.requireNonNull(grunnlagBuilder, "grunnlagBuilder");
        Optional<InntektArbeidYtelseGrunnlag> aggregat = Optional.ofNullable(grunnlagBuilder.getKladd()); // NOSONAR
        Objects.requireNonNull(aggregat, "aggregat"); // NOSONAR
        if (aggregat.isPresent()) {
            final InntektArbeidYtelseGrunnlag aggregat1 = aggregat.get();
            return InntektArbeidYtelseAggregatBuilder.builderFor(hentRiktigVersjon(versjonType, aggregat1), angittReferanse, opprettetTidspunkt, versjonType);
        }
        throw new TekniskException("FP-512369", "Aggregat kan ikke være null ved opprettelse av builder");
    }

    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder builder) {
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = getGrunnlagBuilder(koblingReferanse, builder);
        final ArbeidsforholdInformasjon informasjon = opptjeningAggregatBuilder.getInformasjon();

        // lagre reserverte interne referanser opprettet tidligere
        builder.getNyeInternArbeidsforholdReferanser()
            .forEach(aref -> informasjon.opprettNyReferanse(aref.getArbeidsgiver(), aref.getInternReferanse(), aref.getEksternReferanse()));
        lagreOgFlush(koblingReferanse, opptjeningAggregatBuilder.build());
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

    public GrunnlagReferanse lagrePrJournalpostId(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder oppgittOpptjening) {
        if (oppgittOpptjening == null) {
            return null;
        }
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayGrunnlag);
        grunnlag.leggTilOppgittOpptjening(oppgittOpptjening);

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

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                   List<Inntektsmelding> inntektsmeldingerList) {
        Objects.requireNonNull(inntektsmeldingerList, "inntektsmelding"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingReferanse);

        var utdaterteInntektsmeldingerJournalposter = oppdaterBuilderMedNyeInntektsmeldinger(informasjonBuilder, inntektsmeldingerList, builder);

        InntektArbeidYtelseGrunnlag grunnlag = builder.build();
        var utdaterteInntektsmeldinger = inntektsmeldingerList.stream()
            .filter(it -> utdaterteInntektsmeldingerJournalposter.contains(it.getJournalpostId())
                && harIkkeAltHåndtertJournalpost(grunnlag, it.getJournalpostId()))
            .collect(Collectors.toList());
        var utdatertBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(grunnlag);
        lagreDeaktivertGrunnlagMedUtdaterteInntektsmeldinger(koblingReferanse, informasjonBuilder, utdaterteInntektsmeldinger, utdatertBuilder);
        lagreOgFlush(koblingReferanse, grunnlag);

        return grunnlag.getGrunnlagReferanse();
    }

    private boolean harIkkeAltHåndtertJournalpost(InntektArbeidYtelseGrunnlag build, JournalpostId journalpostId) {
        var imaggregat = build.getInntektsmeldinger();
        return imaggregat.map(inntektsmeldingAggregat -> inntektsmeldingAggregat.getInntektsmeldinger().stream().noneMatch(it -> it.getJournalpostId().equals(journalpostId))).orElse(true);
    }

    private void lagreDeaktivertGrunnlagMedUtdaterteInntektsmeldinger(KoblingReferanse koblingReferanse, ArbeidsforholdInformasjonBuilder informasjonBuilder,
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
                .filter(it -> utdaterteInntektsmeldingerJournalposter.contains(it.getJournalpostId())
                    && harIkkeAltHåndtertJournalpost(build, it.getJournalpostId()))
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

    private InntektArbeidYtelseGrunnlagBuilder getGrunnlagBuilder(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder builder) {
        Objects.requireNonNull(builder, "inntektArbeidYtelserBuilder"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = opprettGrunnlagBuilderFor(koblingReferanse);
        opptjeningAggregatBuilder.medData(builder);
        return opptjeningAggregatBuilder;
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

        sjekkKonsistens(nyttGrunnlag);

        Optional<InntektArbeidYtelseGrunnlag> tidligereAggregat = getAktivtInntektArbeidGrunnlag(koblingReferanse);

        if (tidligereAggregat.isPresent()) {
            InntektArbeidYtelseGrunnlag aggregat = tidligereAggregat.get();
            if (diffResultat(aggregat, nyttGrunnlag, false).isEmpty()) {
                if (aggregat != null) {
                    log.info("Ingen endring i iay-grunnlag, skipper ny lagring - beholder grunnlag: " + aggregat.getGrunnlagReferanse());
                }
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

    /**
     * Kaster exception hvis grunnlaget er i en ugyldig tilstand
     *
     * @param grunnlag grunnlaget som skal sjekkes
     */
    private void sjekkKonsistens(InntektArbeidYtelseGrunnlag grunnlag) {
        final var arbeidsforholdInformasjon = grunnlag.getArbeidsforholdInformasjon()
            .orElseGet(() -> ArbeidsforholdInformasjonBuilder.builder(Optional.empty()).build());

        var grRef = grunnlag.getGrunnlagReferanse();

        grunnlag.getRegisterVersjon().ifPresent(aggregat -> aggregat.getAktørArbeid()
            .stream()
            .map(AktørArbeid::hentAlleYrkesaktiviteter)
            .flatMap(Collection::stream)
            .forEach(it -> {
                if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                    var arRef = aggregat.getEksternReferanse();
                    arbeidsforholdInformasjon.finnEkstern(grRef, it.getArbeidsgiver(), it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                    // (kaster exception hvis ikke)
                }
            }));

        grunnlag.getInntektsmeldinger().ifPresent(aggregat -> aggregat.getInntektsmeldinger()
            .forEach(it -> {
                if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                    arbeidsforholdInformasjon.finnEkstern(grRef, it.getArbeidsgiver(), it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                    // (kaster exception hvis ikke)
                }
            }));

        grunnlag.getSaksbehandletVersjon().ifPresent(aggregat -> aggregat.getAktørArbeid()
            .stream()
            .map(AktørArbeid::hentAlleYrkesaktiviteter)
            .flatMap(Collection::stream)
            .forEach(it -> {
                if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                    var arRef = aggregat.getEksternReferanse();
                    arbeidsforholdInformasjon.finnEkstern(grRef, it.getArbeidsgiver(), it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
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
        nyttGrunnlag.getOppgittOpptjeningAggregat().ifPresent(this::lagreOppgitteOpptjeninger);

        var registerVersjon = entitet.getRegisterVersjon();
        registerVersjon.ifPresent(this::lagreInntektArbeid);

        var saksbehandletFørVersjon = nyttGrunnlag.getSaksbehandletVersjon();
        saksbehandletFørVersjon.ifPresent(this::lagreInntektArbeid);

        nyttGrunnlag.getInntektsmeldinger().ifPresent(ims -> this.lagreInntektsMeldinger(ims));

        entitet.getArbeidsforholdInformasjon().ifPresent(this::lagreInformasjon);
        entityManager.persist(nyttGrunnlag);
    }

    private void lagreOppgitteOpptjeninger(OppgittOpptjeningAggregat oppgittOpptjeningAggregat) {
        entityManager.persist(oppgittOpptjeningAggregat);
        oppgittOpptjeningAggregat.getOppgitteOpptjeninger()
            .forEach(this::lagreOppgittOpptjening);
    }

    private void lagreInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        var arbeidsforholdInformasjonEntitet = arbeidsforholdInformasjon; // NOSONAR
        entityManager.persist(arbeidsforholdInformasjonEntitet);
        for (var referanseEntitet : arbeidsforholdInformasjonEntitet.getArbeidsforholdReferanser()) {
            entityManager.persist(referanseEntitet);
        }
        for (var overstyringEntitet : arbeidsforholdInformasjonEntitet.getOverstyringer()) {
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

        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery("SELECT gr FROM InntektArbeidGrunnlag gr " +
            " JOIN Kobling k ON k.id=gr.koblingId" + // NOSONAR
            " WHERE k.koblingReferanse = :ref" + //$NON-NLS-1$ //NOSONAR
            " AND gr.aktiv = :aktivt", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", koblingReferanse); // NOSONAR
        query.setParameter("aktivt", true);
        List<InntektArbeidYtelseGrunnlag> resultList = query.getResultList();
        if (resultList.isEmpty()) {
            return Optional.empty();
        } else if (resultList.size() == 1) {
            validerKoblingErAktiv(koblingReferanse); // validerer her istdf. spørring for å avdekke om det brukes feil
            final Optional<InntektArbeidYtelseGrunnlag> grunnlag = resultList.stream().findFirst();
            return grunnlag;
        }
        throw new IllegalStateException("Finner flere aktive grunnlag på koblingReferanse=" + koblingReferanse);
    }

    private Optional<Kobling> validerKoblingErAktiv(KoblingReferanse koblingReferanse) {
        return koblingRepository.hentForKoblingReferanse(koblingReferanse);
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
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = getVersjonAvInntektArbeidYtelseForReferanseId(grunnlagReferanse);
        return grunnlag.map(InntektArbeidYtelseGrunnlag.class::cast);
    }

    public Long hentKoblingIdFor(GrunnlagReferanse grunnlagReferanse) {
        var query = entityManager.createQuery("SELECT k FROM Kobling k JOIN InntektArbeidGrunnlag gr ON gr.koblingId=k.id WHERE gr.grunnlagReferanse=:ref", Kobling.class);
        query.setParameter("ref", grunnlagReferanse);
        Kobling kobling = HibernateVerktøy.hentEksaktResultat(query);
        validerKoblingErAktiv(kobling.getKoblingReferanse());
        return kobling.getId();
    }

    private Optional<InntektArbeidYtelseGrunnlag> getVersjonAvInntektArbeidYtelseForReferanseId(GrunnlagReferanse grunnlagReferanse) {
        Objects.requireNonNull(grunnlagReferanse, "aggregatId"); // NOSONAR
        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " +
            " WHERE gr.grunnlagReferanse = :ref ", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", grunnlagReferanse);
        query.setHint(QueryHints.HINT_CACHE_MODE, "IGNORE");
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = query.getResultList().stream().findFirst();

        if (grunnlagOpt.isPresent()) {
            var kobling = koblingRepository.hentForKoblingId(grunnlagOpt.get().getKoblingId());
            validerKoblingErAktiv(kobling.getKoblingReferanse());
        }
        return grunnlagOpt;
    }

    public KoblingReferanse hentKoblingReferanseFor(GrunnlagReferanse grunnlagReferanse) {
        final TypedQuery<KoblingReferanse> query = entityManager
            .createQuery("SELECT k.koblingReferanse FROM Kobling k JOIN InntektArbeidGrunnlag gr ON gr.koblingId = k.id WHERE gr.grunnlagReferanse=:ref",
                KoblingReferanse.class);
        query.setParameter("ref", grunnlagReferanse);
        return query.getSingleResult();
    }

    private Long hentKoblingIdFor(KoblingReferanse koblingReferanse) {
        return koblingRepository.hentForKoblingReferanse(koblingReferanse).map(Kobling::getId).orElse(null);
    }

    public boolean erGrunnlagAktivt(UUID eksternReferanse) {
        Objects.requireNonNull(eksternReferanse, "aggregatId"); // NOSONAR
        final TypedQuery<Boolean> query = entityManager.createQuery("SELECT gr.aktiv FROM InntektArbeidGrunnlag gr JOIN Kobling k ON gr.koblingId=k.id " +
            " WHERE gr.grunnlagReferanse = :ref AND k.aktiv = true ", Boolean.class);
        query.setParameter("ref", new GrunnlagReferanse(eksternReferanse));
        query.setHint(QueryHints.HINT_CACHE_MODE, "IGNORE");
        Optional<Boolean> grunnlagOpt = query.getResultList().stream().findFirst();
        return grunnlagOpt.orElse(false);
    }
}
