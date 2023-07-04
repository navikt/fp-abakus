package no.nav.foreldrepenger.abakus.app.vedlikehold;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.vedtak.ytelse.Aktør;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


/**
 * Input request for å bytte en utgått aktørid med en aktiv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class ByttAktørRequest {

    @JsonProperty(value = "utgatt", required = true)
    @NotNull
    @Valid
    private Aktør utgåttAktør;

    @JsonProperty(value = "gyldig", required = true)
    @NotNull
    @Valid
    private Aktør gyldigAktør;

    public ByttAktørRequest() {
        // Jackson
    }

    public Aktør getUtgåttAktør() {
        return utgåttAktør;
    }

    public Aktør getGyldigAktør() {
        return gyldigAktør;
    }
}
