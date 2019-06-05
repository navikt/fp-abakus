package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittAnnenAktivitetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittFrilansoppdragEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.EgenNæringBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittOpptjeningBuilder.OppgittArbeidsforholdBuilder;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.OppgittUtenlandskVirksomhetEntitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittAnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittEgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittFrilansoppdrag;
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
            var dto = new OppgittOpptjeningDto(oppgittOpptjening.getEksternReferanse(), oppgittOpptjening.getOpprettetTidspunkt());

            dto.medArbeidsforhold(oppgittOpptjening.getOppgittArbeidsforhold().stream().map(this::mapArbeidsforhold).collect(Collectors.toList()));
            dto.medEgenNæring(oppgittOpptjening.getEgenNæring().stream().map(this::mapEgenNæring).collect(Collectors.toList()));
            dto.medAnnenAktivitet(oppgittOpptjening.getAnnenAktivitet().stream().map(this::mapAnnenAktivitet).collect(Collectors.toList()));
            
            oppgittOpptjening.getFrilans().ifPresent(f -> dto.medFrilans(mapFrilans(f)));
            
            return dto;
        }

        private OppgittFrilansDto mapFrilans(OppgittFrilans frilans) {
            var frilansoppdrag = frilans.getFrilansoppdrag().stream().map(this::mapFrilansoppdrag).collect(Collectors.toList());
            var frilansDto = new OppgittFrilansDto(frilansoppdrag)
                .medErNyoppstartet(frilans.getErNyoppstartet())
                .medHarInntektFraFosterhjem(frilans.getHarInntektFraFosterhjem())
                .medHarNærRelasjon(frilans.getHarNærRelasjon());
            return frilansDto;
        }

        private OppgittArbeidsforholdDto mapArbeidsforhold(OppgittArbeidsforhold arbeidsforhold) {
            DatoIntervallEntitet periode1 = arbeidsforhold.getPeriode();
            var periode = new Periode(periode1.getFomDato(), periode1.getTomDato());
            var arbeidType = new ArbeidType(arbeidsforhold.getArbeidType().getKode());

            OppgittUtenlandskVirksomhetDto utenlandskVirksomhet = null;
            if (arbeidsforhold.getUtenlandskVirksomhet() != null) {
                var utvirk = arbeidsforhold.getUtenlandskVirksomhet();
                utenlandskVirksomhet = new OppgittUtenlandskVirksomhetDto(utvirk.getLandkode().getKode(), utvirk.getUtenlandskVirksomhetNavn());
            }
            var dto = new OppgittArbeidsforholdDto(periode, arbeidType)
                .medErUtenlandskInntekt(arbeidsforhold.erUtenlandskInntekt())
                .medUtenlandskVirksomhet(utenlandskVirksomhet);

            return dto;
        }

        private OppgittEgenNæringDto mapEgenNæring(OppgittEgenNæring egenNæring) {
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

            var utenlandskVirksomhet = egenNæring.getUtenlandskVirksomhet();
            if (utenlandskVirksomhet != null) {
                var oppgittUtenlandskVirksomhet = new OppgittUtenlandskVirksomhetDto(
                    utenlandskVirksomhet.getLandkode().getKode(),
                    utenlandskVirksomhet.getUtenlandskVirksomhetNavn());
                dto.medOppgittUtenlandskVirksomhet(oppgittUtenlandskVirksomhet);
            }
            return dto;
        }

        private OppgittFrilansoppdragDto mapFrilansoppdrag(OppgittFrilansoppdrag frilansoppdrag) {
            var periode = new Periode(frilansoppdrag.getPeriode().getFomDato(), frilansoppdrag.getPeriode().getTomDato());
            var oppdragsgiver = frilansoppdrag.getOppdragsgiver();
            return new OppgittFrilansoppdragDto(periode, oppdragsgiver);
        }

        private OppgittAnnenAktivitetDto mapAnnenAktivitet(OppgittAnnenAktivitet annenAktivitet) {
            var periode = new Periode(annenAktivitet.getPeriode().getFomDato(), annenAktivitet.getPeriode().getTomDato());
            var arbeidType = new ArbeidType(annenAktivitet.getArbeidType().getKode());
            return new OppgittAnnenAktivitetDto(periode, arbeidType);
        }

    }

    private class MapFraDto {

        public OppgittOpptjeningBuilder map(OppgittOpptjeningDto oppgittOpptjening) {
            var oppgittOpptjeningEksternReferanse = UUID.fromString(oppgittOpptjening.getEksternReferanse().getReferanse());
            var builder = OppgittOpptjeningBuilder.ny(oppgittOpptjeningEksternReferanse, oppgittOpptjening.getOpprettetTidspunkt());

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

        private OppgittFrilans mapFrilans(OppgittFrilansDto dto) {
            var frilans = new OppgittFrilansEntitet();
            frilans.setErNyoppstartet(dto.isErNyoppstartet());
            frilans.setHarInntektFraFosterhjem(dto.isHarInntektFraFosterhjem());
            frilans.setHarNærRelasjon(dto.isHarNærRelasjon());
            var frilansoppdrag = mapEach(dto.getFrilansoppdrag(), 
                f -> new OppgittFrilansoppdragEntitet(f.getOppdragsgiver(), DatoIntervallEntitet.fraOgMedTilOgMed(f.getPeriode().getFom(), f.getPeriode().getTom())));
            frilans.setFrilansoppdrag(frilansoppdrag);
            return frilans;
        }

        private EgenNæringBuilder mapEgenNæring(OppgittEgenNæringDto dto) {
            var builder = EgenNæringBuilder.ny();

            var org = dto.getVirksomhet() == null ? null : new OrgNummer(dto.getVirksomhet().getIdent());
            var periode = dto.getPeriode();
            
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
                .medVarigEndring(dto.isVarigEndring())
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(periode.getFom(), periode.getTom()));

            return builder;
        }

        private OppgittArbeidsforholdBuilder mapOppgittArbeidsforhold(OppgittArbeidsforholdDto dto) {
            Periode dto1 = dto.getPeriode();
            var builder = OppgittArbeidsforholdBuilder.ny()
                .medArbeidType(tilArbeidtype(dto.getArbeidTypeDto()))
                .medErUtenlandskInntekt(dto.isErUtenlandskInntekt())
                .medPeriode(DatoIntervallEntitet.fraOgMedTilOgMed(dto1.getFom(), dto1.getTom()))
                .medUtenlandskVirksomhet(tilUtenlandskVirksomhet(dto.getUtenlandskVirksomhet()));

            return builder;
        }

        private OppgittUtenlandskVirksomhetEntitet tilUtenlandskVirksomhet(OppgittUtenlandskVirksomhetDto utlandVirksomhet) {
            var landkode = new Landkoder(utlandVirksomhet.getLandkode().getKode());
            return new OppgittUtenlandskVirksomhetEntitet(landkode, utlandVirksomhet.getVirksomhetNavn());
        }

        private OppgittAnnenAktivitetEntitet mapAnnenAktivitet(OppgittAnnenAktivitetDto dto) {
            Periode dto1 = dto.getPeriode();
            var periode = DatoIntervallEntitet.fraOgMedTilOgMed(dto1.getFom(), dto1.getTom());
            var arbeidType = tilArbeidtype(dto.getArbeidTypeDto());
            return new OppgittAnnenAktivitetEntitet(periode, arbeidType);
        }

        private no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType tilArbeidtype(ArbeidType arbeidType) {
            return new no.nav.foreldrepenger.abakus.domene.iay.kodeverk.ArbeidType(arbeidType.getKode());
        }

    }

    public OppgittOpptjeningDto mapTilDto(OppgittOpptjening oppgittOpptjening) {
        return new MapTilDto().map(oppgittOpptjening);
    }

    public OppgittOpptjeningBuilder mapFraDto(OppgittOpptjeningDto oppgittOpptjening) {
        return new MapFraDto().map(oppgittOpptjening);
    }

}
