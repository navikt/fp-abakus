package no.nav.foreldrepenger.abakus.registerdata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.FagsakYtelseTypeRef;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@ApplicationScoped
@FagsakYtelseTypeRef("FP")
public class IAYRegisterInnhentingFPTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    IAYRegisterInnhentingFPTjenesteImpl() {
        // CDI
    }


    @Inject
    public IAYRegisterInnhentingFPTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                               KodeverkRepository kodeverkRepository,
                                               VirksomhetTjeneste virksomhetTjeneste,
                                               InnhentingSamletTjeneste innhentingSamletTjeneste,
                                               AktørConsumer aktørConsumer) {
        super(inntektArbeidYtelseTjeneste,
            kodeverkRepository,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            aktørConsumer);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling behandling) {
        return true;
    }

    @Override
    public InntektArbeidYtelseAggregatBuilder innhentYtelserForInvolverteParter(Kobling behandling) {
        return ytelseRegisterInnhenting.innhentYtelserForInvolverteParter(behandling, behandling.getOpplysningsperiode().tilIntervall(), true);
    }

}
