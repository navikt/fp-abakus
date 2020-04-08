package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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

import no.nav.foreldrepenger.abakus.diff.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.abakus.diff.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyring;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseGraph;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class InntektArbeidYtelseRepository implements ByggInntektArbeidYtelseRepository {
    private static final Logger log = LoggerFactory.getLogger(InntektArbeidYtelseRepository.class);
    private EntityManager entityManager;

    InntektArbeidYtelseRepository() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseRepository(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = getAktivtInntektArbeidGrunnlag(koblingReferanse);
        return grunnlag.orElseThrow(() -> InntektArbeidYtelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(koblingReferanse).toException());
    }

    public List<InntektArbeidYtelseGrunnlag> hentAlleInntektArbeidYtelseGrunnlagFor(AktørId aktørId, KoblingReferanse koblingReferanse, boolean kunAktiv) {
        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr JOIN KOBLING k " + // NOSONAR
            " WHERE k.koblingReferanse = :ref AND k.aktørId = :aktørId " + // NOSONAR
            " AND gr.aktiv = :aktivt", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", koblingReferanse); // NOSONAR
        query.setParameter("aktivt", kunAktiv);
        query.setParameter("aktørId", aktørId);

        if (kunAktiv) {
            final Optional<InntektArbeidYtelseGrunnlag> grunnlag = HibernateVerktøy.hentUniktResultat(query);
            return grunnlag.isPresent() ? List.of(grunnlag.get()) : Collections.emptyList();
        } else {
            return query.getResultStream().map(g -> g).collect(Collectors.toList());
        }
    }

    public Optional<InntektArbeidYtelseAggregat> hentIAYAggregatFor(UUID eksternReferanse) {
        TypedQuery<InntektArbeidYtelseAggregat> query = entityManager.createQuery("SELECT iay " +
            "FROM InntektArbeidYtelser iay " +
            "WHERE iay.eksternReferanse = :eksternReferanse", InntektArbeidYtelseAggregat.class);
        query.setParameter("eksternReferanse", eksternReferanse);

        return HibernateVerktøy.hentUniktResultat(query);
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
                                                             no.nav.foreldrepenger.abakus.kodeverk.YtelseType ytelseType) {

        final TypedQuery<Inntektsmelding> query = entityManager.createQuery("SELECT DISTINCT(im)" +
            " FROM InntektArbeidGrunnlag gr" +
            " JOIN Kobling k ON k.id = gr.koblingId" + // NOSONAR
            " JOIN Inntektsmeldinger ims ON ims.id = gr.inntektsmeldinger.id" + // NOSONAR
            " JOIN Inntektsmelding im ON im.inntektsmeldinger.id = ims.id" + // NOSONAR
            " WHERE k.saksnummer = :ref AND k.ytelseType = :ytelse and k.aktørId = :aktørId "// NOSONAR
            , Inntektsmelding.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        var inntektsmeldingSet = Set.copyOf(query.getResultList());
        return inntektsmeldingSet;
    }

    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdInfoInntektsmeldingerMapFor(AktørId aktørId,
                                                                                                         Saksnummer saksnummer,
                                                                                                         KoblingReferanse ref,
                                                                                                         no.nav.foreldrepenger.abakus.kodeverk.YtelseType ytelseType) {
        final TypedQuery<Object[]> query = entityManager.createQuery("SELECT im, arbInf" +
                " FROM InntektArbeidGrunnlag gr" + // NOSONAR
                " JOIN Kobling k ON k.id = gr.koblingId" + // NOSONAR
                " JOIN Inntektsmeldinger ims ON ims.id = gr.inntektsmeldinger.id" + // NOSONAR
                " JOIN Inntektsmelding im ON im.inntektsmeldinger.id = ims.id" + // NOSONAR
                " JOIN ArbeidsforholdInformasjon arbInf on arbInf.id = gr.arbeidsforholdInformasjon.id" + // NOSONAR
                " WHERE k.saksnummer = :ref AND k.koblingReferanse = :eksternRef AND k.ytelseType = :ytelse and k.aktørId = :aktørId "
            ,Object[].class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        query.setParameter("eksternRef", ref);

        return queryTilMap(query.getResultList());
    }

    public Map<Inntektsmelding, ArbeidsforholdInformasjon> hentArbeidsforholdInfoInntektsmeldingerMapFor(AktørId aktørId,
                                                             Saksnummer saksnummer,
                                                             no.nav.foreldrepenger.abakus.kodeverk.YtelseType ytelseType) {

        final TypedQuery<Object[]> query = entityManager.createQuery("SELECT im, arbInf" +
                " FROM InntektArbeidGrunnlag gr" + // NOSONAR
                " JOIN Kobling k ON k.id = gr.koblingId" + // NOSONAR
                " JOIN Inntektsmeldinger ims ON ims.id = gr.inntektsmeldinger.id" + // NOSONAR
                " JOIN Inntektsmelding im ON im.inntektsmeldinger.id = ims.id" + // NOSONAR
                " JOIN ArbeidsforholdInformasjon arbInf on arbInf.id = gr.arbeidsforholdInformasjon.id" + // NOSONAR
                " WHERE k.saksnummer = :ref AND k.ytelseType = :ytelse and k.aktørId = :aktørId "
            ,Object[].class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
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
        return inntektsmeldingArbinfoMap;
    }

    public List<InntektArbeidYtelseGrunnlag> hentAlleInntektArbeidYtelseGrunnlagFor(AktørId aktørId,
                                                                                    Saksnummer saksnummer,
                                                                                    no.nav.foreldrepenger.abakus.kodeverk.YtelseType ytelseType,
                                                                                    boolean kunAktive) {

        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery("SELECT gr" +
            " FROM InntektArbeidGrunnlag gr" +
            " JOIN Kobling k ON k.id = gr.koblingId" + // NOSONAR
            " WHERE k.saksnummer = :ref AND k.ytelseType = :ytelse and k.aktørId = :aktørId " + // NOSONAR
            (kunAktive ? " AND gr.aktiv = :aktivt" : "") +
            " ORDER BY gr.koblingId, gr.opprettetTidspunkt", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("aktørId", aktørId);
        query.setParameter("ref", saksnummer);
        query.setParameter("ytelse", ytelseType);
        if (kunAktive) {
            query.setParameter("aktivt", kunAktive);
        }

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
     * @param versjonType (REGISTER, SAKSBEHANDLET)
     * @return InntektArbeidYtelseAggregatBuilder
     *         <p>
     *         NB! bør benytte via InntektArbeidYtelseTjeneste og ikke direkte
     */
    public InntektArbeidYtelseAggregatBuilder opprettBuilderFor(KoblingReferanse koblingReferanse, UUID angittAggregatReferanse,
                                                                LocalDateTime angittOpprettetTidspunkt, VersjonType versjonType) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return opprettBuilderFor(versjonType, angittAggregatReferanse, angittOpprettetTidspunkt, grunnlag);
    }

    public Statistikk hentStats() {
        final TypedQuery<Long> query = entityManager.createQuery("SELECT COUNT(gr) FROM InntektArbeidGrunnlag gr ", Long.class);
        Long antallGrunnlag = HibernateVerktøy.hentEksaktResultat(query);
        Query histogramQuery = entityManager.createNativeQuery("select cnt, count(*) from " +
            "( " +
            "select count(*) cnt, kobling_id from gr_arbeid_inntekt grai " +
            "group by kobling_id " +
            ") b " +
            "group by cnt " +
            "order by cnt");
        Map<BigInteger, BigInteger> histogram = new HashMap<>();

        @SuppressWarnings("unchecked")
        List<Object[]> resultList = histogramQuery.getResultList();
        for (Object[] rs : resultList) {

            histogram.put((BigInteger) rs[0], (BigInteger) rs[1]);
        }

        return new Statistikk(antallGrunnlag, histogram);
    }

    public Optional<OppgittOpptjening> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse) {
        TypedQuery<OppgittOpptjening> query = entityManager.createQuery("SELECT oo " +
            "FROM OppgittOpptjening oo " +
            "WHERE oo.eksternReferanse = :eksternReferanse", OppgittOpptjening.class);
        query.setParameter("eksternReferanse", oppgittOpptjeningEksternReferanse);
        return HibernateVerktøy.hentUniktResultat(query);
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
        throw InntektArbeidYtelseFeil.FACTORY.aggregatKanIkkeVæreNull().toException();
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder builder) {
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = getGrunnlagBuilder(koblingReferanse, builder);
        final ArbeidsforholdInformasjon informasjon = opptjeningAggregatBuilder.getInformasjon();

        // lagre reserverte interne referanser opprettet tidligere
        builder.getNyeInternArbeidsforholdReferanser()
            .forEach(aref -> informasjon.opprettNyReferanse(aref.getArbeidsgiver(), aref.getInternReferanse(), aref.getEksternReferanse()));
        lagreOgFlush(koblingReferanse, opptjeningAggregatBuilder.build());
    }

    @Override
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

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                   List<Inntektsmelding> inntektsmeldingerList) {
        Objects.requireNonNull(inntektsmeldingerList, "inntektsmelding"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingReferanse);

        oppdaterBuilderMedNyeInntektsmeldinger(informasjonBuilder, inntektsmeldingerList, builder);

        InntektArbeidYtelseGrunnlag build = builder.build();
        lagreOgFlush(koblingReferanse, build);
        return build.getGrunnlagReferanse();
    }

    /** Legger til nye inntektsmeldinger på angitt IAYG builder. */
    public void oppdaterBuilderMedNyeInntektsmeldinger(ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                                       List<Inntektsmelding> nyeInntektsmeldinger,
                                                       InntektArbeidYtelseGrunnlagBuilder targetBuilder) {

        if(nyeInntektsmeldinger.isEmpty()) {
            return; // quick exit, ingenting nytt å gjøre her
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
            inntektsmeldinger.leggTilEllerErstatt(inntektsmelding);
        }
        targetBuilder.setInntektsmeldinger(inntektsmeldinger);
        targetBuilder.medInformasjon(informasjonBuilder.build());
    }

    private InntektArbeidYtelseGrunnlagBuilder getGrunnlagBuilder(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder builder) {
        Objects.requireNonNull(builder, "inntektArbeidYtelserBuilder"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = opprettGrunnlagBuilderFor(koblingReferanse);
        opptjeningAggregatBuilder.medData(builder);
        return opptjeningAggregatBuilder;
    }

    public boolean erEndring(InntektArbeidYtelseGrunnlag aggregat, InntektArbeidYtelseGrunnlag nyttAggregat) {

        TraverseGraph traverser = TraverseEntityGraphFactory.build(true);

        if (aggregat == null) {
            return true;
        }

        DiffResult diff = new DiffEntity(traverser)
            .diff(aggregat, nyttAggregat);
        return !diff.isEmpty();
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
                log.info("Ingen diff mellom nytt og gammelt grunnlag. Nytt grunnlag: {}, Gammelt grunnlag: {}", nyttGrunnlag, aggregat);
                return;
            }
            log.info("Lagrer nytt grunnlag: {}", nyttGrunnlag);

            aggregat.setAktivt(false);
            entityManager.persist(aggregat);
            entityManager.flush();

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
            .orElse(ArbeidsforholdInformasjonBuilder.builder(Optional.empty()).build());

        grunnlag.getRegisterVersjon().ifPresent(aggregat -> aggregat.getAktørArbeid()
            .stream()
            .map(AktørArbeid::hentAlleYrkesaktiviteter)
            .flatMap(Collection::stream)
            .forEach(it -> {
                if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                    arbeidsforholdInformasjon.finnEkstern(it.getArbeidsgiver(), it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                                                                                                            // (kaster exception hvis ikke)
                }
            }));

        grunnlag.getInntektsmeldinger().ifPresent(aggregat -> aggregat.getInntektsmeldinger()
            .forEach(it -> {
                if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                    arbeidsforholdInformasjon.finnEkstern(it.getArbeidsgiver(), it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                                                                                                            // (kaster exception hvis ikke)
                }
            }));

        grunnlag.getSaksbehandletVersjon().ifPresent(aggregat -> aggregat.getAktørArbeid()
            .stream()
            .map(AktørArbeid::hentAlleYrkesaktiviteter)
            .flatMap(Collection::stream)
            .forEach(it -> {
                if (it.getArbeidsforholdRef().gjelderForSpesifiktArbeidsforhold()) {
                    arbeidsforholdInformasjon.finnEkstern(it.getArbeidsgiver(), it.getArbeidsforholdRef()); // Validerer om det finnes ekstern for intern ref
                                                                                                            // (kaster exception hvis ikke)
                }
            }));
    }

    /**
     * @param nyttGrunnlag
     * @param koblingReferanse
     * @param aktiv
     * @deprecated OBS! Kun for migrering av vedtak fra FPSAK til abakus
     */
    @Deprecated(forRemoval = true)
    public void lagreMigrertGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, KoblingReferanse koblingReferanse) {
        InntektArbeidYtelseGrunnlag entitet = nyttGrunnlag;

        if (nyttGrunnlag.isAktiv()) {
            Optional<InntektArbeidYtelseGrunnlag> tidligereAggregat = getAktivtInntektArbeidGrunnlag(koblingReferanse);
            if (tidligereAggregat.isPresent()) {
                InntektArbeidYtelseGrunnlag aggregat = tidligereAggregat.get();
                aggregat.setAktivt(false);
                entityManager.persist(aggregat);
                entityManager.flush();
            }
        }
        lagreGrunnlag(entitet, koblingReferanse);
        entityManager.flush();
    }

    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlagBuilder builder) {
        lagreOgFlush(koblingReferanse, builder.build());
    }

    @Deprecated(forRemoval = true)
    private void slettGrunnlag(InntektArbeidYtelseGrunnlag grunnlag) {
        log.info("[MIGRERING] Mottatt nytt grunnlag med samme referanse. Sletter grunnlag med grunnlagsref={}", grunnlag.getGrunnlagReferanse());
        grunnlag.getRegisterVersjon().ifPresent(this::slettAggregat);
        grunnlag.getSaksbehandletVersjon().ifPresent(this::slettAggregat);
        grunnlag.getInntektsmeldinger().ifPresent(this::slettInntektsmeldinger);
        grunnlag.getArbeidsforholdInformasjon().ifPresent(this::slettInformasjon);
        grunnlag.getOppgittOpptjening().ifPresent(this::slettOppgittOpptjening);
        entityManager.createNativeQuery("DELETE FROM gr_arbeid_inntekt WHERE id = :grunnlagId")
            .setParameter("grunnlagId", grunnlag.getId())
            .executeUpdate();
    }

    @Deprecated(forRemoval = true)
    private void slettInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        entityManager.createNativeQuery("DELETE FROM iay_arbeidsforhold_refer WHERE informasjon_id = :informasjonId")
            .setParameter("informasjonId", arbeidsforholdInformasjon.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_overstyrte_perioder WHERE arbeidsforhold_id " +
            "IN (SELECT id FROM iay_arbeidsforhold WHERE informasjon_id = :informasjonId)")
            .setParameter("informasjonId", arbeidsforholdInformasjon.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_arbeidsforhold WHERE informasjon_id = :informasjonId")
            .setParameter("informasjonId", arbeidsforholdInformasjon.getId())
            .executeUpdate();
        entityManager.createNativeQuery("UPDATE gr_arbeid_inntekt SET informasjon_id = null " +
            "WHERE informasjon_id = :informasjonId")
            .setParameter("informasjonId", arbeidsforholdInformasjon.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_informasjon WHERE id = :informasjonId")
            .setParameter("informasjonId", arbeidsforholdInformasjon.getId())
            .executeUpdate();
    }

    @Deprecated(forRemoval = true)
    private void slettInntektsmeldinger(InntektsmeldingAggregat inntektsmeldinger) {
        entityManager.createNativeQuery("DELETE FROM iay_gradering WHERE inntektsmelding_id " +
            "IN (SELECT ID FROM iay_inntektsmelding WHERE inntektsmeldinger_id = :inntektsmeldingerId)")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_natural_ytelse WHERE inntektsmelding_id " +
            "IN (SELECT ID FROM iay_inntektsmelding WHERE inntektsmeldinger_id = :inntektsmeldingerId)")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_utsettelse_periode WHERE inntektsmelding_id " +
            "IN (SELECT ID FROM iay_inntektsmelding WHERE inntektsmeldinger_id = :inntektsmeldingerId)")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_refusjon WHERE inntektsmelding_id " +
            "IN (SELECT ID FROM iay_inntektsmelding WHERE inntektsmeldinger_id = :inntektsmeldingerId)")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_inntektsmelding WHERE inntektsmeldinger_id = :inntektsmeldingerId")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
        entityManager.createNativeQuery("UPDATE gr_arbeid_inntekt SET inntektsmeldinger_id = null " +
            "WHERE inntektsmeldinger_id = :inntektsmeldingerId")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_inntektsmeldinger WHERE id = :inntektsmeldingerId")
            .setParameter("inntektsmeldingerId", inntektsmeldinger.getId())
            .executeUpdate();
    }

    @Deprecated(forRemoval = true)
    private void slettOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        entityManager.createNativeQuery("DELETE FROM iay_oppgitt_arbeidsforhold WHERE oppgitt_opptjening_id = :oppgittOpptjeningId")
            .setParameter("oppgittOpptjeningId", oppgittOpptjening.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_egen_naering WHERE oppgitt_opptjening_id = :oppgittOpptjeningId")
            .setParameter("oppgittOpptjeningId", oppgittOpptjening.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_annen_aktivitet WHERE oppgitt_opptjening_id = :oppgittOpptjeningId")
            .setParameter("oppgittOpptjeningId", oppgittOpptjening.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_oppgitt_frilansoppdrag WHERE frilans_id " +
            "IN (SELECT id FROM iay_oppgitt_frilans WHERE oppgitt_opptjening_id = :oppgittOpptjeningId)")
            .setParameter("oppgittOpptjeningId", oppgittOpptjening.getId())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_oppgitt_frilans WHERE oppgitt_opptjening_id = :oppgittOpptjeningId")
            .setParameter("oppgittOpptjeningId", oppgittOpptjening.getId())
            .executeUpdate();
        entityManager.createNativeQuery(
            "UPDATE gr_arbeid_inntekt set oppgitt_opptjening_id = null WHERE oppgitt_opptjening_id IN (SELECT ID FROM iay_oppgitt_opptjening WHERE ekstern_referanse = :eksterRef)")
            .setParameter("eksterRef", oppgittOpptjening.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_oppgitt_opptjening WHERE ekstern_referanse = :eksterRef")
            .setParameter("eksterRef", oppgittOpptjening.getEksternReferanse())
            .executeUpdate();
    }

    @Deprecated(forRemoval = true)
    private void slettAggregat(InntektArbeidYtelseAggregat aggregat) {

        entityManager.createNativeQuery("DELETE FROM iay_permisjon WHERE yrkesaktivitet_id " +
            "IN (SELECT id FROM iay_yrkesaktivitet WHERE aktoer_arbeid_id " +
            "IN (SELECT id FROM iay_aktoer_arbeid WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_aktivitets_avtale WHERE yrkesaktivitet_id " +
            "IN (SELECT id FROM iay_yrkesaktivitet WHERE aktoer_arbeid_id " +
            "IN (SELECT id FROM iay_aktoer_arbeid WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_yrkesaktivitet WHERE aktoer_arbeid_id " +
            "IN (SELECT id FROM iay_aktoer_arbeid WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_aktoer_arbeid WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM iay_inntektspost WHERE inntekt_id " +
            "IN (SELECT id FROM iay_inntekt WHERE aktoer_inntekt_id " +
            "IN (SELECT id FROM iay_aktoer_inntekt WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_inntekt WHERE aktoer_inntekt_id " +
            "IN (SELECT id FROM iay_aktoer_inntekt WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_aktoer_inntekt WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM iay_ytelse_anvist WHERE ytelse_id " +
            "IN (SELECT id FROM iay_relatert_ytelse WHERE aktoer_ytelse_id " +
            "IN (SELECT id FROM iay_aktoer_ytelse WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_ytelse_stoerrelse WHERE ytelse_grunnlag_id IN (SELECT id FROM iay_ytelse_grunnlag WHERE ytelse_id " +
            "IN (SELECT id FROM iay_relatert_ytelse WHERE aktoer_ytelse_id " +
            "IN (SELECT id FROM iay_aktoer_ytelse WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef))))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_ytelse_grunnlag WHERE ytelse_id IN (SELECT id FROM iay_relatert_ytelse WHERE aktoer_ytelse_id " +
            "IN (SELECT id FROM iay_aktoer_ytelse WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager
            .createNativeQuery("DELETE FROM iay_relatert_ytelse WHERE aktoer_ytelse_id IN (SELECT id FROM iay_aktoer_ytelse WHERE inntekt_arbeid_ytelser_id " +
                "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef))")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_aktoer_ytelse WHERE inntekt_arbeid_ytelser_id " +
            "IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();

        entityManager.createNativeQuery(
            "UPDATE gr_arbeid_inntekt set register_id = null WHERE register_id IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery(
            "UPDATE gr_arbeid_inntekt set saksbehandlet_id = null WHERE saksbehandlet_id IN (SELECT ID FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef)")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
        entityManager.createNativeQuery("DELETE FROM iay_inntekt_arbeid_ytelser WHERE ekstern_referanse = :eksterRef")
            .setParameter("eksterRef", aggregat.getEksternReferanse())
            .executeUpdate();
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

        final Optional<InntektArbeidYtelseAggregat> registerVersjon = entitet.getRegisterVersjon();
        registerVersjon.ifPresent(this::lagreInntektArbeid);

        final Optional<InntektArbeidYtelseAggregat> saksbehandletFørVersjon = nyttGrunnlag.getSaksbehandletVersjon();
        saksbehandletFørVersjon.ifPresent(this::lagreInntektArbeid);

        nyttGrunnlag.getInntektsmeldinger().ifPresent(ims -> this.lagreInntektsMeldinger(ims));

        entitet.getArbeidsforholdInformasjon().ifPresent(this::lagreInformasjon);
        entityManager.persist(nyttGrunnlag);
    }

    private void lagreInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        final ArbeidsforholdInformasjon arbeidsforholdInformasjonEntitet = arbeidsforholdInformasjon; // NOSONAR
        entityManager.persist(arbeidsforholdInformasjonEntitet);
        for (ArbeidsforholdReferanse referanseEntitet : arbeidsforholdInformasjonEntitet.getArbeidsforholdReferanser()) {
            entityManager.persist(referanseEntitet);
        }
        for (ArbeidsforholdOverstyring overstyringEntitet : arbeidsforholdInformasjonEntitet.getOverstyringer()) {
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

            for(var fravær : entitet.getOppgittFravær()) {
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
        for (Inntekt inntekt : ((AktørInntektEntitet) aktørInntekt).getInntekt()) {
            entityManager.persist(inntekt);
            for (Inntektspost inntektspost : inntekt.getAlleInntektsposter()) {
                entityManager.persist(inntektspost);
            }
        }
    }

    private void lagreAktørArbeid(AktørArbeid aktørArbeid) {
        for (Yrkesaktivitet yrkesaktivitet : ((AktørArbeidEntitet) aktørArbeid).hentAlleYrkesaktiviteter()) {
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
            log.info("Lagrer ytelse {}", ytelse);
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
            "JOIN Kobling k ON gr.koblingId = k.id " + // NOSONAR
            "WHERE k.koblingReferanse = :ref " + //$NON-NLS-1$ //NOSONAR
            "AND gr.aktiv = :aktivt", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", koblingReferanse); // NOSONAR
        query.setParameter("aktivt", true);
        List<InntektArbeidYtelseGrunnlag> resultList = query.getResultList();
        if (resultList.size() < 2) {
            final Optional<InntektArbeidYtelseGrunnlag> grunnlag = resultList.stream().findFirst();
            return grunnlag;
        }
        throw new IllegalStateException("Finner flere aktive grunnlag på koblingReferanse=" + koblingReferanse);
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
        final TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k JOIN InntektArbeidGrunnlag gr WHERE gr.grunnlagReferanse=:ref",
            Long.class);
        query.setParameter("ref", grunnlagReferanse);
        return query.getSingleResult();
    }

    private Optional<InntektArbeidYtelseGrunnlag> getVersjonAvInntektArbeidYtelseForReferanseId(GrunnlagReferanse grunnlagReferanse) {
        Objects.requireNonNull(grunnlagReferanse, "aggregatId"); // NOSONAR
        final TypedQuery<InntektArbeidYtelseGrunnlag> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " +
            "WHERE gr.grunnlagReferanse = :ref ", InntektArbeidYtelseGrunnlag.class);
        query.setParameter("ref", grunnlagReferanse);
        query.setHint(QueryHints.HINT_CACHE_MODE, "IGNORE");
        Optional<InntektArbeidYtelseGrunnlag> grunnlagOpt = query.getResultList().stream().findFirst();
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
        final TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k WHERE k.koblingReferanse=:ref", Long.class);
        query.setParameter("ref", koblingReferanse);
        return HibernateVerktøy.hentUniktResultat(query).orElse(null);
    }

    public boolean erGrunnlagAktivt(UUID eksternReferanse) {
        Objects.requireNonNull(eksternReferanse, "aggregatId"); // NOSONAR
        final TypedQuery<Boolean> query = entityManager.createQuery("SELECT gr.aktiv FROM InntektArbeidGrunnlag gr " +
            "WHERE gr.grunnlagReferanse = :ref ", Boolean.class);
        query.setParameter("ref", new GrunnlagReferanse(eksternReferanse));
        query.setHint(QueryHints.HINT_CACHE_MODE, "IGNORE");
        Optional<Boolean> grunnlagOpt = query.getResultList().stream().findFirst();
        return grunnlagOpt.orElse(false);
    }
}
