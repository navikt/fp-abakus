package no.nav.foreldrepenger.abakus.vedtak;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseEntitet;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.ytelse.Ytelse;
import no.nav.vedtak.ytelse.v1.YtelseV1;

@ProsessTask(LagreVedtakTask.TASKTYPE)
public class LagreVedtakTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "vedtakEvent.lagre";
    public static final String KEY = "kafka.key";
    private final static ObjectMapper OBJECT_MAPPER = JacksonJsonConfig.getMapper();

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
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
            mottattVedtak = OBJECT_MAPPER.readValue(payload, Ytelse.class);
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Ytelse>> violations = validator.validate(mottattVedtak);
            if (!violations.isEmpty()) {
                // Har feilet validering
                String allErrors = violations.stream().map(String::valueOf).collect(Collectors.joining("\\n"));
                throw new IllegalArgumentException("Vedtatt-ytelse valideringsfeil :: \n " + allErrors);
            }
        } catch (IOException e) {
            throw YtelseFeil.FACTORY.parsingFeil(key, payload, e).toException();
        }
        if (mottattVedtak != null) {
            // TODO: Gjør generisk
            //VedtakYtelseBuilder builder = extractor.extractFrom((YtelseV1) mottattVedtak);

            Optional<VedtakYtelseEntitet> siste = extractor.hentAktivtVedtakFor((YtelseV1)mottattVedtak);
            if (siste.isEmpty()) {
                siste = extractor.hentSisteVedtatteFor((YtelseV1) mottattVedtak);
            }
            VedtakYtelseBuilder builder = extractor.extractFrom((YtelseV1) mottattVedtak);
            
            ytelseRepository.lagre(builder, siste);
        }
    }
}
