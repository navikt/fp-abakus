package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYDtoMapper;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Api(tags = "arbeidsforhold")
@Path("/iay/v1")
@ApplicationScoped
@Transaction
public class GrunnlagRestTjeneste {

    private InntektArbeidYtelseTjeneste iayTjeneste;

    public GrunnlagRestTjeneste() {
    }

    @Inject
    public GrunnlagRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste) {
        this.iayTjeneste = iayTjeneste;
    }

    @POST
    @Path("/grunnlag")
    @ApiOperation(value = "Hent IAY Grunnlag for angitt søke spesifikasjon", response = InntektArbeidYtelseGrunnlagDto.class)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentIayGrunnlag(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid InntektArbeidYtelseGrunnlagRequest spesifikasjon) {

        // TODO: sjekk at spesifikasjon#person matcher grunnlag og sjekk at PersonIdent kun er aktørIds
        AktørId aktørId = new AktørId(spesifikasjon.getPerson().getIdent());

        GrunnlagReferanse grunnlagReferanse = new GrunnlagReferanse(spesifikasjon.getGrunnlagReferanse());
        KoblingReferanse koblingReferanse = getKoblingReferanse(spesifikasjon);
        InntektArbeidYtelseGrunnlag grunnlag = getGrunnlag(spesifikasjon, grunnlagReferanse, koblingReferanse);

        IAYDtoMapper dtoMapper = new IAYDtoMapper(iayTjeneste, aktørId, grunnlagReferanse, koblingReferanse);

        return Response.ok(dtoMapper.mapTilDto(grunnlag, spesifikasjon)).build();
    }

    private KoblingReferanse getKoblingReferanse(InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
        if (spesifikasjon.getKoblingReferanse() != null) {
            return new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        } else {
            var grunnlagReferanse = new GrunnlagReferanse(spesifikasjon.getGrunnlagReferanse());
            return iayTjeneste.hentKoblingReferanse(grunnlagReferanse);
        }
    }

    private InntektArbeidYtelseGrunnlag getGrunnlag(InntektArbeidYtelseGrunnlagRequest spesifikasjon, GrunnlagReferanse grunnlagReferanse, KoblingReferanse koblingReferanse) {
        if (grunnlagReferanse != null) {
            return iayTjeneste.hentAggregat(grunnlagReferanse);
        } else if (koblingReferanse != null) {
        }

        // FIXME: Kobling UUID
        throw new UnsupportedOperationException("Støtter ikke koblingreferanser basert på UUID ennå");
    }

    private class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            InntektArbeidYtelseGrunnlagRequest req = (InntektArbeidYtelseGrunnlagRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getPerson().getIdent());
        }
    }
}
