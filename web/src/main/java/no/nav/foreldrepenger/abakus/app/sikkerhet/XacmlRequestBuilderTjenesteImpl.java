package no.nav.foreldrepenger.abakus.app.sikkerhet;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

@Dependent
@Alternative
@Priority(2)
public class XacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    public XacmlRequestBuilderTjenesteImpl() {
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(AbacAttributter.XACML_1_0_ACTION_ACTION_ID, pdpRequest.getString(AbacAttributter.XACML_1_0_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        Set<IdentPair> identer = hentIdenter(pdpRequest,
            AbacAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE,
            AbacAttributter.RESOURCE_FELLES_PERSON_FNR);

        if (identer.isEmpty()) {
            populerResources(xacmlBuilder, pdpRequest, null);
        } else {
            for (IdentPair ident : identer) {
                populerResources(xacmlBuilder, pdpRequest, ident);
            }
        }

        return xacmlBuilder;
    }

    private void populerResources(XacmlRequestBuilder xacmlBuilder, PdpRequest pdpRequest, IdentPair ident) {
        xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident));
    }

    private XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, IdentPair ident) {
        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(AbacAttributter.RESOURCE_FELLES_DOMENE, pdpRequest.getString(AbacAttributter.RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getString(AbacAttributter.RESOURCE_FELLES_RESOURCE_TYPE));
        resourceAttributeSet.addAttribute(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS, pdpRequest.getString(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_SAKSSTATUS));
        resourceAttributeSet.addAttribute(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS, pdpRequest.getString(AbacAttributter.RESOURCE_FORELDREPENGER_SAK_BEHANDLINGSSTATUS));
        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.key(), ident.it());
        }

        return resourceAttributeSet;
    }

    private Set<IdentPair> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        Set<IdentPair> identer = new HashSet<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream().map(it -> new IdentPair(key, it)).collect(Collectors.toList()));
        }
        return identer;
    }

    private static record IdentPair(String key, String it) {}
}
