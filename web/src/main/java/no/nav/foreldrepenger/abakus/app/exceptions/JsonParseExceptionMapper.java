package no.nav.foreldrepenger.abakus.app.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    @Override
    public Response toResponse(JsonParseException exception) {
        LOG.warn("FP-299955 JSON-parsing feil: {}}", exception.getMessage(), exception);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(String.format("JSON-parsing feil: %s", exception.getMessage())))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
