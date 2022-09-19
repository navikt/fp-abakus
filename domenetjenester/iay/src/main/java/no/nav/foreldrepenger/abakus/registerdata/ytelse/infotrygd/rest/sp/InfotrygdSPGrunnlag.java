package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.AbstractInfotrygdGrunnlag;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;

@ApplicationScoped
@SP
public class InfotrygdSPGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-sykepenger-fp.default/grunnlag";

    @Inject
    public InfotrygdSPGrunnlag(RestClient restClient, @KonfigVerdi(value = "fpabakus.it.sp.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);
    }

    public InfotrygdSPGrunnlag() {
        super();
    }
}
