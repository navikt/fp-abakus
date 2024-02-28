package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@TSP
@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "fpabakus.it.tsp.grunnlag.url",
    endpointDefault = "http://fp-infotrygd-sykepenger.teamforeldrepenger/grunnlag",
    scopesProperty = "fpabakus.it.tsp.scopes", scopesDefault = "api://prod-fss.teamforeldrepenger.fp-infotrygd-sykepenger/.default")
public class TestInfotrygdSPGrunnlag extends AbstractInfotrygdGrunnlag {

    public TestInfotrygdSPGrunnlag() {
        super();
    }
}
