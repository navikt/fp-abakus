package no.nav.foreldrepenger.abakus.registerdata;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

/**
 * Standard IAY register innhenter.
 */
@ApplicationScoped
@YtelseTypeRef()
public class DefaultIAYRegisterInnhentingTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    DefaultIAYRegisterInnhentingTjenesteImpl() {
        // CDI
    }

    @Inject
    public DefaultIAYRegisterInnhentingTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                    VirksomhetTjeneste virksomhetTjeneste,
                                                    InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                    AktørConsumer aktørConsumer,
                                                    SigrunTjeneste sigrunTjeneste, VedtakYtelseRepository vedtakYtelseRepository) {
        super(inntektArbeidYtelseTjeneste,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            aktørConsumer,
            sigrunTjeneste, vedtakYtelseRepository);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling kobling) {
        return inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse())
            .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
            .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
            .orElse(false);
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
