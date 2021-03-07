package no.nav.foreldrepenger.abakus.domene.iay;

import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

public class YrkesaktivitetBuilder {
    private final Yrkesaktivitet kladd;
    private boolean oppdaterer;

    private YrkesaktivitetBuilder(Yrkesaktivitet kladd, boolean oppdaterer) {
        this.kladd = kladd;
        this.oppdaterer = oppdaterer;
    }

    static YrkesaktivitetBuilder ny() {
        return new YrkesaktivitetBuilder(new Yrkesaktivitet(), false);
    }

    static YrkesaktivitetBuilder oppdatere(Yrkesaktivitet oppdatere) {
        return new YrkesaktivitetBuilder(oppdatere, true);
    }

    public static YrkesaktivitetBuilder oppdatere(Optional<Yrkesaktivitet> oppdatere) {
        return oppdatere.map(YrkesaktivitetBuilder::oppdatere).orElseGet(YrkesaktivitetBuilder::ny);
    }

    public static AktivitetsAvtaleBuilder nyAktivitetsAvtaleBuilder() {
        return AktivitetsAvtaleBuilder.ny();
    }

    public static PermisjonBuilder nyPermisjonBuilder() {
        return PermisjonBuilder.ny();
    }

    public YrkesaktivitetBuilder medArbeidType(ArbeidType arbeidType) {
        kladd.setArbeidType(arbeidType);
        return this;
    }

    public YrkesaktivitetBuilder medArbeidsforholdId(InternArbeidsforholdRef arbeidsforholdId) {
        kladd.setArbeidsforholdId(arbeidsforholdId);
        return this;
    }

    public YrkesaktivitetBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        kladd.setArbeidsgiver(arbeidsgiver);
        return this;
    }

    public YrkesaktivitetBuilder medArbeidsgiverNavn(String arbeidsgiver) {
        kladd.setNavnArbeidsgiverUtland(arbeidsgiver);
        return this;
    }

    Yrkesaktivitet getKladd() {
        return kladd;
    }

    public PermisjonBuilder getPermisjonBuilder() {
        return nyPermisjonBuilder();
    }

    public YrkesaktivitetBuilder leggTilPermisjon(Permisjon permisjon) {
        kladd.leggTilPermisjon(permisjon);
        return this;
    }

    public YrkesaktivitetBuilder tilbakestillPermisjon() {
        kladd.tilbakestillPermisjon();
        return this;
    }

    public YrkesaktivitetBuilder tilbakestillAvtaler() {
        kladd.tilbakestillAvtaler();
        return this;
    }

    public YrkesaktivitetBuilder tilbakestillAvtalerInklusiveInntektFrilans() {
        kladd.tilbakestillAvtalerInklusiveInntektFrilans();
        return this;
    }

    public AktivitetsAvtaleBuilder getAktivitetsAvtaleBuilder() {
        return nyAktivitetsAvtaleBuilder();
    }

    public YrkesaktivitetBuilder leggTilAktivitetsAvtale(AktivitetsAvtaleBuilder aktivitetsAvtale) {
        if (!aktivitetsAvtale.isOppdatering()) {
            AktivitetsAvtale aktivitetsAvtaleEntitet = aktivitetsAvtale.build();
            kladd.leggTilAktivitetsAvtale(aktivitetsAvtaleEntitet);
        }
        return this;
    }

    public YrkesaktivitetBuilder migrerFraRegisterTilOverstyrt() {
        this.oppdaterer = false;
        return this;
    }

    public boolean getErOppdatering() {
        return this.oppdaterer;
    }

    public Yrkesaktivitet build() {
        return kladd;
    }

    public AktivitetsAvtaleBuilder getAktivitetsAvtaleBuilder(IntervallEntitet aktivitetsPeriode, boolean erAnsettelsesperioden) {
        AktivitetsAvtaleBuilder oppdater = AktivitetsAvtaleBuilder.oppdater(kladd.getAlleAktivitetsAvtaler()
            .stream()
            .filter(aa -> aa.matcherPeriode(aktivitetsPeriode)
                && (!kladd.erArbeidsforhold() || aa.erAnsettelsesPeriode() == erAnsettelsesperioden))
            .findFirst());
        oppdater.medPeriode(aktivitetsPeriode);
        return oppdater;
    }

    public void fjernPeriode(IntervallEntitet aktivitetsPeriode) {
        kladd.fjernPeriode(aktivitetsPeriode);
    }

    public YrkesaktivitetBuilder medArbeidType(String kode) {
        return medArbeidType(ArbeidType.fraKode(kode));
    }

}
