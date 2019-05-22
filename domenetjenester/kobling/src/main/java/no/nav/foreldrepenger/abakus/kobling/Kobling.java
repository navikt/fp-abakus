package no.nav.foreldrepenger.abakus.kobling;

import java.time.LocalDate;
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
import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Kobling")
@Table(name = "KOBLING")
public class Kobling extends BaseEntitet implements IndexKey {

    /** Abakus intern kobling_id. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING")
    private Long id;

    /** Ekstern Referanse (eks. behandlingUuid). */
    @NaturalId
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "referanse", column = @Column(name = "kobling_referanse", updatable = false, unique = true))
    })
    private KoblingReferanse koblingReferanse;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "ytelse_type", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + YtelseType.DISCRIMINATOR + "'"))
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "bruker_aktoer_id", nullable = false, updatable = false)))
    private AktørId aktørId;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "aktørId", column = @Column(name = "annen_part_aktoer_id")))
    private AktørId annenPartAktørId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "opplysning_periode_fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "opplysning_periode_tom"))
    })
    private DatoIntervallEntitet opplysningsperiode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "fomDato", column = @Column(name = "opptjening_periode_fom")),
            @AttributeOverride(name = "tomDato", column = @Column(name = "opptjening_periode_tom"))
    })
    private DatoIntervallEntitet opptjeningsperiode;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Kobling() {
    }

    public Kobling(KoblingReferanse koblingReferanse, AktørId aktørId, DatoIntervallEntitet opplysningsperiode, YtelseType ytelseType) {
        Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(opplysningsperiode, "opplysningsperiode");
        this.koblingReferanse = koblingReferanse;
        this.aktørId = aktørId;
        this.opplysningsperiode = opplysningsperiode;
        this.ytelseType = ytelseType;
    }

    @Override
    public String getIndexKey() {
        return String.valueOf(koblingReferanse);
    }

    public Long getId() {
        return id;
    }

    public KoblingReferanse getKoblingReferanse() {
        return koblingReferanse;
    }

    public AktørId getAktørId() {
        return aktørId;
    }

    public Optional<AktørId> getAnnenPartAktørId() {
        return Optional.ofNullable(annenPartAktørId);
    }

    public void setAnnenPartAktørId(AktørId annenPartAktørId) {
        this.annenPartAktørId = annenPartAktørId;
    }

    public LocalDate getSkjæringstidspunkt() {
        return opplysningsperiode.getFomDato().plusDays(1);
    }

    public DatoIntervallEntitet getOpplysningsperiode() {
        return opplysningsperiode;
    }

    public void setOpplysningsperiode(DatoIntervallEntitet opplysningsperiode) {
        this.opplysningsperiode = opplysningsperiode;
    }

    public DatoIntervallEntitet getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(DatoIntervallEntitet opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    @Override
    public String toString() {
        return "Kobling{" +
            "KoblingReferanse=" + koblingReferanse +
            ", aktørId=" + aktørId +
            ", annenPartAktørId=" + annenPartAktørId +
            ", opplysningsperiode=" + opplysningsperiode +
            ", opptjeningsperiode=" + opptjeningsperiode +
            '}';
    }
}
