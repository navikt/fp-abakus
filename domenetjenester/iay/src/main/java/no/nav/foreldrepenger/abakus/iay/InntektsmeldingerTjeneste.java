package no.nav.foreldrepenger.abakus.iay;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.InntektsmeldingAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.MapInntektsmeldinger;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.inntektsmelding.v1.InntektsmeldingerDto;

@ApplicationScoped
public class InntektsmeldingerTjeneste {

    private InntektArbeidYtelseRepository repository;

    public InntektsmeldingerTjeneste() {
    }

    @Inject
    public InntektsmeldingerTjeneste(InntektArbeidYtelseRepository repository) {
        this.repository = repository;
    }

    public InntektsmeldingerDto hentAlleInntektsmeldingerForSak(AktørId aktørId, Saksnummer saksnummer, YtelseType ytelseType) {
        var grunnlag = repository.hentAlleInntektArbeidYtelseGrunnlagFor(aktørId, saksnummer, ytelseType, false);
        List<InntektsmeldingDto> inntektsmeldinger = mapUnikeInntektsmeldinger(grunnlag);
        InntektsmeldingerDto inntektsmeldingerDto = new InntektsmeldingerDto();
        inntektsmeldingerDto.medInntektsmeldinger(inntektsmeldinger);
        return inntektsmeldingerDto;
    }

    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjonEntitet::new);
    }

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, ArbeidsforholdInformasjonBuilder informasjonBuilder, List<Inntektsmelding> alleInntektsmeldinger) {
        return repository.lagre(koblingReferanse, informasjonBuilder, alleInntektsmeldinger);
    }

    private ArbeidsforholdInformasjon getArbeidsforholdInformasjon(InntektArbeidYtelseGrunnlag grunnlag) {
        return grunnlag.getArbeidsforholdInformasjon()
            .orElseThrow(() -> new IllegalStateException("Mangler ArbeidsforholdInformasjon i grunnlag (påkrevd her): " + grunnlag.getGrunnlagReferanse()));
    }

    private List<InntektsmeldingDto> mapUnikeInntektsmeldinger(List<InntektArbeidYtelseGrunnlag> grunnlag) {
        return grunnlag.stream().flatMap(iayg ->
            iayg.getInntektsmeldinger()
                .stream()
                .map(InntektsmeldingAggregat::getAlleInntektsmeldinger)
                .flatMap(Collection::stream)
                .map(im -> {
                    var mapper = new MapInntektsmeldinger.MapTilDto(getArbeidsforholdInformasjon(iayg));
                    return mapper.mapInntektsmelding(im);
                })
        ).distinct().collect(Collectors.toList());
    }

}
