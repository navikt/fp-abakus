package no.nav.foreldrepenger.abakus.iay;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjon;
import no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold.ArbeidsforholdInformasjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.inntektsmelding.Inntektsmelding;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;

@ApplicationScoped
public class InntektsmeldingerTjeneste {

    private InntektArbeidYtelseRepository repository;

    public InntektsmeldingerTjeneste() {
    }

    @Inject
    public InntektsmeldingerTjeneste(InntektArbeidYtelseRepository repository) {
        this.repository = repository;
    }

    public ArbeidsforholdInformasjon hentArbeidsforholdInformasjonForKobling(KoblingReferanse koblingReferanse) {
        return repository.hentArbeidsforholdInformasjonForBehandling(koblingReferanse).orElseGet(ArbeidsforholdInformasjon::new);
    }

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse,
                                   ArbeidsforholdInformasjonBuilder informasjonBuilder,
                                   List<Inntektsmelding> alleInntektsmeldinger) {
        return repository.lagre(koblingReferanse, informasjonBuilder, alleInntektsmeldinger);
    }


}
