package no.nav.foreldrepenger.abakus.vedtak;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import no.nav.abakus.iaygrunnlag.JsonObjectMapper;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@ProsessTask(value = "vedtakEvent.lagre", prioritet = 2)
public class LagreVedtakTask implements ProsessTaskHandler {

    public static final String KEY = "kafka.key";
    private final static ObjectMapper OBJECT_MAPPER = JsonObjectMapper.getMapper();

    private VedtakYtelseRepository ytelseRepository;
    private ExtractFromYtelseV1 extractor;

    public LagreVedtakTask() {
    }

    @Inject
    public LagreVedtakTask(VedtakYtelseRepository ytelseRepository, ExtractFromYtelseV1 extractor) {
        this.ytelseRepository = ytelseRepository;
        this.extractor = extractor;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        String key = data.getPropertyValue(KEY);
        String payload = data.getPayloadAsString();

        Ytelse mottattVedtak;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            mottattVedtak = OBJECT_MAPPER.readValue(payload, Ytelse.class);
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Ytelse>> violations = validator.validate(mottattVedtak);
            if (!violations.isEmpty()) {
                // Har feilet validering
                String allErrors = violations.stream().map(String::valueOf).collect(Collectors.joining("\\n"));
                throw new IllegalArgumentException("Vedtatt-ytelse valideringsfeil :: \n " + allErrors);
            }
        } catch (IOException e) {
            throw new TekniskException("FP-328773", String.format("Feil under parsing av vedtak. key={%s} payload={%s}", key, payload), e);
        }
        if (mottattVedtak != null) {
            // TODO: Gjør generisk
            final YtelseV1 mottattVedtak1 = (YtelseV1) mottattVedtak;
            VedtakYtelseBuilder builder = extractor.extractFrom(mottattVedtak1);

            ytelseRepository.lagre(builder);

        }
    }
}
