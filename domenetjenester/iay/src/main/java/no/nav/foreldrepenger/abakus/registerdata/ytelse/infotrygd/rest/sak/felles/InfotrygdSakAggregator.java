package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.Aggregator;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

@ApplicationScoped
@Aggregator
public class InfotrygdSakAggregator implements InfotrygdSakTjeneste {

    private List<InfotrygdSakTjeneste> tjenester;

    InfotrygdSakAggregator() {

    }

    @Inject
    private InfotrygdSakAggregator(@Any Instance<InfotrygdSakTjeneste> tjenester) {
        this.tjenester = tjenesterFra(tjenester);
    }

    private static List<InfotrygdSakTjeneste> tjenesterFra(Instance<InfotrygdSakTjeneste> tjenester) {
        return tjenester
                .stream()
                .collect(toList());
    }

    @Override
    public List<InfotrygdSak> saker(String fnr, LocalDate fom) {
        return tjenester.stream()
                .parallel()
                .map(t -> t.saker(fnr, fom))
                .flatMap(List::stream)
                .collect(toList());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tjenester=" + tjenester + "]";
    }

}
