package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.felles;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.Grunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.beregningsgrunnlag.InfotrygdGrunnlag;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public abstract class AbstractInfotrygdGrunnlag implements InfotrygdGrunnlag {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractInfotrygdGrunnlag.class);

    private OidcRestClient restClient;
    private URI uri;

    public AbstractInfotrygdGrunnlag(OidcRestClient restClient, URI uri) {
        this.restClient = restClient;
        this.uri = uri;
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
                    .addParameter("fodselNr", fnr)
                    .addParameter("fom", konverter(fom))
                    .addParameter("tom", konverter(tom)).build();
            LOG.trace("Slår opp grunnlag SVP fra {}", request);
            var grunnlag = restClient.get(request, Grunnlag[].class);
            LOG.info("fpabacus infotrygd REST SVP fikk grunnlag {}", Arrays.toString(grunnlag));
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", uri, e);
            return emptyList();
        }
    }

    @Override
    public List<Grunnlag> hentGrunnlag(AktørId aktørId, LocalDate fom) {
        return hentGrunnlag(aktørId, fom, LocalDate.now());
    }

    @Override
    public List<Grunnlag> hentGrunnlag(AktørId aktørId, LocalDate fom, LocalDate tom) {
        return null;
    }

    private static String konverter(LocalDate dato) {
        return dato.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
