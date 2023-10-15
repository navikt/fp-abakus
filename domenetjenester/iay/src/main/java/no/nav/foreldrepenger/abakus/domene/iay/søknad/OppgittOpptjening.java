package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.NaturalId;

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.diff.DiffIgnore;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.JournalpostId;

@Immutable
@Entity(name = "OppgittOpptjening")
@Table(name = "IAY_OPPGITT_OPPTJENING")
public class OppgittOpptjening extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_OPPTJENING")
    private Long id;

    /**
     * nullable - denne er kun i bruk når det lagres på aggregat ( se /v2/motta-endepunktet )
     */
    @ManyToOne
    @JoinColumn(name = "oppgitte_opptjeninger_id", updatable = false)
    private OppgittOpptjeningAggregat oppgitteOpptjeninger;

    @NaturalId
    @DiffIgnore
    @Column(name = "ekstern_referanse", updatable = false, unique = true)
    private UUID eksternReferanse;

    @Embedded
    private JournalpostId journalpostId;

    @Column(name = "innsendingstidspunkt", updatable = false, nullable = false)
    private LocalDateTime innsendingstidspunkt;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittArbeidsforhold> oppgittArbeidsforhold;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittEgenNæring> egenNæring;

    @OneToMany(mappedBy = "oppgittOpptjening")
    @ChangeTracked
    private List<OppgittAnnenAktivitet> annenAktivitet;

    @ChangeTracked
    @OneToOne(mappedBy = "oppgittOpptjening")
    private OppgittFrilans frilans;

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

    /* copy ctor. */
    public OppgittOpptjening(OppgittOpptjening orginal) {
        this.eksternReferanse = orginal.getEksternReferanse();
        this.journalpostId = orginal.getJournalpostId();
        this.innsendingstidspunkt = orginal.getInnsendingstidspunkt();
        this.oppgittArbeidsforhold = orginal.getOppgittArbeidsforhold().stream().map(oppgittArbeidsforhold -> {
            OppgittArbeidsforhold kopi = new OppgittArbeidsforhold(oppgittArbeidsforhold);
            kopi.setOppgittOpptjening(this);
            return kopi;
        }).collect(Collectors.toList());
        this.egenNæring = orginal.getEgenNæring().stream().map(egenNæring -> {
            OppgittEgenNæring kopi = new OppgittEgenNæring(egenNæring);
            kopi.setOppgittOpptjening(this);
            return kopi;
        }).collect(Collectors.toList());
        this.annenAktivitet = orginal.getAnnenAktivitet().stream().map(annenAktivitet -> {
            OppgittAnnenAktivitet kopi = new OppgittAnnenAktivitet(annenAktivitet);
            kopi.setOppgittOpptjening(this);
            return kopi;
        }).collect(Collectors.toList());
        this.frilans = orginal.getFrilans().map(frilans -> {
            var kopi = new OppgittFrilans(frilans);
            kopi.setOppgittOpptjening(this);
            return kopi;
        }).orElse(null);
    }

    void setOppgitteOpptjeninger(OppgittOpptjeningAggregat oppgitteOpptjeninger) {
        this.oppgitteOpptjeninger = oppgitteOpptjeninger;
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

    public JournalpostId getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
    }

    public LocalDateTime getInnsendingstidspunkt() {
        return innsendingstidspunkt;
    }

    public void setInnsendingstidspunkt(LocalDateTime innsendingstidspunkt) {
        this.innsendingstidspunkt = innsendingstidspunkt;
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
            frilans.setOppgittOpptjening(this);
            this.frilans = frilans;
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
            egenNæring.setOppgittOpptjening(this);
            this.egenNæring.add(egenNæring);
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
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof OppgittOpptjening)) {
            return false;
        }
        var that = (OppgittOpptjening) o;
        return Objects.equals(oppgittArbeidsforhold, that.oppgittArbeidsforhold) && Objects.equals(egenNæring, that.egenNæring) && Objects.equals(
            annenAktivitet, that.annenAktivitet) && Objects.equals(journalpostId, that.journalpostId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittArbeidsforhold, egenNæring, annenAktivitet, journalpostId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", oppgittArbeidsforhold=" + oppgittArbeidsforhold + ", egenNæring=" + egenNæring
            + ", annenAktivitet=" + annenAktivitet + '>';
    }

    /**
     * Nedstrippet versjon av toString, egnet for logging.
     */
    public String toStringSimple() {
        return getClass().getSimpleName() + "<" + "id=" + id + ", eksternReferanse=" + eksternReferanse + ", oppgittArbeidsforhold=[" + (
            oppgittArbeidsforhold == null ? "0" : oppgittArbeidsforhold.size()) + "]" + ", egenNæring=[" + (
            egenNæring == null ? "0" : egenNæring.size()) + "]" + ", annenAktivitet=[" + (annenAktivitet == null ? "0" : annenAktivitet.size()) + "]"
            + ", frilans=[" + (frilans == null ? "0" : frilans.getFrilansoppdrag().size()) + "]" + '>';
    }
}
