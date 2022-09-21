package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.InfotrygdGrunnlag;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

public abstract class AbstractInfotrygdGrunnlag implements InfotrygdGrunnlag {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractInfotrygdGrunnlag.class);

    protected RestClient restClient;
    protected URI uri;
    protected String uriString;

    public AbstractInfotrygdGrunnlag(RestClient restClient, URI uri) {
        this.restClient = restClient;
        this.uri = uri;
        this.uriString = uri.toString();
    }

    public AbstractInfotrygdGrunnlag() {
    }

    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var path = UriBuilder.fromUri(uri)
                    .queryParam("fnr", fnr)
                    .queryParam("fom", konverter(fom))
                    .queryParam("tom", konverter(tom)).build();
            var grunnlag = restClient.send(RestRequest.newGET(path, TokenFlow.STS_CC, null), Grunnlag[].class);
            return Arrays.asList(grunnlag);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            throw new TekniskException( "FP-180125", String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.", uriString), e);
        }
    }

    @Override
    public List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var path = UriBuilder.fromUri(uri)
                .queryParam("fnr", fnr)
                .queryParam("fom", konverter(fom))
                .queryParam("tom", konverter(tom)).build();
            var grunnlag = restClient.send(RestRequest.newGET(path, TokenFlow.STS_CC, null), Grunnlag[].class);
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
