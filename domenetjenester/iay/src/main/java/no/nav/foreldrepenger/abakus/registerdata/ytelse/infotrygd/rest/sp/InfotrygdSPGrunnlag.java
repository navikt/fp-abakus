package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp;

import jakarta.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@SP
@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "fpabakus.it.sp.grunnlag.url",
    endpointDefault = "http://infotrygd-sykepenger-fp.infotrygd/grunnlag",
    scopesProperty = "fpabakus.it.sp.scopes", scopesDefault = "api://prod-fss.infotrygd.infotrygd-sykepenger-fp/.default")
public class InfotrygdSPGrunnlag extends AbstractInfotrygdGrunnlag {

    public InfotrygdSPGrunnlag() {
        super();
    }
}
