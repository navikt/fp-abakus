package no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten;

public enum InntektsFilter {

    OPPTJENINGSGRUNNLAG("PensjonsgivendeA-Inntekt", InntektsFormål.FORMAAL_PGI),
    BEREGNINGSGRUNNLAG("8-28", InntektsFormål.FORMAAL_FORELDREPENGER),
    SAMMENLIGNINGSGRUNNLAG("8-30", InntektsFormål.FORMAAL_FORELDREPENGER);

    private String kode;
    private InntektsFormål formål;

    InntektsFilter(String kode, InntektsFormål formål) {
        this.kode = kode;
        this.formål = formål;
    }

    public String getKode() {
        return kode;
    }

    public InntektsFormål getFormål() {
        return formål;
    }
}
