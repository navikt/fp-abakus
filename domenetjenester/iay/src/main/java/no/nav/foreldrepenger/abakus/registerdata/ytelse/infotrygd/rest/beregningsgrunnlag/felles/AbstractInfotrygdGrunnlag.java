package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.InfotrygdGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.InfotrygdRestFeil;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public abstract class AbstractInfotrygdGrunnlag implements InfotrygdGrunnlag {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractInfotrygdGrunnlag.class);

    private OidcRestClient restClient;
    private URI uri;
    private String uriString;

    public AbstractInfotrygdGrunnlag(OidcRestClient restClient, URI uri) {
        this.restClient = restClient;
        this.uri = uri;
        this.uriString = uri.toString();
    }

    public AbstractInfotrygdGrunnlag() {
    }

    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom) {
        return hentGrunnlag(fnr, fom, LocalDate.now());
    }

    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var request = new URIBuilder(uri)
                    .addParameter("fnr", fnr)
                    .addParameter("fom", konverter(fom))
                    .addParameter("tom", konverter(tom)).build();
            LOG.trace("Slår opp grunnlag SVP fra {}", request);
            var grunnlag = restClient.get(request, Grunnlag[].class);
            LOG.info("fpabacus infotrygd REST {} fikk grunnlag {}", uriString, Arrays.toString(grunnlag));
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            LOG.error("Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            throw InfotrygdRestFeil.FACTORY.feilfratjeneste(uriString).toException();
        }
    }

    private static String konverter(LocalDate dato) {
        return dato.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
