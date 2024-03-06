package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;

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

    private static final Logger LOG = LoggerFactory.getLogger(ArbeidsforholdTjeneste.class);
    private AaregRestKlient aaregRestKlient;

    public ArbeidsforholdTjeneste() {
        // CDI
    }

    @Inject
    public ArbeidsforholdTjeneste(AaregRestKlient aaregRestKlient) {
        this.aaregRestKlient = aaregRestKlient;
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent ident, IntervallEntitet interval,
                                                                                                      AktørId aktørId) {
        // TODO: kall med aktørid når register har fikset ytelsesproblemer
        List<ArbeidsforholdRS> response = aaregRestKlient.finnArbeidsforholdForArbeidstaker(ident.getIdent(), interval.getFomDato(),
            interval.getTomDato());
        var mapArbeidsforhold = response.stream()
            .map(arbeidsforhold -> mapArbeidsforholdRSTilDto(arbeidsforhold, interval))
            .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));

        valider(mapArbeidsforhold);

        return mapArbeidsforhold;
    }

    private void valider(Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> mapArbeidsforhold) {
        var gruppert = mapArbeidsforhold.keySet().stream().collect(Collectors.groupingBy(r -> new Key(r.getArbeidsgiver(), r.getArbeidsforholdId())));
        var dups = gruppert.entrySet().stream().filter(e -> e.getValue().size() > 1) // duplikater
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (!dups.isEmpty()) {
            String msg = "Mottatt duplikater for arbeidsforhold fra AAreg: " + dups;
            LOG.warn(msg);
            // throw new IllegalStateException(msg);
        }
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdFrilansForIdentIPerioden(PersonIdent ident,
                                                                                                             AktørId aktørId,
                                                                                                             IntervallEntitet interval) {
        // TODO: kall med aktørid når register har fikset ytelsesproblemer
        List<ArbeidsforholdRS> response = aaregRestKlient.finnArbeidsforholdForFrilanser(ident.getIdent(), interval.getFomDato(),
            interval.getTomDato());
        return response.stream()
            .map(arbeidsforhold -> mapArbeidsforholdRSTilDto(arbeidsforhold, interval))
            .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
    }

    private Arbeidsforhold mapArbeidsforholdRSTilDto(ArbeidsforholdRS arbeidsforhold, IntervallEntitet intervall) {
        Arbeidsforhold.Builder builder = new Arbeidsforhold.Builder().medType(arbeidsforhold.getType());

        utledArbeidsgiverRS(arbeidsforhold, builder);

        builder.medArbeidFom(arbeidsforhold.getAnsettelsesperiode().periode().fom());
        if (arbeidsforhold.getAnsettelsesperiode().periode().tom() != null) {
            builder.medArbeidTom(arbeidsforhold.getAnsettelsesperiode().periode().tom());
        }

        builder.medArbeidsavtaler(arbeidsforhold.getArbeidsavtaler()
            .stream()
            .map(aa -> byggArbeidsavtaleRS(aa, arbeidsforhold))
            .filter(av -> overlapperMedIntervall(av, intervall))
            .collect(Collectors.toList()));
        builder.medAnsettelsesPeriode(byggAnsettelsesPeriodeRS(arbeidsforhold));

        builder.medPermisjon(arbeidsforhold.getPermisjonPermitteringer().stream().map(this::byggPermisjonRS).collect(Collectors.toList()));

        return builder.build();
    }

    private void utledArbeidsgiverRS(ArbeidsforholdRS arbeidsforhold, Arbeidsforhold.Builder builder) {
        if (OpplysningspliktigArbeidsgiverRS.Type.Person.equals(arbeidsforhold.getArbeidsgiver().type())) {
            AktørId arbeidsgiver = new AktørId(arbeidsforhold.getArbeidsgiver().aktoerId());
            Person person = new Person.Builder().medAktørId(
                arbeidsgiver).build();
            builder.medArbeidsgiver(person);
            final var uuid = UUID.nameUUIDFromBytes(arbeidsforhold.getType().getBytes(StandardCharsets.UTF_8));
            builder.medArbeidsforholdId(uuid.toString());
        } else if (OpplysningspliktigArbeidsgiverRS.Type.Organisasjon.equals(arbeidsforhold.getArbeidsgiver().type())) {
            Organisasjon organisasjon = new Organisasjon.Builder().medOrgNummer(
                arbeidsforhold.getArbeidsgiver().organisasjonsnummer()).build();
            builder.medArbeidsgiver(organisasjon);
            builder.medArbeidsforholdId(arbeidsforhold.getArbeidsforholdId());
        }
    }

    private Arbeidsavtale byggAnsettelsesPeriodeRS(ArbeidsforholdRS arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder();

        builder.medArbeidsavtaleFom(arbeidsforhold.getAnsettelsesperiode().periode().fom());
        if (arbeidsforhold.getAnsettelsesperiode().periode().tom() != null) {
            builder.medArbeidsavtaleTom(arbeidsforhold.getAnsettelsesperiode().periode().tom());
        }
        builder.erAnsettelsesPerioden();
        return builder.build();
    }

    private Arbeidsavtale byggArbeidsavtaleRS(ArbeidsavtaleRS arbeidsavtale, ArbeidsforholdRS arbeidsforhold) {
        var stillingsprosent = Stillingsprosent.normaliserData(arbeidsavtale.stillingsprosent());
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder().medStillingsprosent(stillingsprosent)
            .medSisteLønnsendringsdato(arbeidsavtale.sistLoennsendring());

        PeriodeRS ansettelsesPeriode = arbeidsforhold.getAnsettelsesperiode().periode();
        LocalDate arbeidsavtaleFom = arbeidsavtale.gyldighetsperiode().fom();
        LocalDate arbeidsavtaleTom = arbeidsavtale.gyldighetsperiode().tom();
        builder.medArbeidsavtaleFom(arbeidsavtaleFom);
        builder.medArbeidsavtaleTom(arbeidsavtaleTom);

        var ansettelseIntervall = ansettelsesPeriode.tom() != null ? IntervallEntitet.fraOgMedTilOgMed(ansettelsesPeriode.fom(),
            ansettelsesPeriode.tom()) : IntervallEntitet.fraOgMed(ansettelsesPeriode.fom());

        if (!ansettelseIntervall.inkluderer(arbeidsavtaleFom)) {
            LOG.info("Arbeidsavtale fom={} ligger utenfor ansettelsesPeriode={}", arbeidsavtaleFom, ansettelseIntervall);
        }

        if (arbeidsavtaleTom != null && arbeidsavtaleTom.isBefore(arbeidsavtaleFom)) {
            LOG.warn("Arbeidsavtale tom={} er før fom={} for orgnr={}, navArbeidsforholdId={}", arbeidsavtaleTom, arbeidsavtaleFom,
                getIdentifikatorString(arbeidsforhold.getArbeidsgiver().organisasjonsnummer()), arbeidsforhold.getNavArbeidsforholdId());
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
        return new Permisjon.Builder().medPermisjonFom(permisjonPermitteringRS.periode().fom())
            .medPermisjonTom(permisjonPermitteringRS.periode().tom())
            .medPermisjonsprosent(permisjonPermitteringRS.prosent())
            .medPermisjonsÅrsak(permisjonPermitteringRS.type())
            .build();
    }

    private boolean overlapperMedIntervall(Arbeidsavtale av, IntervallEntitet interval) {
        final var interval1 =
            av.getArbeidsavtaleTom() == null ? IntervallEntitet.fraOgMed(av.getArbeidsavtaleFom()) : IntervallEntitet.fraOgMedTilOgMed(
                av.getArbeidsavtaleFom(), av.getArbeidsavtaleTom());
        return interval.overlapper(interval1);
    }

    record Key(Arbeidsgiver arbeidsgiver, EksternArbeidsforholdRef arbeidsforholdId) {
    }
}
