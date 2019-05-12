package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.UUID;

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
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYDtoMapper;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

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
    public Response hentIayGrunnlag(@NotNull @Valid InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
        
        // TODO: sjekk at spesifikasjon#person matcher grunnlag og sjekk at PersonIdent kun er aktørIds
        AktørId aktørId = new AktørId(spesifikasjon.getPerson().getIdent());
        
        UUID grunnlagReferanse = spesifikasjon.getGrunnlagReferanse();
        UUID koblingReferanse = getKoblingReferanse(spesifikasjon);
        InntektArbeidYtelseGrunnlag grunnlag = getGrunnlag(spesifikasjon, grunnlagReferanse, koblingReferanse);

        IAYDtoMapper dtoMapper = new IAYDtoMapper(iayTjeneste, aktørId, grunnlagReferanse, koblingReferanse);
        
        return Response.ok(dtoMapper.mapTilDto(grunnlag, spesifikasjon)).build();
    }

    private UUID getKoblingReferanse(InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
        if (spesifikasjon.getKoblingReferanse() != null) {
            return spesifikasjon.getKoblingReferanse();
        } else {
            var grunnlagReferanse = spesifikasjon.getGrunnlagReferanse();
            // FIXME: Kobling UUID
            Long koblingId = iayTjeneste.hentKoblingIdFor(grunnlagReferanse);
            throw new UnsupportedOperationException("Bytt til uuid for koblinger");
        }
    }

    private InntektArbeidYtelseGrunnlag getGrunnlag(InntektArbeidYtelseGrunnlagRequest spesifikasjon, UUID grunnlagReferanse, UUID koblingReferanse) {
        if (grunnlagReferanse != null) {
            return iayTjeneste.hentAggregat(grunnlagReferanse);
        } else if (koblingReferanse != null) {
        }

        // FIXME: Kobling UUID
        throw new UnsupportedOperationException("Støtter ikke koblingreferanser basert på UUID ennå");
    }

}
