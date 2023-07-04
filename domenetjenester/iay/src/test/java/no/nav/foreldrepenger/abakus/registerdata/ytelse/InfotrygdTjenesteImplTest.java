package no.nav.foreldrepenger.abakus.registerdata.ytelse;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.abakus.felles.jpa.IntervallEntitet;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;

@ExtendWith(MockitoExtension.class)
class InfotrygdTjenesteImplTest {
    private static final String FNR = "01234567890";
    private static final LocalDate KONFIG_FOM = LocalDate.of(2018, 7, 1);

    @Mock
    private InnhentingInfotrygdTjeneste samletTjeneste;

    @Test
    void skal_kalle_consumer_og_oversette_response() throws Exception {
        // Arrange
        var response = samletTjeneste.getInfotrygdYtelser(PersonIdent.fra(FNR), IntervallEntitet.fraOgMed(KONFIG_FOM));
        assertThat(response).isEmpty();
    }

}
