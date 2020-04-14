package no.nav.foreldrepenger.abakus.registerdata.fagsakytelser.omp;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.IAYRegisterInnhentingFellesTjenesteImpl;
import no.nav.foreldrepenger.abakus.registerdata.InnhentingSamletTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.MapInntektFraDtoTilDomene;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

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
                                                          SigrunTjeneste sigrunTjeneste, VedtakYtelseRepository vedtakYtelseRepository,
                                                          MapInntektFraDtoTilDomene mapInntektFraDtoTilDomene) {
        super(inntektArbeidYtelseTjeneste,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            sigrunTjeneste, vedtakYtelseRepository, mapInntektFraDtoTilDomene);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling kobling) {
        return true;
    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
