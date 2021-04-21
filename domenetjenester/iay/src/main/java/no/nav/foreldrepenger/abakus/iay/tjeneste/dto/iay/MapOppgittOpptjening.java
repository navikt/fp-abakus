package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.abakus.iaygrunnlag.JournalpostId;
import no.nav.abakus.iaygrunnlag.Organisasjon;
import no.nav.abakus.iaygrunnlag.Periode;
import no.nav.abakus.iaygrunnlag.kodeverk.Landkode;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittAnnenAktivitetDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittArbeidsforholdDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittEgenNæringDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansoppdragDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;
import no.nav.abakus.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningerDto;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder;
import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;

public class MapOppgittOpptjening {

    private static final Comparator<OppgittFrilansoppdragDto> COMP_FRILANSOPPDRAG = Comparator
        .comparing(OppgittFrilansoppdragDto::getOppdragsgiver, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<OppgittAnnenAktivitetDto> COMP_ANNEN_AKTIVITET = Comparator
        .comparing((OppgittAnnenAktivitetDto dto) -> dto.getArbeidTypeDto() == null ? null : dto.getArbeidTypeDto().getKode(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<OppgittArbeidsforholdDto> COMP_OPPGITT_ARBEIDSFORHOLD = Comparator
        .comparing((OppgittArbeidsforholdDto dto) -> dto.getArbeidTypeDto() == null ? null : dto.getArbeidTypeDto().getKode(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getLandkode() == null ? null : dto.getLandkode().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(OppgittArbeidsforholdDto::getInntekt, Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(OppgittArbeidsforholdDto::getVirksomhetNavn, Comparator.nullsLast(Comparator.naturalOrder()));

    private static final Comparator<OppgittEgenNæringDto> COMP_OPPGITT_EGEN_NÆRING = Comparator
        .comparing((OppgittEgenNæringDto dto) -> dto.getVirksomhetTypeDto() == null ? null : dto.getVirksomhetTypeDto().getKode(),
            Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getFom(), Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getPeriode().getTom(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getVirksomhet() == null ? null : dto.getVirksomhet().getIdent(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(dto -> dto.getLandkode() == null ? null : dto.getLandkode().getKode(), Comparator.nullsLast(Comparator.naturalOrder()))
        .thenComparing(OppgittEgenNæringDto::getVirksomhetNavn, Comparator.nullsLast(Comparator.naturalOrder()));

    public OppgittOpptjeningerDto mapTilDto(Collection<OppgittOpptjening> oppgittOpptjeninger) {
        return new OppgittOpptjeningerDto()
            .medOppgitteOpptjeninger(oppgittOpptjeninger.stream().map(this::mapTilDto).collect(Collectors.toList()));
    }

    public OppgittOpptjeningDto mapTilDto(OppgittOpptjening oppgittOpptjening) {
        return new MapTilDto().map(oppgittOpptjening);
    }

    public OppgittOpptjeningBuilder mapFraDto(OppgittOpptjeningDto oppgittOpptjening) {
        return new MapFraDto().map(oppgittOpptjening);
    }

    private class MapTilDto {

        public OppgittOpptjeningDto map(OppgittOpptjening oppgittOpptjening) {
            if (oppgittOpptjening == null)
                return null;

            var dto = new OppgittOpptjeningDto(oppgittOpptjening.getEksternReferanse(), oppgittOpptjening.getOpprettetTidspunkt());

            Optional.ofNullable(oppgittOpptjening.getJournalpostId())
                .ifPresent(jp -> dto.setJournalpostId(new JournalpostId(jp.getVerdi())));
            Optional.ofNullable(oppgittOpptjening.getInnsendingstidspunkt())
                .ifPresent(tidspunkt -> dto.setInnsendingstidspunkt(tidspunkt.atZone(ZoneId.of("Europe/Oslo")).toOffsetDateTime()));
            dto.medArbeidsforhold(oppgittOpptjening.getOppgittArbeidsforhold().stream()
                .map(oa -> this.mapArbeidsforhold(oa)).sorted(COMP_OPPGITT_ARBEIDSFORHOLD).collect(Collectors.toList()));
            dto.medEgenNæring(oppgittOpptjening.getEgenNæring().stream()
                .map(this::mapEgenNæring).sorted(COMP_OPPGITT_EGEN_NÆRING).collect(Collectors.toList()));
            dto.medAnnenAktivitet(oppgittOpptjening.getAnnenAktivitet().stream()
                .map(this::mapAnnenAktivitet).sorted(COMP_ANNEN_AKTIVITET).collect(Collectors.toList()));

            oppgittOpptjening.getFrilans().ifPresent(f -> dto.medFrilans(mapFrilans(f)));

            return dto;
        }

        private OppgittFrilansDto mapFrilans(OppgittFrilans frilans) {
            if (frilans == null)
                return null;

            var frilansoppdrag = frilans.getFrilansoppdrag().stream().map(this::mapFrilansoppdrag).sorted(COMP_FRILANSOPPDRAG).collect(Collectors.toList());
            var frilansDto = new OppgittFrilansDto(frilansoppdrag)
                .medErNyoppstartet(frilans.getErNyoppstartet())
                .medHarInntektFraFosterhjem(frilans.getHarInntektFraFosterhjem())
                .medHarNærRelasjon(frilans.getHarNærRelasjon());
            return frilansDto;
        }

        private OppgittArbeidsforholdDto mapArbeidsforhold(OppgittArbeidsforhold arbeidsforhold) {
            if (arbeidsforhold == null)
                return null;

            IntervallEntitet periode1 = arbeidsforhold.getPeriode();
            var periode = new Periode(periode1.getFomDato(), periode1.getTomDato());
            var arbeidType = arbeidsforhold.getArbeidType();

            var dto = new OppgittArbeidsforholdDto(periode, arbeidType)
                .medErUtenlandskInntekt(arbeidsforhold.erUtenlandskInntekt());

            Landkode landkode = arbeidsforhold.getLandkode();
            var land = landkode == null || landkode.getKode() == null ? Landkode.NOR : landkode;

            var virksomhet = arbeidsforhold.getUtenlandskVirksomhetNavn();
            if (virksomhet != null) {
                dto.medOppgittVirksomhetNavn(virksomhet, land);
            } else {
                dto.setLandkode(land);
            }

            if (arbeidsforhold.getInntekt() != null) {
                dto.setInntekt(arbeidsforhold.getInntekt());
            }

            return dto;
        }

        private OppgittEgenNæringDto mapEgenNæring(OppgittEgenNæring egenNæring) {
            if (egenNæring == null)
                return null;

            IntervallEntitet periode1 = egenNæring.getPeriode();
            var periode = new Periode(periode1.getFomDato(), periode1.getTomDato());

            var org = egenNæring.getOrgnummer() == null ? null : new Organisasjon(egenNæring.getOrgnummer().getId());
            var virksomhetType = egenNæring.getVirksomhetType().getKode();

            var dto = new OppgittEgenNæringDto(periode)
                .medBegrunnelse(egenNæring.getBegrunnelse())
                .medBruttoInntekt(egenNæring.getBruttoInntekt())
                .medEndringDato(egenNæring.getEndringDato())
                .medNyIArbeidslivet(egenNæring.getNyIArbeidslivet())
                .medNyoppstartet(egenNæring.getNyoppstartet())
                .medNærRelasjon(egenNæring.getNærRelasjon())
                .medRegnskapsførerNavn(egenNæring.getRegnskapsførerNavn())
                .medRegnskapsførerTlf(egenNæring.getRegnskapsførerTlf())
                .medVarigEndring(egenNæring.getVarigEndring())
                .medVirksomhet(org)
                .medVirksomhetType(virksomhetType);

            var virksomhet = egenNæring.getUtenlandskVirksomhetNavn();
            Landkode landkode = egenNæring.getLandkode();

            var land = landkode == null || landkode.getKode() == null ? Landkode.NOR : landkode;
            if (virksomhet != null) {
                dto.medOppgittVirksomhetNavn(virksomhet, land);
            } else {
                dto.setLandkode(land);
            }
            return dto;
        }

        private OppgittFrilansoppdragDto mapFrilansoppdrag(OppgittFrilansoppdrag frilansoppdrag) {
            if (frilansoppdrag == null)
                return null;

            var periode = new Periode(frilansoppdrag.getPeriode().getFomDato(), frilansoppdrag.getPeriode().getTomDato());
            var oppdragsgiver = frilansoppdrag.getOppdragsgiver();
            OppgittFrilansoppdragDto oppgittFrilansoppdragDto = new OppgittFrilansoppdragDto(periode, oppdragsgiver);
            if (frilansoppdrag.getInntekt() != null) {
                return oppgittFrilansoppdragDto.medInntekt(frilansoppdrag.getInntekt());
            }
            return oppgittFrilansoppdragDto;
        }

        private OppgittAnnenAktivitetDto mapAnnenAktivitet(OppgittAnnenAktivitet annenAktivitet) {
            if (annenAktivitet == null)
                return null;

            var periode = new Periode(annenAktivitet.getPeriode().getFomDato(), annenAktivitet.getPeriode().getTomDato());
            var arbeidType = annenAktivitet.getArbeidType();
            return new OppgittAnnenAktivitetDto(periode, arbeidType);
        }
    }

    private class MapFraDto {

        public OppgittOpptjeningBuilder map(OppgittOpptjeningDto dto) {
            if (dto == null)
                return null;

            var oppgittOpptjeningEksternReferanse = UUID.fromString(dto.getEksternReferanse().getReferanse());
            var builder = OppgittOpptjeningBuilder.ny(oppgittOpptjeningEksternReferanse, dto.getOpprettetTidspunkt());

            var annenAktivitet = mapEach(dto.getAnnenAktivitet(), this::mapAnnenAktivitet);
            annenAktivitet.forEach(builder::leggTilAnnenAktivitet);

            var arbeidsforhold = mapEach(dto.getArbeidsforhold(), this::mapOppgittArbeidsforhold);
            arbeidsforhold.forEach(builder::leggTilOppgittArbeidsforhold);

            var egenNæring = mapEach(dto.getEgenNæring(), this::mapEgenNæring);
            builder.leggTilEgneNæringer(egenNæring);

            var frilans = mapFrilans(dto.getFrilans());
            builder.leggTilFrilansOpplysninger(frilans);

            if (dto.getJournalpostId() != null) {
                builder.medJournalpostId(new no.nav.foreldrepenger.abakus.typer.JournalpostId(dto.getJournalpostId().getId()));
            }
            if (dto.getInnsendingstidspunkt() != null) {
                builder.medInnsendingstidspuntk(dto.getInnsendingstidspunkt().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
            }

            return builder;
        }

        private <V, R> List<R> mapEach(List<V> data, Function<V, R> transform) {
            if (data == null) {
                return Collections.emptyList();
            }
            return data.stream().map(transform).collect(Collectors.toList());
        }

        private OppgittFrilans mapFrilans(OppgittFrilansDto dto) {
            if (dto == null)
                return null;

            var frilans = new OppgittFrilans();

            frilans.setErNyoppstartet(dto.isErNyoppstartet());
            frilans.setHarNærRelasjon(dto.isHarNærRelasjon());
            frilans.setHarInntektFraFosterhjem(dto.isHarInntektFraFosterhjem());

            var frilansoppdrag = mapEach(dto.getFrilansoppdrag(),
                f -> new OppgittFrilansoppdrag(f.getOppdragsgiver(),
                    IntervallEntitet.fraOgMedTilOgMed(f.getPeriode().getFom(), f.getPeriode().getTom()), f.getInntekt()));
            frilans.setFrilansoppdrag(frilansoppdrag);
            return frilans;
        }

        private EgenNæringBuilder mapEgenNæring(OppgittEgenNæringDto dto) {
            if (dto == null)
                return null;

            var builder = EgenNæringBuilder.ny();

            var org = dto.getVirksomhet() == null ? null : new OrgNummer(dto.getVirksomhet().getIdent());
            var periode = dto.getPeriode();
            builder
                .medBegrunnelse(dto.getBegrunnelse())
                .medBruttoInntekt(dto.getBruttoInntekt())
                .medEndringDato(dto.getEndringDato())
                .medVirksomhet(org)
                .medVirksomhetType(dto.getVirksomhetTypeDto())
                .medRegnskapsførerNavn(trimVedBehov(dto.getRegnskapsførerNavn()))
                .medRegnskapsførerTlf(dto.getRegnskapsførerTlf())
                .medNyIArbeidslivet(dto.isNyIArbeidslivet())
                .medNyoppstartet(dto.isNyoppstartet())
                .medNærRelasjon(dto.isNærRelasjon())
                .medVarigEndring(dto.isVarigEndring())
                .medPeriode(IntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom()));

            Landkode landkode = mapLandkoder(dto.getLandkode());
            builder.medUtenlandskVirksomhet(landkode, dto.getVirksomhetNavn());

            return builder;
        }

        //fix for TSF-798
        // regnskapsførerNavn oppgitt med 566 tegn...
        private String trimVedBehov(String regnskapsførerNavn) {
            if (regnskapsførerNavn != null && regnskapsførerNavn.length() > 400) {
                return regnskapsførerNavn.substring(0, 399);
            }
            return regnskapsførerNavn;
        }

        private OppgittArbeidsforholdBuilder mapOppgittArbeidsforhold(OppgittArbeidsforholdDto dto) {
            if (dto == null)
                return null;

            Periode dto1 = dto.getPeriode();
            var builder = OppgittArbeidsforholdBuilder.ny()
                .medArbeidType(dto.getArbeidTypeDto())
                .medErUtenlandskInntekt(dto.isErUtenlandskInntekt())
                .medInnteket(dto.getInntekt())
                .medPeriode(IntervallEntitet.fraOgMedTilOgMed(dto1.getFom(), dto1.getTom()));

            Landkode landkode = mapLandkoder(dto.getLandkode());
            builder.medUtenlandskVirksomhet(landkode, dto.getVirksomhetNavn());

            return builder;
        }

        private OppgittAnnenAktivitet mapAnnenAktivitet(OppgittAnnenAktivitetDto dto) {
            if (dto == null)
                return null;

            Periode dto1 = dto.getPeriode();
            var periode = IntervallEntitet.fraOgMedTilOgMed(dto1.getFom(), dto1.getTom());
            var arbeidType = dto.getArbeidTypeDto();
            return new OppgittAnnenAktivitet(periode, arbeidType);
        }

        private Landkode mapLandkoder(Landkode landkode) {
            return landkode == null || landkode.getKode() == null ? Landkode.NOR : landkode;
        }

    }

}
