package no.nav.foreldrepenger.abakus.registerdata;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.LønnsinntektBeskrivelse;
import no.nav.abakus.iaygrunnlag.kodeverk.SkatteOgAvgiftsregelType;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

class ByggLønnsinntektInntektTjenesteTest {

    public static final String ORGNR = "910909088";

    @Test
    void skal_mappe_svalbarinntekt() {
        var månedsinntektBuilder = new Månedsinntekt.Builder();
        var svalbardinntekt = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.SVALBARD.getOffisiellKode())
            .build();
        var inntektsInformasjon = new InntektsInformasjon(List.of(svalbardinntekt), List.of(), InntektskildeType.INNTEKT_BEREGNING);
        var arbeidsgivereLookup = Map.of(ORGNR, Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)));

        // Act
        var aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        ByggLønnsinntektInntektTjeneste.mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder,
            arbeidsgivereLookup);

        // Assert
        var inntekter = aktørInntektBuilder.build().getInntekt();
        assertThat(inntekter.size()).isEqualTo(1);
        var inntekt = inntekter.iterator().next();
        var poster = inntekt.getAlleInntektsposter();
        assertThat(poster.size()).isEqualTo(1);
        var inntektspost = poster.iterator().next();

        assertThat(inntektspost.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        assertThat(inntektspost.getLønnsinntektBeskrivelse()).isEqualTo(LønnsinntektBeskrivelse.UDEFINERT);
        assertThat(inntektspost.getSkatteOgAvgiftsregelType()).isEqualTo(SkatteOgAvgiftsregelType.SVALBARD);
        assertThat(inntektspost.getBeløp().getVerdi().compareTo(BigDecimal.TEN)).isEqualTo(0);
    }


    @Test
    void skal_prioritere_NETTOLØNN_FOR_SJØFOLK() {
        var månedsinntektBuilder = new Månedsinntekt.Builder();
        var svalbardinntekt = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.SVALBARD.getOffisiellKode())
            .build();

        var særskiltFradrag = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK.getOffisiellKode())
            .build();

        var inntektsInformasjon = new InntektsInformasjon(List.of(svalbardinntekt, særskiltFradrag), List.of(), InntektskildeType.INNTEKT_BEREGNING);
        var arbeidsgivereLookup = Map.of(ORGNR, Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)));

        // Act
        var aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        ByggLønnsinntektInntektTjeneste.mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder,
            arbeidsgivereLookup);

        // Assert
        var inntekter = aktørInntektBuilder.build().getInntekt();
        assertThat(inntekter.size()).isEqualTo(1);
        var inntekt = inntekter.iterator().next();
        var poster = inntekt.getAlleInntektsposter();
        assertThat(poster.size()).isEqualTo(1);
        var inntektspost = poster.iterator().next();

        assertThat(inntektspost.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        assertThat(inntektspost.getLønnsinntektBeskrivelse()).isEqualTo(LønnsinntektBeskrivelse.UDEFINERT);
        assertThat(inntektspost.getSkatteOgAvgiftsregelType()).isEqualTo(SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK);
        assertThat(inntektspost.getBeløp().getVerdi().compareTo(BigDecimal.valueOf(20))).isEqualTo(0);
    }

    @Test
    void skal_prioritere_SÆRSKILT_FRADRAG_FOR_SJØFOLK() {
        var månedsinntektBuilder = new Månedsinntekt.Builder();
        var svalbardinntekt = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.SVALBARD.getOffisiellKode())
            .build();

        var særskiltFradrag = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK.getOffisiellKode())
            .build();

        var inntektsInformasjon = new InntektsInformasjon(List.of(svalbardinntekt, særskiltFradrag), List.of(), InntektskildeType.INNTEKT_BEREGNING);
        var arbeidsgivereLookup = Map.of(ORGNR, Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)));

        // Act
        var aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        ByggLønnsinntektInntektTjeneste.mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder,
            arbeidsgivereLookup);

        // Assert
        var inntekter = aktørInntektBuilder.build().getInntekt();
        assertThat(inntekter.size()).isEqualTo(1);
        var inntekt = inntekter.iterator().next();
        var poster = inntekt.getAlleInntektsposter();
        assertThat(poster.size()).isEqualTo(1);
        var inntektspost = poster.iterator().next();

        assertThat(inntektspost.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        assertThat(inntektspost.getLønnsinntektBeskrivelse()).isEqualTo(LønnsinntektBeskrivelse.UDEFINERT);
        assertThat(inntektspost.getSkatteOgAvgiftsregelType()).isEqualTo(SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK);
        assertThat(inntektspost.getBeløp().getVerdi().compareTo(BigDecimal.valueOf(20))).isEqualTo(0);
    }

    @Test
    void skal_prioritere_SÆRSKILT_FRADRAG_FOR_SJØFOLK_foran_NETTOLØNN_FOR_SJØFOLK() {
        var månedsinntektBuilder = new Månedsinntekt.Builder();
        var svalbardinntekt = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.NETTOLØNN_FOR_SJØFOLK.getOffisiellKode())
            .build();

        var særskiltFradrag = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK.getOffisiellKode())
            .build();

        var inntektsInformasjon = new InntektsInformasjon(List.of(svalbardinntekt, særskiltFradrag), List.of(), InntektskildeType.INNTEKT_BEREGNING);
        var arbeidsgivereLookup = Map.of(ORGNR, Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)));

        // Act
        var aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        ByggLønnsinntektInntektTjeneste.mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder,
            arbeidsgivereLookup);

        // Assert
        var inntekter = aktørInntektBuilder.build().getInntekt();
        assertThat(inntekter.size()).isEqualTo(1);
        var inntekt = inntekter.iterator().next();
        var poster = inntekt.getAlleInntektsposter();
        assertThat(poster.size()).isEqualTo(1);
        var inntektspost = poster.iterator().next();

        assertThat(inntektspost.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        assertThat(inntektspost.getLønnsinntektBeskrivelse()).isEqualTo(LønnsinntektBeskrivelse.UDEFINERT);
        assertThat(inntektspost.getSkatteOgAvgiftsregelType()).isEqualTo(SkatteOgAvgiftsregelType.SÆRSKILT_FRADRAG_FOR_SJØFOLK);
        assertThat(inntektspost.getBeløp().getVerdi().compareTo(BigDecimal.valueOf(20))).isEqualTo(0);
    }


    @Test
    void skal_mappe_omsorgsstønad() {
        var månedsinntektBuilder = new Månedsinntekt.Builder();


        var omsorgsstønad = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medLønnsbeskrivelseKode(LønnsinntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE.getOffisiellKode())
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.NETTOLØNN.getOffisiellKode())
            .build();

        var inntektsInformasjon = new InntektsInformasjon(List.of(omsorgsstønad), List.of(), InntektskildeType.INNTEKT_BEREGNING);
        var arbeidsgivereLookup = Map.of(ORGNR, Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)));

        // Act
        var aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        ByggLønnsinntektInntektTjeneste.mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder,
            arbeidsgivereLookup);

        // Assert
        var inntekter = aktørInntektBuilder.build().getInntekt();
        assertThat(inntekter.size()).isEqualTo(1);
        var inntekt = inntekter.iterator().next();
        var poster = inntekt.getAlleInntektsposter();
        assertThat(poster.size()).isEqualTo(1);
        var inntektspost = poster.iterator().next();

        assertThat(inntektspost.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        assertThat(inntektspost.getSkatteOgAvgiftsregelType()).isEqualTo(SkatteOgAvgiftsregelType.NETTOLØNN);
        assertThat(inntektspost.getLønnsinntektBeskrivelse()).isEqualTo(LønnsinntektBeskrivelse.KOMMUNAL_OMSORGSLOENN_OG_FOSTERHJEMSGODTGJOERELSE);
        assertThat(inntektspost.getBeløp().getVerdi().compareTo(BigDecimal.valueOf(10))).isEqualTo(0);
    }

    @Test
    void skal_mappe_ukjent_lønnsbeskrivelse_til_udefinert() {
        var månedsinntektBuilder = new Månedsinntekt.Builder();


        var ukjentLønn = månedsinntektBuilder.medBeløp(BigDecimal.TEN)
            .medMåned(YearMonth.now())
            .medArbeidsgiver(ORGNR)
            .medArbeidsforholdRef(null)
            .medLønnsbeskrivelseKode(null)
            .medLønnsbeskrivelseKode("enUkjentKode")
            .medSkatteOgAvgiftsregelType(SkatteOgAvgiftsregelType.NETTOLØNN.getOffisiellKode())
            .build();

        var inntektsInformasjon = new InntektsInformasjon(List.of(ukjentLønn), List.of(), InntektskildeType.INNTEKT_BEREGNING);
        var arbeidsgivereLookup = Map.of(ORGNR, Arbeidsgiver.virksomhet(new OrgNummer(ORGNR)));

        // Act
        var aktørInntektBuilder = InntektArbeidYtelseAggregatBuilder.AktørInntektBuilder.oppdatere(Optional.empty());
        ByggLønnsinntektInntektTjeneste.mapLønnsinntekter(inntektsInformasjon, aktørInntektBuilder,
            arbeidsgivereLookup);

        // Assert
        var inntekter = aktørInntektBuilder.build().getInntekt();
        assertThat(inntekter.size()).isEqualTo(1);
        var inntekt = inntekter.iterator().next();
        var poster = inntekt.getAlleInntektsposter();
        assertThat(poster.size()).isEqualTo(1);
        var inntektspost = poster.iterator().next();

        assertThat(inntektspost.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        assertThat(inntektspost.getSkatteOgAvgiftsregelType()).isEqualTo(SkatteOgAvgiftsregelType.NETTOLØNN);
        assertThat(inntektspost.getLønnsinntektBeskrivelse()).isEqualTo(LønnsinntektBeskrivelse.UDEFINERT);
        assertThat(inntektspost.getBeløp().getVerdi().compareTo(BigDecimal.valueOf(10))).isEqualTo(0);
    }


}
