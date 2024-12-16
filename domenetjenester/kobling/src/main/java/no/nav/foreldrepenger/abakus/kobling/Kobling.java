package no.nav.foreldrepenger.abakus.kobling;

import java.time.LocalDate;
import java.util.Objects;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.abakus.iaygrunnlag.kodeverk.IndexKey;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

@Entity(name = "Kobling")
@Table(name = "KOBLING")
public class Kobling extends BaseEntitet implements IndexKey {

    /**
     * Abakus intern kobling_id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KOBLING")
    private Long id;

    /**
     * Saksnummer (gruppererer alle koblinger under samme saksnummer - typisk generert av FPSAK, eller annet saksbehandlingsystem)
     */
    @Embedded
    @AttributeOverride(name = "saksnummer", column = @Column(name = "saksnummer"))
    private Saksnummer saksnummer;

    /**
     * Ekstern Referanse (eks. behandlingUuid).
     */
    @NaturalId
    @Embedded
    @AttributeOverride(name = "referanse", column = @Column(name = "kobling_referanse", updatable = false, unique = true))
    private KoblingReferanse koblingReferanse;

    @Convert(converter = YtelseTypeKodeverdiConverter.class)
    @Column(name = "ytelse_type", nullable = false)
    private YtelseType ytelseType = YtelseType.UDEFINERT;

    @Embedded
    @AttributeOverride(name = "aktørId", column = @Column(name = "bruker_aktoer_id", nullable = false, updatable = false))
    private AktørId aktørId;

    @Embedded
    @ChangeTracked
    @AttributeOverride(name = "fomDato", column = @Column(name = "opplysning_periode_fom"))
    @AttributeOverride(name = "tomDato", column = @Column(name = "opplysning_periode_tom"))
    private IntervallEntitet opplysningsperiode;

    @Embedded
    @ChangeTracked
    @AttributeOverride(name = "fomDato", column = @Column(name = "opplysning_periode_skattegrunnlag_fom"))
    @AttributeOverride(name = "tomDato", column = @Column(name = "opplysning_periode_skattegrunnlag_tom"))
    private IntervallEntitet opplysningsperiodeSkattegrunnlag;

    @Embedded
    @AttributeOverride(name = "fomDato", column = @Column(name = "opptjening_periode_fom"))
    @AttributeOverride(name = "tomDato", column = @Column(name = "opptjening_periode_tom"))
    private IntervallEntitet opptjeningsperiode;

    /**
     * Inaktive koblinger skal ikke kunne endres. Det betyr oftest at koblingsreferansen er avsluttet i fagsystemet også.
     */
    @Column(name = "aktiv", nullable = false)
    private boolean aktiv = true;

    @Version
    @Column(name = "versjon", nullable = false)
    private long versjon;

    public Kobling() {
    }

    public Kobling(YtelseType ytelseType, Saksnummer saksnummer, KoblingReferanse koblingReferanse, AktørId aktørId) {
        this.saksnummer = Objects.requireNonNull(saksnummer, "saksnummer");
        this.koblingReferanse = Objects.requireNonNull(koblingReferanse, "koblingReferanse");
        this.aktørId = Objects.requireNonNull(aktørId, "aktørId");
        this.ytelseType = ytelseType == null ? YtelseType.UDEFINERT : ytelseType;
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

    public LocalDate getSkjæringstidspunkt() {
        return opplysningsperiode.getFomDato().plusDays(1);
    }

    public IntervallEntitet getOpplysningsperiode() {
        return opplysningsperiode;
    }

    public void setOpplysningsperiode(IntervallEntitet opplysningsperiode) {
        this.opplysningsperiode = opplysningsperiode;
    }

    public IntervallEntitet getOpplysningsperiodeSkattegrunnlag() {
        return opplysningsperiodeSkattegrunnlag;
    }

    public void setOpplysningsperiodeSkattegrunnlag(IntervallEntitet opplysningsperiodeSkattegrunnlag) {
        this.opplysningsperiodeSkattegrunnlag = opplysningsperiodeSkattegrunnlag;
    }

    public IntervallEntitet getOpptjeningsperiode() {
        return opptjeningsperiode;
    }

    public void setOpptjeningsperiode(IntervallEntitet opptjeningsperiode) {
        this.opptjeningsperiode = opptjeningsperiode;
    }

    public Saksnummer getSaksnummer() {
        return saksnummer;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public long getVersjon() {
        return this.versjon;
    }

    @Override
    public String toString() {
        return "Kobling{" + "KoblingReferanse=" + koblingReferanse + ", saksnummer = " + saksnummer + ", opplysningsperiode=" + opplysningsperiode
            + ", opptjeningsperiode=" + opptjeningsperiode + '}';
    }

    public boolean erAktiv() {
        return aktiv;
    }

    public void deaktiver() {
        this.aktiv = false;
    }
}
