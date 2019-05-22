package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.AnnenAktivitetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.FrilansEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.FrilansoppdragEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.UtenlandskVirksomhetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.Frilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.Frilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.kodeverk.VirksomhetType;
import no.nav.foreldrepenger.abakus.kodeverk.Landkoder;
import no.nav.foreldrepenger.abakus.typer.OrgNummer;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittAnnenAktivitetDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittArbeidsforholdDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittEgenNæringDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansoppdragDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittUtenlandskVirksomhetDto;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;

public class MapOppgittOpptjening {

    private class MapTilDto {

        public OppgittOpptjeningDto map(OppgittOpptjening oppgittOpptjening) {
            var dto = new OppgittOpptjeningDto();

            dto.medArbeidsforhold(oppgittOpptjening.getOppgittArbeidsforhold().stream().map(this::mapArbeidsforhold).collect(Collectors.toList()));

            dto.medEgenNæring(oppgittOpptjening.getEgenNæring().stream().map(this::mapEgenNæring).collect(Collectors.toList()));

            if (oppgittOpptjening.getFrilans().isPresent()) {
                var frilans = oppgittOpptjening.getFrilans().get();
                var frilansDto = mapFrilans(frilans);
                dto.medFrilans(frilansDto);
            }

            dto.medAnnenAktivitet(oppgittOpptjening.getAnnenAktivitet().stream().map(this::mapAnnenAktivitet).collect(Collectors.toList()));
            return dto;
        }

        private OppgittFrilansDto mapFrilans(Frilans frilans) {
            var frilansoppdrag = frilans.getFrilansoppdrag().stream().map(this::mapFrilansoppdrag).collect(Collectors.toList());
            var frilansDto = new OppgittFrilansDto(frilansoppdrag)
                .medErNyoppstartet(frilans.getErNyoppstartet())
                .medHarInntektFraFosterhjem(frilans.getHarInntektFraFosterhjem())
                .medHarNærRelasjon(frilans.getHarNærRelasjon());
            return frilansDto;
        }

        private OppgittArbeidsforholdDto mapArbeidsforhold(OppgittArbeidsforhold arbeidsforhold) {
            var periode = tilPeriode(arbeidsforhold.getPeriode());
            var arbeidType = new ArbeidType(arbeidsforhold.getArbeidType().getKode());

            var dto = new OppgittArbeidsforholdDto(periode, arbeidType)
                .medErUtenlandskInntekt(arbeidsforhold.erUtenlandskInntekt());

            if (arbeidsforhold.getUtenlandskVirksomhet() != null) {
                var utvirk = arbeidsforhold.getUtenlandskVirksomhet();
                dto.medUtenlandskVirksomhet(new OppgittUtenlandskVirksomhetDto(utvirk.getLandkode().getKode(), utvirk.getUtenlandskVirksomhetNavn()));
            }

            return dto;
        }

        private OppgittEgenNæringDto mapEgenNæring(EgenNæring egenNæring) {
            var periode = tilPeriode(egenNæring.getPeriode());

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

            var utenlandskVirksomhet = egenNæring.getUtenlandskVirksomhet();
            if (utenlandskVirksomhet != null) {
                var oppgittUtenlandskVirksomhet = new OppgittUtenlandskVirksomhetDto(
                    utenlandskVirksomhet.getLandkode().getKode(),
                    utenlandskVirksomhet.getUtenlandskVirksomhetNavn());
                dto.medOppgittUtenlandskVirksomhet(oppgittUtenlandskVirksomhet);
            }
            return dto;
        }

        private OppgittFrilansoppdragDto mapFrilansoppdrag(Frilansoppdrag frilansoppdrag) {
            var periode = tilPeriode(frilansoppdrag.getPeriode());
            var oppdragsgiver = frilansoppdrag.getOppdragsgiver();
            return new OppgittFrilansoppdragDto(periode, oppdragsgiver);
        }

        private OppgittAnnenAktivitetDto mapAnnenAktivitet(AnnenAktivitet annenAktivitet) {
            var periode = tilPeriode(annenAktivitet.getPeriode());
            var arbeidType = new ArbeidType(annenAktivitet.getArbeidType().getKode());
            return new OppgittAnnenAktivitetDto(periode, arbeidType);
        }

        private Periode tilPeriode(DatoIntervallEntitet periode) {
            return new Periode(periode.getFomDato(), periode.getTomDato());
        }

    }

    private class MapFraDto {

