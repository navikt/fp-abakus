package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.AvsluttedeSaker;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.AvsluttetSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.LøpendeSak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.Sak;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.Saker;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;

public abstract class AbstractInfotrygdSakMapper implements SakMapper {

    protected abstract InfotrygdSak fraAvsluttetSak(AvsluttetSak sak);

    protected abstract InfotrygdSak fraSak(Sak sak);

    protected abstract InfotrygdSak fraLøpendeSak(LøpendeSak sak);

    @Override
    public List<InfotrygdSak> map(Saker saker) {
        return infotrygdSakerFra(
                infotrygdSakerFraLøpendeSaker(saker.getLøpendeSaker()),
                infotrygdSakerFraAvsluttedeSaker(saker.getAvsluttedeSaker()),
                infotrygdSakerFraSaker(saker.getSaker()));
    }

    private Stream<InfotrygdSak> infotrygdSakerFraSaker(List<Sak> saker) {
        return stream(saker)
                .map(this::fraSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraAvsluttedeSaker(AvsluttedeSaker avsluttedeSaker) {
        return stream(avsluttedeSaker.getSaker())
                .map(this::fraAvsluttetSak);
    }

    private Stream<InfotrygdSak> infotrygdSakerFraLøpendeSaker(List<LøpendeSak> løpendeSaker) {
        return stream(løpendeSaker)
                .map(this::fraLøpendeSak);
    }

    private static <T> Stream<T> stream(List<T> list) {
        return Optional.ofNullable(list)
                .map(List::stream)
                .orElseGet(() -> empty());
    }

    private static <T> List<T> infotrygdSakerFra(Stream<T> s1, Stream<T> s2, Stream<T> s3) {
        return concat(s1, concat(s2, s3))
                .collect(toList());
    }
}
