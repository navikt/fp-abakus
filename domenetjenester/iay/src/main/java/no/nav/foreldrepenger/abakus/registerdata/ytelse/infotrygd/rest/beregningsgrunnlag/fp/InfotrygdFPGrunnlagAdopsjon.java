package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.fp;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.FPA;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@FPA
public class InfotrygdFPGrunnlagAdopsjon extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-foreldrepenger.default/foreldrepenger/adopsjon";

    @Inject
    public InfotrygdFPGrunnlagAdopsjon(OidcRestClient restClient,
            @KonfigVerdi(value = "fpabakus.it.fp.grunnlag.adopsjon.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);
    }

    public InfotrygdFPGrunnlagAdopsjon() {
        super();
    }
}
