package no.nav.foreldrepenger.abakus.lonnskomp.kafka;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonRepository;
import no.nav.foreldrepenger.abakus.lonnskomp.domene.LønnskompensasjonVedtak;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.vedtak.json.JacksonJsonConfig;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class LonnskompHendelseHåndterer {

    private static final Logger log = LoggerFactory.getLogger(LonnskompHendelseHåndterer.class);
    private final static ObjectMapper OBJECT_MAPPER = JacksonJsonConfig.getMapper();
    private AktørTjeneste aktørTjeneste;
    private LønnskompensasjonRepository repository;


    public LonnskompHendelseHåndterer() {
    }

    @Inject
    public LonnskompHendelseHåndterer(AktørTjeneste aktørTjeneste, LønnskompensasjonRepository repository) {
        this.aktørTjeneste = aktørTjeneste;
        this.repository = repository;
    }

    void handleMessage(String key, String payload) {
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
            var vedtak = extractFrom(mottattVedtak);
            repository.lagre(vedtak);
        }
    }

    private LønnskompensasjonVedtak extractFrom(LønnskompensasjonVedtakMelding melding) {
        var vedtak = new LønnskompensasjonVedtak();
        vedtak.setAktørId(aktørTjeneste.hentAktørForIdent(new PersonIdent(melding.getFnr()), YtelseType.FORELDREPENGER)
            .orElseThrow(() -> LønnskompensasjonFeil.FACTORY.finnerIkkeAktørIdForPermittert().toException()));
        vedtak.setSakId(melding.getSakId());
        vedtak.setOrgNummer(new OrgNummer(melding.getBedriftNr()));
        vedtak.setPeriode(IntervallEntitet.fraOgMedTilOgMed(LocalDate.parse(melding.getFom(), DateTimeFormatter.ISO_LOCAL_DATE), LocalDate.parse(melding.getTom(), DateTimeFormatter.ISO_LOCAL_DATE)));
        vedtak.setForrigeVedtakDato(melding.getForrigeVedtakDato() != null ? LocalDate.parse(melding.getForrigeVedtakDato(), DateTimeFormatter.ISO_LOCAL_DATE) : null);
        BigDecimal beløpBD = new BigDecimal(melding.getTotalKompensasjon());
        vedtak.setBeløp(new Beløp(beløpBD));

        return vedtak;
    }
}
