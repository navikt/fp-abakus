package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.AaregRestKlient;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.ArbeidsavtaleRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.ArbeidsforholdRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.OpplysningspliktigArbeidsgiverRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.PeriodeRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.PermisjonPermitteringRS;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

@ApplicationScoped
public class ArbeidsforholdTjeneste {

    private static final Logger log = LoggerFactory.getLogger(ArbeidsforholdTjeneste.class);
    private AaregRestKlient aaregRestKlient;

    public ArbeidsforholdTjeneste() {
        // CDI
    }

    @Inject
    public ArbeidsforholdTjeneste(AaregRestKlient aaregRestKlient) {
        this.aaregRestKlient = aaregRestKlient;
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent ident, AktørId aktørId, IntervallEntitet interval) {
        // TODO: kall med aktørid når register har fikset ytelsesproblemer
        List<ArbeidsforholdRS> response = aaregRestKlient.finnArbeidsforholdForArbeidstaker(ident.getIdent(),
            interval.getFomDato(), interval.getTomDato());
        var mapArbeidsforhold = response.stream()
            .map(arbeidsforhold -> mapArbeidsforholdRSTilDto(arbeidsforhold, interval))
            .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));

        valider(mapArbeidsforhold);

        return mapArbeidsforhold;
    }

    private void valider(Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> mapArbeidsforhold) {
        var gruppert = mapArbeidsforhold.keySet().stream()
            .collect(Collectors.groupingBy(r -> new Key(r.getArbeidsgiver(), r.getArbeidsforholdId())));
        var dups = gruppert.entrySet().stream()
            .filter(e -> e.getValue().size() > 1) // duplikater
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        if (!dups.isEmpty()) {
            String msg = "Mottatt duplikater for arbeidsforhold fra AAreg: " + dups;
            log.warn(msg);
            // throw new IllegalStateException(msg);
        }
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdFrilansForIdentIPerioden(PersonIdent ident, AktørId aktørId, IntervallEntitet interval) {
        // TODO: kall med aktørid når register har fikset ytelsesproblemer
        List<ArbeidsforholdRS> response = aaregRestKlient.finnArbeidsforholdForFrilanser(ident.getIdent(),
            interval.getFomDato(), interval.getTomDato());
        return response.stream()
            .map(arbeidsforhold -> mapArbeidsforholdRSTilDto(arbeidsforhold, interval))
            .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
    }

    private Arbeidsforhold mapArbeidsforholdRSTilDto(ArbeidsforholdRS arbeidsforhold, IntervallEntitet intervall) {
        Arbeidsforhold.Builder builder = new Arbeidsforhold.Builder()
            .medType(arbeidsforhold.getType());

        utledArbeidsgiverRS(arbeidsforhold, builder);

        builder.medArbeidFom(arbeidsforhold.getAnsettelsesperiode().getPeriode().getFom());
        if (arbeidsforhold.getAnsettelsesperiode().getPeriode().getTom() != null) {
            builder.medArbeidTom(arbeidsforhold.getAnsettelsesperiode().getPeriode().getTom());
        }

        builder.medArbeidsavtaler(arbeidsforhold.getArbeidsavtaler().stream()
            .map(aa -> byggArbeidsavtaleRS(aa, arbeidsforhold))
            .filter(av -> overlapperMedIntervall(av, intervall))
            .collect(Collectors.toList()));
        builder.medAnsettelsesPeriode(byggAnsettelsesPeriodeRS(arbeidsforhold));

        builder.medPermisjon(arbeidsforhold.getPermisjonPermitteringer().stream()
            .map(this::byggPermisjonRS)
            .collect(Collectors.toList()));

        return builder.build();
    }

    private void utledArbeidsgiverRS(ArbeidsforholdRS arbeidsforhold, Arbeidsforhold.Builder builder) {
        if (OpplysningspliktigArbeidsgiverRS.Type.Person.equals(arbeidsforhold.getArbeidsgiver().getType())) {
            AktørId arbeidsgiver = new AktørId(arbeidsforhold.getArbeidsgiver().getAktoerId());
            no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person person = new no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person.Builder()
                .medAktørId(arbeidsgiver)
                .build();
            builder.medArbeidsgiver(person);
            final var uuid = UUID.nameUUIDFromBytes(arbeidsforhold.getType().getBytes(StandardCharsets.UTF_8));
            builder.medArbeidsforholdId(uuid.toString());
        } else if (OpplysningspliktigArbeidsgiverRS.Type.Organisasjon.equals(arbeidsforhold.getArbeidsgiver().getType())) {
            no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon organisasjon = new no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon.Builder()
                .medOrgNummer(arbeidsforhold.getArbeidsgiver().getOrganisasjonsnummer())
                .build();
            builder.medArbeidsgiver(organisasjon);
            builder.medArbeidsforholdId(arbeidsforhold.getArbeidsforholdId());
        }
    }

    private Arbeidsavtale byggAnsettelsesPeriodeRS(ArbeidsforholdRS arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder();

        builder.medArbeidsavtaleFom(arbeidsforhold.getAnsettelsesperiode().getPeriode().getFom());
        if (arbeidsforhold.getAnsettelsesperiode().getPeriode().getTom() != null) {
            builder.medArbeidsavtaleTom(arbeidsforhold.getAnsettelsesperiode().getPeriode().getTom());
        }
        builder.erAnsettelsesPerioden();
        return builder.build();
    }

    private Arbeidsavtale byggArbeidsavtaleRS(ArbeidsavtaleRS arbeidsavtale,
                                              ArbeidsforholdRS arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder()
            .medStillingsprosent(arbeidsavtale.getStillingsprosent())
            .medBeregnetAntallTimerPrUke(arbeidsavtale.getBeregnetAntallTimerPrUke())
            .medAvtaltArbeidstimerPerUke(arbeidsavtale.getAntallTimerPrUke())
            .medSisteLønnsendringsdato(arbeidsavtale.getSistLoennsendring());

        PeriodeRS ansettelsesPeriode = arbeidsforhold.getAnsettelsesperiode().getPeriode();
        LocalDate arbeidsavtaleFom = arbeidsavtale.getGyldighetsperiode().getFom();
        LocalDate arbeidsavtaleTom = arbeidsavtale.getGyldighetsperiode().getTom();
        builder.medArbeidsavtaleFom(arbeidsavtaleFom);
        builder.medArbeidsavtaleTom(arbeidsavtaleTom);

        var ansettelseIntervall = ansettelsesPeriode.getTom() != null ? IntervallEntitet.fraOgMedTilOgMed(ansettelsesPeriode.getFom(), ansettelsesPeriode.getTom()) : IntervallEntitet.fraOgMed(ansettelsesPeriode.getFom());

        if (!ansettelseIntervall.inkluderer(arbeidsavtaleFom)) {
            log.info("Arbeidsavtale fom={} ligger utenfor ansettelsesPeriode={}", arbeidsavtaleFom, ansettelseIntervall);
        }

        if (arbeidsavtaleTom != null && arbeidsavtaleTom.isBefore(arbeidsavtaleFom)) {
            log.warn("Arbeidsavtale tom={} er før fom={} for orgnr={}, navArbeidsforholdId={}",
                arbeidsavtaleTom, arbeidsavtaleFom, getIdentifikatorString(arbeidsforhold.getArbeidsgiver().getOrganisasjonsnummer()), arbeidsforhold.getNavArbeidsforholdId());
        }

        return builder.build();
    }

    private String getIdentifikatorString(String arbeidsgiverIdentifikator) {
        if (arbeidsgiverIdentifikator == null) {
            return null;
        }
        int length = arbeidsgiverIdentifikator.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + arbeidsgiverIdentifikator.substring(length - 4);
    }

    private Permisjon byggPermisjonRS(PermisjonPermitteringRS permisjonPermitteringRS) {
        return new Permisjon.Builder()
            .medPermisjonFom(permisjonPermitteringRS.getPeriode().getFom())
            .medPermisjonTom(permisjonPermitteringRS.getPeriode().getTom())
            .medPermisjonsprosent(permisjonPermitteringRS.getProsent())
            .medPermisjonsÅrsak(permisjonPermitteringRS.getType())
            .build();
    }

    private boolean overlapperMedIntervall(Arbeidsavtale av, IntervallEntitet interval) {
        final var interval1 = av.getArbeidsavtaleTom() == null ? IntervallEntitet.fraOgMed(av.getArbeidsavtaleFom()) : IntervallEntitet.fraOgMedTilOgMed(av.getArbeidsavtaleFom(), av.getArbeidsavtaleTom());
        return interval.overlapper(interval1);
    }

    static record Key(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdId) {
    }
}
