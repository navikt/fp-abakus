package no.nav.foreldrepenger.abakus.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private static final Logger log = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    @Override
    public Response toResponse(JsonParseException exception) {
        log.warn("FP-299955 JSON-parsing feil: {}}", exception.getMessage(), exception);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(String.format("JSON-parsing feil: %s", exception.getMessage())))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }


}
