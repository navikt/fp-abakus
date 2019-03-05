package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.Landkoder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.UtenlandskVirksomhet;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;

/**
 * Hibernate entitet som modellerer en utenlandsk virksomhet.
 */
@Embeddable
public class UtenlandskVirksomhetEntitet implements UtenlandskVirksomhet, IndexKey, Serializable {

    @ManyToOne
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(column = @JoinColumn(name = "land", referencedColumnName = "kode", nullable = false)),
        @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + Landkoder.DISCRIMINATOR + "'"))})
    private Landkoder landkode = Landkoder.NOR;

    @Column(name = "utenlandsk_virksomhet_navn")
    private String utenlandskVirksomhetNavn;

    public UtenlandskVirksomhetEntitet() {
        //hibernate
    }

    public UtenlandskVirksomhetEntitet(Landkoder landkode, String utenlandskVirksomhetNavn) {
        this.landkode = landkode;
        this.utenlandskVirksomhetNavn = utenlandskVirksomhetNavn;
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(utenlandskVirksomhetNavn, landkode);
    }

    @Override
    public Landkoder getLandkode() {
        return landkode;
    }

    @Override
    public String getUtenlandskVirksomhetNavn() {
        return utenlandskVirksomhetNavn;
    }
}
