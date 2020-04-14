package no.nav.foreldrepenger.abakus.registerdata;

import no.nav.abakus.iaygrunnlag.kodeverk.InntektskildeType;
import no.nav.abakus.iaygrunnlag.kodeverk.InntektspostType;
import no.nav.abakus.iaygrunnlag.kodeverk.OrganisasjonType;
import no.nav.abakus.iaygrunnlag.kodeverk.UtbetaltYtelseFraOffentligeType;
import no.nav.foreldrepenger.abakus.domene.iay.AktørInntekt;
import no.nav.foreldrepenger.abakus.domene.iay.Inntekt;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregat;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Inntektspost;
import no.nav.foreldrepenger.abakus.domene.iay.VersjonType;
import no.nav.foreldrepenger.abakus.domene.virksomhet.Virksomhet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.virksomhet.VirksomhetTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.FrilansArbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.InntektsInformasjon;
import no.nav.foreldrepenger.abakus.registerdata.inntekt.komponenten.Månedsinntekt;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MapInntektFraDtoTilDomeneTest {

    private VirksomhetTjeneste virksomhetTjeneste = mock(VirksomhetTjeneste.class);
    private AktørConsumer aktørConsumer = mock(AktørConsumer.class);;
    private MapInntektFraDtoTilDomene mapper;

    private static final AktørId FIKTIV_SØKER = AktørId.dummy();
    private static final String AG1 = "974625881";
    private static final String AG2 = "974625903";
    private static final String FIKTIVT_FNR = "26089435241";
    private static final String FIKTIV_PRIVAT_AG = AktørId.dummy().getId();

    @Before
    public void setup() {
        mapper = new MapInntektFraDtoTilDomene(virksomhetTjeneste, aktørConsumer);
    }

    @Test
    public void skal_mappe_inntekter_hos_en_virksomhet() {
        // Arrange
        String ref = "REF-1";
        int beløp = 35_000;
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();
        InntektskildeType kilde = InntektskildeType.INNTEKT_SAMMENLIGNING;

        månedsinntekter.addAll(lagInntekterForPeriode(YearMonth.of(2019,1), YearMonth.of(2019,5), beløp, AG1, ref));

        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        when(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(AG1, LocalDate.of(2019,5,1))).thenReturn(lagVirksomhet(AG1));
        when(virksomhetTjeneste.sjekkOmVirksomhetErOrgledd(any(String.class))).thenReturn(false);

        // Act
        mapper.mapFraInntektskomponent(FIKTIV_SØKER, builder, inntektsInformasjon);

        // Assert
        InntektArbeidYtelseAggregat aggregat = builder.build();
        AktørInntekt aktørInntekt = aggregat.getAktørInntekt().stream().filter(ai -> ai.getAktørId().equals(FIKTIV_SØKER)).findFirst().orElse(null);

        assertThat(aktørInntekt).isNotNull();

        Collection<Inntekt> alleInntekter = aktørInntekt.getInntekt();
        assertThat(alleInntekter).hasSize(1);
        Inntekt inntekt = new ArrayList<>(alleInntekter).get(0);
        assertThat(inntekt.getArbeidsgiver().getIdentifikator()).isEqualTo(AG1);
        assertThat(inntekt.getInntektsKilde()).isEqualTo(kilde);

        assertThat(inntekt.getAlleInntektsposter()).hasSize(5);
        verifiserInntektsposter(månedsinntekter, inntekt.getAlleInntektsposter());
    }

    @Test
    public void skal_mappe_inntekter_hos_flere_arbeidsgivere() {
        // Arrange
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();
        InntektskildeType kilde = InntektskildeType.INNTEKT_SAMMENLIGNING;

        String ref1 = "REF-1";
        int beløp1 = 35_000;
        List<Månedsinntekt> inntekterHosAg1 = lagInntekterForPeriode(YearMonth.of(2019,1), YearMonth.of(2019,5), beløp1, AG1, ref1);

        String ref2 = "REF-1";
        int beløp2 = 40_000;
        List<Månedsinntekt> inntekterHosAg2 = lagInntekterForPeriode(YearMonth.of(2019,1), YearMonth.of(2019,10), beløp2, AG2, ref2);

        månedsinntekter.addAll(inntekterHosAg1);
        månedsinntekter.addAll(inntekterHosAg2);

        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        when(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(AG1, LocalDate.of(2019,5,1))).thenReturn(lagVirksomhet(AG1));
        when(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(AG2, LocalDate.of(2019,10,1))).thenReturn(lagVirksomhet(AG2));
        when(virksomhetTjeneste.sjekkOmVirksomhetErOrgledd(any(String.class))).thenReturn(false);

        // Act
        mapper.mapFraInntektskomponent(FIKTIV_SØKER, builder, inntektsInformasjon);

        // Assert
        InntektArbeidYtelseAggregat aggregat = builder.build();
        AktørInntekt aktørInntekt = aggregat.getAktørInntekt().stream().filter(ai -> ai.getAktørId().equals(FIKTIV_SØKER)).findFirst().orElse(null);

        assertThat(aktørInntekt).isNotNull();
        Collection<Inntekt> alleInntekter = aktørInntekt.getInntekt();
        assertThat(alleInntekter).hasSize(2);

        Inntekt inntekt1 = verifiserInntekter(alleInntekter, AG1, kilde);
        verifiserInntektsposter(inntekterHosAg1, inntekt1.getAlleInntektsposter());

        Inntekt inntekt2 = verifiserInntekter(alleInntekter, AG2, kilde);
        verifiserInntektsposter(inntekterHosAg2, inntekt2.getAlleInntektsposter());
    }

    @Test
    public void skal_mappe_inntekt_fra_juridisk_nr_til_underenhet() {
        // Arrange
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();
        InntektskildeType kilde = InntektskildeType.INNTEKT_SAMMENLIGNING;

        String ref1 = "REF-1";
        int beløp1 = 35_000;
        List<Månedsinntekt> inntekterHosAg1 = lagInntekterForPeriode(YearMonth.of(2019,1), YearMonth.of(2019,5), beløp1, AG1, ref1);

        String ref2 = "REF-1";
        int beløp2 = 40_000;
        List<Månedsinntekt> inntekterHosAg2 = lagInntekterForPeriode(YearMonth.of(2019,1), YearMonth.of(2019,10), beløp2, AG2, ref2);

        månedsinntekter.addAll(inntekterHosAg1);
        månedsinntekter.addAll(inntekterHosAg2);

        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        Virksomhet virksomhet = lagVirksomhet(AG1);
        when(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(AG1, LocalDate.of(2019,5,1))).thenReturn(virksomhet);
        when(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(AG2, LocalDate.of(2019,10,1))).thenReturn(virksomhet);
        when(virksomhetTjeneste.sjekkOmVirksomhetErOrgledd(any(String.class))).thenReturn(false);

        // Act
        mapper.mapFraInntektskomponent(FIKTIV_SØKER, builder, inntektsInformasjon);

        // Assert
        InntektArbeidYtelseAggregat aggregat = builder.build();
        AktørInntekt aktørInntekt = aggregat.getAktørInntekt().stream().filter(ai -> ai.getAktørId().equals(FIKTIV_SØKER)).findFirst().orElse(null);

        assertThat(aktørInntekt).isNotNull();
        Collection<Inntekt> alleInntekter = aktørInntekt.getInntekt();
        assertThat(alleInntekter).hasSize(1);

        Inntekt inntekt1 = verifiserInntekter(alleInntekter, AG1, kilde);
        verifiserInntektsposterMedJuridisk(inntekterHosAg1, inntekterHosAg2, inntekt1.getAlleInntektsposter());
    }

    @Test
    public void skal_mappe_inntekter_hos_en_privatperson() {
        // Arrange
        String ref = "REF-1";
        int beløp = 33_333;
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();
        InntektskildeType kilde = InntektskildeType.INNTEKT_BEREGNING;

        månedsinntekter.addAll(lagInntekterForPeriode(YearMonth.of(2019,1), YearMonth.of(2019,5), beløp, FIKTIVT_FNR, ref));

        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        when(aktørConsumer.hentAktørIdForPersonIdent(FIKTIVT_FNR)).thenReturn(Optional.of(FIKTIV_PRIVAT_AG));

        // Act
        mapper.mapFraInntektskomponent(FIKTIV_SØKER, builder, inntektsInformasjon);

        // Assert
        InntektArbeidYtelseAggregat aggregat = builder.build();
        AktørInntekt aktørInntekt = aggregat.getAktørInntekt().stream().filter(ai -> ai.getAktørId().equals(FIKTIV_SØKER)).findFirst().orElse(null);

        assertThat(aktørInntekt).isNotNull();

        Collection<Inntekt> alleInntekter = aktørInntekt.getInntekt();
        assertThat(alleInntekter).hasSize(1);
        Inntekt inntekt = new ArrayList<>(alleInntekter).get(0);
        assertThat(inntekt.getArbeidsgiver().getIdentifikator()).isEqualTo(FIKTIV_PRIVAT_AG);
        assertThat(inntekt.getInntektsKilde()).isEqualTo(kilde);

        assertThat(inntekt.getAlleInntektsposter()).hasSize(5);
        verifiserInntektsposter(månedsinntekter, inntekt.getAlleInntektsposter());
    }

    @Test
    public void skal_mappe_inntekter_fra_ytelser() {
        // Arrange
        String ref = "REF-1";
        int beløp = 33_333;
        List<Månedsinntekt> månedsinntekter = new ArrayList<>();
        List<FrilansArbeidsforhold> arbeidsforhold = new ArrayList<>();
        InntektskildeType kilde = InntektskildeType.INNTEKT_BEREGNING;

        månedsinntekter.addAll(lagInntekterForPeriodeForYtelse(YearMonth.of(2019,1), YearMonth.of(2019,5), beløp, AG1, ref, "foreldrepenger"));

        InntektsInformasjon inntektsInformasjon = new InntektsInformasjon(månedsinntekter, arbeidsforhold, kilde);
        InntektArbeidYtelseAggregatBuilder builder = InntektArbeidYtelseAggregatBuilder.oppdatere(Optional.empty(), VersjonType.REGISTER);

        when(virksomhetTjeneste.hentOgLagreOrganisasjonMedHensynTilJuridisk(AG1, LocalDate.of(2019,5,1))).thenReturn(lagVirksomhet(AG1));
        when(virksomhetTjeneste.sjekkOmVirksomhetErOrgledd(any(String.class))).thenReturn(false);

        // Act
        mapper.mapFraInntektskomponent(FIKTIV_SØKER, builder, inntektsInformasjon);

        // Assert
        InntektArbeidYtelseAggregat aggregat = builder.build();
        AktørInntekt aktørInntekt = aggregat.getAktørInntekt().stream().filter(ai -> ai.getAktørId().equals(FIKTIV_SØKER)).findFirst().orElse(null);

        assertThat(aktørInntekt).isNotNull();

        Collection<Inntekt> alleInntekter = aktørInntekt.getInntekt();
        assertThat(alleInntekter).hasSize(1);
        Inntekt inntekt = new ArrayList<>(alleInntekter).get(0);
        assertThat(inntekt.getArbeidsgiver()).isNull();
        assertThat(inntekt.getInntektsKilde()).isEqualTo(kilde);

        assertThat(inntekt.getAlleInntektsposter()).hasSize(5);
        verifiserInntektsposterForYtelse(månedsinntekter, inntekt.getAlleInntektsposter(), UtbetaltYtelseFraOffentligeType.FORELDREPENGER);
    }

    private Inntekt verifiserInntekter(Collection<Inntekt> alleInntekter, String ag, InntektskildeType kilde) {
        Inntekt inntekt = alleInntekter.stream().filter(i -> i.getArbeidsgiver().getIdentifikator().equals(ag)).findFirst().orElse(null);
        assertThat(inntekt).isNotNull();
        assertThat(inntekt.getInntektsKilde()).isEqualTo(kilde);
        return inntekt;
    }

    private void verifiserInntektsposter(List<Månedsinntekt> månedsinntekter, Collection<Inntektspost> alleInntektsposter) {
        månedsinntekter.forEach(inntekt -> {
            Inntektspost matchendeInntekt = alleInntektsposter.stream().filter(ip -> ip.getPeriode().inkluderer(inntekt.getMåned().atDay(1))).findFirst().orElse(null);
            assertThat(matchendeInntekt).isNotNull();
            assertThat(matchendeInntekt.getOpprinneligUtbetaler()).isEmpty();
            assertThat(matchendeInntekt.getBeløp().getVerdi().compareTo(inntekt.getBeløp())).isEqualTo(0);
            assertThat(matchendeInntekt.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        });
    }

    private void verifiserInntektsposterForYtelse(List<Månedsinntekt> månedsinntekter, Collection<Inntektspost> alleInntektsposter, UtbetaltYtelseFraOffentligeType ytelseType) {
        månedsinntekter.forEach(inntekt -> {
            Inntektspost matchendeInntekt = alleInntektsposter.stream().filter(ip -> ip.getPeriode().inkluderer(inntekt.getMåned().atDay(1))).findFirst().orElse(null);
            assertThat(matchendeInntekt).isNotNull();
            assertThat(matchendeInntekt.getOpprinneligUtbetaler()).isEmpty();
            assertThat(matchendeInntekt.getBeløp().getVerdi().compareTo(inntekt.getBeløp())).isEqualTo(0);
            assertThat(matchendeInntekt.getInntektspostType()).isEqualTo(InntektspostType.YTELSE);
            assertThat(matchendeInntekt.getYtelseType()).isEqualTo(ytelseType);
        });
    }


    private void verifiserInntektsposterMedJuridisk(List<Månedsinntekt> vanligeInntekter, List<Månedsinntekt> inntekterFraJuridiskNr, Collection<Inntektspost> alleInntektsposter) {
        vanligeInntekter.forEach(inntekt -> {
            Inntektspost matchendeInntekt = alleInntektsposter.stream()
                .filter(ip -> ip.getPeriode().inkluderer(inntekt.getMåned().atDay(1)))
                .filter(ip -> ip.getOpprinneligUtbetaler().isEmpty())
                .findFirst()
                .orElse(null);
            assertThat(matchendeInntekt).isNotNull();
            assertThat(matchendeInntekt.getBeløp().getVerdi().compareTo(inntekt.getBeløp())).isEqualTo(0);
            assertThat(matchendeInntekt.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        });
        String juridiskNr = inntekterFraJuridiskNr.get(0).getArbeidsgiver();
        inntekterFraJuridiskNr.forEach(inntekt -> {
            Inntektspost matchendeInntekt = alleInntektsposter.stream()
                .filter(ip -> ip.getPeriode().inkluderer(inntekt.getMåned().atDay(1)))
                .filter(ip -> matcherOpprinneligUtbetaler(juridiskNr, ip))
                .findFirst()
                .orElse(null);
            assertThat(matchendeInntekt).isNotNull();
            assertThat(matchendeInntekt.getBeløp().getVerdi().compareTo(inntekt.getBeløp())).isEqualTo(0);
            assertThat(matchendeInntekt.getInntektspostType()).isEqualTo(InntektspostType.LØNN);
        });
    }

    private boolean matcherOpprinneligUtbetaler(String juridiskNr, Inntektspost ip) {
        String oprinneligUtbetalerId = ip.getOpprinneligUtbetaler().isEmpty() ? null : ip.getOpprinneligUtbetaler().get().getId();
        return juridiskNr.equals(oprinneligUtbetalerId);
    }

    private List<Månedsinntekt> lagInntekterForPeriodeForYtelse(YearMonth fom, YearMonth tom, int beløp, String arbeidsgiverId, String arbeidsforholdRef, String ytelsekode) {
        if (!tom.isAfter(fom)) {
            throw new IllegalStateException("tom kan ikke være før eller lik fom!");
        }
        List<Månedsinntekt> inntekter = new ArrayList<>();
        YearMonth current = fom;
        while (!current.isAfter(tom)) {
            inntekter.add(lagInntektForYtelse(current, beløp, arbeidsgiverId, arbeidsforholdRef, ytelsekode));
            current = current.plusMonths(1);
        }
        return inntekter;
    }

    private Månedsinntekt lagInntektForYtelse(YearMonth måned, int beløp, String arbeidsgiverId, String arbeidsforholdRef, String ytelsekode) {
        return new Månedsinntekt.Builder()
            .medBeløp(BigDecimal.valueOf(beløp))
            .medMåned(måned)
            .medArbeidsgiver(arbeidsgiverId)
            .medArbeidsforholdRef(arbeidsforholdRef)
            .medYtelse(true)
            .medYtelseKode(ytelsekode)
            .build();
    }

    private List<Månedsinntekt> lagInntekterForPeriode(YearMonth fom, YearMonth tom, int beløp, String arbeidsgiverId, String arbeidsforholdRef) {
        if (!tom.isAfter(fom)) {
            throw new IllegalStateException("tom kan ikke være før eller lik fom!");
        }
        List<Månedsinntekt> inntekter = new ArrayList<>();
        YearMonth current = fom;
        while (!current.isAfter(tom)) {
            inntekter.add(lagInntektForYtelse(current, beløp, arbeidsgiverId, arbeidsforholdRef));
            current = current.plusMonths(1);
        }
        return inntekter;
    }

    private Månedsinntekt lagInntektForYtelse(YearMonth måned, int beløp, String arbeidsgiverId, String arbeidsforholdRef) {
        return new Månedsinntekt.Builder()
            .medBeløp(BigDecimal.valueOf(beløp))
            .medMåned(måned)
            .medArbeidsgiver(arbeidsgiverId)
            .medArbeidsforholdRef(arbeidsforholdRef)
            .build();
    }

    private Virksomhet lagVirksomhet(String orgnr) {
        Virksomhet.Builder virk = new Virksomhet.Builder();
        return virk.medNavn("test").medOrganisasjonstype(OrganisasjonType.VIRKSOMHET).medOrgnr(orgnr).build();
    }

}
