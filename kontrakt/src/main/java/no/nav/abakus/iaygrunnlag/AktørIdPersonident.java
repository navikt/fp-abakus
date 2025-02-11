package no.nav.abakus.iaygrunnlag;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktørIdPersonident extends PersonIdent {
    public static final String IDENT_TYPE = "AKTØRID";

    @JsonProperty(value = "ident", required = true)
    @NotNull
    @Pattern(regexp = "^\\d{13}+$", message = "aktørId [${validatedValue}] har ikke gyldig verdi (13 siffer)")
    private String ident;

    @JsonCreator
    public AktørIdPersonident(@JsonProperty(value = "ident", required = true) String kode) {
        this.ident = kode;
    }

    @Override
    public String getIdent() {
        return ident;
    }

    @Override
    public String getIdentType() {
        return IDENT_TYPE;
    }
}
