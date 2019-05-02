package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Objects;
import java.util.Optional;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

@Entity(name = "YtelseStørrelseEntitet")
@Table(name = "IAY_YTELSE_STOERRELSE")
public class YtelseStørrelseEntitet extends BaseEntitet implements YtelseStørrelse, IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_STOERRELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_grunnlag_id", nullable = false, updatable = false, unique = true)
    private YtelseGrunnlagEntitet ytelseGrunnlag;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "hyppighet", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + InntektPeriodeType.DISCRIMINATOR + "'"))
    @ChangeTracked
    private InntektPeriodeType hyppighet = InntektPeriodeType.UDEFINERT;

    @ChangeTracked
    @Embedded
    private OrgNummer orgNummer;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public YtelseStørrelseEntitet() {
        // hibernate
    }

    public YtelseStørrelseEntitet(YtelseStørrelse ytelseStørrelse) {
        ytelseStørrelse.getVirksomhet().ifPresent(orgNummer ->
            this.orgNummer = orgNummer
        );
        this.beløp = ytelseStørrelse.getBeløp();
        this.hyppighet = ytelseStørrelse.getHyppighet();
    }

    @Override
    public String getIndexKey() {
        return IndexKey.createKey(orgNummer);
    }

    @Override
    public Optional<OrgNummer> getVirksomhet() {
        return Optional.ofNullable(orgNummer);
    }

    public void setVirksomhet(OrgNummer orgNummer) {
        this.orgNummer = orgNummer;
    }

    @Override
    public Beløp getBeløp() {
        return beløp;
    }

    public void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    @Override
    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public void setHyppighet(InntektPeriodeType hyppighet) {
        this.hyppighet = hyppighet;
    }

    public void setYtelseGrunnlag(YtelseGrunnlagEntitet ytelseGrunnlag) {
        this.ytelseGrunnlag = ytelseGrunnlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YtelseStørrelseEntitet that = (YtelseStørrelseEntitet) o;
        return Objects.equals(orgNummer, that.orgNummer) &&
            Objects.equals(beløp, that.beløp) &&
            Objects.equals(hyppighet, that.hyppighet);
    }

    @Override
    public int hashCode() {

        return Objects.hash(orgNummer, beløp, hyppighet);
    }

    @Override
    public String toString() {
        return "YtelseStørrelseEntitet{" +
            "virksomhet=" + orgNummer +
            ", beløp=" + beløp +
            ", hyppighet=" + hyppighet +
            '}';
    }

    boolean hasValues() {
        return beløp != null || hyppighet != null || orgNummer != null;
    }
}
