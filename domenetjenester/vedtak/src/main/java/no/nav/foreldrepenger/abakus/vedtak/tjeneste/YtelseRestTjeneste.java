package no.nav.foreldrepenger.abakus.vedtak.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseEntitet;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.ytelse.Ytelse;
import no.nav.vedtak.ytelse.v1.YtelseV1;

@Api(tags = "ytelse")
@Path("/ytelse/v1")
@ApplicationScoped
@Transaction
public class YtelseRestTjeneste {

    private VedtakYtelseRepository ytelseRepository;
    private ExtractFromYtelseV1 extractor;

    public YtelseRestTjeneste() {
    }

    @Inject
    public YtelseRestTjeneste(VedtakYtelseRepository ytelseRepository, ExtractFromYtelseV1 extractor) {
        this.ytelseRepository = ytelseRepository;
        this.extractor = extractor;
    }

    @POST
    @Path("/vedtatt")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Lagrer ytelse vedtak")
    @BeskyttetRessurs(action = CREATE, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response arbeidsforholdForReferanse(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid Ytelse request) {

        //VedtakYtelseBuilder builder = extractor.extractFrom((YtelseV1) request);

        Optional<VedtakYtelseEntitet> siste = extractor.hentSisteVedtatteFor((YtelseV1) request);
        VedtakYtelseBuilder builder = extractor.extractFrom((YtelseV1) request);

        ytelseRepository.lagre(builder, siste);

        return Response.accepted().build();
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {
        @Override
        public AbacDataAttributter apply(Object obj) {
            Ytelse req = (Ytelse) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getAktør().getVerdi());
        }
    }
}
