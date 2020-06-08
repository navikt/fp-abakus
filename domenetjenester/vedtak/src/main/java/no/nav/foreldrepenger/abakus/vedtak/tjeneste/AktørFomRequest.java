package no.nav.foreldrepenger.abakus.vedtak.tjeneste;

import java.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.vedtak.konfig.Tid;


/**
 * Input request strutkur for en Aktør for en periode (evt. dato)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class AktørFomRequest {

    @JsonProperty(value = "aktør", required = true)
    @NotNull
    @Valid
    private Aktør aktør;

    @JsonProperty(value = "fom")
    private LocalDate fom;

    @JsonCreator
    public AktørFomRequest(@JsonProperty(value = "aktør", required = true) @NotNull @Valid Aktør aktør,
                           @JsonProperty(value = "fom") LocalDate fom) {
        this.aktør = aktør;
        this.fom = fom;
    }

    private AktørFomRequest() {
    }

    public Aktør getAktør() {
        return aktør;
    }

    public LocalDate getFom() {
        return fom;
    }

    public LocalDate getFomNonNull() {
        return fom != null ? fom : Tid.TIDENES_BEGYNNELSE;
    }

}
