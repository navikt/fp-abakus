package no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.iay;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.Validation;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittEgenNæringDto;

class IAYValideringTest {

    @Test
    void test_virksomhetnavn() throws Exception {
        var dto = new OppgittEgenNæringDto(new Periode(LocalDate.now(), LocalDate.now()));
        dto.setVirksomhetNavn("hello$£@£$1@£€6{[,æ'\"?`/$£æøåÆØÅ12340ø");
        validateResult(dto, true);
    }

    @Test
    void test_feil_bruttoinntekt() throws Exception {
        var dto = new OppgittEgenNæringDto(new Periode(LocalDate.now(), LocalDate.now()));
        dto.setBruttoInntekt(BigDecimal.valueOf(-1));
        validateResult(dto, false);
    }

    private void validateResult(Object roundTripped, boolean expectOk) {
        assertThat(roundTripped).isNotNull();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(roundTripped);
            if (expectOk) {
                assertThat(violations).isEmpty();
            } else {
                assertThat(violations).isNotEmpty();

            }
        }
    }
}
