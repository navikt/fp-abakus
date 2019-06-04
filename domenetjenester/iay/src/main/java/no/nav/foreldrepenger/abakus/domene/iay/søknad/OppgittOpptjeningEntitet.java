package no.nav.foreldrepenger.abakus.domene.iay.søknad;

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

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.Frilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
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
    private List<EgenNæringEntitet> egenNæring;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<AnnenAktivitetEntitet> annenAktivitet;

    @ChangeTracked
    @OneToOne(mappedBy = "oppgittOpptjening")
    private FrilansEntitet frilans;

    @SuppressWarnings("unused")
    private OppgittOpptjeningEntitet() {
        // hibernate
    }

    public OppgittOpptjeningEntitet(UUID eksternReferanse) {
        this.eksternReferanse = eksternReferanse;
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
    public List<EgenNæring> getEgenNæring() {
        if (this.egenNæring == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(egenNæring);
    }

    @Override
    public List<AnnenAktivitet> getAnnenAktivitet() {
        if (this.annenAktivitet == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(annenAktivitet);
    }

    @Override
    public Optional<Frilans> getFrilans() {
        return Optional.ofNullable(frilans);
    }

    void leggTilFrilans(Frilans frilans) {
        FrilansEntitet frilansEntitet = (FrilansEntitet) frilans;
        frilansEntitet.setOppgittOpptjening(this);
        this.frilans = frilansEntitet;
    }

    void leggTilAnnenAktivitet(AnnenAktivitet annenAktivitet) {
        if (this.annenAktivitet == null) {
            this.annenAktivitet = new ArrayList<>();
        }
        AnnenAktivitetEntitet annenAktivitetEntitet = (AnnenAktivitetEntitet) annenAktivitet;
        annenAktivitetEntitet.setOppgittOpptjening(this);
        this.annenAktivitet.add(annenAktivitetEntitet);
    }

    void leggTilEgenNæring(EgenNæring egenNæring) {
        if (this.egenNæring == null) {
            this.egenNæring = new ArrayList<>();
        }
        EgenNæringEntitet egenNæringEntitet = (EgenNæringEntitet) egenNæring;
        egenNæringEntitet.setOppgittOpptjening(this);
        this.egenNæring.add(egenNæringEntitet);
    }

    void leggTilOppgittArbeidsforhold(OppgittArbeidsforhold oppgittArbeidsforhold) {
        if (this.oppgittArbeidsforhold == null) {
            this.oppgittArbeidsforhold = new ArrayList<>();
        }
        OppgittArbeidsforholdEntitet oppgittArbeidsforholdEntitet = (OppgittArbeidsforholdEntitet) oppgittArbeidsforhold;
        oppgittArbeidsforholdEntitet.setOppgittOpptjening(this);
        this.oppgittArbeidsforhold.add(oppgittArbeidsforholdEntitet);
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
