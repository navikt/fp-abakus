package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.InntektspostType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.typer.Beløp;

public class InntektspostBuilder {
    private InntektspostEntitet inntektspostEntitet;

    InntektspostBuilder(InntektspostEntitet inntektspostEntitet) {
        this.inntektspostEntitet = inntektspostEntitet;
    }

    public static InntektspostBuilder ny() {
        return new InntektspostBuilder(new InntektspostEntitet());
    }

    public InntektspostBuilder medInntektspostType(InntektspostType inntektspostType) {
        this.inntektspostEntitet.setInntektspostType(inntektspostType);
        return this;
    }

    public InntektspostBuilder medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.inntektspostEntitet.setSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
        return this;
    }

    public InntektspostBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        this.inntektspostEntitet.setPeriode(fraOgMed, tilOgMed);
        return this;
    }

    public InntektspostBuilder medBeløp(BigDecimal verdi) {
        this.inntektspostEntitet.setBeløp(new Beløp(verdi));
        return this;
    }

    public InntektspostBuilder medOriginalUtbetalerId(String opprinneligEnhetId) {
        this.inntektspostEntitet.setOpprinneligUtbetalerId(opprinneligEnhetId);
        return this;
    }

    public InntektspostBuilder medYtelse(YtelseInntektspostType offentligYtelseType) {
        this.inntektspostEntitet.setYtelse(offentligYtelseType);
        return this;
    }

    public Inntektspost build() {
        if (inntektspostEntitet.hasValues()) {
            return inntektspostEntitet;
        }
        throw new IllegalStateException();
    }

    public InntektspostBuilder medInntektspostType(String kode) {
        return medInntektspostType(new InntektspostType(kode));
    }

    public InntektspostBuilder medSkatteOgAvgiftsregelType(String kode) {
        return medSkatteOgAvgiftsregelType(new SkatteOgAvgiftsregelType(kode));
    }
}
