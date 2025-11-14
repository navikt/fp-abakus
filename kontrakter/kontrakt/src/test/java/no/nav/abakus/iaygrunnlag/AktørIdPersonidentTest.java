package no.nav.abakus.iaygrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AktørIdPersonidentTest {

    @Test
    void toString_not_logging_aktør() {
        var aktørId = "1234567890123";
        var personident = new AktørIdPersonident(aktørId);
        assertThat(personident.getIdent()).isEqualTo(aktørId);
        assertThat(personident.toString()).startsWith("********").doesNotContain(aktørId).endsWith("<AKTØRID>");
    }

}
