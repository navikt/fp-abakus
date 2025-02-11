package no.nav.foreldrepenger.abakus.app.exceptions;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.abakus.app.exceptions.FeilType.GENERELL_FEIL;

public record FeilDto(FeilType type, String feilmelding, Collection<FeltFeilDto> feltFeil) {

    public FeilDto(String feilmelding, FeilType type) {
        this(type, feilmelding, emptyList());
    }

    public FeilDto(FeilType type, String feilmelding) {
        this(type, feilmelding, emptyList());
    }

    public FeilDto(String feilmelding, Collection<FeltFeilDto> feltFeil) {
        this(GENERELL_FEIL, feilmelding, feltFeil);
    }

    public FeilDto(String feilmelding) {
        this(GENERELL_FEIL, feilmelding, emptyList());
    }

}
