package no.nav.abakus.iaygrunnlag;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.InjectableValues.Std;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.abakus.iaygrunnlag.kodeverk.KodeValidator;

public class JsonObjectMapper {

    private static final ObjectMapper OM;

    static {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        Std std = new InjectableValues.Std();
        std.addValue(KodeValidator.class, KodeValidator.HAPPY_VALIDATOR);
        objectMapper.setInjectableValues(std);
        OM = objectMapper;
    }

    public static ObjectMapper getMapper() {
        return OM;
    }

    public static String getJson(Object object) throws IOException {
        Writer jsonWriter = new StringWriter();
        OM.writerWithDefaultPrettyPrinter().writeValue(jsonWriter, object);
        jsonWriter.flush();
        return jsonWriter.toString();
    }


    /** Lag ObjectMapper med egen-definet kode-validator. */
    @Deprecated(forRemoval = true)
    public static ObjectMapper getMapper(KodeValidator validator) {
        Std std = new InjectableValues.Std();
        std.addValue(KodeValidator.class, validator);
        ObjectMapper objectMapper = OM.copy();
        objectMapper.setInjectableValues(std);
        return objectMapper;
    }

}
