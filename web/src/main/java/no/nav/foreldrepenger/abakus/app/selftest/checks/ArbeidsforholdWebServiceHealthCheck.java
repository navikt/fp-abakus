package no.nav.foreldrepenger.abakus.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.arbeidsforhold.ArbeidsforholdSelftestConsumer;

@ApplicationScoped
public class ArbeidsforholdWebServiceHealthCheck extends WebServiceHealthCheck {

    private ArbeidsforholdSelftestConsumer selftestConsumer;

    ArbeidsforholdWebServiceHealthCheck() {
        // for CDI
    }

    @Inject
    public ArbeidsforholdWebServiceHealthCheck(ArbeidsforholdSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Arbeidsforhold #di_team_registre";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }
}
