package no.nav.foreldrepenger.kontrakter.iaygrunnlag.v1.iay;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.Validation;
import no.nav.abakus.iaygrunnlag.v1.InntektArbeidYtelseGrunnlagDto;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

class IAYMigreringNPETest {

    public static Stream<Arguments> provideArguments() throws URISyntaxException {
        return Stream.of("/iay_case_001.json", "/iay_case_002.json", "/iay_case_003.json", "/iay_case_004.json", "/iay_case_005.json")
            .map(v -> Arguments.of(v));
    }

    @ParameterizedTest
    @MethodSource("provideArguments")
    void test_file(String fileName) throws Exception {

        String str = readTestCase(fileName);

        InntektArbeidYtelseGrunnlagDto dto = DefaultJsonMapper.fromJson(str, InntektArbeidYtelseGrunnlagDto.class);
        assertThat(dto).isNotNull();
        assertThat(dto.getPerson()).isNotNull();

        validateResult(dto);

    }

    private String readTestCase(String fileName) throws IOException {
        String str = null;
        try (var is = getClass().getResourceAsStream(fileName); var s = new java.util.Scanner(is, StandardCharsets.UTF_8); Scanner s2 = s.useDelimiter("\\A")) {

            str = s2.hasNext() ? s2.next() : "";
        }
        return str;
    }

    private void validateResult(Object roundTripped) {
        assertThat(roundTripped).isNotNull();
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var violations = validator.validate(roundTripped);
            assertThat(violations).isEmpty();
        }
    }
}
