package no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold;

import static no.nav.foreldrepenger.abakus.felles.jpa.IntervallUtil.byggIntervall;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
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
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.AnsettelsesPeriode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Gyldighetsperiode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.NorskIdent;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Organisasjon;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Periode;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Person;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Regelverker;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdConsumer;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class ArbeidsforholdTjeneste {

    private static final String TJENESTE = "Arbeidsforhold";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArbeidsforholdTjeneste.class);
    private static boolean isProd = Environment.current().isProd();
    private ArbeidsforholdConsumer arbeidsforholdConsumer;
    private TpsTjeneste tpsTjeneste;
    private AaregRestKlient aaregRestKlient;

    public ArbeidsforholdTjeneste() {
        // CDI
    }

    @Inject
    public ArbeidsforholdTjeneste(ArbeidsforholdConsumer arbeidsforholdConsumer, TpsTjeneste tpsTjeneste, AaregRestKlient aaregRestKlient) {
        this.arbeidsforholdConsumer = arbeidsforholdConsumer;
        this.tpsTjeneste = tpsTjeneste;
        this.aaregRestKlient = aaregRestKlient;
    }

    public Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> finnArbeidsforholdForIdentIPerioden(PersonIdent fnr, AktørId aktørId, Interval interval) {
        final FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstakerResponse = finnArbeidsForhold(fnr, interval);

        // Tar bare de arbeidsforholdene som er løpende.
        var mapWS = mapArbeidsforholdResponseToArbeidsforhold(finnArbeidsforholdPrArbeidstakerResponse, interval);
        if (isProd) {
            hentRsSjekkDiff(mapWS, aktørId, interval);
        }
        return mapWS;
    }

    FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsForhold(PersonIdent fnr, Interval opplysningsPeriode) {
        FinnArbeidsforholdPrArbeidstakerRequest request = new FinnArbeidsforholdPrArbeidstakerRequest();
        Periode periode = new Periode();
        NorskIdent ident = new NorskIdent();
        Regelverker regelverk = new Regelverker();

        try {
            periode.setFom(DateUtil.convertToXMLGregorianCalendar(LocalDateTime.ofInstant(opplysningsPeriode.getStart(), ZoneId.systemDefault())));
            periode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDateTime.ofInstant(opplysningsPeriode.getEnd(), ZoneId.systemDefault())));
            request.setArbeidsforholdIPeriode(periode);

            ident.setIdent(fnr.getIdent());
            request.setIdent(ident);

            regelverk.setKodeRef("A_ORDNINGEN");
            regelverk.setValue("A_ORDNINGEN");
            request.setRapportertSomRegelverk(regelverk);
            return arbeidsforholdConsumer.finnArbeidsforholdPrArbeidstaker(request);
        } catch (FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (FinnArbeidsforholdPrArbeidstakerUgyldigInput e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        }
    }

    Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> mapArbeidsforholdResponseToArbeidsforhold(FinnArbeidsforholdPrArbeidstakerResponse response, Interval interval) {
        if (response != null) {
            return response.getArbeidsforhold().stream()
                .map(arbeidsforhold -> mapArbeidsforholdTilDto(arbeidsforhold, interval))
                .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
        }

        return Collections.emptyMap();
    }

    private Arbeidsforhold mapArbeidsforholdTilDto(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold, Interval intervall) {
        Arbeidsforhold.Builder builder = new Arbeidsforhold.Builder()
            .medType(arbeidsforhold.getArbeidsforholdstype().getKodeRef());

        utledArbeidsgiver(arbeidsforhold, builder);

        AnsettelsesPeriode ansettelsesPeriode = arbeidsforhold.getAnsettelsesPeriode();
        builder.medArbeidFom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getFom()));
        if (ansettelsesPeriode.getPeriode().getTom() != null) {
            builder.medArbeidTom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getTom()));
        }

        builder.medArbeidsavtaler(hentHistoriskeArbeidsAvtaler(arbeidsforhold, intervall));
        builder.medAnsettelsesPeriode(byggAnsettelsesPeriode(arbeidsforhold));

        builder.medPermisjon(arbeidsforhold.getPermisjonOgPermittering().stream()
            .map(this::byggPermisjonDto)
            .collect(Collectors.toList()));

        return builder.build();
    }

    private void utledArbeidsgiver(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold, Arbeidsforhold.Builder builder) {
        if (arbeidsforhold.getArbeidsgiver() instanceof Person) {
            Person arbeidsgiver = (Person) arbeidsforhold.getArbeidsgiver();
            AktørId aktørId = hentAktørIdForIdent(arbeidsgiver).orElseThrow(() -> new IllegalStateException("Fant ikke aktørId for ident " + arbeidsgiver.getAktoerId()));
            no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person person = new no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person.Builder()
                .medAktørId(aktørId)
                .build();
            builder.medArbeidsgiver(person);
            final var uuid = UUID.nameUUIDFromBytes(arbeidsforhold.getArbeidsforholdstype().getValue().getBytes(StandardCharsets.UTF_8));
            builder.medArbeidsforholdId(uuid.toString());
        } else if (arbeidsforhold.getArbeidsgiver() instanceof Organisasjon) {
            Organisasjon arbeidsgiver = (Organisasjon) arbeidsforhold.getArbeidsgiver();
            no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon organisasjon = new no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon.Builder()
                .medOrgNummer(arbeidsgiver.getOrgnummer())
                .build();
            builder.medArbeidsgiver(organisasjon);
            builder.medArbeidsforholdId(arbeidsforhold.getArbeidsforholdID());
        }
    }

    private Arbeidsavtale byggAnsettelsesPeriode(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder();

        final AnsettelsesPeriode ansettelsesPeriode = arbeidsforhold.getAnsettelsesPeriode();
        builder.medArbeidsavtaleFom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getFom()));
        if (ansettelsesPeriode.getPeriode().getTom() != null) {
            builder.medArbeidsavtaleTom(DateUtil.convertToLocalDate(ansettelsesPeriode.getPeriode().getTom()));
        }
        builder.erAnsettelsesPerioden();
        return builder.build();
    }

    private Optional<AktørId> hentAktørIdForIdent(Person arbeidsgiver) {
        return tpsTjeneste.hentAktørForFnr(PersonIdent.fra(arbeidsgiver.getIdent().getIdent()));
    }

    private List<Arbeidsavtale> hentHistoriskeArbeidsAvtaler(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold,
                                                             Interval intervall) {
        final HentArbeidsforholdHistorikkRequest request = new HentArbeidsforholdHistorikkRequest();
        request.setArbeidsforholdId(arbeidsforhold.getArbeidsforholdIDnav());
        try {
            final HentArbeidsforholdHistorikkResponse response = arbeidsforholdConsumer.hentArbeidsforholdHistorikk(request);
            if (response.getArbeidsforhold() == null) {
                return Collections.emptyList();
            }
            return response.getArbeidsforhold().getArbeidsavtale().stream().map(aa -> byggArbeidsavtaleDto(aa, arbeidsforhold))
                .filter(av -> overlapperMedIntervall(av, intervall))
                .collect(Collectors.toList());
        } catch (HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (HentArbeidsforholdHistorikkSikkerhetsbegrensning e) {
            throw ArbeidsforholdTjenesteFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        }
    }

    private boolean overlapperMedIntervall(Arbeidsavtale av, Interval interval) {
        final Interval interval1 = byggIntervall(av.getArbeidsavtaleFom(), av.getArbeidsavtaleTom() != null ? av.getArbeidsavtaleTom() : Tid.TIDENES_ENDE);
        return interval.overlaps(interval1);
    }

    private Arbeidsavtale byggArbeidsavtaleDto(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsavtale arbeidsavtale,
                                               no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.Arbeidsforhold arbeidsforhold) {
        Arbeidsavtale.Builder builder = new Arbeidsavtale.Builder()
            .medStillingsprosent(arbeidsavtale.getStillingsprosent())
            .medBeregnetAntallTimerPrUke(arbeidsavtale.getBeregnetAntallTimerPrUke())
            .medAvtaltArbeidstimerPerUke(arbeidsavtale.getAvtaltArbeidstimerPerUke())
            .medSisteLønnsendringsdato(DateUtil.convertToLocalDate(arbeidsavtale.getSisteLoennsendringsdato()));

        Gyldighetsperiode ansettelsesPeriode = arbeidsforhold.getAnsettelsesPeriode().getPeriode();
        LocalDate arbeidsavtaleFom = DateUtil.convertToLocalDate(arbeidsavtale.getFomGyldighetsperiode());
        LocalDate arbeidsavtaleTom = DateUtil.convertToLocalDate(arbeidsavtale.getTomGyldighetsperiode());
        builder.medArbeidsavtaleFom(arbeidsavtaleFom);
        builder.medArbeidsavtaleTom(arbeidsavtaleTom);

        Interval ansettelsesIntervall = byggIntervall(
            DateUtil.convertToLocalDate(ansettelsesPeriode.getFom()),
            DateUtil.convertToLocalDate(ansettelsesPeriode.getTom()));

        if (!ansettelsesIntervall.contains(arbeidsavtaleFom.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())) {
            LOGGER.info("Arbeidsavtale fom={} ligger utenfor ansettelsesPeriode={}", arbeidsavtaleFom, ansettelsesIntervall);
        }
        return builder.build();
    }

    private Permisjon byggPermisjonDto(no.nav.tjeneste.virksomhet.arbeidsforhold.v3.informasjon.arbeidsforhold.PermisjonOgPermittering permisjonOgPermittering) {
        return new Permisjon.Builder()
            .medPermisjonFom(DateUtil.convertToLocalDate(permisjonOgPermittering.getPermisjonsPeriode().getFom()))
            .medPermisjonTom(DateUtil.convertToLocalDate(permisjonOgPermittering.getPermisjonsPeriode().getTom()))
            .medPermisjonsprosent(permisjonOgPermittering.getPermisjonsprosent())
            .medPermisjonsÅrsak(permisjonOgPermittering.getPermisjonOgPermittering().getKodeRef())
            .build();
    }

    private void hentRsSjekkDiff(Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> ws, AktørId aktørId, Interval opplPeriode) {
        try {
            List<ArbeidsforholdRS> response = aaregRestKlient.finnArbeidsforholdForArbeidstaker(aktørId.getId(),
                LocalDate.ofInstant(opplPeriode.getStart(), ZoneId.systemDefault()), LocalDate.ofInstant(opplPeriode.getEnd(), ZoneId.systemDefault()));
            var rs = response.stream()
                .map(arbeidsforhold -> mapArbeidsforholdRSTilDto(arbeidsforhold, opplPeriode))
                .collect(Collectors.groupingBy(Arbeidsforhold::getIdentifikator));
            var like = ws.entrySet().stream().allMatch(e -> likeListerArbeidsforhold(e.getValue(), rs.getOrDefault(e.getKey(), List.of()))) &&
                rs.entrySet().stream().allMatch(e -> likeListerArbeidsforhold(e.getValue(), ws.getOrDefault(e.getKey(), List.of())));
            if (like) {
                LOGGER.info("ABAKUS AAREG RS like svar med ws");
            } else {
                loggAvvikendeArbeidsforhold(ws, rs);
            }
        } catch (Exception e) {
            LOGGER.info("ABAKUS AAREG RS feil", e);
        }
    }

    private boolean likeListerArbeidsforhold(List<Arbeidsforhold> l1, List<Arbeidsforhold> l2) {
        if (l1 == null && l2 == null)
            return true;
        if (l1 == null || l2 == null)
            return false;
        return l1.containsAll(l2) && l2.containsAll(l1);
    }

    private void loggAvvikendeArbeidsforhold(Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> m1,
                                             Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> m2) {
        List<Arbeidsforhold> l1 = new ArrayList<>();
        List<Arbeidsforhold> l2 = new ArrayList<>();
        List<Arbeidsforhold> avvik1 = new ArrayList<>();
        List<Arbeidsforhold> avvik2 = new ArrayList<>();
        m1.forEach((key, value) -> l1.addAll(value));
        m2.forEach((key, value) -> l2.addAll(value));
        l1.forEach(a -> {
            if (!l2.contains(a)) avvik1.add(a);
        });
        l2.forEach(a -> {
            if (!l1.contains(a)) avvik2.add(a);
        });
        LOGGER.info("ABAKUS AAREG RS avvik ws {} rs {}", avvik1, avvik2);
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
            no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person person = new no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person.Builder()
                .medAktørId(new AktørId(arbeidsforhold.getArbeidsgiver().getAktoerId()))
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

}
