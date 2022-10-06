package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import java.util.List;

import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagMeldekort;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSak;
import no.nav.foreldrepenger.abakus.typer.Saksnummer;

public class MedlemskortUtbetalingsgrunnlagSakMapper {

    private MedlemskortUtbetalingsgrunnlagSakMapper() {
        // Statisk implementasjon
    }

    public static MeldekortUtbetalingsgrunnlagSak tilDomeneModell(MeldekortUtbetalingsgrunnlagSakDto meldekortUtbetalingsgrunnlagSakDto) {
        return MeldekortUtbetalingsgrunnlagSak.MeldekortSakBuilder.ny()
            .leggTilMeldekort(tilMeldekort(meldekortUtbetalingsgrunnlagSakDto.meldekortene()))
            .medType(meldekortUtbetalingsgrunnlagSakDto.type())
            .medTilstand(meldekortUtbetalingsgrunnlagSakDto.tilstand())
            .medKilde(meldekortUtbetalingsgrunnlagSakDto.kilde())
            .medSaksnummer(meldekortUtbetalingsgrunnlagSakDto.saksnummer() != null ? new Saksnummer(meldekortUtbetalingsgrunnlagSakDto.saksnummer()) : null)
            .medSakStatus(meldekortUtbetalingsgrunnlagSakDto.sakStatus())
            .medVedtakStatus(meldekortUtbetalingsgrunnlagSakDto.vedtakStatus())
            .medKravMottattDato(meldekortUtbetalingsgrunnlagSakDto.kravMottattDato())
            .medVedtattDato(meldekortUtbetalingsgrunnlagSakDto.vedtattDato())
            .medVedtaksPeriodeFom(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeFom())
            .medVedtaksPeriodeTom(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeTom())
            .medVedtaksDagsats(meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats() != null ? meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats().verdi() : null)
            .build();
    }

    private static List<MeldekortUtbetalingsgrunnlagMeldekort> tilMeldekort(List<MeldekortUtbetalingsgrunnlagMeldekortDto> meldekortene) {
        return meldekortene.stream()
            .map(MedlemskortUtbetalingsgrunnlagSakMapper::tilMeldekortDomene)
            .toList();
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
