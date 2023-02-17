package no.nav.foreldrepenger.abakus.app.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;

import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonMappingExceptionMapperTest {


    @Test
    public void skal_mappe_InvalidTypeIdException() {
        JsonMappingExceptionMapper mapper = new JsonMappingExceptionMapper();
        @SuppressWarnings("resource") Response resultat = mapper.toResponse(new InvalidTypeIdException(null, "Ukjent type-kode", null, "23525"));
        FeilDto dto = (FeilDto) resultat.getEntity();
        assertThat(dto.feilmelding()).isEqualTo("JSON-mapping feil: Ukjent type-kode");
        assertThat(dto.feltFeil()).isEmpty();
    }
}
