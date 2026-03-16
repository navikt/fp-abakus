package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import java.util.List;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.FagsystemDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.MeldekortUtbetalingsgrunnlagMeldekortDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.MeldekortUtbetalingsgrunnlagSakDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.YtelseStatusDto;
import no.nav.foreldrepenger.kontrakter.fpwsproxy.arena.respons.YtelseTypeDto;

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
            .medKravMottattDato(meldekortUtbetalingsgrunnlagSakDto.kravMottattDato())
            .medVedtattDato(meldekortUtbetalingsgrunnlagSakDto.vedtattDato())
            .medVedtaksPeriodeFom(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeFom())
            .medVedtaksPeriodeTom(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeTom())
            .medVedtaksDagsats(
                meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats() != null ? meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats().verdi() : null)
            .build();
    }

    static Fagsystem tilKilde(FagsystemDto kilde) {
        return switch (kilde) {
            case null -> null;
            case ARENA -> Fagsystem.ARENA;
            default -> throw new IllegalArgumentException("Ukjent fagsystem: " + kilde);
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
