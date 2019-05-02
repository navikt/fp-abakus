package no.nav.foreldrepenger.abakus.registerdata;

import static no.nav.vedtak.konfig.Tid.TIDENES_BEGYNNELSE;
import static no.nav.vedtak.konfig.Tid.TIDENES_ENDE;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.domene.iay.InntektArbeidYtelseAggregatBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.Opptjeningsnøkkel;
import no.nav.foreldrepenger.abakus.domene.iay.Permisjon;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.YrkesaktivitetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.PermisjonsbeskrivelseType;
import no.nav.foreldrepenger.abakus.domene.iay.ArbeidsgiverEntitet;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsavtale;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

class ByggYrkesaktiviteterTjeneste {

    private KodeverkRepository kodeverkRepository;

    ByggYrkesaktiviteterTjeneste(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    YrkesaktivitetBuilder byggYrkesaktivitetForSøker(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold,
                                                     Arbeidsgiver arbeidsgiver, Opptjeningsnøkkel nøkkel, InntektArbeidYtelseAggregatBuilder.AktørArbeidBuilder aktørArbeidBuilder) {
        final ArbeidsforholdIdentifikator arbeidsgiverIdent = arbeidsforhold.getKey();
        final ArbeidType arbeidsforholdType = kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsgiverIdent.getType());
        YrkesaktivitetBuilder builder = aktørArbeidBuilder.getYrkesaktivitetBuilderForNøkkelAvType(nøkkel, arbeidsforholdType);
        opprettMinimalYrkesaktivitet(arbeidsforhold.getKey(), arbeidsgiver, builder);
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

    private YrkesaktivitetEntitet.AktivitetsAvtaleBuilder lagAktivitetsavtale(YrkesaktivitetBuilder builder, LocalDate fom, Arbeidsavtale arbeidsavtale) {
        return builder
            .getAktivitetsAvtaleBuilder()
            .medProsentsats(arbeidsavtale.getStillingsprosent())
            .medAntallTimer(arbeidsavtale.getAvtaltArbeidstimerPerUke())
            .medAntallTimerFulltid(arbeidsavtale.getBeregnetAntallTimerPrUke())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato())
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

    private YrkesaktivitetEntitet.AktivitetsAvtaleBuilder byggAnsettelsesPeriode(YrkesaktivitetBuilder builder, LocalDate fom) {
        return builder
            .getAktivitetsAvtaleBuilder()
            .medPeriode(DatoIntervallEntitet.fraOgMed(fom));
    }

    private void byggPermisjoner(YrkesaktivitetBuilder builder, Arbeidsforhold arbeidsforhold1) {
        builder.tilbakestillPermisjon();
        arbeidsforhold1.getPermisjoner()
            .stream()
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
        YrkesaktivitetEntitet.PermisjonBuilder permisjonBuilder = yrkesaktivitetBuilder.getPermisjonBuilder();
        LocalDate permisjonTom = permisjon.getPermisjonTom() == null ? arbeidsforholdTom : permisjon.getPermisjonTom();
        return permisjonBuilder
            .medProsentsats(permisjon.getPermisjonsprosent())
            .medPeriode(permisjon.getPermisjonFom(), permisjonTom)
            .medPermisjonsbeskrivelseType(kodeverkRepository.finnForKodeverkEiersKode(PermisjonsbeskrivelseType.class, permisjon.getPermisjonsÅrsak(), PermisjonsbeskrivelseType.UDEFINERT))
            .build();
    }

    private void opprettMinimalYrkesaktivitet(ArbeidsforholdIdentifikator arbeidsforhold,
                                              Arbeidsgiver arbeidsgiver, YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        yrkesaktivitetBuilder
            .medArbeidType(kodeverkRepository.finnForKodeverkEiersKode(ArbeidType.class, arbeidsforhold.getType()))
            .medArbeidsforholdId(arbeidsforhold.getArbeidsforholdId())
            .medArbeidsgiver(arbeidsgiver);
    }


    private YrkesaktivitetEntitet.AktivitetsAvtaleBuilder opprettAktivitetsAvtaler(Arbeidsavtale arbeidsavtale,
                                                                                   YrkesaktivitetBuilder yrkesaktivitetBuilder) {
        DatoIntervallEntitet periode;
        if (arbeidsavtale.getArbeidsavtaleTom() == null) {
            periode = DatoIntervallEntitet.fraOgMed(arbeidsavtale.getArbeidsavtaleFom());
        } else {
            periode = DatoIntervallEntitet.fraOgMedTilOgMed(arbeidsavtale.getArbeidsavtaleFom(), arbeidsavtale.getArbeidsavtaleTom());
        }
        YrkesaktivitetEntitet.AktivitetsAvtaleBuilder aktivitetsAvtaleBuilder = yrkesaktivitetBuilder.getAktivitetsAvtaleBuilder(periode, arbeidsavtale.getErAnsettelsesPerioden());
        aktivitetsAvtaleBuilder
            .medProsentsats(arbeidsavtale.getStillingsprosent())
            .medAntallTimer(arbeidsavtale.getAvtaltArbeidstimerPerUke())
            .medAntallTimerFulltid(arbeidsavtale.getBeregnetAntallTimerPrUke())
            .medSisteLønnsendringsdato(arbeidsavtale.getSisteLønnsendringsdato())
            .medPeriode(periode);

        return aktivitetsAvtaleBuilder;
    }
}
