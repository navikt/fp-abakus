package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilans;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;

@Immutable
@Entity(name = "OppgittOpptjening")
@Table(name = "IAY_OPPGITT_OPPTJENING")
public class OppgittOpptjening extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_OPPTJENING")
    private Long id;

    @NaturalId
    @DiffIgnore
    @Column(name = "ekstern_referanse", updatable = false, unique = true)
    private UUID eksternReferanse;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittArbeidsforhold> oppgittArbeidsforhold;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittEgenNæringEntitet> egenNæring;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittAnnenAktivitet> annenAktivitet;

    @ChangeTracked
    @OneToOne(mappedBy = "oppgittOpptjening")
    private OppgittFrilansEntitet frilans;

    @SuppressWarnings("unused")
    private OppgittOpptjening() {
        // hibernate
    }

    OppgittOpptjening(UUID eksternReferanse) {
        this.eksternReferanse = eksternReferanse;
    }

    OppgittOpptjening(UUID eksternReferanse, LocalDateTime opprettetTidspunktOriginalt) {
        this(eksternReferanse);
        super.setOpprettetTidspunkt(opprettetTidspunktOriginalt);
    }

    public List<OppgittArbeidsforhold> getOppgittArbeidsforhold() {
        if (this.oppgittArbeidsforhold == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(oppgittArbeidsforhold);
    }

    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    public Long getId() {
        return id;
    }

    public List<OppgittEgenNæring> getEgenNæring() {
        if (this.egenNæring == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(egenNæring);
    }

    public List<OppgittAnnenAktivitet> getAnnenAktivitet() {
        if (this.annenAktivitet == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(annenAktivitet);
    }

    public Optional<OppgittFrilans> getFrilans() {
        return Optional.ofNullable(frilans);
    }

    void leggTilFrilans(OppgittFrilans frilans) {
        if (frilans != null) {
            OppgittFrilansEntitet frilansEntitet = (OppgittFrilansEntitet) frilans;
            frilansEntitet.setOppgittOpptjening(this);
            this.frilans = frilansEntitet;
        } else {
            this.frilans = null;
        }
    }

    void leggTilAnnenAktivitet(OppgittAnnenAktivitet annenAktivitet) {
        if (this.annenAktivitet == null) {
            this.annenAktivitet = new ArrayList<>();
        }
        if (annenAktivitet != null) {
            annenAktivitet.setOppgittOpptjening(this);
            this.annenAktivitet.add(annenAktivitet);
        }
    }

    void leggTilEgenNæring(OppgittEgenNæring egenNæring) {
        if (this.egenNæring == null) {
            this.egenNæring = new ArrayList<>();
        }
        if (egenNæring != null) {
            OppgittEgenNæringEntitet egenNæringEntitet = (OppgittEgenNæringEntitet) egenNæring;
            egenNæringEntitet.setOppgittOpptjening(this);
            this.egenNæring.add(egenNæringEntitet);
        }
    }

    void leggTilOppgittArbeidsforhold(OppgittArbeidsforhold oppgittArbeidsforhold) {
        if (this.oppgittArbeidsforhold == null) {
            this.oppgittArbeidsforhold = new ArrayList<>();
        }
        if (oppgittArbeidsforhold != null) {
            oppgittArbeidsforhold.setOppgittOpptjening(this);
            this.oppgittArbeidsforhold.add(oppgittArbeidsforhold);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !(o instanceof OppgittOpptjening))
            return false;
        var that = (OppgittOpptjening) o;
        return Objects.equals(oppgittArbeidsforhold, that.oppgittArbeidsforhold) &&
            Objects.equals(egenNæring, that.egenNæring) &&
            Objects.equals(annenAktivitet, that.annenAktivitet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittArbeidsforhold, egenNæring, annenAktivitet);
    }

    @Override
    public String toString() {
        return "OppgittOpptjeningEntitet{" +
            "id=" + id +
            ", oppgittArbeidsforhold=" + oppgittArbeidsforhold +
            ", egenNæring=" + egenNæring +
            ", annenAktivitet=" + annenAktivitet +
            '}';
    }
}
