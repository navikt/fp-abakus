package no.nav.foreldrepenger.abakus.registerdata.ytelse;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import no.nav.foreldrepenger.abakus.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.abakus.registerdata.ytelse.infotrygd.InnhentingInfotrygdTjeneste;
import no.nav.foreldrepenger.abakus.typer.PersonIdent;
import no.nav.vedtak.felles.jpa.tid.DatoIntervallEntitet;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

@RunWith(CdiRunner.class)
public class InfotrygdTjenesteImplTest {
    private static final String FNR = "01234567890";
    private static final LocalDate KONFIG_FOM = LocalDate.of(2018,7,1);

    @Rule
    public RepositoryRule repositoryRule = new UnittestRepositoryRule();

    @Mock
    private InnhentingInfotrygdTjeneste samletTjeneste;

    @Ignore
    @Test
    public void skal_kalle_consumer_og_oversette_response() throws Exception {
        // Arrange
        var response = samletTjeneste.getInfotrygdYtelser(PersonIdent.fra(FNR), DatoIntervallEntitet.fraOgMed(KONFIG_FOM).tilIntervall());
        assertThat(response).isEmpty();
    }

}
