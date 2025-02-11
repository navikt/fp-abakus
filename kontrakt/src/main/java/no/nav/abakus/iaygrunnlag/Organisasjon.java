package no.nav.abakus.iaygrunnlag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class Organisasjon extends Aktør {
    static final String IDENT_TYPE = "ORGNUMMER";

    @JsonProperty(value = "ident", required = true)
    @NotNull
    @Pattern(regexp = "^\\d{9}+$", message = "orgnr [${validatedValue}] har ikke gyldig verdi (9 siffer)")
    private String ident;

    @JsonCreator
    public Organisasjon(@JsonProperty(value = "ident", required = true) String kode) {
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

    @Override
    public boolean getErOrganisasjon() {
        return true;
    }

    @Override
    public boolean getErPerson() {
        return false;
    }
}
