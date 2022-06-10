package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseArbeid;

class InfotrygdgrunnlagAnvistAndelMapperTest {

    @Test
    void skal_mappe_grunnlag_til_dagsats_for_kombinasjon_arbeid_og_dagpenger() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER;
        var orgnr = "973093681";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(10000), InntektPeriodeType.MÅNEDLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(462);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, false, dagsatsArbeid));

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(null, BigDecimal.valueOf(4000), InntektPeriodeType.DAGLIG, false, null));
        var dagsatsDagpenger = BigDecimal.valueOf(1994);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, false, dagsatsDagpenger));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(2);
        var arbeidstakerAndel = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent()).findFirst().get();
        assertThat(arbeidstakerAndel.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeidstakerAndel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeidstakerAndel.getRefusjonsgradProsent().getVerdi()).isEqualTo(BigDecimal.ZERO);
        assertThat(arbeidstakerAndel.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeidstakerAndel.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var dagpengeAndel = anvisninger.stream().filter(a -> a.getArbeidsgiver().isEmpty()).findFirst().get();
        assertThat(dagpengeAndel.getInntektskategori()).isEqualTo(Inntektskategori.DAGPENGER);
        assertThat(dagpengeAndel.getRefusjonsgradProsent().getVerdi()).isEqualTo(BigDecimal.ZERO);
        assertThat(dagpengeAndel.getDagsats().getVerdi()).isEqualTo(dagsatsDagpenger);
        assertThat(arbeidstakerAndel.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }


    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_der_den_ene_tar_alt_og_har_referanse_i_anvisninger_fra_infotrygd() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(15000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(495.00), InntektPeriodeType.UKENTLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(791);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, orgnr, false, dagsatsArbeid));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(1);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

    }

    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_der_begge_har_referanse_i_anvisninger_fra_infotrygd() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(15000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(10000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(791);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, orgnr, false, dagsatsArbeid));
        var dagsatsArbeid2 = BigDecimal.valueOf(213);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, orgnr2, false, dagsatsArbeid2));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(2);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var arbeid2 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr2)).findFirst().get();
        assertThat(arbeid2.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr2);
        assertThat(arbeid2.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid2.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid2.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid2);
        assertThat(arbeid2.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }


    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_med_refusjon_uten_referanse_i_anvisning_fra_infotrygd() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(15000.00), InntektPeriodeType.MÅNEDLIG, true, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(10000.00), InntektPeriodeType.MÅNEDLIG, true, null));
        var dagsatsArbeid = BigDecimal.valueOf(692);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, true, dagsatsArbeid));
        var dagsatsArbeid2 = BigDecimal.valueOf(462);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, true, dagsatsArbeid2));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(2);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var arbeid2 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr2)).findFirst().get();
        assertThat(arbeid2.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr2);
        assertThat(arbeid2.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid2.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid2.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid2);
        assertThat(arbeid2.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }

    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_uten_refusjon_uten_referanse_i_anvisning_fra_infotrygd() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(15000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(10000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(692);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, false, dagsatsArbeid));
        var dagsatsArbeid2 = BigDecimal.valueOf(462);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, false, dagsatsArbeid2));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(2);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var arbeid2 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr2)).findFirst().get();
        assertThat(arbeid2.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr2);
        assertThat(arbeid2.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid2.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid2.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid2);
        assertThat(arbeid2.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }

    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_en_med_og_en_uten_refusjon_uten_referanse_i_anvisning_fra_infotrygd() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(15000.00), InntektPeriodeType.MÅNEDLIG, true, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(10000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(692);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, true, dagsatsArbeid));
        var dagsatsArbeid2 = BigDecimal.valueOf(462);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, false, dagsatsArbeid2));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(2);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var arbeid2 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr2)).findFirst().get();
        assertThat(arbeid2.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr2);
        assertThat(arbeid2.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid2.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid2.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid2);
        assertThat(arbeid2.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }

    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_med_refusjon_over_6G() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(60000.00), InntektPeriodeType.MÅNEDLIG, true, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(10000.00), InntektPeriodeType.MÅNEDLIG, true, null));
        var dagsatsArbeid = BigDecimal.valueOf(1800);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, true, dagsatsArbeid));
        var dagsatsArbeid2 = BigDecimal.valueOf(300);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, true, dagsatsArbeid2));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(2);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var arbeid2 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr2)).findFirst().get();
        assertThat(arbeid2.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr2);
        assertThat(arbeid2.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid2.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid2.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid2);
        assertThat(arbeid2.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }


    @Test
    void skal_mappe_grunnlag_til_dagsats_for_to_arbeidstakere_en_med_refusjon_over_6G() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var arbeidskategori = Arbeidskategori.ARBEIDSTAKER;
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        var infotrygdgrunnlag = new ArrayList<InfotrygdYtelseArbeid>();
        var infotrygdutbetalinger = new ArrayList<InfotrygdYtelseAnvist>();

        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(60000.00), InntektPeriodeType.MÅNEDLIG, true, null));
        infotrygdgrunnlag.add(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(10000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(2100);
        infotrygdutbetalinger.add(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, true, dagsatsArbeid));


        // Act
        var anvisninger = InfotrygdgrunnlagAnvistAndelMapper.oversettYtelseArbeidTilAnvisteAndeler(arbeidskategori, infotrygdgrunnlag, infotrygdutbetalinger);

        // Assert
        assertThat(anvisninger.size()).isEqualTo(1);
        var arbeid1 = anvisninger.stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ONE, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);
    }

}
