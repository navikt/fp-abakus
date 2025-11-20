package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Objects;
import java.util.Optional;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.IndexKeyComposer;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.iay.jpa.InntektPeriodeTypeKodeverdiConverter;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

@Entity(name = "YtelseStørrelse")
@Table(name = "IAY_YTELSE_STOERRELSE")
public class YtelseStørrelse extends BaseEntitet implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_YTELSE_STOERRELSE")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ytelse_grunnlag_id", nullable = false, updatable = false, unique = true)
    private YtelseGrunnlag ytelseGrunnlag;

    @Convert(converter = InntektPeriodeTypeKodeverdiConverter.class)
    @Column(name = "hyppighet", nullable = false, updatable = false)
    @ChangeTracked
    private InntektPeriodeType hyppighet = InntektPeriodeType.UDEFINERT;

    @ChangeTracked
    @Embedded
    private OrgNummer orgNummer;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "verdi", column = @Column(name = "beloep", nullable = false)))
    @ChangeTracked
    private Beløp beløp;

    @Column(name = "er_refusjon", updatable = false)
    @ChangeTracked
    private Boolean erRefusjon;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public YtelseStørrelse() {
        // hibernate
    }

    public YtelseStørrelse(YtelseStørrelse ytelseStørrelse) {
        ytelseStørrelse.getVirksomhet().ifPresent(orgNummer -> this.orgNummer = orgNummer);
        this.beløp = ytelseStørrelse.getBeløp();
        this.hyppighet = ytelseStørrelse.getHyppighet();
    }

    @Override
    public String getIndexKey() {
        Object[] keyParts = {orgNummer};
        return IndexKeyComposer.createKey(keyParts);
    }

    public Optional<OrgNummer> getVirksomhet() {
        return Optional.ofNullable(orgNummer);
    }

    public void setVirksomhet(OrgNummer orgNummer) {
        this.orgNummer = orgNummer;
    }

    public Beløp getBeløp() {
        return beløp;
    }

    public void setBeløp(Beløp beløp) {
        this.beløp = beløp;
    }

    public Boolean getErRefusjon() {
        return erRefusjon;
    }

    public void setErRefusjon(Boolean erRefusjon) {
        this.erRefusjon = erRefusjon;
    }

    public InntektPeriodeType getHyppighet() {
        return hyppighet;
    }

    public void setHyppighet(InntektPeriodeType hyppighet) {
        this.hyppighet = hyppighet;
    }

    public void setYtelseGrunnlag(YtelseGrunnlag ytelseGrunnlag) {
        this.ytelseGrunnlag = ytelseGrunnlag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof YtelseStørrelse)) {
            return false;
        }
        var that = (YtelseStørrelse) o;
        return Objects.equals(orgNummer, that.orgNummer) && Objects.equals(beløp, that.beløp) && Objects.equals(hyppighet, that.hyppighet);
    }

    @Override
    public int hashCode() {

        return Objects.hash(orgNummer, beløp, hyppighet);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "virksomhet=" + orgNummer + ", beløp=" + beløp + ", hyppighet=" + hyppighet + '>';
    }

    boolean hasValues() {
        return beløp != null || hyppighet != null || orgNummer != null;
    }
}
