package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.InfotrygdGrunnlag;
import no.nav.vedtak.exception.TekniskException;
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
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var request = new URIBuilder(uri)
                    .addParameter("fnr", fnr)
                    .addParameter("fom", konverter(fom))
                    .addParameter("tom", konverter(tom)).build();
            var grunnlag = restClient.get(request, Grunnlag[].class);
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            throw new TekniskException( "FP-180125", String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.", uriString), e);
        }
    }

    @Override
    public List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var request = new URIBuilder(uri)
                .addParameter("fnr", fnr)
                .addParameter("fom", konverter(fom))
                .addParameter("tom", konverter(tom)).build();
            var grunnlag = restClient.get(request, Grunnlag[].class);
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            return Collections.emptyList();
        }
    }


    private static String konverter(LocalDate dato) {
        var brukDato = dato == null ? LocalDate.now() : dato;
        return brukDato.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
