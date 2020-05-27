package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.vedtak.konfig.Tid;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JuridiskEnhetVirksomheter {

    @JsonProperty("organisasjonsnummer")
    private String organisasjonsnummer;
    @JsonProperty("type")
    private String type;
    @JsonProperty("organisasjonDetaljer")
    private OrganisasjonDetaljer organisasjonDetaljer;
    @JsonProperty("driverVirksomheter")
    private List<DriverVirksomhet> driverVirksomheter;

    public JuridiskEnhetVirksomheter() {
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getType() {
        return type;
    }

    public LocalDate getRegistreringsdato() {
        return organisasjonDetaljer != null ? organisasjonDetaljer.getRegistreringsdato().toLocalDate() : null;
    }

    public LocalDate getOpphørsdato() {
        return organisasjonDetaljer != null ? organisasjonDetaljer.getOpphørsdato() : null;
    }

    public List<DriverVirksomhet> getDriverVirksomheter() {
        return driverVirksomheter;
    }

    @Override
    public String toString() {
        return "JuridiskEnhetVirksomheter{" +
            "organisasjonsnummer='" + organisasjonsnummer + '\'' +
            ", type='" + type + '\'' +
            ", driverVirksomheter=" + driverVirksomheter +
            '}';
    }

    static class OrganisasjonDetaljer {

        @JsonProperty("registreringsdato")
        private LocalDateTime registreringsdato;
        @JsonProperty("opphoersdato")
        private LocalDate opphoersdato;

        public OrganisasjonDetaljer() {
        }

        public LocalDateTime getRegistreringsdato() {
            return registreringsdato;
        }

        public LocalDate getOpphørsdato() {
            return opphoersdato;
        }

        @Override
        public String toString() {
            return "OrganisasjonDetaljer{" +
                "registreringsdato=" + registreringsdato +
                ", opphørsdato=" + opphoersdato +
                '}';
        }
    }

    public static class DriverVirksomhet {

        @JsonProperty("organisasjonsnummer")
        private String organisasjonsnummer;
        @JsonProperty("gyldighetsperiode")
        private Periode gyldighetsperiode;

        public DriverVirksomhet() {
        }

        public String getOrganisasjonsnummer() {
            return organisasjonsnummer;
        }

        public Periode getGyldighetsperiode() {
            return gyldighetsperiode;
        }

        @Override
        public String toString() {
            return "DriverVirksomhet{" +
                "organisasjonsnummer='" + organisasjonsnummer + '\'' +
                ", gyldighetsperiode=" + gyldighetsperiode +
                '}';
        }
    }

    public static class Periode {
        @JsonProperty("fom")
        private LocalDate fom;
        @JsonProperty("tom")
        private LocalDate tom;

        public Periode() {
        }

        public LocalDate getFom() {
            return fom;
        }

        public LocalDate getTom() {
            return tom != null ? tom : Tid.TIDENES_ENDE;
        }

        @Override
        public String toString() {
            return "Periode{" +
                "fom=" + fom +
                ", tom=" + tom +
                '}';
        }
    }

}

