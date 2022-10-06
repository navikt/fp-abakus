package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.fp;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@FP
@Dependent
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "fpabakus.it.fp.grunnlag.url", endpointDefault = "http://infotrygd-foreldrepenger.default/grunnlag")
public class InfotrygdFPGrunnlag extends AbstractInfotrygdGrunnlag {

    public InfotrygdFPGrunnlag() {
        super();
    }
}
