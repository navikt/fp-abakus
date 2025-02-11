package no.nav.abakus.iaygrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrganisasjonTest {

    @Test
    void toString_not_logging_orgnr() {
        var orgnr = "12345678901";
        var personident = new Organisasjon(orgnr);
        assertThat(personident.getIdent()).isEqualTo(orgnr);
        assertThat(personident.toString()).startsWith("*******").doesNotContain(orgnr).endsWith("<ORGNUMMER>");
    }
}
