package no.nav.foreldrepenger.abakus.app.jackson;

import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import no.nav.abakus.iaygrunnlag.kodeverk.KodeValidator;
import no.nav.foreldrepenger.abakus.app.IndexClasses;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;


    /** Default instance for Jax-rs application. Genererer ikke navn som del av output for kodeverk. */
    public JacksonJsonConfig() {
        this(false);
    }

    public JacksonJsonConfig(boolean serialiserKodelisteNavn) {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // TODO (u139158): PK-44270 Diskutere med Front-end, ønsker i utgangpunktet å fjerne null, men hva med Javascript
        // KodelisteSerializer og KodeverkSerializer bør i tilfelle også støtte JsonInclude.Include.*
        // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(createModule(serialiserKodelisteNavn));
        InjectableValues.Std std = new InjectableValues.Std();
        std.addValue(KodeValidator.class, KodeValidator.HAPPY_VALIDATOR);
        objectMapper.setInjectableValues(std);

        objectMapper.registerSubtypes(getJsonTypeNameClasses());
    }

    private static SimpleModule createModule(boolean serialiserKodelisteNavn) {
        SimpleModule module = new SimpleModule("VL-REST", new Version(1, 0, 0, null, null, null));

        addSerializers(module, serialiserKodelisteNavn);

        return module;
    }

    private static void addSerializers(SimpleModule module, boolean serialiserKodelisteNavn) {
        module.addSerializer(new KodelisteSerializer(serialiserKodelisteNavn));
        module.addSerializer(new StringSerializer());
    }

    /**
     * Scan subtyper dynamisk fra WAR slik at superklasse slipper å deklarere @JsonSubtypes.
     */
    public static List<Class<?>> getJsonTypeNameClasses() {
        Class<JacksonJsonConfig> cls = JacksonJsonConfig.class;
        IndexClasses indexClasses;
        try {
            indexClasses = IndexClasses.getIndexFor(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
            return indexClasses.getClassesWithAnnotation(JsonTypeName.class);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Kunne ikke konvertere CodeSource location til URI", e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }

}
