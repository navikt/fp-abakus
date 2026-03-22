package no.nav.foreldrepenger.abakus.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;

public class RestApiTester {

    static final List<Class<?>> UNNTATT = Collections.singletonList(OpenApiResource.class);

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    liste.add(method);
                }
            }
        }
        return liste;
    }

    private static Collection<Class<?>> finnAlleRestTjenester() {
        var klasser = new ArrayList<>(finnAlleRestTjenester(new ApiConfig()));
        klasser.addAll(finnAlleRestTjenester(new EksternApiConfig()));
        klasser.addAll(finnAlleRestTjenester(new ForvaltningApiConfig()));
        return klasser;
    }

    private static Collection<Class<?>> finnAlleRestTjenester(Application config) {
        return config.getClasses().stream()
            .filter(c -> c.getAnnotation(Path.class) != null)
            .filter(c -> !UNNTATT.contains(c))
            .toList();
    }
}
