package no.nav.foreldrepenger.abakus.app.exceptions;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

class FeltValideringFeil {

    private FeltValideringFeil() {}

    static FunksjonellException feltverdiKanIkkeValideres(List<String> feltnavn) {
        return new FunksjonellException(
                "FP-328673",
                String.format(
                        "Det oppstod en valideringsfeil på felt %s. Vennligst kontroller at alle feltverdier er korrekte.",
                        feltnavn),
                "Kontroller at alle feltverdier er korrekte");
    }

    static TekniskException feilUnderValideringAvContraints(ConstraintViolationException feltnavn) {
        return new TekniskException(
                "FP-232342", "Det oppsto en teknisk feil under validering av contraints.", feltnavn);
    }
}
