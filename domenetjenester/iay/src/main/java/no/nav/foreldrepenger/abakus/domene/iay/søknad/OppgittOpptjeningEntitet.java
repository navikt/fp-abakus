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

import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;

@Entity(name = "OppgittOpptjening")
@Table(name = "IAY_OPPGITT_OPPTJENING")
public class OppgittOpptjeningEntitet extends BaseEntitet implements OppgittOpptjening {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_OPPTJENING")
    private Long id;

    @NaturalId
    @DiffIgnore
    @Column(name = "ekstern_referanse", updatable = false, unique = true)
    private UUID eksternReferanse;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittArbeidsforholdEntitet> oppgittArbeidsforhold;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittEgenNæringEntitet> egenNæring;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittAnnenAktivitetEntitet> annenAktivitet;

    @ChangeTracked
    @OneToOne(mappedBy = "oppgittOpptjening")
    private OppgittFrilansEntitet frilans;

    @SuppressWarnings("unused")
    private OppgittOpptjeningEntitet() {
        // hibernate
    }

    OppgittOpptjeningEntitet(UUID eksternReferanse) {
        this.eksternReferanse = eksternReferanse;
    }

    OppgittOpptjeningEntitet(UUID eksternReferanse, LocalDateTime opprettetTidspunktOriginalt) {
        this(eksternReferanse);
        super.setOpprettetTidspunkt(opprettetTidspunktOriginalt);
    }

    @Override
    public List<OppgittArbeidsforhold> getOppgittArbeidsforhold() {
        if (this.oppgittArbeidsforhold == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(oppgittArbeidsforhold);
    }

    @Override
    public UUID getEksternReferanse() {
        return eksternReferanse;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public List<OppgittEgenNæring> getEgenNæring() {
        if (this.egenNæring == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(egenNæring);
    }

    @Override
    public List<OppgittAnnenAktivitet> getAnnenAktivitet() {
        if (this.annenAktivitet == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(annenAktivitet);
    }

    @Override
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
            OppgittAnnenAktivitetEntitet annenAktivitetEntitet = (OppgittAnnenAktivitetEntitet) annenAktivitet;
            annenAktivitetEntitet.setOppgittOpptjening(this);
            this.annenAktivitet.add(annenAktivitetEntitet);
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
            OppgittArbeidsforholdEntitet oppgittArbeidsforholdEntitet = (OppgittArbeidsforholdEntitet) oppgittArbeidsforhold;
            oppgittArbeidsforholdEntitet.setOppgittOpptjening(this);
            this.oppgittArbeidsforhold.add(oppgittArbeidsforholdEntitet);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        OppgittOpptjeningEntitet that = (OppgittOpptjeningEntitet) o;
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
