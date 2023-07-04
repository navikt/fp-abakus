package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.abakus.iaygrunnlag.Aktør;
import no.nav.abakus.iaygrunnlag.AktørIdPersonident;
import no.nav.abakus.iaygrunnlag.ArbeidsforholdRefDto;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdDto;
import no.nav.abakus.iaygrunnlag.arbeidsforhold.v1.ArbeidsforholdReferanseDto;
import no.nav.abakus.iaygrunnlag.kodeverk.ArbeidType;
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
