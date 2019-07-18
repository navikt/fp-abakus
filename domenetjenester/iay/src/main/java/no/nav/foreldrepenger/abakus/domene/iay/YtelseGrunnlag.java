package no.nav.foreldrepenger.abakus.domene.iay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.abakus.domene.iay.kodeverk.Arbeidskategori;
import no.nav.foreldrepenger.abakus.typer.Beløp;
import no.nav.foreldrepenger.abakus.typer.Stillingsprosent;


public interface YtelseGrunnlag {

    Optional<Arbeidskategori> getArbeidskategori();

    Optional<Stillingsprosent> getDekningsgradProsent();

    Optional<Stillingsprosent> getGraderingProsent();

    Optional<Stillingsprosent> getInntektsgrunnlagProsent();

    Optional<LocalDate> getOpprinneligIdentdato();

    List<YtelseStørrelse> getYtelseStørrelse();

    Optional<Beløp> getVedtaksDagsats();
}
