package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

import static no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys.BEHANDLING_STATUS;
import static no.nav.vedtak.sikkerhet.abac.pdp.ForeldrepengerDataKeys.FAGSAK_STATUS;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
public class PdpRequestBuilderImpl implements PdpRequestBuilder {
    private static final String ABAC_DOMAIN = "foreldrepenger";
    @Override
    public String abacDomene() {
        return ABAC_DOMAIN;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        return AppRessursData.builder()
            .leggTilAktørIdSet(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilFødselsnumre(dataAttributter.getVerdier(StandardAbacAttributtType.FNR))
            // TODO: Hente fra pip-tjenesten? arv fra tidligere... men nå er 2 pips aktuelle ....
            .leggTilRessurs(FAGSAK_STATUS, PipFagsakStatus.UNDER_BEHANDLING)
            .leggTilRessurs(BEHANDLING_STATUS, PipBehandlingStatus.UTREDES)
            .build();
    }
}
