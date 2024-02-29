package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Arbeidsforhold;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Arbeidskategori;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Behandlingstema;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Inntektsperiode;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Orgnummer;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Periode;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Status;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Tema;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Vedtak;

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
            if (erLike(gs, gt)) {
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

    private boolean erLike(List<Grunnlag> g1, List<Grunnlag> g2) {
        var g1s = g1.stream().map(SammenG::new).toList();
        var g2s = g2.stream().map(SammenG::new).toList();

        return g1s.size() == g2s.size() && g1s.containsAll(g2s);
    }

    private record SammenG(Status status, Tema tema, Arbeidskategori kategori, List<SammenA> arbeidsforhold, Periode periode, Behandlingstema behandlingstema, LocalDate identdato, LocalDate opphørFom, List<SammenV> vedtak) {
        public SammenG(Grunnlag g) {
            this(g.status(), g.tema(), g.kategori(), g.arbeidsforhold().stream().map(SammenA::new).toList(), g.periode(), g.behandlingstema(), g.identdato(), g.opphørFom(), g.vedtak().stream().map(SammenV::new).toList());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            SammenG sammenG = (SammenG) o;
            return Objects.equals(status, sammenG.status) &&
                Objects.equals(tema, sammenG.tema) &&
                Objects.equals(kategori, sammenG.kategori) &&
                Objects.equals(periode, sammenG.periode) &&
                Objects.equals(behandlingstema, sammenG.behandlingstema) &&
                Objects.equals(identdato, sammenG.identdato) &&
                Objects.equals(opphørFom, sammenG.opphørFom) &&
                vedtak.size() == sammenG.vedtak.size() && vedtak.containsAll(sammenG.vedtak) &&
                arbeidsforhold.size() == sammenG.arbeidsforhold.size() && arbeidsforhold.containsAll(sammenG.arbeidsforhold);
        }

        @Override
        public int hashCode() {
            return Objects.hash(status, tema, kategori, arbeidsforhold, periode, behandlingstema, identdato, opphørFom, vedtak);
        }
    }

    private record SammenV(Periode periode, int utbetalingsgrad, String arbeidsgiverOrgnr, Boolean erRefusjon, Integer dagsats) {

        public SammenV(Vedtak v) {
            this(v.periode(), v.utbetalingsgrad(), v.arbeidsgiverOrgnr() == null || v.arbeidsgiverOrgnr().startsWith("0") ? null : v.arbeidsgiverOrgnr()  , v.erRefusjon(), v.dagsats());
        }
    }

    private record SammenA(Orgnummer orgnr, Integer inntekt, Inntektsperiode inntektsperiode, Boolean refusjon, LocalDate refusjonTom) {

        public SammenA(Arbeidsforhold a) {
            this(a.orgnr() == null || a.orgnr().orgnr() == null || a.orgnr().orgnr().startsWith("0") ? null : a.orgnr(), a.inntekt(), a.inntektsperiode(), a.refusjon(), a.refusjonTom());
        }
    }

}
