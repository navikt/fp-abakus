package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.ps;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.AbstractInfotrygdGrunnlag;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.JsonConverter;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles.PersonRequest;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

@ApplicationScoped
@PS
public class InfotrygdPSGrunnlag extends AbstractInfotrygdGrunnlag {

    private static final String DEFAULT_URI = "http://infotrygd-grunnlag-paaroerende-sykdom.default/paaroerendeSykdom/grunnlag";
    private final JsonConverter jsonConverter = new JsonConverter();

    @Inject
    public InfotrygdPSGrunnlag(OidcRestClient restClient, @KonfigVerdi(value = "fpabakus.it.ps.grunnlag.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri);
    }

    public InfotrygdPSGrunnlag() {
        super();
    }


    @Override
    public List<Grunnlag> hentGrunnlag(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var jsonResponse = hentGrunnlagIJsonFormat(fnr, fom, tom);
            return jsonConverter.grunnlagBarnResponse(jsonResponse);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            throw new TekniskException("FP-180125", String.format("Tjeneste %s gir feil, meld til #infotrygd_replikering hvis dette skjer gjennom lengre tidsperiode.", uriString), e);
        }

    }

    @Override
    public List<Grunnlag> hentGrunnlagFailSoft(String fnr, LocalDate fom, LocalDate tom) {
        try {
            var jsonResponse = hentGrunnlagIJsonFormat(fnr, fom, tom);
            return jsonConverter.grunnlagBarnResponse(jsonResponse);
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen grunnlag", uriString, e);
            return Collections.emptyList();
        }
    }

    private String hentGrunnlagIJsonFormat(String fnr, LocalDate fom, LocalDate tom) {
        var request = new PersonRequest(konverter(fom), konverter(tom), List.of(fnr));
        return restClient.post(uri, request);
    }

    protected static LocalDate konverter(LocalDate dato) {
        return dato == null ? LocalDate.now() : dato;
    }
}
