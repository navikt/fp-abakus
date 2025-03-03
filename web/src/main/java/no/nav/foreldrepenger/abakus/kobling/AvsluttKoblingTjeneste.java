package no.nav.foreldrepenger.abakus.kobling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.konfig.Environment;

@ApplicationScoped
public class AvsluttKoblingTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AvsluttKoblingTjeneste.class);

    private static final Environment ENV = Environment.current();

    private KoblingTjeneste koblingTjeneste;
    private InntektArbeidYtelseTjeneste iayTjeneste;

    AvsluttKoblingTjeneste() {
        // CDI
    }

    @Inject
    public AvsluttKoblingTjeneste(KoblingTjeneste koblingTjeneste,
                                  InntektArbeidYtelseTjeneste iayTjeneste) {
        this.koblingTjeneste = koblingTjeneste;
        this.iayTjeneste = iayTjeneste;
    }

    /**
     * Avslutter kobling og sletter inaktive grunnlag. Forutsetter at kobling finnes.
     * @param koblingReferanse - referanse til kobling
     * @param ytelseType - ytelseType for å fikse der det mangler ca 2500 koblinger fra 2018 og 2019.
     */
    public void avsluttKobling(KoblingReferanse koblingReferanse, YtelseType ytelseType) {
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var kobling = koblingTjeneste.hentFor(koblingReferanse).orElseThrow();

        LOG.info("Starter avslutting av kobling for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());

        // Midlertidig for å kunne fikse ytelse-type UNDEFINED.
        // Vi fikser manglende ytelseType på kobling
        if (YtelseType.UDEFINERT.equals(kobling.getYtelseType()) && ytelseType != null) {
            kobling.setYtelseType(ytelseType);
        }

        if (!ENV.isProd()) {
            iayTjeneste.slettInaktiveGrunnlagFor(kobling.getKoblingReferanse());
        }

        koblingTjeneste.deaktiver(kobling.getKoblingReferanse());

        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        LOG.info("Ferdig med avlutting av kobling for sak=[{}, {}] med behandling='{}'",
            kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse());
    }
}
