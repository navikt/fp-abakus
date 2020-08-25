package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;

public class Arbeidsforhold {
    private Arbeidsgiver arbeidsgiver;
    private String type;
    private LocalDate arbeidFom;
    private LocalDate arbeidTom;
    private List<Arbeidsavtale> arbeidsavtaler;
    private List<Permisjon> permisjoner;
    private EksternArbeidsforholdRef arbeidsforholdId;

    private Arbeidsforhold(Arbeidsgiver arbeidsgiver, String type, LocalDate arbeidFom, LocalDate arbeidTom,
                           List<Arbeidsavtale> arbeidsavtaler, List<Permisjon> permisjoner, EksternArbeidsforholdRef arbeidsforholdId) {
        this.arbeidsgiver = arbeidsgiver;
        this.type = type;
        this.arbeidFom = arbeidFom;
        this.arbeidTom = arbeidTom;
        this.arbeidsavtaler = arbeidsavtaler;
        this.permisjoner = permisjoner;
        this.arbeidsforholdId = arbeidsforholdId;
    }

    public Arbeidsgiver getArbeidsgiver() {
        return arbeidsgiver;
    }

    public String getType() {
        return type;
    }

    public LocalDate getArbeidFom() {
        return arbeidFom;
    }

    public LocalDate getArbeidTom() {
        return arbeidTom;
    }

    public List<Arbeidsavtale> getArbeidsavtaler() {
        return arbeidsavtaler;
    }

    public List<Permisjon> getPermisjoner() {
        return permisjoner;
    }

    public EksternArbeidsforholdRef getArbeidsforholdId() {
        return arbeidsforholdId;
    }

    public ArbeidsforholdIdentifikator getIdentifikator() {
        return new ArbeidsforholdIdentifikator(arbeidsgiver, arbeidsforholdId, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbeidsforhold that = (Arbeidsforhold) o;
        return Objects.equals(arbeidsgiver, that.arbeidsgiver) &&
            Objects.equals(type, that.type) &&
            Objects.equals(arbeidFom, that.arbeidFom) &&
            Objects.equals(arbeidTom, that.arbeidTom) &&
            Objects.equals(arbeidsavtaler, that.arbeidsavtaler) &&
            Objects.equals(permisjoner, that.permisjoner) &&
            (arbeidsgiver instanceof Organisasjon && Objects.equals(arbeidsforholdId, that.arbeidsforholdId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(arbeidsgiver, type, arbeidFom, arbeidTom, arbeidsavtaler, permisjoner, arbeidsforholdId);
    }

    @Override
    public String toString() {
        return "Arbeidsforhold{" +
            "arbeidsgiver=" + arbeidsgiver +
            ", type='" + type + '\'' +
            ", arbeidFom=" + arbeidFom +
            ", arbeidTom=" + arbeidTom +
            ", arbeidsavtaler=" + arbeidsavtaler +
            ", permisjoner=" + permisjoner +
            ", arbeidsforholdId=" + arbeidsforholdId +
            '}';
    }

    public static class Builder {
        private Arbeidsgiver arbeidsgiver;
        private String type;
        private LocalDate arbeidFom;
        private LocalDate arbeidTom;
        private List<Arbeidsavtale> arbeidsavtaler = new ArrayList<>();
        private List<Permisjon> permisjoner = new ArrayList<>();
        private EksternArbeidsforholdRef arbeidsforholdId;


        public Builder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
            this.arbeidsgiver = arbeidsgiver;
            return this;
        }

        public Builder medType(String type) {
            this.type = type;
            return this;
        }

        public Builder medArbeidsforholdId(String arbeidsforholdId) {
            this.arbeidsforholdId = EksternArbeidsforholdRef.ref(arbeidsforholdId);
            return this;
        }

        public Builder medArbeidFom(LocalDate arbeidFom) {
            this.arbeidFom = arbeidFom;
            return this;
        }

        public Builder medArbeidTom(LocalDate arbeidTom) {
            this.arbeidTom = arbeidTom;
            return this;
        }

        public Builder medArbeidsavtaler(List<Arbeidsavtale> arbeidsavtaler) {
            this.arbeidsavtaler = arbeidsavtaler;
            return this;
        }


        public Builder medAnsettelsesPeriode(Arbeidsavtale avtale) {
            if (this.arbeidsavtaler.isEmpty()) {
                this.arbeidsavtaler = new ArrayList<>();
            }
            this.arbeidsavtaler.add(avtale);
            return this;
        }

        public Builder medPermisjon(List<Permisjon> permisjoner) {
            this.permisjoner = permisjoner;
            return this;
        }

        public Arbeidsforhold build() {
            return new Arbeidsforhold(arbeidsgiver, type, arbeidFom, arbeidTom, arbeidsavtaler, permisjoner, arbeidsforholdId);
        }
    }
}
