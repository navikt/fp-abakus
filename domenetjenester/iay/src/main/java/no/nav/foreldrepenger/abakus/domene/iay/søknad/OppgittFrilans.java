package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Table(name = "IAY_OPPGITT_FRILANS")
@Entity(name = "Frilans")
public class OppgittFrilans extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_FRILANS")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjening oppgittOpptjening;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "inntekt_fra_fosterhjem", nullable = false)
    private boolean harInntektFraFosterhjem;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "nyoppstartet", nullable = false)
    private boolean erNyoppstartet;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "naer_relasjon", nullable = false)
    private boolean harNærRelasjon;

    @OneToMany(mappedBy = "frilans")
    @ChangeTracked
    private List<OppgittFrilansoppdrag> frilansoppdrag;


    public OppgittFrilans() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof OppgittFrilans)) return false;
        var that = (OppgittFrilans) o;
        return harInntektFraFosterhjem == that.harInntektFraFosterhjem &&
            erNyoppstartet == that.erNyoppstartet &&
            harNærRelasjon == that.harNærRelasjon &&
            Objects.equals(oppgittOpptjening, that.oppgittOpptjening) &&
            Objects.equals(frilansoppdrag, that.frilansoppdrag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oppgittOpptjening, harInntektFraFosterhjem, erNyoppstartet, harNærRelasjon, frilansoppdrag);
    }

    @Override
    public String toString() {
        return "FrilansEntitet{" +
            "oppgittOpptjening=" + oppgittOpptjening +
            ", harInntektFraFosterhjem=" + harInntektFraFosterhjem +
            ", erNyoppstartet=" + erNyoppstartet +
            ", harNærRelasjon=" + harNærRelasjon +
            ", frilansoppdrag=" + frilansoppdrag +
            '}';
    }

    public void setOppgittOpptjening(OppgittOpptjening oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    public boolean getHarInntektFraFosterhjem() {
        return harInntektFraFosterhjem;
    }

    public void setHarInntektFraFosterhjem(boolean harInntektFraFosterhjem) {
        this.harInntektFraFosterhjem = harInntektFraFosterhjem;
    }

    public boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public void setErNyoppstartet(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    public boolean getHarNærRelasjon() {
        return harNærRelasjon;
    }

    public void setHarNærRelasjon(boolean harNærRelasjon) {
        this.harNærRelasjon = harNærRelasjon;
    }

    public List<OppgittFrilansoppdrag> getFrilansoppdrag() {
        if (frilansoppdrag != null) {
            return Collections.unmodifiableList(frilansoppdrag);
        }
        return Collections.emptyList();
    }

    public void setFrilansoppdrag(List<OppgittFrilansoppdrag> frilansoppdrag) {
        this.frilansoppdrag = frilansoppdrag.stream()
            .peek(it -> it.setFrilans(this))
            .collect(Collectors.toList());
    }
}
