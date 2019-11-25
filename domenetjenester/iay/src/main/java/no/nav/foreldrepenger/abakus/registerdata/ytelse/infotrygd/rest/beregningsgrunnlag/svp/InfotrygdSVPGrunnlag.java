package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.svp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.SVP;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@SVP
public class InfotrygdSVPGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-svangerskapspenger.default/foreldrepenger/svangerskap";

    public InfotrygdSVPGrunnlag(OidcRestClient restClient,
            @KonfigVerdi(value = "fpabakus.it.svp.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);

    }

    public InfotrygdSVPGrunnlag() {
        super();
    }
}