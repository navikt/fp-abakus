package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.arbeidsforhold;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.ArbeidstakersArbeidsforholdDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsforholdRefDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverDto;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.ArbeidsgiverType;
import no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay.PeriodeDto;
import no.nav.foreldrepenger.abakus.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsforhold;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdIdentifikator;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.ArbeidsforholdTjeneste;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Organisasjon;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Person;
import no.nav.foreldrepenger.abakus.registerdata.arbeidsgiver.person.TpsTjeneste;
import no.nav.foreldrepenger.abakus.typer.AktørId;
import no.nav.foreldrepenger.abakus.typer.ArbeidsforholdRef;
import no.nav.foreldrepenger.abakus.typer.Fagsystem;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.foreldrepenger.abakus.util.IntervallUtil;

@ApplicationScoped
public class ArbeidsforholdDtoTjeneste {

    private ArbeidsforholdTjeneste arbeidsforholdTjeneste;
    private TpsTjeneste tpsTjeneste;
    private KodeverkRepository kodeverkRepository;

    ArbeidsforholdDtoTjeneste() {
    }

    @Inject
    public ArbeidsforholdDtoTjeneste(ArbeidsforholdTjeneste arbeidsforholdTjeneste, TpsTjeneste tpsTjeneste, KodeverkRepository kodeverkRepository) {
        this.arbeidsforholdTjeneste = arbeidsforholdTjeneste;
        this.tpsTjeneste = tpsTjeneste;
        this.kodeverkRepository = kodeverkRepository;
    }

    public ArbeidstakersArbeidsforholdDto mapFor(AktørId aktørId, LocalDate fom, LocalDate tom) {
        PersonIdent personIdent = tpsTjeneste.hentFnrForAktør(aktørId);
        Map<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforhold = arbeidsforholdTjeneste.finnArbeidsforholdForIdentIPerioden(personIdent, IntervallUtil.byggIntervall(fom, tom));

        ArbeidstakersArbeidsforholdDto dto = new ArbeidstakersArbeidsforholdDto();
        dto.setArbeidsforhold(arbeidsforhold.entrySet().stream().map(this::mapTilArbeidsforhold).collect(Collectors.toList()));
        return dto;
    }

    private ArbeidsforholdDto mapTilArbeidsforhold(Map.Entry<ArbeidsforholdIdentifikator, List<Arbeidsforhold>> arbeidsforholdEntry) {
        ArbeidsforholdDto dto = new ArbeidsforholdDto();
        ArbeidsforholdIdentifikator key = arbeidsforholdEntry.getKey();
        dto.setArbeidsgiver(mapArbeidsgiver(key.getArbeidsgiver()));
        dto.setArbeidsforholdId(mapArbeidsforholdId(key.getArbeidsforholdId()));
        dto.setAnsettelsesperiode(mapAnsettelsesPerioder(arbeidsforholdEntry.getValue()));
        dto.setType(kodeverkRepository.finn(ArbeidType.class, key.getType()));
        return dto;
    }

    private List<PeriodeDto> mapAnsettelsesPerioder(List<Arbeidsforhold> arbeidsforhold) {
        return arbeidsforhold.stream().map(it -> new PeriodeDto(it.getArbeidFom(), it.getArbeidTom())).collect(Collectors.toList());
    }

    private ArbeidsforholdRefDto mapArbeidsforholdId(ArbeidsforholdRef arbeidsforholdId) {
        if (arbeidsforholdId == null) {
            return null;
        }
        ArbeidsforholdRefDto arbeidsforholdRefDto = new ArbeidsforholdRefDto();
        arbeidsforholdRefDto.leggTilReferanse(Fagsystem.AAREGISTERET, arbeidsforholdId.getReferanse());
        return arbeidsforholdRefDto;
    }

    private ArbeidsgiverDto mapArbeidsgiver(no.nav.foreldrepenger.abakus.registerdata.arbeidsforhold.Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver instanceof Person) {
            return new ArbeidsgiverDto(arbeidsgiver.getIdentifikator(), ArbeidsgiverType.PRIVAT);
        } else if (arbeidsgiver instanceof Organisasjon) {
            return new ArbeidsgiverDto(arbeidsgiver.getIdentifikator(), ArbeidsgiverType.VIRKSOMHET);
        }
        throw new IllegalArgumentException("Utvikler feil: Arbeidsgiver av ukjent type.");
    }

    public ArbeidsforholdReferanseDto mapArbeidsforhold(ArbeidsgiverDto arbeidsgiver, String eksternReferanse, String internReferanse) {
        ArbeidsforholdReferanseDto dto = new ArbeidsforholdReferanseDto();
        dto.setArbeidsgiver(arbeidsgiver);
        ArbeidsforholdRefDto refDto = new ArbeidsforholdRefDto();
        refDto.leggTilReferanse(Fagsystem.AAREGISTERET, eksternReferanse);
        refDto.leggTilReferanse(Fagsystem.FPABAKUS, internReferanse);
        dto.setArbeidsforholdReferanse(refDto);

        return dto;
    }
}
