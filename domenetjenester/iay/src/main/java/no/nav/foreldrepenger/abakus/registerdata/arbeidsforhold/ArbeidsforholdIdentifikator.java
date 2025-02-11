package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;

import java.util.Objects;

public class ArbeidsforholdIdentifikator {
    private Arbeidsgiver arbeidsgiver;
    private EksternArbeidsforholdRef arbeidsforholdId;
    private String type;

    public ArbeidsforholdIdentifikator(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdId, String type) {
        this.arbeidsgiver = arbeidsgiver;
        this.arbeidsforholdId = arbeidsforholdId;
        this.type = type;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public EksternArbeidsforholdRef getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public String getType() {
        return type;
    }

    public boolean harArbeidsforholdRef() {
        return arbeidsforholdId != null;
    }

    @Override
    public String toString() {
        return "ArbeidsforholdIdentifikator{" + "arbeidsgiver=" + arbeidsgiver + ", arbeidsforholdId=" + arbeidsforholdId + ", type='" + type + '\''
            + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArbeidsforholdIdentifikator that = (ArbeidsforholdIdentifikator) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) && Objects.equals(arbeidsforholdId, that.arbeidsforholdId) && Objects.equals(type,
            that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, arbeidsforholdId, type);
    }
}
