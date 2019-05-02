package no.nav.foreldrepenger.abakus.typer;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;

/**
 * Id som genereres fra NAV Aktør Register. Denne iden benyttes til interne forhold i Nav og vil ikke endres f.eks. dersom bruker går fra
 * DNR til FNR i Folkeregisteret. Tilsvarende vil den kunne referere personer som har ident fra et utenlandsk system.
 */
@Embeddable
public class OrgNummer implements Serializable, Comparable<OrgNummer>, IndexKey, TraverseValue {

    @JsonValue
    @Column(name = "org_nummer", updatable = false, length = 50)
    private String orgNummer;  // NOSONAR

    protected OrgNummer() {
        // for hibernate
    }

    public OrgNummer(String orgNummer) {
        Objects.requireNonNull(orgNummer, "orgNummer");
        if (!OrganisasjonsNummerValidator.erGyldig(orgNummer)) {
            // skal ikke skje, funksjonelle feilmeldinger håndteres ikke her.
            throw new IllegalArgumentException("Ikke gyldig orgnummer..");
        }
        this.orgNummer = orgNummer;
    }

    @Override
    public String getIndexKey() {
        return orgNummer;
    }

    public String getId() {
        return orgNummer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof OrgNummer)) {
            return false;
        }
        OrgNummer other = (OrgNummer) obj;
        return Objects.equals(orgNummer, other.orgNummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgNummer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + orgNummer + ">";
    }

    @Override
    public int compareTo(OrgNummer o) {
        // TODO: Burde ikke finnes
        return orgNummer.compareTo(o.orgNummer);
    }
}
