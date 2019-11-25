package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.fp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.FP;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@FP
public class InfotrygdFPGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-foreldrepenger.default/foreldrepenger/svangerskap";

    public InfotrygdFPGrunnlag(OidcRestClient restClient,
            @KonfigVerdi(value = "fpabakus.it.fp.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);

    }

    public InfotrygdFPGrunnlag() {
        super();
    }
}