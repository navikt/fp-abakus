package no.nav.foreldrepenger.abakus.vedtak.extract.v1;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.Inntektskategori;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.abakus.vedtak.ytelse.*;
import no.nav.abakus.vedtak.ytelse.v1.YtelseV1;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Anvisning;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.AnvistAndel;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.ArbeidsgiverIdent;
import no.nav.abakus.vedtak.ytelse.v1.anvisning.Inntektklasse;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;
import no.nav.foreldrepenger.abakus.vedtak.domene.Arbeidsgiver;
import no.nav.foreldrepenger.abakus.vedtak.domene.VedtakYtelse;
import no.nav.foreldrepenger.abakus.vedtak.domene.YtelseAnvist;

import java.util.List;
import java.util.stream.Collectors;

public final class ConvertToYtelseV1 {

    private ConvertToYtelseV1() {
    }

    public static Ytelse convert(VedtakYtelse vedtak) {
        var ytelse = new YtelseV1();
        var aktør = new Aktør();
        aktør.setVerdi(vedtak.getAktør().getId());
        ytelse.setAktør(aktør);
        ytelse.setVedtattTidspunkt(vedtak.getVedtattTidspunkt());
        ytelse.setYtelse(mapYtelser(vedtak.getYtelseType()));
        ytelse.setSaksnummer(vedtak.getSaksnummer().getVerdi());
        ytelse.setVedtakReferanse(vedtak.getVedtakReferanse().toString());
        ytelse.setYtelseStatus(mapStatus(vedtak.getStatus()));
        ytelse.setKildesystem(mapKildesystem(vedtak.getKilde()));
        ytelse.setTilleggsopplysninger(vedtak.getTilleggsopplysninger());
        var periode = new Periode();
        periode.setFom(vedtak.getPeriode().getFomDato());
        periode.setTom(vedtak.getPeriode().getTomDato());
        ytelse.setPeriode(periode);
        var anvist = vedtak.getYtelseAnvist().stream().map(ConvertToYtelseV1::mapLagretAnvist).collect(Collectors.toList());
        ytelse.setAnvist(anvist);
        return ytelse;
    }


    private static Anvisning mapLagretAnvist(YtelseAnvist anvist) {
        var anvisning = new Anvisning();
        var periode = new Periode();
        periode.setFom(anvist.getAnvistFom());
        periode.setTom(anvist.getAnvistTom());
        anvisning.setPeriode(periode);
        anvist.getBeløp().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setBeløp);
        anvist.getDagsats().map(Beløp::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setDagsats);
        anvist.getUtbetalingsgradProsent().map(Stillingsprosent::getVerdi).map(Desimaltall::new).ifPresent(anvisning::setUtbetalingsgrad);
        anvisning.setAndeler(mapAndeler(anvist));

        return anvisning;
    }

    private static List<AnvistAndel> mapAndeler(YtelseAnvist anvist) {
        return anvist.getAndeler()
            .stream()
            .map(a -> new AnvistAndel(a.getArbeidsgiver().map(ConvertToYtelseV1::mapArbeidsgiverIdent).orElse(null), a.getArbeidsforholdId(),
                new Desimaltall(a.getDagsats().getVerdi()),
                a.getUtbetalingsgradProsent() == null ? null : new Desimaltall(a.getUtbetalingsgradProsent().getVerdi()),
                a.getRefusjonsgradProsent() == null ? null : new Desimaltall(a.getRefusjonsgradProsent().getVerdi()),
                fraInntektskategori(a.getInntektskategori())))
            .collect(Collectors.toList());
    }


    private static ArbeidsgiverIdent mapArbeidsgiverIdent(Arbeidsgiver arbeidsgiver) {
        if (arbeidsgiver == null) {
            return null;
        }
        return new ArbeidsgiverIdent(arbeidsgiver.getIdentifikator());
    }

    public static Kildesystem mapKildesystem(Fagsystem fagsystem) {
        return switch (fagsystem) {
            case FPSAK -> Kildesystem.FPSAK;
            case K9SAK, INFOTRYGD -> Kildesystem.K9SAK;
            default -> null;
        };
    }

    public static Status mapStatus(YtelseStatus ytelseStatus) {
        return switch (ytelseStatus) {
            case OPPRETTET, UNDER_BEHANDLING -> Status.UNDER_BEHANDLING;
            case LØPENDE -> Status.LØPENDE;
            case AVSLUTTET -> Status.AVSLUTTET;
            default -> Status.UKJENT;
        };
    }

    public static Ytelser mapYtelser(YtelseType kodeverk) {
        return switch (kodeverk) {
            case PLEIEPENGER_SYKT_BARN -> Ytelser.PLEIEPENGER_SYKT_BARN;
            case PLEIEPENGER_NÆRSTÅENDE -> Ytelser.PLEIEPENGER_NÆRSTÅENDE;
            case OMSORGSPENGER -> Ytelser.OMSORGSPENGER;
            case OPPLÆRINGSPENGER -> Ytelser.OPPLÆRINGSPENGER;

            case ENGANGSTØNAD -> Ytelser.ENGANGSTØNAD;
            case FORELDREPENGER -> Ytelser.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> Ytelser.SVANGERSKAPSPENGER;

            case FRISINN -> Ytelser.FRISINN;
            default -> null;
        };
    }

    public static Inntektklasse fraInntektskategori(Inntektskategori inntektskategori) {
        return switch (inntektskategori) {
            case ARBEIDSTAKER -> Inntektklasse.ARBEIDSTAKER;
            case ARBEIDSTAKER_UTEN_FERIEPENGER -> Inntektklasse.ARBEIDSTAKER_UTEN_FERIEPENGER;
            case FRILANSER -> Inntektklasse.FRILANSER;
            case SELVSTENDIG_NÆRINGSDRIVENDE -> Inntektklasse.SELVSTENDIG_NÆRINGSDRIVENDE;
            case DAGPENGER -> Inntektklasse.DAGPENGER;
            case ARBEIDSAVKLARINGSPENGER -> Inntektklasse.ARBEIDSAVKLARINGSPENGER;
            case SJØMANN -> Inntektklasse.MARITIM;
            case DAGMAMMA -> Inntektklasse.DAGMAMMA;
            case JORDBRUKER -> Inntektklasse.JORDBRUKER;
            case FISKER -> Inntektklasse.FISKER;
            default -> Inntektklasse.INGEN;
        };
    }
}
