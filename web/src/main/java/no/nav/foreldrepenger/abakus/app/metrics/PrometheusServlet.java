package no.nav.foreldrepenger.abakus.app.metrics;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.codahale.metrics.MetricRegistry;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;

@ApplicationScoped
public class PrometheusServlet extends MetricsServlet {

    private transient MetricRegistry registry; // NOSONAR

    @Override
    public void init(ServletConfig config) throws ServletException {
        // Hook the Dropwizard registry into the Prometheus registry
        // via the DropwizardExports collector.
        CollectorRegistry.defaultRegistry.register(new DropwizardExports(registry));
    }

    @Inject
    public void setRegistry(MetricRegistry registry) {
        this.registry = registry; // NOSONAR
    }

}
