package no.nav.foreldrepenger.abakus.kodeverk;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.metamodel.ManagedType;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;

/**
 * Genererer kodeverk til bruk i enhetstester ved bruk av AbstractTestScenario.
 */
public class KodeverkTilJsonProducerTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private final EntityManager em = repoRule.getEntityManager();

    public static File getOutputDir() {
        File currentDir = new File(".");

        File path = new File(currentDir, "target/test-classes");
        while (!path.exists() && currentDir.getParentFile() != null) {
            currentDir = currentDir.getParentFile();
            path = new File(currentDir, "target/test-classes");
        }

        return path;

    }

    @Test
    public void skal_dumpe_kodeverk_til_json_format_for_bruk_i_scenario_tester() throws Exception {

        Map<Class<?>, Object> dump = new TreeMap<>(Comparator.comparing(Class::getName));
        Set<Class<?>> classes = new LinkedHashSet<>();

        var emf = em.getEntityManagerFactory();

        classes.addAll(getEntityClasses(emf, Kodeliste.class::isAssignableFrom));
        classes.addAll(getEntityClasses(emf, KodeverkTabell.class::isAssignableFrom));

        classes.forEach(c -> dump.put(c, getDump(c, " order by kode")));

        Condition<Class<?>> alwaysTrue = new Condition<>(c -> true, "");
        Assertions.assertThat(classes).haveAtLeast(1, alwaysTrue); // kun for å sjekk at vi i det minste finner noe
        writeToFile(dump);

    }

    private static Set<Class<?>> getEntityClasses(EntityManagerFactory emf, Predicate<Class<?>> filter) {
        Set<ManagedType<?>> managedTypes = emf.getMetamodel().getManagedTypes();
        return managedTypes.stream().map(javax.persistence.metamodel.Type::getJavaType).filter(c -> !Modifier.isAbstract(c.getModifiers())).filter(filter).collect(Collectors.toSet());
    }

    private List<?> getDump(Class<?> cls, String suffix) {
        Query query = em.createQuery("from " + cls.getName() + suffix);
        return query.getResultList();
    }

    private void writeToFile(Map<Class<?>, Object> dump) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        om.registerModule(new MyModule());
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        ObjectWriter objectWriter = om.writerWithDefaultPrettyPrinter();

        File outputDir = getOutputDir();
        for (Map.Entry<Class<?>, Object> entry : dump.entrySet()) {

            if (!entry.getKey().isAnnotationPresent(DiscriminatorValue.class)) {
                if (entry.getKey().isAnnotationPresent(Entity.class)) {
                    String name = entry.getKey().getAnnotation(Entity.class).name();
                    writeKodeverk(objectWriter, outputDir, entry.getValue(), name);
                } else {
                    System.out.println("Mangler @Entity eller @Discriminator:" + entry.getKey());
                }
            } else {
                String name = entry.getKey().getAnnotation(DiscriminatorValue.class).value();
                writeKodeverk(objectWriter, outputDir, entry.getValue(), name);
            }
        }
    }

    private void writeKodeverk(ObjectWriter objectWriter, File outputDir, Object value, String name)
            throws IOException {
        File outputFile = new File(outputDir, KodeverkFraJson.FILE_NAME_PREFIX + name + KodeverkFraJson.FILE_NAME_SUFFIX);
        outputFile.delete();
        objectWriter.writeValue(outputFile, value);
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@kode")
    @JsonIgnoreProperties({"endretAv", "opprettetAv", "opprettetTidspunkt", "endretTidspunkt", "id", "gyldigTilOgMed",
            "gyldigFraOgMed", "displayNavn", "beskrivelse"})
    public static class PropertyFilterKodeverkTabellMixIn {
    }

    @JsonIgnoreProperties({"endretAv", "opprettetAv", "opprettetTidspunkt", "endretTidspunkt", "id", "gyldigTilOgMed",
            "gyldigFraOgMed", "displayNavn", "beskrivelse"})
    public static class PropertyFilterMixIn {
    }

    public class MyModule extends SimpleModule {
        @SuppressWarnings("deprecation")
        public MyModule() {
            super("ModuleName", new Version(0, 0, 1, null));
        }

        @Override
        public void setupModule(SetupContext context) {
            context.setMixInAnnotations(Kodeliste.class, PropertyFilterMixIn.class);
            context.setMixInAnnotations(KodeverkTabell.class, PropertyFilterKodeverkTabellMixIn.class);
        }
    }

}
