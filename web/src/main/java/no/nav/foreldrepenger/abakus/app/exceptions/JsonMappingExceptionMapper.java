package no.nav.foreldrepenger.abakus.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        LOG.warn("FP-252294 JSON-mapping feil: {}", exception.getMessage(), exception);
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(new FeilDto(String.format("JSON-mapping feil: %s", exception.getMessage()), FeilType.GENERELL_FEIL))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }


}
