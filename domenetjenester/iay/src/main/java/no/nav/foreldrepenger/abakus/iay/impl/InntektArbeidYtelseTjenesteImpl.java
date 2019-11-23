package no.nav.foreldrepenger.abakus.iay.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlagBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest.GrunnlagVersjon;

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
    public List<InntektArbeidYtelseGrunnlag> hentGrunnlagEtterspurtFor(AktørId aktørId,
                                                                       Saksnummer saksnummer,
                                                                       YtelseType ytelseType,
                                                                       GrunnlagVersjon grunnlagVersjon) {

        boolean kunAktive = GrunnlagVersjon.SISTE.equals(grunnlagVersjon); // shortcutter litt opphenting
        var grunnlag = hentAlleGrunnlagFor(aktørId, saksnummer, ytelseType, kunAktive);

        var grunnlagByKobling = grunnlag.stream()
            .collect(Collectors.groupingBy(InntektArbeidYtelseGrunnlag::getKoblingId));

        var grunnlagEtterspurt = grunnlagByKobling.entrySet().stream()
            .flatMap(e -> filterGrunnlag(e.getKey(), e.getValue(), grunnlagVersjon).stream());

        return grunnlagEtterspurt.collect(Collectors.toList());
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
    public void lagre(KoblingReferanse koblingReferanse, InntektArbeidYtelseGrunnlagBuilder builder) {
        repository.lagre(koblingReferanse, builder);
    }

    @Override
    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjonEntitet::new);
    }

    @Override
    public Optional<OppgittOpptjeningEntitet> hentOppgittOpptjeningFor(UUID oppgittOpptjeningEksternReferanse) {
        return repository.hentOppgittOpptjeningFor(oppgittOpptjeningEksternReferanse);
    }

    @Override
    public Optional<InntektArbeidYtelseAggregatEntitet> hentIAYAggregatFor(UUID eksternReferanse) {
        return repository.hentIAYAggregatFor(eksternReferanse);
    }

    @Override
    public boolean erGrunnlagAktivt(UUID eksternReferanse) {
        return repository.erGrunnlagAktivt(eksternReferanse);
    }

    @Override
    public void slettAltForSak(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        repository.slettAltForSak(aktørId, saksnummer, ytelseType);
    }

    @Override
    public void kopierGrunnlagFraKoblingTilKobling(KoblingReferanse fraKobling, KoblingReferanse tilKobling) {
        repository.kopierGrunnlagFraEksisterendeBehandling(fraKobling, tilKobling);
    }

    @Override
    public KoblingReferanse hentKoblingReferanse(GrunnlagReferanse grunnlagReferanse) {
        return repository.hentKoblingReferanseFor(grunnlagReferanse);
    }
    

    private List<InntektArbeidYtelseGrunnlag> filterGrunnlag(Long koblingId, List<InntektArbeidYtelseGrunnlag> grunnlagPerKobling,
                                                             GrunnlagVersjon grunnlagVersjon) {
        if (!grunnlagPerKobling.stream().allMatch(g -> Objects.equals(koblingId, g.getKoblingId()))) {
            throw new IllegalArgumentException("Utvikler-feil: Fikk grunnlag som ikke har riktig koblingId: " + koblingId);
        }

        // quick returns
        if (GrunnlagVersjon.ALLE.equals(grunnlagVersjon) || grunnlagPerKobling.isEmpty()) {
            return grunnlagPerKobling;
        }

        var sortertKopi = grunnlagPerKobling.stream()
            .sorted(Comparator.comparing(InntektArbeidYtelseGrunnlag::getOpprettetTidspunkt, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(Collectors.toCollection(LinkedList::new));

        InntektArbeidYtelseGrunnlag første = sortertKopi.getFirst(); // vil alltid være her da vi sjekker på tom liste først
        InntektArbeidYtelseGrunnlag siste = sortertKopi.getLast();

        // dobbeltsjekk siste skal være aktivt
        if (!siste.isAktiv()) {
            throw new IllegalStateException("Siste grunnlag på " + koblingId + " er ikke aktivt, grunnlagReferanse: " + siste.getGrunnlagReferanse());
        }

        switch (grunnlagVersjon) {
            case FØRSTE:
                return List.of(første);
            case SISTE:
                return List.of(siste);
            case FØRSTE_OG_SISTE:
                return Objects.equals(første, siste) ? List.of(første) : List.of(første, siste);
            default:
                throw new UnsupportedOperationException("GrunnlagVersjon " + grunnlagVersjon + " er ikke støttet her for " + koblingId);
        }

    }
}
