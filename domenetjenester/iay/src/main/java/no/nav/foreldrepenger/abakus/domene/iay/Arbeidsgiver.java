package no.nav.foreldrepenger.abakus.domene.iay;

import jakarta.persistence.*;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Arbeidsgiver implements IndexKey, TraverseValue, Serializable {
    /**
     * Kun en av denne og {@link #arbeidsgiverAktørId} kan være satt. Sett denne hvis ArbeidsgiverEntitet er en Organisasjon.
     */
    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "orgNummer", column = @Column(name = "arbeidsgiver_orgnr", updatable = false)))
    private OrgNummer arbeidsgiverOrgnr;

    /**
     * Kun en av denne og {@link #virksomhet} kan være satt. Sett denne hvis ArbeidsgiverEntitet er en Enkelt person.
     */
    @ChangeTracked
    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "arbeidsgiver_aktor_id", updatable = false)))
    private AktørId arbeidsgiverAktørId;

    private Arbeidsgiver(OrgNummer arbeidsgiverOrgnr, AktørId arbeidsgiverAktørId) {
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    private Arbeidsgiver() {
        // Plattform (CDI, Hibernate, Jackson)
    }

    public static Arbeidsgiver virksomhet(Virksomhet virksomhet) {
        return new Arbeidsgiver(new OrgNummer(virksomhet.getOrgnr()), null);
    }

    public static Arbeidsgiver virksomhet(OrgNummer orgnr) {
        return new Arbeidsgiver(orgnr, null);
    }

    public static Arbeidsgiver person(AktørId arbeidsgiverAktørId) {
        return new Arbeidsgiver(null, arbeidsgiverAktørId);
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {"arbeidsgiverAktørId", arbeidsgiverAktørId};
        Object[] keyParts1 = {"virksomhet", arbeidsgiverOrgnr};
        return arbeidsgiverOrgnr == null ? IndexKeyComposer.createKey(keyParts) : IndexKeyComposer.createKey(keyParts1);
    }

    public OrgNummer getOrgnr() {
        return arbeidsgiverOrgnr;
    }

    public AktørId getAktørId() {
        return arbeidsgiverAktørId;
    }

    /**
     * Returneer ident for ArbeidsgiverEntitet. Kan være Org nummer eller AktørDto id (dersom ArbeidsgiverEntitet er en enkelt person -
     * f.eks. for Frilans el.)
     */
    public String getIdentifikator() {
        if (arbeidsgiverAktørId != null) {
            return arbeidsgiverAktørId.getId();
        }
        return arbeidsgiverOrgnr.getId();
    }

    /**
     * Return true hvis ArbeidsgiverEntitet er en {@link Virksomhet}, false hvis en Person.
     */
    public boolean getErVirksomhet() {
        return this.arbeidsgiverOrgnr != null;
    }

    /**
     * Return true hvis ArbeidsgiverEntitet er en {@link AktørId}, ellers false.
     */
    public boolean erAktørId() {
        return this.arbeidsgiverAktørId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Arbeidsgiver)) {
            return false;
        }
        Arbeidsgiver that = (Arbeidsgiver) o;
        return Objects.equals(arbeidsgiverOrgnr, that.arbeidsgiverOrgnr) && Objects.equals(arbeidsgiverAktørId, that.arbeidsgiverAktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverOrgnr, arbeidsgiverAktørId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "arbeidsgiverOrgnr=" + arbeidsgiverOrgnr + ", arbeidsgiverAktørId='" + arbeidsgiverAktørId + '\''
            + '>';
    }
}
