package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.InfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;

@ApplicationScoped
public class InfotrygdGrunnlagAggregator {

    private List<InfotrygdGrunnlag> tjenester;

    InfotrygdGrunnlagAggregator() {
    }

    @Inject
    public InfotrygdGrunnlagAggregator(@Any Instance<InfotrygdGrunnlag> tjenester) {
        this.tjenester = tjenester.stream().collect(toList());
    }

    public List<Grunnlag> hentAggregertGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        return tjenester.stream().map(t -> t.hentGrunnlag(fnr, fom, tom)).flatMap(List::stream).collect(toList());
    }

    public List<Grunnlag> hentAggregertGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        return tjenester.stream().map(t -> t.hentGrunnlagFailSoft(fnr, fom, tom)).flatMap(List::stream).collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tjenester=" + tjenester + "]";
    }

}
