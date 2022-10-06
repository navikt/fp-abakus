package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sp;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@SP
@Dependent
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "fpabakus.it.sp.grunnlag.url", endpointDefault = "http://infotrygd-sykepenger-fp.default/grunnlag")
public class InfotrygdSPGrunnlag extends AbstractInfotrygdGrunnlag {

    public InfotrygdSPGrunnlag() {
        super();
    }
}
