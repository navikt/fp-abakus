package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    private OrganisasjonstypeEReg type;
    @JsonProperty("organisasjonDetaljer")
    private OrganisasjonDetaljer organisasjonDetaljer;
    @JsonProperty("driverVirksomheter")
    private List<DriverVirksomhet> driverVirksomheter;

    public JuridiskEnhetVirksomheter() {
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public OrganisasjonstypeEReg getType() {
        return type;
    }

    public List<String> getEksaktVirksomhetForDato(LocalDate hentedato) {
        if (!OrganisasjonstypeEReg.JURIDISK_ENHET.equals(type) || getOpphørsdatoNonNull().isBefore(hentedato))
            return Collections.emptyList();
        List<DriverVirksomhet> virksomheter = driverVirksomheter != null ? driverVirksomheter : Collections.emptyList();
        return virksomheter.stream()
            .filter(v -> v.getGyldighetsperiode().getFom().isBefore(hentedato) && v.getGyldighetsperiode().getTomNonNull().isAfter(hentedato))
            .map(DriverVirksomhet::getOrganisasjonsnummer)
            .collect(Collectors.toList());
    }

    public LocalDate getRegistreringsdato() {
        return organisasjonDetaljer != null ? organisasjonDetaljer.getRegistreringsdato().toLocalDate() : null;
    }

    public LocalDate getOpphørsdato() {
        return organisasjonDetaljer != null ? organisasjonDetaljer.getOpphørsdato() : null;
    }

    private LocalDate getOpphørsdatoNonNull() {
        return organisasjonDetaljer != null && organisasjonDetaljer.getOpphørsdato() != null ? organisasjonDetaljer.getOpphørsdato() : Tid.TIDENES_ENDE;
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
            return tom;
        }

        public LocalDate getTomNonNull() {
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

