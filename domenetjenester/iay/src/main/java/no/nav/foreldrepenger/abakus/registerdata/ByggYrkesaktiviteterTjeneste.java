package no.nav.foreldrepenger.abakus.registerdata;

import static no.nav.vedtak.konfig.Tid.TIDENES_BEGYNNELSE;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.AktivitetsAvtaleBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.domene.iay.Permisjon;
import no.nav.foreldrepenger.abakus.domene.iay.PermisjonBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsavtale;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.typer.InternArbeidsforholdRef;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

class ByggYrkesaktiviteterTjeneste {

    private KodeverkRepository kodeverkRepository;

    ByggYrkesaktiviteterTjeneste(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    YrkesaktivitetBuilder byggYrkesaktivitetForSøker(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold,
                                                     Arbeidsgiver arbeidsgiver,
                                                     InternArbeidsforholdRef internReferanse,
                                                     InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {

        var opptjeningsNøkkel = new Opptjeningsnøkkel(internReferanse, arbeidsgiver);
        final ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        final ArbeidType arbeidsforholdType = kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsgiverIdent.getType());
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

    private void byggYrkesaktivitetForPrivatperson(List<Arbeidsforhold> arbeidsforhold, YrkesaktivitetBuilder builder) {
        for (Arbeidsforhold arbeid : arbeidsforhold) {
            byggPermisjoner(builder, arbeid);
        }
        LocalDate fom = finnFomDatoForAnsettelsesPeriode(arbeidsforhold);
        sammenstillAktivitetsavtaler(arbeidsforhold, builder, fom);
        builder.leggTilAktivitetsAvtale(byggAnsettelsesPeriode(builder, fom));
    }

    private void sammenstillAktivitetsavtaler(List<Arbeidsforhold> arbeidsforhold, YrkesaktivitetBuilder builder, LocalDate fom) {
        finnSisteArbeidsavtale(arbeidsforhold)
            .ifPresent(arbeidsavtale -> builder
                .leggTilAktivitetsAvtale(lagAktivitetsavtale(builder, fom, arbeidsavtale))
            );
    }

    private AktivitetsAvtaleBuilder lagAktivitetsavtale(YrkesaktivitetBuilder builder, LocalDate fom, Arbeidsavtale arbeidsavtale) {
        return builder
            .getAktivitetsAvtaleBuilder()
            .medProsentsats(arbeidsavtale.getStillingsprosent())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato() != null ? arbeidsavtale.getSisteLønnsendringsdato() : fom)
            .medAntallTimer(arbeidsavtale.getAvtaltArbeidstimerPerUke()) // merk, inneholder mye søppel
            .medAntallTimerFulltid(arbeidsavtale.getBeregnetAntallTimerPrUke()) // merk, innneholder mye søppel
            .medPeriode(DatoIntervallEntitet.fraOgMed(fom));
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
            .min(LocalDate::compareTo).orElse(TIDENES_BEGYNNELSE);
    }

    private AktivitetsAvtaleBuilder byggAnsettelsesPeriode(YrkesaktivitetBuilder builder, LocalDate fom) {
        return builder
            .getAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMed(fom));
    }

    private void byggPermisjoner(YrkesaktivitetBuilder builder, Arbeidsforhold arbeidsforhold1) {
        builder.tilbakestillPermisjon();
        arbeidsforhold1.getPermisjoner()
            .stream()
            .filter(p -> p.getPermisjonFom() != null && p.getPermisjonFom().isBefore(arbeidsforhold1.getArbeidTom()))
            .map(p -> opprettPermisjoner(p, builder, arbeidsforhold1.getArbeidTom()))
            .forEach(builder::leggTilPermisjon);
    }

    private void byggYrkesaktivitetForVirksomhet(List<Arbeidsforhold> arbeidsforhold, YrkesaktivitetBuilder builder) {
        for (Arbeidsforhold arbeid : arbeidsforhold) {
            arbeid.getArbeidsavtaler()
                .stream()
                .map(a -> opprettAktivitetsAvtaler(a, builder))
                .forEach(builder::leggTilAktivitetsAvtale);

            byggPermisjoner(builder, arbeid);
        }
    }

    private Permisjon opprettPermisjoner(no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Permisjon permisjon,
                                         YrkesaktivitetBuilder yrkesaktivitetBuilder, LocalDate arbeidsforholdTom) {
        PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();
        LocalDate permisjonTom = permisjon.getPermisjonTom() == null ? arbeidsforholdTom : permisjon.getPermisjonTom();
        return permisjonBuilder
            .medProsentsats(permisjon.getPermisjonsprosent())
            .medPeriode(permisjon.getPermisjonFom(), permisjonTom)
            .medPermisjonsbeskrivelseType(kodeverkRepository.finnForKodeverkEiersKode(PermisjonsbeskrivelseType.class, permisjon.getPermisjonsÅrsak(), PermisjonsbeskrivelseType.UDEFINERT))
            .build();
    }

    private void opprettMinimalYrkesaktivitet(ArbeidsforholdIdentifikator arbeidsforhold,
                                              Arbeidsgiver arbeidsgiver,
                                              InternArbeidsforholdRef internReferanse,
                                              YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        yrkesaktivitetBuilder
            .medArbeidType(kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsforhold.getType()))
            .medArbeidsforholdId(internReferanse)
            .medArbeidsgiver(arbeidsgiver);
    }


    private AktivitetsAvtaleBuilder opprettAktivitetsAvtaler(Arbeidsavtale arbeidsavtale,
                                                             YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        DatoIntervallEntitet periode;
        if (arbeidsavtale.getArbeidsavtaleTom() == null) {
            periode = DatoIntervallEntitet.fraOgMed(arbeidsavtale.getArbeidsavtaleFom());
        } else {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(arbeidsavtale.getArbeidsavtaleFom(), arbeidsavtale.getArbeidsavtaleTom());
        }
        AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, arbeidsavtale.getErAnsettelsesPerioden());
        aktivitetsAvtaleBuilder
            .medProsentsats(arbeidsavtale.getStillingsprosent())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato() != null || arbeidsavtale.getErAnsettelsesPerioden() ? arbeidsavtale.getSisteLønnsendringsdato() : periode.getFomDato())
            .medAntallTimer(arbeidsavtale.getAvtaltArbeidstimerPerUke()) // merk, innneholder mye søppel
            .medAntallTimerFulltid(arbeidsavtale.getBeregnetAntallTimerPrUke()) // merk, innneholder mye søppel
            .medPeriode(periode);

        return aktivitetsAvtaleBuilder;
    }
}
