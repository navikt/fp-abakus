package no.nav.foreldrepenger.abakus.app.konfig;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;
import no.nav.vedtak.sikkerhet.jaxrs.AuthenticationFilterDelegate;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    private ResourceInfo resourceinfo;

    public AuthenticationFilter() {
        // Ingenting
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) {
        AuthenticationFilterDelegate.fjernKontekst();
    }

    @Override
    public void filter(ContainerRequestContext req) {
        AuthenticationFilterDelegate.validerSettKontekst(resourceinfo, req);
    }

}
