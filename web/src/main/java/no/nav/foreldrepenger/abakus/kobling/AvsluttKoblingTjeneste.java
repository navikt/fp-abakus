package no.nav.foreldrepenger.abakus.kobling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;

@ApplicationScoped
public class AvsluttKoblingTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AvsluttKoblingTjeneste.class);

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
     *
     * @param koblingReferanse - referanse til kobling
     */
    public void avsluttKobling(KoblingReferanse koblingReferanse) {
        var koblingLås = koblingTjeneste.taSkrivesLås(koblingReferanse);
        var kobling = koblingTjeneste.hentFor(koblingReferanse).orElseThrow();

        LOG.info("Starter avslutting av kobling for sak=[{}, {}] med behandling='{}'", kobling.getSaksnummer(), kobling.getYtelseType(),
            kobling.getKoblingReferanse());

        iayTjeneste.slettInaktiveGrunnlagFor(kobling.getKoblingReferanse());
        koblingTjeneste.deaktiver(kobling.getKoblingReferanse());
        koblingTjeneste.oppdaterLåsVersjon(koblingLås);

        LOG.info("Ferdig med avlutting av kobling for sak=[{}, {}] med behandling='{}'",
            kobling.getSaksnummer(), kobling.getYtelseType(), kobling.getKoblingReferanse());
    }
}
