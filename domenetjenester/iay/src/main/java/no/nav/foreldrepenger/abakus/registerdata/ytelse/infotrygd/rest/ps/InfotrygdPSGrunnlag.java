package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.context.Dependent;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.PersonRequest;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.AbstractInfotrygdGrunnlag;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
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

    protected static LocalDate konverter(LocalDate dato) {
        return dato == null ? LocalDate.now() : dato;
    }

    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        try {
            return hentGrunnlagIJsonFormat(fnr, fom, tom);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", getRestConfig().endpoint(), e);
            throw new TekniskException("FP-180125",
                String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.",
                    getRestConfig().endpoint()), e);
        }

    }

    @Override
    public List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        try {
            return hentGrunnlagIJsonFormat(fnr, fom, tom);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", getRestConfig().endpoint(), e);
            return Collections.emptyList();
        }
    }

    private List<Grunnlag> hentGrunnlagIJsonFormat(String fnr, LocalDate fom, LocalDate tom) {
        var prequest = new PersonRequest(konverter(fom), konverter(tom), List.of(fnr));
        var request = RestRequest.newPOSTJson(prequest, getRestConfig().endpoint(), getRestConfig());
        var resultat = getRestClient().send(request, Grunnlag[].class);
        return Arrays.asList(resultat);
    }
}
