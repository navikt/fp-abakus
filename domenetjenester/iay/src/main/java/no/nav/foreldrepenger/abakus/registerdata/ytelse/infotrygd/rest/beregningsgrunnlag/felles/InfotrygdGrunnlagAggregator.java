package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles;

import static java.time.LocalDate.now;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.Aggregator;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.Grunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.InfotrygdGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;

@ApplicationScoped
@Aggregator
public class InfotrygdGrunnlagAggregator implements InfotrygdGrunnlag {

    private List<InfotrygdGrunnlag> tjenester;

    InfotrygdGrunnlagAggregator() {

    }

    @Inject
    public InfotrygdGrunnlagAggregator(@Any Instance<InfotrygdGrunnlag> tjenester) {
        this.tjenester = tjenesterFra(tjenester);
    }

    private static List<InfotrygdGrunnlag> tjenesterFra(Instance<InfotrygdGrunnlag> tjenester) {
        return tjenester
                .stream()
                .collect(toList());
    }

    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom) {
        return hentGrunnlag(fnr, fom, now());
    }

    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {

        return tjenester.stream()
                .parallel()
                .map(t -> t.hentGrunnlag(fnr, fom, tom))
                .flatMap(List::stream)
                .collect(toList());
    }

    @Override
    public List<Grunnlag> hentGrunnlag(AktørId aktørId, LocalDate fom) {
        return hentGrunnlag(aktørId, fom, now());

    }

    @Override
    public List<Grunnlag> hentGrunnlag(AktørId aktørId, LocalDate fom, LocalDate tom) {
        return tjenester.stream()
                .parallel()
                .map(t -> t.hentGrunnlag(aktørId, fom, tom))
                .flatMap(List::stream)
                .collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tjenester=" + tjenester + "]";
    }

}
