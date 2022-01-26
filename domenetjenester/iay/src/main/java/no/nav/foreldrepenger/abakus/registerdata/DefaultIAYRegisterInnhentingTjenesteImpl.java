package no.nav.foreldrepenger.abakus.registerdata;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningAggregat;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.kontroll.YtelseTypeRef;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.sigrun.SigrunTjeneste;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;

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
                                                    AktørTjeneste aktørConsumer,
                                                    SigrunTjeneste sigrunTjeneste, VedtattYtelseInnhentingTjeneste vedtattYtelseInnhentingTjeneste) {
        super(inntektArbeidYtelseTjeneste,
            virksomhetTjeneste,
            innhentingSamletTjeneste,
            aktørConsumer,
            sigrunTjeneste, vedtattYtelseInnhentingTjeneste);
    }

    @Override
    public boolean skalInnhenteNæringsInntekterFor(Kobling kobling) {
        Optional<InntektArbeidYtelseGrunnlag> grunnlag = inntektArbeidYtelseTjeneste.hentGrunnlagFor(kobling.getKoblingReferanse());

        //FP,SVP,FRISINN bruker ikke aggregat for oppgitt opptjening (støtter kun en pr behandling)
        boolean harOppgittSNOpptjeningUtenAggregat = grunnlag
            .flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjening)
            .map(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty())
            .orElse(false);

        //OMP, PSB bruker aggregat for oppgitt opptjening (støtter mange pr behandling)
        Optional<OppgittOpptjeningAggregat> aggregat = grunnlag.flatMap(InntektArbeidYtelseGrunnlag::getOppgittOpptjeningAggregat);
        boolean harOppgittOpptjeningSNMedAggregat = aggregat.isPresent() && aggregat.get().getOppgitteOpptjeninger().stream().anyMatch(oppgittOpptjening -> !oppgittOpptjening.getEgenNæring().isEmpty());

        return harOppgittSNOpptjeningUtenAggregat || harOppgittOpptjeningSNMedAggregat ;


    }

    @Override
    public boolean skalInnhenteYtelseGrunnlag(Kobling kobling) {
        return true;
    }

}
