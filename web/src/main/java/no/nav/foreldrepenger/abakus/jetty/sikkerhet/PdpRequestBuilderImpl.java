package no.nav.foreldrepenger.abakus.jetty.sikkerhet;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.AbacFagsakStatus;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */
@ApplicationScoped
@Alternative
@Priority(2)
// HACK - FORDI DENNE KOLLIDERER MED DUMMYREQUESTBUILDER FRA FELLES-SIKKERHET-TESTUTILITIES NÅR VI KJØRER JETTY
public class PdpRequestBuilderImpl implements PdpRequestBuilder {

    public static final String ABAC_DOMAIN = "foreldrepenger";

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(AbacAttributter.XACML_1_0_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource().getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, attributter.getVerdier(StandardAbacAttributtType.AKTØR_ID));
        pdpRequest.put(AbacAttributter.RESOURCE_FELLES_PERSON_FNR, attributter.getVerdier(StandardAbacAttributtType.FNR));
        // TODO: Hente fra pip-tjenesten?
        pdpRequest.put(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, AbacBehandlingStatus.UTREDES.getEksternKode());
        pdpRequest.put(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, AbacFagsakStatus.UNDER_BEHANDLING.getEksternKode());
        return pdpRequest;
    }
}
