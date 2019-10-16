package no.nav.foreldrepenger.abakus.iay;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;

@ApplicationScoped
public class OppgittOpptjeningTjeneste {

    private InntektArbeidYtelseRepository repository;

    public OppgittOpptjeningTjeneste() {
    }

    @Inject
    public OppgittOpptjeningTjeneste(InntektArbeidYtelseRepository repository) {
        this.repository = repository;
    }

    public GrunnlagReferanse lagre(KoblingReferanse koblingReferanse, OppgittOpptjeningBuilder builder) {
        return repository.lagre(koblingReferanse, builder);
    }
}
