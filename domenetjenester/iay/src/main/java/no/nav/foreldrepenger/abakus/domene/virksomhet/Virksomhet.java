package no.nav.foreldrepenger.abakus.domene.virksomhet;

import no.nav.abakus.iaygrunnlag.kodeverk.OrganisasjonType;

import java.time.LocalDate;
import java.util.Objects;

public class Virksomhet {

    private String orgnr;
    private String navn;
    private LocalDate registrert;
    private LocalDate avsluttet;
    private LocalDate oppstart;
    private OrganisasjonType organisasjonType = OrganisasjonType.UDEFINERT;

    public Virksomhet() {
    }

    public String getOrgnr() {
        return orgnr;
    }

    public String getNavn() {
        return navn;
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    public LocalDate getOppstart() {
        return oppstart;
    }

    public LocalDate getAvslutt() {
        return avsluttet;
    }

    public OrganisasjonType getOrganisasjonstype() {
        return organisasjonType;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Virksomhet)) {
            return false;
        }
        Virksomhet other = (Virksomhet) obj;
        return Objects.equals(this.getOrgnr(), other.getOrgnr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgnr);
    }

    @Override
    public String toString() {
        return "Virksomhet{" + "navn=" + navn + ", orgnr=" + orgnr + '}';
    }

    public static class Builder {
        final private Virksomhet mal;

        /**
         * For oppretting av
         */
        public Builder() {
            this.mal = new Virksomhet();
        }

        public Builder medOrgnr(String orgnr) {
            this.mal.orgnr = orgnr;
            return this;
        }

        public Builder medNavn(String navn) {
            this.mal.navn = navn;
            return this;
        }

        public Builder medOppstart(LocalDate oppstart) {
            this.mal.oppstart = oppstart;
            return this;
        }

        public Builder medAvsluttet(LocalDate avsluttet) {
            this.mal.avsluttet = avsluttet;
            return this;
        }

        public Builder medRegistrert(LocalDate registrert) {
            this.mal.registrert = registrert;
            return this;
        }

        public Builder medOrganisasjonstype(OrganisasjonType organisasjonsType) {
            this.mal.organisasjonType = organisasjonsType;
            return this;
        }

        public Virksomhet build() {
            return mal;
        }
    }
}
