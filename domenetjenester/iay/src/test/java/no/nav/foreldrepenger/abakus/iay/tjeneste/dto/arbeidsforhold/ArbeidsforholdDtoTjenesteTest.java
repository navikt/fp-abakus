package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold;

import static no.nav.foreldrepenger.abakus.typer.OrgNummer.KUNSTIG_ORG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsavtale;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Permisjon;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.konfig.Tid;

@ExtendWith(MockitoExtension.class)
class ArbeidsforholdDtoTjenesteTest {
    @Mock
    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    @Mock
    private AktørTjeneste aktørConsumer;
    private ArbeidsforholdDtoTjeneste arbeidsforholdDtoTjeneste;
    private static final LocalDate FRA_DATO = LocalDate.now().minusWeeks(2);
    private static final LocalDate TIL_DATO = LocalDate.now().plusWeeks(2);


    @BeforeEach
    void setUp() {
        arbeidsforholdDtoTjeneste = new ArbeidsforholdDtoTjeneste(arbeidsforholdTjeneste, aktørConsumer);
    }

    @Test
    void mapArbeidsforholdMedPermisjoner() {

        var personIdent = new PersonIdent("12345678");
        var aktørId = AktørId.dummy();
        var intervall = IntervallEntitet.fraOgMedTilOgMed(FRA_DATO, TIL_DATO);
        var eksternRef = EksternArbeidsforholdRef.ref("eksternRef");
        var orgnr = new OrgNummer(KUNSTIG_ORG);
        var arbeidsgiver = new Organisasjon(orgnr.getId());

        var arbeidsgiverIdentifikator = new ArbeidsforholdIdentifikator(arbeidsgiver, eksternRef,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.getOffisiellKode());

        var arbeidsavtalerTilMap = List.of(lagArbeidsavtale(FRA_DATO, FRA_DATO.plusWeeks(2).minusDays(1), BigDecimal.valueOf(30)),
            lagArbeidsavtale(FRA_DATO.plusWeeks(2), null, BigDecimal.valueOf(70)));
        var permisjonerTilMap = List.of(lagPermisjon(FRA_DATO, FRA_DATO.plusWeeks(1), BigDecimal.ZERO),
            lagPermisjon(FRA_DATO.plusWeeks(2), Tid.TIDENES_ENDE, BigDecimal.valueOf(20)));
        var arbeidsforhold = List.of(lagArbeidsforhold(arbeidsgiver, arbeidsavtalerTilMap, permisjonerTilMap));

        when(aktørConsumer.hentIdentForAktør(any())).thenReturn(Optional.of(personIdent));
        when(arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(personIdent, aktørId, intervall)).thenReturn(
            Map.of(arbeidsgiverIdentifikator, arbeidsforhold));

        var arbeidsforholdDto = arbeidsforholdDtoTjeneste.mapArbForholdOgPermisjoner(aktørId, FRA_DATO, TIL_DATO);

        assertThat(arbeidsforholdDto).hasSize(1);
        assertThat(arbeidsforholdDto.getFirst().getType()).isEqualTo(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        assertThat(arbeidsforholdDto.getFirst().getArbeidsforholdId().getEksternReferanse()).isEqualTo(eksternRef.getReferanse());
        assertThat(arbeidsforholdDto.getFirst().getArbeidsgiver().getIdent()).isEqualTo(KUNSTIG_ORG);
        assertThat(arbeidsforholdDto.getFirst().getArbeidsavtaler()).hasSize(2);

        var arbeidsavtaler = arbeidsforholdDto.getFirst().getArbeidsavtaler();
        assertThat(arbeidsavtaler.getFirst().periode().getFom()).isEqualTo(arbeidsavtalerTilMap.getFirst().getArbeidsavtaleFom());
        assertThat(arbeidsavtaler.getFirst().periode().getTom()).isEqualTo(arbeidsavtalerTilMap.getFirst().getArbeidsavtaleTom());
        assertThat(arbeidsavtaler.getFirst().stillingsprosent()).isEqualByComparingTo(arbeidsavtalerTilMap.getFirst().getStillingsprosent());
        assertThat(arbeidsavtaler.get(1).periode().getFom()).isEqualTo(arbeidsavtalerTilMap.get(1).getArbeidsavtaleFom());
        assertThat(arbeidsavtaler.get(1).periode().getTom()).isEqualTo(TIL_DATO);
        assertThat(arbeidsavtaler.get(1).stillingsprosent()).isEqualByComparingTo(arbeidsavtalerTilMap.get(1).getStillingsprosent());

        var permisjoner = arbeidsforholdDto.getFirst().getPermisjoner();
        assertThat(permisjoner.getFirst().getPeriode().getFom()).isEqualTo(permisjonerTilMap.getFirst().getPermisjonFom());
        assertThat(permisjoner.getFirst().getPeriode().getTom()).isEqualTo(permisjonerTilMap.getFirst().getPermisjonTom());
        assertThat(permisjoner.getFirst().getProsentsats()).isEqualByComparingTo(permisjonerTilMap.getFirst().getPermisjonsprosent());
        assertThat(permisjoner.getFirst().getType()).isEqualTo(PermisjonsbeskrivelseType.PERMISJON_MED_FORELDREPENGER);
        assertThat(permisjoner.get(1).getPeriode().getFom()).isEqualTo(permisjonerTilMap.get(1).getPermisjonFom());
        assertThat(permisjoner.get(1).getPeriode().getTom()).isEqualTo(TIL_DATO);
        assertThat(permisjoner.get(1).getProsentsats()).isEqualByComparingTo(permisjonerTilMap.get(1).getPermisjonsprosent());
        assertThat(permisjoner.get(1).getType()).isEqualTo(PermisjonsbeskrivelseType.PERMISJON_MED_FORELDREPENGER);

    }

    @Test
    void mapArbeidsforholdMedOverlappendePermisjoner() {

        var personIdent = new PersonIdent("12345678");
        var aktørId = AktørId.dummy();
        var intervall = IntervallEntitet.fraOgMedTilOgMed(FRA_DATO, TIL_DATO);
        var eksternRef = EksternArbeidsforholdRef.ref("eksternRef");
        var orgnr = new OrgNummer(KUNSTIG_ORG);
        var arbeidsgiver = new Organisasjon(orgnr.getId());

        var arbeidsgiverIdentifikator = new ArbeidsforholdIdentifikator(arbeidsgiver, eksternRef,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.getOffisiellKode());

        var arbeidsavtalerTilMap = List.of(lagArbeidsavtale(FRA_DATO, FRA_DATO.plusWeeks(2).minusDays(1), BigDecimal.valueOf(30)),
            lagArbeidsavtale(FRA_DATO.plusWeeks(2), null, BigDecimal.valueOf(70)));
        var permisjonerTilMap = List.of(lagPermisjon(FRA_DATO, FRA_DATO.plusWeeks(1), BigDecimal.ZERO),
            lagPermisjon(FRA_DATO.plusDays(1), FRA_DATO.plusDays(5), BigDecimal.valueOf(100)),
            lagPermisjon(FRA_DATO.plusWeeks(2), Tid.TIDENES_ENDE, BigDecimal.valueOf(20)));
        var arbeidsforhold = List.of(lagArbeidsforhold(arbeidsgiver, arbeidsavtalerTilMap, permisjonerTilMap));

        when(aktørConsumer.hentIdentForAktør(any())).thenReturn(Optional.of(personIdent));
        when(arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(personIdent, aktørId, intervall)).thenReturn(
            Map.of(arbeidsgiverIdentifikator, arbeidsforhold));

        var arbeidsforholdDto = arbeidsforholdDtoTjeneste.mapArbForholdOgPermisjoner(aktørId, FRA_DATO, TIL_DATO);

        assertThat(arbeidsforholdDto).hasSize(1);
        assertThat(arbeidsforholdDto.getFirst().getType()).isEqualTo(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        assertThat(arbeidsforholdDto.getFirst().getArbeidsforholdId().getEksternReferanse()).isEqualTo(eksternRef.getReferanse());
        assertThat(arbeidsforholdDto.getFirst().getArbeidsgiver().getIdent()).isEqualTo(KUNSTIG_ORG);
        assertThat(arbeidsforholdDto.getFirst().getArbeidsavtaler()).hasSize(2);

        var arbeidsavtaler = arbeidsforholdDto.getFirst().getArbeidsavtaler();
        assertThat(arbeidsavtaler.getFirst().periode().getFom()).isEqualTo(arbeidsavtalerTilMap.getFirst().getArbeidsavtaleFom());
        assertThat(arbeidsavtaler.getFirst().periode().getTom()).isEqualTo(arbeidsavtalerTilMap.getFirst().getArbeidsavtaleTom());
        assertThat(arbeidsavtaler.getFirst().stillingsprosent()).isEqualByComparingTo(arbeidsavtalerTilMap.getFirst().getStillingsprosent());
        assertThat(arbeidsavtaler.get(1).periode().getFom()).isEqualTo(arbeidsavtalerTilMap.get(1).getArbeidsavtaleFom());
        assertThat(arbeidsavtaler.get(1).periode().getTom()).isEqualTo(TIL_DATO);
        assertThat(arbeidsavtaler.get(1).stillingsprosent()).isEqualByComparingTo(arbeidsavtalerTilMap.get(1).getStillingsprosent());

        var permisjoner = arbeidsforholdDto.getFirst().getPermisjoner();
        assertThat(permisjoner).hasSize(5);
        assertThat(permisjoner.getFirst().getPeriode().getFom()).isEqualTo(permisjonerTilMap.getFirst().getPermisjonFom());
        assertThat(permisjoner.get(4).getPeriode().getTom()).isEqualTo(TIL_DATO);

    }

    @Test
    void mapArbeidsforholdUtenPermisjoner() {

        var personIdent = new PersonIdent("12345678");
        var aktørId = AktørId.dummy();
        var intervall = IntervallEntitet.fraOgMedTilOgMed(FRA_DATO, TIL_DATO);
        var eksternRef = EksternArbeidsforholdRef.ref("eksternRef");
        var orgnr = new OrgNummer(KUNSTIG_ORG);
        var arbeidsgiver = new Organisasjon(orgnr.getId());

        var arbeidsgiverIdentifikator = new ArbeidsforholdIdentifikator(arbeidsgiver, eksternRef,
            ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.getOffisiellKode());

        var arbeidsavtalerTilMap = List.of(lagArbeidsavtale(FRA_DATO.minusWeeks(1), FRA_DATO.plusWeeks(2).minusDays(1), BigDecimal.valueOf(100)));
        var arbeidsforhold = List.of(lagArbeidsforhold(arbeidsgiver, arbeidsavtalerTilMap, Collections.emptyList()));

        when(aktørConsumer.hentIdentForAktør(any())).thenReturn(Optional.of(personIdent));
        when(arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(personIdent, aktørId, intervall)).thenReturn(
            Map.of(arbeidsgiverIdentifikator, arbeidsforhold));

        var arbeidsforholdDto = arbeidsforholdDtoTjeneste.mapArbForholdOgPermisjoner(aktørId, FRA_DATO, TIL_DATO);

        assertThat(arbeidsforholdDto).hasSize(1);
        assertThat(arbeidsforholdDto.getFirst().getType()).isEqualTo(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD);
        assertThat(arbeidsforholdDto.getFirst().getArbeidsforholdId().getEksternReferanse()).isEqualTo(eksternRef.getReferanse());
        assertThat(arbeidsforholdDto.getFirst().getArbeidsgiver().getIdent()).isEqualTo(KUNSTIG_ORG);
        assertThat(arbeidsforholdDto.getFirst().getArbeidsavtaler()).hasSize(1);

        var arbeidsavtaler = arbeidsforholdDto.getFirst().getArbeidsavtaler();
        assertThat(arbeidsavtaler.getFirst().periode().getFom()).isEqualTo(FRA_DATO);
        assertThat(arbeidsavtaler.getFirst().periode().getTom()).isEqualTo(arbeidsavtalerTilMap.getFirst().getArbeidsavtaleTom());
        assertThat(arbeidsavtaler.getFirst().stillingsprosent()).isEqualByComparingTo(arbeidsavtalerTilMap.getFirst().getStillingsprosent());

        assertThat(arbeidsforholdDto.getFirst().getPermisjoner()).isEmpty();
    }

    private Permisjon lagPermisjon(LocalDate fraDato, LocalDate tilDato, BigDecimal permisjonProsent) {
        return new Permisjon.Builder().medPermisjonsÅrsak(PermisjonsbeskrivelseType.PERMISJON_MED_FORELDREPENGER.getOffisiellKode())
            .medPermisjonFom(fraDato)
            .medPermisjonTom(tilDato)
            .medPermisjonsprosent(permisjonProsent)
            .build();
    }

    private Arbeidsavtale lagArbeidsavtale(LocalDate fraDato, LocalDate tilDato, BigDecimal stillingsprosent) {
        return new Arbeidsavtale.Builder().medArbeidsavtaleFom(fraDato).medArbeidsavtaleTom(tilDato).medStillingsprosent(stillingsprosent).build();
    }

    private Arbeidsforhold lagArbeidsforhold(Arbeidsgiver arbeidsgiver, List<Arbeidsavtale> arbeidsavtaler, List<Permisjon> permisjoner) {
        return new Arbeidsforhold.Builder().medArbeidFom(FRA_DATO)
            .medArbeidTom(TIL_DATO)
            .medType(ArbeidType.ORDINÆRT_ARBEIDSFORHOLD.getOffisiellKode())
            .medArbeidsgiver(arbeidsgiver)
            .medArbeidsavtaler(arbeidsavtaler)
            .medPermisjon(permisjoner)
            .build();
    }
}
