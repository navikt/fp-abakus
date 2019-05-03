package no.nav.foreldrepenger.abakus.kobling.kontroll;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.Stereotype;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * For å skille på implementasjoner av forskjellige
 */
@Qualifier
@Stereotype
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface YtelseTypeRef {

    /**
     * Kode-verdi som skiller ulike implementasjoner for ulike behandling typer.
     * <p>
     * Må matche ett innslag i <code>FAGSAK_YTELSE_TYPE</code> tabell for å kunne kjøres.
     *
     * @see no.nav.foreldrepenger.abakus.kodeverk.YtelseType
     */
    String value() default "*";

    /**
     * AnnotationLiteral som kan brukes ved CDI søk.
     */
    class FagsakYtelseTypeRefLiteral extends AnnotationLiteral<YtelseTypeRef> implements YtelseTypeRef {

        private String navn;

        public FagsakYtelseTypeRefLiteral(String navn) {
            this.navn = navn;
        }

        @Override
        public String value() {
            return navn;
        }

    }

}
