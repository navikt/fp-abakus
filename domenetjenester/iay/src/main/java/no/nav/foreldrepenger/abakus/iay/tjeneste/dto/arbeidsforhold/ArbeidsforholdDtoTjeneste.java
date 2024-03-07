package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.arbeid.v1.PermisjonDto;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsavtaleDto;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdDto;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdReferanseDto;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.abakus.iaygrunnlag.kodeverk.PermisjonsbeskrivelseType;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.aktor.AktørTjeneste;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.EksternArbeidsforholdRef;
import no.nav.fpsak.tidsserie.LocalDateInterval;
import no.nav.fpsak.tidsserie.LocalDateSegment;
import no.nav.fpsak.tidsserie.LocalDateTimeline;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class ArbeidsforholdDtoTjeneste {

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private AktørTjeneste aktørConsumer;

    ArbeidsforholdDtoTjeneste() {
    }

    @Inject
    public ArbeidsforholdDtoTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste, AktørTjeneste aktørConsumer) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    public List<ArbeidsforholdDto> mapFor(AktørId aktørId, LocalDate fom, LocalDate tom, YtelseType ytelse) {
        var ident = aktørConsumer.hentIdentForAktør(aktørId, ytelse).orElseThrow();
        var intervall = tom == null ? IntervallEntitet.fraOgMed(fom) : IntervallEntitet.fraOgMedTilOgMed(fom, tom);
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(ident,
            aktørId, intervall);

        return arbeidsforhold.entrySet().stream().map(this::mapTilArbeidsforhold).collect(Collectors.toList());
    }

    public List<ArbeidsforholdDto> mapArbForholdOgPermisjoner(AktørId aktørId, LocalDate fom, LocalDate tom, YtelseType ytelse) {
        var ident = aktørConsumer.hentIdentForAktør(aktørId, ytelse).orElseThrow();
        var intervall = tom == null ? IntervallEntitet.fraOgMed(fom) : IntervallEntitet.fraOgMedTilOgMed(fom, tom);
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(ident,
            aktørId, intervall);

        return arbeidsforhold.entrySet().stream().map(this::mapTilArbeidsforholdMedPermisjoner).collect(Collectors.toList());
    }

    private ArbeidsforholdDto mapTilArbeidsforholdMedPermisjoner(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholdEntry) {
        ArbeidsforholdIdentifikator key = arbeidsforholdEntry.getKey();
        Aktør arbeidsgiver = mapArbeidsgiver(key.getArbeidsgiver());
        ArbeidType arbeidType = ArbeidType.finnForKodeverkEiersKode(key.getType());
        ArbeidsforholdDto dto = new ArbeidsforholdDto(arbeidsgiver, arbeidType);
        dto.setArbeidsforholdId(mapArbeidsforholdId(key.getArbeidsforholdId()));
        dto.setAnsettelsesperiode(mapAnsettelsesPerioder(arbeidsforholdEntry.getValue()));
        dto.setArbeidsavtaler(arbeidsforholdEntry.getValue().stream().map(this::mapArbeidsavtaler).flatMap(Collection::stream).toList());
        dto.setPermisjoner(arbeidsforholdEntry.getValue().stream().map(this::mapPermisjoner).flatMap(Collection::stream).toList());
        return dto;
    }

    private List<ArbeidsavtaleDto> mapArbeidsavtaler(Arbeidsforhold arbeidsforhold) {
        LocalDateInterval ansettelse = new LocalDateInterval(arbeidsforhold.getArbeidFom(), arbeidsforhold.getArbeidTom());
        var tidslinje = arbeidsforhold.getArbeidsavtaler().stream()
            .filter(arbeidsavtale -> !arbeidsavtale.getErAnsettelsesPerioden())
            .filter(arbeidsavtale -> arbeidsavtale.getStillingsprosent() != null)
            .map(a -> new LocalDateSegment<>(a.getArbeidsavtaleFom(), a.getArbeidsavtaleTom(), a.getStillingsprosent()))
            .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));
        return tidslinje.intersection(ansettelse).stream().map(s -> new ArbeidsavtaleDto(new Periode(safeFom(s.getFom()), safeTom(s.getTom())), s.getValue())).toList();
    }

    private List<PermisjonDto> mapPermisjoner(Arbeidsforhold arbeidsforhold) {
        LocalDateInterval ansettelse = new LocalDateInterval(arbeidsforhold.getArbeidFom(), arbeidsforhold.getArbeidTom());
        var tidslinje = arbeidsforhold.getPermisjoner()
            .stream()
            .filter(permisjon -> permisjon.getPermisjonsprosent() != null)
            .map(p -> new LocalDateSegment<>(p.getPermisjonFom(), p.getPermisjonTom(),
                new PermisjonTidslinjeObjekt(p.getPermisjonsprosent(), p.getPermisjonsÅrsak())))
            .collect(Collectors.collectingAndThen(Collectors.toList(), LocalDateTimeline::new));

        return tidslinje.intersection(ansettelse)
            .stream()
            .map(s -> new PermisjonDto(new Periode(safeFom(s.getFom()), safeTom(s.getTom())),
                PermisjonsbeskrivelseType.finnForKodeverkEiersKode(s.getValue().permisjonsÅrsak))
                .medProsentsats(s.getValue().permisjonsprosent()))
            .toList();
    }

    private static LocalDate safeFom(LocalDate fom) {
        return fom != null ? fom : Tid.TIDENES_BEGYNNELSE;
    }

    private static LocalDate safeTom(LocalDate tom) {
        return tom!= null ? tom: Tid.TIDENES_ENDE;
    }

    private record PermisjonTidslinjeObjekt(BigDecimal permisjonsprosent, String permisjonsÅrsak) {}

    private ArbeidsforholdDto mapTilArbeidsforhold(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholdEntry) {
        ArbeidsforholdIdentifikator key = arbeidsforholdEntry.getKey();
        Aktør arbeidsgiver = mapArbeidsgiver(key.getArbeidsgiver());
        ArbeidType arbeidType = ArbeidType.finnForKodeverkEiersKode(key.getType());
        ArbeidsforholdDto dto = new ArbeidsforholdDto(arbeidsgiver, arbeidType);
        dto.setArbeidsforholdId(mapArbeidsforholdId(key.getArbeidsforholdId()));
        dto.setAnsettelsesperiode(mapAnsettelsesPerioder(arbeidsforholdEntry.getValue()));
        return dto;
    }

    private List<Periode> mapAnsettelsesPerioder(List<Arbeidsforhold> arbeidsforhold) {
        return arbeidsforhold.stream().map(it -> new Periode(it.getArbeidFom(), it.getArbeidTom())).collect(Collectors.toList());
    }

    private ArbeidsforholdRefDto mapArbeidsforholdId(EksternArbeidsforholdRef arbeidsforholdId) {
        if (arbeidsforholdId == null || arbeidsforholdId.getReferanse() == null || arbeidsforholdId.getReferanse().isEmpty()) {
            return null;
        }
        return new ArbeidsforholdRefDto(null, arbeidsforholdId.getReferanse());
    }

    private Aktør mapArbeidsgiver(no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver instanceof Person person) {
            return new AktørIdPersonident(person.getAktørId());
        } else if (arbeidsgiver instanceof Organisasjon organisasjon) {
            return new no.nav.abakus.iaygrunnlag.Organisasjon(organisasjon.getOrgNummer());
        }
        throw new IllegalArgumentException("Utvikler feil: ArbeidsgiverEntitet av ukjent type.");
    }

    public ArbeidsforholdReferanseDto mapArbeidsforhold(Aktør arbeidsgiver, String eksternReferanse, String internReferanse) {
        return new ArbeidsforholdReferanseDto(arbeidsgiver, new ArbeidsforholdRefDto(internReferanse, eksternReferanse));
    }
}
