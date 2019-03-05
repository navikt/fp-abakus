package no.nav.foreldrepenger.abakus.domene.iay;

import static no.nav.foreldrepenger.abakus.diff.RegisterdataDiffsjekker.eksistenssjekkResultat;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.nav.foreldrepenger.abakus.behandling.Fagsystem;
import no.nav.foreldrepenger.abakus.diff.RegisterdataDiffsjekker;
import no.nav.foreldrepenger.abakus.diff.TraverseEntityGraphFactory;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdHandlingType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdOverstyringEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdReferanseEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Gradering;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.InntektsmeldingEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.NaturalYtelse;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Refusjon;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.UtsettelsePeriode;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.Frilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@ApplicationScoped
public class InntektArbeidYtelseRepositoryImpl implements InntektArbeidYtelseRepository {
    private static final String BEH_NULL = "behandling";
    private EntityManager entityManager;

    public InntektArbeidYtelseRepositoryImpl() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(Long behandlingId) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getAktivtInntektArbeidGrunnlag(behandlingId);
        return grunnlag.orElseThrow(() -> InntektArbeidYtelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(behandlingId).toException());
    }

    private Optional<InntektArbeidYtelseGrunnlag> hentAggregatPåIdHvisEksisterer(Long aggregatId) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getVersjonAvInntektArbeidYtelseForGrunnlagId(aggregatId);
        return grunnlag.isPresent() ? Optional.of(grunnlag.get()) : Optional.empty();
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long behandlingId) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getAktivtInntektArbeidGrunnlag(behandlingId);
        return grunnlag.isPresent() ? Optional.of(grunnlag.get()) : Optional.empty();
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentFørsteVersjon(Long behandlingId) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getInitielVersjonInntektArbeidGrunnlagForBehandling(behandlingId);
        return grunnlag.orElseThrow(() -> InntektArbeidYtelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(behandlingId).toException());
    }

    @Override
    public DiffResult diffResultat(InntektArbeidYtelseGrunnlagEntitet grunnlag1, InntektArbeidYtelseGrunnlagEntitet grunnlag2, boolean onlyCheckTrackedFields) {
        return new RegisterdataDiffsjekker(onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderFor(Long behandlingId, VersjonType versjonType) {
        return opprettBuilderForBuilder(InntektArbeidYtelseGrunnlagBuilder.oppdatere(hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId)), versjonType);
    }

    @Override
    public ArbeidsforholdInformasjonBuilder opprettInformasjonBuilderFor(Long behandlingId) {
        return ArbeidsforholdInformasjonBuilder.oppdatere(InntektArbeidYtelseGrunnlagBuilder.oppdatere(hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId)).getInformasjon());
    }

    @Override
    public void lagre(Long behandlingId, InntektArbeidYtelseAggregatBuilder builder) {
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = getGrunnlagBuilder(behandlingId, builder);
        lagreOgFlush(behandlingId, opptjeningAggregatBuilder.build());
    }

    @Override
    public void lagre(Long behandlingId, OppgittOpptjeningBuilder oppgittOpptjening) {
        if (oppgittOpptjening == null) {
            return;
        }
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidAggregat = hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidAggregat);
        grunnlag.medOppgittOpptjening(oppgittOpptjening);

        lagreOgFlush(behandlingId, grunnlag.build());
    }

    @Override
    public void lagre(Long behandlingId, AktørId søkerAktørId, ArbeidsforholdInformasjonBuilder informasjon) {
        Objects.requireNonNull(informasjon, "informasjon"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(behandlingId);

        builder.ryddOppErstattedeArbeidsforhold(søkerAktørId, informasjon.getReverserteErstattArbeidsforhold());
        builder.ryddOppErstattedeArbeidsforhold(søkerAktørId, informasjon.getErstattArbeidsforhold());
        builder.medInformasjon(informasjon.build());

        lagreOgFlush(behandlingId, builder.build());
    }

    @Override
    public void lagre(Long behandlingId, Inntektsmelding inntektsmelding) {
        Objects.requireNonNull(inntektsmelding, "inntektsmelding"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(behandlingId);

        final ArbeidsforholdInformasjon informasjon = builder.getInformasjon();
        if (inntektsmelding.gjelderForEtSpesifiktArbeidsforhold()) {
            final ArbeidsforholdRef arbeidsforholdRef = informasjon
                .finnEllerOpprett(inntektsmelding.getArbeidsgiver(), inntektsmelding.getArbeidsforholdRef());
            ((InntektsmeldingEntitet) inntektsmelding).setArbeidsforholdId(arbeidsforholdRef);
        }

        final InntektsmeldingAggregatEntitet inntektsmeldinger = (InntektsmeldingAggregatEntitet) builder.getInntektsmeldinger();

        // Kommet inn inntektsmelding på arbeidsforhold som vi har gått videre med uten inntektsmelding?
        if (kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(inntektsmelding, informasjon)) {
            final ArbeidsforholdInformasjonBuilder informasjonBuilder = ArbeidsforholdInformasjonBuilder.oppdatere(informasjon);
            informasjonBuilder.fjernOverstyringVedrørende(inntektsmelding.getArbeidsgiver(), inntektsmelding.getArbeidsforholdRef());
            builder.medInformasjon(informasjonBuilder.build());
        }

        inntektsmeldinger.leggTil(inntektsmelding);
        builder.setInntektsmeldinger(inntektsmeldinger);

        lagreOgFlush(behandlingId, builder.build());
    }

    private boolean kommetInntektsmeldingPåArbeidsforholdHvorViTidligereBehandletUtenInntektsmelding(Inntektsmelding inntektsmelding,
                                                                                                     ArbeidsforholdInformasjon informasjon) {
        return informasjon.getOverstyringer()
            .stream()
            .anyMatch(ov -> (Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_UTEN_INNTEKTSMELDING)
                || Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.IKKE_BRUK)
                || Objects.equals(ov.getHandling(), ArbeidsforholdHandlingType.BRUK_MED_OVERSTYRT_PERIODE))
                && ov.getArbeidsgiver().getErVirksomhet()
                && ov.getArbeidsgiver().equals(inntektsmelding.getArbeidsgiver())
                && ov.getArbeidsforholdRef().gjelderFor(inntektsmelding.getArbeidsforholdRef()));
    }

    private InntektArbeidYtelseGrunnlagBuilder getGrunnlagBuilder(Long behandlingId, InntektArbeidYtelseAggregatBuilder builder) {
        Objects.requireNonNull(builder, "inntektArbeidYtelserBuilder"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = opprettGrunnlagBuilderFor(behandlingId);
        opptjeningAggregatBuilder.medData(builder);
        return opptjeningAggregatBuilder;
    }

    @Override
    public boolean erEndring(Kobling behandling, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        TraverseEntityGraph traverseEntityGraph = TraverseEntityGraphFactory.build(true);

        Long behandlingId = behandling.getId();
        final Optional<InntektArbeidYtelseGrunnlag> inntektArbeidYtelseGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId);

        InntektArbeidYtelseGrunnlagBuilder inntektArbeidYtelseGrunnlagBuilder = getGrunnlagBuilder(behandlingId, inntektArbeidYtelseAggregatBuilder);
        if (!inntektArbeidYtelseGrunnlag.isPresent() && inntektArbeidYtelseGrunnlagBuilder.erOppdatert()) {
            return false;
        }

        if (inntektArbeidYtelseGrunnlag.isPresent()) {
            final InntektArbeidYtelseGrunnlag gammelt = inntektArbeidYtelseGrunnlag.get();
            final DiffResult diff = new DiffEntity(traverseEntityGraph)
                .diff(gammelt, inntektArbeidYtelseGrunnlagBuilder.getKladd());
            return !diff.isEmpty();
        }

        return true;
    }

    @Override
    public boolean erEndringPåInntektsmelding(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå) {
        Optional<InntektsmeldingAggregat> eksisterendeInntektsmelding = før.getInntektsmeldinger();
        Optional<InntektsmeldingAggregat> nyInntektsmelding = nå.getInntektsmeldinger();

        Optional<Boolean> eksistenssjekkResultat = eksistenssjekkResultat(eksisterendeInntektsmelding, nyInntektsmelding);
        if (eksistenssjekkResultat.isPresent()) {
            return eksistenssjekkResultat.get();
        }

        InntektsmeldingAggregat eksisterende = eksisterendeInntektsmelding.get(); // NOSONAR - presens sjekket ovenfor
        InntektsmeldingAggregat ny = nyInntektsmelding.get(); // NOSONAR - presens sjekket ovenfor
        DiffResult diff = new RegisterdataDiffsjekker().getDiffEntity().diff(eksisterende, ny);
        return !diff.isEmpty();
    }

    @Override
    public boolean erEndringPåAktørArbeid(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, LocalDate skjæringstidspunkt) {
        Collection<AktørArbeid> eksisterendeAktørArbeid = før.getAktørArbeidFørStp(skjæringstidspunkt);
        Collection<AktørArbeid> nyAktørArbeid = nå.getAktørArbeidFørStp(skjæringstidspunkt);

        DiffResult diff = new RegisterdataDiffsjekker().getDiffEntity().diff(eksisterendeAktørArbeid, nyAktørArbeid);
        return !diff.isEmpty();
    }

    @Override
    public boolean erEndringPåAktørInntekt(InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, LocalDate skjæringstidspunkt) {
        Collection<AktørInntekt> eksisterendeAktørInntekt = før.getAktørInntektFørStp(skjæringstidspunkt);
        Collection<AktørInntekt> nyAktørInntekt = nå.getAktørInntektFørStp(skjæringstidspunkt);

        DiffResult diff = new RegisterdataDiffsjekker().getDiffEntity().diff(eksisterendeAktørInntekt, nyAktørInntekt);
        return !diff.isEmpty();
    }

    @Override
    public AktørYtelseEndring endringPåAktørYtelse(Saksnummer egetSaksnummer, InntektArbeidYtelseGrunnlag før, InntektArbeidYtelseGrunnlag nå, LocalDate skjæringstidspunkt) {
        // TODO (mglittum): Skrive om slik at vi ikke differ mot egen fagsak
        Predicate<Ytelse> predikatKildeFpsak = ytelse -> ytelse.getKilde().equals(Fagsystem.FPSAK) && !ytelse.getSaksnummer().equals(egetSaksnummer);
        Predicate<Ytelse> predikatKildeEksterneRegistre = ytelse -> !ytelse.getKilde().equals(Fagsystem.FPSAK);

        List<Ytelse> førYtelserFpsak = hentYtelser(før, skjæringstidspunkt, predikatKildeFpsak);
        List<Ytelse> nåYtelserFpsak = hentYtelser(nå, skjæringstidspunkt, predikatKildeFpsak);
        boolean ytelserFpsakEndret = !new RegisterdataDiffsjekker().getDiffEntity().diff(førYtelserFpsak, nåYtelserFpsak).isEmpty();

        List<Ytelse> førYtelserEkstern = hentYtelser(før, skjæringstidspunkt, predikatKildeEksterneRegistre);
        List<Ytelse> nåYtelserEkstern = hentYtelser(nå, skjæringstidspunkt, predikatKildeEksterneRegistre);
        boolean ytelserEksterneRegistreEndret = !new RegisterdataDiffsjekker().getDiffEntity().diff(førYtelserEkstern, nåYtelserEkstern).isEmpty();

        return new AktørYtelseEndring(ytelserFpsakEndret, ytelserEksterneRegistreEndret);
    }

    private List<Ytelse> hentYtelser(InntektArbeidYtelseGrunnlag ytelseGrunnlag, LocalDate skjæringstidspunkt, Predicate<Ytelse> predikatYtelseskilde) {
        return ytelseGrunnlag.getAktørYtelseFørStp(skjæringstidspunkt).stream()
            .flatMap(it -> it.getYtelser().stream())
            .filter(predikatYtelseskilde)
            .collect(Collectors.toList());
    }

    @Override
    public boolean erEndring(Kobling behandling, Kobling nyBehandling) {
        InntektArbeidYtelseGrunnlag nyttAggregat = hentInntektArbeidYtelseForBehandling(behandling.getId());
        InntektArbeidYtelseGrunnlag eksisterendeAggregat = hentInntektArbeidYtelseGrunnlagForBehandling(behandling.getId())
            .orElse(null);
        return erEndring(eksisterendeAggregat, nyttAggregat);
    }

    @Override
    public boolean erEndring(InntektArbeidYtelseGrunnlag aggregat, InntektArbeidYtelseGrunnlag nyttAggregat) {

        TraverseEntityGraph traverseEntityGraph = TraverseEntityGraphFactory.build(true);

        if (aggregat == null) {
            return true;
        }

        DiffResult diff = new DiffEntity(traverseEntityGraph)
            .diff(aggregat, nyttAggregat);
        return !diff.isEmpty();
    }

    @Override
    public boolean harArbeidsforholdMedArbeidstyperSomAngitt(Long behandlingId, AktørId aktørId, Set<ArbeidType> angitteArbeidtyper, LocalDate skjæringstidspunkt) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> inntektArbeidYtelseGrunnlag = getAktivtInntektArbeidGrunnlag(behandlingId);
        if (!inntektArbeidYtelseGrunnlag.isPresent()) {
            return false;
        }
        DatoIntervallEntitet stp = DatoIntervallEntitet.fraOgMedTilOgMed(skjæringstidspunkt, skjæringstidspunkt);
        Optional<AktørArbeid> aktørArbeid = inntektArbeidYtelseGrunnlag.get().getAktørArbeidFørStp(aktørId, skjæringstidspunkt);
        return aktørArbeid.filter(aktørArbeid1 -> !harIngenArbeidsforholdMedLøpendeAktivitetsavtale(aktørArbeid1.getYrkesaktiviteter(), stp)).isPresent();
    }

    @Override
    public void kopierGrunnlagFraEksisterendeBehandling(Long fraBehandlingId, Long tilBehandlingId) {
        Optional<InntektArbeidYtelseGrunnlag> origAggregat = hentInntektArbeidYtelseGrunnlagForBehandling(fraBehandlingId);
        origAggregat.ifPresent(orig -> {
            InntektArbeidYtelseGrunnlagEntitet entitet = new InntektArbeidYtelseGrunnlagEntitet(orig);
            lagreOgFlush(tilBehandlingId, entitet);
        });
    }

    private boolean harIngenArbeidsforholdMedLøpendeAktivitetsavtale(Collection<Yrkesaktivitet> yrkesaktiviteter, DatoIntervallEntitet skjæringstidspunkt) {
        return yrkesaktiviteter.stream()
            .filter(Yrkesaktivitet::erArbeidsforhold)
            .map(Yrkesaktivitet::getAnsettelsesPerioder)
            .flatMap(Collection::stream)
            .noneMatch(aa -> aa.getErLøpende() || aa.getPeriode().overlapper(skjæringstidspunkt));
    }

    private InntektArbeidYtelseGrunnlagBuilder opprettGrunnlagBuilderFor(Long behandlingId) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidAggregat = hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId);
        return InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidAggregat);
    }

    private void lagreOgFlush(Long behandlingId, InntektArbeidYtelseGrunnlag nyttGrunnlag) {
        Objects.requireNonNull(behandlingId, BEH_NULL);

        if (nyttGrunnlag == null) {
            return;
        }

        Optional<InntektArbeidYtelseGrunnlagEntitet> tidligereAggregat = getAktivtInntektArbeidGrunnlag(behandlingId);

        if (tidligereAggregat.isPresent()) {
            InntektArbeidYtelseGrunnlagEntitet aggregat = tidligereAggregat.get();
            if (diffResultat(aggregat, (InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag, false).isEmpty()) {
                return;
            }
            aggregat.setAktivt(false);
            entityManager.persist(aggregat);
            entityManager.flush();

            lagreGrunnlag(nyttGrunnlag, behandlingId);
        } else {
            lagreGrunnlag(nyttGrunnlag, behandlingId);
        }
        entityManager.flush();
    }

    private void lagreGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, Long behandlingId) {
        ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).setBehandling(behandlingId);
        ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).setReferanse(UUID.randomUUID());

        nyttGrunnlag.getOppgittOpptjening().ifPresent(this::lagreOppgittOpptjening);

        final Optional<InntektArbeidYtelseAggregat> registerVersjon = ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).getRegisterVersjon();
        registerVersjon.ifPresent(this::lagreInntektArbeid);

        final Optional<InntektArbeidYtelseAggregat> saksbehandletFørVersjon = nyttGrunnlag.getSaksbehandletVersjon();
        saksbehandletFørVersjon.ifPresent(this::lagreInntektArbeid);

        nyttGrunnlag.getInntektsmeldinger().ifPresent(this::lagreInntektsMeldinger);

        ((InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag).getInformasjon().ifPresent(this::lagreInformasjon);
        entityManager.persist(nyttGrunnlag);
    }

    private void lagreInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        final ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjonEntitet = (ArbeidsforholdInformasjonEntitet) arbeidsforholdInformasjon; //NOSONAR
        entityManager.persist(arbeidsforholdInformasjonEntitet);
        for (ArbeidsforholdReferanseEntitet referanseEntitet : arbeidsforholdInformasjonEntitet.getReferanser()) {
            entityManager.persist(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringEntitet overstyringEntitet : arbeidsforholdInformasjonEntitet.getOverstyringer()) {
            entityManager.persist(overstyringEntitet);
        }
    }


    private void lagreOppgittOpptjening(OppgittOpptjening entitet) {
        entityManager.persist(entitet);

        for (AnnenAktivitet aktivitet : entitet.getAnnenAktivitet()) {
            entityManager.persist(aktivitet);
        }

        for (EgenNæring næring : entitet.getEgenNæring()) {
            entityManager.persist(næring);
        }

        entitet.getFrilans().ifPresent(frilans -> {
            entityManager.persist(frilans);
            for (Frilansoppdrag frilansoppdrag : frilans.getFrilansoppdrag()) {
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
        for (Inntektsmelding entitet : inntektsmeldingAggregat.getInntektsmeldinger()) {
            entityManager.persist(entitet);
            for (Gradering gradering : entitet.getGraderinger()) {
                entityManager.persist(gradering);
            }

            for (NaturalYtelse naturalYtelse : entitet.getNaturalYtelser()) {
                entityManager.persist(naturalYtelse);
            }

            for (UtsettelsePeriode utsettelsePeriode : entitet.getUtsettelsePerioder()) {
                entityManager.persist(utsettelsePeriode);
            }

            for (Refusjon refusjon : entitet.getEndringerRefusjon()) {
                entityManager.persist(refusjon);
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
            for (Inntektspost inntektspost : inntekt.getInntektspost()) {
                entityManager.persist(inntektspost);
            }
        }
    }

    private void lagreAktørArbeid(AktørArbeid aktørArbeid) {
        for (Yrkesaktivitet yrkesaktivitet : ((AktørArbeidEntitet) aktørArbeid).hentAlleYrkesaktiviter()) {
            entityManager.persist(yrkesaktivitet);
            for (AktivitetsAvtale aktivitetsAvtale : ((YrkesaktivitetEntitet) yrkesaktivitet).getAlleAktivitetsAvtaler()) {
                entityManager.persist(aktivitetsAvtale);
            }
            for (Permisjon permisjon : yrkesaktivitet.getPermisjon()) {
                entityManager.persist(permisjon);
            }
        }
    }

    private void lagreAktørYtelse(AktørYtelse aktørYtelse) {
        for (Ytelse ytelse : aktørYtelse.getYtelser()) {
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

    private InntektArbeidYtelseAggregatBuilder opprettBuilderFor(Optional<InntektArbeidYtelseGrunnlag> aggregat, VersjonType versjon) {
        Objects.requireNonNull(aggregat, "aggregat"); // NOSONAR $NON-NLS-1$
        if (aggregat.isPresent()) {
            final InntektArbeidYtelseGrunnlag aggregat1 = aggregat.get();
            return InntektArbeidYtelseAggregatBuilder.oppdatere(hentRiktigVersjon(versjon, aggregat1), versjon);
        }
        throw InntektArbeidYtelseFeil.FACTORY.aggregatKanIkkeVæreNull().toException();
    }

    private Optional<InntektArbeidYtelseAggregat> hentRiktigVersjon(VersjonType versjonType, InntektArbeidYtelseGrunnlag aggregat) {
        if (versjonType == VersjonType.REGISTER) {
            return ((InntektArbeidYtelseGrunnlagEntitet) aggregat).getRegisterVersjon();
        } else if (versjonType == VersjonType.SAKSBEHANDLET) {
            return aggregat.getSaksbehandletVersjon();
        }
        throw new IllegalStateException("Kunne ikke finne riktig versjon av InntektArbeidYtelseGrunnlag");
    }

    private InntektArbeidYtelseAggregatBuilder opprettBuilderForBuilder(InntektArbeidYtelseGrunnlagBuilder aggregatBuilder, VersjonType versjonType) {
        Objects.requireNonNull(aggregatBuilder, "aggregatBuilder"); // NOSONAR $NON-NLS-1$
        return opprettBuilderFor(Optional.ofNullable(aggregatBuilder.getKladd()), versjonType);

    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getAktivtInntektArbeidGrunnlag(Long behandlingId) {
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " +  // NOSONAR
            "WHERE gr.behandlingId = :behandlingId " + //$NON-NLS-1$ //NOSONAR
            "AND gr.aktiv = :aktivt", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("behandlingId", behandlingId); // NOSONAR $NON-NLS-1$
        query.setParameter("aktivt", true);
        final Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = HibernateVerktøy.hentUniktResultat(query);
        grunnlag.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        return grunnlag;
    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getInitielVersjonInntektArbeidGrunnlagForBehandling(Long behandlingId) {
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " + // NOSONAR $NON-NLS-1$
            "WHERE gr.behandlingId = :behandlingId " + //$NON-NLS-1$ //NOSONAR
            "order by gr.opprettetTidspunkt, gr.id", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("behandlingId", behandlingId); // NOSONAR $NON-NLS-1$

        final Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = query.getResultList().stream().findFirst();
        grunnlag.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        return grunnlag;
    }

    @Override
    public Optional<Long> hentIdPåAktivInntektArbeidYtelseForBehandling(Long behandlingId) {
        return getAktivtInntektArbeidGrunnlag(behandlingId)
            .map(InntektArbeidYtelseGrunnlagEntitet::getId);
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForGrunnlagId(Long grunnlagId) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> optGrunnlag = getVersjonAvInntektArbeidYtelseForGrunnlagId(grunnlagId);
        return optGrunnlag.isPresent() ? optGrunnlag.get() : null;
    }


    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForGrunnlagId(Long inntektArbeidYtelseGrunnlagId) {
        final Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentAggregatPåIdHvisEksisterer(inntektArbeidYtelseGrunnlagId);
        return hentArbeidsforholdInformasjon(grunnlag);
    }

    private Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        if (grunnlag.isPresent()) {
            final Optional<InntektArbeidYtelseGrunnlagEntitet> inntektArbeidYtelseGrunnlag = Optional.of((InntektArbeidYtelseGrunnlagEntitet) grunnlag.get());
            return inntektArbeidYtelseGrunnlag.flatMap(InntektArbeidYtelseGrunnlagEntitet::getInformasjon);
        }
        return Optional.empty();
    }


    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(Long behandlingId) {
        final Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(behandlingId);
        return hentArbeidsforholdInformasjon(grunnlag);
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentForrigeVersjonAvInntektsmeldingForBehandling(Long behandlingId) {
        Objects.requireNonNull(behandlingId, "behandlingId"); // NOSONAR $NON-NLS-1$

        Query query = entityManager.createQuery(
            "SELECT gr FROM InntektArbeidGrunnlag gr " + // NOSONAR $NON-NLS-1$
                "WHERE gr.behandlingId = :behandlingId " + //$NON-NLS-1$ //NOSONAR
                "AND gr.aktiv = false " +
                "AND gr.inntektsmeldinger.id = (SELECT DISTINCT MAX(gr2.inntektsmeldinger.id) " +
                "FROM InntektArbeidGrunnlag gr2 " +
                "WHERE gr2.behandlingId = :behandlingId " + //$NON-NLS-1$ //NOSONAR
                "AND gr2.aktiv = false " +
                "and gr2.inntektsmeldinger.id <> (SELECT DISTINCT MAX(gr3.inntektsmeldinger.id) " +
                "                FROM InntektArbeidGrunnlag gr3 " +
                "                WHERE gr3.behandlingId = :behandlingId)) " + //$NON-NLS-1$ //NOSONAR
                "ORDER BY gr.opprettetTidspunkt DESC"
            , InntektArbeidYtelseGrunnlag.class);

        query.setParameter("behandlingId", behandlingId); // NOSONAR $NON-NLS-1$

        if (query.getResultList().isEmpty()) {
            return Optional.empty();
        }
        InntektArbeidYtelseGrunnlagEntitet resultat = (InntektArbeidYtelseGrunnlagEntitet) query.getResultList().get(0);
        resultat.taHensynTilBetraktninger();
        return Optional.of(resultat);
    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getVersjonAvInntektArbeidYtelseForGrunnlagId(Long grunnlagId) {
        Objects.requireNonNull(grunnlagId, "aggregatId"); // NOSONAR $NON-NLS-1$
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " + // NOSONAR $NON-NLS-1$
            "WHERE gr.id = :aggregatId ", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("aggregatId", grunnlagId);
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlagOpt = query.getResultList().stream().findFirst();
        grunnlagOpt.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        return grunnlagOpt;
    }
}
