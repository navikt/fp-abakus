package no.nav.foreldrepenger.abakus.registerdata;

import static no.nav.vedtak.konfig.Tid.TIDENES_BEGYNNELSE;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.domene.iay.Permisjon;
import no.nav.foreldrepenger.abakus.domene.iay.PermisjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsavtale;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;

class ByggYrkesaktiviteterTjeneste {

    YrkesaktivitetBuilder byggYrkesaktivitetForSøker(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold,
                                                     Arbeidsgiver arbeidsgiver,
                                                     InternArbeidsforholdRef internReferanse,
                                                     InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {

        var opptjeningsNøkkel = new Opptjeningsnøkkel(internReferanse, arbeidsgiver);
        final ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        final ArbeidType arbeidsforholdType = ArbeidType.finnForKodeverkEiersKode(arbeidsgiverIdent.getType());
        YrkesaktivitetBuilder builder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(opptjeningsNøkkel, arbeidsforholdType);
        opprettMinimalYrkesaktivitet(arbeidsforhold.getKey(), arbeidsgiver, internReferanse, builder);
        builder.tilbakestillAvtaler();

        if (arbeidsgiver.getErVirksomhet()) {
            byggYrkesaktivitetForVirksomhet(arbeidsforhold.getValue(), builder);
        } else {
            byggYrkesaktivitetForPrivatperson(arbeidsforhold.getValue(), builder);
        }
        return builder;
    }

    /*
     * Arbeidsforhold rapportert med privat arbeidsgiver (både ordinært og forenklet oppgjør) har åpne akt.avtaler og ans.perioder (uten tom)
     * Dette er gjort etter funksjonell diskusjon for å være sikrere på å kunne matche mot inntekter - dvs LA STÅ inntil videre fraOgMed for privat AG
     * Litt logging viser at ans.perioder gjerne er 3uker - 6/12 mnd, mens det ofte er et gap mellom ansFom og avtaleFom
     */
    private void byggYrkesaktivitetForPrivatperson(List<Arbeidsforhold> arbeidsforhold, YrkesaktivitetBuilder builder) {
        for (Arbeidsforhold arbeid : arbeidsforhold) {
            byggPermisjoner(builder, arbeid);
        }
        LocalDate fom = finnFomDatoForAnsettelsesPeriode(arbeidsforhold);
        sammenstillAktivitetsavtaler(arbeidsforhold, builder, fom);
        builder.leggTilAktivitetsAvtale(byggAnsettelsesPeriode(builder, fom));
    }

    private void sammenstillAktivitetsavtaler(List<Arbeidsforhold> arbeidsforhold, YrkesaktivitetBuilder builder, LocalDate fom) {
        finnSisteArbeidsavtale(arbeidsforhold).ifPresent(
            arbeidsavtale -> builder.leggTilAktivitetsAvtale(lagAktivitetsavtale(builder, fom, arbeidsavtale)));
    }

    private AktivitetsAvtaleBuilder lagAktivitetsavtale(YrkesaktivitetBuilder builder, LocalDate fom, Arbeidsavtale arbeidsavtale) {
        return builder.getAktivitetsAvtaleBuilder()
            .medProsentsats(arbeidsavtale.getStillingsprosent())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato() != null ? arbeidsavtale.getSisteLønnsendringsdato() : fom)
            .medPeriode(IntervallEntitet.fraOgMed(fom));
    }

    private Optional<Arbeidsavtale> finnSisteArbeidsavtale(List<Arbeidsforhold> arbeidsforhold) {
        return arbeidsforhold.stream()
            .flatMap(arbeid -> arbeid.getArbeidsavtaler().stream())
            .filter(avtale -> !avtale.getErAnsettelsesPerioden())
            .max(Comparator.comparing(a -> a.getArbeidsavtaleTom() != null ? a.getArbeidsavtaleTom() : TIDENES_ENDE));
    }

