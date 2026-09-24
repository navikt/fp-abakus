package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.foreldrepenger.abakus.felles.AppAbacAttributtType;
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

        Set<String> saksnumre = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        Set<UUID> koblingReferanser = dataAttributter.getVerdier(AppAbacAttributtType.KOBLING_REFERANSE);

        return standardbuilder(saksnumre, koblingReferanser).build();
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        Set<String> saksnumre = dataAttributter.getVerdier(StandardAbacAttributtType.SAKSNUMMER);
        Set<UUID> koblingReferanser = dataAttributter.getVerdier(AppAbacAttributtType.KOBLING_REFERANSE);

        var builder = standardbuilder(saksnumre, koblingReferanser)
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilIdenter(dataAttributter.getVerdier(StandardAbacAttributtType.FNR));
        return builder.build();
    }

    private AppRessursData.Builder standardbuilder(Set<String> saksnumre, Set<UUID> koblingReferanser) {
        var builder = AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
        saksnumre.stream().findFirst().ifPresent(builder::medSaksnummer);
        saksnumre.stream().findFirst().ifPresent(builder::medLoggSaksnummer);
        koblingReferanser.stream().findFirst().ifPresent(builder::medLoggBehandling);
        return builder;
    }
}
