package no.nav.abakus.callback.registerdata;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Unik referanse representert som UUID
 */
public class ReferanseDto {

    public static final String UUID_REGEX = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";

    @NotNull
    @Valid
    private UUID referanse;

    public UUID getReferanse() {
        return referanse;
    }

    public void setReferanse(UUID referanse) {
        this.referanse = referanse;
    }
}
