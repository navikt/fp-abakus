package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;
import java.time.LocalDate;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class YtelseGrunnlagBuilder {
    private final YtelseGrunnlagEntitet ytelseGrunnlagEntitet;

    YtelseGrunnlagBuilder(YtelseGrunnlagEntitet ytelseGrunnlagEntitet) {
        this.ytelseGrunnlagEntitet = ytelseGrunnlagEntitet;
    }

    static YtelseGrunnlagBuilder ny() {
        return new YtelseGrunnlagBuilder(new YtelseGrunnlagEntitet());
    }

    public YtelseGrunnlagBuilder medArbeidskategori(Arbeidskategori arbeidskategori) {
        this.ytelseGrunnlagEntitet.setArbeidskategori(arbeidskategori);
        return this;
    }

    public YtelseGrunnlagBuilder medDekningsgradProsent(BigDecimal prosent) {
        this.ytelseGrunnlagEntitet.setDekningsgradProsent(new Stillingsprosent(prosent));
        return this;
    }

    public YtelseGrunnlagBuilder medGraderingProsent(BigDecimal prosent) {
        this.ytelseGrunnlagEntitet.setGraderingProsent(new Stillingsprosent(prosent));
        return this;
    }

    public YtelseGrunnlagBuilder medInntektsgrunnlagProsent(BigDecimal prosent) {
        this.ytelseGrunnlagEntitet.setInntektsgrunnlagProsent(new Stillingsprosent(prosent));
        return this;
    }

    public YtelseGrunnlagBuilder medOpprinneligIdentdato(LocalDate dato) {
        this.ytelseGrunnlagEntitet.setOpprinneligIdentdato(dato);
        return this;
    }

    public YtelseGrunnlagBuilder medYtelseStørrelse(YtelseStørrelse ytelseStørrelse) {
        this.ytelseGrunnlagEntitet.leggTilYtelseStørrelse(ytelseStørrelse);
        return this;
    }

    public void tilbakestillStørrelse() {
        this.ytelseGrunnlagEntitet.tilbakestillStørrelse();
    }

    public YtelseGrunnlag build() {
        return ytelseGrunnlagEntitet;
    }

    public YtelseStørrelseBuilder getStørrelseBuilder() {
        return YtelseStørrelseBuilder.ny();
    }

    public YtelseGrunnlagBuilder medArbeidskategori(String kode) {
        return medArbeidskategori(new Arbeidskategori(kode));
    }
}
