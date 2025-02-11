package no.nav.abakus.iaygrunnlag.ytelse.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_EMPTY)
public class YtelseDto {

    @JsonProperty(value = "fagsystem", required = true)
    @NotNull
    private Fagsystem fagsystem;

    @JsonProperty(value = "ytelseType", required = true)
    @NotNull
    private YtelseType ytelseType;


    @JsonProperty(value = "periode", required = true)
    @Valid
    @NotNull
    private Periode periode;

    @JsonProperty(value = "status", required = true)
    @NotNull
    private YtelseStatus status;

    @JsonProperty(value = "saksnummer")
    @Valid
    @Pattern(regexp = "^[A-Za-z0-9_\\.\\-:]+$", message = "Saksnummer [${validatedValue}] matcher ikke tillatt pattern '{value}'")
    private String saksnummer;

    @JsonProperty(value = "vedtattTidspunkt")
    private LocalDateTime vedtattTidspunkt;

    @JsonProperty(value = "anvisninger")
    @Valid
    private List<AnvisningDto> anvisninger;

    @JsonProperty(value = "ytelseGrunnlag")
    @Valid
    private YtelseGrunnlagDto grunnlag;

    @JsonCreator
    public YtelseDto(@JsonProperty(value = "fagsystem", required = true) @NotNull Fagsystem fagsystem,
                     @JsonProperty(value = "ytelseType", required = true) @NotNull YtelseType ytelseType,
                     @JsonProperty(value = "periode", required = true) @Valid @NotNull Periode periode,
                     @JsonProperty(value = "status", required = true) @NotNull YtelseStatus status) {
        Objects.requireNonNull(fagsystem, "fagsystem");
        Objects.requireNonNull(ytelseType, "ytelseType");
        Objects.requireNonNull(periode, "periode");
        Objects.requireNonNull(status, "status");
        this.fagsystem = fagsystem;
        this.ytelseType = ytelseType;
        this.periode = periode;
        this.status = status;
    }

    public List<AnvisningDto> getAnvisninger() {
        return anvisninger;
    }

    public void setAnvisninger(List<AnvisningDto> anvisninger) {
        this.anvisninger = anvisninger;
    }

    public YtelseDto medAnvisninger(List<AnvisningDto> anvisninger) {
        this.anvisninger = anvisninger;
        return this;
    }

    public YtelseGrunnlagDto getGrunnlag() {
        return grunnlag;
    }

    public void setGrunnlag(YtelseGrunnlagDto grunnlag) {
        this.grunnlag = grunnlag;
    }

    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    public void setVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        this.vedtattTidspunkt = vedtattTidspunkt;
    }

    public YtelseDto medGrunnlag(YtelseGrunnlagDto grunnlag) {
        this.grunnlag = grunnlag;
        return this;
    }

    public Fagsystem getFagsystemDto() {
        return fagsystem;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Periode getPeriode() {
        return periode;
    }

    public YtelseStatus getStatus() {
        return status;
    }

    public YtelseDto medSaksnummer(String saksnummer) {
        setSaksnummer(saksnummer);
        return this;
    }

    public YtelseDto medVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        setVedtattTidspunkt(vedtattTidspunkt);
        return this;
    }
}
