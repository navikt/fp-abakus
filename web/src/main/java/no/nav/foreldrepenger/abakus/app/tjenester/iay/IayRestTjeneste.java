package no.nav.foreldrepenger.abakus.app.tjenester.iay;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseRepository;
import no.nav.vedtak.felles.jpa.Transaction;

@Api(tags = {"iay"})
@Path("/iay")
@Transaction
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class IayRestTjeneste {

    private InntektArbeidYtelseRepository inntektArbeidYtelseRepository;

    public IayRestTjeneste() {
        // Bare for RESTeasy
    }
    
    @Inject
    public IayRestTjeneste(InntektArbeidYtelseRepository inntektArbeidYtelseRepository) {
        this.inntektArbeidYtelseRepository = inntektArbeidYtelseRepository;
    }
    
    
    
}
