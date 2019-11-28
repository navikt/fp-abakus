package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

@ApplicationScoped
public class InfotrygdSakAggregator  {

    private List<InfotrygdSakTjeneste> tjenester;

    InfotrygdSakAggregator() {

    }

    public InfotrygdSakAggregator(@Any Instance<InfotrygdSakTjeneste> tjenester) {
        this.tjenester = tjenesterFra(tjenester);
    }

    private static List<InfotrygdSakTjeneste> tjenesterFra(Instance<InfotrygdSakTjeneste> tjenester) {
        return tjenester
                .stream()
                .collect(toList());
    }

    public List<InfotrygdSak> saker(String fnr, LocalDate fom) {
        return tjenester.stream()
                .parallel()
                .map(t -> t.saker(fnr, fom))
                .flatMap(List::stream)
                .collect(toList());
    }

    public String toString() {
        return getClass().getSimpleName() + "[tjenester=" + tjenester + "]";
    }

}
