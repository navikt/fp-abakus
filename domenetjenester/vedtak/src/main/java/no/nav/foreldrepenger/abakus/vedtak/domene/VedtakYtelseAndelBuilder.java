package no.nav.foreldrepenger.abakus.vedtak.domene;

import java.math.BigDecimal;

import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class VedtakYtelseAndelBuilder {
    private final VedtakYtelseAndel vedtakYtelseAndel;

    VedtakYtelseAndelBuilder(VedtakYtelseAndel vedtakYtelseAndel) {
        this.vedtakYtelseAndel = vedtakYtelseAndel;
    }

    public static VedtakYtelseAndelBuilder ny() {
        return new VedtakYtelseAndelBuilder(new VedtakYtelseAndel());
    }

    public VedtakYtelseAndelBuilder medDagsats(BigDecimal dagsats) {
        if (dagsats != null) {
            this.vedtakYtelseAndel.setDagsats(new Beløp(dagsats));
        }
        return this;
    }

    public VedtakYtelseAndelBuilder medUtbetalingsgrad(BigDecimal verdi) {
        if (verdi != null) {
            this.vedtakYtelseAndel.setUtbetalingsgradProsent(new Stillingsprosent(verdi));
        }
        return this;
    }

    public VedtakYtelseAndelBuilder medRefusjonsgrad(BigDecimal verdi) {
        if (verdi != null) {
            this.vedtakYtelseAndel.setRefusjonsgradProsent(new Stillingsprosent(verdi));
        }
        return this;
    }

    public VedtakYtelseAndelBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver != null) {
            this.vedtakYtelseAndel.setArbeidsgiver(arbeidsgiver);
        }
        return this;
    }

    public VedtakYtelseAndelBuilder medArbeidsforholdId(String arbeidsforholdId) {
        this.vedtakYtelseAndel.setArbeidsforholdId(arbeidsforholdId);
        return this;
    }


    public VedtakYtelseAndelBuilder medInntektskategori(Inntektskategori inntektskategori) {
        if (inntektskategori != null) {
            this.vedtakYtelseAndel.setInntektskategori(inntektskategori);
        }
        return this;
    }

    public VedtakYtelseAndel build() {
        return vedtakYtelseAndel;
    }

}
