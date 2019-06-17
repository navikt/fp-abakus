package no.nav.foreldrepenger.abakus.iay.tjeneste;

import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursResourceAttributt.FAGSAK;

import java.util.Objects;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.UnsupportedOperationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import no.nav.foreldrepenger.abakus.domene.iay.GrunnlagReferanse;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseGrunnlag;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.IAYTilDtoMapper;
import no.nav.foreldrepenger.abakus.kobling.Kobling;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kobling.KoblingTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.request.InntektArbeidYtelseGrunnlagRequest;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Api(tags = "iay")
@Path("/iay/grunnlag/v1")
@ApplicationScoped
@Transaction
public class GrunnlagRestTjeneste {

    private InntektArbeidYtelseTjeneste iayTjeneste;
    private KoblingTjeneste koblingTjeneste;

    public GrunnlagRestTjeneste() {
        // for CDI
    }

    @Inject
    public GrunnlagRestTjeneste(InntektArbeidYtelseTjeneste iayTjeneste,
                                KoblingTjeneste koblingTjeneste) {
        this.iayTjeneste = iayTjeneste;
        this.koblingTjeneste = koblingTjeneste;
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Hent IAY Grunnlag for angitt søke spesifikasjon", response = InntektArbeidYtelseGrunnlagDto.class)
    @BeskyttetRessurs(action = READ, ressurs = FAGSAK)
    @SuppressWarnings("findsecbugs:JAXRS_ENDPOINT")
    public Response hentIayGrunnlag(@NotNull @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) @Valid InntektArbeidYtelseGrunnlagRequest spesifikasjon) {

        var aktørId = new AktørId(spesifikasjon.getPerson().getIdent());

        var grunnlagReferanse = new GrunnlagReferanse(spesifikasjon.getGrunnlagReferanse());
        var koblingReferanse = getKoblingReferanse(aktørId, spesifikasjon);
        var grunnlag = getGrunnlag(spesifikasjon, grunnlagReferanse, koblingReferanse);

        var dtoMapper = new IAYTilDtoMapper(aktørId, grunnlagReferanse, koblingReferanse);

        return Response.ok(dtoMapper.mapTilDto(grunnlag, spesifikasjon)).build();
    }

    private KoblingReferanse getKoblingReferanse(AktørId aktørId, InntektArbeidYtelseGrunnlagRequest spesifikasjon) {
        KoblingReferanse koblingReferanse;
        if (spesifikasjon.getKoblingReferanse() != null) {
            koblingReferanse = new KoblingReferanse(spesifikasjon.getKoblingReferanse());
        } else {
            var grunnlagReferanse = new GrunnlagReferanse(spesifikasjon.getGrunnlagReferanse());
            koblingReferanse = iayTjeneste.hentKoblingReferanse(grunnlagReferanse);
        }

        Kobling kobling = koblingTjeneste.hentFor(koblingReferanse).orElseThrow(IllegalArgumentException::new);

        if (!Objects.equals(kobling.getAktørId(), aktørId)) {
            // post-condition sjekk at angitt person stemmer med koblingens aktør
            throw new IllegalArgumentException("Kobling ikke knyttet til angitt aktør: " + aktørId);
        }
        return koblingReferanse;
    }

    private InntektArbeidYtelseGrunnlag getGrunnlag(@SuppressWarnings("unused") InntektArbeidYtelseGrunnlagRequest spesifikasjon,
                                                    GrunnlagReferanse grunnlagReferanse,
                                                    KoblingReferanse koblingReferanse) {
        if (grunnlagReferanse != null) {
            var grunnlag = iayTjeneste.hentAggregat(grunnlagReferanse);
            if (koblingReferanse != null) {
                var grunnlagsKoblingReferanse = iayTjeneste.hentKoblingReferanse(grunnlagReferanse);
                if (!Objects.equals(koblingReferanse, grunnlagsKoblingReferanse)) {
                    // returner kun angitt koblingReferanse i feilmelding, ikke den som er på grunnlag (sikkerhet).
                    throw new IllegalStateException("Angitt koblingreferanse matcher ikke grunnlag: " + koblingReferanse);
                }
            }
            return grunnlag;
        } else if (koblingReferanse != null) {
            return iayTjeneste.hentAggregat(koblingReferanse);
        }

        throw new UnsupportedOperationException("Må ha grunnlagReferanse eller koblingReferanse");
    }

    private class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (InntektArbeidYtelseGrunnlagRequest) obj;
            return AbacDataAttributter.opprett().leggTil(StandardAbacAttributtType.AKTØR_ID, req.getPerson().getIdent());
        }
    }
}
