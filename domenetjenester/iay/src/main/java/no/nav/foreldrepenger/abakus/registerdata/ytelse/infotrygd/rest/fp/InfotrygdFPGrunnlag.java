package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.fp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.AbstractInfotrygdGrunnlag;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.StsSystemRestKlient;

@ApplicationScoped
@FP
public class InfotrygdFPGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-foreldrepenger.default/grunnlag";

    @Inject
    public InfotrygdFPGrunnlag(OidcRestClient restClient, StsSystemRestKlient stsklient,  @KonfigVerdi(value = "fpabakus.it.fp.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, stsklient);
    }

    public InfotrygdFPGrunnlag() {
        super();
    }
}
