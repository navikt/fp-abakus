package no.nav.foreldrepenger.abakus.iay.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

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
    public InntektArbeidYtelseGrunnlag hentAggregat(KoblingReferanse koblingReferanse) {
        return repository.hentInntektArbeidYtelseForBehandling(koblingReferanse);
    }

    @Override
    public InntektArbeidYtelseGrunnlag hentAggregat(GrunnlagReferanse referanse) {
        return repository.hentInntektArbeidYtelseForReferanse(referanse).orElseThrow();
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
    public List<InntektArbeidYtelseGrunnlag> hentAlleGrunnlagFor(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType, boolean kunAktive) {
        return repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, saksnummer, ytelseType, kunAktive);
    }
    
    @Override
    public List<InntektArbeidYtelseGrunnlag> hentAlleGrunnlagFor(AktørId aktørId, KoblingReferanse koblingReferanse, boolean kunAktive) {
        return repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, koblingReferanse, kunAktive);
    }

    @Override
    public Optional<InntektArbeidYtelseGrunnlag> hentGrunnlagFor(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentInntektArbeidYtelseForReferanse(grunnlagReferanse);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForRegister(KoblingReferanse koblingReferanse, UUID angittReferanse,
                                                                        LocalDateTime angittOpprettetTidspunkt) {
        return repository.opprettBuilderFor(koblingReferanse, angittReferanse, angittOpprettetTidspunkt, VersjonType.REGISTER);
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder opprettBuilderForSaksbehandlet(KoblingReferanse koblingReferanse, UUID angittReferanse,
                                                                             LocalDateTime angittOpprettetTidspunkt) {
        return repository.opprettBuilderFor(koblingReferanse, angittReferanse, angittOpprettetTidspunkt, VersjonType.SAKSBEHANDLET);
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
    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjonEntitet::new);
    }

    @Override
    public Optional<OppgittOpptjeningEntitet> hentOppgittOpptjeningFor(KoblingReferanse koblingReferanse, UUID oppgittOpptjeningEksternReferanse) {
        return repository.hentOppgittOpptjeningFor(koblingReferanse, oppgittOpptjeningEksternReferanse);
    }

    @Override
    public Optional<InntektArbeidYtelseAggregatEntitet> hentIAYAggregatFor(KoblingReferanse koblingReferanse, UUID eksternReferanse) {
        return repository.hentIAYAggregatFor(koblingReferanse, eksternReferanse);
    }

    @Override
    public KoblingReferanse hentKoblingReferanse(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentKoblingReferanseFor(grunnlagReferanse);
    }
}
