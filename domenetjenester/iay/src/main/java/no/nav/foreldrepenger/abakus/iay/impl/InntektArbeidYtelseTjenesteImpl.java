package no.nav.foreldrepenger.abakus.iay.impl;

import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;

@ApplicationScoped
public class InntektArbeidYtelseTjenesteImpl implements InntektArbeidYtelseTjeneste {

    private InntektArbeidYtelseRepository repository;

    InntektArbeidYtelseTjenesteImpl() {
        // CDI
    }

    @Inject
    public InntektArbeidYtelseTjenesteImpl(InntektArbeidYtelseRepository repository) {
        Objects.requireNonNull(repository, "repository");
        this.repository = repository;
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(Kobling koblingen) {
        return repository.hentInntektArbeidYtelseForBehandling(koblingen.getKoblingReferanse());
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(GrunnlagReferanse referanse) {
        return repository.hentInntektArbeidYtelseForReferanse(referanse);
    }

    @Override
    public Long hentKoblingIdFor(GrunnlagReferanse referanse) {
        return repository.hentKoblingIdFor(referanse);
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentGrunnlagFor(KoblingReferanse koblingReferanse) {
        return repository.hentInntektArbeidYtelseGrunnlagForBehandling(koblingReferanse);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(KoblingReferanse koblingReferanse) {
        return repository.opprettBuilderFor(koblingReferanse, VersjonType.REGISTER);
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        repository.lagre(koblingReferanse, inntektArbeidYtelseAggregatBuilder);
    }

    @Override
    public void lagre(KoblingReferanse koblingReferanse, AktørId aktørId, ArbeidsforholdInformasjonBuilder builder) {
        repository.lagre(koblingReferanse, aktørId, builder);
    }
    
    @Override
    public ArbeidsforholdRef finnReferanseFor(KoblingReferanse koblingReferanse, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef,
                                              boolean beholdErstattetVerdi) {
        final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse);
        if (arbeidsforholdInformasjon.isPresent()) {
            final ArbeidsforholdInformasjon informasjon = arbeidsforholdInformasjon.get();
            if (beholdErstattetVerdi) {
                return informasjon.finnForEksternBeholdHistoriskReferanse(arbeidsgiver, arbeidsforholdRef);
            }
            return informasjon.finnForEkstern(arbeidsgiver, arbeidsforholdRef);
        }
        return arbeidsforholdRef;
    }

    @Override
    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjonEntitet::new);
    }

    @Override
    public KoblingReferanse hentKoblingReferanse(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentKoblingReferanseFor(grunnlagReferanse);
    }
}
