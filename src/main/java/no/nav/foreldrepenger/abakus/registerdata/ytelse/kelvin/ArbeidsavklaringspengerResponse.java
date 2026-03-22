package no.nav.foreldrepenger.abakus.registerdata.ytelse.kelvin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * Dagsats og barnetillegg*antallbarn er trekkpliktig og tas med ved overgang til foreldrepenger
 * Dagsats er 66% av beregningsgrunnlag / 260. Justeres gjerne med virkning fra 1/5.
 * Barnetillegg er fast sats pr barn - justeres gjerne 1/1.
 * DagsatsEtterUføreReduksjon skal være dagsats til full utbetaling etter samordning med uføretrygd
 */
public record ArbeidsavklaringspengerResponse(List<AAPVedtak> vedtak) {

    public record AAPVedtak(Integer barnMedStonad, Integer barnetillegg, Integer beregningsgrunnlag,
                            Integer dagsats, Integer dagsatsEtterUføreReduksjon,
                            Kildesystem kildesystem, AAPPeriode periode, String saksnummer, String status,
                            String vedtakId, LocalDate vedtaksdato, List<AAPUtbetaling> utbetaling) { }

    public record AAPPeriode(LocalDate fraOgMedDato, LocalDate tilOgMedDato) {}

    // Dagsats fra kilde = KELVIN er redusert med utbetalingsgrad slik at belop = (dagsats + barnetillegg) * virkedager
    // Barnetillegg her skal være multiplisert med antall barn. Fra kilde KELVIN er den redusert med utbetalingsgrad
    // Ukjent hvordan utbetalingsgrad ser ut i tilfelle UFO-samordning
    public record AAPUtbetaling(AAPPeriode periode, Integer belop, Integer dagsats,
                                Integer barnetillegg, AAPReduksjon reduksjon, Integer utbetalingsgrad) {
    }

    public record AAPReduksjon(BigDecimal annenReduksjon, BigDecimal timerArbeidet) { }

    public enum Kildesystem {
        ARENA, KELVIN
    }

}

