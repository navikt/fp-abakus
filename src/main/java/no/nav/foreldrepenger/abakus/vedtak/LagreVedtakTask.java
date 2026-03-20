package no.nav.foreldrepenger.abakus.vedtak;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseBuilder;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelseRepository;
import no.nav.foreldrepenger.abakus.vedtak.extract.v1.ExtractFromYtelseV1;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ApplicationScoped
@ProsessTask(value = "vedtakEvent.lagre", prioritet = 2)
public class LagreVedtakTask implements ProsessTaskHandler {

    public static final String KEY = "kafka.key";

    private VedtakYtelseRepository ytelseRepository;
    private ExtractFromYtelseV1 extractor;
    private ProsessTaskTjeneste prosessTaskTjeneste;

    public LagreVedtakTask() {
    }

    @Inject
    public LagreVedtakTask(VedtakYtelseRepository ytelseRepository, ExtractFromYtelseV1 extractor, ProsessTaskTjeneste prosessTaskTjeneste) {
        this.ytelseRepository = ytelseRepository;
        this.extractor = extractor;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        String key = data.getPropertyValue(KEY);
        String payload = data.getPayloadAsString();

        Ytelse mottattVedtak;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            mottattVedtak = DefaultJsonMapper.fromJson(payload, Ytelse.class);
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Ytelse>> violations = validator.validate(mottattVedtak);
            if (!violations.isEmpty()) {
                // Har feilet validering
                String allErrors = violations.stream().map(String::valueOf).collect(Collectors.joining("\\n"));
                throw new IllegalArgumentException("Vedtatt-ytelse valideringsfeil :: \n " + allErrors);
            }
        }
        if (mottattVedtak != null) {
            // TODO: Gjør generisk
            final YtelseV1 mottattVedtak1 = (YtelseV1) mottattVedtak;
            VedtakYtelseBuilder builder = extractor.extractFrom(mottattVedtak1);

            ytelseRepository.lagre(builder);

            var publiser = lagPubliserTaskData(mottattVedtak1, key);
            prosessTaskTjeneste.lagre(publiser);
        }
    }

    private static ProsessTaskData lagPubliserTaskData(YtelseV1 mottattVedtak, String key) {
        var publiser = ProsessTaskData.forProsessTask(PubliserVedtakTask.class);
        publiser.setProperty(PubliserVedtakTask.KEY, Optional.ofNullable(mottattVedtak.getSaksnummer()).orElse(key));
        if (mottattVedtak.getAktør().erAktørId()) {
            publiser.setProperty(PubliserVedtakTask.AKTØRID, mottattVedtak.getAktør().getVerdi());
        } else {
            publiser.setProperty(PubliserVedtakTask.IDENT, mottattVedtak.getAktør().getVerdi());
        }
        publiser.setProperty(PubliserVedtakTask.TEMA, utledTema(mottattVedtak.getYtelse()));
        return publiser;
    }

    private static String utledTema(Ytelser ytelse) {
        return switch (ytelse) {
            case ENGANGSTØNAD, FORELDREPENGER, SVANGERSKAPSPENGER -> "FOR";
            case PLEIEPENGER_SYKT_BARN, PLEIEPENGER_NÆRSTÅENDE, OMSORGSPENGER, OPPLÆRINGSPENGER -> "OMS";
            case FRISINN -> "FRI";
        };
    }
}
