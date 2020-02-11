package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.svp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@SVP
public class InfotrygdSVPGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-svangerskapspenger.default/grunnlag";

    @Inject
    public InfotrygdSVPGrunnlag(OidcRestClient restClient, @KonfigVerdi(value = "fpabakus.it.svp.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);

    }

    public InfotrygdSVPGrunnlag() {
        super();
    }
}
