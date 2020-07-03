package no.nav.foreldrepenger.abakus.registerdata.fagsakytelser.omp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.foreldrepenger.abakus.registerdata.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.MapInntektFraDtoTilDomene;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

/**
 * Standard IAY register innhenter.
 */
@ApplicationScoped
@YtelseTypeRef("OMP")
public class OmsorgspengerIAYRegisterInnhentingTjenesteImpl extends IAYRegisterInnhentingFellesTjenesteImpl {

    OmsorgspengerIAYRegisterInnhentingTjenesteImpl() {
        // CDI
    }

    @Inject
    public OmsorgspengerIAYRegisterInnhentingTjenesteImpl(InntektArbeidYtelseTjeneste inntektArbeidYtelseTjeneste,
                                                          VirksomhetTjeneste virksomhetTjeneste,
                                                          InnhentingSamletTjeneste innhentingSamletTjeneste,
                                                          SigrunTjeneste sigrunTjeneste,
                                                          VedtakYtelseRepository vedtakYtelseRepository,
                                                          MapInntektFraDtoTilDomene mapInntektFraDtoTilDomene,
                                                          AktørTjeneste aktørConsumer) {
        super(inntektArbeidYtelseTjeneste,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            sigrunTjeneste,
            vedtakYtelseRepository,
            mapInntektFraDtoTilDomene,
            aktørConsumer);
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
