package no.nav.foreldrepenger.abakus.domene.iay.arbeidsforhold;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.BekreftetPermisjon;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class ArbeidsforholdOverstyringBuilder {

    private final ArbeidsforholdOverstyringEntitet kladd;
    private final boolean oppdatering;

    private ArbeidsforholdOverstyringBuilder(ArbeidsforholdOverstyringEntitet kladd, boolean oppdatering) {
        this.kladd = kladd;
        this.oppdatering = oppdatering;
    }

    static ArbeidsforholdOverstyringBuilder ny() {
        return new ArbeidsforholdOverstyringBuilder(new ArbeidsforholdOverstyringEntitet(), false);
    }

    static ArbeidsforholdOverstyringBuilder oppdatere(ArbeidsforholdOverstyringEntitet oppdatere) {
        return new ArbeidsforholdOverstyringBuilder(new ArbeidsforholdOverstyringEntitet(oppdatere), true);
    }

    public static ArbeidsforholdOverstyringBuilder oppdatere(Optional<ArbeidsforholdOverstyringEntitet> oppdatere) {
        return oppdatere.map(ArbeidsforholdOverstyringBuilder::oppdatere).orElseGet(ArbeidsforholdOverstyringBuilder::ny);
    }

    public ArbeidsforholdOverstyringBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiverEntitet) {
        kladd.setArbeidsgiver(arbeidsgiverEntitet);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medArbeidsforholdRef(ArbeidsforholdRef ref) {
        kladd.setArbeidsforholdRef(ref);
        return this;
    }

    /** Angi ny arbeidsforholdreferanse som skal erstatte opprinnelig. */
    public ArbeidsforholdOverstyringBuilder medNyArbeidsforholdRef(ArbeidsforholdRef ref) {
        kladd.setNyArbeidsforholdRef(ref);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medHandling(ArbeidsforholdHandlingType type) {
        kladd.setHandling(type);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medAngittArbeidsgiverNavn(String navn) {
        kladd.setArbeidsgiverNavn(navn);
        return this;
    }
    
    public ArbeidsforholdOverstyringBuilder medAngittStillingsprosent(Stillingsprosent prosent) {
        kladd.setStillingsprosent(prosent);
        return this;
    }
    
    public ArbeidsforholdOverstyringBuilder medBekreftetPermisjon(BekreftetPermisjon bekreftetPermisjon) {
        kladd.setBekreftetPermisjon(bekreftetPermisjon);
        return this;
    }
    
    public ArbeidsforholdOverstyringBuilder medBeskrivelse(String beskrivelse) {
        kladd.setBeskrivelse(beskrivelse);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder medInformasjon(ArbeidsforholdInformasjonEntitet informasjonEntitet) {
        kladd.setInformasjon(informasjonEntitet);
        return this;
    }

    public ArbeidsforholdOverstyringBuilder leggTilOverstyrtPeriode(LocalDate fom, LocalDate tom) {
        kladd.leggTilOverstyrtPeriode(fom, tom);
        return this;
    }

    public ArbeidsforholdOverstyringEntitet build() {
        return kladd;
    }

    boolean isOppdatering() {
        return oppdatering;
    }
}
