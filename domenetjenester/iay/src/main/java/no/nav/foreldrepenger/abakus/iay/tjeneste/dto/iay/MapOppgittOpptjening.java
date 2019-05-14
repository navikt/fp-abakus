package no.nav.foreldrepenger.abakus.iay.tjeneste.dto.iay;

import java.util.stream.Collectors;

import org.jboss.weld.exceptions.UnsupportedOperationException;

import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.AnnenAktivitet;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.EgenNæring;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.Frilans;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.Frilansoppdrag;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittArbeidsforhold;
import no.nav.foreldrepenger.abakus.domene.iay.søknad.grunnlag.OppgittOpptjening;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Organisasjon;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.Periode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.ArbeidType;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.kodeverk.Landkode;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittAnnenAktivitetDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittArbeidsforholdDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittEgenNæringDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittFrilansoppdragDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittOpptjeningDto;
import no.nav.foreldrepenger.kontrakter.iaygrunnlag.oppgittopptjening.v1.OppgittUtenlandskVirksomhetDto;

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
            var periode = new Periode(arbeidsforhold.getPeriode().getFomDato(), arbeidsforhold.getPeriode().getTomDato());
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

        private OppgittEgenNæringDto mapEgenNæring(EgenNæring egenNæring) {
            var periode = new Periode(egenNæring.getPeriode().getFomDato(), egenNæring.getPeriode().getTomDato());
            OppgittUtenlandskVirksomhetDto oppgittUtenlandskVirksomhet = new OppgittUtenlandskVirksomhetDto(Landkode.SVERIGE, "DuGamleDuFria");
            var org = egenNæring.getOrgnummer() == null ? null : new Organisasjon(egenNæring.getOrgnummer().getId());
            String virksomhetType = egenNæring.getVirksomhetType().getKode();
            return new OppgittEgenNæringDto(periode)
                .medBegrunnelse(egenNæring.getBegrunnelse())
                .medBruttoInntekt(egenNæring.getBruttoInntekt())
                .medEndringDato(egenNæring.getEndringDato())
                .medNyIArbeidslivet(egenNæring.getNyIArbeidslivet())
                .medNyoppstartet(egenNæring.getNyoppstartet())
                .medNærRelasjon(egenNæring.getNærRelasjon())
                .medOppgittUtenlandskVirksomhet(oppgittUtenlandskVirksomhet)
                .medRegnskapsførerNavn(egenNæring.getRegnskapsførerNavn())
                .medRegnskapsførerTlf(egenNæring.getRegnskapsførerTlf())
                .medVarigEndring(egenNæring.getVarigEndring())
                .medVirksomhet(org)
                .medVirksomhetType(virksomhetType);
        }

        private OppgittFrilansoppdragDto mapFrilansoppdrag(Frilansoppdrag frilansoppdrag) {
            var periode = new Periode(frilansoppdrag.getPeriode().getFomDato(), frilansoppdrag.getPeriode().getTomDato());
            var oppdragsgiver = frilansoppdrag.getOppdragsgiver();
            return new OppgittFrilansoppdragDto(periode, oppdragsgiver);
        }

        private OppgittAnnenAktivitetDto mapAnnenAktivitet(AnnenAktivitet annenAktivitet) {
            var periode = new Periode(annenAktivitet.getPeriode().getFomDato(), annenAktivitet.getPeriode().getTomDato());
            var arbeidType = new ArbeidType(annenAktivitet.getArbeidType().getKode());
            return new OppgittAnnenAktivitetDto(periode, arbeidType);
        }
    }

    private class MapFraDto {

        public OppgittOpptjening map(OppgittOpptjeningDto oppgittOpptjening) {
            // FIXME Map OppgittOpptjening på vei inn
            throw new UnsupportedOperationException("Not Yet Implemented");
        }
    }

    public OppgittOpptjeningDto mapTilDto(OppgittOpptjening oppgittOpptjening) {
        return new MapTilDto().map(oppgittOpptjening);
    }

    public OppgittOpptjening mapFraDto(OppgittOpptjeningDto oppgittOpptjening) {
        return new MapFraDto().map(oppgittOpptjening);
    }

}
