package no.nav.foreldrepenger.abakus.app.exceptions;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import no.nav.vedtak.exception.VLException;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintViolationMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Collection<FeltFeilDto> feilene = new ArrayList<>();

        var constraintViolations = exception.getConstraintViolations();
        for (var constraintViolation : constraintViolations) {
            var feltNavn = getFeltNavn(constraintViolation.getPropertyPath());
            feilene.add(new FeltFeilDto(feltNavn, constraintViolation.getMessage()));
        }
        VLException feil;
        if (feilene.isEmpty()) {
            feil = FeltValideringFeil.feilUnderValideringAvContraints(exception);
        } else {
            var feltNavn = feilene.stream().map(FeltFeilDto::toString).toList();
            feil = FeltValideringFeil.feltverdiKanIkkeValideres(feltNavn);
        }
        LOG.warn(feil.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(new FeilDto(feil.getMessage(), feilene)).type(MediaType.APPLICATION_JSON).build();
    }

    private String getFeltNavn(Path propertyPath) {
        return propertyPath instanceof PathImpl pi ? pi.getLeafNode().toString() : null;
    }

}
