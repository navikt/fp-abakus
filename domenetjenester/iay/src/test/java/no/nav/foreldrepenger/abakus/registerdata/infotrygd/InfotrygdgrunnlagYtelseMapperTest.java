package no.nav.foreldrepenger.abakus.registerdata.infotrygd;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.Arbeidskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektPeriodeType;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.TemaUnderkategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseAnvist;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseArbeid;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.dto.InfotrygdYtelseGrunnlag;

class InfotrygdgrunnlagYtelseMapperTest {

    @Test
    void skal_mappe_grunnlag_til_dagsats_for_kombinasjon_arbeid_og_dagpenger() {
        // Arrange
        var fom = LocalDate.now().minusDays(3);
        var tom = LocalDate.now();
        var utbetalingsgrad = BigDecimal.valueOf(100);
        var builder = InfotrygdYtelseGrunnlag.getBuilder()
            .medGradering(utbetalingsgrad)
            .medYtelseType(YtelseType.PLEIEPENGER_SYKT_BARN)
            .medTemaUnderkategori(TemaUnderkategori.PÅRØRENDE_PLEIEPENGER)
            .medArbeidskategori(Arbeidskategori.KOMBINASJON_ARBEIDSTAKER_OG_DAGPENGER)
            .medVedtaksPeriodeFom(fom)
            .medVedtaksPeriodeTom(tom)
            .medVedtattTidspunkt(LocalDateTime.now());
        var orgnr = "973093681";
        builder.leggTilArbeidsforhold(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(10000), InntektPeriodeType.MÅNEDLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(462);
        builder.leggTillAnvistPerioder(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, orgnr, false, dagsatsArbeid));

        builder.leggTilArbeidsforhold(new InfotrygdYtelseArbeid(null, BigDecimal.valueOf(1994), InntektPeriodeType.DAGLIG, false, null));
        var dagsatsDagpenger = BigDecimal.valueOf(1994);
        builder.leggTillAnvistPerioder(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, null, false, dagsatsDagpenger));

        InfotrygdYtelseGrunnlag grunnlag = builder.build();
        var aktørYtelseBuilder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        InfotrygdgrunnlagYtelseMapper.oversettInfotrygdYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag);
        var aktørYtelse = aktørYtelseBuilder.build();

        // Assert
        assertThat(aktørYtelse.getAlleYtelser().size()).isEqualTo(1);
        var ytelseMappet = aktørYtelse.getAlleYtelser().iterator().next();
        assertThat(ytelseMappet.getYtelseAnvist().size()).isEqualTo(1);
        var ytelseAnvist = ytelseMappet.getYtelseAnvist().iterator().next();
        assertThat(ytelseAnvist.getAnvistFOM()).isEqualTo(fom);
        assertThat(ytelseAnvist.getAnvistTOM()).isEqualTo(tom);

        assertThat(ytelseAnvist.getYtelseAnvistAndeler().size()).isEqualTo(2);
        var arbeidstakerAndel = ytelseAnvist.getYtelseAnvistAndeler().stream().filter(a -> a.getArbeidsgiver().isPresent()).findFirst().get();
        assertThat(arbeidstakerAndel.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeidstakerAndel.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeidstakerAndel.getRefusjonsgradProsent().getVerdi().compareTo(BigDecimal.ZERO)).isEqualTo(0);
        assertThat(arbeidstakerAndel.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeidstakerAndel.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

        var dagpengeAndel = ytelseAnvist.getYtelseAnvistAndeler().stream().filter(a -> a.getArbeidsgiver().isEmpty()).findFirst().get();
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
        var builder = InfotrygdYtelseGrunnlag.getBuilder()
            .medGradering(utbetalingsgrad)
            .medYtelseType(YtelseType.PLEIEPENGER_SYKT_BARN)
            .medTemaUnderkategori(TemaUnderkategori.PÅRØRENDE_PLEIEPENGER)
            .medArbeidskategori(Arbeidskategori.ARBEIDSTAKER)
            .medVedtaksPeriodeFom(fom)
            .medVedtaksPeriodeTom(tom)
            .medVedtattTidspunkt(LocalDateTime.now());
        var orgnr = "973093681";
        var orgnr2 = "910909088";

        builder.leggTilArbeidsforhold(new InfotrygdYtelseArbeid(orgnr, BigDecimal.valueOf(15000.00), InntektPeriodeType.MÅNEDLIG, false, null));
        builder.leggTilArbeidsforhold(new InfotrygdYtelseArbeid(orgnr2, BigDecimal.valueOf(495.00), InntektPeriodeType.UKENTLIG, false, null));
        var dagsatsArbeid = BigDecimal.valueOf(791);
        builder.leggTillAnvistPerioder(new InfotrygdYtelseAnvist(fom, tom, utbetalingsgrad, orgnr, false, dagsatsArbeid));

        InfotrygdYtelseGrunnlag grunnlag = builder.build();
        var aktørYtelseBuilder = InntektArbeidYtelseAggregatBuilder.AktørYtelseBuilder.oppdatere(Optional.empty());

        // Act
        InfotrygdgrunnlagYtelseMapper.oversettInfotrygdYtelseGrunnlagTilYtelse(aktørYtelseBuilder, grunnlag);
        var aktørYtelse = aktørYtelseBuilder.build();

        // Assert
        assertThat(aktørYtelse.getAlleYtelser().size()).isEqualTo(1);
        var ytelseMappet = aktørYtelse.getAlleYtelser().iterator().next();
        assertThat(ytelseMappet.getYtelseAnvist().size()).isEqualTo(1);
        var ytelseAnvist = ytelseMappet.getYtelseAnvist().iterator().next();
        assertThat(ytelseAnvist.getAnvistFOM()).isEqualTo(fom);
        assertThat(ytelseAnvist.getAnvistTOM()).isEqualTo(tom);

        assertThat(ytelseAnvist.getYtelseAnvistAndeler().size()).isEqualTo(1);
        var arbeid1 = ytelseAnvist.getYtelseAnvistAndeler().stream().filter(a -> a.getArbeidsgiver().isPresent() && a.getArbeidsgiver().get().getOrgnr().getId().equals(orgnr)).findFirst().get();
        assertThat(arbeid1.getArbeidsgiver().get().getIdentifikator()).isEqualTo(orgnr);
        assertThat(arbeid1.getInntektskategori()).isEqualTo(Inntektskategori.ARBEIDSTAKER);
        assertThat(arbeid1.getRefusjonsgradProsent().getVerdi()).isCloseTo(BigDecimal.ZERO, Offset.offset(BigDecimal.valueOf(0.000001)));
        assertThat(arbeid1.getDagsats().getVerdi()).isEqualTo(dagsatsArbeid);
        assertThat(arbeid1.getUtbetalingsgradProsent().getVerdi()).isEqualTo(utbetalingsgrad);

    }

}
