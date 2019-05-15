package no.nav.foreldrepenger.abakus.domene.iay;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.diff.TraverseValue;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

@Embeddable
public class ArbeidsgiverEntitet implements Arbeidsgiver, IndexKey, TraverseValue {
    /**
     * Kun en av denne og {@link #arbeidsgiverAktørId} kan være satt. Sett denne hvis Arbeidsgiver er en Organisasjon.
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

    private ArbeidsgiverEntitet(OrgNummer arbeidsgiverOrgnr, AktørId arbeidsgiverAktørId) {
        this.arbeidsgiverOrgnr = arbeidsgiverOrgnr;
        this.arbeidsgiverAktørId = arbeidsgiverAktørId;
    }

    private ArbeidsgiverEntitet() {
    }

    public static Arbeidsgiver virksomhet(Virksomhet virksomhet) {
        return new ArbeidsgiverEntitet(new OrgNummer(virksomhet.getOrgnr()), null);
    }

    public static Arbeidsgiver virksomhet(OrgNummer orgnr) {
        return new ArbeidsgiverEntitet(orgnr, null);
    }

    public static Arbeidsgiver person(AktørId arbeidsgiverAktørId) {
        return new ArbeidsgiverEntitet(null, arbeidsgiverAktørId);
    }

    @Override
    public String getIndexKey() {
        return arbeidsgiverOrgnr == null
            ? IndexKey.createKey("arbeidsgiverAktørId", arbeidsgiverAktørId)
            : IndexKey.createKey("virksomhet", arbeidsgiverOrgnr);
    }

    @Override
    public OrgNummer getOrgnr() {
        return arbeidsgiverOrgnr;
    }

    @Override
    public AktørId getAktørId() {
        return arbeidsgiverAktørId;
    }

    /**
     * Returneer ident for arbeidsgiver. Kan være Org nummer eller AktørDto id (dersom arbeidsgiver er en enkelt person -
     * f.eks. for Frilans el.)
     */
    @Override
    public String getIdentifikator() {
        if (arbeidsgiverAktørId != null) {
            return arbeidsgiverAktørId.getId();
        }
        return arbeidsgiverOrgnr.getId();
    }

    /**
     * Return true hvis arbeidsgiver er en {@link Virksomhet}, false hvis en Person.
     */
    @Override
    public boolean getErVirksomhet() {
        return this.arbeidsgiverOrgnr != null;
    }

    /**
     * Return true hvis arbeidsgiver er en {@link AktørId}, ellers false.
     */
    @Override
    public boolean erAktørId() {
        return this.arbeidsgiverAktørId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof ArbeidsgiverEntitet)) {
            return false;
        }
        ArbeidsgiverEntitet that = (ArbeidsgiverEntitet) o;
        return Objects.equals(arbeidsgiverOrgnr, that.arbeidsgiverOrgnr) &&
            Objects.equals(arbeidsgiverAktørId, that.arbeidsgiverAktørId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiverOrgnr, arbeidsgiverAktørId);
    }

    @Override
    public String toString() {
        return "ArbeidsgiverEntitet{" +
            "arbeidsgiverOrgnr=" + arbeidsgiverOrgnr +
            ", arbeidsgiverAktørId='" + arbeidsgiverAktørId + '\'' +
            '}';
    }
}
