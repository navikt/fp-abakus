package no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.rest.felles;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.vedtak.felles.integrasjon.infotrygd.grunnlag.v1.respons.Grunnlag;

public class JsonConverter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<Grunnlag> grunnlagBarnResponse(String json) {
        return convert(json, new TypeReference<>() {
        });
    }


    private <T> T convert(String json, TypeReference<T> typeReference) {
        try {
            return JsonObjectMapper.getMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("Feil ved konvertering fra JSON", e);
            throw new IllegalStateException("Feil ved konvertering fra JSON", e);
        }
    }
}
