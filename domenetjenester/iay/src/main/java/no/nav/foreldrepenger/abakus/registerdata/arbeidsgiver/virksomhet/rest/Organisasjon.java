package no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.rest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Organisasjon {

    @JsonProperty("organisasjonsnummer")
    private String organisasjonsnummer;
    @JsonProperty("type")
    private String type;
    @JsonProperty("navn")
    private Navn navn;
    @JsonProperty("organisasjonDetaljer")
    private OrganisasjonDetaljer organisasjonDetaljer;
    @JsonProperty("virksomhetDetaljer")
    private VirksomhetDetaljer virksomhetDetaljer;

    public Organisasjon() {
    }

    public String getOrganisasjonsnummer() {
        return organisasjonsnummer;
    }

    public String getType() {
        return type;
    }

    public String getNavn() {
        return navn != null ? navn.getNavn() : null;
    }

    public LocalDate getRegistreringsdato() {
        return organisasjonDetaljer != null ? organisasjonDetaljer.getRegistreringsdato().toLocalDate() : null;
    }

    public LocalDate getOpphørsdato() {
        return organisasjonDetaljer != null ? organisasjonDetaljer.getOpphoersdato() : null;
    }

    public LocalDate getOppstartsdato() {
        return virksomhetDetaljer != null ? virksomhetDetaljer.getOppstartsdato() : null;
    }

    public LocalDate getNedleggelsesdato() {
        return virksomhetDetaljer != null ? virksomhetDetaljer.getNedleggelsesdato() : null;
    }

    @Override
    public String toString() {
        return "Organisasjon{" +
            "organisasjonsnummer='" + organisasjonsnummer + '\'' +
            ", type='" + type + '\'' +
            ", navn=" + navn +
            ", organisasjonDetaljer=" + organisasjonDetaljer +
            ", virksomhetDetaljer=" + virksomhetDetaljer +
            '}';
    }

    static class Navn {

        @JsonProperty("navnelinje1")
        private String navnelinje1;
        @JsonProperty("navnelinje2")
        private String navnelinje2;
        @JsonProperty("navnelinje3")
        private String navnelinje3;
        @JsonProperty("navnelinje4")
        private String navnelinje4;
        @JsonProperty("navnelinje5")
        private String navnelinje5;

        public String getNavn() {
            return Stream.of(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5)
                .filter(n -> n != null && !n.isEmpty())
                .map(String::trim)
                .reduce("", (a, b) -> a + " " + b).trim();
        }

        @Override
        public String toString() {
            return "Navn{" +
                "navn='" + getNavn() + '\'' +
                '}';
        }
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

        public LocalDate getOpphoersdato() {
            return opphoersdato;
        }

        @Override
        public String toString() {
            return "OrganisasjonDetaljer{" +
                "registreringsdato=" + registreringsdato +
                ", opphoersdato=" + opphoersdato +
                '}';
        }
    }

    static class VirksomhetDetaljer {

        @JsonProperty("oppstartsdato")
        private LocalDate oppstartsdato;
        @JsonProperty("nedleggelsesdato")
        private LocalDate nedleggelsesdato;

        public VirksomhetDetaljer() {
        }

        public LocalDate getOppstartsdato() {
            return oppstartsdato;
        }

        public LocalDate getNedleggelsesdato() {
            return nedleggelsesdato;
        }

        @Override
        public String toString() {
            return "VirksomhetDetaljer{" +
                "oppstartsdato=" + oppstartsdato +
                ", nedleggelsesdato=" + nedleggelsesdato +
                '}';
        }
    }

}

