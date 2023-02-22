package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.spokelse.AbstractSpøkelseKlient;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "spokelse.grunnlag.url", endpointDefault = "http://spokelse.tbd.svc.cluster.local/grunnlag", scopesProperty = "spokelse.grunnlag.scopes", scopesDefault = "api://prod-gcp.tbd.spokelse/.default")
public class SpøkelseKlient extends AbstractSpøkelseKlient {

    public SpøkelseKlient() {
        super();
    }
}
