package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.felles.diff.DiffEntity;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseEntityGraph;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class InntektArbeidYtelseRepositoryImpl implements InntektArbeidYtelseRepository {
    private static final Logger log = LoggerFactory.getLogger(InntektArbeidYtelseRepositoryImpl.class);
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
    public InntektArbeidYtelseGrunnlag hentInntektArbeidYtelseForBehandling(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getAktivtInntektArbeidGrunnlag(koblingReferanse);
        return grunnlag.orElseThrow(() -> InntektArbeidYtelseFeil.FACTORY.fantIkkeForventetGrunnlagPåBehandling(koblingReferanse).toException());
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getAktivtInntektArbeidGrunnlag(koblingReferanse);
        return grunnlag.map(InntektArbeidYtelseGrunnlag.class::cast);
    }

    @Override
    public DiffResult diffResultat(InntektArbeidYtelseGrunnlag grunnlag1, InntektArbeidYtelseGrunnlag grunnlag2, boolean onlyCheckTrackedFields) {
        return new RegisterdataDiffsjekker(onlyCheckTrackedFields).getDiffEntity().diff(grunnlag1, grunnlag2);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderFor(KoblingReferanse koblingReferanse, UUID angittAggregatReferanse, LocalDateTime angittOpprettetTidspunkt, VersjonType versjonType) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return opprettBuilderFor(versjonType, angittAggregatReferanse, angittOpprettetTidspunkt, grunnlag);
    }

    private InntektArbeidYtelseAggregatBuilder opprettBuilderFor(VersjonType versjonType, UUID angittReferanse, LocalDateTime opprettetTidspunkt, Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        InntektArbeidYtelseGrunnlagBuilder grunnlagBuilder = InntektArbeidYtelseGrunnlagBuilder.oppdatere(grunnlag);
        Objects.requireNonNull(grunnlagBuilder, "grunnlagBuilder");
        Optional<InntektArbeidYtelseGrunnlag> aggregat = Optional.ofNullable(grunnlagBuilder.getKladd()); // NOSONAR $NON-NLS-1$
        Objects.requireNonNull(aggregat, "aggregat"); // NOSONAR $NON-NLS-1$
        if (aggregat.isPresent()) {
            final InntektArbeidYtelseGrunnlag aggregat1 = aggregat.get();
            return InntektArbeidYtelseAggregatBuilder.builderFor(hentRiktigVersjon(versjonType, aggregat1), angittReferanse, opprettetTidspunkt, versjonType);
        }
        throw InntektArbeidYtelseFeil.FACTORY.aggregatKanIkkeVæreNull().toException();
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder builder) {
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = getGrunnlagBuilder(koblingReferanse, builder);
        lagreOgFlush(koblingReferanse, opptjeningAggregatBuilder.build());
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder oppgittOpptjening) {
        if (oppgittOpptjening == null) {
            return;
        }
        Optional<InntektArbeidYtelseGrunnlag> iayGrunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);

        InntektArbeidYtelseGrunnlagBuilder grunnlag = InntektArbeidYtelseGrunnlagBuilder.oppdatere(iayGrunnlag);
        grunnlag.medOppgittOpptjening(oppgittOpptjening);

        lagreOgFlush(koblingReferanse, grunnlag.build());
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, AktørId søkerAktørId, ArbeidsforholdInformasjonBuilder informasjon) {
        Objects.requireNonNull(informasjon, "informasjon"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingReferanse);

        builder.ryddOppErstattedeArbeidsforhold(søkerAktørId, informasjon.getReverserteErstattArbeidsforhold());
        builder.ryddOppErstattedeArbeidsforhold(søkerAktørId, informasjon.getErstattArbeidsforhold());
        builder.medInformasjon(informasjon.build());

        lagreOgFlush(koblingReferanse, builder.build());
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, Inntektsmelding inntektsmelding) {
        Objects.requireNonNull(inntektsmelding, "inntektsmelding"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder builder = opprettGrunnlagBuilderFor(koblingReferanse);

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

        lagreOgFlush(koblingReferanse, builder.build());
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

    private InntektArbeidYtelseGrunnlagBuilder getGrunnlagBuilder(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder builder) {
        Objects.requireNonNull(builder, "inntektArbeidYtelserBuilder"); // NOSONAR
        InntektArbeidYtelseGrunnlagBuilder opptjeningAggregatBuilder = opprettGrunnlagBuilderFor(koblingReferanse);
        opptjeningAggregatBuilder.medData(builder);
        return opptjeningAggregatBuilder;
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

    private InntektArbeidYtelseGrunnlagBuilder opprettGrunnlagBuilderFor(KoblingReferanse koblingReferanse) {
        Optional<InntektArbeidYtelseGrunnlag> inntektArbeidAggregat = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return InntektArbeidYtelseGrunnlagBuilder.oppdatere(inntektArbeidAggregat);
    }

    private void lagreOgFlush(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlag nyttGrunnlag) {
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");

        if (nyttGrunnlag == null) {
            return;
        }

        Optional<InntektArbeidYtelseGrunnlagEntitet> tidligereAggregat = getAktivtInntektArbeidGrunnlag(koblingReferanse);

        if (tidligereAggregat.isPresent()) {
            InntektArbeidYtelseGrunnlagEntitet aggregat = tidligereAggregat.get();
            if (diffResultat(aggregat, nyttGrunnlag, false).isEmpty()) {
                return;
            }
            aggregat.setAktivt(false);
            entityManager.persist(aggregat);
            entityManager.flush();

            lagreGrunnlag(nyttGrunnlag, koblingReferanse);
        } else {
            lagreGrunnlag(nyttGrunnlag, koblingReferanse);
        }
        entityManager.flush();
    }

    @Override
    public void lagreMigrertGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, KoblingReferanse koblingReferanse, boolean aktiv) {
        InntektArbeidYtelseGrunnlagEntitet entitet = (InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag;
        entitet.setAktivt(aktiv);
        // Sjekker om grunnlaget finnes fra før. Hvis tilfelle så slettes dette.
        hentInntektArbeidYtelseForReferanse(nyttGrunnlag.getGrunnlagReferanse()).map(InntektArbeidYtelseGrunnlagEntitet.class::cast).ifPresent(this::slettGrunnlag);

        lagreGrunnlag(entitet, koblingReferanse);
        entityManager.flush();
    }

    @Deprecated(forRemoval = true)
    private void slettGrunnlag(InntektArbeidYtelseGrunnlagEntitet grunnlag) {
        log.info("[MIGRERING] Mottatt nytt grunnlag med samme referanse. Sletter grunnlag med grunnlagsref={}", grunnlag.getGrunnlagReferanse());
        grunnlag.getRegisterVersjon().ifPresent(this::slettAggregat);
        grunnlag.getSaksbehandletVersjon().ifPresent(this::slettAggregat);
        grunnlag.getInntektsmeldinger().ifPresent(this::slettInntektsmeldinger);
        grunnlag.getArbeidsforholdInformasjon().ifPresent(this::slettInformasjon);
        grunnlag.getOppgittOpptjening().ifPresent(this::slettOppgittOpptjening);

        entityManager.remove(grunnlag);
        entityManager.flush();
    }

    @Deprecated(forRemoval = true)
    private void slettInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        arbeidsforholdInformasjon.getOverstyringer().forEach(ov -> {
            ov.getArbeidsforholdOverstyrtePerioder().forEach(entityManager::remove);
            entityManager.remove(ov);
        });
        arbeidsforholdInformasjon.getArbeidsforholdReferanser().forEach(entityManager::remove);

        entityManager.remove(arbeidsforholdInformasjon);
    }

    @Deprecated(forRemoval = true)
    private void slettOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        oppgittOpptjening.getAnnenAktivitet().forEach(entityManager::remove);
        oppgittOpptjening.getEgenNæring().forEach(entityManager::remove);
        oppgittOpptjening.getOppgittArbeidsforhold().forEach(entityManager::remove);
        oppgittOpptjening.getFrilans().ifPresent(it -> {
            it.getFrilansoppdrag().forEach(entityManager::remove);
            entityManager.remove(it);
        });

        entityManager.remove(oppgittOpptjening);
    }

    @Deprecated(forRemoval = true)
    private void slettInntektsmeldinger(InntektsmeldingAggregat inntektsmeldinger) {
        inntektsmeldinger.getAlleInntektsmeldinger().forEach(im -> {
            im.getEndringerRefusjon().forEach(entityManager::remove);
            im.getGraderinger().forEach(entityManager::remove);
            im.getNaturalYtelser().forEach(entityManager::remove);
            im.getUtsettelsePerioder().forEach(entityManager::remove);

            entityManager.remove(im);
        });

        entityManager.remove(inntektsmeldinger);
    }

    @Deprecated(forRemoval = true)
    private void slettAggregat(InntektArbeidYtelseAggregat aggregat) {
        aggregat.getAktørArbeid()
            .forEach(aa -> ((AktørArbeidEntitet) aa).hentAlleYrkesaktiviter()
                .forEach(yr -> {
                    yr.getAlleAktivitetsAvtaler()
                        .forEach(entityManager::remove);
                    yr.getPermisjon()
                        .forEach(entityManager::remove);
                    entityManager.remove(yr);
                }));
        aggregat.getAktørInntekt()
            .forEach(aa -> ((AktørInntektEntitet) aa).getInntekt()
                .forEach(yr -> {
                    yr.getInntektspost()
                        .forEach(entityManager::remove);
                    entityManager.remove(yr);
                }));
        aggregat.getAktørYtelse()
            .forEach(aa -> aa.getYtelser()
                .forEach(yr -> {
                    yr.getYtelseGrunnlag().ifPresent(entityManager::remove);
                    yr.getYtelseAnvist().forEach(entityManager::remove);
                    entityManager.remove(yr);
                }));
        entityManager.remove(aggregat);
    }

    private void lagreGrunnlag(InntektArbeidYtelseGrunnlag nyttGrunnlag, KoblingReferanse koblingReferanse) {
        InntektArbeidYtelseGrunnlagEntitet entitet = (InntektArbeidYtelseGrunnlagEntitet) nyttGrunnlag;
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

        nyttGrunnlag.getInntektsmeldinger().ifPresent(this::lagreInntektsMeldinger);

        entitet.getArbeidsforholdInformasjon().ifPresent(this::lagreInformasjon);
        entityManager.persist(nyttGrunnlag);
    }

    private void lagreInformasjon(ArbeidsforholdInformasjon arbeidsforholdInformasjon) {
        final ArbeidsforholdInformasjonEntitet arbeidsforholdInformasjonEntitet = (ArbeidsforholdInformasjonEntitet) arbeidsforholdInformasjon; // NOSONAR
        entityManager.persist(arbeidsforholdInformasjonEntitet);
        for (ArbeidsforholdReferanseEntitet referanseEntitet : arbeidsforholdInformasjonEntitet.getArbeidsforholdReferanser()) {
            entityManager.persist(referanseEntitet);
        }
        for (ArbeidsforholdOverstyringEntitet overstyringEntitet : arbeidsforholdInformasjonEntitet.getOverstyringer()) {
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

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getAktivtInntektArbeidGrunnlag(KoblingReferanse koblingReferanse) {
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr JOIN KOBLING k " + // NOSONAR
            "WHERE k.koblingReferanse = :ref " + //$NON-NLS-1$ //NOSONAR
            "AND gr.aktiv = :aktivt", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("ref", koblingReferanse); // NOSONAR $NON-NLS-1$
        query.setParameter("aktivt", true);
        final Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = HibernateVerktøy.hentUniktResultat(query);
        grunnlag.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        return grunnlag;
    }

    private Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjon(Optional<InntektArbeidYtelseGrunnlag> grunnlag) {
        if (grunnlag.isPresent()) {
            final Optional<InntektArbeidYtelseGrunnlagEntitet> inntektArbeidYtelseGrunnlag = Optional.of((InntektArbeidYtelseGrunnlagEntitet) grunnlag.get());
            return inntektArbeidYtelseGrunnlag.flatMap(InntektArbeidYtelseGrunnlagEntitet::getArbeidsforholdInformasjon);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ArbeidsforholdInformasjon> hentArbeidsforholdInformasjonForBehandling(KoblingReferanse koblingReferanse) {
        final Optional<InntektArbeidYtelseGrunnlag> grunnlag = hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
        return hentArbeidsforholdInformasjon(grunnlag);
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseForReferanse(GrunnlagReferanse grunnlagReferanse) {
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlag = getVersjonAvInntektArbeidYtelseForReferanseId(grunnlagReferanse);
        return grunnlag.map(InntektArbeidYtelseGrunnlag.class::cast);
    }

    @Override
    public Long hentKoblingIdFor(GrunnlagReferanse grunnlagReferanse) {
        final TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k JOIN InntektArbeidGrunnlag gr WHERE gr.grunnlagReferanse=:ref", Long.class);
        query.setParameter("ref", grunnlagReferanse);
        return query.getSingleResult();
    }

    private Optional<InntektArbeidYtelseGrunnlagEntitet> getVersjonAvInntektArbeidYtelseForReferanseId(GrunnlagReferanse grunnlagReferanse) {
        Objects.requireNonNull(grunnlagReferanse, "aggregatId"); // NOSONAR $NON-NLS-1$
        final TypedQuery<InntektArbeidYtelseGrunnlagEntitet> query = entityManager.createQuery("FROM InntektArbeidGrunnlag gr " +
            "WHERE gr.grunnlagReferanse = :ref ", InntektArbeidYtelseGrunnlagEntitet.class);
        query.setParameter("ref", grunnlagReferanse);
        Optional<InntektArbeidYtelseGrunnlagEntitet> grunnlagOpt = query.getResultStream().findFirst();
        grunnlagOpt.ifPresent(InntektArbeidYtelseGrunnlagEntitet::taHensynTilBetraktninger);
        return grunnlagOpt;
    }

    @Override
    public KoblingReferanse hentKoblingReferanseFor(GrunnlagReferanse grunnlagReferanse) {
        final TypedQuery<KoblingReferanse> query = entityManager.createQuery("SELECT k.koblingReferanse FROM Kobling k JOIN InntektArbeidGrunnlag gr WHERE gr.grunnlagReferanse=:ref", KoblingReferanse.class);
        query.setParameter("ref", grunnlagReferanse);
        return query.getSingleResult();
    }


    private Long hentKoblingIdFor(KoblingReferanse koblingReferanse) {
        final TypedQuery<Long> query = entityManager.createQuery("SELECT k.id FROM Kobling k k.koblingReferanse=:ref", Long.class);
        query.setParameter("ref", koblingReferanse);
        return query.getSingleResult();
    }
}
