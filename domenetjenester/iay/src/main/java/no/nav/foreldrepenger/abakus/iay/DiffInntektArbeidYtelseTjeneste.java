package no.nav.foreldrepenger.abakus.iay;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.foreldrepenger.abakus.felles.diff.DiffResult;

@ApplicationScoped
public class DiffInntektArbeidYtelseTjeneste {

    private InntektArbeidYtelseRepository repository;

    public DiffInntektArbeidYtelseTjeneste() {
    }

    @Inject
    public DiffInntektArbeidYtelseTjeneste(InntektArbeidYtelseRepository repository) {
        this.repository = repository;
    }

    /**
     * @param referanse UUID som unikt identifiserer grunnlaget
     * @return henter aggregat, null hvis det ikke finnes.
     */
    public InntektArbeidYtelseGrunnlag hentAggregat(String referanse) {
        return repository.hentInntektArbeidYtelseForReferanse(UUID.fromString(referanse));
    }

    public DiffResult diff(InntektArbeidYtelseGrunnlag gammelGrunnlag, InntektArbeidYtelseGrunnlag nyttGrunnlag) {
        return repository.diffResultat(gammelGrunnlag, nyttGrunnlag, true);
    }
}
