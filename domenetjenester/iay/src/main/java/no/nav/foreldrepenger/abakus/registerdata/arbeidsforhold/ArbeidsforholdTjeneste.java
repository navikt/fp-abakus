package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import static no.nav.foreldrepenger.abakus.felles.jpa.IntervallUtil.byggIntervall;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.AaregRestKlient;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.ArbeidsavtaleRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.ArbeidsforholdRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.OpplysningspliktigArbeidsgiverRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.PeriodeRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.rest.PermisjonPermitteringRS;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.TpsTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class ArbeidsforholdTjeneste {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTjeneste.class);
    private TpsTjeneste tpsTjeneste;
    private AaregRestKlient aaregRestKlient;

    public ArbeidsforholdTjeneste() {
        // CDI
    }

    @Inject
    public ArbeidsforholdTjeneste(TpsTjeneste tpsTjeneste, AaregRestKlient aaregRestKlient) {
        this.tpsTjeneste = tpsTjeneste;
        this.aaregRestKlient = aaregRestKlient;
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent ident, AktørId aktørId, Interval interval) {
        // TODO: kall med aktørid når register har fikset ytelsesproblemer
        List<ArbeidsforholdRS> response = aaregRestKlient.finnArbeidsforholdForArbeidstaker(ident.getIdent(),
            LocalDate.ofInstant(interval.getStart(), ZoneId.systemDefault()), LocalDate.ofInstant(interval.getEnd(), ZoneId.systemDefault()));
        return response.stream()
            .map(arbeidsforhold -> mapArbeidsforholdRSTilDto(arbeidsforhold, interval))
            .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
    }

    private Arbeidsforhold mapArbeidsforholdRSTilDto(ArbeidsforholdRS arbeidsforhold, Interval intervall) {
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
            AktørId arbeidsgiver;
            if (arbeidsforhold.getArbeidsgiver().getAktoerId() == null) {
                LOGGER.info("ABAKUS AAREG RS privat ag uten aktoerId");
                arbeidsgiver = hentAktørIdForIdent(PersonIdent.fra(arbeidsforhold.getArbeidsgiver().getOffentligIdent()))
                    .orElseThrow(() -> new IllegalStateException("Fant ikke aktørId for ident " + arbeidsforhold.getArbeidsgiver().getOffentligIdent()));
            } else {
                LOGGER.warn("ABAKUS AAREG RS privat ag med aktoerId. På tide å fikse koden i ArbeidsforholdTjeneste!");
                arbeidsgiver = new AktørId(arbeidsforhold.getArbeidsgiver().getAktoerId());
            }
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

        Interval ansettelsesIntervall = byggIntervall(ansettelsesPeriode.getFom(), ansettelsesPeriode.getTom());

        if (!ansettelsesIntervall.contains(arbeidsavtaleFom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) {
            LOGGER.info("Arbeidsavtale fom={} ligger utenfor ansettelsesPeriode={}", arbeidsavtaleFom, ansettelsesIntervall);
        }
        return builder.build();
    }

    private Permisjon byggPermisjonRS(PermisjonPermitteringRS permisjonPermitteringRS) {
        return new Permisjon.Builder()
            .medPermisjonFom(permisjonPermitteringRS.getPeriode().getFom())
            .medPermisjonTom(permisjonPermitteringRS.getPeriode().getTom())
            .medPermisjonsprosent(permisjonPermitteringRS.getProsent())
            .medPermisjonsÅrsak(permisjonPermitteringRS.getType())
            .build();
    }

    private Optional<AktørId> hentAktørIdForIdent(PersonIdent arbeidsgiver) {
        return tpsTjeneste.hentAktørForFnr(arbeidsgiver);
    }

    private boolean overlapperMedIntervall(Arbeidsavtale av, Interval interval) {
        final Interval interval1 = byggIntervall(av.getArbeidsavtaleFom(), av.getArbeidsavtaleTom() != null ? av.getArbeidsavtaleTom() : Tid.TIDENES_ENDE);
        return interval.overlaps(interval1);
    }



}
