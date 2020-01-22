package no.nav.foreldrepenger.abakus.typer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.Test;

public class StillingsprosentTest {

    @Test
    public void skalReturnereAbsolutVerdiHvisMinus() {
        Stillingsprosent minus = new Stillingsprosent(BigDecimal.valueOf(-90));

        assertThat(minus.getVerdi()).isEqualTo(BigDecimal.valueOf(90));
    }

    @Test
    public void skalReturnere500VerdiHvisStørrereEn500() {
        Stillingsprosent minus = new Stillingsprosent(BigDecimal.valueOf(600));

        assertThat(minus.getVerdi()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    public void skalReturnere500HvisMinusOgStørrereEn500() {
        Stillingsprosent minus = new Stillingsprosent(BigDecimal.valueOf(-600));

        assertThat(minus.getVerdi()).isEqualTo(BigDecimal.valueOf(500));
    }

    @Test
    public void skalReturnereVerdiMellom0og500() {
        BigDecimal spVerdi = BigDecimal.valueOf(100);
        Stillingsprosent hundre = new Stillingsprosent(spVerdi);

        assertThat(hundre.getVerdi()).isEqualTo(spVerdi);
    }

    @Test
    public void skalReturnere0VerdiHvisNull() {
        BigDecimal value = null;
        Stillingsprosent nullValue = new Stillingsprosent(value);

        assertThat(nullValue.getVerdi()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void skalReturnereFalseHvisEqualsMedNullVerdi() {
        BigDecimal value = null;
        Stillingsprosent nullValue = new Stillingsprosent(value);
        Stillingsprosent hundreValue = new Stillingsprosent(BigDecimal.TEN);

        assertThat(nullValue.equals(hundreValue)).isFalse();
        assertThat(hundreValue.equals(nullValue)).isFalse();
    }

    @Test
    public void skalReturnereTrueHvisEqualsMedLikeObjekter() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);
        Stillingsprosent sp2 = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.equals(sp2)).isTrue();
    }

    @Test
    public void skalReturnereTrueHvisEqualsMedSegSelv() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.equals(sp)).isTrue();
    }

    @Test
    public void skalReturnereFalseHvisEqualsMedNull() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.equals(null)).isFalse();
    }

    @Test
    public void skalReturnereHashMedNullReferanse() {
        BigDecimal value = null;
        Stillingsprosent sp = new Stillingsprosent(value);

        assertThat(sp.hashCode()).isNotNull();
    }

    @Test
    public void skalReturnereVerdiMedToString() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.TEN);

        assertThat(sp.toString()).contains("verdi=" + sp.getVerdi());
    }

    @Test
    public void skalReturnereTrueHvisVerdiErZero() {
        Stillingsprosent sp = new Stillingsprosent(BigDecimal.ZERO);

        assertThat(sp.erNulltall()).isTrue();
    }

    @Test
    public void skalReturnereIndexHvisVerdiErNull() {
        BigDecimal value = null;
        Stillingsprosent sp = new Stillingsprosent(value);

        assertThat(sp.getIndexKey()).isEqualTo("0.00");
    }

    @Test
    public void skalReturnereZeroVerdiForNullStillingsprosent() {
        Stillingsprosent sp = Stillingsprosent.nullProsent();

        assertThat(sp.getVerdi()).isEqualTo(BigDecimal.ZERO);
    }
}
