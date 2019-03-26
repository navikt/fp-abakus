package no.nav.foreldrepenger.abakus.iay.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
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
        return repository.hentInntektArbeidYtelseForBehandling(koblingen.getId());
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(UUID referanse) {
        return repository.hentInntektArbeidYtelseForReferanse(referanse);
    }

    @Override
    public Long hentKoblingIdFor(UUID referanse) {
        return repository.hentKoblingForReferanse(referanse);
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentInntektArbeidYtelseGrunnlagForBehandling(Long koblingId) {
        return repository.hentInntektArbeidYtelseGrunnlagForBehandling(koblingId);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(Long koblingId) {
        return repository.opprettBuilderFor(koblingId, VersjonType.REGISTER);
    }

    @Override
    public void lagre(Long koblingId, InntektArbeidYtelseAggregatBuilder inntektArbeidYtelseAggregatBuilder) {
        repository.lagre(koblingId, inntektArbeidYtelseAggregatBuilder);
    }

    @Override
    public void lagre(Long koblingId, AktørId aktørId, ArbeidsforholdInformasjonBuilder builder) {
        repository.lagre(koblingId, aktørId, builder);
    }

    @Override
    public ArbeidsforholdRef finnReferanseFor(Long behandlingId, Arbeidsgiver arbeidsgiver, ArbeidsforholdRef arbeidsforholdRef, boolean beholdErstattetVerdi) {
        final Optional<ArbeidsforholdInformasjon> arbeidsforholdInformasjon = repository.hentArbeidsforholdInformasjonForBehandling(behandlingId);
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
    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(Long koblingId) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingId).orElseGet(ArbeidsforholdInformasjonEntitet::new);
    }
}
