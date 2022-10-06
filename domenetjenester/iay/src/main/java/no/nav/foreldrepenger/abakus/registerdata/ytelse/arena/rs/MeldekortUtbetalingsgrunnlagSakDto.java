package no.nav.foreldrepenger.abakus.registerdata.ytelse.arena.rs;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import no.nav.abakus.iaygrunnlag.kodeverk.Fagsystem;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseStatus;
import no.nav.abakus.iaygrunnlag.kodeverk.YtelseType;


public record MeldekortUtbetalingsgrunnlagSakDto(Fagsystem kilde,
                                                 LocalDate kravMottattDato,
                                                 List<MeldekortUtbetalingsgrunnlagMeldekortDto> meldekortene,
                                                 String sakStatus,
                                                 String saksnummer,
                                                 YtelseStatus tilstand,
                                                 YtelseType type,
                                                 String vedtakStatus,
                                                 BeløpDto vedtaksDagsats,
                                                 LocalDate vedtaksPeriodeFom,
                                                 LocalDate vedtaksPeriodeTom,
                                                 LocalDate vedtattDato) {

    public MeldekortUtbetalingsgrunnlagSakDto {
        meldekortene = Optional.ofNullable(meldekortene).orElse(Collections.emptyList());
    }
}
