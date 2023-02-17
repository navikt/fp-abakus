package no.nav.foreldrepenger.abakus.app;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.abakus.app.jackson.JacksonJsonConfig;

public class SjekkDtoStrukturTest {

    private static final List<Class<?>> KONTRAKT_LOKASJONER = JacksonJsonConfig.getKontraktLokasjoner();

    private static final List<String> SKIPPED = Arrays.asList("class", "kode");

    public static Stream<Arguments> provideArguments() throws URISyntaxException {
        List<Arguments> params = new ArrayList<>();

        // avled code location fra klassene
        KONTRAKT_LOKASJONER.stream().map(c -> {
            try {
                return c.getProtectionDomain().getCodeSource().getLocation().toURI();
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Ikke en URI for klasse: " + c, e);
            }
        }).distinct().forEach(uri -> {
            IndexClasses.getIndexFor(uri)
                .getClasses(ci -> ci.name().toString().endsWith("Dto"), c -> !c.isInterface())
                .stream()
                .forEach(c -> params.add(Arguments.of(c.getName(), c)));

        });

        return params.stream();
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    public void skal_ha_riktig_navn_på_properties_i_dto_eller_konfiguret_med_annotations(Class<?> cls) throws Exception {
        sjekkJsonProperties(cls);
    }

    private void sjekkJsonProperties(Class<?> c) throws IntrospectionException {
        List<Field> fields = Arrays.asList(c.getDeclaredFields());
        Set<String> fieldNames = fields.stream()
            .filter(f -> !f.isSynthetic() && !Modifier.isStatic(f.getModifiers()))
            .filter(f -> f.getAnnotation(JsonProperty.class) == null)
            .filter(f -> f.getAnnotation(JsonValue.class) == null)
            .filter(f -> f.getAnnotation(JsonIgnore.class) == null)
            .map(f -> f.getName())
            .collect(Collectors.toSet());

        if (!fieldNames.isEmpty()) {
            for (PropertyDescriptor prop : Introspector.getBeanInfo(c, c.getSuperclass()).getPropertyDescriptors()) {
                if (prop.getReadMethod() != null) {
                    Method readName = prop.getReadMethod();
                    String propName = prop.getName();
                    if (!SKIPPED.contains(propName)) {
                        if (readName.getAnnotation(JsonIgnore.class) == null && readName.getAnnotation(JsonProperty.class) == null) {
                            Assertions.assertThat(propName)
                                .as("Gettere er ikke samstemt med felt i klasse, sørg for matchende bean navn og return type eller bruk @JsonProperty/@JsonIgnore/@JsonValue til å sette navn for json struktur: "
                                    + c.getName())
                                .isIn(fieldNames);
                        }
                    }
                }

                if (prop.getWriteMethod() != null) {
                    Method readName = prop.getWriteMethod();
                    String propName = prop.getName();
                    if (!SKIPPED.contains(propName)) {
                        if (readName.getAnnotation(JsonIgnore.class) == null && readName.getAnnotation(JsonProperty.class) == null) {
                            Assertions.assertThat(propName)
                                .as("Settere er ikke samstemt med felt i klasse, sørg for matchende bean navn og return type eller bruk @JsonProperty/@JsonIgnore/@JsonValue til å sette navn for json struktur: "
                                    + c.getName())
                                .isIn(fieldNames);
                        }
                    }
                }
            }
        }
    }

}
