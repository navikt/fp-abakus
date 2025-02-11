package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena;

import org.junit.jupiter.api.Test;

import static no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MedlemskortUtbetalingsgrunnlagSakMapper.tilKilde;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MedlemskortUtbetalingsgrunnlagSakMapper.tilTilstand;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MedlemskortUtbetalingsgrunnlagSakMapper.tilType;
import static no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.MeldekortUtbetalingsgrunnlagSakDtoSeraliseringOgDeseraliseringTest.getMeldekortUtbetalingsgrunnlagSakDto;
import static org.assertj.core.api.Assertions.assertThat;

class MedlemskortUtbetalingsgrunnlagSakMapperTest {

    @Test
    void testMappingKonsistens() {
        var meldekortUtbetalingsgrunnlagSakDto = getMeldekortUtbetalingsgrunnlagSakDto();
        var domeneobjekt = MedlemskortUtbetalingsgrunnlagSakMapper.tilDomeneModell(meldekortUtbetalingsgrunnlagSakDto);

        assertThat(meldekortUtbetalingsgrunnlagSakDto.meldekortene()).hasSameSizeAs(domeneobjekt.getMeldekortene()).hasSize(1);
        var meldekortUtbetalingsgrunnlagMeldekortDto = meldekortUtbetalingsgrunnlagSakDto.meldekortene().get(0);
        var meldekortDomene = domeneobjekt.getMeldekortene().get(0);
        assertThat(meldekortUtbetalingsgrunnlagMeldekortDto.meldekortFom()).isEqualTo(meldekortDomene.getMeldekortFom());
        assertThat(meldekortUtbetalingsgrunnlagMeldekortDto.meldekortTom()).isEqualTo(meldekortDomene.getMeldekortTom());
        assertThat(meldekortUtbetalingsgrunnlagMeldekortDto.dagsats()).isEqualTo(meldekortDomene.getDagsats());
        assertThat(meldekortUtbetalingsgrunnlagMeldekortDto.beløp()).isEqualTo(meldekortDomene.getBeløp());
        assertThat(meldekortUtbetalingsgrunnlagMeldekortDto.utbetalingsgrad()).isEqualTo(meldekortDomene.getUtbetalingsgrad());

        assertThat(tilType(meldekortUtbetalingsgrunnlagSakDto.type())).isEqualTo(domeneobjekt.getYtelseType());
        assertThat(tilTilstand(meldekortUtbetalingsgrunnlagSakDto.tilstand())).isEqualTo(domeneobjekt.getYtelseTilstand());
        assertThat(tilKilde(meldekortUtbetalingsgrunnlagSakDto.kilde())).isEqualTo(domeneobjekt.getKilde());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.saksnummer()).isEqualTo(domeneobjekt.getSaksnummer().getVerdi());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.sakStatus()).isEqualTo(domeneobjekt.getSakStatus());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.vedtakStatus()).isEqualTo(domeneobjekt.getVedtakStatus());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.kravMottattDato()).isEqualTo(domeneobjekt.getKravMottattDato());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.vedtattDato()).isEqualTo(domeneobjekt.getVedtattDato());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeFom()).isEqualTo(domeneobjekt.getVedtaksPeriodeFom());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.vedtaksPeriodeTom()).isEqualTo(domeneobjekt.getVedtaksPeriodeTom());
        assertThat(meldekortUtbetalingsgrunnlagSakDto.vedtaksDagsats().verdi()).isEqualTo(domeneobjekt.getVedtaksDagsats().getVerdi());
    }
}
