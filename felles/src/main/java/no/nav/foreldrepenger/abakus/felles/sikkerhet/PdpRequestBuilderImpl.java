package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    @Override
    public AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        return minimalbuilder().build();
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {

        var builder = minimalbuilder()
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.FNR));
        // TODO legg på saksnummer relevante steder (mange requests har saksnummer). Send saksnummer hvis finnes, ellers ident
        // Ta med denne dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER).stream().findFirst().map(String::valueOf).ifPresent(builder::medSaksnummer);
        return builder.build();
    }

    private AppRessursData.Builder minimalbuilder() {
        return AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
    }
}
