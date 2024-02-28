package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps.InfotrygdPSGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps.PS;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp.InfotrygdSPGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp.SP;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp.TSP;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp.TestInfotrygdSPGrunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.InfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;

@ApplicationScoped
public class InfotrygdGrunnlagAggregator {
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdGrunnlagAggregator.class);
    private List<InfotrygdGrunnlag> tjenester;
    private InfotrygdGrunnlag sykepenger;
    private InfotrygdGrunnlag testSykepenger;

    InfotrygdGrunnlagAggregator() {
    }

    @Inject
    public InfotrygdGrunnlagAggregator(@PS InfotrygdPSGrunnlag ps, @SP InfotrygdSPGrunnlag sp, @TSP TestInfotrygdSPGrunnlag tsp) {
        this.tjenester = List.of(ps, sp);
        this.sykepenger = sp;
        this.testSykepenger = tsp;
    }

    public List<Grunnlag> hentAggregertGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        sammenlignSykepenger(fnr, fom, tom);
        return tjenester.stream().map(t -> t.hentGrunnlag(fnr, fom, tom)).flatMap(List::stream).collect(toList());
    }

    public List<Grunnlag> hentAggregertGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        sammenlignSykepenger(fnr, fom, tom);
        return tjenester.stream().map(t -> t.hentGrunnlagFailSoft(fnr, fom, tom)).flatMap(List::stream).collect(toList());
    }

    private void sammenlignSykepenger(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var gs = sykepenger.hentGrunnlagFailSoft(fnr, fom, tom);
            var gt = testSykepenger.hentGrunnlagFailSoft(fnr, fom, tom);
            if (gs.size() == gt.size() && gs.containsAll(gt)) {
                LOG.info("ABAKUS-SYKEPENGER like svar");
            } else {
                LOG.info("ABAKUS-SYKEPENGER ulike svar gamle {} nye {}", gs, gt);
            }
        } catch (Exception e) {
            LOG.info("ABAKUS-SYKEPENGER fikk feil", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tjenester=" + tjenester + "]";
    }

}
