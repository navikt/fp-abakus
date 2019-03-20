package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ytelse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;

public class Grunnlag {

    private Arbeidskategori arbeidskategori;
    private LocalDate opprinneligIdentDato;
    private BigDecimal dekningsgradProsent;
    private BigDecimal graderingProsent;
    private BigDecimal inntektsgrunnlagProsent;
    private List<Fordeling> fordeling;

    public Grunnlag() {
    }

    public Arbeidskategori getArbeidskategori() {
        return arbeidskategori;
    }

    public void setArbeidskategori(Arbeidskategori arbeidskategori) {
        this.arbeidskategori = arbeidskategori;
    }

    public LocalDate getOpprinneligIdentDato() {
        return opprinneligIdentDato;
    }

    public void setOpprinneligIdentDato(LocalDate opprinneligIdentDato) {
        this.opprinneligIdentDato = opprinneligIdentDato;
    }

    public BigDecimal getDekningsgradProsent() {
        return dekningsgradProsent;
    }

    public void setDekningsgradProsent(BigDecimal dekningsgradProsent) {
        this.dekningsgradProsent = dekningsgradProsent;
    }

    public BigDecimal getGraderingProsent() {
        return graderingProsent;
    }

    public void setGraderingProsent(BigDecimal graderingProsent) {
        this.graderingProsent = graderingProsent;
    }

    public BigDecimal getInntektsgrunnlagProsent() {
        return inntektsgrunnlagProsent;
    }

    public void setInntektsgrunnlagProsent(BigDecimal inntektsgrunnlagProsent) {
        this.inntektsgrunnlagProsent = inntektsgrunnlagProsent;
    }

    public List<Fordeling> getFordeling() {
        return fordeling;
    }

    public void setFordeling(List<Fordeling> fordeling) {
        this.fordeling = fordeling;
    }
}
