package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.*;

import java.util.List;

public class MedlemskortUtbetalingsgrunnlagSakMapper {

    private MedlemskortUtbetalingsgrunnlagSakMapper() {
        // Statisk implementasjon
    }

    public static MeldekortUtbetalingsgrunnlagSak tilDomeneModell(MeldekortUtbetalingsgrunnlagSakDto meldekortUtbetalingsgrunnlagSakDto) {
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(tilMeldekort(meldekortUtbetalingsgrunnlagSakDto.meldekortene()))
            .medType(tilType(meldekortUtbetalingsgrunnlagSakDto.type()))
            .medTilstand(tilTilstand(meldekortUtbetalingsgrunnlagSakDto.tilstand()))
            .medKilde(tilKilde(meldekortUtbetalingsgrunnlagSakDto.kilde()))
            .medSaksnummer(
                meldekortUtbetalingsgrunnlagSakDto.saksnummer() != null ? new Saksnummer(meldekortUtbetalingsgrunnlagSakDto.saksnummer()) : null)
            .medSakStatus(meldekortUtbetalingsgrunnlagSakDto.sakStatus())
            .medVedtakStatus(meldekortUtbetalingsgrunnlagSakDto.vedtakStatus())
            .medKravMottattDato(meldekortUtbetalingsgrunnlagSakDto.kravMottattDato())
            .medVedtattDato(meldekortUtbetalingsgrunnlagSakDto.vedtattDato())
            .medVedtaksPeriodeFom(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeFom())
            .medVedtaksPeriodeTom(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeTom())
            .medVedtaksDagsats(
                meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats() != null ? meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats().verdi() : null)
            .build();
    }

    static Fagsystem tilKilde(FagsystemDto kilde) {
        if (kilde == null) {
            return null;
        }
        return switch (kilde) {
            case BISYS -> Fagsystem.BISYS;
            case BIDRAGINNKREVING -> Fagsystem.BIDRAGINNKREVING;
            case FPSAK -> Fagsystem.FPSAK;
            case FPABAKUS -> Fagsystem.FPABAKUS;
            case K9SAK -> Fagsystem.K9SAK;
            case VLSP -> Fagsystem.VLSP;
            case TPS -> Fagsystem.TPS;
            case JOARK -> Fagsystem.JOARK;
            case INFOTRYGD -> Fagsystem.INFOTRYGD;
            case ARENA -> Fagsystem.ARENA;
            case INNTEKT -> Fagsystem.INNTEKT;
            case MEDL -> Fagsystem.MEDL;
            case GOSYS -> Fagsystem.GOSYS;
            case GRISEN -> Fagsystem.GRISEN;
            case GSAK -> Fagsystem.GSAK;
            case HJE_HEL_ORT -> Fagsystem.HJE_HEL_ORT;
            case ENHETSREGISTERET -> Fagsystem.ENHETSREGISTERET;
            case AAREGISTERET -> Fagsystem.AAREGISTERET;
            case PESYS -> Fagsystem.PESYS;
            case SKANNING -> Fagsystem.SKANNING;
            case VENTELONN -> Fagsystem.VENTELONN;
            case UNNTAK -> Fagsystem.UNNTAK;
            case ØKONOMI -> Fagsystem.ØKONOMI;
            case ØVRIG -> Fagsystem.ØVRIG;
        };
    }

    static YtelseStatus tilTilstand(YtelseStatusDto tilstand) {
        if (tilstand == null) {
            return null;
        }
        return switch (tilstand) {
            case OPPR -> YtelseStatus.OPPRETTET;
            case UBEH -> YtelseStatus.UNDER_BEHANDLING;
            case LOP -> YtelseStatus.LØPENDE;
            case AVSLU -> YtelseStatus.AVSLUTTET;
        };
    }

    static YtelseType tilType(YtelseTypeDto type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case DAG -> YtelseType.DAGPENGER;
            case AAP -> YtelseType.ARBEIDSAVKLARINGSPENGER;
        };
    }

    private static List<MeldekortUtbetalingsgrunnlagMeldekort> tilMeldekort(List<MeldekortUtbetalingsgrunnlagMeldekortDto> meldekortene) {
        return meldekortene.stream().map(MedlemskortUtbetalingsgrunnlagSakMapper::tilMeldekortDomene).toList();
    }

    private static MeldekortUtbetalingsgrunnlagMeldekort tilMeldekortDomene(MeldekortUtbetalingsgrunnlagMeldekortDto meldekortUtbetalingsgrunnlagMeldekortDto) {
        return MeldekortUtbetalingsgrunnlagMeldekort.MeldekortMeldekortBuilder.ny()
            .medMeldekortFom(meldekortUtbetalingsgrunnlagMeldekortDto.meldekortFom())
            .medMeldekortTom(meldekortUtbetalingsgrunnlagMeldekortDto.meldekortTom())
            .medDagsats(meldekortUtbetalingsgrunnlagMeldekortDto.dagsats())
            .medBeløp(meldekortUtbetalingsgrunnlagMeldekortDto.beløp())
            .medUtbetalingsgrad(meldekortUtbetalingsgrunnlagMeldekortDto.utbetalingsgrad())
            .build();
    }
}
