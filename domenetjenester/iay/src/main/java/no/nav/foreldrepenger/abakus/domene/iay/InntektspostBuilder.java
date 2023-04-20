package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.LønnsinntektBeskrivelse;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseType;
import no.nav.foreldrepenger.abakus.typer.Beløp;

public class InntektspostBuilder {
    private Inntektspost inntektspost;

    InntektspostBuilder(Inntektspost inntektspost) {
        this.inntektspost = inntektspost;
    }

    public static InntektspostBuilder ny() {
        return new InntektspostBuilder(new Inntektspost());
    }

    public InntektspostBuilder medInntektspostType(InntektspostType inntektspostType) {
        this.inntektspost.setInntektspostType(inntektspostType);
        return this;
    }

    public InntektspostBuilder medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType skatteOgAvgiftsregelType) {
        this.inntektspost.setSkatteOgAvgiftsregelType(skatteOgAvgiftsregelType);
        return this;
    }

    public InntektspostBuilder medLønnsinntektBeskrivelse(LønnsinntektBeskrivelse lønnsinntektBeskrivelse) {
        this.inntektspost.setLønnsinntektBeskrivelse(lønnsinntektBeskrivelse);
        return this;
    }

    public InntektspostBuilder medPeriode(LocalDate fraOgMed, LocalDate tilOgMed) {
        this.inntektspost.setPeriode(fraOgMed, tilOgMed);
        return this;
    }

    public InntektspostBuilder medBeløp(BigDecimal verdi) {
        this.inntektspost.setBeløp(new Beløp(verdi));
        return this;
    }

    public InntektspostBuilder medYtelse(UtbetaltYtelseType offentligYtelseType) {
        this.inntektspost.setYtelse(offentligYtelseType);
        return this;
    }

    public Inntektspost build() {
        if (inntektspost.hasValues()) {
            return inntektspost;
        }
        throw new IllegalStateException();
    }

}
