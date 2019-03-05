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
import javax.persistence.Table;

import no.nav.foreldrepenger.abakus.felles.diff.IndexKey;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

@Entity(name = "Kobling")
@Table(name = "KOBLING")
public class Kobling implements IndexKey {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING")
    private Long id;

    @Column(name = "referanse_id", updatable = false, nullable = false, unique = true)
    private String referanseId;

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

    public Kobling() {
    }

    public Kobling(String referanseId, AktørId aktørId, DatoIntervallEntitet opplysningsperiode) {
        Objects.requireNonNull(referanseId, "referanseId");
        Objects.requireNonNull(aktørId, "aktørId");
        Objects.requireNonNull(opplysningsperiode, "opplysningsperiode");
        this.referanseId = referanseId;
        this.aktørId = aktørId;
        this.opplysningsperiode = opplysningsperiode;
    }

    @Override
    public String getIndexKey() {
        return String.valueOf(referanseId);
    }

    public Long getId() {
        return id;
    }

    public String getReferanse() {
        return referanseId;
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

    @Override
    public String toString() {
        return "Kobling{" +
            "referanseId=" + referanseId +
            ", aktørId=" + aktørId +
            ", annenPartAktørId=" + annenPartAktørId +
            ", opplysningsperiode=" + opplysningsperiode +
            ", opptjeningsperiode=" + opptjeningsperiode +
            '}';
    }
}
