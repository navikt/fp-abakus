package no.nav.foreldrepenger.abakus.domene.iay.søknad;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.felles.diff.ChangeTracked;
import no.nav.foreldrepenger.abakus.felles.jpa.BaseEntitet;
import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;


@Table(name = "IAY_OPPGITT_FRILANS")
@Entity(name = "Frilans")
public class OppgittFrilansEntitet extends BaseEntitet implements OppgittFrilans {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_SO_OPPGITT_FRILANS")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "oppgitt_opptjening_id", nullable = false, updatable = false)
    private OppgittOpptjeningEntitet oppgittOpptjening;

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
    private List<OppgittFrilansoppdragEntitet> frilansoppdrag;


    public OppgittFrilansEntitet() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OppgittFrilansEntitet that = (OppgittFrilansEntitet) o;
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

    public void setOppgittOpptjening(OppgittOpptjeningEntitet oppgittOpptjening) {
        this.oppgittOpptjening = oppgittOpptjening;
    }

    @Override
    public boolean getHarInntektFraFosterhjem() {
        return harInntektFraFosterhjem;
    }

    public void setHarInntektFraFosterhjem(boolean harInntektFraFosterhjem) {
        this.harInntektFraFosterhjem = harInntektFraFosterhjem;
    }

    @Override
    public boolean getErNyoppstartet() {
        return erNyoppstartet;
    }

    public void setErNyoppstartet(boolean erNyoppstartet) {
        this.erNyoppstartet = erNyoppstartet;
    }

    @Override
    public boolean getHarNærRelasjon() {
        return harNærRelasjon;
    }

    public void setHarNærRelasjon(boolean harNærRelasjon) {
        this.harNærRelasjon = harNærRelasjon;
    }

    @Override
    public List<OppgittFrilansoppdrag> getFrilansoppdrag() {
        if (frilansoppdrag != null) {
            return Collections.unmodifiableList(frilansoppdrag);
        }
        return Collections.emptyList();
    }

    public void setFrilansoppdrag(List<OppgittFrilansoppdragEntitet> frilansoppdrag) {
        this.frilansoppdrag = frilansoppdrag;
    }
}
