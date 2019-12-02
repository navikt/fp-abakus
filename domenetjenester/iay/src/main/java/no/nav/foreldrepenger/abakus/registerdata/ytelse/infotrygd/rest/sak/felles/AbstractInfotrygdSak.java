package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.felles;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.sak.Saker;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.sak.InfotrygdSak;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class AbstractInfotrygdSak implements InfotrygdSakTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractInfotrygdSak.class);

    private OidcRestClient restClient;
    private URI uri;
    private SakMapper mapper;

    public AbstractInfotrygdSak(OidcRestClient restClient, URI uri, SakMapper mapper) {
        this.restClient = restClient;
        this.uri = uri;
        this.mapper = mapper;
    }

    public AbstractInfotrygdSak() {
    }

    @Override
    public List<InfotrygdSak> saker(String fnr, LocalDate fom) {
        try {
            var request = new URIBuilder(uri).addParameter("fnr", fnr).build();
            LOG.trace("Slår opp saker fra {}", request);
            var respons = restClient.get(request, Saker.class);
            LOG.info("Fpakakus infotrygd rest. Fikk saker {}", respons);
            var saker = mapper.map(respons);
            LOG.info("Fpakakus infotrygd rest. Mappet saker {}", saker);
            return saker;
        } catch (Exception e) {
            LOG.info("Feil ved oppslag mot {}, returnerer ingen saker", uri, e);
            return emptyList();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[restClient=" + restClient + ", uri=" + uri + "]";
    }

}
