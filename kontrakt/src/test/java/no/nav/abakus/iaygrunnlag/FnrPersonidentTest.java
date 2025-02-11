package no.nav.abakus.iaygrunnlag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FnrPersonidentTest {

    @Test
    void toString_not_logging_fnr() {
        var fnr = "12345678901";
        var personident = new FnrPersonident(fnr);
        assertThat(personident.getIdent()).isEqualTo(fnr);
        assertThat(personident.toString()).startsWith("*******").doesNotContain(fnr).endsWith("<FNR>");
    }
}
