package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansoppdragEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittUtenlandskVirksomhetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.iay.InntektArbeidYtelseTjeneste;
import no.nav.foreldrepenger.abakus.kobling.KoblingReferanse;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Landkode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittAnnenAktivitetDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittArbeidsforholdDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittEgenNæringDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansoppdragDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapOppgittOpptjening {

    private InntektArbeidYtelseTjeneste iayTjeneste;

    public MapOppgittOpptjening(InntektArbeidYtelseTjeneste iayTjeneste) {
        this.iayTjeneste = iayTjeneste;
    }

    public OppgittOpptjeningDto mapTilDto(OppgittOpptjening oppgittOpptjening) {
        return new MapTilDto().map(oppgittOpptjening);
    }

    public OppgittOpptjeningBuilder mapFraDto(KoblingReferanse koblingReferanse, OppgittOpptjeningDto oppgittOpptjening) {
        return new MapFraDto().map(koblingReferanse, oppgittOpptjening);
    }

    private class MapTilDto {

        public OppgittOpptjeningDto map(OppgittOpptjening oppgittOpptjening) {
            if (oppgittOpptjening == null) return null;

            var dto = new OppgittOpptjeningDto(oppgittOpptjening.getEksternReferanse(), oppgittOpptjening.getOpprettetTidspunkt());

            dto.medArbeidsforhold(oppgittOpptjening.getOppgittArbeidsforhold().stream().map(this::mapArbeidsforhold).collect(Collectors.toList()));
            dto.medEgenNæring(oppgittOpptjening.getEgenNæring().stream().map(this::mapEgenNæring).collect(Collectors.toList()));
            dto.medAnnenAktivitet(oppgittOpptjening.getAnnenAktivitet().stream().map(this::mapAnnenAktivitet).collect(Collectors.toList()));

            oppgittOpptjening.getFrilans().ifPresent(f -> dto.medFrilans(mapFrilans(f)));

            return dto;
        }

        private OppgittFrilansDto mapFrilans(OppgittFrilans frilans) {
            if (frilans == null) return null;

            var frilansoppdrag = frilans.getFrilansoppdrag().stream().map(this::mapFrilansoppdrag).collect(Collectors.toList());
            var frilansDto = new OppgittFrilansDto(frilansoppdrag)
                .medErNyoppstartet(frilans.getErNyoppstartet())
                .medHarInntektFraFosterhjem(frilans.getHarInntektFraFosterhjem())
                .medHarNærRelasjon(frilans.getHarNærRelasjon());
            return frilansDto;
        }

        private OppgittArbeidsforholdDto mapArbeidsforhold(OppgittArbeidsforhold arbeidsforhold) {
            if (arbeidsforhold == null) return null;

            DatoIntervallEntitet periode1 = arbeidsforhold.getPeriode();
            var periode = new Periode(periode1.getFomDato(), periode1.getTomDato());
            var arbeidType = KodeverkMapper.mapArbeidTypeTilDto(arbeidsforhold.getArbeidType());

            var dto = new OppgittArbeidsforholdDto(periode, arbeidType)
                .medErUtenlandskInntekt(arbeidsforhold.erUtenlandskInntekt());

            var virksomhet = arbeidsforhold.getUtenlandskVirksomhet();
            if (virksomhet != null) {
                var landKode = new Landkode(virksomhet.getLandkode().getKode());
                dto.medOppgittVirksomhetNavn(virksomhet.getNavn(), landKode);
            }

            return dto;
        }

        private OppgittEgenNæringDto mapEgenNæring(OppgittEgenNæring egenNæring) {
            if (egenNæring == null) return null;

            DatoIntervallEntitet periode1 = egenNæring.getPeriode();
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

            var virksomhet = egenNæring.getUtenlandskVirksomhet();
            if (virksomhet != null) {
                var landKode = new Landkode(virksomhet.getLandkode().getKode());
                dto.medOppgittVirksomhetNavn(virksomhet.getNavn(), landKode);
            }
            return dto;
        }

        private OppgittFrilansoppdragDto mapFrilansoppdrag(OppgittFrilansoppdrag frilansoppdrag) {
            if (frilansoppdrag == null) return null;

            var periode = new Periode(frilansoppdrag.getPeriode().getFomDato(), frilansoppdrag.getPeriode().getTomDato());
            var oppdragsgiver = frilansoppdrag.getOppdragsgiver();
            return new OppgittFrilansoppdragDto(periode, oppdragsgiver);
        }

        private OppgittAnnenAktivitetDto mapAnnenAktivitet(OppgittAnnenAktivitet annenAktivitet) {
            if (annenAktivitet == null) return null;

            var periode = new Periode(annenAktivitet.getPeriode().getFomDato(), annenAktivitet.getPeriode().getTomDato());
            var arbeidType = KodeverkMapper.mapArbeidTypeTilDto(annenAktivitet.getArbeidType());
            return new OppgittAnnenAktivitetDto(periode, arbeidType);
        }

    }

    private class MapFraDto {

        MapFraDto() {
            Objects.requireNonNull(iayTjeneste, "iayTjeneste");
        }

        public OppgittOpptjeningBuilder map(KoblingReferanse koblingReferanse, OppgittOpptjeningDto dto) {
            if (dto == null) return null;

            var oppgittOpptjeningEksternReferanse = UUID.fromString(dto.getEksternReferanse().getReferanse());
            Optional<OppgittOpptjeningEntitet> oppgittOpptjening = iayTjeneste.hentOppgittOpptjeningFor(koblingReferanse, oppgittOpptjeningEksternReferanse);
            if (oppgittOpptjening.isPresent()) {
                return OppgittOpptjeningBuilder.eksisterende(oppgittOpptjening.get());
            }
            var builder = OppgittOpptjeningBuilder.ny(oppgittOpptjeningEksternReferanse, dto.getOpprettetTidspunkt());

            var annenAktivitet = mapEach(dto.getAnnenAktivitet(), this::mapAnnenAktivitet);
            annenAktivitet.forEach(builder::leggTilAnnenAktivitet);

            var arbeidsforhold = mapEach(dto.getArbeidsforhold(), this::mapOppgittArbeidsforhold);
            arbeidsforhold.forEach(builder::leggTilOppgittArbeidsforhold);

            var egenNæring = mapEach(dto.getEgenNæring(), this::mapEgenNæring);
            builder.leggTilEgneNæringer(egenNæring);

            var frilans = mapFrilans(dto.getFrilans());
            builder.leggTilFrilansOpplysninger(frilans);


            return builder;
        }

        private <V, R> List<R> mapEach(List<V> data, Function<V, R> transform) {
            if (data == null) {
                return Collections.emptyList();
            }
            return data.stream().map(transform).collect(Collectors.toList());
        }

        private OppgittFrilans mapFrilans(OppgittFrilansDto dto) {
            if (dto == null) return null;

            var frilans = new OppgittFrilansEntitet();

            frilans.setErNyoppstartet(dto.isErNyoppstartet());
            frilans.setHarNærRelasjon(dto.isHarNærRelasjon());
            frilans.setHarInntektFraFosterhjem(dto.isHarInntektFraFosterhjem());

            var frilansoppdrag = mapEach(dto.getFrilansoppdrag(),
                f -> new OppgittFrilansoppdragEntitet(f.getOppdragsgiver(), DatoIntervallEntitet.fraOgMedTilOgMed(f.getPeriode().getFom(), f.getPeriode().getTom())));
            frilans.setFrilansoppdrag(frilansoppdrag);
            return frilans;
        }

        private EgenNæringBuilder mapEgenNæring(OppgittEgenNæringDto dto) {
            if (dto == null) return null;

            var builder = EgenNæringBuilder.ny();

            var org = dto.getVirksomhet() == null ? null : new OrgNummer(dto.getVirksomhet().getIdent());
            var periode = dto.getPeriode();
            var virksomhet = tilUtenlandskVirksomhet(dto);
            builder
                .medBegrunnelse(dto.getBegrunnelse())
                .medBruttoInntekt(dto.getBruttoInntekt())
                .medEndringDato(dto.getEndringDato())
                .medUtenlandskVirksomhet(virksomhet)
                .medVirksomhet(org)
                .medVirksomhetType(KodeverkMapper.mapVirksomhetTypeFraDto(dto.getVirksomhetTypeDto()))
                .medRegnskapsførerNavn(dto.getRegnskapsførerNavn())
                .medRegnskapsførerTlf(dto.getRegnskapsførerTlf())
                .medNyIArbeidslivet(dto.isNyIArbeidslivet())
                .medNyoppstartet(dto.isNyoppstartet())
                .medNærRelasjon(dto.isNærRelasjon())
                .medVarigEndring(dto.isVarigEndring())
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom()));

            return builder;
        }

        private OppgittArbeidsforholdBuilder mapOppgittArbeidsforhold(OppgittArbeidsforholdDto dto) {
            if (dto == null) return null;

            var virksomhet = tilUtenlandskVirksomhet(dto);
            Periode dto1 = dto.getPeriode();
            var builder = OppgittArbeidsforholdBuilder.ny()
                .medArbeidType(KodeverkMapper.mapArbeidType(dto.getArbeidTypeDto()))
                .medErUtenlandskInntekt(dto.isErUtenlandskInntekt())
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(dto1.getFom(), dto1.getTom()))
                .medUtenlandskVirksomhet(virksomhet);

            return builder;
        }

        private OppgittUtenlandskVirksomhetEntitet tilUtenlandskVirksomhet(OppgittArbeidsforholdDto dto) {
            if (dto == null) return null;
            if (dto.getVirksomhetNavn() == null) return null;

            var landkode = new Landkoder(dto.getLandkode().getKode());
            return new OppgittUtenlandskVirksomhetEntitet(landkode, dto.getVirksomhetNavn());
        }

        private OppgittUtenlandskVirksomhetEntitet tilUtenlandskVirksomhet(OppgittEgenNæringDto dto) {
            if (dto == null) return null;
            if (dto.getVirksomhetNavn() == null) return null;

            var landkode = new Landkoder(dto.getLandkode().getKode());
            return new OppgittUtenlandskVirksomhetEntitet(landkode, dto.getVirksomhetNavn());
        }

        private OppgittAnnenAktivitetEntitet mapAnnenAktivitet(OppgittAnnenAktivitetDto dto) {
            if (dto == null) return null;

            Periode dto1 = dto.getPeriode();
            var periode = DatoIntervallEntitet.fraOgMedTilOgMed(dto1.getFom(), dto1.getTom());
            var arbeidType = KodeverkMapper.mapArbeidType(dto.getArbeidTypeDto());
            return new OppgittAnnenAktivitetEntitet(periode, arbeidType);
        }

    }

}
