package no.nav.foreldrepenger.abakus.registerdata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

/** Standard IAY register innhenter. */
@ApplicationScoped
@YtelseTypeRef()
public class DefaultIAYRegisterInnhentingTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    DefaultIAYRegisterInnhentingTjenesteImpl() {
        // CDI
    }

    @Inject
    public DefaultIAYRegisterInnhentingTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                    KodeverkRepository kodeverkRepository,
                                                    VirksomhetTjeneste virksomhetTjeneste,
                                                    InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                    AktørConsumer aktørConsumer,
                                                    SigrunTjeneste sigrunTjeneste,
                                                    VedtakYtelseRepository vedtakYtelseRepository,
                                                    InntektMapper inntektMapper) {
        super(inntektArbeidYtelseTjeneste,
            kodeverkRepository,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            sigrunTjeneste, vedtakYtelseRepository, inntektMapper);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling behandling) {
        return true;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
