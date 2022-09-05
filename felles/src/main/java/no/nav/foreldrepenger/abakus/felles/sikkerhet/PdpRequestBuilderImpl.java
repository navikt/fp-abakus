package no.nav.foreldrepenger.abakus.felles.sikkerhet;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pdp.BehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pdp.FagsakStatus;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "duplo";

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(AbacAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, AbacResourceMapper.mapResource(attributter.getResource()));
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID));
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_PERSON_FNR, attributter.getVerdier(StandardAbacAttributtType.FNR));
        // TODO: Hente fra pip-tjenesten?
        pdpRequest.put(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, AbacBehandlingStatus.UTREDES.getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
        return pdpRequest;
    }

    @Override
    public String abacDomene() {
        return ABAC_DOMAIN;
    }

    @Override
    public boolean nyttAbacGrensesnitt() {
        return true;
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        return AppRessursData.builder()
            .leggTilAktørIdSet(dataAttributter.getVerdier(StandardAbacAttributtType.AKTØR_ID))
            .leggTilFødselsnumre(dataAttributter.getVerdier(StandardAbacAttributtType.FNR))
            // TODO: Hente fra pip-tjenesten? arv fra tidligere... men nå er 2 pips aktuelle ....
            .medFagsakStatus(FagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(BehandlingStatus.UTREDES)
            .build();
    }
}
