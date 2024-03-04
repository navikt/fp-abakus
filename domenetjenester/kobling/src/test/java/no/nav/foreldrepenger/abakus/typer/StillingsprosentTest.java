package no.nav.foreldrepenger.abakus.typer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.jupiter.api.Test;


class StillingsprosentTest {

    @Test
    void skalReturnereAbsolutVerdiHvisMinus() {
        Stillingsprosent minus = new Stillingsprosent(BigDecimal.valueOf(-90));

        assertThat(minus.getVerdi()).isEqualTo(BigDecimal.valueOf(90));
    }

    @Test
    void skalReturnereTidelVerdiHvisStørrereEn500() {
        Stillingsprosent minus = new Stillingsprosent(BigDecimal.valueOf(600));

        assertThat(minus.getVerdi()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    void skalReturnereTidelHvisMinusOgStørrereEn500() {
        Stillingsprosent minus = new Stillingsprosent(BigDecimal.valueOf(-600));

        assertThat(minus.getVerdi()).isEqualByComparingTo(BigDecimal.valueOf(60));
    }

    @Test
    void skalReturnereVerdiMellom0og500() {
        BigDecimal spVerdi = BigDecimal.valueOf(100);
        Stillingsprosent hundre = new Stillingsprosent(spVerdi);

        assertThat(hundre.getVerdi()).isEqualTo(spVerdi);
    }

    @Test
    void skalReturnereNullVerdiHvisNullValue() {
        BigDecimal value = null;
        Stillingsprosent nullValue = new Stillingsprosent(value);

        assertThat(nullValue.getVerdi()).isNull();
    }

    @Test
    void skalReturnereFalseHvisEqualsMedNullVerdi() {
        BigDecimal value = null;
        Stillingsprosent nullValue = new Stillingsprosent(value);
        Stillingsprosent hundreValue = new Stillingsprosent(BigDecimal.TEN);

        assertThat(nullValue.equals(hundreValue)).isFalse();
        assertThat(hundreValue.equals(nullValue)).isFalse();
    }

    @Test
    void skalReturnereTrueHvisEqualsMedLikeObjekter() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);
        Stillingsprosent sp2 = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.equals(sp2)).isTrue();
    }

    @Test
    void skalReturnereTrueHvisEqualsToNullObjekter() {
        Stillingsprosent sp = Stillingsprosent.nullProsent();
        Stillingsprosent sp2 = Stillingsprosent.nullProsent();

        assertThat(sp.equals(sp2)).isTrue();
    }

    @Test
    void skalReturnereTrueHvisEqualsMedSegSelv() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.equals(sp)).isTrue();
    }

    @Test
    void skalReturnereFalseHvisEqualsMedNull() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.equals(null)).isFalse();
    }

    @Test
    void skalReturnereHashMedNullReferanse() {
        BigDecimal value = null;
        Stillingsprosent sp = new Stillingsprosent(value);

        assertThat(sp.hashCode()).isEqualTo(31); // Array av ett element som er null
    }

    @Test
    void skalReturnereVerdiMedToString() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.toString()).contains("verdi=" + sp.getVerdi());
    }

    @Test
    void skalReturnereTrueHvisVerdiErZero() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.ZERO);

        assertThat(sp.erNulltall()).isTrue();
    }

    @Test
    void skalReturnereIndexHvisVerdiErNull() {
        BigDecimal value = null;
        Stillingsprosent sp = new Stillingsprosent(value);

        assertThat(sp.getIndexKey()).isEqualTo("-");
    }

    @Test
    void skalReturnereNullVerdiForNullStillingsprosent() {
        Stillingsprosent sp = Stillingsprosent.nullProsent();

        assertThat(sp.getVerdi()).isNull();
    }
}
