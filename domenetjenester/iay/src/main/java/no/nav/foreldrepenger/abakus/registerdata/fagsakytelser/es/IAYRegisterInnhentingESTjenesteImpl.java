package no.nav.foreldrepenger.abakus.registerdata.fagsakytelser.es;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.foreldrepenger.abakus.registerdata.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

@ApplicationScoped
@YtelseTypeRef("ES")
public class IAYRegisterInnhentingESTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    protected IAYRegisterInnhentingESTjenesteImpl() {
        super();
    }

    @Inject
    public IAYRegisterInnhentingESTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                               VirksomhetTjeneste virksomhetTjeneste,
                                               InnhentingSamletTjeneste innhentingSamletTjeneste,
                                               AktørTjeneste aktørConsumer,
                                               SigrunTjeneste sigrunTjeneste, VedtakYtelseRepository vedtakYtelseRepository) {
        super(inntektArbeidYtelseTjeneste,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            aktørConsumer,
            sigrunTjeneste, vedtakYtelseRepository);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling behandling) {
        return false;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return false;
    }

}
