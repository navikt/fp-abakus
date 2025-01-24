package no.nav.foreldrepenger.abakus.kobling.kontroll;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Stereotype;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

/** For å skille på implementasjoner av forskjellige */
@Repeatable(YtelseTypeRef.ContainerOfYtelseTypeRef.class)
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface YtelseTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike behandling typer.
     *
     * <p>Må matche ett innslag i <code>FAGSAK_YTELSE_TYPE</code> tabell for å kunne kjøres.
     *
     * @see no.nav.abakus.iaygrunnlag.kodeverk.YtelseType
     */
    YtelseType value() default YtelseType.UDEFINERT;

    /**
     * container for repeatable annotations.
     *
     * @see https://docs.oracle.com/javase/tutorial/java/annotations/repeating.html
     */
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
    @Documented
    public @interface ContainerOfYtelseTypeRef {
        YtelseTypeRef[] value();
    }

    /** AnnotationLiteral som kan brukes ved CDI søk. */
    class YtelseTypeRefLiteral extends AnnotationLiteral<YtelseTypeRef> implements YtelseTypeRef {

        private YtelseType navn;

        public YtelseTypeRefLiteral(YtelseType navn) {
            this.navn = navn;
        }

        @Override
        public YtelseType value() {
            return navn == null ? YtelseType.UDEFINERT : navn;
        }
    }

    @SuppressWarnings("unchecked")
    public static final class Lookup {

        private Lookup() {}

        public static <I> Optional<I> find(Class<I> cls, YtelseType ytelseTypeKode) {
            return find(cls, (CDI<I>) CDI.current(), ytelseTypeKode);
        }

        /**
         * Kan brukes til å finne instanser blant angitte som matcher følgende kode, eller default '*' implementasjon.
         * Merk at Instance bør være injected med riktig forventet klassetype og @Any qualifier.
         */
        public static <I> Optional<I> find(Instance<I> instances, YtelseType ytelseTypeKode) {
            return find(null, instances, ytelseTypeKode);
        }

        public static <I> List<Instance<I>> list(Class<I> cls, Instance<I> instances, YtelseType ytelseTypeKode) {
            Objects.requireNonNull(instances, "instances");

            final List<Instance<I>> resultat = new ArrayList<>();
            Consumer<YtelseType> search = (YtelseType s) -> {
                var inst = select(cls, instances, new YtelseTypeRefLiteral(s));
                if (inst.isUnsatisfied()) {
                    return;
                }
                resultat.add(inst);
            };

            search.accept(ytelseTypeKode);
            search.accept(YtelseType.UDEFINERT); // finn default
            return List.copyOf(resultat);
        }

        public static <I> Optional<I> find(Class<I> cls, Instance<I> instances, YtelseType ytelseTypeKode) {
            Objects.requireNonNull(instances, "instances");

            for (var fagsakLiteral : coalesce(ytelseTypeKode, YtelseType.UDEFINERT)) {
                var inst = select(cls, instances, new YtelseTypeRef.YtelseTypeRefLiteral(fagsakLiteral));
                if (inst.isResolvable()) {
                    return Optional.of(getInstance(inst));
                } else {
                    if (inst.isAmbiguous()) {
                        throw new IllegalStateException("Har flere matchende instanser for klasse : " + cls.getName()
                                + ", fagsakType=" + fagsakLiteral);
                    }
                }
            }

            return Optional.empty();
        }

        private static <I> Instance<I> select(Class<I> cls, Instance<I> instances, Annotation anno) {
            return cls != null ? instances.select(cls, anno) : instances.select(anno);
        }

        private static <I> I getInstance(Instance<I> inst) {
            var i = inst.get();
            if (i.getClass().isAnnotationPresent(Dependent.class)) {
                throw new IllegalStateException(
                        "Kan ikke ha @Dependent scope bean ved Instance lookup dersom en ikke også håndtere lifecycle selv: "
                                + i.getClass());
            }
            return i;
        }

        private static List<YtelseType> coalesce(YtelseType... vals) {
            return Arrays.stream(vals).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }
    }
}
