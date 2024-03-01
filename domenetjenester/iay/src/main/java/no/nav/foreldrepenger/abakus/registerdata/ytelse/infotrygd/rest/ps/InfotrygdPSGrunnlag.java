package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@PS
@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "fpabakus.it.ps.grunnlag.url",
    endpointDefault = "http://k9-infotrygd-grunnlag-paaroerende-sykdom.k9saksbehandling/paaroerendeSykdom/grunnlag",
    scopesProperty = "fpabakus.it.ps.scopes", scopesDefault = "api://prod-fss.k9saksbehandling.k9-infotrygd-grunnlag-paaroerende-sykdom/.default")
public class InfotrygdPSGrunnlag extends AbstractInfotrygdGrunnlag {

    public InfotrygdPSGrunnlag() {
        super();
    }

}
