package no.nav.abakus.vedtak.ytelse.v1;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.Aktør;
import no.nav.abakus.vedtak.ytelse.Kildesystem;
import no.nav.abakus.vedtak.ytelse.Periode;
import no.nav.abakus.vedtak.ytelse.Status;
import no.nav.abakus.vedtak.ytelse.Ytelse;
import no.nav.abakus.vedtak.ytelse.Ytelser;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;

public class YtelseV1 extends Ytelse {

    @NotNull
    @Valid
    @JsonProperty("aktør")
    private Aktør aktør;

    @NotNull
    @Valid
    @JsonProperty("vedtattTidspunkt")
    private LocalDateTime vedtattTidspunkt;

    @Deprecated(forRemoval = true)
    @NotNull // fjernes i neste fase
    @JsonProperty("type")
    private YtelseType type;

    //@NotNull - enable etter overgang
    @JsonProperty("ytelse")
    private Ytelser ytelse;

    @Pattern(regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$", flags = { Pattern.Flag.CASE_INSENSITIVE })
    @JsonProperty("saksnummer")
    private String saksnummer;

    @NotNull
    @Pattern(regexp = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b")
    @JsonProperty("vedtakReferanse")
    private String vedtakReferanse;

    @Deprecated(forRemoval = true)
    @NotNull // fjernes i neste fase
    @JsonProperty("status")
    private YtelseStatus status;

    //@NotNull
    @JsonProperty("ytelseStatus")
    private Status ytelseStatus;

    @Deprecated(forRemoval = true)
    @JsonProperty("fagsystem")
    @NotNull // fjernes i neste fase
    private Fagsystem fagsystem;

    @JsonProperty("kildesystem")
    //@NotNull - enable ette overgang
    private Kildesystem kildesystem;

    @NotNull
    @Valid
    @JsonProperty("periode")
    private Periode periode;

    @JsonProperty("tilleggsopplysninger")
    private String tilleggsopplysninger;

    @NotNull
    @Valid
    @JsonProperty("anvist")
    private List<Anvisning> anvist = new ArrayList<>();

    public YtelseV1() {
    }

    @Override
    public YtelseType getType() {
        return type;
    }

    public void setType(YtelseType type) {
        this.type = type;
    }

    @Override
    public Ytelser getYtelse() {
        return ytelse;
    }

    public void setYtelse(Ytelser ytelse) {
        this.ytelse = ytelse;
    }

    @Override
    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public YtelseStatus getStatus() {
        return status;
    }

    public void setStatus(YtelseStatus status) {
        this.status = status;
    }

    public Status getYtelseStatus() {
        return ytelseStatus;
    }

    public void setYtelseStatus(Status ytelseStatus) {
        this.ytelseStatus = ytelseStatus;
    }

    public Fagsystem getFagsystem() {
        return fagsystem;
    }

    public void setFagsystem(Fagsystem fagsystem) {
        this.fagsystem = fagsystem;
    }

    public Kildesystem getKildesystem() {
        return kildesystem;
    }

    public void setKildesystem(Kildesystem kildesystem) {
        this.kildesystem = kildesystem;
    }

    public Periode getPeriode() {
        return periode;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public List<Anvisning> getAnvist() {
        return anvist;
    }

    public void setAnvist(List<Anvisning> anvist) {
        this.anvist = anvist;
    }

    @Override
    public Aktør getAktør() {
        return aktør;
    }

    public void setAktør(Aktør aktør) {
        this.aktør = aktør;
    }

    public LocalDateTime getVedtattTidspunkt() {
        return vedtattTidspunkt;
    }

    public void setVedtattTidspunkt(LocalDateTime vedtattTidspunkt) {
        this.vedtattTidspunkt = vedtattTidspunkt;
    }

    public String getVedtakReferanse() {
        return vedtakReferanse;
    }

    public void setVedtakReferanse(String vedtakReferanse) {
        this.vedtakReferanse = vedtakReferanse;
    }

    public String getTilleggsopplysninger() {
        return tilleggsopplysninger;
    }

    public void setTilleggsopplysninger(String tilleggsopplysninger) {
        this.tilleggsopplysninger = tilleggsopplysninger;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[aktør=" + aktør + ", vedtattTidspunkt=" + vedtattTidspunkt + ", type="
                + type + ", saksnummer=" + saksnummer + ", vedtakReferanse=" + vedtakReferanse + ", status=" + status
                + ", fagsystem=" + fagsystem + ", periode=" + periode + ", anvist=" + anvist + "]";
    }
}
