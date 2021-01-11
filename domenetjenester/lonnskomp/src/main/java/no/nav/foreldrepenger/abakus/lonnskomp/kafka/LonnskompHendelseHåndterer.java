package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.lonnskomp.LagreLønnskompensasjonTask;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonAnvist;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class LonnskompHendelseHåndterer {

    private static final Logger log = LoggerFactory.getLogger(LonnskompHendelseHåndterer.class);
    private final static ObjectMapper OBJECT_MAPPER = JacksonJsonConfig.getMapper();
    private LønnskompensasjonRepository repository;
    private ProsessTaskRepository prosessTaskRepository;


    public LonnskompHendelseHåndterer() {
    }

    @Inject
    public LonnskompHendelseHåndterer(LønnskompensasjonRepository repository, ProsessTaskRepository taskRepository) {
        this.repository = repository;
        this.prosessTaskRepository = taskRepository;
    }

    public void handleMessage(String key, String payload) {
        log.debug("Mottatt ytelse-vedtatt hendelse med key='{}', payload={}", key, payload);

        LønnskompensasjonVedtakMelding mottattVedtak;
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
            mottattVedtak = OBJECT_MAPPER.readValue(payload, LønnskompensasjonVedtakMelding.class);
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<LønnskompensasjonVedtakMelding>> violations = validator.validate(mottattVedtak);
            if (!violations.isEmpty()) {
                // Har feilet validering
                String allErrors = violations.stream().map(String::valueOf).collect(Collectors.joining("\\n"));
                throw new IllegalArgumentException("Vedtatt-ytelse valideringsfeil :: \n " + allErrors);
            }
        } catch (IOException e) {
            throw LønnskompensasjonFeil.FACTORY.parsingFeil(key, payload, e).toException();
        }
        if (mottattVedtak != null) {
            var sakId = mottattVedtak.getSakId() != null ? mottattVedtak.getSakId() : mottattVedtak.getId();
            var vedtak = extractFrom(mottattVedtak, sakId);
            if (skalLagreVedtak(vedtak)) {
                repository.lagre(vedtak);

                ProsessTaskData data = new ProsessTaskData(LagreLønnskompensasjonTask.TASKTYPE);
                data.setProperty(LagreLønnskompensasjonTask.SAK, sakId);
                prosessTaskRepository.lagre(data);
            }
        }
    }

    private LønnskompensasjonVedtak extractFrom(LønnskompensasjonVedtakMelding melding, String sakId) {
        var vedtak = new LønnskompensasjonVedtak();
        vedtak.setFnr(melding.getFnr());
        vedtak.setSakId(sakId);
        vedtak.setOrgNummer(new OrgNummer(melding.getBedriftNr()));
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(melding.getFom(), melding.getTom()));
        vedtak.setForrigeVedtakDato(melding.getForrigeVedtakDato());
        BigDecimal beløpBD = melding.getTotalKompensasjon();
        vedtak.setBeløp(new Beløp(beløpBD));
        Map<YearMonth, List<UtbetalingsdagMelding>> sortert = melding.getDagBeregninger().stream()
            .filter(b -> b.getLønnskompensasjonsbeløp() != null && !Objects.equals("-", b.getLønnskompensasjonsbeløp()))
            .collect(Collectors.groupingBy(b -> YearMonth.from(b.getDato())));
        sortert.forEach((k,v) -> {
            var sumDager = v.stream().map(UtbetalingsdagMelding::getLønnskompensasjonsbeløp).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sumDager.compareTo(BigDecimal.ZERO) > 0) {
                var anvist = LønnskompensasjonAnvist.LønnskompensasjonAnvistBuilder.ny()
                    .medBeløp(sumDager)
                    .medAnvistPeriode(IntervallEntitet.fraOgMedTilOgMed(k.atDay(1), k.atEndOfMonth()))
                    .build();
                vedtak.leggTilAnvistPeriode(anvist);
            }
        });
        return vedtak;
    }

    private boolean skalLagreVedtak(LønnskompensasjonVedtak vedtak) {
        LønnskompensasjonVedtak eksisterende = repository.hentSak(vedtak.getSakId()).orElse(null);
        if (eksisterende == null)
            return true;
        var skalLagres = (eksisterende.getForrigeVedtakDato() == null && vedtak.getForrigeVedtakDato() != null) ||
            (eksisterende.getForrigeVedtakDato() != null && vedtak.getForrigeVedtakDato() != null && vedtak.getForrigeVedtakDato().isAfter(eksisterende.getForrigeVedtakDato()));
        if (!skalLagres)
            log.info("Forkaster lønnskompensasjon siden en sitter på nyere vedtak. {} er eldre enn {}", vedtak, eksisterende);
        return skalLagres;
    }
}