    private LocalDate finnFomDatoForAnsettelsesPeriode(List<Arbeidsforhold> arbeidsforhold) {
        return arbeidsforhold.stream()
            .flatMap(arbeid -> arbeid.getArbeidsavtaler().stream())
            .map(Arbeidsavtale::getArbeidsavtaleFom)
            .min(LocalDate::compareTo)
            .orElse(TIDENES_BEGYNNELSE);
    }

    private AktivitetsAvtaleBuilder byggAnsettelsesPeriode(YrkesaktivitetBuilder builder, LocalDate fom) {
        return builder.getAktivitetsAvtaleBuilder().medPeriode(IntervallEntitet.fraOgMed(fom));
    }

    private void byggPermisjoner(YrkesaktivitetBuilder builder, Arbeidsforhold arbeidsforhold1) {
        final LocalDate arbeidTom = arbeidsforhold1.getArbeidTom() != null ? arbeidsforhold1.getArbeidTom() : TIDENES_ENDE;
        builder.tilbakestillPermisjon();
        arbeidsforhold1.getPermisjoner()
            .stream()
            .filter(p -> p.getPermisjonFom() != null && p.getPermisjonFom().isBefore(arbeidTom))
            .map(p -> opprettPermisjoner(p, builder, arbeidTom))
            .forEach(builder::leggTilPermisjon);
    }

    private void byggYrkesaktivitetForVirksomhet(List<Arbeidsforhold> arbeidsforhold, YrkesaktivitetBuilder builder) {
        for (Arbeidsforhold arbeid : arbeidsforhold) {
            arbeid.getArbeidsavtaler().stream().map(a -> opprettAktivitetsAvtaler(a, builder)).forEach(builder::leggTilAktivitetsAvtale);

            byggPermisjoner(builder, arbeid);
        }
    }

    private Permisjon opprettPermisjoner(no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Permisjon permisjon,
                                         YrkesaktivitetBuilder yrkesaktivitetBuilder,
                                         LocalDate arbeidsforholdTom) {
        PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();
        LocalDate permisjonTom = permisjon.getPermisjonTom() == null ? arbeidsforholdTom : permisjon.getPermisjonTom();
        return permisjonBuilder.medProsentsats(permisjon.getPermisjonsprosent())
            .medPeriode(permisjon.getPermisjonFom(), permisjonTom)
            .medPermisjonsbeskrivelseType(PermisjonsbeskrivelseType.finnForKodeverkEiersKode(permisjon.getPermisjonsÅrsak()))
            .build();
    }

    private void opprettMinimalYrkesaktivitet(ArbeidsforholdIdentifikator arbeidsforhold,
                                              Arbeidsgiver arbeidsgiver,
                                              InternArbeidsforholdRef internReferanse,
                                              YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        yrkesaktivitetBuilder.medArbeidType(ArbeidType.finnForKodeverkEiersKode(arbeidsforhold.getType()))
            .medArbeidsforholdId(internReferanse)
            .medArbeidsgiver(arbeidsgiver);
    }


    private AktivitetsAvtaleBuilder opprettAktivitetsAvtaler(Arbeidsavtale arbeidsavtale, YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        IntervallEntitet periode;
        if (arbeidsavtale.getArbeidsavtaleTom() == null) {
            periode = IntervallEntitet.fraOgMed(arbeidsavtale.getArbeidsavtaleFom());
        } else {
            periode = IntervallEntitet.fraOgMedTilOgMed(arbeidsavtale.getArbeidsavtaleFom(), arbeidsavtale.getArbeidsavtaleTom());
        }
        AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode,
            arbeidsavtale.getErAnsettelsesPerioden());
        aktivitetsAvtaleBuilder.medProsentsats(arbeidsavtale.getStillingsprosent())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato() != null
                || arbeidsavtale.getErAnsettelsesPerioden() ? arbeidsavtale.getSisteLønnsendringsdato() : periode.getFomDato())
            .medPeriode(periode);

        return aktivitetsAvtaleBuilder;
    }
}
