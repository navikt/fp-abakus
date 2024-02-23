package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import no.nav.foreldrepenger.konfig.Cluster;
import no.nav.foreldrepenger.konfig.Environment;
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

    private static final Cluster CLUSTER = Environment.current().getCluster();
    private static final String ABAC_DOMAIN = "k9";
    private static final List<String> INTERNAL_CLUSTER_NAMESPACE = List.of(CLUSTER.clusterName() + ":k9saksbehandling",
        CLUSTER.clusterName() + ":teamforeldrepenger");

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
            .leggTilRessurs(DuploDataKeys.FAGSAK_STATUS, PipFagsakStatus.UNDER_BEHANDLING)
            .leggTilRessurs(DuploDataKeys.BEHANDLING_STATUS, PipBehandlingStatus.UTREDES)
            .build();
    }

    @Override
    public boolean internAzureConsumer(String azpName) {
        return INTERNAL_CLUSTER_NAMESPACE.stream().anyMatch(azpName::startsWith);
    }
}
