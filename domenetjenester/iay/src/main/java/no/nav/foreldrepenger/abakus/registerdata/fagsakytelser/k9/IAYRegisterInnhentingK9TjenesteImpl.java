package no.nav.foreldrepenger.abakus.registerdata.fagsakytelser.k9;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.foreldrepenger.abakus.registerdata.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.VedtattYtelseInnhentingTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

@ApplicationScoped
@YtelseTypeRef("PSB")
@YtelseTypeRef("OMP")
public class IAYRegisterInnhentingK9TjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    protected IAYRegisterInnhentingK9TjenesteImpl() {
        super();
    }

    @Inject
    public IAYRegisterInnhentingK9TjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                               VirksomhetTjeneste virksomhetTjeneste,
                                               InnhentingSamletTjeneste innhentingSamletTjeneste,
                                               AktørTjeneste aktørConsumer,
                                               SigrunTjeneste sigrunTjeneste, VedtattYtelseInnhentingTjeneste VedtattYtelseInnhentingTjeneste) {
        super(inntektArbeidYtelseTjeneste,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            aktørConsumer,
            sigrunTjeneste, VedtattYtelseInnhentingTjeneste);
    }

    // Skal alltid innhente næringsinntekter om det ligger i informasjonselementer, dette for å støtte 8-47.
    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling behandling) {
        return true;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