        public OppgittOpptjeningBuilder map(OppgittOpptjeningDto oppgittOpptjening) {
            var oppgittOpptjeningEksternReferanse = UUID.fromString(oppgittOpptjening.getEksternReferanse().getReferanse());
            var builder = OppgittOpptjeningBuilder.ny(oppgittOpptjeningEksternReferanse);

            var annenAktivitet = mapEach(oppgittOpptjening.getAnnenAktivitet(), this::mapAnnenAktivitet);
            annenAktivitet.forEach(builder::leggTilAnnenAktivitet);

            var arbeidsforhold = mapEach(oppgittOpptjening.getArbeidsforhold(), this::mapOppgittArbeidsforhold);
            arbeidsforhold.forEach(builder::leggTilOppgittArbeidsforhold);

            var egenNæring = mapEach(oppgittOpptjening.getEgenNæring(), this::mapEgenNæring);
            builder.leggTilEgneNæringer(egenNæring);

            var frilans = mapFrilans(oppgittOpptjening.getFrilans());
            builder.leggTilFrilansOpplysninger(frilans);

            return builder;
        }

        private <V, R> List<R> mapEach(List<V> data, Function<V, R> transform) {
            if (data == null) {
                return Collections.emptyList();
            }
            return data.stream().map(transform).collect(Collectors.toList());
        }

        private Frilans mapFrilans(OppgittFrilansDto dto) {
            var frilans = new FrilansEntitet();
            frilans.setErNyoppstartet(dto.isErNyoppstartet());
            frilans.setHarInntektFraFosterhjem(dto.isHarInntektFraFosterhjem());
            frilans.setHarNærRelasjon(dto.isHarNærRelasjon());
            var frilansoppdrag = mapEach(dto.getFrilansoppdrag(), f -> new FrilansoppdragEntitet(f.getOppdragsgiver(), tilDatoIntervall(f.getPeriode())));
            frilans.setFrilansoppdrag(frilansoppdrag);
            return frilans;
        }

        private EgenNæringBuilder mapEgenNæring(OppgittEgenNæringDto dto) {
            var builder = EgenNæringBuilder.ny();

            var org = dto.getVirksomhet() == null ? null : new OrgNummer(dto.getVirksomhet().getIdent());

            builder
                .medBegrunnelse(dto.getBegrunnelse())
                .medBruttoInntekt(dto.getBruttoInntekt())
                .medEndringDato(dto.getEndringDato())
                .medUtenlandskVirksomhet(tilUtenlandskVirksomhet(dto.getOppgittUtenlandskVirksomhet()))
                .medVirksomhet(org)
                .medVirksomhetType(new VirksomhetType(dto.getVirksomhetTypeDto().getKode()))
                .medRegnskapsførerNavn(dto.getRegnskapsførerNavn())
                .medRegnskapsførerTlf(dto.getRegnskapsførerTlf())
                .medNyIArbeidslivet(dto.isNyIArbeidslivet())
                .medNyoppstartet(dto.isNyoppstartet())
                .medNærRelasjon(dto.isNærRelasjon())
                .medVarigEndring(dto.isVarigEndring());

            return builder;
        }

        private OppgittArbeidsforholdBuilder mapOppgittArbeidsforhold(OppgittArbeidsforholdDto dto) {
            var builder = OppgittArbeidsforholdBuilder.ny()
                .medArbeidType(tilArbeidtype(dto.getArbeidTypeDto()))
                .medErUtenlandskInntekt(dto.isErUtenlandskInntekt())
                .medPeriode(tilDatoIntervall(dto.getPeriode()))
                .medUtenlandskVirksomhet(tilUtenlandskVirksomhet(dto.getUtenlandskVirksomhet()));

            return builder;
        }

        private UtenlandskVirksomhetEntitet tilUtenlandskVirksomhet(OppgittUtenlandskVirksomhetDto utlandVirksomhet) {
            var landkode = new Landkoder(utlandVirksomhet.getLandkode().getKode());
            return new UtenlandskVirksomhetEntitet(landkode, utlandVirksomhet.getVirksomhetNavn());
        }

        private AnnenAktivitetEntitet mapAnnenAktivitet(OppgittAnnenAktivitetDto dto) {
            var periode = tilDatoIntervall(dto.getPeriode());
            var arbeidType = tilArbeidtype(dto.getArbeidTypeDto());
            return new AnnenAktivitetEntitet(periode, arbeidType);
        }

        private no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType tilArbeidtype(ArbeidType arbeidType) {
            return new no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType(arbeidType.getKode());
        }

        private DatoIntervallEntitet tilDatoIntervall(Periode dto) {
            return DatoIntervallEntitet.fraOgMedTilOgMed(dto.getFom(), dto.getTom());
        }

    }

    public OppgittOpptjeningDto mapTilDto(OppgittOpptjening oppgittOpptjening) {
        return new MapTilDto().map(oppgittOpptjening);
    }

    public OppgittOpptjeningBuilder mapFraDto(OppgittOpptjeningDto oppgittOpptjening) {
        return new MapFraDto().map(oppgittOpptjening);
    }

}
