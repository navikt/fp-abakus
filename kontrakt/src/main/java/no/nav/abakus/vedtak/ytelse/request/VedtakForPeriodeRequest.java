package no.nav.abakus.vedtak.ytelse.request;

import java.util.Objects;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Ytelser;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_ABSENT, content = JsonInclude.Include.NON_EMPTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE, creatorVisibility = JsonAutoDetect.Visibility.NONE)
public class VedtakForPeriodeRequest {

    public static final Set<Ytelser> ALLE_YTELSER = Set.of(Ytelser.FORELDREPENGER, Ytelser.SVANGERSKAPSPENGER,
        Ytelser.PLEIEPENGER_SYKT_BARN, Ytelser.PLEIEPENGER_NÆRSTÅENDE, Ytelser.OPPLÆRINGSPENGER, Ytelser.OMSORGSPENGER,
        Ytelser.FRISINN);

    @JsonProperty(value = "ident", required = true)
    @NotNull
    @Valid
    private Aktør ident;

    @JsonProperty(value = "periode", required = true)
    @NotNull
    @Valid
    private Periode periode;

    @JsonProperty(value = "ytelser")
    private Set<Ytelser> ytelser = ALLE_YTELSER;

    private VedtakForPeriodeRequest() {
    }

    public VedtakForPeriodeRequest(Aktør ident, Periode periode) {
        this.ident = Objects.requireNonNull(ident);
        this.periode = Objects.requireNonNull(periode);
    }

    public VedtakForPeriodeRequest(Aktør ident, Periode periode, Set<Ytelser> ytelser) {
        this.ident = Objects.requireNonNull(ident);
        this.periode = Objects.requireNonNull(periode);
        this.ytelser = Objects.requireNonNull(ytelser);
    }

    public Aktør getIdent() {
        return ident;
    }

    public Periode getPeriode() {
        return periode;
    }

    public Set<Ytelser> getYtelser() {
        return ytelser;
    }
}
