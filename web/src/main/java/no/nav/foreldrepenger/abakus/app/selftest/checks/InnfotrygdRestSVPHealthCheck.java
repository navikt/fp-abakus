package no.nav.foreldrepenger.abakus.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class InnfotrygdRestSVPHealthCheck extends ExtHealthCheck {

    @Inject
    @KonfigVerdi(value = "fpabakus.it.svp.grunnlag.url")
    private String restUrl;  // NOSONAR

    InnfotrygdRestSVPHealthCheck() {
        // for CDI proxy
    }

    @Override
    protected String getDescription() {
        return "Test av rs Infotrygd Svangerskapspenger ";
    }

    @Override
    protected String getEndpoint() {
        return restUrl;
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }

    @Override
    protected InternalResult performCheck() {
        InternalResult intTestRes = new InternalResult();
        // TODO: Finne standard måte å selfteste GET-endepunkt. Legg inn ping i fp-felles?

        intTestRes.noteResponseTime();
        intTestRes.setOk(true);
        return intTestRes;
    }
}
