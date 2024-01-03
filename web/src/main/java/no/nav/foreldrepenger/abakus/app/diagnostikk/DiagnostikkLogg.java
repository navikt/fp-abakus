package no.nav.foreldrepenger.abakus.app.diagnostikk;

import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

import jakarta.persistence.*;

@Entity(name = "DiagnostikkLogg")
@Table(name = "DIAGNOSTIKK_LOGG")
public class DiagnostikkLogg extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIAGNOSTIKK_LOGG")
    @Column(name = "id")
    private Long id;

    @Column(name = "saksnummer", nullable = false, updatable = false, insertable = true)
    private Saksnummer saksnummer;

    DiagnostikkLogg() {
        // Hibernate
    }

    public DiagnostikkLogg(Saksnummer saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Long getId() {
        return id;
    }

    public Saksnummer getSaksnummer() {
        return this.saksnummer;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<saksnummer=" + saksnummer + ">";
    }

    @PreRemove
    protected void onDelete() {
        throw new IllegalStateException("Skal aldri kunne slette. [id=" + id + "]");
    }
}
