package no.nav.foreldrepenger.abakus.iay.tjeneste.migrering;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;


public class DatoTidkonverteringTest {

    private static final LocalDate VINTERDAG = LocalDate.of(2019, 02, 17);
    private static final LocalTime NOON = LocalTime.of(12, 00);

    @Test
    public void skal_konvertere_localdatetime_til_zulu_og_tilbake() throws Exception {
        var ts1 = LocalDateTime.of(VINTERDAG, NOON);
        System.out.println(ts1);
        // til offset
        var zulu0 = ZonedDateTime.of(ts1, ZoneId.systemDefault()).toOffsetDateTime();
        System.out.println(zulu0);

        // til offset - skal gi samme som over
        System.out.println(ts1.atZone(ZoneId.systemDefault()).toOffsetDateTime());

        // til zulu
        var zulu1 = zulu0.atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime();
        System.out.println(zulu1);


        // zulu til localdatetime
        var ts1_1 = zulu1.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println(ts1_1);

    }

}
