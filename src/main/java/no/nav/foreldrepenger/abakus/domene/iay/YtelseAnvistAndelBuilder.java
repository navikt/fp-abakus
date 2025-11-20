package no.nav.foreldrepenger.abakus.domene.iay;

import java.math.BigDecimal;

import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

public class YtelseAnvistAndelBuilder {
    private final YtelseAnvistAndel ytelseAnvistAndel;

    YtelseAnvistAndelBuilder(YtelseAnvistAndel ytelseAnvistAndel) {
        this.ytelseAnvistAndel = ytelseAnvistAndel;
    }

    public static YtelseAnvistAndelBuilder ny() {
        return new YtelseAnvistAndelBuilder(new YtelseAnvistAndel());
    }

    public YtelseAnvistAndelBuilder medDagsats(BigDecimal dagsats) {
        if (dagsats != null) {
            this.ytelseAnvistAndel.setDagsats(new Beløp(dagsats));
        }
        return this;
    }

    public YtelseAnvistAndelBuilder medUtbetalingsgrad(BigDecimal verdi) {
        if (verdi != null) {
            this.ytelseAnvistAndel.setUtbetalingsgradProsent(Stillingsprosent.utbetalingsgrad(verdi));
        }
        return this;
    }

    public YtelseAnvistAndelBuilder medRefusjonsgrad(BigDecimal verdi) {
        if (verdi != null) {
            this.ytelseAnvistAndel.setRefusjonsgradProsent(Stillingsprosent.utbetalingsgrad(verdi));
        }
        return this;
    }

    public YtelseAnvistAndelBuilder medArbeidsgiver(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver != null) {
            this.ytelseAnvistAndel.setArbeidsgiver(arbeidsgiver);
        }
        return this;
    }

    public YtelseAnvistAndelBuilder medArbeidsforholdRef(InternArbeidsforholdRef arbeidsforholdRef) {
        this.ytelseAnvistAndel.setArbeidsforholdRef(arbeidsforholdRef);
        return this;
    }


    public YtelseAnvistAndelBuilder medInntektskategori(Inntektskategori inntektskategori) {
        if (inntektskategori != null) {
            this.ytelseAnvistAndel.setInntektskategori(inntektskategori);
        }
        return this;
    }

    public YtelseAnvistAndel build() {
        return ytelseAnvistAndel;
    }

}
